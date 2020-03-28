/*
 * Copyright 2017-2020 Carlos Conyers
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

/** Provides access to server-side request parameters. */
trait RequestParameters {
  /**
   * Gets named parameter as `String`.
   *
   * @param name parameter name
   *
   * @throws ParameterNotFound if parameter does not exist
   */
  def getString(name: String): String

  /**
   * Gets named parameter as `Int`.
   *
   * @param name parameter name
   *
   * @throws ParameterNotFound if parameter does not exist
   * @throws ParameterNotConvertible if parameter cannot be converted
   */
  def getInt(name: String): Int

  /**
   * Gets named parameter as `Long`.
   *
   * @param name parameter name
   *
   * @throws ParameterNotFound if parameter does not exist
   * @throws ParameterNotConvertible if parameter cannot be converted
   */
  def getLong(name: String): Long
}
