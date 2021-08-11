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

import java.security.InvalidParameterException

import scala.language.implicitConversions

import scamper.{ BodyParser, HttpRequest, HttpResponse }
import scamper.Implicits.{ *, given }
import scamper.RequestMethod.Registry.Get
import scamper.ResponseStatus.Registry.*

class ErrorHandlerSpec extends org.scalatest.flatspec.AnyFlatSpec:
  given BodyParser[String] = BodyParser.text()

  object InterruptedErrorHandler extends ErrorHandler:
    def apply(req: HttpRequest) =
      case _: InterruptedException => RequestTimeout(s"${req.method} ${req.target}")

  object IllegalArgumentErrorHandler extends ErrorHandler:
    def apply(req: HttpRequest) =
      case _: NumberFormatException    => LengthRequired(s"${req.method} ${req.target}")
      case _: IllegalArgumentException => BadRequest(s"${req.method} ${req.target}")

  object InvalidParameterErrorHandler extends ErrorHandler:
    def apply(req: HttpRequest) =
      case _: InvalidParameterException => ServiceUnavailable(s"${req.method} ${req.target}")

  object NotImplementedErrorHandler extends ErrorHandler:
    def apply(req: HttpRequest) =
      case _: NotImplementedError => NotImplemented(s"${req.method} ${req.target}")

  object DefaultErrorHandler extends ErrorHandler:
    def apply(req: HttpRequest) =
      case _ => InternalServerError(s"${req.method} ${req.target}")

  val req = Get("/test")

  it should "test error handler chain" in {
    val onError = ErrorHandler.coalesce(
      InterruptedErrorHandler,
      IllegalArgumentErrorHandler,
      InvalidParameterErrorHandler,
      NotImplementedErrorHandler,
      DefaultErrorHandler
    )

    assert(compare(onError(req)(InterruptedException()), RequestTimeout("GET /test")))
    assert(compare(onError(req)(IllegalArgumentException()), BadRequest("GET /test")))
    assert(compare(onError(req)(NumberFormatException()), LengthRequired("GET /test")))
    assert(compare(onError(req)(InvalidParameterException()), BadRequest("GET /test")))
    assert(compare(onError(req)(NotImplementedError()), NotImplemented("GET /test")))
    assert(compare(onError(req)(NoSuchFieldException()), InternalServerError("GET /test")))
    assert(compare(onError(req)(Exception()), InternalServerError("GET /test")))
    assert(compare(onError(req)(Error()), InternalServerError("GET /test")))
    assert(compare(onError(req)(Throwable()), InternalServerError("GET /test")))
  }

  it should "test error handler chain in different order" in {
    val onError = ErrorHandler.coalesce(
      InterruptedErrorHandler,
      InvalidParameterErrorHandler,
      IllegalArgumentErrorHandler,
      NotImplementedErrorHandler,
      DefaultErrorHandler
    )

    assert(compare(onError(req)(InterruptedException()), RequestTimeout("GET /test")))
    assert(compare(onError(req)(IllegalArgumentException()), BadRequest("GET /test")))
    assert(compare(onError(req)(NumberFormatException()), LengthRequired("GET /test")))
    assert(compare(onError(req)(InvalidParameterException()), ServiceUnavailable("GET /test")))
    assert(compare(onError(req)(NotImplementedError()), NotImplemented("GET /test")))
    assert(compare(onError(req)(NoSuchFieldException()), InternalServerError("GET /test")))
    assert(compare(onError(req)(Exception()), InternalServerError("GET /test")))
    assert(compare(onError(req)(Error()), InternalServerError("GET /test")))
    assert(compare(onError(req)(Throwable()), InternalServerError("GET /test")))
  }

  it should "test error handler chain in reverse order" in {
    val onError = InterruptedErrorHandler
      .after(IllegalArgumentErrorHandler)
      .after(InvalidParameterErrorHandler)
      .after(NotImplementedErrorHandler)
      .after(DefaultErrorHandler)

    assert(compare(onError(req)(InterruptedException()), InternalServerError("GET /test")))
    assert(compare(onError(req)(IllegalArgumentException()), InternalServerError("GET /test")))
    assert(compare(onError(req)(NumberFormatException()), InternalServerError("GET /test")))
    assert(compare(onError(req)(InvalidParameterException()), InternalServerError("GET /test")))
    assert(compare(onError(req)(NotImplementedError()), InternalServerError("GET /test")))
    assert(compare(onError(req)(NoSuchFieldException()), InternalServerError("GET /test")))
    assert(compare(onError(req)(Exception()), InternalServerError("GET /test")))
    assert(compare(onError(req)(Error()), InternalServerError("GET /test")))
    assert(compare(onError(req)(Throwable()), InternalServerError("GET /test")))
  }

  it should "test error handler chain with no default" in {
    val onError = InterruptedErrorHandler
      .after(IllegalArgumentErrorHandler)
      .after(InvalidParameterErrorHandler)
      .after(NotImplementedErrorHandler)

    assert(compare(onError(req)(InterruptedException()), RequestTimeout("GET /test")))
    assert(compare(onError(req)(IllegalArgumentException()), BadRequest("GET /test")))
    assert(compare(onError(req)(NumberFormatException()), LengthRequired("GET /test")))
    assert(compare(onError(req)(InvalidParameterException()), ServiceUnavailable("GET /test")))
    assert(compare(onError(req)(NotImplementedError()), NotImplemented("GET /test")))
    assertThrows[MatchError](onError(req)(NoSuchFieldException()))
    assertThrows[MatchError](onError(req)(Exception()))
    assertThrows[MatchError](onError(req)(Error()))
    assertThrows[MatchError](onError(req)(Throwable()))
  }

  private def compare(a: HttpResponse, b: HttpResponse) =
    a.status == b.status && a.as[String] == b.as[String]
