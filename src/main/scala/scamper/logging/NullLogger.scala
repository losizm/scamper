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
package scamper.logging

/**
 * Provides logger to nothing.
 *
 * @note Each logger method is a noop.
 */
object NullLogger extends Logger:
  def trace(message: String): Unit = ()
  def trace(format: String, args: Any*): Unit = ()
  def trace(message: String, cause: Throwable): Unit = ()

  def debug(message: String): Unit = ()
  def debug(format: String, args: Any*): Unit = ()
  def debug(message: String, cause: Throwable): Unit = ()

  def info(message: String): Unit = ()
  def info(format: String, args: Any*): Unit = ()
  def info(message: String, cause: Throwable): Unit = ()

  def warn(message: String): Unit = ()
  def warn(format: String, args: Any*): Unit = ()
  def warn(message: String, cause: Throwable): Unit = ()

  def error(message: String): Unit = ()
  def error(format: String, args: Any*): Unit = ()
  def error(message: String, cause: Throwable): Unit = ()
