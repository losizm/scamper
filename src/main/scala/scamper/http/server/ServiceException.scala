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

/**
 * Indicates exception in managed service.
 *
 * @constructor Constructs exception with supplied message and cause.
 *
 * @param message detail message
 * @param cause underlying cause
 */
class ServiceException(message: String, cause: Throwable) extends RuntimeException(message, cause):
  /** Constructs exception. */
  def this() = this(null, null)

  /**
   * Constructs exception with supplied message.
   *
   * @param message detail message
   */
  def this(message: String) = this(message, null)

  /**
   * Constructs exception with supplied cause.
   *
   * @param cause underlying cause
   */
  def this(cause: Throwable) = this(null, cause)
