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
 * Defines managed service.
 *
 * A managed service is started when the server is created; it is stopped when
 * the server is shut down.
 */
trait ManagedService:
  /** Gets service name. */
  def name: String

  /**
   * Starts service.
   *
   * @param server associated server
   */
  def start(server: HttpServer): Unit

  /**
   * Stops service.
   *
   * @note A service's `stop()` method may be invoked without prior invocation
   * of `start()`.
   */
  def stop(): Unit

/**
 * Defines noncritical service.
 *
 * If a noncritical service fails during startup, it does not halt server
 * creation.
 */
trait NoncriticalService extends ManagedService
