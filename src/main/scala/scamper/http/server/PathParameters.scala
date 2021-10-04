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
package server

import scala.util.Try

/** Defines access to path parameters. */
trait PathParameters:
  /**
   * Gets parameter value as `String`.
   *
   * @param name parameter name
   *
   * @throws ParameterNotFound if parameter does not exist
   */
  def getString(name: String): String

  /**
   * Gets parameter value as `Int`.
   *
   * @param name parameter name
   *
   * @throws ParameterNotFound if parameter does not exist
   * @throws ParameterNotConvertible if parameter cannot be converted
   */
  def getInt(name: String): Int

  /**
   * Gets parameter value as `Long`.
   *
   * @param name parameter name
   *
   * @throws ParameterNotFound if parameter does not exist
   * @throws ParameterNotConvertible if parameter cannot be converted
   */
  def getLong(name: String): Long

private class MapPathParameters(params: Map[String, String]) extends PathParameters:
  def getString(name: String): String =
    params.getOrElse(name, throw ParameterNotFound(name))

  def getInt(name: String): Int =
    val value = getString(name)
    Try(value.toInt).getOrElse(throw ParameterNotConvertible(name, value))

  def getLong(name: String): Long =
    val value = getString(name)
    Try(value.toLong).getOrElse(throw ParameterNotConvertible(name, value))
