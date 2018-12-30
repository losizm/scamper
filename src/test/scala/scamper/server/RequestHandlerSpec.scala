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

import org.scalatest.FlatSpec

import scamper.ImplicitConverters.stringToUri
import scamper.RequestMethods.GET
import scamper.ResponseStatuses.Ok
import scamper.headers.ContentLength

import Implicits.ServerHttpRequestType

class RequestHandlerSpec extends FlatSpec {
  "RequestHandler" should "be composed with another" in {
    val filter: RequestFilter = _.withContentLength(9)
    val processor: RequestProcessor = req => Ok().withContentLength(req.getContentLength.getOrElse(0))
    val req = GET("/")

    assert {
      filter.compose(processor).apply(req).exists(_.contentLength == 0)
    }

    assert {
      filter.orElse(processor).apply(req).exists(_.contentLength == 9)
    }

    assert {
      processor.compose(filter).apply(req).exists(_.contentLength == 9)
    }

    assert {
      processor.orElse(filter).apply(req).exists(_.contentLength == 0)
    }
  }
}
