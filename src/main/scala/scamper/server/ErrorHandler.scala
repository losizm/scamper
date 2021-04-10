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
package scamper.server

import scamper.{ HttpRequest, HttpResponse }

/** Defines utility for handling error during request processing. */
trait ErrorHandler {
  /**
   * Creates response for given error.
   *
   * @param error error generated during request processing
   * @param request request for which error was generated
   */
  def apply(error: Throwable, request: HttpRequest): HttpResponse
}
