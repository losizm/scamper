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

private object Validate:
  inline def notNull[T](value: T): T =
    if value == null then
      throw NullPointerException()
    value

  inline def notNull[T](value: T, message: => String): T =
    if value == null then
      throw NullPointerException(message)
    value

  inline def noNulls[T <: Seq[?]](values: T): T =
    if values == null then
      throw NullPointerException()
    if values.contains(null) then
      throw IllegalArgumentException()
    values

  inline def noNulls[T <: Seq[?]](values: T, message: => String): T =
    if values == null then
      throw NullPointerException()
    if values.contains(null) then
      throw IllegalArgumentException(message)
    values
