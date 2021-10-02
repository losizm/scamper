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
