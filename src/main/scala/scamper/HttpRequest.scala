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
  lazy val queryParameters: Map[String, Seq[String]] =
    query.map(QueryParser.parse).getOrElse(Map.empty)

  /**
   * Gets value for named query parameter.
   *
   * If there are multiple parameters with given name, then value of first
   * occurrence is retrieved.
   */
  def getQueryParameterValue(name: String): Option[String] =
    queryParameters.get(name).flatMap(_.headOption)

  /** Gets all values for named query parameter. */
  def getQueryParameterValues(name: String): Seq[String] =
    queryParameters.get(name).getOrElse(Nil)

  /**
   * Gets requested host.
   *
   * Value retrieved from Host header.
   */
  def host: Option[String] =
    getHeaderValue("Host")

  /**
   * Get accepted content types.
   *
   * Value retrieved from Accept header.
   */
  def accept: Seq[ContentType] =
    getHeaderValue("Accept").map { value =>
      value.split("\\s*,\\s*").map(ContentType.apply).toSeq
    }.getOrElse(Nil)

  /**
   * Get accepted encodings.
   *
   * Value retrieved from Accept-Encoding header.
   */
  def acceptEncoding: Seq[String] =
    getHeaderValue("Accept-Encoding").map { value =>
      value.split("\\s*,\\s*").toSeq
    }.getOrElse(Nil)

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
  def withQueryParameters(params: Map[String, Seq[String]]): HttpRequest

  /**
   * Creates new request replacing query parameters.
   *
   * @return new request
   */
  def withQueryParameters(params: (String, String)*): HttpRequest

  /**
   * Creates new message replacing host.
   *
   * @return new message
   */
  def withHost(host: String): MessageType =
    withHeader(Header("Host", host))

  /**
   * Creates new message replacing accepted content types.
   *
   * @return new message
   */
  def withAccept(types: ContentType*): MessageType =
    withHeader(Header("Accept", types.mkString(", ")))

  /**
   * Creates new message replacing accepted encodings.
   *
   * @return new message
   */
  def withAcceptEncoding(encodings: String*): MessageType =
    withHeader(Header("Accept-Encoding", encodings.mkString(", ")))
}

/** Provides HttpRequest factory methods. */
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

  def withQueryParameters(params: Map[String, Seq[String]]): HttpRequest =
    withURI(uriObject.withQueryParameters(params).toString)

  def withQueryParameters(params: (String, String)*): HttpRequest =
    withURI(uriObject.withQueryParameters(params : _*).toString)
}

