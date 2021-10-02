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
package scamper.server

import scamper.HttpRequest
import scamper.ResponseStatus.Registry.Ok
import scamper.cookies.*

object CookieApplication extends RoutingApplication:
  def apply(router: Router): Unit =
    router.get("/foo/bar/baz/*") { implicit req =>
      Ok()
        .putCookies(getCookies(req, "foo", router.toAbsolutePath("/foo")))
        .putCookies(getCookies(req, "bar", router.toAbsolutePath("/foo/bar")))
        .putCookies(getCookies(req, "baz", router.toAbsolutePath("/foo/bar/baz")))
    }

    router.get("/foo/bar/*") { implicit req =>
      Ok()
        .putCookies(getCookies(req, "foo", router.toAbsolutePath("/foo")))
        .putCookies(getCookies(req, "bar", router.toAbsolutePath("/foo/bar")))
    }

    router.get("/foo/*") { implicit req =>
      Ok()
        .putCookies(getCookies(req, "foo", router.toAbsolutePath("/foo")))
    }

  private def getCookies(req: HttpRequest, name: String, path: String): Seq[SetCookie] =
    val public = req.getCookieValue(s"${name}_public").isEmpty
    val secure = req.getCookieValue(s"${name}_secure").isEmpty && req.server.isSecure

    (if public then Seq(SetCookie(s"${name}_public", s"${name}_public_value", path = Some(path), secure = false)) else Nil) ++
    (if secure then Seq(SetCookie(s"${name}_secure", s"${name}_secure_value", path = Some(path), secure = true )) else Nil)
