/*
 * Copyright 2019 Carlos Conyers
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package scamper.server

import java.io.{ Closeable, File, InputStream }
import java.net.{ InetAddress, InetSocketAddress, Socket, SocketTimeoutException, URI, URISyntaxException }
import java.time.Instant
import java.util.concurrent._
import java.util.concurrent.atomic.AtomicLong

import javax.net.ServerSocketFactory
import javax.net.ssl.{ SSLException, SSLServerSocketFactory }

import scala.collection.mutable.ArrayBuffer
import scala.concurrent.{ ExecutionContext, Future }
import scala.util.{ Failure, Success, Try }

import scamper._
import scamper.Auxiliary.SocketType
import scamper.ResponseStatus.Registry._
import scamper.headers.{ Connection, ContentLength, ContentType, Date, TransferEncoding }
import scamper.logging.{ ConsoleLogger, Logger, NullLogger }
import scamper.types.TransferCoding

private object DefaultHttpServer {
  private val count = new AtomicLong(0)

  case class Application(
    poolSize: Int = Runtime.getRuntime.availableProcessors(),
    queueSize: Int = Runtime.getRuntime.availableProcessors() * 4,
    bufferSize: Int = 8192,
    headerSize: Int = Try(sys.props("scamper.server.headerSize").toInt).getOrElse(1024),
    readTimeout: Int = 5000,
    keepAliveSeconds: Int = Try(sys.props("scamper.server.keepAliveSeconds").toInt).getOrElse(60),
    logger: Logger = ConsoleLogger,
    requestHandlers: Seq[RequestHandler] = Nil,
    responseFilters: Seq[ResponseFilter] = Nil,
    errorHandler: Option[ErrorHandler] = None,
    factory: ServerSocketFactory = ServerSocketFactory.getDefault()
  )

  def apply(app: Application, host: InetAddress, port: Int) =
    new DefaultHttpServer(count.incrementAndGet(), app)(host, port)
}

private class DefaultHttpServer private (id: Long, app: DefaultHttpServer.Application)(val host: InetAddress, val port: Int) extends HttpServer {
  val poolSize = app.poolSize.max(1)
  val queueSize = app.queueSize.max(0)
  val headerSize = app.headerSize.max(1024)
  val bufferSize = app.bufferSize.max(1024)
  val readTimeout = app.readTimeout.max(0)
  val keepAliveSeconds = app.keepAliveSeconds.max(0)
  val logger = if (app.logger == null) NullLogger else app.logger

  private val authority = s"${host.getCanonicalHostName}:$port"
  private val threadGroup = new ThreadGroup(s"scamper-server-$id")
  private val requestHandler = RequestHandler.coalesce(app.requestHandlers : _*)
  private val responseFilter = ResponseFilter.chain(app.responseFilters : _*)
  private val errorHandler = app.errorHandler.getOrElse(new ErrorHandler {
    def apply(err: Throwable, req: HttpRequest): HttpResponse = {
      val id = req.getAttribute[String]("scamper.server.message.correlate").getOrElse("<unknown>")
      logger.error(s"$authority - Error while handling request for $id", err)
      InternalServerError()
    }
  })
  private val serverSocket = app.factory.createServerSocket()
  private val chunked = TransferCoding("chunked")
  private val serviceUnavailable = ServiceUnavailable().withHeader(Header("Retry-After", 120))
  private var closed = false

  private val writerContext = ExecutionContext.fromExecutorService {
    val threadFactory = new ThreadFactory {
      private val count = new AtomicLong(0)
      def newThread(task: Runnable) = {
        val thread = new Thread(threadGroup, task, s"scamper-server-$id-writer-${count.incrementAndGet()}")
        thread.setDaemon(true)
        thread
      }
    }

    new ThreadPoolExecutor(
      2,
      poolSize * 2,
      keepAliveSeconds,
      TimeUnit.SECONDS,
      new LinkedBlockingQueue[Runnable](),
      threadFactory
    )
  }

  private val serviceContext = ExecutionContext.fromExecutorService {
    val rejectedExecutionHandler = new RejectedExecutionHandler {
      def rejectedExecution(task: Runnable, executor: ThreadPoolExecutor): Unit = throw new RejectedExecutionException()
    }

    val threadFactory = new ThreadFactory {
      private val count = new AtomicLong(0)
      def newThread(task: Runnable) = {
        val thread = new Thread(threadGroup, task, s"scamper-server-$id-service-${count.incrementAndGet()}")
        thread.setDaemon(true)
        thread
      }
    }

    new ThreadPoolExecutor(
      poolSize,
      poolSize,
      keepAliveSeconds,
      TimeUnit.SECONDS,
      queueSize match {
        case 0 => new SynchronousQueue()
        case _ => new ArrayBlockingQueue[Runnable](queueSize)
      },
      threadFactory,
      rejectedExecutionHandler
    )
  }

  private val closerContext = ExecutionContext.fromExecutorService {
    val threadFactory = new ThreadFactory {
      private val count = new AtomicLong(0)
      def newThread(task: Runnable) = {
        val thread = new Thread(threadGroup, task, s"scamper-server-$id-closer-${count.incrementAndGet()}")
        thread.setDaemon(true)
        thread
      }
    }

    new ThreadPoolExecutor(
      2,
      poolSize.max(2),
      keepAliveSeconds,
      TimeUnit.SECONDS,
      new LinkedBlockingQueue[Runnable](),
      threadFactory
    )
  }

  val isSecure: Boolean = app.factory.isInstanceOf[SSLServerSocketFactory]

  def isClosed: Boolean = synchronized(closed)

  def close(): Unit = synchronized {
    if (!closed) {
      Try(logger.info(s"$authority - Shutting down server"))
      Try(serverSocket.close())
      Try(writerContext.shutdownNow())
      Try(serviceContext.shutdownNow())
      Try(closerContext.shutdownNow())
      Try(logger.asInstanceOf[Closeable].close())
      closed = true
    }
  }

  override def toString(): String = s"HttpServer(host=$host, port=$port, isSecure=$isSecure, isClosed=$isClosed)"

  try {
    logger.info(s"$authority - Starting server")
    logger.info(s"$authority - Secure: $isSecure")
    logger.info(s"$authority - Logger: $logger")
    logger.info(s"$authority - Pool Size: $poolSize")
    logger.info(s"$authority - Queue Size: $queueSize")
    logger.info(s"$authority - Buffer Size: $bufferSize")
    logger.info(s"$authority - Read Timeout: $readTimeout")

    serverSocket.bind(new InetSocketAddress(host, port), queueSize)
    Service.start()

    logger.info(s"$authority - Server is up and running")
  } catch {
    case e: Exception =>
      logger.error(s"$authority - Failed to start server", e)
      close()
      throw e
  }

  private case class ReadError(status: ResponseStatus) extends HttpException(status.reason)

  private object Service extends Thread(threadGroup, s"scamper-server-$id-service") {
    private val requestCount = new AtomicLong(0)
    private def nextCorrelate = Base64.encodeToString(s"${requestCount.incrementAndGet}:${System.currentTimeMillis}")

    override def run(): Unit =
      while (!isClosed)
        try
          service(serverSocket.accept())
        catch {
          case e: Exception if serverSocket.isClosed => close() // Ensure server is closed
          case e: Exception => logger.warn(s"$authority - Error while waiting for connection: $e")
        }

    private def service(implicit socket: Socket): Unit = {
      val connection = socket.getInetAddress.getHostAddress + ":" + socket.getPort
      val correlate = nextCorrelate
      val tag = connection + " (" + correlate + ")"

      logger.info(s"$authority - Connection received from $tag")

      def onReadError: PartialFunction[Throwable, HttpResponse] = {
        case ReadError(status)              => status()
        case err: IllegalArgumentException  => BadRequest()
        case err: IndexOutOfBoundsException => BadRequest()
        case err: SocketTimeoutException    => RequestTimeout()
        case err: ResponseAborted           => throw err
        case err: SSLException              => throw err
        case err =>
          logger.error(s"$authority - Error while reading request from $tag", err)
          InternalServerError()
      }

      def onHandleError(req: HttpRequest): PartialFunction[Throwable, HttpResponse] = {
        case err: SocketTimeoutException => RequestTimeout()
        case err: ResponseAborted        => throw err
        case err: SSLException           => throw err
        case err                         => errorHandler(err, req)
      }

      Future {
        try {
          logger.info(s"$authority - Servicing request from $tag")
          socket.setSoTimeout(readTimeout)

          Try(read())
            .map(req => addAttributes(req, correlate))
            .fold(err => Try(onReadError(err)), req => Try(handle(req)).recover { case err => onHandleError(req)(err) })
            .map(res => addAttributes(res, correlate))
            .map { res =>
              Try(filter(res))
                .map { res =>
                  write(res)
                  logger.info(s"$authority - Response sent to $tag")
                  Try(res.body.getInputStream.close()) // Close filtered response body
                }.recover {
                  case err => logger.error(s"$authority - Error while filtering response to $tag", err)
                }

              Try(res.body.getInputStream.close()) // Close unfiltered response body
            }.get
        } catch {
          case err: ResponseAborted =>
            logger.warn(s"$authority - Response aborted while servicing request from $tag", err)

          case err: SSLException =>
            logger.warn(s"$authority - SSL error while servicing request from $tag", err)

          case err: Exception =>
            logger.error(s"$authority - Unhandled error while servicing request from $tag", err)
        }
      } (serviceContext).onComplete {
        case Success(_) =>
          logger.info(s"$authority - Closing connection to $tag")
          Try(socket.close())

        case Failure(err) =>
          if (err.isInstanceOf[RejectedExecutionException]) {
            logger.warn(s"$authority - Server overflow while servicing request from $tag", err)
            Try(addAttributes(serviceUnavailable, correlate))
              .map(filter)
              .map(write)
              .map(_ => logger.info(s"$authority - (Server Overflow Mode) Response sent to $tag"))
              .recover {
                case err => logger.error(s"$authority - (Server Overflow Mode) Error while filtering response to $tag", err)
              }
            logger.info(s"$authority - Closing connection to $tag")
          }
          Try(socket.close())
      } (closerContext)
    }

    private def read()(implicit socket: Socket): HttpRequest = {
      val buffer = new Array[Byte](bufferSize)
      val method = readMethod(buffer)
      val target = readTarget(buffer)
      val version = readVersion(buffer)
      val startLine = RequestLine(method, target, version)
      val headers = readHeaders(buffer)

      HttpRequest(startLine, headers, Entity.fromInputStream(socket.getInputStream))
    }

    private def readMethod(buffer: Array[Byte])(implicit socket: Socket): RequestMethod =
      RequestMethod(socket.getToken(" ", buffer))

    private def readTarget(buffer: Array[Byte])(implicit socket: Socket): URI =
      try
        new URI(socket.getToken(" ", buffer))
      catch {
        case _: IndexOutOfBoundsException => throw ReadError(UriTooLong)
        case _: URISyntaxException        => throw ReadError(BadRequest)
      }

    private def readVersion(buffer: Array[Byte])(implicit socket: Socket): HttpVersion = {
      val regex = "HTTP/(.+)".r

      socket.getLine(buffer) match {
        case regex(version) => HttpVersion.parse(version)
        case _ => throw ReadError(BadRequest)
      }
    }

    private def readHeaders(buffer: Array[Byte])(implicit socket: Socket): Seq[Header] = {
      val headers = new ArrayBuffer[Header]
      val readLimit = headerSize * bufferSize
      var readSize = 0
      var line = ""

      try {
        while ({ line = socket.getLine(buffer); line != "" }) {
          readSize += line.size

          if (readSize <= readLimit)
            line.matches("[ \t]+.*") match {
              case true =>
                if (headers.isEmpty) throw ReadError(BadRequest)
                val last = headers.last
                headers.update(headers.size - 1, Header(last.name, last.value + " " + line.trim()))
              case false =>
                if (headers.size < headerSize)
                  headers += Header.parse(line)
                else
                  throw ReadError(RequestHeaderFieldsTooLarge)
            }
          else
            throw ReadError(RequestHeaderFieldsTooLarge)
        }
      } catch {
        case _: IndexOutOfBoundsException => throw ReadError(RequestHeaderFieldsTooLarge)
      }

      headers.toSeq
    }

    private def write(res: HttpResponse)(implicit socket: Socket): Unit = {
      socket.shutdownInput()
      socket.writeLine(res.startLine.toString)
      res.headers.map(_.toString).foreach(socket.writeLine)
      socket.writeLine()

      if (!res.body.isKnownEmpty) {
        val buffer = new Array[Byte](bufferSize)
        var length = 0

        res.getTransferEncoding.map { encoding =>
          val in = encode(res.body.getInputStream, encoding)
          while ({ length = in.read(buffer); length != -1 }) {
            socket.writeLine(length.toHexString)
            socket.write(buffer, 0, length)
            socket.writeLine()
          }
          socket.writeLine("0")
          socket.writeLine()
        }.getOrElse {
          val in = res.body.getInputStream
          while ({ length = in.read(buffer); length != -1 })
            socket.write(buffer, 0, length)
        }
      }

      socket.flush()
    }

    private def encode(in: InputStream, encoding: Seq[TransferCoding]): InputStream =
      encoding.foldLeft(in) { (in, enc) =>
        if (enc.isChunked) in
        else if (enc.isGzip) Compressor.gzip(in, bufferSize)(writerContext)
        else if (enc.isDeflate) Compressor.deflate(in, bufferSize)(writerContext)
        else throw new HttpException(s"Unsupported transfer encoding: $enc")
      }

    private def handle(req: HttpRequest): HttpResponse =
      requestHandler(req) match {
        case req: HttpRequest  => NotFound()
        case res: HttpResponse => res
      }

    private def filter(res: HttpResponse): HttpResponse =
      responseFilter(prepare(res).withDate(Instant.now).withConnection("close"))

    private def prepare(res: HttpResponse): HttpResponse =
      if (res.hasTransferEncoding)
        res.withTransferEncoding(res.transferEncoding.filterNot(_.isChunked) :+ chunked : _*).removeContentLength
      else if (res.hasContentLength)
        res
      else
        res.body.getLength match {
          case Some(0) => res.getContentType.map(_ => res.withContentLength(0)).getOrElse(res)
          case Some(n) => res.withContentLength(n)
          case None    => res.withTransferEncoding(chunked)
        }

    private def addAttributes(req: HttpRequest, id: String)(implicit socket: Socket): HttpRequest =
      req.withAttributes("scamper.server.message.socket" -> socket, "scamper.server.message.correlate" -> id)

    private def addAttributes(res: HttpResponse, id: String)(implicit socket: Socket): HttpResponse =
      res.withAttributes("scamper.server.message.socket" -> socket, "scamper.server.message.correlate" -> id)
  }
}
