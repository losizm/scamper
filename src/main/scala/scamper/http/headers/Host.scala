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
package headers

/** Provides standardized access to Host header. */
given toHost: Conversion[HttpRequest, Host] = Host(_)

/** Provides standardized access to Host header. */
class Host(request: HttpRequest) extends AnyVal:
  /** Tests for Host header. */
  def hasHost: Boolean =
    request.hasHeader("Host")

  /**
   * Gets Host header value.
   *
   * @throws HeaderNotFound if Host is not present
   */
  def host: String =
    hostOption.getOrElse(throw HeaderNotFound("Host"))

  /** Gets Host header value if present. */
  def hostOption: Option[String] =
    request.getHeaderValue("Host")

  /** Creates new request with Host header set to supplied value. */
  def setHost(value: String): HttpRequest =
    request.putHeaders(Header("Host", value))

  /** Creates new request with Host header removed. */
  def hostRemoved: HttpRequest =
    request.removeHeaders("Host")
