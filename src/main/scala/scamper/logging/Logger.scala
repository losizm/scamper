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

/** Defines logger. */
trait Logger:
  /**
   * Logs trace message.
   *
   * @param message log message
   */
  def trace(message: String): Unit

  /**
   * Logs formatted trace message.
   *
   * @param format message format
   * @param args message arguments
   */
  def trace(format: String, args: Any*): Unit

  /**
   * Logs trace message and stack trace of given cause.
   *
   * @param message log message
   * @param cause `Throwable` whose stack trace to log
   */
  def trace(message: String, cause: Throwable): Unit

  /**
   * Logs debug message.
   *
   * @param message log message
   */
  def debug(message: String): Unit

  /**
   * Logs formatted debug message.
   *
   * @param format message format
   * @param args message arguments
   */
  def debug(format: String, args: Any*): Unit

  /**
   * Logs debug message and stack trace of given cause.
   *
   * @param message log message
   * @param cause `Throwable` whose stack trace to log
   */
  def debug(message: String, cause: Throwable): Unit

  /**
   * Logs information message.
   *
   * @param message log message
   */
  def info(message: String): Unit

  /**
   * Logs formatted information message.
   *
   * @param format message format
   * @param args message arguments
   */
  def info(format: String, args: Any*): Unit

  /**
   * Logs information message and stack trace of given cause.
   *
   * @param message log message
   * @param cause `Throwable` whose stack trace to log
   */
  def info(message: String, cause: Throwable): Unit

  /**
   * Logs warning message.
   *
   * @param message log message
   */
  def warn(message: String): Unit

  /**
   * Logs formatted warning message.
   *
   * @param format message format
   * @param args message arguments
   */
  def warn(format: String, args: Any*): Unit

  /**
   * Logs warning message and stack trace of given cause.
   *
   * @param message log message
   * @param cause `Throwable` whose stack trace to log
   */
  def warn(message: String, cause: Throwable): Unit

  /**
   * Logs error message.
   *
   * @param message log message
   */
  def error(message: String): Unit

  /**
   * Logs formatted error message.
   *
   * @param format message format
   * @param args message arguments
   */
  def error(format: String, args: Any*): Unit

  /**
   * Logs error message and stack trace of given cause.
   *
   * @param message log message
   * @param cause `Throwable` whose stack trace to log
   */
  def error(message: String, cause: Throwable): Unit
