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

/** Adds standardized access to Authentication-Info header. */
given toAuthenticationInfo: Conversion[HttpResponse, AuthenticationInfo] =  AuthenticationInfo(_)

/** Adds standardized access to Authentication-Info header. */
class AuthenticationInfo(response: HttpResponse) extends AnyVal:
  /** Tests for Authentication-Info header. */
  def hasAuthenticationInfo: Boolean =
    response.hasHeader("Authentication-Info")

  /**
   * Gets Authentication-Info header values.
   *
   * @return header value or empty map if Authentication-Info is not present
   */
  def authenticationInfo: Map[String, String] =
    authenticationInfoOption.getOrElse(Map.empty)

  /** Gets Authentication-Info header value if present. */
  def authenticationInfoOption: Option[Map[String, String]] =
    response.getHeaderValue("Authentication-Info").map(AuthParams.parse)

  /** Creates new response with Authentication-Info header set to supplied values. */
  def setAuthenticationInfo(values: Map[String, String]): HttpResponse =
    response.putHeaders(Header("Authentication-Info", AuthParams.format(values.toMap).trim))

  /** Creates new response with Authentication-Info header set to supplied values. */
  def setAuthenticationInfo(one: (String, String), more: (String, String)*): HttpResponse =
    setAuthenticationInfo((one +: more).toMap)

  /** Creates new response with Authentication-Info header removed. */
  def authenticationInfoRemoved: HttpResponse =
    response.removeHeaders("Authentication-Info")
