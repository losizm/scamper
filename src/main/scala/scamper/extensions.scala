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

import java.io.File

import scamper.types.MediaType

extension [T <: HttpMessage & MessageBuilder[T]](message: T)
  /**
   * Creates new message with supplied text as message body.
   *
   * After adding body to message, the Content-Type header is set to
   * `text/plain` with its charset parameter set accordingly, and
   * Content-Length is set to length of encoded characters.
   *
   * @param text message body
   * @param charset character set
   */
  def setTextBody(text: String, charset: String = "UTF-8"): T =
    val entity = Entity(text, charset)
    message.setBody(entity)
      .putHeaders(
        Header("Content-Type", s"text/plain; charset=$charset"),
        Header("Content-Length", entity.knownSize.get)
      )

  /**
   * Creates new message with content from supplied file as message body.
   *
   * After adding body to message, the Content-Type header is set based on
   * file type, and Content-Length is set to file size.
   *
   * @param file message body
   */
  def setFileBody(file: File): T =
    val entity = Entity(file)
    val mediaType = MediaType.forFile(file).getOrElse(Auxiliary.applicationOctetStream)
    message.setBody(entity)
      .putHeaders(
        Header("Content-Type", mediaType.toString),
        Header("Content-Length", entity.knownSize.get)
      )

  /**
   * Creates new message with supplied form data as message body.
   *
   * After adding body to message, the Content-Type header is set to
   * `application/x-www-form-urlencoded`, and Content-Length is set to length
   * of encoded form data.
   *
   * @param data message body
   */
  def setFormBody(data: Map[String, Seq[String]]): T =
    setFormBody(QueryString(data))

  /**
   * Creates new message with supplied form data as message body.
   *
   * After adding body to message, the Content-Type header is set to
   * `application/x-www-form-urlencoded`, and Content-Length is set to length
   * of encoded form data.
   *
   * @param data message body
   */
  def setFormBody(data: Seq[(String, String)]): T =
    setFormBody(QueryString(data))

  /**
   * Creates new message with supplied form data as message body.
   *
   * After adding body to message, the Content-Type header is set to
   * `application/x-www-form-urlencoded`, and Content-Length is set to length
   * of encoded form data.
   *
   * @param one form data
   * @param more additional form data
   */
  def setFormBody(one: (String, String), more: (String, String)*): T =
    setFormBody(QueryString(one +: more))

  /**
   * Creates new message with supplied query string as message body.
   *
   * After adding body to message, the Content-Type header is set to
   * `application/x-www-form-urlencoded`, and Content-Length is set to length
   * of encoded query string.
   *
   * @param query message body
   */
  def setFormBody(query: QueryString): T =
    val entity = Entity(query)
    message.setBody(entity)
      .putHeaders(
        Header("Content-Type", "application/x-www-form-urlencoded"),
        Header("Content-Length", entity.knownSize.get)
      )
