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
package websocket

/** Defines state of WebSocket session. */
enum SessionState:
  /**
   * Session is pending.
   *
   * In this state, a session does not read incoming messages. However, it can
   * send messages.
   */
  case Pending extends SessionState

  /**
   * Session is open.
   *
   * In this state, a session reads incoming messages. It can also send
   * messages.
   */
  case Open extends SessionState

  /**
   * Session is closed.
   *
   * In this state, a session can neither receive nor send messages.
   */
  case Closed extends SessionState
