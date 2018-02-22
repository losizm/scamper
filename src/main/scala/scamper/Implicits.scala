package scamper

import bantam.nx.io._
import bantam.nx.lang._
import java.io.File
import java.net.{ HttpURLConnection, URI, URL }
import scala.annotation.tailrec
import scala.util.Try

/** Provides implicit conversion functions and type classes. */
object Implicits {
  /** Converts string to [[Header]]. */
  implicit val stringToHeader = (header: String) => Header(header)

  /** Converts tuple to [[Header]] where tuple is key-value pair. */
  implicit val tupleToHeader = (header: (String, String)) => Header(header._1, header._2)

  /** Converts string to [[MediaType]]. */
  implicit val stringToMediaType = (mediaType: String) => MediaType(mediaType)

  /** Converts string to [[Version]]. */
  implicit val stringToVersion = (version: String) => Version(version)

  /** Converts integer to [[Status]]. */
  implicit val intToStatus = (status: Int) => Status(status)

  /** Converts byte array to [[Entity]]. */
  implicit val bytesToEntity = (entity: Array[Byte]) => Entity(entity)

  /** Converts string to [[Entity]] where text is UTF-8 encoded. */
  implicit val stringToEntity = (entity: String) => Entity(entity, "UTF-8")

  /**
   * Converts tuple to [[Entity]] where tuple is text and character encoding.
   */
  implicit val tupleToEntity = (entity: (String, String)) => Entity(entity._1, entity._2)

  /** Converts file to [[Entity]]. */
  implicit val fileToEntity = (entity: File) => Entity(entity)

  /**
   * Type class of [[HttpRequest]] that adds method for sending request and
   * receiving [[HttpResponse]].
   */
  implicit class HttpRequestType(request: HttpRequest) {
    /**
     * Sends request and passes response to supplied handler.
     *
     * @param secure specifies whether to use HTTPS protocol
     * @param f response handler
     *
     * @return value returned from supplied handler
     */
    def send[T](secure: Boolean = false)(f: HttpResponse => T): T = {
      val scheme = if (secure) "https" else "http"
      val uri = request.uri.toURI

      if (uri.isAbsolute) {
        val host = uri.getAuthority
        val url = uri.toURL(scheme, host)
        val headers = Header("Host", host) +: request.headers.filterNot(_.key.equalsIgnoreCase("Host"))

        url.request(request.method, headers, Some(request.body))(f)
      } else {
        val host = getHost(uri)
        val url = uri.toURL(scheme, host)
        val headers = Header("Host", host) +: request.headers.filterNot(_.key.equalsIgnoreCase("Host"))

        url.request(request.method, headers, Some(request.body))(f)
      }
    }

    private def getHost(uri: URI): String = {
      val authority = uri.getAuthority

      if (authority != null) authority
      else request.host.getOrElse(throw HeaderNotFound("Host"))
    }
  }

  /**
   * Type class of <code>java.net.URI</code> that adds methods for building new
   * URI.
   */
  implicit class URIType(uri: URI) {
    /** Gets query parameters. */
    def getQueryParams(): Map[String, Seq[String]] =
      QueryParser.parse(uri.getQuery)

    /**
     * Gets value for named query parameter.
     *
     * If there are multiple parameters with given name, then value of first
     * occurrence is retrieved.
     */
    def getQueryParamValue(name: String): Option[String] =
      getQueryParams.get(name).flatMap(_.headOption)

    /** Gets all values for named query parameter. */
    def getQueryParamValues(name: String): Seq[String] =
      getQueryParams.get(name).getOrElse(Nil)

    /** Converts URI to URL using supplied scheme and authority. */
    def toURL(scheme: String, authority: String): URL =
      buildURI(scheme, authority, uri.getPath, uri.getQuery, uri.getFragment).toURL

    /** Creates new URI replacing path. */
    def withPath(path: String): URI =
      createURI(path, uri.getQuery)

    /** Creates new URI replacing query. */
    def withQuery(query: String): URI =
      createURI(uri.getPath, query)

    /** Creates new URI replacing query parameters. */
    def withQueryParams(params: Map[String, Seq[String]]): URI =
      withQuery(QueryParser.format(params))

    /** Creates new URI replacing query parameters. */
    def withQueryParams(params: (String, String)*): URI =
      withQuery(QueryParser.format(params : _*))

    private def createURI(path: String, query: String): URI =
      buildURI(uri.getScheme, uri.getRawAuthority, path, query, uri.getRawFragment).toURI
  }

