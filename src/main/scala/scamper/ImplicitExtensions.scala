/*
 * Copyright 2018 Carlos Conyers
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package scamper

import java.net.{ HttpURLConnection, Socket, URI, URL, URLDecoder, URLEncoder }
import java.time.{ LocalDate, LocalDateTime, OffsetDateTime }

import scala.util.Try

/** Includes type classes for String, URI, and URL. */
object ImplicitExtensions {
  private val crlf = "\r\n".getBytes("ascii")

  /** Adds HTTP related extension methods to {@code java.net.Socket}. */
  implicit class HttpSocketType(val socket: Socket) extends AnyVal {
    /**
     * Reads next byte from socket input stream.
     *
     * @return byte
     */
    def read(): Int = socket.getInputStream().read()

    /**
     * Reads bytes from socket input stream into supplied buffer.
     *
     * @param buffer buffer into which bytes are read
     *
     * @return number of bytes read
     */
    def read(buffer: Array[Byte]): Int = socket.getInputStream().read(buffer)

    /**
     * Reads bytes from socket input stream into supplied buffer.
     *
     * @param buffer buffer into which bytes are read
     * @param offset starting offset in buffer
     * @param length maximum number of bytes to read
     *
     * @return number of bytes read
     */
    def read(buffer: Array[Byte], offset: Int, length: Int): Int =
      socket.getInputStream().read(buffer, offset, length)

    /**
     * Reads line of ASCII text from socket input stream.
     *
     * @param buffer byte buffer for text
     *
     * @return line of text
     */
    def readLine(buffer: Array[Byte]): String = {
      var len = 0
      var byte = -1

      while ({ byte = read(); byte != '\n' && byte != -1}) {
        buffer(len) = byte.toByte
        len += 1
      }

      if (len > 0 && buffer(len - 1) == '\r')
        len -= 1

      new String(buffer, 0, len, "ascii")
    }

    /**
     * Writes byte to socket output stream.
     *
     * @param byte byte to be written
     */
    def write(byte: Int): Unit = socket.getOutputStream().write(byte)

    /**
     * Writes bytes from supplied buffer to socket output stream.
     *
     * @param buffer buffer from which bytes are written
     */
    def write(buffer: Array[Byte]): Unit = socket.getOutputStream().write(buffer)

    /**
     * Writes bytes from supplied buffer to socket output stream.
     *
     * @param buffer buffer from which bytes are read
     * @param offset starting offset in buffer
     * @param length number of bytes to write
     */
    def write(buffer: Array[Byte], offset: Int, length: Int): Unit =
      socket.getOutputStream().write(buffer, offset, length)

    /**
     * Writes line of ASCII text to socket output stream. The text is written
     * followed by CRLF.
     *
     * @param text line of text
     */
    def writeLine(text: String): Unit = {
      write(text.getBytes("ascii"))
      write(crlf)
    }

    /** Writes empty line followed by CRLF. */
    def writeLine(): Unit = write(crlf)
  }

  /** Adds HTTP related extension methods to {@code String}. */
  implicit class HttpStringType(val string: String) extends AnyVal {
    /**
     * Converts to LocalDate.
     *
     * The date string must be in ISO-8601 extended local date format, such as
     * {@code 2016-11-08}.
     */
    def toLocalDate: LocalDate = LocalDate.parse(string)

    /**
     * Converts to LocalDateTime.
     *
     * The date string must be in ISO-8601 extended local date-time format, such
     * as {@code 2016-11-08T21:00:00}.
     */
    def toLocalDateTime: LocalDateTime = LocalDateTime.parse(string)

    /**
     * Converts to OffsetDateTime.
     *
     * The date string must be in either one of two formats:
     *
     * <ul>
     *   <li>ISO-8601 extended offset date-time format, such as
     *   {@code 2016-11-08T21:00:00-05:00}</li>
     *   <li>RFC 1123 format, such as
     *   {@code Tue, 8 Nov 2016 21:00:00 -0500}</li>
     * </ul>
     */
    def toOffsetDateTime: OffsetDateTime =
      Try(OffsetDateTime.parse(string)).getOrElse(DateValue.parse(string))

    /** Converts to URI. */
    def toURI: URI = new URI(string)

    /** Converts to URL. */
    def toURL: URL = new URL(string)

    /**
     * Encodes to application/x-www-form-urlencoded using the given character encoding.
     *
     * @param encoding the character encoding
     */
    def toURLEncoded(encoding: String): String = URLEncoder.encode(string, encoding)

    /**
     * Decodes from application/x-www-form-urlencoded using the given character encoding.
     *
     * @param encoding the character encoding
     */
    def toURLDecoded(encoding: String): String = URLDecoder.decode(string, encoding)
  }

