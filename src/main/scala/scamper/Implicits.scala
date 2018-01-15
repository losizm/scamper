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

  /** Converts a tuple to a [[Header]]. */
  implicit val tupleToHeader = (header: (String, String)) => Header(header._1, header._2)

  /** Converts a string to a [[Version]]. */
  implicit val stringToVersion = (version: String) => Version(version)

  /** Converts a string to a <code>java.net.URI</code>. */
  implicit val stringToURI = (uri: String) => new URI(uri)

  /** Converts a string to a <code>java.net.URL</code>. */
  implicit val stringToURL = (url: String) => new URL(url)

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
        headers.foreach(header => conn.addRequestProperty(header.key, header.value))

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
        headers.foreach(header => conn.addRequestProperty(header.key, header.value))
        conn.setRequestProperty("X-Scamper-Chunked-Managed", "true")

        conn.setDoOutput(true)
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
        headers.foreach(header => conn.addRequestProperty(header.key, header.value))
        conn.setRequestProperty("X-Scamper-Chunked-Managed", "true")

        conn.setDoOutput(true)
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
        headers.foreach(header => conn.addRequestProperty(header.key, header.value))
        conn.setRequestProperty("X-Scamper-Chunked-Managed", "true")

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
    private def getHeaders(conn: HttpURLConnection, keyIndex: Int, headers: Seq[Header]): Seq[Header] =
      conn.getHeaderFieldKey(keyIndex) match {
        case null => headers
        case key  => getHeaders(conn, keyIndex + 1, headers :+ Header(key, conn.getHeaderField(keyIndex)))
      }

    private def getHeaders(conn: HttpURLConnection): Seq[Header] = {
      val headers = getHeaders(conn, 1, Nil)

      if ("chunked".equalsIgnoreCase(conn.getHeaderField("Transfer-Encoding")))
        headers :+ Header("X-Scamper-Chunked-Managed: true")
      else headers
    }

    private def getBody(conn: HttpURLConnection): Option[Entity] =
      Some(
        Entity(() =>
          if (conn.getResponseCode >= 400) conn.getErrorStream
          else conn.getInputStream
        )
      )
  }
}

