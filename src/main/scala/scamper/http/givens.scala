/*
 * Copyright 2023 Carlos Conyers
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

import java.io.{ File, InputStream, OutputStream }
import java.time.Instant

/** Converts string to `Uri`. */
given stringToUri: Conversion[String, Uri] = Uri(_)

/** Converts string to [[Header]]. */
given stringToHeader: Conversion[String, Header] = Header(_)

/** Converts tuple to [[Header]] where tuple is name-value pair. */
given tupleToHeader: Conversion[(String, String), Header] = Header(_, _)

/** Converts tuple to [[Header]] where tuple is name-value pair. */
given tupleToHeaderWithLongValue: Conversion[(String, Long), Header] = Header(_, _)

/** Converts tuple to [[Header]] where tuple is name-value pair. */
given tupleToHeaderWithIntValue: Conversion[(String, Int), Header] = Header(_, _)

/** Converts tuple to [[Header]] where tuple is name-value pair. */
given tupleToHeaderWithDateValue: Conversion[(String, Instant), Header] = Header(_, _)

/** Converts byte array to [[Entity]]. */
given bytesToEntity: Conversion[Array[Byte], Entity] = Entity(_)

/** Converts string to [[Entity]]. */
given stringToEntity: Conversion[String, Entity] = Entity(_, "UTF-8")

/** Converts file to [[Entity]]. */
given fileToEntity: Conversion[File, Entity] = Entity(_)

/** Converts input stream to [[Entity]]. */
given inputStreamToEntity: Conversion[InputStream, Entity] = Entity(_)

/** Converts writer to [[Entity]]. */
given writerToEntity: Conversion[(OutputStream => Unit), Entity] = Entity(_)

/** Converts string to [[RequestMethod]]. */
given stringToRequestMethod: Conversion[String, RequestMethod] = RequestMethod(_)

/** Converts int to [[ResponseStatus]]. */
given intToResponseStatus: Conversion[Int, ResponseStatus] = ResponseStatus(_)