  /** Adds HTTP related extension methods to {@code java.net.URI}. */
  implicit class HttpUriType(val uri: URI) extends AnyVal {
    /** Gets query parameters. */
    def getQueryParams(): Map[String, Seq[String]] =
      QueryParams.parse(uri.getRawQuery)

    /**
     * Gets value for named query parameter.
     *
     * If there are multiple parameters with given name, then value of first
     * occurrence is retrieved.
     */
    def getQueryParamValue(name: String): Option[String] =
      getQueryParams().get(name).flatMap(_.headOption)

    /** Gets all values for named query parameter. */
    def getQueryParamValues(name: String): Seq[String] =
      getQueryParams().getOrElse(name, Nil)

    /** Creates new URI replacing scheme. */
    def withScheme(scheme: String): URI =
      buildURI(scheme, uri.getRawAuthority, uri.getRawPath, uri.getRawQuery, uri.getRawFragment)

    /** Creates new URI replacing authority. */
    def withAuthority(authority: String): URI =
      buildURI(uri.getScheme, authority, uri.getRawPath, uri.getRawQuery, uri.getRawFragment)

    /** Creates new URI replacing path. */
    def withPath(path: String): URI =
      buildURI(uri.getScheme, uri.getRawAuthority, path, uri.getRawQuery, uri.getRawFragment)

    /** Creates new URI replacing query. */
    def withQuery(query: String): URI =
      buildURI(uri.getScheme, uri.getRawAuthority, uri.getRawPath, query, uri.getRawFragment)

    /** Creates new URI replacing query parameters. */
    def withQueryParams(params: Map[String, Seq[String]]): URI =
      withQuery(QueryParams.format(params))

    /** Creates new URI replacing query parameters. */
    def withQueryParams(params: (String, String)*): URI =
      withQuery(QueryParams.format(params : _*))

    /** Creates new URI replacing fragment. */
    def withFragment(fragment: String): URI =
      buildURI(uri.getScheme, uri.getRawAuthority, uri.getRawPath, uri.getRawQuery, fragment)

    private def buildURI(scheme: String, authority: String, path: String, query: String, fragment: String): URI = {
      val uriBuilder = new StringBuilder()

      if (scheme != null) uriBuilder.append(scheme).append(":")
      if (authority != null) uriBuilder.append("//").append(authority)

      uriBuilder.append('/').append(path.dropWhile(_ == '/'))

      if (query != null && !query.isEmpty) uriBuilder.append('?').append(query)
      if (fragment != null) uriBuilder.append('#').append(fragment)

      new URI(uriBuilder.toString)
    }
  }

  /** Adds HTTP related extension methods to {@code java.net.URL}. */
  implicit class HttpUrlType(val url: URL) extends AnyVal {
    /** Gets query parameters. */
    def getQueryParams(): Map[String, Seq[String]] =
      QueryParams.parse(url.getQuery)

    /**
     * Gets value for named query parameter.
     *
     * If there are multiple parameters with given name, then value of first
     * occurrence is retrieved.
     */
    def getQueryParamValue(name: String): Option[String] =
      getQueryParams().get(name).flatMap(_.headOption)

    /** Gets all values for named query parameter. */
    def getQueryParamValues(name: String): Seq[String] =
      getQueryParams().getOrElse(name, Nil)

    /**
     * Opens HTTP connection and passes it to supplied handler.
     *
     * The connection is disconnected upon handler return.
     *
     * @param handler connection handler
     *
     * @return value from supplied handler
     */
    def withConnection[T](handler: HttpURLConnection => T): T = {
      val conn = url.openConnection().asInstanceOf[HttpURLConnection]
      try handler(conn)
      finally Try(conn.disconnect())
    }
  }
}
