/*
 * Copyright 2021 Carlos Conyers
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
package scamper
package http
package server

import java.io.{ Closeable, EOFException, File, InputStream }
import java.net.{ InetAddress, InetSocketAddress, Socket, SocketTimeoutException, URISyntaxException }
import java.time.Instant
import java.util.concurrent.RejectedExecutionException
import java.util.concurrent.atomic.{ AtomicBoolean, AtomicLong }

import javax.net.ServerSocketFactory
import javax.net.ssl.{ SSLException, SSLServerSocketFactory }

import org.slf4j.LoggerFactory.getLogger

import scala.collection.mutable.ArrayBuffer
import scala.concurrent.{ ExecutionContext, Future }
import scala.util.{ Failure, Success, Try }

import scamper.http.headers.*
import scamper.http.types.{ KeepAliveParameters, TransferCoding }

import RequestMethod.Registry.Connect
import ResponseStatus.Registry.*
import RuntimeProperties.server.*

private object HttpServerImpl:
  private val count = AtomicLong(0)

  case class Application(
    loggerName:          Option[String] = None,
    backlogSize:         Int = 50,
    poolSize:            Int = Runtime.getRuntime.availableProcessors(),
    queueSize:           Int = Runtime.getRuntime.availableProcessors() * 4,
    bufferSize:          Int = 8192,
    readTimeout:         Int = 5000,
    headerLimit:         Int = 100,
    keepAlive:           Option[KeepAliveParameters] = None,
    lifecycleHooks:      Seq[LifecycleHook] = Nil,
    requestHandlers:     Seq[RequestHandler] = Nil,
    responseFilters:     Seq[ResponseFilter] = Nil,
    errorHandlers:       Seq[ErrorHandler] = Nil,
    serverSocketFactory: ServerSocketFactory = ServerSocketFactory.getDefault()
  )

  def apply(host: InetAddress, port: Int, app: Application) =
    new HttpServerImpl(count.incrementAndGet(), InetSocketAddress(host, port), app)

private class HttpServerImpl(id: Long, socketAddress: InetSocketAddress, app: HttpServerImpl.Application) extends HttpServer:
  private val logger = app.loggerName.map(getLogger).getOrElse(getLogger(getClass))

  val backlogSize = app.backlogSize.max(1)
  val poolSize    = app.poolSize.max(1)
  val queueSize   = app.queueSize.max(0)
  val bufferSize  = app.bufferSize.max(1024)
  val readTimeout = app.readTimeout.max(100)
  val headerLimit = app.headerLimit.max(10)
  val keepAlive   = app.keepAlive.map(params => KeepAliveParameters(params.timeout.max(1), params.max.max(1)))
  val isSecure    = app.serverSocketFactory.isInstanceOf[SSLServerSocketFactory]

  private val serverSocket = app.serverSocketFactory.createServerSocket()

  serverSocket.bind(socketAddress, backlogSize)

  val host = serverSocket.getInetAddress
  val port = serverSocket.getLocalPort

  private val defaultErrorHandler = new ErrorHandler:
    def apply(req: HttpRequest): PartialFunction[Throwable, HttpResponse] =
      case err: Throwable =>
        val correlate = req.getAttributeOrElse("scamper.http.server.message.correlate", "<unknown>")
        logger.error(s"$authority - Error while handling request (correlate=$correlate)", err)
        InternalServerError()

  private val authority         = s"${host.getCanonicalHostName}:$port"
  private val connectionManager = ConnectionManager(keepAlive)
  private val lifecycleHooks    = app.lifecycleHooks
  private val requestHandler    = RequestHandler.coalesce(app.requestHandlers)
  private val responseFilter    = ResponseFilter.chain(app.responseFilters)
  private val errorHandler      = ErrorHandler.coalesce(app.errorHandlers :+ defaultErrorHandler)
  private val chunked           = TransferCoding("chunked")
  private val closed            = AtomicBoolean(false)
  private val threadGroup       = ThreadGroup(s"scamper-server-$id")

  private val serviceExecutor =
    ThreadPoolExecutorService
      .fixed(
        name        = s"scamper-server-$id-service",
        poolSize    = poolSize,
        queueSize   = queueSize,
        threadGroup = Some(threadGroup)
      ) { (_, _) =>
        throw RejectedExecutionException(s"Rejected scamper-server-$id-service task")
      }

  private val keepAliveExecutor =
    ThreadPoolExecutorService
      .dynamic(
        name             = s"scamper-server-$id-keepAlive",
        corePoolSize     = poolSize,
        maxPoolSize      = poolSize * keepAlivePoolSizeFactor,
        queueSize        = 0,
        keepAliveSeconds = 60L,
        threadGroup      = Some(threadGroup)
      ) { (_, _) =>
        throw ReadAborted(s"rejected scamper-server-$id-keepAlive task")
      }

  private val upgradeExecutor =
    ThreadPoolExecutorService
      .dynamic(
        name             = s"scamper-server-$id-upgrade",
        corePoolSize     = poolSize,
        maxPoolSize      = poolSize * upgradePoolSizeFactor,
        queueSize        = 0,
        keepAliveSeconds = 60L,
        threadGroup      = Some(threadGroup)
      ) { (_, _) =>
        throw RejectedExecutionException(s"Rejected scamper-server-$id-upgrade task")
      }

  private val encoderExecutor =
    ThreadPoolExecutorService
      .dynamic(
        name             = s"scamper-server-$id-encoder",
        corePoolSize     = poolSize,
        maxPoolSize      = poolSize * encoderPoolSizeFactor,
        queueSize        = 0,
        keepAliveSeconds = 60L,
        threadGroup      = Some(threadGroup)
      ) { (task, executor) =>
        logger.warn(s"$authority - Running rejected scamper-server-$id-encoder task on dedicated thread")
        executor.getThreadFactory.newThread(task).start()
      }

  private val closerExecutor =
    ThreadPoolExecutorService
      .fixed(
        name             = s"scamper-server-$id-closer",
        poolSize         = poolSize,
        queueSize        = poolSize * closerQueueSizeFactor,
        threadGroup      = Some(threadGroup)
      ) { (task, executor) =>
        logger.warn(s"$authority - Running rejected scamper-server-$id-closer task on dedicated thread")
        executor.getThreadFactory.newThread(task).start()
      }

  try
    startLifecycleHooks()

    logger.info(s"$authority - Starting server")
    logger.info(s"$authority - Secure: $isSecure")
    logger.info(s"$authority - Backlog Size: $backlogSize")
    logger.info(s"$authority - Pool Size: $poolSize")
    logger.info(s"$authority - Queue Size: $queueSize")
    logger.info(s"$authority - Buffer Size: $bufferSize")
    logger.info(s"$authority - Read Timeout: $readTimeout")
    logger.info(s"$authority - Header Limit: $headerLimit")
    logger.info(s"$authority - Keep-Alive: ${keepAlive.getOrElse("disabled")}")

    ServiceManager.start()

    logger.info(s"$authority - Server is up and running")
  catch
    case e: Exception =>
      Try(logger.error(s"$authority - Failed to start server", e))
      close()
      throw e

  def isClosed: Boolean =
    closed.get()

  def close(): Unit =
    if closed.compareAndSet(false, true) then
      Try(logger.info(s"$authority - Shutting down server"))
      Try(serverSocket.close())
      Try(keepAliveExecutor.shutdownNow())
      Try(upgradeExecutor.shutdownNow())
      Try(encoderExecutor.shutdownNow())
      Try(serviceExecutor.shutdownNow())
      Try(closerExecutor.shutdownNow())
      Try(stopLifecycleHooks())

  override def toString(): String =
    s"HttpServer(host=$host, port=$port, isSecure=$isSecure, isClosed=$isClosed)"

  private def startLifecycleHooks(): Unit =
    logger.info(s"$authority - Calling start lifecycle hooks")
    lifecycleHooks.foreach { hook =>
      try
        hook.process(LifecycleEvent.Start(this))
      catch case err: Exception =>
        if hook.isCriticalService then
          throw LifecycleException(s"Critical service failure: ${err.getClass.getName}", err)
        else
          logger.warn(s"$authority - Start lifecycle hook failure", err)
    }

  private def stopLifecycleHooks(): Unit =
    logger.info(s"$authority - Calling stop lifecycle hooks")
    lifecycleHooks.reverse.foreach { hook =>
      try
        hook.process(LifecycleEvent.Stop(this))
      catch case err: Exception =>
        logger.warn(s"$authority - Stop lifecycle hook failure", err)
    }

  private object ServiceManager extends Thread(threadGroup, s"scamper-server-$id-service-manager"): //
    private val connectionCount = AtomicLong(0)
    private val serviceCount    = AtomicLong(0)

    override def run(): Unit =
      while !isClosed do
        try
          val socket = serverSocket.accept()
          service(connectionCount.incrementAndGet, 1)(using socket)
        catch
          case e: Exception if serverSocket.isClosed => close() // Ensure server is closed
          case e: Exception => logger.warn(s"$authority - Error caught in service loop: $e")

    private def createCorrelate(serviceId: Long, connectionId: Long, requestCount: Int): String =
      f"${System.currentTimeMillis}%x-$serviceId%04x-$connectionId%04x-$requestCount%02x"

    private def service(connectionId: Long, requestCount: Int)(using socket: Socket): Unit =
      val serviceId  = serviceCount.incrementAndGet
      val correlate  = createCorrelate(serviceId, connectionId, requestCount)
      val connection = socket.getInetAddress.getHostAddress + ":" + socket.getPort
      val tag        = connection + " (correlate=" + correlate + ")"

      def onReadError: PartialFunction[Throwable, HttpResponse] =
        case ReadError(status)              => status()
        case err: IllegalArgumentException  => BadRequest()
        case err: IndexOutOfBoundsException => BadRequest()
        case err: SocketTimeoutException    => RequestTimeout()
        case err: ResponseAborted           => throw err
        case err: SSLException              => throw err
        case err =>
          logger.error(s"$authority - Error while reading request from $tag", err)
          InternalServerError()

      def onHandleRequest(req: HttpRequest): Try[HttpResponse] =
        Try(handle(req)).recover {
          case err: SocketTimeoutException => RequestTimeout()
          case err: ResponseAborted        => throw err
          case err: SSLException           => throw err
          case err                         => errorHandler(req)(err)
        }

      def onHandleResponse(res: HttpResponse): ConnectionManagement =
        Try(filter(res)).recover {
          case err =>
            logger.error(s"$authority - Error while filtering response to $tag", err)
            InternalServerError().setDate().setConnection("close")
        }.map { res =>
          try
            write(res)
            logger.info(s"$authority - Response sent to $tag")
            connectionManager.evaluate(res)
          finally
            Try(res.body.data.close()) // Close filtered response body
        }.map { connectionManagement =>
          Try(res.body.data.close()) // Close unfiltered response body
          connectionManagement
        }.recover {
          case err =>
            logger.error(s"$authority - Error while writing response to $tag", err)
            CloseConnection
        }.get

      def onBeginService(firstByte: Byte): ConnectionManagement =
        try
          logger.info(s"$authority - Servicing request from $tag")
          socket.setSoTimeout(readTimeout)

          var request: HttpRequest = null

          Try(read(firstByte))
            .map(req => addAttributes(req, socket, requestCount, correlate))
            .map { req => request = req; req }
            .fold(err => Try(onReadError(err)), req => onHandleRequest(req))
            .map(res => addAttributes(res, socket, requestCount, correlate))
            .map(addRequestAttribute(_, request))
            .map(onHandleResponse)
            .get
        catch
          case err: ResponseAborted =>
            logger.warn(s"$authority - Response aborted while servicing request from $tag", err)
            CloseConnection

          case err: SSLException =>
            logger.warn(s"$authority - SSL error while servicing request from $tag", err)
            CloseConnection

          case err: Exception =>
            logger.error(s"$authority - Unhandled error while servicing request from $tag", err)
            CloseConnection

      val result = (requestCount == 1) match
        case true =>
          logger.info(s"$authority - Connection accepted from $tag")
          Future(onBeginService(readByte(false)))(using serviceExecutor)

        case false =>
          Future(readByte(true))(using keepAliveExecutor)
            .map(onBeginService)(using serviceExecutor)

      result.onComplete {
        case Success(CloseConnection) =>
          logger.info(s"$authority - Closing connection to $tag")
          Try(socket.close())

        case Success(PersistConnection) =>
          logger.info(s"$authority - Persisting connection to $tag")
          service(connectionId, requestCount + 1)

        case Success(UpgradeConnection(upgrade)) =>
          logger.info(s"$authority - Upgrading connection to $tag")
          Future(upgrade(socket))(using upgradeExecutor)

        case Failure(err: ReadAborted) =>
          (requestCount > 1) match
            case true  => logger.info(s"$authority - Keep-alive aborted with ${err.reason} from $tag")
            case false => logger.info(s"$authority - Service aborted with ${err.reason} from $tag")
          logger.info(s"$authority - Closing connection to $tag")
          Try(socket.close())

        case Failure(err: RejectedExecutionException) =>
          logger.warn(s"$authority - Request overflow while servicing request from $tag")
          val res = ServiceUnavailable().setRetryAfter(Instant.now.plusSeconds(300))
          Try(addAttributes(res, socket, requestCount, correlate)).map(onHandleResponse)
          logger.info(s"$authority - Closing connection to $tag")
          Try(socket.close())

        case Failure(err) =>
          logger.info(s"$authority - Closing connection to $tag")
          Try(socket.close())
      }(using closerExecutor)

    private def readByte(keepingAlive: Boolean)(using socket: Socket): Byte =
      keepingAlive match
        case true  => socket.setSoTimeout(connectionManager.keepAliveTimeout * 1000)
        case false => socket.setSoTimeout(readTimeout)

      try
        socket.read() match
          case -1   => throw EOFException()
          case byte => byte.toByte
      catch
        case err: Exception => throw ReadAborted(err.getClass.getName)

    private def read(firstByte: Byte)(using socket: Socket): HttpRequest =
      val buffer = new Array[Byte](bufferSize)

      buffer(0) = firstByte

      val method    = readMethod(buffer, 1)
      val target    = readTarget(buffer)
      val version   = readVersion(buffer)
      val startLine = RequestLine(method, target, version)
      val headers   = readHeaders(buffer)

      HttpRequest(startLine, headers, Entity(socket.getInputStream))

    private def readMethod(buffer: Array[Byte], offset: Int)(using socket: Socket): RequestMethod =
      try
        RequestMethod(socket.getToken(" ", buffer, offset))
      catch
        case _: IndexOutOfBoundsException => throw ReadError(NotImplemented)
        case _: IllegalArgumentException  => throw ReadError(BadRequest)


    private def readTarget(buffer: Array[Byte])(using socket: Socket): Uri =
      try
        Uri(socket.getToken(" ", buffer))
      catch
        case _: IndexOutOfBoundsException => throw ReadError(UriTooLong)
        case _: URISyntaxException        => throw ReadError(BadRequest)

    private def readVersion(buffer: Array[Byte])(using socket: Socket): HttpVersion =
      try
        HttpVersion(socket.getLine(buffer))
      catch
        case _: IndexOutOfBoundsException => throw ReadError(BadRequest)
        case _: IllegalArgumentException  => throw ReadError(BadRequest)

    private def readHeaders(buffer: Array[Byte])(using socket: Socket): Seq[Header] =
      val headers   = new ArrayBuffer[Header]
      val readLimit = headerLimit * bufferSize
      var readSize  = 0
      var line      = ""

      try
        while { line = socket.getLine(buffer); line != "" } do
          readSize += line.size

          if readSize <= readLimit then
            line.matches("[ \t]+.*") match
              case true =>
                if headers.isEmpty then throw ReadError(BadRequest)
                val last = headers.last
                headers.update(headers.size - 1, Header(last.name, last.value + " " + line.trim()))
              case false =>
                if headers.size < headerLimit then
                  headers += Header(line)
                else
                  throw ReadError(RequestHeaderFieldsTooLarge)
          else
            throw ReadError(RequestHeaderFieldsTooLarge)
      catch
        case _: IndexOutOfBoundsException => throw ReadError(RequestHeaderFieldsTooLarge)

      headers.toSeq

    private def write(res: HttpResponse)(using socket: Socket): Unit =
      socket.writeLine(res.startLine.toString)
      res.headers.foreach(header => socket.writeLine(header.toString))
      socket.writeLine()

      if !res.body.isKnownEmpty then
        val buffer = new Array[Byte](bufferSize)
        var length = 0

        res.transferEncodingOption.map { encoding =>
          val in = encode(res.body.data, encoding)
          while { length = in.read(buffer); length != -1 } do
            socket.writeLine(length.toHexString)
            socket.write(buffer, 0, length)
            socket.writeLine()
          socket.writeLine("0")
          socket.writeLine()
        }.getOrElse {
          val in = res.body.data
          while { length = in.read(buffer); length != -1 } do
            socket.write(buffer, 0, length)
        }

      socket.flush()

    private def encode(in: InputStream, encoding: Seq[TransferCoding]): InputStream =
      encoding.foldLeft(in) { (in, enc) =>
        if      enc.isChunked then in
        else if enc.isGzip    then Compressor.gzip(in, bufferSize)(using encoderExecutor)
        else if enc.isDeflate then Compressor.deflate(in, bufferSize)
        else                  throw HttpException(s"Unsupported transfer encoding: $enc")
      }

    private def handle(req: HttpRequest): HttpResponse =
      requestHandler(req) match
        case req: HttpRequest  => connectionManager.filter(req, NotFound())
        case res: HttpResponse => connectionManager.filter(req, res)

    private def filter(res: HttpResponse): HttpResponse =
      responseFilter {
        res.hasConnection match
          case true  => prepare(res).setDate()
          case false => prepare(res).setDate().setConnection("close")
      }

    private def prepare(res: HttpResponse): HttpResponse =
      if res.hasTransferEncoding then
        res.setTransferEncoding(res.transferEncoding.filterNot(_.isChunked) :+ chunked)
          .contentLengthRemoved
      else if res.hasContentLength then
        res
      else
        res.body.knownSize
          .map(n => if excludeContentLength(res) then res else res.setContentLength(n))
          .getOrElse(res.setTransferEncoding(chunked))

    private def excludeContentLength(res: HttpResponse): Boolean =
      import scala.language.implicitConversions

      res.isInformational || res.status == NoContent || res.request.exists(_.method == Connect)

    private def addAttributes[T <: HttpMessage & MessageBuilder[T]](msg: T, socket: Socket, requestCount: Int, correlate: String): T =
      msg.putAttributes(
        "scamper.http.server.message.server"       -> HttpServerImpl.this,
        "scamper.http.server.message.socket"       -> socket,
        "scamper.http.server.message.requestCount" -> requestCount,
        "scamper.http.server.message.correlate"    -> correlate
      )

    private def addRequestAttribute(res: HttpResponse, req: HttpRequest): HttpResponse =
      req != null match
        case true  => res.putAttributes("scamper.http.server.response.request" -> req)
        case false => res
