package scamper

/** HTTP request */
trait HttpRequest extends HttpMessage {
  type MessageType = HttpRequest
  type LineType = RequestLine
  type CookieType = PlainCookie

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
  lazy val queryParams: Map[String, Seq[String]] =
    query.map(QueryParser.parse).getOrElse(Map.empty)

  /**
   * Gets value for named query parameter.
   *
   * If there are multiple parameters with given name, then value of first
   * occurrence is retrieved.
   */
  def getQueryParamValue(name: String): Option[String] =
    queryParams.get(name).flatMap(_.headOption)

  /** Gets all values for named query parameter. */
  def getQueryParamValues(name: String): Seq[String] =
    queryParams.get(name).getOrElse(Nil)

  /**
   * Gets all request cookies.
   *
   * Values retrieved from Cookie header.
   */
  lazy val cookies: Seq[PlainCookie] =
    getHeaderValue("Cookie")
      .map(_.split("\\s*;\\s*"))
      .map(_.map(PlainCookie.apply).toSeq)
      .getOrElse(Nil)

  /**
   * Gets requested host.
   *
   * Value retrieved from Host header.
   */
  lazy val host: Option[String] =
    getHeaderValue("Host")

  /**
   * Get accepted media types.
   *
   * Value retrieved from Accept header.
   */
  lazy val accept: Seq[MediaType] =
    getHeaderValue("Accept")
      .map(_.split("\\s*,\\s*")
      .map(MediaType.apply).toSeq)
      .getOrElse(Nil)

  /**
   * Get accepted encodings.
   *
   * Value retrieved from Accept-Encoding header.
   */
  lazy val acceptEncoding: Seq[String] =
    getHeaderValue("Accept-Encoding")
      .map(_.split("\\s*,\\s*").toSeq)
      .getOrElse(Nil)

  /**
   * Creates new request replacing method.
   *
   * @return new request
   */
  def withMethod(method: String): MessageType

  /**
   * Creates new request replacing URI.
   *
   * @return new request
   */
  def withURI(uri: String): MessageType

  /**
   * Creates new request replacing version.
   *
   * @return new request
   */
  def withVersion(version: Version): MessageType

  /**
   * Creates new request replacing path component of URI.
   *
   * @return new request
   */
  def withPath(path: String): HttpRequest

  /**
   * Creates new request replacing query component of URI.
   *
   * @return new request
   */
  def withQuery(query: String): HttpRequest

  /**
   * Creates new request replacing query parameters.
   *
   * @return new request
   */
  def withQueryParams(params: Map[String, Seq[String]]): HttpRequest

  /**
   * Creates new request replacing query parameters.
   *
   * @return new request
   */
  def withQueryParams(params: (String, String)*): HttpRequest

  /**
   * Creates new request replacing host.
   *
   * @return new request
   */
  def withHost(host: String): MessageType =
    withHeader(Header("Host", host))

  /**
   * Creates new request replacing accepted media types.
   *
   * @return new request
   */
  def withAccept(types: MediaType*): MessageType =
    withHeader(Header("Accept", types.mkString(", ")))

  /**
   * Creates new request replacing accepted encodings.
   *
   * @return new request
   */
  def withAcceptEncoding(encodings: String*): MessageType =
    withHeader(Header("Accept-Encoding", encodings.mkString(", ")))
}

/** HttpRequest factory */
object HttpRequest {
  /** Creates HttpRequest using supplied attributes. */
  def apply(requestLine: RequestLine, headers: Seq[Header], body: Entity): HttpRequest =
    SimpleHttpRequest(requestLine, headers, body)

  /** Creates HttpRequest using supplied attributes. */
  def apply(method: String, uri: String, headers: Seq[Header] = Nil, body: Entity = Entity.empty, version: Version = Version(1, 1)): HttpRequest =
    SimpleHttpRequest(RequestLine(method, uri, version), headers, body)
}

private case class SimpleHttpRequest(startLine: RequestLine, headers: Seq[Header], body: Entity) extends HttpRequest {
  import Implicits.URIType

  private lazy val uriObject = new java.net.URI(uri)

  lazy val path = uriObject.getPath
  lazy val query = Option(uriObject.getRawQuery)

  def addHeaders(newHeaders: Header*): HttpRequest =
    copy(headers = headers ++ newHeaders)

  def withHeaders(newHeaders: Header*): HttpRequest =
    copy(headers = newHeaders)

  def withCookies(newCookies: PlainCookie*): HttpRequest =
    copy(headers = headers.filterNot(_.key.equalsIgnoreCase("Cookie")) :+ Header("Cookie", newCookies.mkString("; ")))

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

  def withQueryParams(params: Map[String, Seq[String]]): HttpRequest =
    withURI(uriObject.withQueryParams(params).toString)

  def withQueryParams(params: (String, String)*): HttpRequest =
    withURI(uriObject.withQueryParams(params : _*).toString)
}

