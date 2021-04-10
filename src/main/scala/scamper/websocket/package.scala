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
package scamper

/** Defines WebSocket implementation. */
package object websocket {
  /** Converts string to [[WebSocketExtension]]. */
  implicit val stringToWebSocketExtension = (ext: String) => WebSocketExtension.parse(ext)

  /** Provides reason for invalid WebSocket request. */
  case class InvalidWebSocketRequest(reason: String) extends HttpException(reason)

  /** Provides reason for WebSocket handshake failure. */
  case class WebSocketHandshakeFailure(reason: String) extends HttpException(reason)

  /** Provides status code of WebSocket error. */
  case class WebSocketError(statusCode: StatusCode) extends HttpException(statusCode.toString)

  /** Provides standardized access to Sec-WebSocket-Accept header. */
  implicit class SecWebSocketAccept(private val response: HttpResponse) extends AnyVal {
    /** Tests for Sec-WebSocket-Accept header. */
    def hasSecWebSocketAccept: Boolean =
      response.hasHeader("Sec-WebSocket-Accept")

    /**
     * Gets Sec-WebSocket-Accept header value.
     *
     * @throws HeaderNotFound if Sec-WebSocket-Accept is not present
     */
    def secWebSocketAccept: String =
      getSecWebSocketAccept.getOrElse(throw HeaderNotFound("Sec-WebSocket-Accept"))

    /** Gets Sec-WebSocket-Accept header value if present. */
    def getSecWebSocketAccept: Option[String] =
      response.getHeaderValue("Sec-WebSocket-Accept")

    /** Creates new response setting Sec-WebSocket-Accept header to supplied value. */
    def setSecWebSocketAccept(value: String): HttpResponse =
      response.putHeaders(Header("Sec-WebSocket-Accept", value))

    /** Creates new response removing Sec-WebSocket-Accept header. */
    def removeSecWebSocketAccept: HttpResponse =
      response.removeHeaders("Sec-WebSocket-Accept")
  }

  /** Provides standardized access to Sec-WebSocket-Extensions header. */
  implicit class SecWebSocketExtensions[T <: HttpMessage](private val message: T) extends AnyVal {
    /** Tests for Sec-WebSocket-Extensions header. */
    def hasSecWebSocketExtensions: Boolean =
      message.hasHeader("Sec-WebSocket-Extensions")

    /**
     * Gets Sec-WebSocket-Extensions header values.
     *
     * @throws HeaderNotFound if Sec-WebSocket-Extensions is not present
     */
    def secWebSocketExtensions: Seq[WebSocketExtension] =
      getSecWebSocketExtensions.getOrElse(Nil)

    /** Gets Sec-WebSocket-Extensions header values if present. */
    def getSecWebSocketExtensions: Option[Seq[WebSocketExtension]] =
      message.getHeaderValues("Sec-WebSocket-Extensions")
        .map(WebSocketExtension.parseAll)
        .reduceLeftOption(_ ++ _)

    /** Creates new message setting Sec-WebSocket-Extensions header to supplied values. */
    def setSecWebSocketExtensions(values: Seq[WebSocketExtension])(implicit ev: <:<[T, MessageBuilder[T]]): T =
      message.putHeaders(Header("Sec-WebSocket-Extensions", values.mkString(", ")))

    /** Creates new message setting Sec-WebSocket-Extensions header to supplied values. */
    def setSecWebSocketExtensions(one: WebSocketExtension, more: WebSocketExtension*)(implicit ev: <:<[T, MessageBuilder[T]]): T =
      setSecWebSocketExtensions(one +: more)

    /** Creates new message removing Sec-WebSocket-Extensions header. */
    def removeSecWebSocketExtensions(implicit ev: <:<[T, MessageBuilder[T]]): T =
      message.removeHeaders("Sec-WebSocket-Extensions")
  }

  /** Provides standardized access to Sec-WebSocket-Key header. */
  implicit class SecWebSocketKey(private val request: HttpRequest) extends AnyVal {
    /** Tests for Sec-WebSocket-Key header. */
    def hasSecWebSocketKey: Boolean =
      request.hasHeader("Sec-WebSocket-Key")

    /**
     * Gets Sec-WebSocket-Key header value.
     *
     * @throws HeaderNotFound if Sec-WebSocket-Key is not present
     */
    def secWebSocketKey: String =
      getSecWebSocketKey.getOrElse(throw HeaderNotFound("Sec-WebSocket-Key"))

    /** Gets Sec-WebSocket-Key header value if present. */
    def getSecWebSocketKey: Option[String] =
      request.getHeaderValue("Sec-WebSocket-Key")

    /** Creates new request setting Sec-WebSocket-Key header to supplied value. */
    def setSecWebSocketKey(value: String): HttpRequest =
      request.putHeaders(Header("Sec-WebSocket-Key", value))

    /** Creates new request removing Sec-WebSocket-Key header. */
    def removeSecWebSocketKey: HttpRequest =
      request.removeHeaders("Sec-WebSocket-Key")
  }

  /** Provides standardized access to Sec-WebSocket-Protocol header. */
  implicit class SecWebSocketProtocol[T <: HttpMessage](private val message: T) extends AnyVal {
    /** Tests for Sec-WebSocket-Protocol header. */
    def hasSecWebSocketProtocol: Boolean =
      message.hasHeader("Sec-WebSocket-Protocol")

    /**
     * Gets Sec-WebSocket-Protocol header values.
     *
     * @throws HeaderNotFound if Sec-WebSocket-Protocol is not present
     */
    def secWebSocketProtocol: Seq[String] =
      getSecWebSocketProtocol.getOrElse(Nil)

    /** Gets Sec-WebSocket-Protocol header values if present. */
    def getSecWebSocketProtocol: Option[Seq[String]] =
      message.getHeaderValue("Sec-WebSocket-Protocol").map(ListParser.apply)

    /** Creates new message setting Sec-WebSocket-Protocol header to supplied values. */
    def setSecWebSocketProtocol(values: Seq[String])(implicit ev: <:<[T, MessageBuilder[T]]): T =
      message.putHeaders(Header("Sec-WebSocket-Protocol", values.mkString(", ")))

    /** Creates new message setting Sec-WebSocket-Protocol header to supplied values. */
    def setSecWebSocketProtocol(one: String, more: String*)(implicit ev: <:<[T, MessageBuilder[T]]): T =
      setSecWebSocketProtocol(one +: more)

    /** Creates new message removing Sec-WebSocket-Protocol header. */
    def removeSecWebSocketProtocol(implicit ev: <:<[T, MessageBuilder[T]]): T =
      message.removeHeaders("Sec-WebSocket-Protocol")
  }

  /** Provides standardized access to Sec-WebSocket-Protocol-Client header. */
  implicit class SecWebSocketProtocolClient(private val request: HttpRequest) extends AnyVal {
    /** Tests for Sec-WebSocket-Protocol-Client header. */
    def hasSecWebSocketProtocolClient: Boolean =
      request.hasHeader("Sec-WebSocket-Protocol-Client")

    /**
     * Gets Sec-WebSocket-Protocol-Client header value.
     *
     * @throws HeaderNotFound if Sec-WebSocket-Protocol-Client is not present
     */
    def secWebSocketProtocolClient: String =
      getSecWebSocketProtocolClient.getOrElse(throw HeaderNotFound("Sec-WebSocket-Protocol-Client"))

    /** Gets Sec-WebSocket-Protocol-Client header value if present. */
    def getSecWebSocketProtocolClient: Option[String] =
      request.getHeaderValue("Sec-WebSocket-Protocol-Client")

    /** Creates new request setting Sec-WebSocket-Protocol-Client header to supplied value. */
    def setSecWebSocketProtocolClient(value: String): HttpRequest =
      request.putHeaders(Header("Sec-WebSocket-Protocol-Client", value))

    /** Creates new request removing Sec-WebSocket-Protocol-Client header. */
    def removeSecWebSocketProtocolClient: HttpRequest =
      request.removeHeaders("Sec-WebSocket-Protocol-Client")
  }

  /** Provides standardized access to Sec-WebSocket-Protocol-Server header. */
  implicit class SecWebSocketProtocolServer(private val response: HttpResponse) extends AnyVal {
    /** Tests for Sec-WebSocket-Protocol-Server header. */
    def hasSecWebSocketProtocolServer: Boolean =
      response.hasHeader("Sec-WebSocket-Protocol-Server")

    /**
     * Gets Sec-WebSocket-Protocol-Server header value.
     *
     * @throws HeaderNotFound if Sec-WebSocket-Protocol-Server is not present
     */
    def secWebSocketProtocolServer: String =
      getSecWebSocketProtocolServer.getOrElse(throw HeaderNotFound("Sec-WebSocket-Protocol-Server"))

    /** Gets Sec-WebSocket-Protocol-Server header value if present. */
    def getSecWebSocketProtocolServer: Option[String] =
      response.getHeaderValue("Sec-WebSocket-Protocol-Server")

    /** Creates new response setting Sec-WebSocket-Protocol-Server header to supplied value. */
    def setSecWebSocketProtocolServer(value: String): HttpResponse =
      response.putHeaders(Header("Sec-WebSocket-Protocol-Server", value))

    /** Creates new response removing Sec-WebSocket-Protocol-Server header. */
    def removeSecWebSocketProtocolServer: HttpResponse =
      response.removeHeaders("Sec-WebSocket-Protocol-Server")
  }

  /** Provides standardized access to Sec-WebSocket-Version header. */
  implicit class SecWebSocketVersion[T <: HttpMessage](private val message: T) extends AnyVal {
    /** Tests for Sec-WebSocket-Version header. */
    def hasSecWebSocketVersion: Boolean =
      message.hasHeader("Sec-WebSocket-Version")

    /**
     * Gets Sec-WebSocket-Version header value.
     *
     * @throws HeaderNotFound if Sec-WebSocket-Version is not present
     */
    def secWebSocketVersion: String =
      getSecWebSocketVersion.getOrElse(throw HeaderNotFound("Sec-WebSocket-Version"))

    /** Gets Sec-WebSocket-Version header value if present. */
    def getSecWebSocketVersion: Option[String] =
      message.getHeaderValue("Sec-WebSocket-Version")

    /** Creates new message setting Sec-WebSocket-Version header to supplied value. */
    def setSecWebSocketVersion(value: String)(implicit ev: <:<[T, MessageBuilder[T]]): T =
      message.putHeaders(Header("Sec-WebSocket-Version", value))

    /** Creates new message removing Sec-WebSocket-Version header. */
    def removeSecWebSocketVersion(implicit ev: <:<[T, MessageBuilder[T]]): T =
      message.removeHeaders("Sec-WebSocket-Version")
  }

  /** Provides standardized access to Sec-WebSocket-Version-Client header. */
  implicit class SecWebSocketVersionClient(private val request: HttpRequest) extends AnyVal {
    /** Tests for Sec-WebSocket-Version-Client header. */
    def hasSecWebSocketVersionClient: Boolean =
      request.hasHeader("Sec-WebSocket-Version-Client")

    /**
     * Gets Sec-WebSocket-Version-Client header values.
     *
     * @throws HeaderNotFound if Sec-WebSocket-Version-Client is not present
     */
    def secWebSocketVersionClient: Seq[String] =
      getSecWebSocketVersionClient.getOrElse(Nil)

    /** Gets Sec-WebSocket-Version-Client header values if present. */
    def getSecWebSocketVersionClient: Option[Seq[String]] =
      request.getHeaderValue("Sec-WebSocket-Version-Client")
        .map(ListParser.apply)

    /** Creates new request setting Sec-WebSocket-Version-Client header to supplied values. */
    def setSecWebSocketVersionClient(values: Seq[String]): HttpRequest =
      request.putHeaders(Header("Sec-WebSocket-Version-Client", values.mkString(", ")))

    /** Creates new request setting Sec-WebSocket-Version-Client header to supplied values. */
    def setSecWebSocketVersionClient(one: String, more: String*): HttpRequest =
      setSecWebSocketVersionClient(one +: more)

    /** Creates new request removing Sec-WebSocket-Version-Client header. */
    def removeSecWebSocketVersionClient: HttpRequest =
      request.removeHeaders("Sec-WebSocket-Version-Client")
  }

  /** Provides standardized access to Sec-WebSocket-Version-Server header. */
  implicit class SecWebSocketVersionServer(private val response: HttpResponse) extends AnyVal {
    /** Tests for Sec-WebSocket-Version-Server header. */
    def hasSecWebSocketVersionServer: Boolean =
      response.hasHeader("Sec-WebSocket-Version-Server")

    /**
     * Gets Sec-WebSocket-Version-Server header values.
     *
     * @throws HeaderNotFound if Sec-WebSocket-Version-Server is not present
     */
    def secWebSocketVersionServer: Seq[String] =
      getSecWebSocketVersionServer.getOrElse(Nil)

    /** Gets Sec-WebSocket-Version-Server header values if present. */
    def getSecWebSocketVersionServer: Option[Seq[String]] =
      response.getHeaderValue("Sec-WebSocket-Version-Server")
        .map(ListParser.apply)

    /** Creates new response setting Sec-WebSocket-Version-Server header to supplied values. */
    def setSecWebSocketVersionServer(values: Seq[String]): HttpResponse =
      response.putHeaders(Header("Sec-WebSocket-Version-Server", values.mkString(", ")))

    /** Creates new response setting Sec-WebSocket-Version-Server header to supplied values. */
    def setSecWebSocketVersionServer(one: String, more: String*): HttpResponse =
      setSecWebSocketVersionServer(one +: more)

    /** Creates new response removing Sec-WebSocket-Version-Server header. */
    def removeSecWebSocketVersionServer: HttpResponse =
      response.removeHeaders("Sec-WebSocket-Version-Server")
  }
}
