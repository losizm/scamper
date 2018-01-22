package scamper

import scala.util.Try

/**
 * A representation of an HTTP message.
 *
 * @see [[HttpRequest]], [[HttpResponse]]
 */
trait HttpMessage {
  type MessageType <: HttpMessage
  type LineType <: StartLine

  /** Message start line */
  def startLine: LineType

  /** Sequence of message headers */
  def headers: Seq[Header]

  /**
   * Gets header value for specified key.
   *
   * If there are multiple headers for key, then the value of first occurrence
   * is retrieved.
   */
  def getHeaderValue(key: String): Option[String] =
    headers.collectFirst {
      case Header(k, value) if k.equalsIgnoreCase(key) => value
    }

  /** Gets all header values for specified key. */
  def getHeaderValues(key: String): List[String] =
    headers.collect {
      case Header(k, value) if k.equalsIgnoreCase(key) => value
    }.toList

  /** Message body */
  def body: Entity

  /** Parses the message body. */
  def parse[T](implicit bodyParser: BodyParser[T]): Try[T] =
    Try(bodyParser(this))

  /**
   * Gets the content type.
   *
   * The value is retrieved from the Content-Type header.
   */
  def contentType: Option[ContentType] =
    getHeaderValue("Content-Type").map(ContentType.apply)

  /**
   * Gets the content length.
   *
   * The value is retrieved from the Content-Length header.
   */
  def contentLength: Option[Long] =
    getHeaderValue("Content-Length").map(_.toLong)

  /**
   * Gets the content encoding.
   *
   * The value is retrieved from the Content-Encoding header.
   */
  def contentEncoding: Option[String] =
    getHeaderValue("Content-Encoding")

  /**
   * Tests whether the message body is chunked.
   *
   * This is determined by inspecting the Transfer-Encoding header.
   */
  def isChunked: Boolean =
    getHeaderValue("Transfer-Encoding").exists("chunked".equalsIgnoreCase)

  /**
   * Creates a copy of this message replacing the start line.
   *
   * @return the new message
   */
  def withStartLine(line: LineType): MessageType

  /**
   * Creates a copy of this message replacing the supplied header.
   *
   * All previous headers having the same key as supplied header are removed and
   * replaced with the single header instance.
   *
   * @return the new message
   */
  def withHeader(header: Header): MessageType =
    withHeaders {
      headers.filterNot(_.key.equalsIgnoreCase(header.key)) :+ header : _*
    }

  /**
   * Creates a copy of this message removing all headers having the supplied
   * key.
   *
   * @return the new message
   */
  def withoutHeader(key: String): MessageType =
    withHeaders {
      headers.filterNot(_.key.equalsIgnoreCase(key)) : _*
    }

  /**
   * Creates a copy of this message including additional headers.
   *
   * @return the new message
   */
  def addHeaders(headers: Header*): MessageType

  /**
   * Creates a copy of this message replacing the headers.
   *
   * All previous headers are removed, and the new message contains only the
   * supplied headers.
   *
   * @return the new message
   */
  def withHeaders(headers: Header*): MessageType

  /**
   * Creates a copy of this message replacing the body.
   *
   * @return the new message
   */
  def withBody(body: Entity): MessageType

  /**
   * Creates a copy of this message replacing the content type.
   *
   * @return the new message
   */
  def withContentType(contentType: ContentType): MessageType =
    withHeader(Header("Content-Type", contentType.toString))

  /**
   * Creates a copy of this message replacing the content length.
   *
   * @return the new message
   */
  def withContentLength(length: Long): MessageType =
    withHeader(Header("Content-Length", length.toString))

  /**
   * Creates a copy of this message replacing the content encoding.
   *
   * @return the new message
   */
  def withContentEncoding(encoding: String): MessageType =
    withHeader(Header("Content-Encoding", encoding))

