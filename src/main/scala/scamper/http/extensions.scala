/*
 * Copyright 2021 Carlos Conyers
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
package http

import java.io.{ File, InputStream, OutputStream }
import java.time.Instant

import scamper.http.types.MediaType

extension (headers: Seq[Header])
  /** Tests for header with given name. */
  def hasHeader(name: String): Boolean =
    headers.exists(_.name.equalsIgnoreCase(name))

  /** Gets first header with given name. */
  def getHeader(name: String): Option[Header] =
    headers.find(_.name.equalsIgnoreCase(name))

  /** Gets first header with given name, or returns default if header not present. */
  def getHeaderOrElse(name: String, default: => Header): Header =
    getHeader(name).getOrElse(default)

  /** Gets first header value with given name. */
  def getHeaderValue(name: String): Option[String] =
    getHeader(name).map(_.value)

  /**
   * Gets first header value with given name, or returns default if header not
   * present.
   */
  def getHeaderValueOrElse(name: String, default: => String): String =
    getHeaderValue(name).getOrElse(default)

  /** Gets headers with given name. */
  def getHeaders(name: String): Seq[Header] =
    headers.filter(_.name.equalsIgnoreCase(name))

  /** Gets header values with given name. */
  def getHeaderValues(name: String): Seq[String] =
    getHeaders(name).map(_.value)

extension [T <: HttpMessage & MessageBuilder[T]](message: T)
  /**
   * Creates new message with supplied bytes as message body.
   *
   * __Content-Type__ is set to `application/octet-stream`; __Content-Length__
   * is set to length of bytes.
   *
   * @param bytes message body
   */
  def setOctetBody(bytes: Array[Byte]): T =
    setBodyContent(Entity(bytes), MediaType.octetStream)

  /**
   * Creates new message with supplied text as message body.
   *
   * __Content-Type__ is set to `text/plain` with specified charset, and
   * __Content-Length__ is set to length of encoded text.
   *
   * @param text message body
   * @param charset character set
   */
  def setPlainBody(text: String, charset: String = "UTF-8"): T =
    setBodyContent(Entity(text.getBytes(charset)), MediaType.plain(charset))

  /**
   * Creates new message with supplied file as message body.
   *
   * __Content-Type__ is set according to file name, and __Content-Length__ is
   * set to length of file.
   *
   * @param file message body
   *
   * @see [[scamper.http.types.MediaType$.forFile MediaType.forFile]]
   */
  def setFileBody(file: File): T =
    setBodyContent(Entity(file), MediaType.forFile(file).getOrElse(MediaType.octetStream))

  /**
   * Creates new message with supplied query string as message body.
   *
   * __Content-Type__ is set to `application/x-www-form-urlencoded`, and
   * __Content-Length__ is set to length of encoded form data.
   *
   * @param query message body
   */
  def setFormBody(query: QueryString): T =
    setBodyContent(Entity(query), MediaType.formUrlencoded)

  /**
   * Creates new message with supplied form data as message body.
   *
   * __Content-Type__ is set to `application/x-www-form-urlencoded`, and
   * __Content-Length__ is set to length of encoded form data.
   *
   * @param data form data
   * @param more additional form data
   */
  def setFormBody(data: (String, String), more: (String, String)*): T =
    setFormBody(QueryString(data +: more))

  private def setBodyContent(entity: Entity, contentType: MediaType): T =
    message.setBody(entity).putHeaders(
      Header("Content-Type", contentType.toString),
      Header("Content-Length", entity.knownSize.get)
    )
