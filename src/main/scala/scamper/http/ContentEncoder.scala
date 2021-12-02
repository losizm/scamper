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

import scala.concurrent.ExecutionContext

import scamper.http.headers.{ ContentEncoding, ContentLength, TransferEncoding }
import scamper.http.types.{ ContentCoding, TransferCoding }

private object ContentEncoder:
  private val gzip    = ContentCoding("gzip")
  private val deflate = ContentCoding("deflate")
  private val chunked = TransferCoding("chunked")

  def gzip[T <: HttpMessage & MessageBuilder[T]](msg: T, bufferSize: Int = 8192)(using executor: ExecutionContext): T =
    addContentEncoding(msg, gzip)
      .setBody(Entity(Compressor.gzip(msg.body.data, bufferSize)))

  def deflate[T <: HttpMessage & MessageBuilder[T]](msg: T, bufferSize: Int = 8192): T =
    addContentEncoding(msg, deflate)
      .setBody(Entity(Compressor.deflate(msg.body.data, bufferSize)))

  private def addContentEncoding[T <: HttpMessage & MessageBuilder[T]](msg: T, value: ContentCoding): T =
    msg.getContentEncoding
      .map(_ :+ value)
      .map(msg.setContentEncoding)
      .getOrElse(msg.setContentEncoding(value))
      .setTransferEncoding(getTransferEncoding(msg))
      .removeContentLength

  private def getTransferEncoding(msg: HttpMessage): Seq[TransferCoding] =
    msg.transferEncoding.filterNot(_.isChunked) :+ chunked
