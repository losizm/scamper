package scamper

/** A representation of an HTTP request. */
trait HttpRequest extends HttpMessage {
  type MessageType = HttpRequest
  type LineType = RequestLine

  /** Request method */
  def method: String = startLine.method

  /** Request URI */
  def uri: String = startLine.uri

  /** HTTP version */
  def version: Version = startLine.version

  /** Path component of URI */
  def path: String

  /** Query component of URI */
  def query: Option[String]

  /** Query parameters */
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
   * Creates a copy of this request replacing the path component of URI.
   *
   * @return the new request
   */
  def withPath(path: String): HttpRequest

  /**
   * Creates a copy of this request replacing the query component of URI.
   *
   * @return the new request
   */
  def withQuery(query: String): HttpRequest

  /**
   * Creates a copy of this request replacing the query parameters.
   *
   * @return the new request
   */
  def withQueryParameters(params: Map[String, List[String]]): HttpRequest

  /**
   * Creates a copy of this request replacing the query parameters.
   *
   * @return the new request
   */
  def withQueryParameters(params: (String, String)*): HttpRequest

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

  def withQueryParameters(params: (String, String)*): HttpRequest = {
    val query = QueryParser.format(params : _*)

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

