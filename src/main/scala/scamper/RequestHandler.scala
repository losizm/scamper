package scamper

/** Handles HTTP requests. */
trait RequestHandler {
  /**
   * Handles the incoming request.
   *
   * If the handler fulfills the request, then a response is returned.
   * Otherwise, the handler passes the request, as is or with modifications, to
   * the next handler in the chain.
   */
  def apply(request: HttpRequest, next: RequestHandlerChain): HttpResponse
}

/** Represents a chain of request handlers. */
class RequestHandlerChain private (handler: RequestHandler, next: RequestHandlerChain) {
  /** Forwards the request through the request handler chain. */
  def apply(request: HttpRequest): HttpResponse =
    handler(request, next)
}

/** RequestHandlerChain factory */
object RequestHandlerChain {
  private val notFound = new RequestHandlerChain((_, _) => HttpResponse(Status.NotFound), null)

  /** Creates a request handler chain with supplied handlers. */
  def apply(handlers: RequestHandler*): RequestHandlerChain =
    handlers.foldRight(notFound) { (handler, next) => new RequestHandlerChain(handler, next) }
}

