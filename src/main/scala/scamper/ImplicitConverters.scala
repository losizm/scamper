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

import java.io.{ File, InputStream }
import java.net.URI
import java.time.Instant

/** Includes implicit converter functions. */
object ImplicitConverters {
  /** Converts string to {@code java.net.URI}. */
  implicit val stringToURI = (uri: String) => new URI(uri)

  /** Converts string to [[Header]]. */
  implicit val stringToHeader = (header: String) => Header.parse(header)

  /** Converts tuple to [[Header]] where tuple is name-value pair. */
  implicit val tupleToHeader = (header: (String, String)) => Header(header._1, header._2)

  /** Converts tuple to [[Header]] where tuple is name-value pair. */
  implicit val tupleToHeaderWithLongValue = (header: (String, Long)) => Header(header._1, header._2)

  /** Converts tuple to [[Header]] where tuple is name-value pair. */
  implicit val tupleToHeaderWithIntValue = (header: (String, Int)) => Header(header._1, header._2)

  /** Converts tuple to [[Header]] where tuple is name-value pair. */
  implicit val tupleToHeaderWithDateValue = (header: (String, Instant)) => Header(header._1, header._2)

  /** Converts byte array to [[Entity]]. */
  implicit val bytesToEntity = (entity: Array[Byte]) => Entity(entity)

  /** Converts string to [[Entity]] where text is UTF-8 encoded. */
  implicit val stringToEntity = (entity: String) => Entity(entity)

  /** Converts file to [[Entity]]. */
  implicit val fileToEntity = (entity: File) => Entity(entity)

  /** Converts input stream to [[Entity]]. */
  implicit val inputStreamToEntity = (entity: InputStream) => Entity(entity)

  /** Converts string to [[RequestMethod]]. */
  implicit val stringToRequestMethod = (method: String) => RequestMethod(method)

  /** Converts int to [[ResponseStatus]]. */
  implicit val stringToResponseStatus = (statusCode: Int) => ResponseStatus(statusCode)
}
