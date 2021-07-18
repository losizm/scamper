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

import java.time.Instant

/** Provides logger to console. */
object ConsoleLogger extends Logger:
  def trace(message: String): Unit =
    log("trace", message)

  def trace(format: String, args: Any*): Unit =
    log("trace", format.format(args*))

  def trace(message: String, cause: Throwable): Unit =
    log("trace", message, cause)

  def debug(message: String): Unit =
    log("debug", message)

  def debug(format: String, args: Any*): Unit =
    log("debug", format.format(args*))

  def debug(message: String, cause: Throwable): Unit =
    log("debug", message, cause)

  def info(message: String): Unit =
    log("info", message)

  def info(format: String, args: Any*): Unit =
    log("info", format.format(args*))

  def info(message: String, cause: Throwable): Unit =
    log("info", message, cause)

  def warn(message: String): Unit =
    log("warn", message)

  def warn(format: String, args: Any*): Unit =
    log("warn", format.format(args*))

  def warn(message: String, cause: Throwable): Unit =
    log("warn", message, cause)

  def error(message: String): Unit =
    log("error", message)

  def error(format: String, args: Any*): Unit =
    log("error", format.format(args*))

  def error(message: String, cause: Throwable): Unit =
    log("error", message, cause)

  private def log(level: String, message: String): Unit =
    Console.println(s"${Instant.now()} [$level] $message")

  private def log(level: String, message: String, cause: Throwable): Unit =
    log(level, message)
    cause.printStackTrace(Console.out)
