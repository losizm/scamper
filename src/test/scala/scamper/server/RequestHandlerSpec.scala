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

import scala.language.implicitConversions

import scamper.{ Header, HttpRequest, HttpResponse, stringToUri }
import scamper.RequestMethod.Registry.Get
import scamper.ResponseStatus.Registry.Ok

class RequestHandlerSpec extends org.scalatest.flatspec.AnyFlatSpec:
  it should "compose request handlers" in {
    val req = Get("/")
    val filter: RequestHandler = _.putHeaders(Header("foo", "bar"))
    val processor: RequestHandler = req => Ok().putHeaders(req.getHeader("foo").getOrElse(Header("foo", "baz")))

    assert { filter.after(processor)(req).asInstanceOf[HttpResponse].getHeaderValue("foo").contains("baz") }
    assert { filter.before(processor)(req).asInstanceOf[HttpResponse].getHeaderValue("foo").contains("bar") }

    assert { processor.after(filter)(req).asInstanceOf[HttpResponse].getHeaderValue("foo").contains("bar") }
    assert { processor.before(filter)(req).asInstanceOf[HttpResponse].getHeaderValue("foo").contains("baz") }
  }
