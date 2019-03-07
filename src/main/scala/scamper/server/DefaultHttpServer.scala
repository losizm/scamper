/*
 * Copyright 2018 Carlos Conyers
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

import java.io.{ File, FileWriter, PrintWriter }
import java.net.{ InetAddress, InetSocketAddress, Socket, SocketTimeoutException, URI, URISyntaxException }
import java.time.Instant
import java.util.concurrent.{ ArrayBlockingQueue, RejectedExecutionHandler, TimeUnit, ThreadFactory, ThreadPoolExecutor }
import java.util.concurrent.atomic.AtomicLong

import javax.net.ServerSocketFactory
import javax.net.ssl.{ SSLException, SSLServerSocketFactory }

import scala.collection.mutable.ArrayBuffer
import scala.concurrent.{ ExecutionContext, Future }
import scala.util.{ Failure, Success, Try }

import scamper.{ Base64, Entity, Header, HttpException, HttpRequest, HttpResponse, HttpVersion, RequestLine, RequestMethod, ResponseStatus }
import scamper.Auxiliary.SocketType
import scamper.ResponseStatuses.{ BadRequest, InternalServerError, NotFound, RequestTimeout, UriTooLong, RequestHeaderFieldsTooLarge }
import scamper.headers.{ Connection, ContentLength, ContentType, Date, TransferEncoding }
import scamper.types.ImplicitConverters.stringToTransferCoding

private object DefaultHttpServer {
  private val count = new AtomicLong(0)

  case class Application(
    poolSize: Int = Runtime.getRuntime.availableProcessors(),
    queueSize: Int = Runtime.getRuntime.availableProcessors() * 4,
    bufferSize: Int = 8192,
    headerSize: Int = Try(sys.props("scamper.server.headerSize").toInt).getOrElse(1024),
    readTimeout: Int = 5000,
    keepAliveSeconds: Int = Try(sys.props("scamper.server.keepAliveSeconds").toInt).getOrElse(60),
    log: File = new File("server.log").getCanonicalFile(),
    requestHandlers: Seq[RequestHandler] = Nil,
    responseFilters: Seq[ResponseFilter] = Nil,
    errorHandler: Option[ErrorHandler] = None,
    factory: ServerSocketFactory = ServerSocketFactory.getDefault()
  )

  def apply(app: Application, host: InetAddress, port: Int) =
    new DefaultHttpServer(count.incrementAndGet(), app)(host, port)
}

private class DefaultHttpServer private (id: Long, app: DefaultHttpServer.Application)(val host: InetAddress, val port: Int) extends HttpServer {
  val poolSize = app.poolSize
  val queueSize = app.queueSize
  val headerSize = app.headerSize
  val bufferSize = app.bufferSize.max(1024)
  val readTimeout = app.readTimeout
  val keepAliveSeconds = app.keepAliveSeconds
  val log = app.log

  private val authority = s"${host.getCanonicalHostName}:$port"
  private val threadGroup = new ThreadGroup(s"httpserver-$id")
  private val requestHandler = RequestHandler.coalesce(app.requestHandlers : _*)
  private val responseFilter = ResponseFilter.chain(app.responseFilters : _*)
  private val errorHandler = app.errorHandler.getOrElse(new ErrorHandler {
    def apply(err: Throwable, req: HttpRequest): HttpResponse = {
      val id = req.getAttribute[String]("scamper.server.transactionId").getOrElse("<unknown>")
      logMessage(s"[error] Error while handling request for $id", Some(err))
      InternalServerError()
    }
  })
  private val logWriter = new PrintWriter(new FileWriter(log, true), true)
  private val serverSocket = app.factory.createServerSocket()
  private var closed = false

  private implicit val executor = ExecutionContext.fromExecutorService {
    object ServiceUnavailableHandler extends RejectedExecutionHandler {
      def rejectedExecution(task: Runnable, executor: ThreadPoolExecutor): Unit = logMessage("[error] Task rejected")
    }

    object ServiceThreadFactory extends ThreadFactory {
      private val count = new AtomicLong(0)
      def newThread(task: Runnable) = new Thread(threadGroup, task, s"httpserver-$id-service-${count.incrementAndGet()}")
    }

    new ThreadPoolExecutor(
      poolSize,
      poolSize,
      keepAliveSeconds,
      TimeUnit.SECONDS,
      new ArrayBlockingQueue[Runnable](queueSize),
      ServiceThreadFactory,
      ServiceUnavailableHandler
    )
  }

  val isSecure: Boolean = app.factory.isInstanceOf[SSLServerSocketFactory]

  def isClosed: Boolean = synchronized(closed)

  def close(): Unit = synchronized {
    if (!closed) {
      Try(logMessage("[info] Shutting down server"))
      Try(serverSocket.close())
      Try(executor.shutdownNow())
      Try(logWriter.close())
      closed = true
    }
  }

  override def toString(): String = s"HttpServer(host=$host, port=$port, isSecure=$isSecure, isClosed=$isClosed)"

  try {
    logMessage(s"[info] Starting server at $authority")
    logMessage(s"[info] Secure: $isSecure")
    logMessage(s"[info] Log: $log")
    logMessage(s"[info] Pool Size: $poolSize")
    logMessage(s"[info] Queue Size: $queueSize")
    logMessage(s"[info] Read Timeout: $readTimeout")

    serverSocket.bind(new InetSocketAddress(host, port), queueSize)
    Service.start()

    logMessage(s"[info] Server running at $authority")
  } catch {
    case e: Exception =>
      logMessage(s"[error] Failed to start server at $authority", Some(e))
      close()
      throw e
  }

  private def logMessage(message: String, error: Option[Throwable] = None): Unit = {
    logWriter.printf("%s [%s]%s%n", Instant.now(), authority, message)
    error.foreach(_.printStackTrace(logWriter))
  }

  private case class ReadError(status: ResponseStatus) extends HttpException(status.reason)

  private object Service extends Thread(threadGroup, s"httpserver-$id-service") {
    private val transactionCount = new AtomicLong(0)
    private def nextTransactionId = Base64.encodeToString(transactionCount.incrementAndGet + ":" + System.currentTimeMillis)

    override def run(): Unit =
      while (!isClosed)
        try
          service(serverSocket.accept())
        catch {
          case e: Exception if serverSocket.isClosed => close() // Ensure server is closed
          case e: Exception => logMessage(s"[warning] Error while waiting for connection: $e", Some(e))
        }

    private def service(implicit socket: Socket): Unit = {
      val connection = socket.getInetAddress.getHostAddress + ":" + socket.getPort
      logMessage(s"[info] Connection received from $connection")

      def onReadError: PartialFunction[Throwable, HttpResponse] = {
        case ReadError(status)              => status()
        case err: IllegalArgumentException  => BadRequest()
        case err: IndexOutOfBoundsException => BadRequest()
        case err: SocketTimeoutException    => RequestTimeout()
        case err: ResponseAborted           => throw err
        case err: SSLException              => throw err
        case err =>
          logMessage(s"[error] Error while reading request from $connection", Some(err))
          InternalServerError()
      }

      def onHandleError(req: HttpRequest): PartialFunction[Throwable, HttpResponse] = {
        case err: SocketTimeoutException => RequestTimeout()
        case err: ResponseAborted        => throw err
        case err: SSLException           => throw err
        case err                         => errorHandler(err, req)
      }

      Future {
        val transactionId = nextTransactionId

        try {
          logMessage(s"[info] Servicing request from $connection as $transactionId")
          socket.setSoTimeout(readTimeout)

          Try(read())
            .map(req => addAttributes(req, transactionId))
            .fold(err => Try(onReadError(err)), req => Try(handle(req)).recover { case err => onHandleError(req)(err) })
            .map(res => addAttributes(res, transactionId))
            .map { res =>
              Try(filter(res))
                .map { res =>
                  write(res)
                  logMessage(s"[info] Response sent for $transactionId")
                  Try(res.body.getInputStream.close()) // Close filtered response body
                }.recover {
                  case err => logMessage(s"[error] Error while filtering response for $transactionId", Some(err))
                }

              Try(res.body.getInputStream.close()) // Close unfiltered response body
            }.get
        } catch {
          case err: ResponseAborted =>
            logMessage(s"[warn] Response aborted while servicing request for $transactionId", Some(err))

          case err: SSLException =>
            logMessage(s"[warn] SSL error while servicing request for $transactionId", Some(err))

          case err: Throwable =>
            logMessage(s"[error] Fatal error while servicing request for $transactionId", Some(err))
            throw err
        } finally {
          logMessage(s"[info] Closing connection to $transactionId")
          Try(socket.close())
        }
      }
    }

    private def read()(implicit socket: Socket): HttpRequest = {
      val buffer = new Array[Byte](bufferSize)
      val method = readMethod(buffer)
      val target = readTarget(buffer)
      val version = readVersion(buffer)
      val startLine = RequestLine(method, target, version)
      val headers = readHeaders(buffer)

      HttpRequest(startLine, headers, Entity(socket.getInputStream))
    }

    private def readMethod(buffer: Array[Byte])(implicit socket: Socket): RequestMethod =
      RequestMethod(socket.readToken(" ", buffer))

    private def readTarget(buffer: Array[Byte])(implicit socket: Socket): URI =
      try
        new URI(socket.readToken(" ", buffer))
      catch {
        case _: IndexOutOfBoundsException => throw ReadError(UriTooLong)
        case _: URISyntaxException        => throw ReadError(BadRequest)
      }

    private def readVersion(buffer: Array[Byte])(implicit socket: Socket): HttpVersion = {
      val regex = "HTTP/(.+)".r

      socket.readLine(buffer) match {
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
        while ({ line = socket.readLine(buffer); line != "" }) {
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
      socket.writeLine(res.startLine.toString)
      res.headers.map(_.toString).foreach(socket.writeLine)
      socket.writeLine()

      if (!res.body.isKnownEmpty) {
        val in = res.body.getInputStream
        val buffer = new Array[Byte](bufferSize)
        var length = 0

        res.getTransferEncoding.map { _ =>
          while ({ length = in.read(buffer); length != -1 }) {
            socket.writeLine(length.toHexString)
            socket.write(buffer, 0, length)
            socket.writeLine()
          }

          socket.writeLine("0")
          socket.writeLine()
        }.getOrElse {
          while ({ length = in.read(buffer); length != -1 })
            socket.write(buffer, 0, length)
        }
      }

      socket.flush()
    }

    private def handle(req: HttpRequest): HttpResponse =
      requestHandler(req).getOrElse(NotFound())

    private def filter(res: HttpResponse): HttpResponse =
      responseFilter(prepare(res).withDate(Instant.now).withConnection("close"))

    private def prepare(res: HttpResponse): HttpResponse =
      if (res.hasTransferEncoding || res.hasContentLength)
        res
      else
        res.body.getLength match {
          case Some(0) => res.getContentType.map(_ => res.withContentLength(0)).getOrElse(res)
          case Some(n) => res.withContentLength(n)
          case None    => res.withTransferEncoding("chunked")
        }

    private def addAttributes(req: HttpRequest, id: String)(implicit socket: Socket): HttpRequest =
      req.withAttributes("scamper.server.socket" -> socket, "scamper.server.transactionId" -> id)

    private def addAttributes(res: HttpResponse, id: String)(implicit socket: Socket): HttpResponse =
      res.withAttributes("scamper.server.socket" -> socket, "scamper.server.transactionId" -> id)
  }
}
