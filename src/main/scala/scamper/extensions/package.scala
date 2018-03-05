package scamper

import java.net.{ HttpURLConnection, URI, URL }
import scala.annotation.tailrec
import scala.util.Try

/** Contains type classes of HttpRequest, URI, and URL. */
package object extensions {
  /** Type class of [[HttpRequest]]. */
  implicit class HttpRequestExtension(request: HttpRequest) {
    /**
     * Sends request and passes response to supplied handler.
     *
     * To make effective use of this method, either the Host header must be set,
     * or the request URI must be absolute. Also note that if the request URI is
     * absolute, its scheme is overridden in accordance to {@code secure}.
     *
     * @param secure specifies whether to use HTTPS protocol
     * @param f response handler
     *
     * @return value returned from supplied handler
     */
    def send[T](secure: Boolean = false)(f: HttpResponse => T): T = {
      val scheme = if (secure) "https" else "http"
      val uri = new URI(request.uri)
      val host = getHost(uri)
      val headers = Header("Host", host) +: request.headers.filterNot(_.key.equalsIgnoreCase("Host"))

      uri.toURL(scheme, host).request(request.method, headers, Some(request.body))(f)
    }

    private def getHost(uri: URI): String =
      Option(uri.getAuthority).orElse(request.getHeaderValue("Host")).getOrElse(throw HeaderNotFound("Host"))
  }

  /**
   * Type class of {@code java.net.URI}.
   */
  implicit class URIExtension(uri: URI) {
    /** Gets query parameters. */
    def getQueryParams(): Map[String, Seq[String]] =
      QueryParams.parse(uri.getQuery)

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
      new URL(buildURI(scheme, authority, uri.getPath, uri.getQuery, uri.getFragment))

    /** Creates new URI replacing path. */
    def withPath(path: String): URI =
      createURI(path, uri.getQuery)

    /** Creates new URI replacing query. */
    def withQuery(query: String): URI =
      createURI(uri.getPath, query)

    /** Creates new URI replacing query parameters. */
    def withQueryParams(params: Map[String, Seq[String]]): URI =
      withQuery(QueryParams.format(params))

    /** Creates new URI replacing query parameters. */
    def withQueryParams(params: (String, String)*): URI =
      withQuery(QueryParams.format(params : _*))

    private def createURI(path: String, query: String): URI =
      new URI(buildURI(uri.getScheme, uri.getRawAuthority, path, query, uri.getRawFragment))
  }

  /** Type class of {@code java.net.URL}. */
  implicit class URLExtension(url: URL) {
    /** Gets the query parameters. */
    def getQueryParams(): Map[String, Seq[String]] =
      QueryParams.parse(url.getQuery)

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
      createURL(url.getPath, QueryParams.format(params))

    /** Creates new URL replacing query parameters. */
    def withQueryParams(params: (String, String)*): URL =
      createURL(url.getPath, QueryParams.format(params : _*))

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
      new URL(buildURI(url.getProtocol, url.getAuthority, path, query, url.getRef))

    private def writeBody(conn: HttpURLConnection, body: Entity): Unit = {
      body.length match {
        case Some(length) =>
          conn.setRequestProperty("Content-Length", length.toString)
          conn.setFixedLengthStreamingMode(length)

        case None =>
          conn.setRequestProperty("Transfer-Encoding", "chunked")
          conn.setChunkedStreamingMode(8192)
      }

      body.withInputStream { in =>
        val out = conn.getOutputStream
        val buf = new Array[Byte](8192)
        var len = 0

        while ({ len = in.read(buf); len != -1 })
          out.write(buf, 0, len)
      }
    }

    private def getHeaders(conn: HttpURLConnection): Seq[Header] = {
      val headers = getHeaders(conn, 1, Nil)

      if ("chunked".equalsIgnoreCase(conn.getHeaderField("Transfer-Encoding")))
        headers :+ Header("X-Scamper-Transfer-Decoding: chunked")
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
