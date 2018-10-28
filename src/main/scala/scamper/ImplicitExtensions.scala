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

import java.net.{ HttpURLConnection, URI, URL, URLDecoder, URLEncoder }
import java.time.{ LocalDate, LocalDateTime, OffsetDateTime }

import scala.util.Try

/** Includes type classes for String, URI, and URL. */
object ImplicitExtensions {
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

    /** Converts URI to URL using supplied scheme and authority. */
    def toURL(scheme: String, authority: String): URL =
      buildURI(scheme, authority, uri.getRawPath, uri.getRawQuery, uri.getRawFragment).toURL

    /** Creates new URI replacing path. */
    def withPath(path: String): URI =
      createURI(path, uri.getRawQuery)

    /** Creates new URI replacing query. */
    def withQuery(query: String): URI =
      createURI(uri.getRawPath, query)

    /** Creates new URI replacing query parameters. */
    def withQueryParams(params: Map[String, Seq[String]]): URI =
      withQuery(QueryParams.format(params))

    /** Creates new URI replacing query parameters. */
    def withQueryParams(params: (String, String)*): URI =
      withQuery(QueryParams.format(params : _*))

    private def createURI(path: String, query: String): URI =
      buildURI(uri.getScheme, uri.getRawAuthority, path, query, uri.getRawFragment).toURI
  }

  /** Adds HTTP related extension methods to {@code java.net.URL}. */
  implicit class HttpUrlType(val url: URL) extends AnyVal {
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
      getQueryParams().get(name).flatMap(_.headOption)

    /** Gets all values for named query parameter. */
    def getQueryParamValues(name: String): Seq[String] =
      getQueryParams().getOrElse(name, Nil)

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

    private def createURL(path: String, query: String): URL =
      buildURI(url.getProtocol, url.getAuthority, path, query, url.getRef).toURL
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
