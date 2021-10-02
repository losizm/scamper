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
package multipart

import java.io.File

/** Converts tuple to [[TextPart]] where tuple is name-content pair. */
given tupleToTextPart: Conversion[(String, String), TextPart] with
  def apply(part: (String, String)) = TextPart(part._1, part._2)

/** Converts tuple to [[FilePart]] where tuple is name-content pair. */
given tupleToFilePart: Conversion[(String, File), FilePart] with
  def apply(part: (String, File)) = FilePart(part._1, part._2)

/**
 * Adds extension methods to HttpMessage for building messages with various
 * content types.
 */
implicit class MultipartMessage[T <: HttpMessage](message: T) extends AnyVal:
  /**
   * Creates new message with supplied multipart as message body.
   *
   * After adding body to message, the Content-Type header is set to
   * `multipart/form-data` with a boundary parameter whose value is used
   * to delimit parts in encoded message body.
   *
   * @param multipart message body
   */
  def setMultipartBody(multipart: Multipart)(implicit ev: <:<[T, MessageBuilder[T]]): T =
    val boundary = Multipart.boundary()
    message.setBody(multipart.toEntity(boundary))
      .putHeaders(Header("Content-Type", s"multipart/form-data; boundary=$boundary"))

  /**
   * Creates new message with supplied parts as message body, with the parts
   * encoded as multipart form data.
   *
   * After adding body to message, the Content-Type header is set to
   * `multipart/form-data` with a boundary parameter whose value is used
   * to delimit parts in encoded message body.
   *
   * @param parts message body
   */
  def setMultipartBody(parts: Seq[Part])(implicit ev: <:<[T, MessageBuilder[T]]): T =
    setMultipartBody(Multipart(parts))

  /**
   * Creates new message with supplied parts as message body, with the parts
   * encoded as multipart form data.
   *
   * After adding body to message, the Content-Type header is set to
   * `multipart/form-data` with a boundary parameter whose value is used
   * to delimit parts in encoded message body.
   *
   * @param one part
   * @param more additional parts
   */
  def setMultipartBody(one: Part, more: Part*)(implicit ev: <:<[T, MessageBuilder[T]]): T =
    setMultipartBody(Multipart(one +: more))
