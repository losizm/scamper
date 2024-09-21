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
package auth

/** Adds standardized access to Proxy-Authentication-Info header. */
given toProxyAuthenticationInfo: Conversion[HttpResponse, ProxyAuthenticationInfo] = ProxyAuthenticationInfo(_)

/** Adds standardized access to Proxy-Authentication-Info header. */
class ProxyAuthenticationInfo(response: HttpResponse) extends AnyVal:
  /** Tests for Proxy-Authentication-Info header. */
  def hasProxyAuthenticationInfo: Boolean =
    response.hasHeader("Proxy-Authentication-Info")

  /**
   * Gets Proxy-Authentication-Info header values.
   *
   * @return header value or empty map if Proxy-Authentication-Info is not present
   */
  def proxyAuthenticationInfo: Map[String, String] =
    proxyAuthenticationInfoOption.getOrElse(Map.empty)

  /** Gets Proxy-Authentication-Info header value if present. */
  def proxyAuthenticationInfoOption: Option[Map[String, String]] =
    response.getHeaderValue("Proxy-Authentication-Info").map(AuthParams.parse)

  /** Creates new response with Proxy-Authentication-Info header set to supplied values. */
  def setProxyAuthenticationInfo(values: Map[String, String]): HttpResponse =
    response.putHeaders(Header("Proxy-Authentication-Info", AuthParams.format(values).trim))

  /** Creates new response with Proxy-Authentication-Info header set to supplied values. */
  def setProxyAuthenticationInfo(one: (String, String), more: (String, String)*): HttpResponse =
    setProxyAuthenticationInfo((one +: more).toMap)

  /** Creates new response with Proxy-Authentication-Info header removed. */
  def proxyAuthenticationInfoRemoved: HttpResponse =
    response.removeHeaders("Proxy-Authentication-Info")
