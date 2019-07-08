/*
 * Copyright 2019 Carlos Conyers
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

/**
 * Indicates exception in HTTP processing.
 *
 * @constructor Constructs HttpException with supplied detail message and cause.
 *
 * @param message detail message
 * @param cause underlying cause
 */
class HttpException(message: String, cause: Throwable) extends RuntimeException(message, cause) {
  /** Constructs HttpException. */
  def this() = this(null, null)

  /**
   * Constructs HttpException with supplied detail message.
   *
   * @param message detail message
   */
  def this(message: String) = this(message, null)

  /**
   * Constructs HttpException with supplied cause.
   *
   * @param cause underlying cause
   */
  def this(cause: Throwable) = this(null, cause)
}

/** Indicates absence of specified header. */
case class HeaderNotFound(name: String) extends HttpException(name)

/**
 * Indicates entity exceeds established limit.
 *
 * `EntityTooLarge` is a complement to `ReadLimitExceeded`. Whereas
 * `ReadLimitExceeded` applies to the raw bytes of an input stream,
 * `EntityTooLarge` pertains to the entity itself, potentially subjected to
 * decompression.
 *
 * @see [[ReadLimitExceeded]]
 */
case class EntityTooLarge(limit: Long) extends HttpException(s"Entity cannot exceed $limit byte(s)")

