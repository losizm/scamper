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

private inline def notNull[T](value: T): T =
  if value == null then
    throw NullPointerException()
  value

private inline def notNull[T](value: T, message: => String): T =
  if value == null then
    throw NullPointerException(message)
  value

private inline def noNulls[T <: Option[?]](value: T): T =
  if value == null || value.contains(null) then
    throw NullPointerException()
  value

private inline def noNulls[T <: Option[?]](value: T, message: => String): T =
  if value == null || value.contains(null) then
    throw NullPointerException(message)
  value

private inline def noNulls[T <: Seq[?]](values: T): T =
  if values == null || values.contains(null) then
    throw NullPointerException()
  values

private inline def noNulls[T <: Seq[?]](values: T, message: => String): T =
  if values == null || values.contains(null) then
    throw NullPointerException(message)
  values
