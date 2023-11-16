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

import java.io.*
import java.nio.file.Files

import scala.util.Try

private implicit class FileExtensions(file: File) extends AnyVal:
  def withOutputStream[T](f: OutputStream => T): T =
    val out = FileOutputStream(file)
    try f(out)
    finally Try(out.close())

  def withInputStream[T](f: InputStream => T): T =
    val in = FileInputStream(file)
    try f(in)
    finally Try(in.close())

  def getBytes(): Array[Byte] =
    Files.readAllBytes(file.toPath)

  def setBytes(bytes: Array[Byte]): File =
    Files.write(file.toPath, bytes)
    file
