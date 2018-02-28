package scamper

/** Handles HTTP request. */
trait RequestHandler {
  /**
   * Handles request.
   *
   * If handler fulfills request, then response is returned. Otherwise, handler
   * passes request, as is or with modifications, to next handler in chain.
   */
  def apply(request: HttpRequest, next: RequestHandlerChain): HttpResponse
}

/** A chain of request handlers */
class RequestHandlerChain private (handler: RequestHandler, next: RequestHandlerChain) {
  /** Forwards request through chain. */
  def apply(request: HttpRequest): HttpResponse =
    handler(request, next)
}

/** RequestHandlerChain factory */
object RequestHandlerChain {
  private val chain = new RequestHandlerChain((req, _) => throw new HttpException(s"Unhandled request: ${req.startLine}"), null)

  /** Creates chain with supplied handlers. */
  def apply(handlers: RequestHandler*): RequestHandlerChain =
    handlers.foldRight(chain) { (handler, next) => new RequestHandlerChain(handler, next) }
}

