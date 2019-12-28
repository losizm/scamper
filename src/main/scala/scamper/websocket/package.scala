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

/** Provides specialized access to WebSocket headers and types. */
package object websocket {
  /** Globally Unique Identifier for WebSocket (258EAFA5-E914-47DA-95CA-C5AB0DC85B11) */
  val guid = "258EAFA5-E914-47DA-95CA-C5AB0DC85B11"

  /** Indicates error occurred on websocket with supplied status code. */
  case class WebSocketError(status: StatusCode) extends Exception(s"${status.value} ($status)")

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
}
