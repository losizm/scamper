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
import java.net.URI
import java.time.Instant

/** Includes implicit converter functions. */
object Implicits {
  /** Converts string to {@code java.net.URI}. */
  implicit val stringToUri = (uri: String) => new URI(uri)

  /** Converts string to [[Header]]. */
  implicit val stringToHeader = (header: String) => Header.parse(header)

  /** Converts tuple to [[Header]] where tuple is name-value pair. */
  implicit val tupleToHeader = (header: (String, String)) => Header(header._1, header._2)

  /** Converts tuple to [[Header]] where tuple is name-value pair. */
  implicit val tupleToHeaderWithLongValue = (header: (String, Long)) => Header(header._1, header._2)

  /** Converts tuple to [[Header]] where tuple is name-value pair. */
  implicit val tupleToHeaderWithIntValue = (header: (String, Int)) => Header(header._1, header._2)

  /** Converts tuple to [[Header]] where tuple is name-value pair. */
  implicit val tupleToHeaderWithDateValue = (header: (String, Instant)) => Header(header._1, header._2)

  /** Converts byte array to [[Entity]]. */
  implicit val bytesToEntity = (entity: Array[Byte]) => Entity.fromBytes(entity)

  /** Converts string to [[Entity]]. */
  implicit val stringToEntity = (entity: String) => Entity.fromString(entity, "UTF-8")

  /** Converts file to [[Entity]]. */
  implicit val fileToEntity = (entity: File) => Entity.fromFile(entity)

  /** Converts input stream to [[Entity]]. */
  implicit val inputStreamToEntity = (entity: InputStream) => Entity.fromInputStream(entity)

  /** Converts output stream writer to [[Entity]]. */
  implicit val writerToEntity = (writer: OutputStream => Unit) => Entity.fromOutputStream(writer)

  /** Converts string to [[RequestMethod]]. */
  implicit val stringToRequestMethod = (method: String) => RequestMethod(method)

  /** Converts int to [[ResponseStatus]]. */
  implicit val intToResponseStatus = (statusCode: Int) => ResponseStatus(statusCode)

  /** Adds methods to HttpMessage for building message with mulitpart body. */
  implicit class MultipartHttpMessageType[T <: HttpMessage](val message: T) extends AnyVal {
    /**
     * Creates new message with supplied multipart body.
     *
     * Before adding body to message, the Content-Type header is set to
     * `multipart/form-data` with a boundary parameter whose value is used
     * to delimit parts in encoded message body.
     *
     * @param body multipart body
     */
    def withMultipartBody(body: Multipart)(implicit ev: <:<[T, MessageBuilder[T]]): T = {
      val boundary = Multipart.boundary()
      message.withHeader(Header("Content-Type", s"multipart/form-data; boundary=$boundary"))
        .withBody(Entity.fromMultipart(body, boundary))
    }

    /**
     * Creates new message with multipart body constructed from supplied parts.
     *
     * Before adding body to message, the Content-Type header is set to
     * `multipart/form-data` with a boundary parameter whose value is used
     * to delimit parts in encoded message body.
     *
     * @param parts parts used to construct multipart body
     */
    def withMultipartBody(parts: Part*)(implicit ev: <:<[T, MessageBuilder[T]]): T =
      withMultipartBody(Multipart(parts : _*))
  }
}
