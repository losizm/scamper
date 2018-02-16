package scamper

/** Handles HTTP request. */
trait RequestHandler {
  /**
   * Handles incoming request.
   *
   * If handler fulfills request, then response is returned. Otherwise, handler
   * passes request, as is or with modifications, to next handler in chain.
   */
  def apply(request: HttpRequest, next: RequestHandlerChain): HttpResponse
}

/** A chain of request handlers */
class RequestHandlerChain private (handler: RequestHandler, next: RequestHandlerChain) {
  /** Forwards request through request handler chain. */
  def apply(request: HttpRequest): HttpResponse =
    handler(request, next)
}

/** RequestHandlerChain factory */
object RequestHandlerChain {
  private val notFound = new RequestHandlerChain((_, _) => HttpResponses.NotFound, null)

  /** Creates request handler chain with supplied handlers. */
  def apply(handlers: RequestHandler*): RequestHandlerChain =
    handlers.foldRight(notFound) { (handler, next) => new RequestHandlerChain(handler, next) }
}

