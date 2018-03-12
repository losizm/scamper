package scamper

/**
 * HTTP request method
 *
 * @see [[RequestMethods]]
 */
trait RequestMethod {
  /** Method name */
  def name: String

  /** Creates HttpRequest with supplied URI and headers. */
  def apply(uri: String, headers: Header*): HttpRequest =
    HttpRequest(this, uri, headers)

  /** Creates HttpRequest with supplied URI, entity, and headers. */
  def apply(uri: String, body: Entity, headers: Header*): HttpRequest =
    HttpRequest(this, uri, headers, body)
}

/**
 * RequestMethod factory
 *
 * @see [[RequestMethods]]
 */
object RequestMethod {
  import Grammar.Token
  import RequestMethods._

  /** Gets RequestMethod for given code. */
  def apply(name: String): RequestMethod =
    name match {
      case "GET"     => GET
      case "HEAD"    => HEAD
      case "POST"    => POST
      case "PUT"     => PUT
      case "DELETE"  => DELETE
      case "OPTIONS" => OPTIONS
      case "TRACE"   => TRACE
      case "CONNECT" => CONNECT
      case _  =>
        Token(name).map(RequestMethodImpl(_)).getOrElse {
          throw new IllegalArgumentException(s"Invalid request method name: $name")
        }
    }

  /** Destructures RequestMethod. */
  def unapply(method: RequestMethod): Option[String] =
    Some(method.name)
}

private case class RequestMethodImpl(name: String) extends RequestMethod

