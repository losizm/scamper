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

/** Provides standardized access to Sec-WebSocket-Version-Client header. */
implicit class SecWebSocketVersionClient(request: HttpRequest) extends AnyVal:
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

  /** Creates new request with Sec-WebSocket-Version-Client header set to supplied values. */
  def setSecWebSocketVersionClient(values: Seq[String]): HttpRequest =
    request.putHeaders(Header("Sec-WebSocket-Version-Client", values.mkString(", ")))

  /** Creates new request with Sec-WebSocket-Version-Client header set to supplied values. */
  def setSecWebSocketVersionClient(one: String, more: String*): HttpRequest =
    setSecWebSocketVersionClient(one +: more)

  /** Creates new request with Sec-WebSocket-Version-Client header removed. */
  def removeSecWebSocketVersionClient: HttpRequest =
    request.removeHeaders("Sec-WebSocket-Version-Client")
