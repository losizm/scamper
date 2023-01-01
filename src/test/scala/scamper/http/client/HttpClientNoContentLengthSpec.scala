/*
 * Copyright 2022 Carlos Conyers
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

import java.io.{ ByteArrayOutputStream, File }

class HttpClientNoContentLengthSpec extends org.scalatest.flatspec.AnyFlatSpec:
  private val data = File("README.md")

  it should "read response with no Content-Length header" in withServer { port =>
    val buffer = ByteArrayOutputStream()

    HttpClient().get(Uri(s"http://localhost:$port/")) { res =>
      info(res.startLine.toString)
      res.headers.foreach(header => info(header.toString))
      res.drain(buffer, 256 * 1024L)

      assert(res.isSuccessful)
      assert(!res.hasHeader("Content-Length"))
      assert(buffer.size == data.length)
    }
  }

  private def withServer[T](f: Int => T): T =
    val server = NoContentLengthServer(data)

    try
      server.open()
      f(server.port)
    finally
      server.close()
