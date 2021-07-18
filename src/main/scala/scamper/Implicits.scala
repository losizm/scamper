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

import java.io.{ File, InputStream, OutputStream }
import java.time.Instant

import scamper.types.MediaType

/** Includes implicit conversions and extension methods. */
object Implicits:
  /** Converts string to `Uri`. */
  given stringToUri: Conversion[String, Uri] with
    def apply(uri: String) = Uri(uri)

  /** Converts string to [[Header]]. */
  given stringToHeader: Conversion[String, Header] with
    def apply(header: String) = Header(header)

  /** Converts tuple to [[Header]] where tuple is name-value pair. */
  given tupleToHeader: Conversion[(String, String), Header] with
    def apply(header: (String, String)) = Header(header._1, header._2)

  /** Converts tuple to [[Header]] where tuple is name-value pair. */
  given tupleToHeaderWithLongValue: Conversion[(String, Long), Header] with
    def apply(header: (String, Long)) = Header(header._1, header._2)

  /** Converts tuple to [[Header]] where tuple is name-value pair. */
  given tupleToHeaderWithIntValue: Conversion[(String, Int), Header] with
    def apply(header: (String, Int)) = Header(header._1, header._2)

  /** Converts tuple to [[Header]] where tuple is name-value pair. */
  given tupleToHeaderWithDateValue: Conversion[(String, Instant), Header] with
    def apply(header: (String, Instant)) = Header(header._1, header._2)

  /** Converts tuple to [[TextPart]] where tuple is name-content pair. */
  given tupleToTextPart: Conversion[(String, String), TextPart] with
    def apply(part: (String, String)) = TextPart(part._1, part._2)

  /** Converts tuple to [[FilePart]] where tuple is name-content pair. */
  given tupleToFilePart: Conversion[(String, File), FilePart] with
    def apply(part: (String, File)) = FilePart(part._1, part._2)

  /** Converts byte array to [[Entity]]. */
  given bytesToEntity: Conversion[Array[Byte], Entity] with
    def apply(entity: Array[Byte]) = Entity(entity)

  /** Converts string to [[Entity]]. */
  given stringToEntity: Conversion[String, Entity] with
    def apply(entity: String) = Entity(entity, "UTF-8")

  /** Converts file to [[Entity]]. */
  given fileToEntity: Conversion[File, Entity] with
    def apply(entity: File) = Entity(entity)

  /** Converts input stream to [[Entity]]. */
  given inputStreamToEntity: Conversion[InputStream, Entity] with
    def apply(entity: InputStream) = Entity(entity)

  /** Converts writer to [[Entity]]. */
  given writerToEntity: Conversion[(OutputStream => Unit), Entity] with
    def apply(writer: OutputStream => Unit) = Entity(writer)

  /** Converts string to [[RequestMethod]]. */
  given stringToRequestMethod: Conversion[String, RequestMethod] with
    def apply(method: String) = RequestMethod(method)

  /** Converts int to [[ResponseStatus]]. */
  given intToResponseStatus: Conversion[Int, ResponseStatus] with
    def apply(statusCode: Int) = ResponseStatus(statusCode)

  /**
   * Adds extension methods to HttpMessage for building messages with various
   * content types.
   */
  implicit class HttpMessageType[T <: HttpMessage](message: T) extends AnyVal:
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
    def setTextBody(text: String, charset: String = "UTF-8")(implicit ev: <:<[T, MessageBuilder[T]]): T =
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
    def setFileBody(file: File)(implicit ev: <:<[T, MessageBuilder[T]]): T =
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
    def setFormBody(data: Map[String, Seq[String]])(implicit ev: <:<[T, MessageBuilder[T]]): T =
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
    def setFormBody(data: Seq[(String, String)])(implicit ev: <:<[T, MessageBuilder[T]]): T =
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
    def setFormBody(one: (String, String), more: (String, String)*)(implicit ev: <:<[T, MessageBuilder[T]]): T =
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
    def setFormBody(query: QueryString)(implicit ev: <:<[T, MessageBuilder[T]]): T =
      val entity = Entity(query)
      message.setBody(entity)
        .putHeaders(
          Header("Content-Type", "application/x-www-form-urlencoded"),
          Header("Content-Length", entity.knownSize.get)
        )

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
      message.setBody(Entity(multipart, boundary))
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
