package scamper

/** Registered request methods */
object RequestMethods {
  /** GET request method */
  val GET: RequestMethod = RequestMethodImpl("GET")

  /** HEAD request method */
  val HEAD: RequestMethod = RequestMethodImpl("HEAD")

  /** POST request method */
  val POST: RequestMethod = RequestMethodImpl("POST")

  /** PUT request method */
  val PUT: RequestMethod = RequestMethodImpl("PUT")

  /** DELETE request method */
  val DELETE: RequestMethod = RequestMethodImpl("DELETE")

  /** OPTIONS request method */
  val OPTIONS: RequestMethod = RequestMethodImpl("OPTIONS")

  /** TRACE request method */
  val TRACE: RequestMethod = RequestMethodImpl("TRACE")

  /** CONNECT request method */
  val CONNECT: RequestMethod = RequestMethodImpl("CONNECT")
}

