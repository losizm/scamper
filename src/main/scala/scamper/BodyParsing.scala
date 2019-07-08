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
trait BodyParsing {
  /**
   * Gets maximum body length.
   *
   * The input stream obtained from `withInputStream` throws [[ReadLimitExceeded]]
   * if an attempt is made to read beyond `maxLength` from message body.
   */
  def maxLength: Long

  /** Gets buffer size. */
  def bufferSize: Int

  /**
   * Provides input stream to decoded message body.
   *
   * The decoded input stream is passed to supplied function.
   *
   * @param message HTTP message
   * @param f stream handler
   *
   * @return value from applied handler
   * @note Input stream throws [[ReadLimitExceeded]] if attempt is made to read
   *   beyond `maxLength` from message body.
   */
  def withInputStream[T](message: HttpMessage)(f: InputStream => T): T =
    if (message.body.isKnownEmpty)
      f(EmptyInputStream)
    else
      message match {
        case res: HttpResponse if res.status.isInformational => f(EmptyInputStream)
        case res: HttpResponse if res.status.code == 204     => f(EmptyInputStream)
        case res: HttpResponse if res.status.code == 304     => f(EmptyInputStream)
        case _ =>
          message.body.withInputStream { in =>
            val transferIn = message.transferEncoding match {
              case Nil      => new BoundedInputStream(in, maxLength, getContentLength(message))
              case encoding => transferInputStream(new BoundedInputStream(in, maxLength, Long.MaxValue), encoding)
            }

            val contentIn = contentInputStream(transferIn, message.contentEncoding)

            f(contentIn)
          }
      }

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
