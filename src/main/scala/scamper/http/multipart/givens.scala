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

/** Converts tuple to [[TextPart]] where tuple is name-content pair. */
given tupleToTextPart: Conversion[(String, String), TextPart] with
  def apply(part: (String, String)) = TextPart(part._1, part._2)

/** Converts tuple to [[FilePart]] where tuple is name-content pair. */
given tupleToFilePart: Conversion[(String, File), FilePart] with
  def apply(part: (String, File)) = FilePart(part._1, part._2)
