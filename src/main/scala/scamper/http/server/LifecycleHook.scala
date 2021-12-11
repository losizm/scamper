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
 * Defines lifecycle hook.
 *
 * @see [[CriticalService]]
 */
@FunctionalInterface
trait LifecycleHook:
  /**
   * Test whether hook is a critical service.
   *
   * @see [[CriticalService]]
   */
  final def isCriticalService: Boolean =
    isInstanceOf[CriticalService]

  /**
   * Processes lifecycle event.
   *
   * @param event lifecycle event
   */
  def process(event: LifecycleEvent): Unit

/**
 * Tags lifecycle hook as a critical service.
 *
 * Server creation is halted if a critical service fails to process the start
 * event.
 *
 * @see [[LifecycleEvent.Start]]
 */
trait CriticalService { this: LifecycleHook => }
