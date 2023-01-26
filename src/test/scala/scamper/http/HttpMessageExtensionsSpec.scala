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

import java.io.File

import scala.language.implicitConversions

import RequestMethod.Registry.{ Post, Put }
import ResponseStatus.Registry.{ Created, Ok }

class HttpMessageExtensionsSpec extends org.scalatest.flatspec.AnyFlatSpec:
  it should "create with octet stream body" in {
    val text  = "Hello, world!"
    val utf8  = text.getBytes("UTF-8")
    val utf16 = text.getBytes("UTF-16")

    val req = Post("/messages").setOctetBody(utf8)
    assert(req.getHeaderValue("Content-Type").contains("application/octet-stream"))
    assert(req.getHeaderValue("Content-Length").contains(utf8.length.toString))

    val res = Created().setOctetBody(utf16)
    assert(res.getHeaderValue("Content-Type").contains("application/octet-stream"))
    assert(res.getHeaderValue("Content-Length").contains(utf16.length.toString))
  }

  it should "create with plain body" in {
    val text  = "Hello, world!"
    val utf8  = text.getBytes("UTF-8")
    val utf16 = text.getBytes("UTF-16")

    val req = Post("/messages").setPlainBody(text)
    assert(req.getHeaderValue("Content-Type").contains("text/plain; charset=UTF-8"))
    assert(req.getHeaderValue("Content-Length").contains(utf8.length.toString))

    val res = Created().setPlainBody(text, "UTF-16")
    assert(res.getHeaderValue("Content-Type").contains("text/plain; charset=UTF-16"))
    assert(res.getHeaderValue("Content-Length").contains(utf16.length.toString))
  }

  it should "create with file body" in {
    val file = File("src/test/resources/test.html")

    val req = Put("/messages").setFileBody(file)
    try
      assert(req.getHeaderValue("Content-Type").contains("text/html"))
      assert(req.getHeaderValue("Content-Length").contains(file.length.toString))
    finally
      req.body.data.close()
  

    val res = Ok().setFileBody(file)
    try
      assert(res.getHeaderValue("Content-Type").contains("text/html"))
      assert(res.getHeaderValue("Content-Length").contains(file.length.toString))
    finally
      res.body.data.close()
  }

  it should "create with form body" in {
    val query = QueryString("userId=1000&userName=lupita")

    val req = Post("/messages").setFormBody(query)
    try
      assert(req.getHeaderValue("Content-Type").contains("application/x-www-form-urlencoded"))
      assert(req.getHeaderValue("Content-Length").contains(query.toString.length.toString))
    finally
      req.body.data.close()
  
    val res = Ok().setFormBody("userId" -> "1000", "userName" -> "lupita")
    try
      assert(res.getHeaderValue("Content-Type").contains("application/x-www-form-urlencoded"))
      assert(res.getHeaderValue("Content-Length").contains(query.toString.length.toString))
    finally
      res.body.data.close()
  }
