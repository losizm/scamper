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
package scamper.client

import java.net.Socket
import java.util.concurrent.atomic.{ AtomicBoolean, AtomicReference }

import scala.language.implicitConversions

import scamper.*
import scamper.Implicits.given
import scamper.headers.*
import scamper.server.TestServer
import scamper.types.{ *, given }

import RequestMethod.Registry.*
import ResponseStatus.Registry.*

class HttpClientGeneralSpec extends org.scalatest.flatspec.AnyFlatSpec with TestServer:
  private val requestLineCheck            = AtomicBoolean()
  private val hostHeaderCheck             = AtomicBoolean()
  private val userAgentHeaderCheck        = AtomicBoolean()
  private val contentTypeHeaderCheck      = AtomicBoolean()
  private val contentEncodingHeaderCheck  = AtomicBoolean()
  private val transferEncodingHeaderCheck = AtomicBoolean()
  private val acceptHeaderCheck           = AtomicBoolean()
  private val acceptEncodingHeaderCheck   = AtomicBoolean()
  private val connectionHeaderCheck       = AtomicBoolean()
  private val requestTestHeaderCheck      = AtomicBoolean()
  private val absoluteTarget              = AtomicReference[Uri]()
  private val correlate                   = AtomicReference[String]()
  private val requestClient               = AtomicReference[HttpClient]()
  private val socket                      = AtomicReference[Socket]()

  it should "validate request and response" in testClient(false)

  it should "validate request and response with SSL/TLS" in testClient(true)

  private def testClient(secure: Boolean) = withServer(secure) { implicit server =>
    given bodyParser: BodyParser[String] = BodyParser.text(8192)
    given client: HttpClient = HttpClient
      .settings()
      .trust(Resources.truststore)
      .accept("text/html; q=0.9", "text/plain; q=0.1")
      .acceptEncoding("deflate", "gzip")
      .continueTimeout(1000)
      .readTimeout(5000)
      .bufferSize(1024)
      .outgoing(_.addHeaders("Request-Test-1: test"))
      .outgoing(_.addHeaders("Request-Test-2: test"))
      .outgoing(_.putHeaders("Request-Test-2: update-1"))
      .outgoing(_.addHeaders("Request-Test-2: update-2"))
      .outgoing(checkRequest(_))
      .incoming(_.addHeaders("Response-Test-1: test"))
      .incoming(_.addHeaders("Response-Test-2: test"))
      .incoming(_.putHeaders("Response-Test-2: update-1"))
      .incoming(_.addHeaders("Response-Test-2: update-2"))
      .create()

    info("check client settings")
    assert(client.accept == Seq[MediaRange]("text/html; q=0.9", "text/plain; q=0.1"))
    assert(client.acceptEncoding == Seq[ContentCodingRange]("deflate", "gzip"))
    assert(client.continueTimeout == 1000)
    assert(client.readTimeout == 5000)
    assert(client.bufferSize == 1024)

    info("send request")
    Post(s"$serverUri/echo")
      .setTextBody("This is a test." * 100)
      .setGzipContentEncoding()
      .send { res =>
        info("check response status")
        assert(res.status == Ok)

        info("check response test headers")
        assert(res.getHeaderValue("Response-Test-1").contains("test"))
        assert(res.getHeaderValues("Response-Test-1") == Seq("test"))
        assert(res.getHeaderValue("Response-Test-2").contains("update-1"))
        assert(res.getHeaderValues("Response-Test-2") == Seq("update-1", "update-2"))
        assert(res.request.getHeaderValue("Request-Test-1").contains("test"))
        assert(res.request.getHeaderValues("Request-Test-1") == Seq("test"))
        assert(res.request.getHeaderValue("Request-Test-2").contains("update-1"))
        assert(res.request.getHeaderValues("Request-Test-2") == Seq("update-1", "update-2"))

        info("check response body")
        assert(res.as[String] == "This is a test." * 100)

        info("check correlate")
        assert(absoluteTarget.get == Uri(s"$serverUri/echo"))
        assert(res.absoluteTarget == Uri(s"$serverUri/echo"))

        info("check absolute target")
        assert(res.correlate == correlate.get)

        info("check client reference")
        assert(requestClient.get == client)
        assert(res.client == client)

        info("check socket reference")
        assert(res.socket == socket.get)
        assert(!res.socket.isClosed)
      }

    assert(requestLineCheck.get)
    assert(hostHeaderCheck.get)
    assert(userAgentHeaderCheck.get)
    assert(contentTypeHeaderCheck.get)
    assert(contentEncodingHeaderCheck.get)
    assert(transferEncodingHeaderCheck.get)
    assert(acceptHeaderCheck.get)
    assert(acceptEncodingHeaderCheck.get)
    assert(connectionHeaderCheck.get)
    assert(requestTestHeaderCheck.get)
    assert(socket.get.isClosed)
  }

  private def checkRequest(req: HttpRequest): HttpRequest =
    checkRequestLine(req)
    checkHostHeader(req)
    checkUserAgentHeader(req)
    checkContentTypeHeader(req)
    checkContentEncodingHeader(req)
    checkTransferEncodingHeader(req)
    checkAcceptHeader(req)
    checkAcceptEncodingHeader(req)
    checkConnectionHeader(req)
    checkRequestTestHeaders(req)
    checkRequestAttributes(req)
    req

  private def checkRequestLine(req: HttpRequest): Unit =
    info("check request line")
    requestLineCheck.set(
      req.isPost &&
      req.target == Uri("/echo") &&
      req.version == HttpVersion(1, 1)
    )

  private def checkHostHeader(req: HttpRequest): Unit =
    info("check Host header")
    hostHeaderCheck.set(req.host == req.absoluteTarget.getAuthority)

  private def checkUserAgentHeader(req: HttpRequest): Unit =
    info("check User-Agent header")
    userAgentHeaderCheck.set(
      req.userAgent.head.name == "Scamper" &&
      req.userAgent.head.version.exists(_.matches("\\d+(\\.\\d+(\\.\\d+)?)?"))
    )

  private def checkContentTypeHeader(req: HttpRequest): Unit =
    info("check Content-Type header")
    contentTypeHeaderCheck.set(req.contentType == MediaType("text/plain; charset=UTF-8"))

  private def checkContentEncodingHeader(req: HttpRequest): Unit =
    info("check Content-Encoding header")
    contentEncodingHeaderCheck.set(req.contentEncoding == Seq(ContentCoding("gzip")))

  private def checkTransferEncodingHeader(req: HttpRequest): Unit =
    info("check Transfer-Encoding header")
    transferEncodingHeaderCheck.set(req.transferEncoding == Seq(TransferCoding("chunked")))

  private def checkAcceptHeader(req: HttpRequest): Unit =
    info("check Accept header")
    acceptHeaderCheck.set(req.accept == Seq[MediaRange]("text/html; q=0.9", "text/plain; q=0.1"))

  private def checkAcceptEncodingHeader(req: HttpRequest): Unit =
    info("check Accept-Encoding header")
    acceptEncodingHeaderCheck.set(req.acceptEncoding == Seq[ContentCodingRange]("deflate", "gzip"))

  private def checkConnectionHeader(req: HttpRequest): Unit =
    info("check Connection header")
    connectionHeaderCheck.set(req.connection == Seq("close"))

  private def checkRequestTestHeaders(req: HttpRequest): Unit =
    info("check request test headers")
    requestTestHeaderCheck.set(
      req.getHeaderValue("Request-Test-1").contains("test") &&
      req.getHeaderValues("Request-Test-1") == Seq("test") &&
      req.getHeaderValue("Request-Test-2").contains("update-1") &&
      req.getHeaderValues("Request-Test-2") == Seq("update-1", "update-2")
    )

  private def checkRequestAttributes(req: HttpRequest): Unit =
    info("check request attributes")
    absoluteTarget.set(req.absoluteTarget)
    correlate.set(req.correlate)
    requestClient.set(req.client)
    socket.set(req.socket)
