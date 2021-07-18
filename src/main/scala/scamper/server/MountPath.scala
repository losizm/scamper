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
package scamper.server

import scala.util.matching.Regex

private class MountPath private (val value: String):
  private val regex = value match
    case "/" => "/.*"
    case _   => s"${Regex.quote(value)}(/.*)?"

  def matches(path: String): Boolean =
    path.matches(regex)

private object MountPath:
  def apply(value: String): MountPath =
    new MountPath(normalize(value))

  def normalize(value: String): String =
    val path = NormalizePath(value)

    if !path.matches("""/|(/[\w+\-.~%]+)+""") || path.matches("""/\.\.(/.*)?""") then
      throw IllegalArgumentException(s"Invalid mount path: $path")

    path