  /**
   * Creates a copy of this message replacing the transfer encoding.
   *
   * If chunked is true, then the Transfer-Encoding header is set to chunked;
   * otherwise, the header is removed.
   *
   * @return the new message
   */
  def withChunked(chunked: Boolean): MessageType =
    if (chunked) withHeader(Header("Transfer-Encoding", "chunked"))
    else withoutHeader("Transfer-Encoding")
}

/** A representation of an HTTP request. */
trait HttpRequest extends HttpMessage {
  type MessageType = HttpRequest
  type LineType = RequestLine

  /** Request method (i.e., GET, POST, etc.) */
  def method: String = startLine.method

  /** Request URI */
  def uri: String = startLine.uri

  /** HTTP version of request message */
  def version: Version = startLine.version

  /** Path component of URI */
  def path: String

  /** Query component of URI */
  def query: Option[String]

  /** Query parameters from URI */
  lazy val queryParameters: Map[String, List[String]] =
    query.map(QueryParser.parse).getOrElse(Map.empty)

  /**
   * Gets value for named query parameter.
   *
   * If there are multiple parameters with given name, then the value of first
   * occurrence is retrieved.
   */
  def getQueryParameterValue(name: String): Option[String] =
    queryParameters.get(name).flatMap(_.headOption)

  /** Gets all values for named query parameter. */
  def getQueryParameterValues(name: String): List[String] =
    queryParameters.get(name).getOrElse(Nil)

  /**
   * Gets the requested host.
   *
   * The value is retrieved from the Host header.
   */
  def host: Option[String] =
    getHeaderValue("Host")

  /**
   * Creates a copy of this request replacing the request method.
   *
   * @return the new request
   */
  def withMethod(method: String): MessageType

  /**
   * Creates a copy of this request replacing the request URI.
   *
   * @return the new request
   */
  def withURI(uri: String): MessageType

  /**
   * Creates a copy of this request replacing the HTTP version.
   *
   * @return the new request
   */
  def withVersion(version: Version): MessageType

  /**
   * Creates a copy of this request replacing the request path.
   *
   * @return the new request
   */
  def withPath(path: String): HttpRequest

  /**
   * Creates a copy of this request replacing the request query.
   *
   * @return the new request
   */
  def withQuery(query: String): HttpRequest

  /**
   * Creates a copy of this request replacing the request query parameters.
   *
   * @return the new request
   */
  def withQueryParameters(params: Map[String, List[String]]): HttpRequest

  /**
   * Creates a copy of this message replacing the host.
   *
   * @return the new message
   */
  def withHost(host: String): MessageType =
    withHeader(Header("Host", host))
}

/** Provides HttpRequest factory methods. */
object HttpRequest {
  /** Creates an HttpRequest using the supplied attributes. */
  def apply(requestLine: RequestLine, headers: Seq[Header], body: Entity): HttpRequest =
    SimpleHttpRequest(requestLine, headers, body)

  /** Creates an HttpRequest using the supplied attributes. */
  def apply(method: String, uri: String, headers: Seq[Header] = Nil, body: Entity = Entity.empty, version: Version = Version(1, 1)): HttpRequest =
    SimpleHttpRequest(RequestLine(method, uri, version), headers, body)
}

