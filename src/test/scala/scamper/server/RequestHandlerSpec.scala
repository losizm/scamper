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

import scamper.Implicits.{ tupleToHeader, stringToUri }
import scamper.{ HttpRequest, HttpResponse }
import scamper.RequestMethod.Registry.GET
import scamper.ResponseStatus.Registry.Ok

class RequestHandlerSpec extends org.scalatest.flatspec.AnyFlatSpec {
  "RequestHandler" should "be composed with another" in {
    val req = GET("/")
    val filter: RequestHandler = _.withHeader("foo" -> "bar")
    val processor: RequestHandler = req => Ok().withHeader(req.getHeader("foo").getOrElse("foo" -> "baz"))

    assert { filter.compose(processor)(req).asInstanceOf[HttpResponse].getHeaderValue("foo").contains("baz") }
    assert { filter.orElse(processor)(req).asInstanceOf[HttpResponse].getHeaderValue("foo").contains("bar") }

    assert { processor.compose(filter)(req).asInstanceOf[HttpResponse].getHeaderValue("foo").contains("bar") }
    assert { processor.orElse(filter)(req).asInstanceOf[HttpResponse].getHeaderValue("foo").contains("baz") }
  }
}
