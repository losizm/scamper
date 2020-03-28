/*
 * Copyright 2017-2020 Carlos Conyers
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

/**
 * Defines state of WebSocket session.
 *
 * @see [[ReadyState$ ReadyState]]
 */
sealed trait ReadyState

/** Registry of `ReadyState`. */
object ReadyState {
  /** Session is pending. */
  case object Pending extends ReadyState

  /** Session is open. */
  case object Open extends ReadyState

  /** Session is closed. */
  case object Closed extends ReadyState
}
