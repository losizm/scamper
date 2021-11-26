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

import Validate.notNull

private class MountRequestHandler private (path: MountPath, handler: RequestHandler) extends RequestHandler:
  def apply(req: HttpRequest): HttpMessage =
    path.matches(req.path) match
      case true  => handler(req)
      case false => req

private object MountRequestHandler:
  def apply(path: String, handler: RequestHandler): MountRequestHandler =
    new MountRequestHandler(MountPath(path), notNull(handler))