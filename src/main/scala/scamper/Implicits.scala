/*
 * Copyright 2019 Carlos Conyers
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
import java.time.Instant

import scamper.types.MediaType

/** Includes implicit converter functions. */
object Implicits {
  /** Converts string to `Uri`. */
  implicit val stringToUri = (uri: String) => Uri(uri)

  /** Converts string to [[Header]]. */
  implicit val stringToHeader = (header: String) => Header(header)

  /** Converts tuple to [[Header]] where tuple is name-value pair. */
  implicit val tupleToHeader = (header: (String, String)) => Header(header._1, header._2)

  /** Converts tuple to [[Header]] where tuple is name-value pair. */
  implicit val tupleToHeaderWithLongValue = (header: (String, Long)) => Header(header._1, header._2)

  /** Converts tuple to [[Header]] where tuple is name-value pair. */
  implicit val tupleToHeaderWithIntValue = (header: (String, Int)) => Header(header._1, header._2)

  /** Converts tuple to [[Header]] where tuple is name-value pair. */
  implicit val tupleToHeaderWithDateValue = (header: (String, Instant)) => Header(header._1, header._2)

  /** Converts tuple to [[TextPart]] where tuple is name-content pair. */
  implicit val tupleToTextPart = (part: (String, String)) => TextPart(part._1, part._2)

  /** Converts tuple to [[FilePart]] where tuple is name-content pair. */
  implicit val tupleToFilePart = (part: (String, File)) => FilePart(part._1, part._2)

  /** Converts byte array to [[Entity]]. */
  implicit val bytesToEntity = (entity: Array[Byte]) => Entity(entity)

  /** Converts string to [[Entity]]. */
  implicit val stringToEntity = (entity: String) => Entity(entity, "UTF-8")

  /** Converts file to [[Entity]]. */
  implicit val fileToEntity = (entity: File) => Entity(entity)

  /** Converts input stream to [[Entity]]. */
  implicit val inputStreamToEntity = (entity: InputStream) => Entity(entity)

  /** Converts writer to [[Entity]]. */
  implicit val writerToEntity = (writer: OutputStream => Unit) => Entity(writer)

  /** Converts string to [[RequestMethod]]. */
  implicit val stringToRequestMethod = (method: String) => RequestMethod(method)

  /** Converts int to [[ResponseStatus]]. */
  implicit val intToResponseStatus = (statusCode: Int) => ResponseStatus(statusCode)

  /**
   * Adds extension methods to HttpMessage for building messages with various
   * content types.
   */
  implicit class HttpMessageType[T <: HttpMessage](private val message: T) extends AnyVal {
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
    def withTextBody(text: String, charset: String = "UTF-8")(implicit ev: <:<[T, MessageBuilder[T]]): T = {
      val entity = Entity(text, charset)
      message.withBody(entity)
        .withHeader(Header("Content-Type", s"text/plain; charset=$charset"))
        .withHeader(Header("Content-Length", entity.getLength.get))
    }

    /**
     * Creates new message with content from supplied file as message body.
     *
     * After adding body to message, the Content-Type header is set based on
     * file type, and Content-Length is set to file size.
     *
     * @param file message body
     */
    def withFileBody(file: File)(implicit ev: <:<[T, MessageBuilder[T]]): T = {
      val entity = Entity(file)
      val mediaType = MediaType.fromFile(file).getOrElse(Auxiliary.`application/octet-stream`)
      message.withBody(entity)
        .withHeader(Header("Content-Type", mediaType.toString))
        .withHeader(Header("Content-Length", entity.getLength.get))
    }

    /**
     * Creates new message with supplied form data as message body.
     *
     * After adding body to message, the Content-Type header is set to
     * `application/x-www-form-urlencoded`, and Content-Length is set to length
     * of encoded form data.
     *
     * @param data message body
     */
    def withFormBody(data: Map[String, Seq[String]])(implicit ev: <:<[T, MessageBuilder[T]]): T =
      withQueryBody(QueryString(data))

    /**
     * Creates new message with supplied form data as message body.
     *
     * After adding body to message, the Content-Type header is set to
     * `application/x-www-form-urlencoded`, and Content-Length is set to length
     * of encoded form data.
     *
     * @param data message body
     */
    def withFormBody(data: (String, String)*)(implicit ev: <:<[T, MessageBuilder[T]]): T =
      withQueryBody(QueryString(data : _*))

    /**
     * Creates new message with supplied query string as message body.
     *
     * After adding body to message, the Content-Type header is set to
     * `application/x-www-form-urlencoded`, and Content-Length is set to length
     * of encoded query string.
     *
     * @param query message body
     */
    def withQueryBody(query: QueryString)(implicit ev: <:<[T, MessageBuilder[T]]): T = {
      val entity = Entity(query)
      message.withBody(entity)
        .withHeader(Header("Content-Type", "application/x-www-form-urlencoded"))
        .withHeader(Header("Content-Length", entity.getLength.get))
    }

    /**
     * Creates new message with supplied multipart as message body.
     *
     * After adding body to message, the Content-Type header is set to
     * `multipart/form-data` with a boundary parameter whose value is used
     * to delimit parts in encoded message body.
     *
     * @param multipart message body
     */
    def withMultipartBody(multipart: Multipart)(implicit ev: <:<[T, MessageBuilder[T]]): T = {
      val boundary = Multipart.boundary()
      message.withBody(Entity(multipart, boundary))
        .withHeader(Header("Content-Type", s"multipart/form-data; boundary=$boundary"))
    }

    /**
     * Creates new message with supplied parts as message body, with the parts
     * encoded as multipart form data.
     *
     * Before adding body to message, the Content-Type header is set to
     * `multipart/form-data` with a boundary parameter whose value is used
     * to delimit parts in encoded message body.
     *
     * @param parts message body
     */
    def withMultipartBody(parts: Part*)(implicit ev: <:<[T, MessageBuilder[T]]): T =
      withMultipartBody(Multipart(parts : _*))
  }
}
