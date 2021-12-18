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

import java.net.Socket

/** Adds client extensions to `HttpMessage`. */
implicit class ClientHttpMessage(message: HttpMessage) extends AnyVal:
  /** Gets message socket. */
  def socket: Socket = message.getAttribute("scamper.http.client.message.socket").get

  /**
   * Gets message correlate.
   *
   * Each outgoing request is assigned a tag (i.e., correlate), which is later
   * reassigned to its incoming response.
   */
  def correlate: String = message.getAttribute("scamper.http.client.message.correlate").get

  /**
   * Gets absolute target.
   *
   * The absolute target (i.e., absolute URI) is assigned to each outgoing
   * request and later reassigned to its incoming response.
   */
  def absoluteTarget: Uri = message.getAttribute("scamper.http.client.message.absoluteTarget").get

  /** Gets client to which this message belongs. */
  def client: HttpClient = message.getAttribute("scamper.http.client.message.client").get
