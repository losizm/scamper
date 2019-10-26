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

import java.io.InputStream
import java.util.zip.{ GZIPInputStream, InflaterInputStream }

import headers.{ ContentEncoding, ContentLength, TransferEncoding }
import types.{ ContentCoding, TransferCoding }

/** A mixin providing access to decoded message body. */
trait BodyDecoding {
  /** Gets maximum length of message body. */
  def maxLength: Long

  /**
   * Gets input stream to decoded message body.
   *
   * @param message HTTP message
   *
   * @note The decoded input stream throws [[ReadLimitExceeded]] if it attempts
   *   to read beyond `maxLength` of message body.
   */
  def decode(message: HttpMessage): InputStream =
    if (message.body.isKnownEmpty)
      EmptyInputStream
    else
      message match {
        case res: HttpResponse if res.status.isInformational => EmptyInputStream
        case res: HttpResponse if res.status.code == 204     => EmptyInputStream
        case res: HttpResponse if res.status.code == 304     => EmptyInputStream
        case _ =>
          message.body.withInputStream { in =>
            val transferIn = message.transferEncoding match {
              case Nil      => new BoundedInputStream(in, maxLength, getContentLength(message))
              case encoding => transferInputStream(new BoundedInputStream(in, maxLength, Long.MaxValue), encoding)
            }

            contentInputStream(transferIn, message.contentEncoding)
          }
      }

  /**
   * Gets input stream to decoded message body and passes it to supplied
   * function.
   *
   * @param message HTTP message
   * @param f function
   *
   * @return value from applied function
   *
   * @note The decoded input stream throws [[ReadLimitExceeded]] if it attempts
   *   to read beyond `maxLength` of message body.
   */
  def withDecoded[T](message: HttpMessage)(f: InputStream => T): T =
    f { decode(message) }

  private def transferInputStream(in: InputStream, encoding: Seq[TransferCoding]): InputStream =
    encoding.takeRight(6).foldRight(in) { (encoding, in) =>
      if (encoding.isChunked)
        new ChunkedInputStream(in)
      else if (encoding.isGzip)
        new GZIPInputStream(in)
      else if (encoding.isDeflate)
        new InflaterInputStream(in)
      else throw new HttpException(s"Unsupported transfer encoding: $encoding")
    }

  private def contentInputStream(in: InputStream, encoding: Seq[ContentCoding]): InputStream =
    encoding.takeRight(6).foldRight(in) { (encoding, in) =>
      if (encoding.isGzip)
        new GZIPInputStream(in)
      else if (encoding.isDeflate)
        new InflaterInputStream(in)
      else if (encoding.isIdentity)
        in
      else throw new HttpException(s"Unsupported content encoding: $encoding")
    }

  private def getContentLength(message: HttpMessage): Long =
    message.getContentLength.orElse(message.body.getLength).getOrElse(0)
}

/** Provides factory methods for `BodyDecoding`. */
object BodyDecoding {
  /** Creates instance of `BodyDecoding` that enforces specified max length. */
  def apply(maxLength: Long): BodyDecoding =
    maxLength match {
      case length => new BodyDecoding { val maxLength = length }
    }
}
