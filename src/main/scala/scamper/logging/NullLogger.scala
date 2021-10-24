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
  /** Logs nothing. */
  def trace(message: String): Unit = ()

  /** Logs nothing. */
  def trace(format: String, args: Any*): Unit = ()

  /** Logs nothing. */
  def trace(message: String, cause: Throwable): Unit = ()

  /** Logs nothing. */
  def debug(message: String): Unit = ()

  /** Logs nothing. */
  def debug(format: String, args: Any*): Unit = ()

  /** Logs nothing. */
  def debug(message: String, cause: Throwable): Unit = ()

  /** Logs nothing. */
  def info(message: String): Unit = ()

  /** Logs nothing. */
  def info(format: String, args: Any*): Unit = ()

  /** Logs nothing. */
  def info(message: String, cause: Throwable): Unit = ()

  /** Logs nothing. */
  def warn(message: String): Unit = ()

  /** Logs nothing. */
  def warn(format: String, args: Any*): Unit = ()

  /** Logs nothing. */
  def warn(message: String, cause: Throwable): Unit = ()

  /** Logs nothing. */
  def error(message: String): Unit = ()

  /** Logs nothing. */
  def error(format: String, args: Any*): Unit = ()

  /** Logs nothing. */
  def error(message: String, cause: Throwable): Unit = ()
