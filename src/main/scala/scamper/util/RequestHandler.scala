package scamper.util

import scamper.{ HttpException, HttpRequest, HttpResponse }

/** Provides utility for handling HTTP request. */
trait RequestHandler {
  /**
   * Handles request.
   *
   * If handler satisfies request, then it returns a response. Otherwise, it
   * returns a request, which can be the original request or an alternate one.
   */
  def apply(request: HttpRequest): Either[HttpRequest, HttpResponse]
}

/** Indicates no response generated for specified request. */
case class RequestNotSatisfied(request: HttpRequest) extends HttpException

/** Provides utility for applying a chain of request handlers to a request. */
object RequestHandlerChain {
  /**
   * Sends request through chain of request handlers in search of response.
   *
   * The chain is broken upon first generated response. That is, the handlers
   * are invoked up to first generated response, and the remaining handlers are
   * bypassed.
   *
   * @throws RequestNotSatisfied if no response is generated
   */
  def getResponse(request: HttpRequest, handlers: Seq[RequestHandler]): HttpResponse = {
    val init: Either[HttpRequest, HttpResponse] = Left(request)

    handlers.foldLeft(init) { (prev, handler) =>
      prev.fold(req => handler(req), res => Right(res))
    } match {
      case Right(response) => response
      case Left(request) => throw RequestNotSatisfied(request)
    }
  }
}

