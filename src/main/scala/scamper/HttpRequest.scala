package scamper

/** Representation of HTTP request. */
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
   * Gets requested host.
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
  /** Creates an HttpRequest using supplied attributes. */
  def apply(requestLine: RequestLine, headers: Seq[Header], body: Entity): HttpRequest =
    SimpleHttpRequest(requestLine, headers, body)

  /** Creates an HttpRequest using supplied attributes. */
  def apply(method: String, uri: String, headers: Seq[Header] = Nil, body: Entity = Entity.empty, version: Version = Version(1, 1)): HttpRequest =
    SimpleHttpRequest(RequestLine(method, uri, version), headers, body)
}

private case class SimpleHttpRequest(startLine: RequestLine, headers: Seq[Header], body: Entity) extends HttpRequest {
  import Implicits.URIType

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
    withURI(uriObject.withPath(newPath).toString)

  def withQuery(newQuery: String): HttpRequest =
    withURI(uriObject.withQuery(newQuery).toString)

  def withQueryParameters(params: Map[String, List[String]]): HttpRequest =
    withURI(uriObject.withQuery(params).toString)

  def withQueryParameters(params: (String, String)*): HttpRequest =
    withURI(uriObject.withQuery(params : _*).toString)
}

