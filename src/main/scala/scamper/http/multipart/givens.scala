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
package multipart

import java.io.File

/** Converts `Tuple` to `Part` using string content. */
given tupleToStringPart: Conversion[(String, String), Part] with
  def apply(part: (String, String)) = Part(part._1, part._2)

/** Converts `Tuple` to `Part` using byte content. */
given tupleToByteArrayPart: Conversion[(String, Array[Byte]), Part] with
  def apply(part: (String, Array[Byte])) = Part(part._1, part._2)

/** Converts `Tuple` to `Part` using file content. */
given tupleToFilePart: Conversion[(String, File), Part] with
  def apply(part: (String, File)) = Part(part._1, part._2)