  /**
   * Type class of <code>java.net.URL</code> that adds methods for building new
   * URL and sending HTTP request.
   */
  implicit class URLType(url: URL) {
    /** Gets the query parameters. */
    def getQueryParams(): Map[String, Seq[String]] =
      QueryParser.parse(url.getQuery)

    /**
     * Gets value for named query parameter.
     *
     * If there are multiple parameters with given name, then value of first
     * occurrence is retrieved.
     */
    def getQueryParamValue(name: String): Option[String] =
      getQueryParams.get(name).flatMap(_.headOption)

    /** Gets all values for named query parameter. */
    def getQueryParamValues(name: String): Seq[String] =
      getQueryParams.get(name).getOrElse(Nil)

    /** Creates new URL replacing path. */
    def withPath(path: String): URL =
      createURL(path, url.getQuery)

    /** Creates new URL replacing query. */
    def withQuery(query: String): URL =
      createURL(url.getPath, query)

    /** Creates new URL replacing query parameters. */
    def withQueryParams(params: Map[String, Seq[String]]): URL =
      createURL(url.getPath, QueryParser.format(params))

    /** Creates new URL replacing query parameters. */
    def withQueryParams(params: (String, String)*): URL =
      createURL(url.getPath, QueryParser.format(params : _*))

    /**
     * Opens HTTP connection and passes it to supplied handler.
     *
     * The connection is disconnected upon handler's return.
     *
     * @param f connection handler
     *
     * @return value returned from supplied handler
     */
    def withConnection[T](f: HttpURLConnection => T): T = {
      val conn = url.openConnection()

      try f(conn.asInstanceOf[HttpURLConnection])
      finally Try(conn.asInstanceOf[HttpURLConnection].disconnect())
    }

    /**
     * Sends HTTP request and passes response to supplied handler.
     *
     * @param method request method
     * @param headers request headers
     * @param body request entity body
     * @param f response handler
     *
     * @return value returned from supplied handler
     */
    def request[T](method: String, headers: Seq[Header] = Nil, body: Option[Entity] = None)(f: HttpResponse => T): T =
      withConnection { conn =>
        conn.setRequestMethod(method)
        headers.foreach(header => conn.addRequestProperty(header.key, header.value))

        body.filterNot(_.isKnownEmpty).foreach { entity =>
          conn.setDoOutput(true)
          writeBody(conn, entity)
        }

        val statusLine = StatusLine(conn.getHeaderField(0))
        val response = HttpResponse(statusLine, getHeaders(conn), getBody(conn))

        f(response)
      }

    /**
     * Sends GET request and passes response to supplied handler.
     *
     * @param headers request headers
     * @param f response handler
     *
     * @return value returned from supplied handler
     */
    def get[T](headers: Header*)(f: HttpResponse => T): T =
      request("GET", headers)(f)

    /**
     * Sends POST request and passes response to supplied handler.
     *
     * @param body request entity body
     * @param headers request headers
     * @param f response handler
     *
     * @return value returned from supplied handler
     */
    def post[T](body: Entity, headers: Header*)(f: HttpResponse => T): T =
      request("POST", headers, Option(body))(f)

    /**
     * Sends PUT request and passes response to supplied handler.
     *
     * @param body request entity body
     * @param headers request headers
     * @param f response handler
     *
     * @return value returned from supplied handler
     */
    def put[T](body: Entity, headers: Header*)(f: HttpResponse => T): T =
      request("PUT", headers, Option(body))(f)

    /**
     * Sends DELETE request and passes response to supplied handler.
     *
     * @param headers request headers
     * @param f response handler
     *
     * @return value returned from supplied handler
     */
    def delete[T](headers: Header*)(f: HttpResponse => T): T =
      request("DELETE", headers)(f)

    /**
     * Sends HEAD request and passes response to supplied handler.
     *
     * @param headers request headers
     * @param f response handler
     *
     * @return value returned from supplied handler
     */
    def head[T](headers: Header*)(f: HttpResponse => T): T =
      request("HEAD", headers)(f)

    /**
     * Sends TRACE request and passes response to supplied handler.
     *
     * @param headers request headers
     * @param f response handler
     *
     * @return value returned from supplied handler
     */
    def trace[T](headers: Header*)(f: HttpResponse => T): T =
      request("TRACE", headers)(f)

    /**
     * Sends OPTIONS request and passes response to supplied handler.
     *
     * @param headers request headers
     * @param f response handler
     *
     * @return value returned from supplied handler
     */
    def options[T](headers: Header*)(f: HttpResponse => T): T =
      request("OPTIONS", headers)(f)

    private def createURL(path: String, query: String): URL =
      buildURI(url.getProtocol, url.getAuthority, path, query, url.getRef).toURL

    private def writeBody(conn: HttpURLConnection, body: Entity): Unit = {
      body.length match {
        case Some(length) =>
          conn.setRequestProperty("Content-Length", length.toString)
          conn.setFixedLengthStreamingMode(length)

        case None =>
          conn.setRequestProperty("Transfer-Encoding", "chunked")
          conn.setChunkedStreamingMode(8192)
      }

      body.withInputStream(conn.getOutputStream << _)
    }

    private def getHeaders(conn: HttpURLConnection): Seq[Header] = {
      val headers = getHeaders(conn, 1, Nil)

      if ("chunked".equalsIgnoreCase(conn.getHeaderField("Transfer-Encoding")))
        headers :+ Header("X-Scamper-Decoding: chunked")
      else headers
    }

    @tailrec
    private def getHeaders(conn: HttpURLConnection, keyIndex: Int, headers: Seq[Header]): Seq[Header] =
      conn.getHeaderFieldKey(keyIndex) match {
        case null => headers
        case key  => getHeaders(conn, keyIndex + 1, headers :+ Header(key, conn.getHeaderField(keyIndex)))
      }

    private def getBody(conn: HttpURLConnection): Entity =
      Entity(() =>
        if (conn.getResponseCode < 400) conn.getInputStream
        else conn.getErrorStream
      )
  }

  private def buildURI(scheme: String, authority: String, path: String, query: String, fragment: String): String = {
    val uriBuilder = new StringBuilder()

    if (scheme != null) uriBuilder.append(scheme).append(":")
    if (authority != null) uriBuilder.append("//").append(authority)

    uriBuilder.append('/').append(path.dropWhile(_ == '/'))

    if (query != null && !query.isEmpty) uriBuilder.append('?').append(query)
    if (fragment != null) uriBuilder.append('#').append(fragment)

    uriBuilder.toString
  }
}

