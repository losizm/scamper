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

import java.io.{ ByteArrayInputStream, InputStream }
import java.util.zip.{ GZIPInputStream, InflaterInputStream }

import headers.{ ContentEncoding, ContentLength, TransferEncoding }
import types.TransferCoding

/** A mixin providing access to decoded message body. */
trait BodyParsing {
  /** Gets maximum body length. */
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
   */
  def withInputStream[T](message: HttpMessage)(f: InputStream => T): T =
    if (message.body.isKnownEmpty)
      f(emptyInputStream)
    else
      message match {
        case res: HttpResponse if res.status.isInformational => f(emptyInputStream)
        case res: HttpResponse if res.status.code == 204     => f(emptyInputStream)
        case res: HttpResponse if res.status.code == 304     => f(emptyInputStream)
        case _ =>
          message.body.withInputStream { in =>
            val decoded = message.transferEncoding match {
              case Nil      => new BoundedInputStream(in, getContentLength(message))
              case encoding => decodeInputStream(in, encoding)
            }

            getContentEncoding(message) match {
              case "gzip"     => f(new GZIPInputStream(decoded))
              case "deflate"  => f(new InflaterInputStream(decoded))
              case "identity" => f(decoded)
              case encoding   => throw new HttpException(s"Unsupported content encoding: $encoding")
            }
          }
      }

  private def emptyInputStream: InputStream =
    new ByteArrayInputStream(Array.empty)

  private def decodeInputStream(in: InputStream, encoding: Seq[TransferCoding]): InputStream =
    encoding.foldRight(in) { (encoding, in) =>
      if (encoding.isChunked)
        new ChunkedInputStream(in)
      else if (encoding.isGzip)
        new GZIPInputStream(in)
      else if (encoding.isDeflate)
        new InflaterInputStream(in)
      else throw new HttpException(s"Unsupported transfer encoding: $encoding")
    }

  private def getContentLength(message: HttpMessage): Long =
    message.getContentLength.orElse(message.body.getLength).getOrElse(0)

  private def getContentEncoding(message: HttpMessage): String =
    message.contentEncoding.lastOption.map(_.name).getOrElse("identity")
}
