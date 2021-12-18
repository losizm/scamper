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

/** Provides standardized access to Server header. */
implicit class Server(response: HttpResponse) extends AnyVal:
  /** Tests for Server header. */
  def hasServer: Boolean =
    response.hasHeader("Server")

  /**
   * Gets Server header values.
   *
   * @return header values or empty sequence if Server is not present
   */
  def server: Seq[ProductType] =
    getServer.getOrElse(Nil)

  /** Gets Server header values if present. */
  def getServer: Option[Seq[ProductType]] =
    response.getHeaderValue("Server").map(ProductType.parseAll)

  /** Creates new response with Server header set to supplied values. */
  def setServer(values: Seq[ProductType]): HttpResponse =
    response.putHeaders(Header("Server", values.mkString(" ")))

  /** Creates new response with Server header set to supplied values. */
  def setServer(one: ProductType, more: ProductType*): HttpResponse =
    setServer(one +: more)

  /** Creates new response with Server header removed. */
  def removeServer: HttpResponse =
    response.removeHeaders("Server")
