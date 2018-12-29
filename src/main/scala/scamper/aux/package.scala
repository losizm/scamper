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

import java.io.{ File, InputStream, OutputStream }
import java.net.{ Socket, URI, URLDecoder, URLEncoder }
import java.nio.file.{ Paths, Path }
import java.time.Instant

import scala.collection.mutable.ArrayBuffer
import scala.util.Try

/** Provides auxiliary type classes. */
package object aux {
  private val crlf = "\r\n".getBytes("UTF-8")

  /** Adds extension methods to {@code java.io.InputStream}. */
  implicit class InputStreamType(val in: InputStream) extends AnyVal {
    /**
     * Reads remaining bytes into byte array.
     *
     * @param bufferSize buffer size used to copy bytes
     */
    def getBytes(bufferSize: Int = 8192): Array[Byte] = {
      val bytes = new ArrayBuffer[Byte]
      val buffer = new Array[Byte](bufferSize.max(1024))
      var len = 0

      while ({ len = in.read(buffer); len != -1 })
        bytes ++= buffer.take(len)

      bytes.toArray
    }

    /**
     * Reads remaining bytes as text.
     *
     * @param bufferSize buffer size used to copy bytes
     */
    def getText(bufferSize: Int = 8192): String =
      new String(getBytes(bufferSize), "UTF-8")

    /**
     * Reads token from input stream. The delimiter is removed before token is
     * returned.
     *
     * @param delimiters token delimiters
     * @param buffer token buffer
     */
    def readToken(delimiters: String, buffer: Array[Byte]): String = {
      var length = 0
      var byte = -1

      while ({ byte = in.read(); !delimiters.contains(byte) && byte != -1}) {
        buffer(length) = byte.toByte
        length += 1
      }

      new String(buffer, 0, length, "UTF-8")
    }

    /**
     * Reads line of text from input stream. The CRLF characters are removed
     * before text is returned.
     *
     * @param buffer text buffer
     */
    def readLine(buffer: Array[Byte]): String = {
      var length = 0
      var byte = -1

      while ({ byte = in.read(); byte != '\n' && byte != -1}) {
        buffer(length) = byte.toByte
        length += 1
      }

      if (length > 0 && buffer(length - 1) == '\r')
        length -= 1

      new String(buffer, 0, length, "UTF-8")
    }
  }

  /** Adds extension methods to {@code java.io.OutputStream}. */
  implicit class OutputStreamType(val out: OutputStream) extends AnyVal {
    /**
     * Writes supplied text followed by CRLF characters to output stream.
     *
     * @param text text
     */
    def writeLine(text: String): Unit = {
      out.write(text.getBytes("UTF-8"))
      out.write(crlf)
    }

    /** Writes CRLF characters to output stream. */
    def writeLine(): Unit = out.write(crlf)
  }

  /** Adds extension methods to {@code java.net.Socket}. */
  implicit class SocketType(val socket: Socket) extends AnyVal {
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
     * Reads line of text from socket input stream. The CRLF characters are
     * removed before value is returned.
     *
     * @param buffer text buffer
     *
     * @return line of text
     */
    def readLine(buffer: Array[Byte]): String =
      socket.getInputStream().readLine(buffer)

    /**
     * Reads token from socket input stream. The delimiter is removed before
     * token is returned.
     *
     * @param delimiters token delimiters
     * @param buffer token buffer
     */
    def readToken(delimiters: String, buffer: Array[Byte]): String =
      socket.getInputStream().readToken(delimiters, buffer)

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
     * Writes supplied text and CRLF characters to socket output stream.
     *
     * @param text text
     */
    def writeLine(text: String): Unit = socket.getOutputStream().writeLine(text)

    /** Writes CRLF characters to output stream. */
    def writeLine(): Unit = socket.getOutputStream().writeLine()

    /** Flushes socket output stream. */
    def flush(): Unit = socket.getOutputStream().flush()
  }

  /** Adds extension methods to {@code String}. */
  implicit class StringType(val string: String) extends AnyVal {
    /**
     * Tests whether string matches any of supplied regular expressions.
     *
     * <strong>Note:</strong> If `regexes` is empty, then `false` is returned.
     *
     * @return `true` if string matches at least one regular expression, `false`
     *  otherwise
     */
    def matchesAny(regexes: String*): Boolean = regexes.exists(string.matches)

    /**
     * Converts to Instant.
     *
     * The date string must be in either one of two formats:
     *
     * <ul>
     *   <li>ISO-8601 instant format, such as `2016-11-08T21:00:00Z`</li>
     *   <li>RFC 1123 format, such as `Tue, 8 Nov 2016 21:00:00 GMT`</li>
     * </ul>
     */
    def toInstant: Instant =
      Try(Instant.parse(string)).getOrElse(DateValue.parse(string))

    /** Converts to File. */
    def toFile: File = new File(string)

    /** Converts to Path. */
    def toPath: Path = Paths.get(string)

    /** Converts to URI. */
    def toUri: URI = new URI(string)

    /** Encodes to application/x-www-form-urlencoded using UTF-8 character encoding. */
    def toUrlEncoded: String = URLEncoder.encode(string, "UTF-8")

    /**
     * Encodes to application/x-www-form-urlencoded using given character encoding.
     *
     * @param encoding character encoding
     */
    def toUrlEncoded(encoding: String): String = URLEncoder.encode(string, encoding)

    /** Decodes from application/x-www-form-urlencoded using UTF-8 character encoding. */
    def toUrlDecoded: String = URLDecoder.decode(string, "UTF-8")

    /**
     * Decodes from application/x-www-form-urlencoded using given character encoding.
     *
     * @param encoding character encoding
     */
    def toUrlDecoded(encoding: String): String = URLDecoder.decode(string, encoding)
  }

  /** Adds extension methods to {@code java.net.URI}. */
  implicit class UriType(val uri: URI) extends AnyVal {
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
      buildUri(scheme, uri.getRawAuthority, uri.getRawPath, uri.getRawQuery, uri.getRawFragment)

    /** Creates new URI replacing authority. */
    def withAuthority(authority: String): URI =
      buildUri(uri.getScheme, authority, uri.getRawPath, uri.getRawQuery, uri.getRawFragment)

    /** Creates new URI replacing path. */
    def withPath(path: String): URI =
      buildUri(uri.getScheme, uri.getRawAuthority, path, uri.getRawQuery, uri.getRawFragment)

    /** Creates new URI replacing query. */
    def withQuery(query: String): URI =
      buildUri(uri.getScheme, uri.getRawAuthority, uri.getRawPath, query, uri.getRawFragment)

    /** Creates new URI replacing query parameters. */
    def withQueryParams(params: Map[String, Seq[String]]): URI =
      withQuery(QueryParams.format(params))

    /** Creates new URI replacing query parameters. */
    def withQueryParams(params: (String, String)*): URI =
      withQuery(QueryParams.format(params : _*))

    /** Creates new URI replacing fragment. */
    def withFragment(fragment: String): URI =
      buildUri(uri.getScheme, uri.getRawAuthority, uri.getRawPath, uri.getRawQuery, fragment)

    private def buildUri(scheme: String, authority: String, path: String, query: String, fragment: String): URI = {
      val uri = new StringBuilder()

      if (scheme != null) uri.append(scheme).append(":")
      if (authority != null) uri.append("//").append(authority)

      uri.append('/').append(path.dropWhile(_ == '/'))

      if (query != null && ! query.isEmpty) uri.append('?').append(query)
      if (fragment != null && ! fragment.isEmpty) uri.append('#').append(fragment)

      new URI(uri.toString)
    }
  }
}
