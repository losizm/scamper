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
package headers

import scamper.http.types.TransferCoding

/** Adds standardized access to Transfer-Encoding header. */
given toTransferEncoding[T <: HttpMessage]: Conversion[T, TransferEncoding[T]] = TransferEncoding(_)

/** Adds standardized access to Transfer-Encoding header. */
class TransferEncoding[T <: HttpMessage](message: T) extends AnyVal:
  /** Tests for Transfer-Encoding header. */
  def hasTransferEncoding: Boolean =
    message.hasHeader("Transfer-Encoding")

  /**
   * Gets Transfer-Encoding header values.
   *
   * @return header values or empty sequence if Transfer-Encoding is not present
   */
  def transferEncoding: Seq[TransferCoding] =
    transferEncodingOption.getOrElse(Nil)

  /** Gets Transfer-Encoding header values if present. */
  def transferEncodingOption: Option[Seq[TransferCoding]] =
    message.getHeaderValue("Transfer-Encoding")
      .map(ListParser.apply)
      .map(_.map(TransferCoding.parse))

  /** Creates new message with Transfer-Encoding header set to supplied values. */
  def setTransferEncoding(values: Seq[TransferCoding]): T =
    message.asInstanceOf[MessageBuilder[T]].putHeaders(Header("Transfer-Encoding", values.mkString(", ")))

  /** Creates new message with Transfer-Encoding header set to supplied values. */
  def setTransferEncoding(one: TransferCoding, more: TransferCoding*): T =
    setTransferEncoding(one +: more)

  /** Creates new message with Transfer-Encoding header removed. */
  def transferEncodingRemoved: T =
    message.asInstanceOf[MessageBuilder[T]].removeHeaders("Transfer-Encoding")
