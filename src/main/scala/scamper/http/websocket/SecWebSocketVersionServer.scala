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
package websocket

/** Provides standardized access to Sec-WebSocket-Version-Server header. */
implicit class SecWebSocketVersionServer(response: HttpResponse) extends AnyVal:
  /** Tests for Sec-WebSocket-Version-Server header. */
  def hasSecWebSocketVersionServer: Boolean =
    response.hasHeader("Sec-WebSocket-Version-Server")

  /**
   * Gets Sec-WebSocket-Version-Server header values.
   *
   * @throws HeaderNotFound if Sec-WebSocket-Version-Server is not present
   */
  def secWebSocketVersionServer: Seq[String] =
    secWebSocketVersionServerOption.getOrElse(Nil)

  /** Gets Sec-WebSocket-Version-Server header values if present. */
  def secWebSocketVersionServerOption: Option[Seq[String]] =
    response.getHeaderValue("Sec-WebSocket-Version-Server")
      .map(ListParser.apply)

  /** Creates new response with Sec-WebSocket-Version-Server header set to supplied values. */
  def setSecWebSocketVersionServer(values: Seq[String]): HttpResponse =
    response.putHeaders(Header("Sec-WebSocket-Version-Server", values.mkString(", ")))

  /** Creates new response with Sec-WebSocket-Version-Server header set to supplied values. */
  def setSecWebSocketVersionServer(one: String, more: String*): HttpResponse =
    setSecWebSocketVersionServer(one +: more)

  /** Creates new response with Sec-WebSocket-Version-Server header removed. */
  def secWebSocketVersionServerRemoved: HttpResponse =
    response.removeHeaders("Sec-WebSocket-Version-Server")
