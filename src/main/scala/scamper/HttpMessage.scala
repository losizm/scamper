/*
 * Copyright 2017-2020 Carlos Conyers
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

/** Defines HTTP message. */
sealed trait HttpMessage {
  /** Type of start line in message */
  type LineType <: StartLine

  /** Gets message start line. */
  def startLine: LineType

  /** Gets HTTP version. */
  def version: HttpVersion =
    startLine.version

  /** Gets message headers. */
  def headers: Seq[Header]

  /** Gets message body. */
  def body: Entity

  /**
   * Gets message attributes.
   *
   * @note Attributes are arbitrary values associated with message and are not
   * part of transmitted message.
   */
  def attributes: Map[String, Any]

  /**
   * Gets message body as instance of `T`.
   *
   * @param parser body parser
   */
  def as[T](implicit parser: BodyParser[T]): T =
    parser.parse(this)

  /** Tests for header with given name. */
  def hasHeader(name: String): Boolean =
    headers.exists(_.name.equalsIgnoreCase(name))

  /** Gets first header with given name. */
  def getHeader(name: String): Option[Header] =
    headers.find(_.name.equalsIgnoreCase(name))

  /** Gets first header with given name, or returns default if header not present. */
  def getHeaderOrElse(name: String, default: => Header): Header =
    getHeader(name).getOrElse(default)

  /** Gets first header value with given name. */
  def getHeaderValue(name: String): Option[String] =
    getHeader(name).map(_.value)

  /**
   * Gets first header value with given name, or returns default if header not
   * present.
   */
  def getHeaderValueOrElse(name: String, default: => String): String =
    getHeaderValue(name).getOrElse(default)

  /** Gets headers with given name. */
  def getHeaders(name: String): Seq[Header] =
    headers.filter(_.name.equalsIgnoreCase(name))

  /** Gets header values with given name. */
  def getHeaderValues(name: String): Seq[String] =
    getHeaders(name).map(_.value)

  /**
   * Gets attribute value with given name.
   *
   * @param name attribute name
   */
  def getAttribute[T](name: String): Option[T] =
    attributes.get(name).map(_.asInstanceOf[T])

  /**
   * Gets attribute value with given name, or returns default if attribute not
   * present.
   *
   * @param name attribute name
   * @param default default value
   */
  def getAttributeOrElse[T](name: String, default: => T): T =
    getAttribute(name).getOrElse(default)
}

/**
 * Defines HTTP request.
 *
 * @see [[HttpResponse]]
 */
trait HttpRequest extends HttpMessage with MessageBuilder[HttpRequest] {
  type LineType = RequestLine

  /** Gets request method. */
  def method: RequestMethod =
    startLine.method

  /** Tests for GET method. */
  def isGet: Boolean =
    method == RequestMethod.Registry.Get

  /** Tests for POST method. */
  def isPost: Boolean =
    method == RequestMethod.Registry.Post

  /** Tests for PUT method. */
  def isPut: Boolean =
    method == RequestMethod.Registry.Put

  /** Tests for PATCH method. */
  def isPatch: Boolean =
    method == RequestMethod.Registry.Patch

  /** Tests for DELETE method. */
  def isDelete: Boolean =
    method == RequestMethod.Registry.Delete

  /** Tests for HEAD method. */
  def isHead: Boolean =
    method == RequestMethod.Registry.Head

  /** Tests for OPTIONS method. */
  def isOptions: Boolean =
    method == RequestMethod.Registry.Options

  /** Tests for TRACE method. */
  def isTrace: Boolean =
    method == RequestMethod.Registry.Trace

  /** Tests for CONNECT method. */
  def isConnect: Boolean =
    method == RequestMethod.Registry.Connect

  /** Gets request target. */
  def target: Uri =
    startLine.target

  /** Gets target path. */
  def path: String

  /** Gets query string. */
  def query: QueryString

  /**
   * Creates request with new method.
   *
   * @return new request
   */
  def setMethod(method: RequestMethod): HttpRequest =
    setStartLine(RequestLine(method, target, version))

  /**
   * Creates request with new target.
   *
   * @return new request
   */
  def setTarget(target: Uri): HttpRequest =
    setStartLine(RequestLine(method, target, version))

  /**
   * Creates request with new target path.
   *
   * @return new request
   */
  def setPath(path: String): HttpRequest

  /**
   * Creates request with new query.
   *
   * @return new request
   */
  def setQuery(query: QueryString): HttpRequest

  /**
   * Creates request with new query using supplied parameters.
   *
   * @return new request
   */
  def setQuery(params: Map[String, Seq[String]]): HttpRequest =
    setQuery(QueryString(params))

  /**
   * Creates request with new query using supplied parameters.
   *
   * @return new request
   */
  def setQuery(params: Seq[(String, String)]): HttpRequest =
    setQuery(QueryString(params))

  /**
   * Creates request with new query using supplied parameters.
   *
   * @return new request
   */
  def setQuery(one: (String, String), more: (String, String)*): HttpRequest =
    setQuery(one +: more)

  /**
   * Creates request with new HTTP version.
   *
   * @return new request
   */
  def setVersion(version: HttpVersion): HttpRequest =
    setStartLine(RequestLine(method, target, version))

}

/** Provides factory for `HttpRequest`. */
object HttpRequest {
  /** Creates request with supplied message parts. */
  def apply(requestLine: RequestLine, headers: Seq[Header], body: Entity): HttpRequest =
    HttpRequestImpl(requestLine, headers, body)

  /** Creates request with supplied message parts. */
  def apply(method: RequestMethod, target: Uri = Uri("/"), headers: Seq[Header] = Nil, body: Entity = Entity.empty, version: HttpVersion = HttpVersion(1, 1)): HttpRequest =
    HttpRequestImpl(RequestLine(method, target, version), headers, body)
}

/**
 * Defines HTTP response.
 *
 * @see [[HttpRequest]]
 */
trait HttpResponse extends HttpMessage with MessageBuilder[HttpResponse] {
  type LineType = StatusLine

  /** Gets response status. */
  def status: ResponseStatus =
    startLine.status

  /** Gets status code. */
  def statusCode: Int =
   status.statusCode

  /** Gets reason phrase. */
  def reasonPhrase: String =
    status.reasonPhrase

  /** Tests for informational status. */
  def isInformational: Boolean =
    status.isInformational

  /** Tests for successful status. */
  def isSuccessful: Boolean =
    status.isSuccessful

  /** Tests for redirection status. */
  def isRedirection: Boolean =
    status.isRedirection

  /** Tests for client error status. */
  def isClientError: Boolean =
    status.isClientError

  /** Tests for server error status. */
  def isServerError: Boolean =
    status.isServerError

  /**
   * Creates response with new status.
   *
   * @return new response
   */
  def setStatus(status: ResponseStatus): HttpResponse =
    setStartLine(StatusLine(status, version))

  /**
   * Creates response with new HTTP version.
   *
   * @return new response
   */
  def setVersion(version: HttpVersion): HttpResponse =
    setStartLine(StatusLine(status, version))
}

/** Provides factory for `HttpResponse`. */
object HttpResponse {
  /** Creates response with supplied message parts. */
  def apply(statusLine: StatusLine, headers: Seq[Header], body: Entity): HttpResponse =
    HttpResponseImpl(statusLine, headers, body)

  /** Creates response with supplied message parts. */
  def apply(status: ResponseStatus, headers: Seq[Header] = Nil, body: Entity = Entity.empty, version: HttpVersion = HttpVersion(1, 1)): HttpResponse =
    HttpResponseImpl(StatusLine(status, version), headers, body)
}
