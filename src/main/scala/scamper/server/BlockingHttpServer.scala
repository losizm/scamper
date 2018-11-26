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
import java.net.{ InetAddress, InetSocketAddress, Socket }
import java.time.OffsetDateTime
import java.util.concurrent.{ ArrayBlockingQueue, RejectedExecutionHandler, TimeUnit, ThreadFactory, ThreadPoolExecutor }
import java.util.concurrent.atomic.AtomicInteger

import javax.net.ServerSocketFactory
import javax.net.ssl.{ SSLException, SSLServerSocketFactory }

import scala.collection.mutable.ArrayBuffer
import scala.concurrent.{ ExecutionContext, Future }
import scala.util.Try

import scamper.{ Header, HttpRequest, HttpResponse, RequestLine }
import scamper.ImplicitConverters.{ inputStreamToEntity, stringToEntity }
import scamper.ResponseStatuses.NotFound
import scamper.auxiliary.SocketType
import scamper.headers.{ Connection, ContentLength, ContentType, TransferEncoding }
import scamper.types.ImplicitConverters.stringToTransferCoding

private object BlockingHttpServer {
  private val count = new AtomicInteger(0)

  case class Configuration(
    poolSize: Int = Runtime.getRuntime.availableProcessors(),
    queueSize: Int = Runtime.getRuntime.availableProcessors() * 4,
    readTimeout: Int = 5000,
    keepAliveSeconds: Int = 60,
    log: File = new File("server.log"),
    handlers: Seq[RequestHandler] = Nil,
    factory: ServerSocketFactory = ServerSocketFactory.getDefault()
  )

  def apply(host: InetAddress, port: Int, config: Configuration) =
    new BlockingHttpServer(count.incrementAndGet(), host, port, config)
}

private class BlockingHttpServer private(val id: Int, val host: InetAddress, val port: Int, config: BlockingHttpServer.Configuration) extends HttpServer {
  private val authority = s"${host.getCanonicalHostName}:$port"
  private val threadGroup = new ThreadGroup(s"httpserver-$id")
  private val poolSize = config.poolSize
  private val queueSize = config.queueSize
  private val keepAliveSeconds = config.keepAliveSeconds
  private val handlers = config.handlers
  private val logger = new PrintWriter(new FileWriter(config.log, true), true)
  private val serverSocket = config.factory.createServerSocket()
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

  val isSecure: Boolean = config.factory.isInstanceOf[SSLServerSocketFactory]

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
    serverSocket.bind(new InetSocketAddress(host, port), queueSize)
    Service.start()
    log(s"[info] Server running at $authority")
  } catch {
    case cause: Exception =>
      log(s"[error] Failed to start server at $authority", Some(cause))
      close()
      throw cause
  }

  private def log(message: String, cause: Option[Throwable] = None): Unit = {
    logger.printf("%s [%s]%s%n", OffsetDateTime.now(), authority, message)
    cause.foreach(_.printStackTrace(logger))
  }

  private object Service extends Thread(threadGroup, s"httpserver-$id-service") {
    override def run(): Unit = {
      while (!isClosed)
        try
          service(serverSocket.accept())
        catch {
          case _ if !isClosed => // Ignore if server is closed

          case cause: Exception =>
            if (serverSocket.isClosed) {
              log(s"[error] Error while waiting for connection: $cause", Some(cause))
              Try(close())
            } else {
              log(s"[warning] Error while waiting for connection: $cause", Some(cause))
            }
        }
    }

    private def service(implicit socket: Socket): Unit = {
      log(s"[info] Connection received from ${format(socket)}")

      Future {
        try {
          log(s"[info] Servicing request from ${format(socket)}")
          socket.setSoTimeout(config.readTimeout)

          val init: Either[HttpRequest, HttpResponse] = Left(read())

          val res = handlers.foldLeft(init) { (result, handler) =>
            result.left.flatMap(req => handler(req))
          } match {
            case Right(res) => res
            case Left(req)  => NotFound()
          }

          write(getEffectiveResponse(res))
          log(s"[info] Response sent to ${format(socket)}")
        } catch {
          case cause: Exception =>
            log(s"[error] Error while servicing request from ${format(socket)}", Some(cause))
        } finally {
            log(s"[info] Closing connection to ${format(socket)}")
            Try(socket.close())
        }
      }
    }

    private def format(socket: Socket): String =
      socket.getInetAddress.getHostAddress + ":" + socket.getPort

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

    private def getEffectiveResponse(res: HttpResponse): HttpResponse = {
      if (res.hasTransferEncoding || res.hasContentLength)
        res
      else
        res.body.getLength match {
          case Some(0) => res.getContentType.map(_ => res.withContentLength(0)).getOrElse(res)
          case Some(n) => res.withContentLength(n)
          case None    => res.withTransferEncoding("chunked")
        }
    }.withConnection("close")
  }
}
