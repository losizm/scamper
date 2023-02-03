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

import java.io.InputStream
import java.util.zip.{ GZIPInputStream, InflaterInputStream }

import scamper.http.headers.{ ContentEncoding, ContentLength, TransferEncoding }
import scamper.http.types.{ ContentCoding, TransferCoding }

/** Provides access to decoded message body. */
trait BodyDecoder:
  /** Gets maximum length of message body. */
  def maxLength: Long

  /**
   * Gets decoded message body.
   *
   * @param message HTTP message
   *
   * @note The input stream throws [[ReadLimitExceeded]] if the decoder reads
   * beyond `maxLength` of message body.
   */
  def decode(message: HttpMessage): InputStream =
    if message.body.isKnownEmpty then
      EmptyInputStream
    else
      message match
        case res: HttpResponse if res.isInformational   => EmptyInputStream
        case res: HttpResponse if res.statusCode == 204 => EmptyInputStream
        case res: HttpResponse if res.statusCode == 304 => EmptyInputStream
        case _ =>
          val transferIn = message.transferEncoding match
            case Nil      => BoundedInputStream(message.body.data, maxLength, getContentLength(message))
            case encoding => transferInputStream(BoundedInputStream(message.body.data, maxLength, Long.MaxValue), encoding)

          contentInputStream(transferIn, message.contentEncoding)

  private def transferInputStream(in: InputStream, encoding: Seq[TransferCoding]): InputStream =
    encoding.takeRight(6).foldRight(in) { (encoding, in) =>
      if      encoding.isChunked then ChunkedInputStream(in)
      else if encoding.isGzip    then GZIPInputStream(in)
      else if encoding.isDeflate then InflaterInputStream(in)
      else throw HttpException(s"Unsupported transfer encoding: $encoding")
    }

  private def contentInputStream(in: InputStream, encoding: Seq[ContentCoding]): InputStream =
    encoding.takeRight(6).foldRight(in) { (encoding, in) =>
      if      encoding.isGzip     then GZIPInputStream(in)
      else if encoding.isDeflate  then InflaterInputStream(in)
      else if encoding.isIdentity then in
      else throw HttpException(s"Unsupported content encoding: $encoding")
    }

  private def getContentLength(message: HttpMessage): Long =
    message.contentLengthOption
      .orElse(message.body.knownSize)
      .orElse(Option.when(message.isInstanceOf[HttpResponse])(Long.MaxValue))
      .getOrElse(0)

/** Provides factory for `BodyDecoder`. */
object BodyDecoder:
  /**
   * Creates decoder with specified maximum length.
   *
   * @param maxLength maximum length of message body
   */
  def apply(maxLength: Long): BodyDecoder =
    BodyDecoderImpl(maxLength)

  /**
   * Gets decoded message body.
   *
   * @param message HTTP message
   * @param maxLength maximum length of message body
   *
   * @note The input stream throws [[ReadLimitExceeded]] if the decoder reads
   * beyond `maxLength` of message body.
   */
  def decode(message: HttpMessage, maxLength: Long): InputStream =
    BodyDecoderImpl(maxLength).decode(message)

private class BodyDecoderImpl(val maxLength: Long) extends BodyDecoder
