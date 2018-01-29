package scamper

import bantam.nx.io._
import java.io.File
import java.net.{ HttpURLConnection, URI, URL }
import scala.annotation.tailrec
import scala.util.Try
import scamper._

/** Provides implicit conversion functions and type classes. */
object Implicits {
  /** Converts a string to [[Header]]. */
  implicit val stringToHeader = (header: String) => Header(header)

  /**
   * Converts a tuple to [[Header]] where the first element is the header key
   * and the second is the value.
   */
  implicit val tupleToHeader = (header: (String, String)) => Header(header._1, header._2)

  /** Converts a string to [[ContentType]]. */
  implicit val stringToContentType = (contentType: String) => ContentType(contentType)

  /** Converts a string to [[Version]]. */
  implicit val stringToVersion = (version: String) => Version(version)

  /** Converts an integer to [[Status]]. */
  implicit val intToStatus = (status: Int) => Status(status)

  /** Converts a byte array to [[Entity]]. */
  implicit val bytesToEntity = (entity: Array[Byte]) => Entity(entity)

  /**
   * Converts a string to [[Entity]] where the text is to be UTF-8 encoded.
   */
  implicit val stringToEntity = (entity: String) => Entity(entity, "UTF-8")

  /**
   * Converts a tuple to [[Entity]] where the first element is text and the
   * second is the character encoding.
   */
  implicit val tupleToEntity = (entity: (String, String)) => Entity(entity._1, entity._2)

  /** Converts a file to [[Entity]]. */
  implicit val fileToEntity = (entity: File) => Entity(entity)

  /**
   * A type class of <code>java.net.URI</code> that adds methods for building
   * new URIs.
   */
  implicit class URIType(uri: URI) {
    /** Creates a new URI replacing the path. */
    def withPath(path: String): URI =
      buildURI(path, Option(uri.getQuery))

    /** Creates a new URI replacing the query. */
    def withQuery(query: String): URI =
      buildURI(uri.getPath, Option(query))

    /** Creates a new URI replacing the query with supplied parameters. */
    def withQuery(params: Map[String, List[String]]): URI =
      withQuery(QueryParser.format(params))

    /** Creates a new URI replacing the query with supplied parameters. */
    def withQuery(params: (String, String)*): URI =
      withQuery(QueryParser.format(params : _*))

    private def buildURI(path: String, query: Option[String]): URI = {
      val uriBuilder = new StringBuilder()

      val scheme = uri.getScheme
      if (scheme != null) uriBuilder.append(scheme).append("://")

      val authority = uri.getRawAuthority
      if (authority != null)
        uriBuilder.append(authority).append('/').append(path.dropWhile(_ == '/'))
      else uriBuilder.append(path)

      query.filterNot(_.matches("\\s+")).foreach(uriBuilder.append('?').append(_))

      val fragment = uri.getRawFragment
      if (fragment != null) uriBuilder.append('#').append(fragment)

      new URI(uriBuilder.toString)
    }
  }

  /**
   * A type class of <code>java.net.URL</code> that adds methods for building
   * new URLs and sending HTTP requests.
   */
  implicit class URLType(url: URL) {
    /** Creates a new URL replacing the path. */
    def withPath(path: String): URL =
      buildURL(path, Option(url.getQuery))

    /** Creates a new URL replacing the query. */
    def withQuery(query: String): URL =
      buildURL(url.getPath, Option(query))

    /** Creates a new URL replacing the query with supplied parameters. */
    def withQuery(params: Map[String, List[String]]): URL =
      buildURL(url.getPath, Option(QueryParser.format(params)))

    /** Creates a new URL replacing the query with supplied parameters. */
    def withQuery(params: (String, String)*): URL =
      buildURL(url.getPath, Option(QueryParser.format(params : _*)))

    /**
     * Opens HTTP connection and passes it to supplied handler.
     *
     * The connection is disconnected upon handler's return.
     *
     * @param f connection handler
     *
     * @return the value returned from supplied handler
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
     * @return the value returned from supplied handler
     */
    def request[T](method: String, headers: Seq[Header] = Nil, body: Option[Entity] = None)(f: HttpResponse => T): T =
      withConnection { conn =>
        conn.setRequestMethod(method)
        headers.foreach(header => conn.addRequestProperty(header.key, header.value))

        body.foreach { entity =>
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
     * @return the value returned from supplied handler
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
     * @return the value returned from supplied handler
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
     * @return the value returned from supplied handler
     */
    def put[T](body: Entity, headers: Header*)(f: HttpResponse => T): T =
      request("PUT", headers, Option(body))(f)

    /**
     * Sends DELETE request and passes response to supplied handler.
     *
     * @param headers request headers
     * @param f response handler
     *
     * @return the value returned from supplied handler
     */
    def delete[T](headers: Header*)(f: HttpResponse => T): T =
      request("DELETE", headers)(f)

    /**
     * Sends HEAD request and passes response to supplied handler.
     *
     * @param headers request headers
     * @param f response handler
     *
     * @return the value returned from supplied handler
     */
    def head[T](headers: Header*)(f: HttpResponse => T): T =
      request("HEAD", headers)(f)

    /**
     * Sends TRACE request and passes response to supplied handler.
     *
     * @param headers request headers
     * @param f response handler
     *
     * @return the value returned from supplied handler
     */
    def trace[T](headers: Header*)(f: HttpResponse => T): T =
      request("TRACE", headers)(f)

    /**
     * Sends OPTIONS request and passes response to supplied handler.
     *
     * @param headers request headers
     * @param f response handler
     *
     * @return the value returned from supplied handler
     */
    def options[T](headers: Header*)(f: HttpResponse => T): T =
      request("OPTIONS", headers)(f)

    private def buildURL(path: String, query: Option[String]): URL = {
      val urlBuilder = new StringBuilder()

      urlBuilder.append(url.getProtocol).append("://")
      urlBuilder.append(url.getAuthority).append('/')
      urlBuilder.append(path.dropWhile(_ == '/'))
      query.foreach(urlBuilder.append('?').append(_))

      val ref = url.getRef
      if (ref != null) urlBuilder.append('#').append(ref)

      new URL(urlBuilder.toString)
    }

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
}

