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

import java.io.{ InputStream, SequenceInputStream }
import java.util.zip.{ GZIPInputStream, InflaterInputStream }

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
    message.body.withInputStream { in =>
      val dechunked = isChunked(message) match {
        case true  => new ChunkedInputStream(in)
        case false => new BoundedInputStream(in, getContentLength(message))
      }

      getContentEncoding(message) match {
        case "gzip"     => f(new GZIPInputStream(dechunked))
        case "deflate"  => f(new InflaterInputStream(dechunked))
        case "identity" => f(dechunked)
        case encoding   => throw new HttpException(s"Unsupported content encoding: $encoding")
      }
    }

  private def isChunked(message: HttpMessage): Boolean =
    message.hasHeader("Transfer-Encoding")

  private def getContentLength(message: HttpMessage): Long =
    message.getHeaderValue("Content-Length")
      .map(_.toLong)
      .getOrElse(0)

  private def getContentEncoding(message: HttpMessage): String =
    message.getHeaderValue("Content-Encoding")
      .map(ListParser.apply)
      .flatMap(_.headOption)
      .map(_.toLowerCase)
      .getOrElse("identity")
}