private case class SimpleHttpRequest(startLine: RequestLine, headers: Seq[Header], body: Entity) extends HttpRequest {
  private lazy val uriObject = new java.net.URI(uri)

  lazy val path = uriObject.getPath
  lazy val query = Option(uriObject.getRawQuery)

  def addHeaders(moreHeaders: Header*): HttpRequest =
    copy(headers = headers ++ moreHeaders)

  def withHeaders(newHeaders: Header*): HttpRequest =
    copy(headers = newHeaders)

  def withBody(newBody: Entity): HttpRequest =
    copy(body = newBody)

  def withStartLine(line: RequestLine): HttpRequest =
    copy(startLine = line)

  def withMethod(newMethod: String): HttpRequest =
    copy(startLine = startLine.copy(method = newMethod))

  def withURI(newURI: String): HttpRequest =
    copy(startLine = startLine.copy(uri = newURI))

  def withVersion(newVersion: Version): HttpRequest =
    copy(startLine = startLine.copy(version = newVersion))

  def withPath(newPath: String): HttpRequest =
    withURI(buildURI(newPath, query))

  def withQuery(newQuery: String): HttpRequest =
    withURI(buildURI(path, Option(newQuery)))

  def withQueryParameters(params: Map[String, List[String]]): HttpRequest = {
    val query = QueryParser.format(params)

    if (query.isEmpty) withQuery(null)
    else withQuery(query)
  }

  private def buildURI(path: String, query: Option[String]): String = {
    val uri = new StringBuilder()

    val scheme = uriObject.getScheme
    if (scheme != null) uri.append(scheme).append("://")

    val authority = uriObject.getRawAuthority
    if (authority != null) uri.append(authority).append('/')

    uri.append(path)
    query.foreach(uri.append('?').append(_))

    val fragment = uriObject.getRawFragment
    if (fragment != null) uri.append('#').append(fragment)

    uri.toString
  }
}

/** A representation of an HTTP response. */
trait HttpResponse extends HttpMessage {
  type MessageType = HttpResponse
  type LineType = StatusLine

  /** Response status */
  def status: Status = startLine.status

  /** HTTP version of response message */
  def version: Version = startLine.version

  /**
   * Gets the location.
   *
   * The value is retrieved from the Location header.
   */
  def location: Option[String] =
    getHeaderValue("Location")

  /**
   * Creates a copy of this response replacing the response status.
   *
   * @return the new response
   */
  def withStatus(status: Status): MessageType

  /**
   * Creates a copy of this response replacing the HTTP version.
   *
   * @return the new response
   */
  def withVersion(version: Version): MessageType

  /**
   * Creates a copy of this message replacing the location.
   *
   * @return the new message
   */
  def withLocation(location: String): MessageType =
    withHeader(Header("Location", location))
}

/** Provides HttpResponse factory methods. */
object HttpResponse {
  /** Creates an HttpResponse using the supplied attributes. */
  def apply(statusLine: StatusLine, headers: Seq[Header], body: Entity): HttpResponse =
    SimpleHttpResponse(statusLine, headers, body)

  /** Creates an HttpResponse using the supplied attributes. */
  def apply(status: Status, headers: Seq[Header] = Nil, body: Entity = Entity.empty, version: Version = Version(1, 1)): HttpResponse =
    SimpleHttpResponse(StatusLine(version, status), headers, body)
}

private case class SimpleHttpResponse(startLine: StatusLine, headers: Seq[Header], body: Entity) extends HttpResponse {
  def addHeaders(moreHeaders: Header*): HttpResponse =
    copy(headers = headers ++ moreHeaders)

  def withHeaders(newHeaders: Header*): HttpResponse =
    copy(headers = newHeaders)

  def withBody(newBody: Entity): HttpResponse =
    copy(body = newBody)

  def withStartLine(line: StatusLine) =
    copy(startLine = line)

  def withStatus(newStatus: Status): HttpResponse =
    copy(startLine = startLine.copy(status = newStatus))

  def withVersion(newVersion: Version): HttpResponse =
    copy(startLine = startLine.copy(version = newVersion))
}

private object QueryParser {
  import bantam.nx.lang.StringType

  def parse(query: String): Map[String, List[String]] =
    query.split("&").map(_.split("=")).collect {
      case Array(name, value) if !name.isEmpty => name.toURLDecoded -> value.toURLDecoded
      case Array(name)        if !name.isEmpty => name.toURLDecoded -> ""
    }.groupBy(_._1).map {
      case (name, value) => name -> value.map(_._2).toList
    }

  def format(params: Map[String, List[String]]): String =
    params.toSeq.map {
      case (name, values) =>
        values.map(value => s"${name.toURLEncoded}=${value.toURLEncoded}").mkString("&")
    }.mkString("&")
}

