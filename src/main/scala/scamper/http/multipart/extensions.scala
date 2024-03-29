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
package multipart

extension [T <: HttpMessage & MessageBuilder[T]](message: T)
  /**
   * Creates new message with multipart message body.
   *
   * __Content-Type__ is set to `multipart/form-data` with generated boundary
   * parameter.
   *
   * @param multipart message body
   */
  def setMultipartBody(multipart: Multipart): T =
    val boundary = Multipart.boundary()
    message.setBody(multipart.toEntity(boundary))
      .putHeaders(Header("Content-Type", s"multipart/form-data; boundary=$boundary"))

  /**
   * Creates new message with multipart message body.
   *
   * __Content-Type__ is set to `multipart/form-data` with generated boundary
   * parameter.
   *
   * @param parts message body
   */
  def setMultipartBody(parts: Seq[Part]): T =
    setMultipartBody(Multipart(parts))

  /**
   * Creates new message with multipart message body.
   *
   * __Content-Type__ is set to `multipart/form-data` with generated boundary
   * parameter.
   *
   * @param one part
   * @param more additional parts
   */
  def setMultipartBody(one: Part, more: Part*): T =
    setMultipartBody(Multipart(one +: more))
