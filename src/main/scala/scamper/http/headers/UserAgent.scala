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

import scamper.http.types.ProductType

/** Provides standardized access to User-Agent header. */
implicit class UserAgent(request: HttpRequest) extends AnyVal:
  /** Tests for User-Agent header. */
  def hasUserAgent: Boolean =
    request.hasHeader("User-Agent")

  /**
   * Gets User-Agent header values.
   *
   * @return header values or empty sequence if User-Agent is not present
   */
  def userAgent: Seq[ProductType] =
    getUserAgent.getOrElse(Nil)

  /** Gets User-Agent header values if present. */
  def getUserAgent: Option[Seq[ProductType]] =
    request.getHeaderValue("User-Agent").map(ProductType.parseAll)

  /** Creates new request with User-Agent header set to supplied value. */
  def setUserAgent(values: Seq[ProductType]): HttpRequest =
    request.putHeaders(Header("User-Agent", values.mkString(" ")))

  /** Creates new request with User-Agent header set to supplied value. */
  def setUserAgent(one: ProductType, more: ProductType*): HttpRequest =
    setUserAgent(one +: more)

  /** Creates new request with User-Agent header removed. */
  def removeUserAgent: HttpRequest =
    request.removeHeaders("User-Agent")
