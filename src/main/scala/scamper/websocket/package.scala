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
package scamper

import java.security.{ MessageDigest, SecureRandom }

import scala.util.Try

import RequestMethod.Registry._
import ResponseStatus.Registry._
import headers.{ Connection, Upgrade }

/** Provides specialized access to WebSocket headers and types. */
package object websocket {
  private val random = new SecureRandom()

  /** Globally Unique Identifier for WebSocket (258EAFA5-E914-47DA-95CA-C5AB0DC85B11) */
  val guid = "258EAFA5-E914-47DA-95CA-C5AB0DC85B11"

  /** Provides reason for invalid WebSocket request. */
  case class InvalidWebSocketRequest(reason: String) extends HttpException(reason)

  /** Provides status code of WebSocket error. */
  case class WebSocketError(statusCode: StatusCode) extends HttpException(s"${statusCode.value} ($statusCode)")

  /** Gets randomly generated WebSocket key. */
  def generateWebSocketKey(): String = {
    val key = new Array[Byte](16)
    random.nextBytes(key)
    Base64.encodeToString(key)
  }

  /**
   * Generates `Sec-WebSocket-Accept` header value using supplied WebSocket key.
   *
   * @param key WebSocket key
   *
   * @throws IllegalArgumentException if WebSocket key is invalid
   */
  def acceptWebSocketKey(key: String): String = {
    checkWebSocketKeyValue(key)
    Base64.encodeToString { hash(key + guid) }
  }

  /**
   * Checks validity of WebSocket request.
   *
   * @param req WebSocket request
   *
   * @throws InvalidWebSocketRequest if WebSocket request is invalid
   *
   * @return unmodified WebSocket request
   */
  def checkWebSocketRequest(req: HttpRequest): HttpRequest = {
    if (req.method != GET)
      throw InvalidWebSocketRequest(s"Invalid method for WebSocket request: ${req.method}")

    if (!checkUpgrade(req))
      throw InvalidWebSocketRequest("Missing or invalid header: Upgrade")

    if (!checkConnection(req))
      throw InvalidWebSocketRequest("Missing or invalid header: Connection")

    if (!checkWebSocketKey(req))
      throw InvalidWebSocketRequest("Missing or invalid header: Sec-WebSocket-Key")

    if (!checkWebSocketVersion(req))
      throw InvalidWebSocketRequest("Missing or invalid header: Sec-WebSocket-Version")

    req
  }

  /**
   * Checks for successful WebSocket handshake based on supplied request and
   * response.
   *
   * @param req WebSocket request
   * @param res WebSocket response
   *
   * @return `true` if handshake is successful; `false` otherwise
   *
   * @throws InvalidWebSocketRequest if WebSocket request is invalid
   */
  def checkWebSocketHandshake(req: HttpRequest, res: HttpResponse): Boolean = {
    checkWebSocketRequest(req)

    res.status == SwitchingProtocols &&
      checkUpgrade(res) &&
      checkConnection(res) &&
      checkWebSocketAccept(res, req.secWebSocketKey)
  }

  /** Provides standardized access to Sec-WebSocket-Key header. */
  implicit class SecWebSocketKey(private val request: HttpRequest) extends AnyVal {
    /**
     * Gets Sec-WebSocket-Key header value.
     *
     * @throws HeaderNotFound if Sec-WebSocket-Key is not present
     */
    def secWebSocketKey: String = getSecWebSocketKey.getOrElse(throw HeaderNotFound("Sec-WebSocket-Key"))

    /** Gets Sec-WebSocket-Key header value if present. */
    def getSecWebSocketKey: Option[String] =
      request.getHeaderValue("Sec-WebSocket-Key")

    /** Tests whether Sec-WebSocket-Key header is present. */
    def hasSecWebSocketKey: Boolean = request.hasHeader("Sec-WebSocket-Key")

    /** Creates new request setting Sec-WebSocket-Key header to supplied value. */
    def withSecWebSocketKey(value: String): HttpRequest =
      request.withHeader(Header("Sec-WebSocket-Key", value))

    /** Creates new request removing Sec-WebSocket-Key header. */
    def removeSecWebSocketKey(): HttpRequest = request.removeHeaders("Sec-WebSocket-Key")
  }

  /** Provides standardized access to Sec-WebSocket-Extensions header. */
  implicit class SecWebSocketExtensions[T <: HttpMessage](private val message: T) extends AnyVal {
    /**
     * Gets Sec-WebSocket-Extensions header value.
     *
     * @throws HeaderNotFound if Sec-WebSocket-Extensions is not present
     */
    def secWebSocketExtensions: String = getSecWebSocketExtensions.getOrElse(throw HeaderNotFound("Sec-WebSocket-Extensions"))

    /** Gets Sec-WebSocket-Extensions header value if present. */
    def getSecWebSocketExtensions: Option[String] =
      message.getHeaderValue("Sec-WebSocket-Extensions")

    /** Tests whether Sec-WebSocket-Extensions header is present. */
    def hasSecWebSocketExtensions: Boolean = message.hasHeader("Sec-WebSocket-Extensions")

    /** Creates new message setting Sec-WebSocket-Extensions header to supplied value. */
    def withSecWebSocketExtensions(value: String)(implicit ev: <:<[T, MessageBuilder[T]]): T =
      message.withHeader(Header("Sec-WebSocket-Extensions", value))

    /** Creates new message removing Sec-WebSocket-Extensions header. */
    def removeSecWebSocketExtensions()(implicit ev: <:<[T, MessageBuilder[T]]): T =
      message.removeHeaders("Sec-WebSocket-Extensions")
  }

  /** Provides standardized access to Sec-WebSocket-Accept header. */
  implicit class SecWebSocketAccept(private val response: HttpResponse) extends AnyVal {
    /**
     * Gets Sec-WebSocket-Accept header value.
     *
     * @throws HeaderNotFound if Sec-WebSocket-Accept is not present
     */
    def secWebSocketAccept: String = getSecWebSocketAccept.getOrElse(throw HeaderNotFound("Sec-WebSocket-Accept"))

    /** Gets Sec-WebSocket-Accept header value if present. */
    def getSecWebSocketAccept: Option[String] =
      response.getHeaderValue("Sec-WebSocket-Accept")

    /** Tests whether Sec-WebSocket-Accept header is present. */
    def hasSecWebSocketAccept: Boolean = response.hasHeader("Sec-WebSocket-Accept")

    /** Creates new response setting Sec-WebSocket-Accept header to supplied value. */
    def withSecWebSocketAccept(value: String): HttpResponse =
      response.withHeader(Header("Sec-WebSocket-Accept", value))

    /** Creates new response removing Sec-WebSocket-Accept header. */
    def removeSecWebSocketAccept(): HttpResponse = response.removeHeaders("Sec-WebSocket-Accept")
  }

  /** Provides standardized access to Sec-WebSocket-Protocol header. */
  implicit class SecWebSocketProtocol[T <: HttpMessage](private val message: T) extends AnyVal {
    /**
     * Gets Sec-WebSocket-Protocol header value.
     *
     * @throws HeaderNotFound if Sec-WebSocket-Protocol is not present
     */
    def secWebSocketProtocol: String = getSecWebSocketProtocol.getOrElse(throw HeaderNotFound("Sec-WebSocket-Protocol"))

    /** Gets Sec-WebSocket-Protocol header value if present. */
    def getSecWebSocketProtocol: Option[String] =
      message.getHeaderValue("Sec-WebSocket-Protocol")

    /** Tests whether Sec-WebSocket-Protocol header is present. */
    def hasSecWebSocketProtocol: Boolean = message.hasHeader("Sec-WebSocket-Protocol")

    /** Creates new message setting Sec-WebSocket-Protocol header to supplied value. */
    def withSecWebSocketProtocol(value: String)(implicit ev: <:<[T, MessageBuilder[T]]): T =
      message.withHeader(Header("Sec-WebSocket-Protocol", value))

    /** Creates new message removing Sec-WebSocket-Protocol header. */
    def removeSecWebSocketProtocol()(implicit ev: <:<[T, MessageBuilder[T]]): T =
      message.removeHeaders("Sec-WebSocket-Protocol")
  }

  /** Provides standardized access to Sec-WebSocket-Version header. */
  implicit class SecWebSocketVersion[T <: HttpMessage](private val message: T) extends AnyVal {
    /**
     * Gets Sec-WebSocket-Version header value.
     *
     * @throws HeaderNotFound if Sec-WebSocket-Version is not present
     */
    def secWebSocketVersion: String = getSecWebSocketVersion.getOrElse(throw HeaderNotFound("Sec-WebSocket-Version"))

    /** Gets Sec-WebSocket-Version header value if present. */
    def getSecWebSocketVersion: Option[String] =
      message.getHeaderValue("Sec-WebSocket-Version")

    /** Tests whether Sec-WebSocket-Version header is present. */
    def hasSecWebSocketVersion: Boolean = message.hasHeader("Sec-WebSocket-Version")

    /** Creates new message setting Sec-WebSocket-Version header to supplied value. */
    def withSecWebSocketVersion(value: String)(implicit ev: <:<[T, MessageBuilder[T]]): T =
      message.withHeader(Header("Sec-WebSocket-Version", value))

    /** Creates new message removing Sec-WebSocket-Version header. */
    def removeSecWebSocketVersion()(implicit ev: <:<[T, MessageBuilder[T]]): T =
      message.removeHeaders("Sec-WebSocket-Version")
  }

  private def checkUpgrade(msg: HttpMessage): Boolean =
    msg.upgrade.exists { protocol =>
      protocol.name == "websocket" && protocol.version.isEmpty
    }

  private def checkConnection(msg: HttpMessage): Boolean =
    msg.connection.exists(_ equalsIgnoreCase "upgrade")

  private def checkWebSocketKey(req: HttpRequest): Boolean =
    req.getSecWebSocketKey
      .map(checkWebSocketKeyValue)
      .getOrElse(false)

  private def checkWebSocketKeyValue(key: String): Boolean =
    Try(Base64.decode(key).size == 16).getOrElse(false)

  private def checkWebSocketVersion(msg: HttpMessage): Boolean =
    msg.getSecWebSocketVersion
      .map(_ == "13")
      .getOrElse(false)

  private def checkWebSocketAccept(res: HttpResponse, key: String): Boolean =
    res.getSecWebSocketAccept
      .map(checkWebSocketAcceptValue(_, key))
      .getOrElse(false)

  private def checkWebSocketAcceptValue(value: String, key: String): Boolean =
    value == acceptWebSocketKey(key)

  private def hash(value: String): Array[Byte] =
    MessageDigest.getInstance("SHA-1").digest(value.getBytes("utf-8"))
}
