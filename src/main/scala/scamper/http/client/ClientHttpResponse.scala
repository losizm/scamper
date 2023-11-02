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
package client

/** Adds client extensions to `HttpResponse`. */
given toClientHttpResponse: Conversion[HttpResponse, ClientHttpResponse] = ClientHttpResponse(_)

/** Adds client extensions to `HttpResponse`. */
class ClientHttpResponse(response: HttpResponse) extends AnyVal:
  /**
   * Gets corresponding request.
   *
   * @note The request is the outgoing request after filters are applied, and
   * the message entity's input stream is an active object.
   */
  def request: HttpRequest =
    response.getAttribute("scamper.http.client.response.request").get

  /**
   * Claims ownership of response.
   *
   * The owner is responsible for managing message resources, such
   * as underlying socket connection.
   */
  def claim(): HttpResponse =
    response.getAttribute[HttpClientConnection]("scamper.http.client.message.connection")
      .get
      .setManaged(false)
    response
