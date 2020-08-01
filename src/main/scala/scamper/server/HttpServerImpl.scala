/*
 * Copyright 2017-2020 Carlos Conyers
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

import java.io.{ Closeable, EOFException, File, InputStream }
import java.net.{ InetAddress, InetSocketAddress, Socket, SocketTimeoutException, URISyntaxException }
import java.time.Instant
import java.util.concurrent.RejectedExecutionException
import java.util.concurrent.atomic.AtomicLong

import javax.net.ServerSocketFactory
import javax.net.ssl.{ SSLException, SSLServerSocketFactory }

import scala.collection.mutable.ArrayBuffer
import scala.concurrent.{ ExecutionContext, Future }
import scala.util.{ Failure, Success, Try }

import scamper._
import scamper.Auxiliary.{ SocketType, getIntProperty }
import scamper.RequestMethod.Registry._
import scamper.ResponseStatus.Registry._
import scamper.headers.{ Connection, ContentLength, ContentType, Date, KeepAlive, RetryAfter, TransferEncoding, Upgrade }
import scamper.logging.{ ConsoleLogger, Logger, NullLogger }
import scamper.types.{ KeepAliveParameters, TransferCoding }

private object HttpServerImpl {
  private val count = new AtomicLong(0)

  case class Application(
    logger: Logger = ConsoleLogger,
    backlogSize: Int = 50,
    poolSize: Int = Runtime.getRuntime.availableProcessors(),
    queueSize: Int = Runtime.getRuntime.availableProcessors() * 4,
    bufferSize: Int = 8192,
    readTimeout: Int = 5000,
    headerLimit: Int = 100,
    keepAlive: Option[KeepAliveParameters] = None,
    requestHandlers: Seq[RequestHandler] = Nil,
    responseFilters: Seq[ResponseFilter] = Nil,
    errorHandler: Option[ErrorHandler] = None,
    serverSocketFactory: ServerSocketFactory = ServerSocketFactory.getDefault()
  )

  def apply(host: InetAddress, port: Int, app: Application) =
    new HttpServerImpl(host, port)(count.incrementAndGet(), app)
}

private class HttpServerImpl(val host: InetAddress, val port: Int)
    (id: Long, app: HttpServerImpl.Application) extends HttpServer {
  private case class ReadError(status: ResponseStatus) extends HttpException(status.reason)
  private case class ReadAborted(reason: String) extends HttpException(s"Read aborted with $reason")

  private sealed trait ConnectionManagement
  private case object CloseConnection extends ConnectionManagement
  private case object PersistConnection extends ConnectionManagement
  private case class UpgradeConnection(upgrade: Socket => Unit) extends ConnectionManagement

  val logger = if (app.logger == null) NullLogger else app.logger
  val backlogSize = app.backlogSize.max(1)
  val poolSize = app.poolSize.max(1)
  val queueSize = app.queueSize.max(0)
  val bufferSize = app.bufferSize.max(1024)
  val readTimeout = app.readTimeout.max(100)
  val headerLimit = app.headerLimit.max(10)
  val keepAlive = app.keepAlive.map(params => KeepAliveParameters(params.timeout.max(1), params.max.max(2)))

  private val keepAliveEnabled = keepAlive.isDefined
  private val keepAliveMax = keepAlive.map(_.max).getOrElse(1)
  private val keepAliveTimeout = keepAlive.map(_.timeout).getOrElse(0)

  private val keepAlivePoolSizeFactor = getIntProperty("scamper.server.keepAlive.poolSizeFactor", 2).max(1)
  private val upgradePoolSizeFactor = getIntProperty("scamper.server.upgrade.poolSizeFactor", 2).max(1)
  private val encoderPoolSizeFactor = getIntProperty("scamper.server.encoder.poolSizeFactor", 2).max(1)
  private val closerPoolSizeFactor = getIntProperty("scamper.server.closer.poolSizeFactor", 2).max(1)

  private val authority = s"${host.getCanonicalHostName}:$port"

  private val requestHandler = RequestHandler.coalesce(app.requestHandlers)
  private val responseFilter = ResponseFilter.chain(app.responseFilters)
  private val errorHandler = app.errorHandler.getOrElse(new ErrorHandler {
    def apply(err: Throwable, req: HttpRequest): HttpResponse = {
      val correlate = req.getAttribute[String]("scamper.server.message.correlate").getOrElse("<unknown>")
      logger.error(s"$authority - Error while handling request (correlate=$correlate)", err)
      InternalServerError()
    }
  })

  private val serverSocket = app.serverSocketFactory.createServerSocket()
  private val chunked = TransferCoding("chunked")
  private var closed = false

  private val threadGroup = new ThreadGroup(s"scamper-server-$id")

  private val serviceContext = ThreadPoolExecutorService
    .fixed(s"scamper-server-$id-service", poolSize, queueSize, Some(threadGroup)) {
      (_, _) => throw new RejectedExecutionException(s"Rejected scamper-server-$id-service task")
    }

  private val keepAliveContext = ThreadPoolExecutorService
    .dynamic(s"scamper-server-$id-keepAlive", poolSize, poolSize * keepAlivePoolSizeFactor, 60L, 0, Some(threadGroup)) {
      (_, _) => throw new ReadAborted(s"rejected scamper-server-$id-keepAlive task")
    }

  private val upgradeContext = ThreadPoolExecutorService
    .dynamic(s"scamper-server-$id-upgrade", poolSize, poolSize * upgradePoolSizeFactor, 60L, 0, Some(threadGroup)) {
      (_, _) => throw new RejectedExecutionException(s"Rejected scamper-server-$id-upgrade task")
    }

  private val encoderContext = ThreadPoolExecutorService
    .dynamic(s"scamper-server-$id-encoder", poolSize, poolSize * encoderPoolSizeFactor, 60L, 0, Some(threadGroup)) {
      (task, executor) =>
        logger.warn(s"$authority - Running rejected scamper-server-$id-encoder task on dedicated thread")
        executor.getThreadFactory.newThread(task).start()
    }

  private val closerContext = ThreadPoolExecutorService
    .dynamic(s"scamper-server-$id-closer", poolSize, poolSize * closerPoolSizeFactor, 60L, 0, Some(threadGroup)) {
      (task, executor) =>
        logger.warn(s"$authority - Running rejected scamper-server-$id-closer task on dedicated thread")
        executor.getThreadFactory.newThread(task).start()
    }

  val isSecure: Boolean = app.serverSocketFactory.isInstanceOf[SSLServerSocketFactory]

  def isClosed: Boolean = synchronized(closed)

  def close(): Unit = synchronized {
    if (!closed) {
      Try(logger.info(s"$authority - Shutting down server"))
      Try(serverSocket.close())
      Try(keepAliveContext.shutdownNow())
      Try(encoderContext.shutdownNow())
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
    logger.info(s"$authority - Backlog Size: $backlogSize")
    logger.info(s"$authority - Pool Size: $poolSize")
    logger.info(s"$authority - Queue Size: $queueSize")
    logger.info(s"$authority - Buffer Size: $bufferSize")
    logger.info(s"$authority - Read Timeout: $readTimeout")
    logger.info(s"$authority - Header Limit: $headerLimit")
    logger.info(s"$authority - Keep-Alive: ${keepAlive.getOrElse("disabled")}")

    serverSocket.bind(new InetSocketAddress(host, port), backlogSize)
    ServiceManager.start()

    logger.info(s"$authority - Server is up and running")
  } catch {
    case e: Exception =>
      logger.error(s"$authority - Failed to start server", e)
      close()
      throw e
  }

  private object ConnectionManager {
    private val keepAliveHeader = Header("Keep-Alive", s"timeout=$keepAliveTimeout, max=$keepAliveMax")
    private val connectionKeepAlive = Header("Connection", "keep-alive")
    private val connectionClose = Header("Connection", "close")

    def apply(req: HttpRequest, res: HttpResponse): HttpResponse =
      if (isUpgrade(res))
        res
      else
        doKeepAlive(req, res) match {
          case true  => res.withHeader(connectionKeepAlive).withHeader(keepAliveHeader)
          case false => res.withHeader(connectionClose)
        }

    private def doKeepAlive(req: HttpRequest, res: HttpResponse): Boolean =
      keepAliveEnabled &&
        isKeepAliveRequested(req) &&
        isKeepAliveMaxLeft(req) &&
        isKeepAliveSafe(req, res)

    private def isKeepAliveRequested(req: HttpRequest): Boolean =
      req.connection.exists(_ equalsIgnoreCase "keep-alive")

    private def isKeepAliveMaxLeft(req: HttpRequest): Boolean =
      req.getAttribute[Int]("scamper.server.message.requestCount")
        .map(_ < keepAliveMax)
        .getOrElse(false)

    private def isKeepAliveSafe(req: HttpRequest, res: HttpResponse): Boolean =
      res.status.isSuccessful ||
        ((req.method == GET || req.method == HEAD) && res.status.isRedirection)

    private def isUpgrade(res: HttpResponse): Boolean =
      res.status == SwitchingProtocols && res.hasUpgrade &&
        res.connection.exists(_ equalsIgnoreCase "upgrade")
  }

  private object ServiceManager extends Thread(threadGroup, s"scamper-server-$id-service-manager") {
    private val connectionCount = new AtomicLong(0)
    private val serviceCount = new AtomicLong(0)

    private def createCorrelate(serviceId: Long, connectionId: Long, requestCount: Int): String =
      f"${System.currentTimeMillis}%x-$serviceId%04x-$connectionId%04x-$requestCount%02x"

    override def run(): Unit =
      while (!isClosed)
        try {
          implicit val socket = serverSocket.accept()
          service(connectionCount.incrementAndGet, 1)
        } catch {
          case e: Exception if serverSocket.isClosed => close() // Ensure server is closed
          case e: Exception => logger.warn(s"$authority - Error while waiting for connection: $e")
        }

    private def service(connectionId: Long, requestCount: Int)(implicit socket: Socket): Unit = {
      val serviceId = serviceCount.incrementAndGet
      val connection = socket.getInetAddress.getHostAddress + ":" + socket.getPort
      val correlate = createCorrelate(serviceId, connectionId, requestCount)
      val tag = connection + " (correlate=" + correlate + ")"

      if (requestCount == 1)
        logger.info(s"$authority - Connection accepted from $tag")

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

      def onHandleRequest(req: HttpRequest): Try[HttpResponse] =
        Try(handle(req)).recover {
          case err: SocketTimeoutException => RequestTimeout()
          case err: ResponseAborted        => throw err
          case err: SSLException           => throw err
          case err                         => errorHandler(err, req)
        }

      def onHandleResponse(res: HttpResponse): ConnectionManagement =
        Try(filter(res)).recover {
          case err =>
            logger.error(s"$authority - Error while filtering response to $tag", err)
            InternalServerError().withDate(Instant.now).withConnection("close")
        }.map { res =>
          write(res)
          logger.info(s"$authority - Response sent to $tag")
          Try(res.body.getInputStream.close()) // Close filtered response body
          val connection = res.connection

          if (connection.exists(_ equalsIgnoreCase "upgrade"))
            UpgradeConnection(res.getAttribute("scamper.server.connection.upgrade").get)
          else if (connection.exists(_ equalsIgnoreCase "keep-alive"))
            PersistConnection
          else
            CloseConnection
        }.map { connectionManagement =>
          Try(res.body.getInputStream.close()) // Close unfiltered response body
          connectionManagement
        }.recover {
          case err =>
            logger.error(s"$authority - Error while writing response to $tag", err)
            CloseConnection
        }.get

      def onBeginService(firstByte: Byte): ConnectionManagement =
        try {
          logger.info(s"$authority - Servicing request from $tag")
          socket.setSoTimeout(readTimeout)

          Try(read(firstByte))
            .map(req => addAttributes(req, socket, requestCount, correlate))
            .fold(err => Try(onReadError(err)), req => onHandleRequest(req))
            .map(res => addAttributes(res, socket, requestCount, correlate))
            .map(onHandleResponse)
            .get
        } catch {
          case err: ResponseAborted =>
            logger.warn(s"$authority - Response aborted while servicing request from $tag", err)
            CloseConnection
          case err: SSLException =>
            logger.warn(s"$authority - SSL error while servicing request from $tag", err)
            CloseConnection
          case err: Exception =>
            logger.error(s"$authority - Unhandled error while servicing request from $tag", err)
            CloseConnection
        }

      val result = (requestCount > 1) match {
        case true  =>
          Future { readByte(true) } (keepAliveContext)
            .map { onBeginService } (serviceContext)
        case false =>
          Future { onBeginService(readByte(false)) } (serviceContext)
      }

      result.onComplete {
        case Success(CloseConnection) =>
          logger.info(s"$authority - Closing connection to $tag")
          Try(socket.close())

        case Success(PersistConnection) =>
          logger.info(s"$authority - Persisting connection to $tag")
          service(connectionId, requestCount + 1)

        case Success(UpgradeConnection(upgrade)) =>
          logger.info(s"$authority - Upgrading connection to $tag")
          Future(upgrade(socket))(upgradeContext)

        case Failure(err: ReadAborted) =>
          (requestCount > 1) match {
            case true  => logger.info(s"$authority - Keep-alive aborted with ${err.reason} from $tag")
            case false => logger.info(s"$authority - Service aborted with ${err.reason} from $tag")
          }
          logger.info(s"$authority - Closing connection to $tag")
          Try(socket.close())

        case Failure(err: RejectedExecutionException) =>
          logger.warn(s"$authority - Request overflow while servicing request from $tag")
          val res = ServiceUnavailable().withRetryAfter(Instant.now.plusSeconds(300))
          Try(addAttributes(res, socket, requestCount, correlate)).map(onHandleResponse)
          logger.info(s"$authority - Closing connection to $tag")
          Try(socket.close())

        case Failure(err) =>
          logger.info(s"$authority - Closing connection to $tag")
          Try(socket.close())
      } (closerContext)
    }

    private def readByte(keepingAlive: Boolean)(implicit socket: Socket): Byte = {
      keepingAlive match {
        case true  => socket.setSoTimeout(keepAliveTimeout * 1000)
        case false => socket.setSoTimeout(readTimeout)
      }

      try
        socket.read() match {
          case -1   => throw new EOFException()
          case byte => byte.toByte
        }
      catch {
        case err: Exception => throw ReadAborted(err.getClass.getName)
      }
    }

    private def read(firstByte: Byte)(implicit socket: Socket): HttpRequest = {
      val buffer = new Array[Byte](bufferSize)

      buffer(0) = firstByte

      val method = readMethod(buffer, 1)
      val target = readTarget(buffer)
      val version = readVersion(buffer)
      val startLine = RequestLine(method, target, version)
      val headers = readHeaders(buffer)

      HttpRequest(startLine, headers, Entity(socket.getInputStream))
    }

    private def readMethod(buffer: Array[Byte], offset: Int)(implicit socket: Socket): RequestMethod =
      RequestMethod(socket.getToken(" ", buffer, offset))

    private def readTarget(buffer: Array[Byte])(implicit socket: Socket): Uri =
      try
        Uri(socket.getToken(" ", buffer))
      catch {
        case _: IndexOutOfBoundsException => throw ReadError(UriTooLong)
        case _: URISyntaxException        => throw ReadError(BadRequest)
      }

    private def readVersion(buffer: Array[Byte])(implicit socket: Socket): HttpVersion = {
      val regex = "HTTP/(.+)".r

      socket.getLine(buffer) match {
        case regex(version) => HttpVersion(version)
        case _ => throw ReadError(BadRequest)
      }
    }

    private def readHeaders(buffer: Array[Byte])(implicit socket: Socket): Seq[Header] = {
      val headers = new ArrayBuffer[Header]
      val readLimit = headerLimit * bufferSize
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
                if (headers.size < headerLimit)
                  headers += Header(line)
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
        else if (enc.isGzip) Compressor.gzip(in, bufferSize)(encoderContext)
        else if (enc.isDeflate) Compressor.deflate(in, bufferSize)(encoderContext)
        else throw new HttpException(s"Unsupported transfer encoding: $enc")
      }

    private def handle(req: HttpRequest): HttpResponse = {
      requestHandler(req) match {
        case req: HttpRequest  => ConnectionManager(req, NotFound())
        case res: HttpResponse => ConnectionManager(req, res)
      }
    }

    private def filter(res: HttpResponse): HttpResponse =
      responseFilter {
        res.hasConnection match {
          case true  => prepare(res).withDate(Instant.now)
          case false => prepare(res).withDate(Instant.now).withConnection("close")
        }
      }

    private def prepare(res: HttpResponse): HttpResponse =
      if (res.hasTransferEncoding)
        res.withTransferEncoding(res.transferEncoding.filterNot(_.isChunked) :+ chunked)
          .removeContentLength()
      else if (res.hasContentLength)
        res
      else
        res.body.getLength match {
          case Some(0) => res.getContentType.map(_ => res.withContentLength(0)).getOrElse(res)
          case Some(n) => res.withContentLength(n)
          case None    => res.withTransferEncoding(chunked)
        }

    private def addAttributes[T <: HttpMessage](msg: T, socket: Socket, requestCount: Int, correlate: String)
        (implicit ev: <:<[T, MessageBuilder[T]]): T =
      msg
        .withAttribute("scamper.server.message.socket"       -> socket)
        .withAttribute("scamper.server.message.requestCount" -> requestCount)
        .withAttribute("scamper.server.message.correlate"    -> correlate)
        .withAttribute("scamper.server.message.logger"       -> logger)
  }
}
