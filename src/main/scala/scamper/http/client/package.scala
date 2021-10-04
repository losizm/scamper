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
package client

/** Indicates request is aborted. */
case class RequestAborted(message: String) extends HttpException(message)

/** Defines filter for outgoing request. */
@FunctionalInterface
trait RequestFilter:
  /** Filters outgoing request. */
  def apply(req: HttpRequest): HttpRequest

/** Defines handler for incoming response. */
@FunctionalInterface
trait ResponseHandler[T]:
  /** Handles response. */
  def apply(res: HttpResponse): T

/** Defines filter for incoming response. */
@FunctionalInterface
trait ResponseFilter extends ResponseHandler[HttpResponse]
