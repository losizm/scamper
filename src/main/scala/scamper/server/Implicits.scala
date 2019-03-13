/*
 * Copyright 2018 Carlos Conyers
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

import java.net.Socket

import scala.util.Try

import scamper.{ HttpException, HttpMessage, HttpRequest, StatusLine }
import scamper.ResponseStatuses.Continue
import scamper.Auxiliary.SocketType
import scamper.headers.Expect

/** Includes server-related type classes. */
object Implicits {
  /** Adds server-side extension methods to `HttpMessage`. */
  implicit class ServerHttpMessageType(val msg: HttpMessage) extends AnyVal {
    /**
     * Gets message correlate.
     *
     * Each incoming request is assigned a tag (i.e., <em>correlate</em>), which is
     * later assigned to its outgoing response.
     */
    def correlate(): String = msg.getAttributeOrElse("scamper.server.message.correlate", "")

    /** Gets socket associated with message.  */
    def socket(): Socket = msg.getAttributeOrElse("scamper.server.message.socket", throw new HttpException("Socket not available"))
  }

  /** Adds server-side extension methods to `HttpRequest`. */
  implicit class ServerHttpRequestType(val req: HttpRequest) extends AnyVal {
    /** Gets request parameters. */
    def params(): RequestParameters =
      new TargetedRequestParameters(req.getAttributeOrElse("scamper.server.request.parameters", Map.empty[String, String]))

    /**
     * Send interim `100 Continue` response if request includes `Expect` header
     * with `100-Continue`.
     *
     * @return `true` if response was sent; `false` otherwise
     */
    def continue(): Boolean =
      req.getExpect
        .filter(_.toLowerCase == "100-continue")
        .flatMap(_ => Try(req.socket).toOption)
        .map { socket =>
          socket.writeLine(StatusLine(Continue).toString)
          socket.writeLine()
          socket.flush()
        }.isDefined
  }
}
