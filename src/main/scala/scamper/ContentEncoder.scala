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

import scala.concurrent.ExecutionContext
import scala.language.implicitConversions

private object ContentEncoder:
  private val `Content-Encoding: gzip` = Header("Content-Encoding", "gzip")
  private val `Content-Encoding: deflate` = Header("Content-Encoding", "deflate")

  def gzip[T <: HttpMessage with MessageBuilder[T]](msg: T, bufferSize: Int = 8192)(using ec: ExecutionContext): T =
    msg.getHeaderValue("Content-Encoding")
      .map { enc => Header("Content-Encoding", enc + ", gzip") }
      .map(msg.putHeaders(_))
      .getOrElse { msg.putHeaders(`Content-Encoding: gzip`) }
      .removeHeaders("Content-Length")
      .setBody { Compressor.gzip(msg.body.data, bufferSize) }

  def deflate[T <: HttpMessage with MessageBuilder[T]](msg: T, bufferSize: Int = 8192): T =
    msg.getHeaderValue("Content-Encoding")
      .map { enc => Header("Content-Encoding", enc + ", deflate") }
      .map(msg.putHeaders(_))
      .getOrElse { msg.putHeaders(`Content-Encoding: deflate`) }
      .removeHeaders("Content-Length")
      .setBody { Compressor.deflate(msg.body.data, bufferSize) }
