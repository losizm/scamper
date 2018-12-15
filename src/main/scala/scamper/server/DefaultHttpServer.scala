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
import java.net.{ InetAddress, InetSocketAddress, Socket, SocketTimeoutException }
import java.time.Instant
import java.util.concurrent.{ ArrayBlockingQueue, RejectedExecutionHandler, TimeUnit, ThreadFactory, ThreadPoolExecutor }
import java.util.concurrent.atomic.AtomicInteger

import javax.net.ServerSocketFactory
import javax.net.ssl.SSLServerSocketFactory

import scala.collection.mutable.ArrayBuffer
import scala.concurrent.{ ExecutionContext, Future }
import scala.util.Try

import scamper.{ Header, HttpRequest, HttpResponse, RequestLine }
import scamper.ImplicitConverters.inputStreamToEntity
import scamper.ResponseStatuses.{ NotFound, RequestTimeout }
import scamper.auxiliary.SocketType
import scamper.headers.{ Connection, ContentLength, ContentType, Date, TransferEncoding }
import scamper.types.ImplicitConverters.stringToTransferCoding

private object DefaultHttpServer {
  private val count = new AtomicInteger(0)

  case class Application(
    poolSize: Int = Runtime.getRuntime.availableProcessors(),
    queueSize: Int = Runtime.getRuntime.availableProcessors() * 4,
    readTimeout: Int = 5000,
    keepAliveSeconds: Int = 60,
    log: File = new File("server.log").getCanonicalFile(),
    requestHandlers: Seq[RequestHandler] = Nil,
    responseFilters: Seq[ResponseFilter] = Nil,
    factory: ServerSocketFactory = ServerSocketFactory.getDefault()
  )

  def apply(host: InetAddress, port: Int, app: Application) =
    new DefaultHttpServer(count.incrementAndGet(), host, port, app)
}

private class DefaultHttpServer private(val id: Int, val host: InetAddress, val port: Int, app: DefaultHttpServer.Application) extends HttpServer {
  val readTimeout = app.readTimeout
  val poolSize = app.poolSize
  val queueSize = app.queueSize
  val log = app.log

  private val authority = s"${host.getCanonicalHostName}:$port"
  private val threadGroup = new ThreadGroup(s"httpserver-$id")

  private val keepAliveSeconds = app.keepAliveSeconds
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

    val queue = new ArrayBlockingQueue[Runnable](queueSize)
    new ThreadPoolExecutor(poolSize, poolSize, keepAliveSeconds, TimeUnit.SECONDS, queue, ServiceThreadFactory, ServiceUnavailableHandler)
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

      Future {
        try {
          log(s"[info] Servicing request from $connection")
          socket.setSoTimeout(readTimeout)

          val req = read()
          val res = handle(req)

          Try(filter(res)).map { res =>
            write(res)
            log(s"[info] Response sent to $connection")
            Try(res.body.getInputStream.close()) // Close filtered response body
          }.recover {
            case e => log(s"[error] Error while filtering response to $connection", Some(e))
          }

          Try(res.body.getInputStream.close()) // Close unfiltered response body
        } catch {
          case e: Exception => log(s"[error] Error while servicing request from $connection", Some(e))
        } finally {
          log(s"[info] Closing connection to $connection")
          Try(socket.close())
        }
      }
    }

    private def read()(implicit socket: Socket): HttpRequest = {
      val buffer = new Array[Byte](8192)
      val startLine = RequestLine.parse(socket.readLine(buffer))
      val headers = new ArrayBuffer[Header](8)
      var line = ""

      while ({ line = socket.readLine(buffer); line != "" })
        headers += Header.parse(line)

      HttpRequest(startLine, headers.toSeq, socket.getInputStream)
    }

    private def write(res: HttpResponse)(implicit socket: Socket): Unit = {
      socket.writeLine(res.startLine.toString)
      res.headers.map(_.toString).foreach(socket.writeLine)
      socket.writeLine()

      if (!res.body.isKnownEmpty) {
        val in = res.body.getInputStream
        val buffer = new Array[Byte](8192)
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
      try
        requestHandler(req).getOrElse(NotFound())
      catch {
        case _: SocketTimeoutException => RequestTimeout()
      }

    private def filter(res: HttpResponse): HttpResponse =
      responseFilter(prepare(res))

    private def prepare(res: HttpResponse): HttpResponse = {
      if (res.hasTransferEncoding || res.hasContentLength)
        res
      else
        res.body.getLength match {
          case Some(0) => res.getContentType.map(_ => res.withContentLength(0)).getOrElse(res)
          case Some(n) => res.withContentLength(n)
          case None    => res.withTransferEncoding("chunked")
        }
    }.withDate(Instant.now).withConnection("close")
  }
}
