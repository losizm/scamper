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

/** Defines lifecycle event. */
enum LifecycleEvent(server: HttpServer) extends java.util.EventObject(server):
  /**
   * Provided as notification when server starts.
   *
   * @param server server for which event is generated
   */
  case Start(server: HttpServer) extends LifecycleEvent(server)

  /**
   * Provided as notification when server stops.
   *
   * @param server server for which event is generated
   */
  case Stop(server: HttpServer) extends LifecycleEvent(server)
