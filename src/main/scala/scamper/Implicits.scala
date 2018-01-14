package scamper

import bantam.nx.io._
import java.net.{ HttpURLConnection, URI, URL }
import scala.annotation.tailrec
import scala.util.Try
import scamper._

/** A collection of implicits. */
object Implicits {
  /** Converts a string to a [[Header]]. */
  implicit val stringToHeader = (header: String) => Header(header)

  /** Converts a string to a [[Version]]. */
  implicit val stringToVersion = (version: String) => Version(version)

  /** Converts a string to a <code>java.net.URI</code>. */
  implicit val stringToURI = (uri: String) => new URI(uri)

  /** Converts a string to a <code>java.net.URL</code>. */
  implicit val stringToURL = (url: String) => new URL(url)

  /** Converts a tuple to a [[Header]]. */
  implicit val tupleToHeader = (header: (String, String)) => Header(header._1, header._2)

  /**
   * A type class of <code>java.net.URL</code> which adds methods for sending
   * HTTP requests and handling the response.
   */
  implicit class URLType(url: URL) {
    /**
     * Opens an HTTP connection and passes it to the supplied handler.
     *
     * The connection is disconnected upon handler's return.
     *
     * @param f connection handler
     *
     * @return the value returned from supplied handler
     */
    def withConnection[T](f: HttpURLConnection => T): Try[T] =
      Try {
        val conn = url.openConnection()

        try f(conn.asInstanceOf[HttpURLConnection])
        finally Try(conn.asInstanceOf[HttpURLConnection].disconnect())
      }

    /**
     * Sends a GET request and passes the response to the supplied handler.
     *
     * @param headers request headers
     * @param f response handler
     *
     * @return the value returned from supplied handler
     */
    def get[T](headers: Header*)(f: HttpResponse => T): Try[T] =
      withConnection { conn =>
        conn.setRequestMethod("GET")
        headers.foreach(header => conn.setRequestProperty(header.key, header.value))

        val statusLine = StatusLine(conn.getHeaderField(0))
        val response = HttpResponse(statusLine, getHeaders(conn), getBody(conn))

        f(response)
      }

    /**
     * Sends a POST request and passes the response to the supplied handler.
     *
     * @param headers request headers
     * @param body request entity body
     * @param f response handler
     *
     * @return the value returned from supplied handler
     */
    def post[T](body: Entity, headers: Header*)(f: HttpResponse => T): Try[T] =
      withConnection { conn =>
        conn.setRequestMethod("POST")
        conn.setDoOutput(true)
        headers.foreach(header => conn.setRequestProperty(header.key, header.value))

        writeBody(conn, body)

        val statusLine = StatusLine(conn.getHeaderField(0))
        val response = HttpResponse(statusLine, getHeaders(conn), getBody(conn))

        f(response)
      }

    /**
     * Sends a PUT request and passes the response to the supplied handler.
     *
     * @param headers request headers
     * @param body request entity body
     * @param f response handler
     *
     * @return the value returned from supplied handler
     */
    def put[T](body: Entity, headers: Header*)(f: HttpResponse => T): Try[T] =
      withConnection { conn =>
        conn.setRequestMethod("PUT")
        conn.setDoOutput(true)
        headers.foreach(header => conn.setRequestProperty(header.key, header.value))

        writeBody(conn, body)

        val statusLine = StatusLine(conn.getHeaderField(0))
        val response = HttpResponse(statusLine, getHeaders(conn), getBody(conn))

        f(response)
      }

    /**
     * Sends a DELETE request and passes the response to the supplied handler.
     *
     * @param headers request headers
     * @param f response handler
     *
     * @return the value returned from supplied handler
     */
    def delete[T](headers: Header*)(f: HttpResponse => T): Try[T] =
      withConnection { conn =>
        conn.setRequestMethod("DELETE")
        headers.foreach(header => conn.setRequestProperty(header.key, header.value))

        val statusLine = StatusLine(conn.getHeaderField(0))
        val response = HttpResponse(statusLine, getHeaders(conn), getBody(conn))

        f(response)
      }

    private def writeBody(conn: HttpURLConnection, body: Entity): Unit = {
      body.size match {
        case Some(length) =>
          conn.setRequestProperty("Content-Length", length.toString)
          conn.setFixedLengthStreamingMode(length)

        case None =>
          conn.setRequestProperty("Transfer-Encoding", "chunked")
          conn.setChunkedStreamingMode(8192)
      }

      body.withInputStream(conn.getOutputStream << _)
    }

    @tailrec
    private def getHeaders(conn: HttpURLConnection, keyIndex: Int = 1, headers: Seq[Header] = Nil): Seq[Header] =
      conn.getHeaderFieldKey(keyIndex) match {
        case null => headers
        case key  => getHeaders(conn, keyIndex + 1, headers :+ Header(key, conn.getHeaderField(keyIndex)))
      }

    private def getBody(conn: HttpURLConnection): Option[Entity] =
      Some(Entity(() => conn.getInputStream))
  }
}

