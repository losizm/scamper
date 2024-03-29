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
 * Indicates response was aborted.
 *
 * A `RequestHandler` throws `ResponseAborted` if no response should be sent
 * for the request.
 */
case class ResponseAborted(message: String) extends HttpException(message)

/** Indicates parameter is not found. */
case class ParameterNotFound(name: String) extends HttpException(name)

/** Indicates parameter cannot be converted. */
case class ParameterNotConvertible(name: String, value: String) extends HttpException(s"$name=$value")

private case class ReadError(status: ResponseStatus) extends HttpException(status.reasonPhrase)

private case class ReadAborted(reason: String) extends HttpException(s"Read aborted with $reason")
