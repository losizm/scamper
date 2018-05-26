/*
 * Copyright 2018 Carlos Conyers
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

/**
 * HTTP response status
 *
 * @see [[ResponseStatuses]]
 */
trait ResponseStatus {
  /** Status code */
  def code: Int

  /** Reason phrase */
  def reason: String

  /** Tests for informational status code. */
  def isInformational: Boolean =
    code >= 100 && code <= 199

  /** Tests for successful status code. */
  def isSuccessful: Boolean =
    code >= 200 && code <= 299

  /** Tests for redirection status code. */
  def isRedirection: Boolean =
    code >= 300 && code <= 399

  /** Tests for client error status code. */
  def isClientError: Boolean =
    code >= 400 && code <= 499

  /** Tests for server error status code. */
  def isServerError: Boolean =
    code >= 500 && code <= 599

  /** Converts to HttpResponse with supplied body. */
  def apply(body: Entity = Entity.empty): HttpResponse =
    HttpResponse(this, Nil, body)
}

/**
 * ResponseStatus factory
 *
 * @see [[ResponseStatuses]]
 */
object ResponseStatus {
  private val statuses = new scala.collection.mutable.TreeMap[Int, ResponseStatus]

  /**
   * Gets registered ResponseStatus for given status code.
   *
   * {@code NoSuchElementException} is thrown if a response status is not
   * registered for status code.
   */
  def apply(code: Int): ResponseStatus =
    statuses(code)

  /** Creates ResponseStatus with supplied code and reason. */
  def apply(code: Int, reason: String): ResponseStatus =
    ResponseStatusImpl(code, reason)

  /** Destructures ResponseStatus. */
  def unapply(status: ResponseStatus): Option[(Int, String)] =
    Some((status.code, status.reason))

  private def add(code: Int, reason: String): Unit =
    statuses += code -> ResponseStatus(code, reason)

  add(100, "Continue")
  add(101, "Switching Protocols")
  add(200, "OK")
  add(201, "Created")
  add(202, "Accepted")
  add(203, "Non-Authoritative Information")
  add(204, "No Content")
  add(205, "Reset Content")
  add(206, "Partial Content")
  add(300, "Multiple Choices")
  add(301, "Moved Permanently")
  add(302, "Found")
  add(303, "See Other")
  add(304, "Not Modified")
  add(305, "Use Proxy")
  add(307, "Temporary Redirect")
  add(400, "Bad Request")
  add(401, "Unauthorized")
  add(402, "Payment Required")
  add(403, "Forbidden")
  add(404, "Not Found")
  add(405, "Method Not Allowed")
  add(406, "Not Acceptable")
  add(407, "Proxy Authentication Required")
  add(408, "Request Timeout")
  add(409, "Conflict")
  add(410, "Gone")
  add(411, "Length Required")
  add(412, "Precondition Failed")
  add(413, "Payload Too Large")
  add(414, "URI Too Long")
  add(415, "Unsupported Media Type")
  add(416, "Range Not Satisfiable")
  add(417, "Expectation Failed")
  add(426, "Upgrade Required")
  add(500, "Internal Server Error")
  add(501, "Not Implemented")
  add(502, "Bad Gateway")
  add(503, "Service Unavailable")
  add(504, "Gateway Timeout")
  add(505, "HTTP Version Not Supported")
}

private case class ResponseStatusImpl(code: Int, reason: String) extends ResponseStatus {
  override lazy val toString: String = s"$code $reason"
}
