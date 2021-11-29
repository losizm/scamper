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

extension [T <: HttpMessage & MessageBuilder[T]](message: T)
  /**
   * Creates new message with supplied bytes as message body.
   *
   * __Content-Type__ is set to `application/octet-stream`; __Content-Length__
   * is set to length of bytes.
   *
   * @param text message body
   * @param charset character set
   */
  def setOctetStream(bytes: Array[Byte]): T =
    setBody(Entity(bytes), MediaType.octetStream)

  /**
   * Creates new message with supplied text as message body.
   *
   * __Content-Type__ is set to `text/plain` with specified charset;
   * __Content-Length__ is set to length of encoded text.
   *
   * @param text message body
   * @param charset character set
   */
  def setPlain(text: String, charset: String = "UTF-8"): T =
    setBody(Entity(text.getBytes(charset)), MediaType.plain(charset))

  /**
   * Creates new message with supplied file as message body.
   *
   * __Content-Type__ is set according to file type; __Content-Length__ is
   * set to length of file.
   *
   * @param file message body
   */
  def setFile(file: File): T =
    setBody(Entity(file), MediaType.forFile(file).getOrElse(MediaType.octetStream))

  /**
   * Creates new message with form data from supplied query string as message
   * body.
   *
   * __Content-Type__ is set to `application/x-www-form-urlencoded`;
   * __Content-Length__ is set to length of encoded form data.
   *
   * @param query message body
   *
   * @note Content-Type and Content-Length headers are set accordingly.
   */
  def setForm(query: QueryString): T =
    setBody(Entity(query), MediaType.formUrlencoded)

  /**
   * Creates new message with supplied form data as message body.
   *
   * __Content-Type__ is set to `application/x-www-form-urlencoded`;
   * __Content-Length__ is set to length of encoded form data.
   *
   * @param data message body
   */
  def setForm(data: Map[String, Seq[String]]): T =
    setForm(QueryString(data))

  /**
   * Creates new message with supplied form data as message body.
   *
   * __Content-Type__ is set to `application/x-www-form-urlencoded`;
   * __Content-Length__ is set to length of encoded form data.
   *
   * @param data message body
   */
  def setForm(data: Seq[(String, String)]): T =
    setForm(QueryString(data))

  /**
   * Creates new message with supplied form data as message body.
   *
   * __Content-Type__ is set to `application/x-www-form-urlencoded`;
   * __Content-Length__ is set to length of encoded form data.
   *
   * @param data form data
   * @param more additional form data
   */
  def setForm(data: (String, String), more: (String, String)*): T =
    setForm(QueryString(data +: more))

  private inline def setBody(entity: Entity, contentType: MediaType): T =
    message.setBody(entity).putHeaders(
      Header("Content-Type", contentType.toString),
      Header("Content-Length", entity.knownSize.get)
    )
