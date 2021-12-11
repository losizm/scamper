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

class HttpServerLifecycleHookSpec extends org.scalatest.flatspec.AnyFlatSpec:
  private val starts = AtomicInteger(0)
  private val stops  = AtomicInteger(0)

  private class TestHook(error: Option[Exception]) extends LifecycleHook:
    var started = 0
    var stopped = 0

    def process(event: LifecycleEvent) =
      event match
        case LifecycleEvent.Start(server) =>
          error.foreach(throw(_))
          started = starts.incrementAndGet()
          assert(server.host.getHostName == "localhost")
          assert(!server.isClosed)

        case LifecycleEvent.Stop(server)  =>
          stopped = stops.incrementAndGet()
          assert(server.host.getHostName == "localhost")
          assert(server.isClosed)

  private class TestCriticalService(error: Option[Exception]) extends TestHook(error), CriticalService

  private class TestRequestHandler extends TestHook(None) with RequestHandler: //
    def apply(req: HttpRequest): HttpMessage = req

  private class TestResponseFilter extends TestHook(None) with ResponseFilter: //
    def apply(res: HttpResponse): HttpResponse = res

  private class TestErrorHandler extends TestHook(None) with ErrorHandler: //
    def apply(req: HttpRequest): PartialFunction[Throwable, HttpResponse] =
      case _ => InternalServerError()

  it should "create server with lifecycle hooks" in {
    val hook1 = TestCriticalService(None)
    val hook2 = TestCriticalService(None)
    val hook3 = TestHook(None)
    val hook4 = TestHook(None)
    val hook5 = TestCriticalService(None)

    assert(hook1.isCriticalService)
    assert(hook2.isCriticalService)
    assert(!hook3.isCriticalService)
    assert(!hook4.isCriticalService)
    assert(hook5.isCriticalService)

    reset()
    createServer(hook1, hook2, hook3, hook4, hook5)

    assert(hook1.started == 2)
    assert(hook2.started == 3)
    assert(hook3.started == 4)
    assert(hook4.started == 5)
    assert(hook5.started == 6)

    assert(hook1.stopped == 7)
    assert(hook2.stopped == 6)
    assert(hook3.stopped == 5)
    assert(hook4.stopped == 4)
    assert(hook5.stopped == 3)
  }

  it should "create server with failed lifecycle hook" in {
    val hook1 = TestCriticalService(None)
    val hook2 = TestCriticalService(None)
    val hook3 = TestHook(Some(UnsupportedOperationException()))
    val hook4 = TestHook(None)
    val hook5 = TestCriticalService(None)

    assert(hook1.isCriticalService)
    assert(hook2.isCriticalService)
    assert(!hook3.isCriticalService)
    assert(!hook4.isCriticalService)
    assert(hook5.isCriticalService)

    reset()
    createServer(hook1, hook2, hook3, hook4, hook5)

    assert(hook1.started == 2)
    assert(hook2.started == 3)
    assert(hook3.started == 0)
    assert(hook4.started == 4)
    assert(hook5.started == 5)

    assert(hook1.stopped == 7)
    assert(hook2.stopped == 6)
    assert(hook3.stopped == 5)
    assert(hook4.stopped == 4)
    assert(hook5.stopped == 3)
  }

  it should "not create server with failed critical service" in {
    val hook1 = TestCriticalService(None)
    val hook2 = TestCriticalService(Some(UnsupportedOperationException()))
    val hook3 = TestHook(None)
    val hook4 = TestHook(None)
    val hook5 = TestCriticalService(None)

    assert(hook1.isCriticalService)
    assert(hook2.isCriticalService)
    assert(!hook3.isCriticalService)
    assert(!hook4.isCriticalService)
    assert(hook5.isCriticalService)

    reset()
    assert {
      intercept[LifecycleException](createServer(hook1, hook2, hook3, hook4, hook5))
        .getCause()
        .isInstanceOf[UnsupportedOperationException]
    }

    assert(hook1.started == 2)
    assert(hook2.started == 0)
    assert(hook3.started == 0)
    assert(hook4.started == 0)
    assert(hook5.started == 0)

    assert(hook1.stopped == 7)
    assert(hook2.stopped == 6)
    assert(hook3.stopped == 5)
    assert(hook4.stopped == 4)
    assert(hook5.stopped == 3)
  }

  private def reset(): Unit =
    starts.set(0)
    stops.set(0)

  private def createServer(hook: LifecycleHook, more: LifecycleHook*): Unit =
    ServerApplication()
      .logger(NullLogger)
      .incoming(TestRequestHandler())
      .trigger(hook +: more)
      .outgoing(TestResponseFilter())
      .recover(TestErrorHandler())
      .create("localhost", 0)
      .close()

  extension (app: ServerApplication)
    def trigger(hooks: Seq[LifecycleHook]): app.type =
      hooks.foldLeft(app)(_ trigger _)
