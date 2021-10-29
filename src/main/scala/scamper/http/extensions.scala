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
   * Creates new message with supplied text as message body.
   *
   * @param text message body
   * @param charset character set
   *
   * @note The Content-Type and Content-Length headers are set accordingly.
   */
  def setTextBody(text: String, charset: String = "UTF-8"): T =
    setBody(Entity(text.getBytes(charset)), s"text/plain; charset=$charset")

  /**
   * Creates new message with content from supplied file as message body.
   *
   * @param file message body
   *
   * @note The Content-Type and Content-Length headers are set accordingly.
   */
  def setFileBody(file: File): T =
    setBody(Entity(file), MediaType.forFile(file).getOrElse(MediaType.octetStream).toString)

  /**
   * Creates new message with form data from supplied query string as message
   * body.
   *
   * @param query message body
   *
   * @note The Content-Type and Content-Length headers are set accordingly.
   */
  def setFormBody(query: QueryString): T =
    setBody(Entity(query), "application/x-www-form-urlencoded")

  /**
   * Creates new message with supplied form data as message body.
   *
   * @param data message body
   *
   * @note The Content-Type and Content-Length headers are set accordingly.
   */
  def setFormBody(data: Map[String, Seq[String]]): T =
    setFormBody(QueryString(data))

  /**
   * Creates new message with supplied form data as message body.
   *
   * @param data message body
   *
   * @note The Content-Type and Content-Length headers are set accordingly.
   */
  def setFormBody(data: Seq[(String, String)]): T =
    setFormBody(QueryString(data))

  /**
   * Creates new message with supplied form data as message body.
   *
   * @param data form data
   * @param more additional form data
   *
   * @note The Content-Type and Content-Length headers are set accordingly.
   */
  def setFormBody(data: (String, String), more: (String, String)*): T =
    setFormBody(QueryString(data +: more))

  private inline def setBody(entity: Entity, contentType: String): T =
    message.setBody(entity)
      .putHeaders(
        Header("Content-Type", contentType),
        Header("Content-Length", entity.knownSize.get)
      )
