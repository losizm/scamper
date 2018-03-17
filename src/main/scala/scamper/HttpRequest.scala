package scamper

import java.net.URI
import scamper.extensions.URIExtension

/**
 * HTTP request
 *
 * @see [[HttpResponse]]
 */
trait HttpRequest extends HttpMessage {
  type MessageType = HttpRequest
  type LineType = RequestLine
  type CookieType = PlainCookie

  /** Request method */
  def method: RequestMethod = startLine.method

  /** Request URI */
  def uri: String = startLine.uri

  /** HTTP version */
  def version: Version = startLine.version

  /** Path component of URI */
  def path: String

  /** Query parameters */
  def queryParams: Map[String, Seq[String]]

  /**
   * Gets value for named query parameter.
   *
   * If there are multiple parameters with given name, then value of first
   * occurrence is retrieved.
   */
  def getQueryParamValue(name: String): Option[String]

  /** Gets all values for named query parameter. */
  def getQueryParamValues(name: String): Seq[String]

  /**
   * Creates new request replacing method.
   *
   * @return new request
   */
  def withMethod(method: RequestMethod): MessageType

  /**
   * Creates new request replacing URI.
   *
   * @return new request
   */
  def withURI(uri: String): MessageType

  /**
   * Creates new request replacing path component of URI.
   *
   * @return new request
   */
  def withPath(path: String): MessageType

  /**
   * Creates new request replacing query parameters.
   *
   * @return new request
   */
  def withQueryParams(params: Map[String, Seq[String]]): MessageType

  /**
   * Creates new request replacing query parameters.
   *
   * @return new request
   */
  def withQueryParams(params: (String, String)*): MessageType

  /**
   * Creates new request replacing version.
   *
   * @return new request
   */
  def withVersion(version: Version): MessageType
}

/** HttpRequest factory */
object HttpRequest {
  /** Creates HttpRequest using supplied attributes. */
  def apply(requestLine: RequestLine, headers: Seq[Header], body: Entity): HttpRequest =
    HttpRequestImpl(requestLine, headers, body)

  /** Creates HttpRequest using supplied attributes. */
  def apply(method: RequestMethod, uri: String = "/", headers: Seq[Header] = Nil, body: Entity = Entity.empty, version: Version = Version(1, 1)): HttpRequest =
    HttpRequestImpl(RequestLine(method, uri, version), headers, body)
}

private case class HttpRequestImpl(startLine: RequestLine, headers: Seq[Header], body: Entity) extends HttpRequest {
  private lazy val uriObject = new URI(uri)

  lazy val path: String = uriObject.getRawPath

  lazy val queryParams: Map[String, Seq[String]] =
    uriObject.getRawQuery match {
      case null  => Map.empty
      case query => QueryParams.parse(query)
    }

  lazy val cookies: Seq[PlainCookie] =
    getHeaderValue("Cookie")
      .map(ListParser(_, true))
      .map(_.map(PlainCookie(_)).toSeq)
      .getOrElse(Nil)

  def getQueryParamValue(name: String): Option[String] =
    queryParams.get(name).flatMap(_.headOption)

  def getQueryParamValues(name: String): Seq[String] =
    queryParams.get(name).getOrElse(Nil)

  def withHeaders(newHeaders: Header*): HttpRequest =
    copy(headers = newHeaders)

  def withCookies(newCookies: PlainCookie*): HttpRequest =
    copy(headers = headers.filterNot(_.key.equalsIgnoreCase("Cookie")) :+ Header("Cookie", newCookies.mkString("; ")))

  def withBody(newBody: Entity): HttpRequest =
    copy(body = newBody)

  def withStartLine(newStartLine: RequestLine): HttpRequest =
    copy(startLine = newStartLine)

  def withMethod(newMethod: RequestMethod): HttpRequest =
    copy(startLine = RequestLine(newMethod, uri, version))

  def withURI(newURI: String): HttpRequest =
    copy(startLine = RequestLine(method, newURI, version))

  def withVersion(newVersion: Version): HttpRequest =
    copy(startLine = RequestLine(method, uri, newVersion))

  def withPath(newPath: String): HttpRequest =
    withURI(uriObject.withPath(newPath).toString)

  def withQueryParams(params: Map[String, Seq[String]]): HttpRequest =
    withURI(uriObject.withQueryParams(params).toString)

  def withQueryParams(params: (String, String)*): HttpRequest =
    withURI(uriObject.withQueryParams(params : _*).toString)
}

