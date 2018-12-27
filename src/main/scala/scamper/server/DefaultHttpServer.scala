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
import java.util.concurrent.atomic.AtomicInteger

import javax.net.ServerSocketFactory
import javax.net.ssl.{ SSLException, SSLServerSocketFactory }

import scala.collection.mutable.ArrayBuffer
import scala.concurrent.{ ExecutionContext, Future }
import scala.util.{ Failure, Success, Try }

import scamper.{ Entity, Header, HttpException, HttpRequest, HttpResponse, HttpVersion, RequestLine, RequestMethod, ResponseStatus }
import scamper.ResponseStatuses.{ BadRequest, InternalServerError, NotFound, RequestTimeout, UriTooLong, RequestHeaderFieldsTooLarge }
import scamper.auxiliary.SocketType
import scamper.headers.{ Connection, ContentLength, ContentType, Date, TransferEncoding }
import scamper.types.ImplicitConverters.stringToTransferCoding

private object DefaultHttpServer {
  private val count = new AtomicInteger(0)

  case class Application(
    poolSize: Int = Runtime.getRuntime.availableProcessors(),
    queueSize: Int = Runtime.getRuntime.availableProcessors() * 4,
    bufferSize: Int = Try(sys.props("scamper.server.bufferSize").toInt).getOrElse(8192),
    headerSize: Int = Try(sys.props("scamper.server.headerSize").toInt).getOrElse(1024),
    readTimeout: Int = 5000,
    keepAliveSeconds: Int = Try(sys.props("scamper.server.keepAliveSeconds").toInt).getOrElse(60),
    log: File = new File("server.log").getCanonicalFile(),
    requestHandlers: Seq[RequestHandler] = Nil,
    responseFilters: Seq[ResponseFilter] = Nil,
    factory: ServerSocketFactory = ServerSocketFactory.getDefault()
  )

  def apply(app: Application, host: InetAddress, port: Int) =
    new DefaultHttpServer(count.incrementAndGet(), app)(host, port)
}

private class DefaultHttpServer private (id: Int, app: DefaultHttpServer.Application)(val host: InetAddress, val port: Int) extends HttpServer {
  val poolSize = app.poolSize
  val queueSize = app.queueSize
  val headerSize = app.headerSize
  val bufferSize = app.bufferSize
  val readTimeout = app.readTimeout
  val keepAliveSeconds = app.keepAliveSeconds
  val log = app.log

  private val authority = s"${host.getCanonicalHostName}:$port"
  private val threadGroup = new ThreadGroup(s"httpserver-$id")
  private val requestHandler = RequestHandler.coalesce(app.requestHandlers : _*)
  private val responseFilter = ResponseFilter.chain(app.responseFilters : _*)
  private val logger = new PrintWriter(new FileWriter(app.log, true), true)
  private val serverSocket = app.factory.createServerSocket()
  private var closed = false

  private implicit val executor = ExecutionContext.fromExecutorService {
    object ServiceUnavailableHandler extends RejectedExecutionHandler {
      def rejectedExecution(task: Runnable, executor: ThreadPoolExecutor): Unit = log("[error] Task rejected")
    }

    object ServiceThreadFactory extends ThreadFactory {
      private val count = new AtomicInteger(0)
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
      Try(log("[info] Shutting down server"))
      Try(serverSocket.close())
      Try(executor.shutdownNow())
      Try(logger.close())
      closed = true
    }
  }

  override def toString(): String = s"HttpServer(host=$host, port=$port, isSecure=$isSecure, isClosed=$isClosed)"

  try {
    log(s"[info] Starting server at $authority")
    log(s"[info] Secure: $isSecure")
    log(s"[info] Log: $log")
    log(s"[info] Pool Size: $poolSize")
    log(s"[info] Queue Size: $queueSize")
    log(s"[info] Read Timeout: $readTimeout")

    serverSocket.bind(new InetSocketAddress(host, port), queueSize)
    Service.start()

    log(s"[info] Server running at $authority")
  } catch {
    case e: Exception =>
      log(s"[error] Failed to start server at $authority", Some(e))
      close()
      throw e
  }

  private def log(message: String, error: Option[Throwable] = None): Unit = {
    logger.printf("%s [%s]%s%n", Instant.now(), authority, message)
    error.foreach(_.printStackTrace(logger))
  }

  private case class ReadError(status: ResponseStatus) extends HttpException(status.reason)

  private object Service extends Thread(threadGroup, s"httpserver-$id-service") {
    override def run(): Unit =
      while (!isClosed)
        try
          service(serverSocket.accept())
        catch {
          case e: Exception if serverSocket.isClosed => close() // Ensure server is closed
          case e: Exception => log(s"[warning] Error while waiting for connection: $e", Some(e))
        }

    private def service(implicit socket: Socket): Unit = {
      val connection = socket.getInetAddress.getHostAddress + ":" + socket.getPort
      log(s"[info] Connection received from $connection")

      def onReadError: PartialFunction[Throwable, HttpResponse] = {
        case ReadError(status)              => status()
        case err: IllegalArgumentException  => BadRequest()
        case err: IndexOutOfBoundsException => BadRequest()
        case err: SocketTimeoutException    => RequestTimeout()
        case err: ResponseAborted           => throw err
        case err: SSLException              => throw err
        case err =>
          log(s"[error] Error while reading request from $connection", Some(err))
          InternalServerError()
      }

      def onHandleError: PartialFunction[Throwable, HttpResponse] = {
        case err: SocketTimeoutException => RequestTimeout()
        case err: ResponseAborted        => throw err
        case err: SSLException           => throw err
        case err =>
          log(s"[error] Error while handling request from $connection", Some(err))
          InternalServerError()
      }

      Future {
        try {
          log(s"[info] Servicing request from $connection")
          socket.setSoTimeout(readTimeout)

          Try(read())
            .fold(err => Try(onReadError(err)), req => Try(handle(req)))
            .recover(onHandleError)
            .map { res =>
              Try(filter(res))
                .map { res =>
                  write(res)
                  log(s"[info] Response sent to $connection")
                  Try(res.body.getInputStream.close()) // Close filtered response body
                }.recover {
                  case err => log(s"[error] Error while filtering response to $connection", Some(err))
                }

              Try(res.body.getInputStream.close()) // Close unfiltered response body
            }.get
        } catch {
          case err: ResponseAborted =>
            log(s"[warn] Response aborted while servicing request from $connection", Some(err))

          case err: SSLException =>
            log(s"[warn] SSL error while servicing request from $connection", Some(err))

          case err: Throwable =>
            log(s"[error] Fatal error while servicing request from $connection", Some(err))
            throw err
        } finally {
          log(s"[info] Closing connection to $connection")
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
  }
}
