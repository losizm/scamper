/*
 * Copyright 2020 Carlos Conyers
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
package scamper.websocket

import scamper.logging.Logger

private class SessionLogger(id: String, logger: Logger) extends Logger {
  def trace(message: String): Unit =
    logger.trace(s"@WebSocket($id) $message")

  def trace(format: String, args: Any*): Unit =
    logger.trace(s"@WebSocket($id) $format", args : _*)

  def trace(message: String, cause: Throwable): Unit =
    logger.trace(s"@WebSocket($id) $message", cause)

  def debug(message: String): Unit =
    logger.debug(s"@WebSocket($id) $message")

  def debug(format: String, args: Any*): Unit =
    logger.debug(s"@WebSocket($id) $format", args : _*)

  def debug(message: String, cause: Throwable): Unit =
    logger.debug(s"@WebSocket($id) $message", cause)

  def info(message: String): Unit =
    logger.info(s"@WebSocket($id) $message")

  def info(format: String, args: Any*): Unit =
    logger.info(s"@WebSocket($id) $format", args : _*)

  def info(message: String, cause: Throwable): Unit =
    logger.info(s"@WebSocket($id) $message", cause)

  def warn(message: String): Unit =
    logger.warn(s"@WebSocket($id) $message")

  def warn(format: String, args: Any*): Unit =
    logger.warn(s"@WebSocket($id) $format", args : _*)

  def warn(message: String, cause: Throwable): Unit =
    logger.warn(s"@WebSocket($id) $message", cause)

  def error(message: String): Unit =
    logger.error(s"@WebSocket($id) $message")

  def error(format: String, args: Any*): Unit =
    logger.error(s"@WebSocket($id) $format", args : _*)

  def error(message: String, cause: Throwable): Unit =
    logger.error(s"@WebSocket($id) $message", cause)
}
