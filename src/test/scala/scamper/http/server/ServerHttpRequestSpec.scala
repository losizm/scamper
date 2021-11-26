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

import scala.language.implicitConversions

import scamper.http.types.MediaType

import RequestMethod.Registry.Get

class ServerHttpRequestSpec extends org.scalatest.flatspec.AnyFlatSpec:
  it should "find accepted media type" in {
    val types = Seq(
      MediaType("text/html"),
      MediaType("application/json"),
      MediaType("application/xml"),
      MediaType("text/plain"),
    )

    val req1 = Get("/")
    assert(req1.findAccepted(types).contains(MediaType("text/html")))

    val req2 = Get("/").setHeaders("Accept: */*")
    assert(req2.findAccepted(types).contains(MediaType("text/html")))

    val req3 = Get("/").setHeaders("Accept: application/json; q=0.3, application/xml; q=0.5, */*; q=0.2")
    assert(req3.findAccepted(types).contains(MediaType("application/xml")))

    val req4 = Get("/").setHeaders("Accept: application/json; q=0.3, application/xml; q=0.5, */*")
    assert(req4.findAccepted(types).contains(MediaType("text/html")))

    val req5 = Get("/").setHeaders("Accept: application/json; q=0.8, */*; q=0.2")
    assert(req5.findAccepted(types).contains(MediaType("application/json")))

    val req6 = Get("/").setHeaders("Accept: image/jpeg")
    assert(req6.findAccepted(types).isEmpty)
  }
