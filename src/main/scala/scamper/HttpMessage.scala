/*
 * Copyright 2019 Carlos Conyers
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

import java.net.URI

/** HTTP message */
sealed trait HttpMessage {
  /** Type of start line used in message */
  type LineType <: StartLine

  /** Gets message start line. */
  def startLine: LineType

  /** Gets HTTP version. */
  def version: HttpVersion = startLine.version

  /** Gets message headers. */
  def headers: Seq[Header]

  /** Gets message body. */
  def body: Entity

  /**
   * Gets message attributes.
   *
   * Attributes are arbitrary values associated with message and are not part of
   * transmitted message.
   */
  def attributes: Map[String, Any]

  /** Parses message body as defined type. */
  def as[T](implicit parser: BodyParser[T]): T =
    parser.parse(this)

  /** Tests whether header with given name is present. */
  def hasHeader(name: String): Boolean =
    headers.exists(_.name.equalsIgnoreCase(name))

  /** Gets first header with given name.  */
  def getHeader(name: String): Option[Header] =
    headers.find(_.name.equalsIgnoreCase(name))

  /** Gets first header with given name, or returns default if header not present. */
  def getHeaderOrElse(name: String, default: => Header): Header =
    getHeader(name).getOrElse(default)

  /** Gets value of first header with given name. */
  def getHeaderValue(name: String): Option[String] =
    getHeader(name).map(_.value)

  /**
   * Gets value of first header with given name, or returns default if header not
   * present.
   */
  def getHeaderValueOrElse(name: String, default: => String): String =
    getHeaderValue(name).getOrElse(default)

  /** Gets all headers with given name. */
  def getHeaders(name: String): Seq[Header] =
    headers.filter(_.name.equalsIgnoreCase(name))

  /** Gets value of all headers with given name. */
  def getHeaderValues(name: String): Seq[String] =
    getHeaders(name).map(_.value)

  /**
   * Gets value of attribute with given name.
   *
   * @param name attribute name
   */
  def getAttribute[T](name: String): Option[T] =
    attributes.get(name).map(_.asInstanceOf[T])

  /**
   * Gets value of attribute with given name, or returns default if attribute
   * not present.
   *
   * @param name attribute name
   * @param default default value
   */
  def getAttributeOrElse[T](name: String, default: => T): T =
    getAttribute(name).getOrElse(default)
}

/**
 * HTTP request
 *
 * @see [[HttpResponse]]
 */
trait HttpRequest extends HttpMessage with MessageBuilder[HttpRequest] {
  type LineType = RequestLine

  /** Gets request method. */
  def method: RequestMethod = startLine.method

  /** Gets request target. */
  def target: URI = startLine.target

  /** Gets target path. */
  def path: String

  /** Gets query string. */
  def query: QueryString

  /**
   * Creates request with new method.
   *
   * @return new request
   */
  def withMethod(method: RequestMethod): HttpRequest

  /**
   * Creates request with new target.
   *
   * @return new request
   */
  def withTarget(target: URI): HttpRequest

  /**
   * Creates request with new target path.
   *
   * @return new request
   */
  def withPath(path: String): HttpRequest

  /**
   * Creates request with new query.
   *
   * @return new request
   */
  def withQuery(query: QueryString): HttpRequest

  /**
   * Creates request with new query parameters.
   *
   * @return new request
   */
  def withQuery(params: Map[String, Seq[String]]): HttpRequest

  /**
   * Creates request with new query parameters.
   *
   * @return new request
   */
  def withQuery(params: (String, String)*): HttpRequest

  /**
   * Creates request with new HTTP version.
   *
   * @return new request
   */
  def withVersion(version: HttpVersion): HttpRequest
}

/** HttpRequest factory */
object HttpRequest {
  /** Creates HttpRequest with supplied values. */
  def apply(requestLine: RequestLine, headers: Seq[Header], body: Entity): HttpRequest =
    HttpRequestImpl(requestLine, headers, body)

  /** Creates HttpRequest with supplied values. */
  def apply(method: RequestMethod, target: URI = new URI("/"), headers: Seq[Header] = Nil, body: Entity = Entity.empty, version: HttpVersion = HttpVersion(1, 1)): HttpRequest =
    HttpRequestImpl(RequestLine(method, target, version), headers, body)
}

/**
 * HTTP response
 *
 * @see [[HttpRequest]]
 */
trait HttpResponse extends HttpMessage with MessageBuilder[HttpResponse] {
  type LineType = StatusLine

  /** Gets response status. */
  def status: ResponseStatus = startLine.status

  /**
   * Creates response with new response status.
   *
   * @return new response
   */
  def withStatus(status: ResponseStatus): HttpResponse

  /**
   * Creates response with new HTTP version.
   *
   * @return new response
   */
  def withVersion(version: HttpVersion): HttpResponse
}

/** HttpResponse factory */
object HttpResponse {
  /** Creates HttpResponse with supplied values. */
  def apply(statusLine: StatusLine, headers: Seq[Header], body: Entity): HttpResponse =
    HttpResponseImpl(statusLine, headers, body)

  /** Creates HttpResponse with supplied values. */
  def apply(status: ResponseStatus, headers: Seq[Header] = Nil, body: Entity = Entity.empty, version: HttpVersion = HttpVersion(1, 1)): HttpResponse =
    HttpResponseImpl(StatusLine(status, version), headers, body)
}
