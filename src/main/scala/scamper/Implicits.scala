package scamper

import bantam.nx.io._
import java.io.File
import java.net.{ HttpURLConnection, URL }
import scala.annotation.tailrec
import scala.util.Try
import scamper._

/** Provides implicit conversion functions and type classes. */
object Implicits {
  /** Converts a string to a [[Header]]. */
  implicit val stringToHeader = (header: String) => Header(header)

  /**
   * Converts a tuple to a [[Header]] where the first element is the key and the
   * second is the value.
   */
  implicit val tupleToHeader = (header: (String, String)) => Header(header._1, header._2)

  /** Converts a string to a [[Version]]. */
  implicit val stringToVersion = (version: String) => Version(version)

  /** Converts an integer to a [[Status]]. */
  implicit val intToStatus = (status: Int) => Status(status)

  /** Converts a byte array to an [[Entity]]. */
  implicit val bytesToEntity = (entity: Array[Byte]) => Entity(entity)

  /**
   * Converts a string to an [[Entity]] where the text is to be UTF-8 encoded.
   */
  implicit val stringToEntity = (entity: String) => Entity(entity, "UTF-8")

  /**
   * Converts a tuple to an [[Entity]] where the first element is the text and
   * the second is the character encoding.
   */
  implicit val tupleToEntity = (entity: (String, String)) => Entity(entity._1, entity._2)

  /** Converts a file to an [[Entity]]. */
  implicit val fileToEntity = (entity: File) => Entity(entity)

  /**
   * A type class of <code>java.net.URL</code> that adds methods for sending
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
     * Sends the HTTP request and passes the response to the supplied handler.
     *
     * @param method request method
     * @param headers request headers
     * @param body request entity body
     * @param f response handler
     *
     * @return the value returned from supplied handler
     */
    def request[T](method: String, headers: Seq[Header] = Nil, body: Option[Entity] = None)(f: HttpResponse => T): Try[T] =
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
     * Sends a GET request and passes the response to the supplied handler.
     *
     * @param headers request headers
     * @param f response handler
     *
     * @return the value returned from supplied handler
     */
    def get[T](headers: Header*)(f: HttpResponse => T): Try[T] =
      request("GET", headers)(f)

    /**
     * Sends a POST request and passes the response to the supplied handler.
     *
     * @param body request entity body
     * @param headers request headers
     * @param f response handler
     *
     * @return the value returned from supplied handler
     */
    def post[T](body: Entity, headers: Header*)(f: HttpResponse => T): Try[T] =
      request("POST", headers, Option(body))(f)

    /**
     * Sends a PUT request and passes the response to the supplied handler.
     *
     * @param body request entity body
     * @param headers request headers
     * @param f response handler
     *
     * @return the value returned from supplied handler
     */
    def put[T](body: Entity, headers: Header*)(f: HttpResponse => T): Try[T] =
      request("PUT", headers, Option(body))(f)

    /**
     * Sends a DELETE request and passes the response to the supplied handler.
     *
     * @param headers request headers
     * @param f response handler
     *
     * @return the value returned from supplied handler
     */
    def delete[T](headers: Header*)(f: HttpResponse => T): Try[T] =
      request("DELETE", headers)(f)

    /**
     * Sends a HEAD request and passes the response to the supplied handler.
     *
     * @param headers request headers
     * @param f response handler
     *
     * @return the value returned from supplied handler
     */
    def head[T](headers: Header*)(f: HttpResponse => T): Try[T] =
      request("HEAD", headers)(f)

    /**
     * Sends a TRACE request and passes the response to the supplied handler.
     *
     * @param headers request headers
     * @param f response handler
     *
     * @return the value returned from supplied handler
     */
    def trace[T](headers: Header*)(f: HttpResponse => T): Try[T] =
      request("TRACE", headers)(f)

    /**
     * Sends an OPTIONS request and passes the response to the supplied handler.
     *
     * @param headers request headers
     * @param f response handler
     *
     * @return the value returned from supplied handler
     */
    def options[T](headers: Header*)(f: HttpResponse => T): Try[T] =
      request("OPTIONS", headers)(f)

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
        if (conn.getResponseCode >= 400) conn.getErrorStream
        else conn.getInputStream
      )
  }
}

