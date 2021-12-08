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

import logging.NullLogger
import ResponseStatus.Registry.InternalServerError

import java.util.concurrent.atomic.AtomicInteger

class HttpServerManagedServiceSpec extends org.scalatest.flatspec.AnyFlatSpec:
  private val starts = AtomicInteger(0)
  private val stops  = AtomicInteger(0)

  private class TestService(val name: String, val error: Option[Exception]) extends ManagedService:
    var started = 0
    var stopped = 0

    def start(server: HttpServer) =
      error.foreach(cause => throw cause)
      started = starts.incrementAndGet()

    def stop() =
      stopped = stops.incrementAndGet()

  private class NoncriticalTestService(name: String, error: Option[Exception])
    extends TestService(name, error) with NoncriticalService

  private class TestRequestHandler(name: String, error: Option[Exception]) extends TestService(name, error) with RequestHandler: //
    def apply(req: HttpRequest): HttpMessage = req

  private class TestResponseFilter(name: String, error: Option[Exception]) extends TestService(name, error) with ResponseFilter: //
    def apply(res: HttpResponse): HttpResponse = res

  private class TestErrorHandler(name: String, error: Option[Exception]) extends TestService(name, error) with ErrorHandler: //
    def apply(req: HttpRequest): PartialFunction[Throwable, HttpResponse] =
      case _ => InternalServerError()

  it should "create server with managed services" in {
    val s1 = TestService("S1", None)
    val s2 = TestService("S2", None)
    val s3 = NoncriticalTestService("S3", None)
    val s4 = NoncriticalTestService("S4", None)
    val s5 = TestService("S5", None)

    reset()
    createServer(s1, s2, s3, s4, s5)

    assert(s1.started == 2)
    assert(s2.started == 3)
    assert(s3.started == 4)
    assert(s4.started == 5)
    assert(s5.started == 6)

    assert(s1.stopped == 7)
    assert(s2.stopped == 6)
    assert(s3.stopped == 5)
    assert(s4.stopped == 4)
    assert(s5.stopped == 3)
  }

  it should "create server with failed noncritical managed service" in {
    val s1 = TestService("S1", None)
    val s2 = TestService("S2", None)
    val s3 = NoncriticalTestService("S3", Some(UnsupportedOperationException()))
    val s4 = NoncriticalTestService("S4", None)
    val s5 = TestService("S5", None)

    reset()
    createServer(s1, s2, s3, s4, s5)

    assert(s1.started == 2)
    assert(s2.started == 3)
    assert(s3.started == 0)
    assert(s4.started == 4)
    assert(s5.started == 5)

    assert(s1.stopped == 7)
    assert(s2.stopped == 6)
    assert(s3.stopped == 1)
    assert(s4.stopped == 5)
    assert(s5.stopped == 4)
  }

  it should "not create server with failed critical managed service" in {
    val s1 = TestService("S1", None)
    val s2 = TestService("S2", Some(UnsupportedOperationException()))
    val s3 = NoncriticalTestService("S3", None)
    val s4 = NoncriticalTestService("S4", None)
    val s5 = TestService("S5", None)

    reset()
    assert {
      intercept[ServiceException](createServer(s1, s2, s3, s4, s5))
        .getCause()
        .isInstanceOf[UnsupportedOperationException]
    }

    assert(s1.started == 2)
    assert(s2.started == 0)
    assert(s3.started == 0)
    assert(s4.started == 0)
    assert(s5.started == 0)

    assert(s1.stopped == 7)
    assert(s2.stopped == 6)
    assert(s3.stopped == 5)
    assert(s4.stopped == 4)
    assert(s5.stopped == 3)
  }

  private def reset(): Unit =
    starts.set(0)
    stops.set(0)

  private def createServer(service: ManagedService, more: ManagedService*): Unit =
    ServerApplication()
      .logger(NullLogger)
      .incoming(new TestRequestHandler("S6", None))
      .manage(service +: more)
      .outgoing(new TestResponseFilter("S7", None))
      .recover(new TestErrorHandler("S8", None))
      .create("localhost", 0)
      .close()
