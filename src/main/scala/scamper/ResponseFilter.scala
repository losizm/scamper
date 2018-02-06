package scamper

/** Filters response conditionally. */
trait ResponseFilter {
  /** Tests whether response matches filter condition. */
  def apply(response: HttpResponse): Boolean

  /**
   * Returns Some(response) if response matches filter condition, None
   * otherwise.
   */
  def unapply(response: HttpResponse): Option[HttpResponse] =
    if (apply(response)) Some(response) else None
}

/** Provides status-based response filter implementations. */
object ResponseFilter {
  /**
   * Filters informational responses.
   *
   * See [[Status.isInformational]].
   */
  val Informational = ResponseFilter(_.status.isInformational)

  /**
   * Filters successful responses.
   *
   * See [[Status.isSuccessful]].
   */
  val Successful = ResponseFilter(_.status.isSuccessful)

  /**
   * Filters redirection responses.
   *
   * See [[Status.isRedirection]].
   */
  val Redirection = ResponseFilter(_.status.isRedirection)

  /**
   * Filters client error responses.
   *
   * See [[Status.isClientError]].
   */
  val ClientError = ResponseFilter(_.status.isClientError)

  /**
   * Filters server error responses.
   *
   * See [[Status.isServerError]].
   */
  val ServerError = ResponseFilter(_.status.isServerError)

  private def apply(predicate: HttpResponse => Boolean): ResponseFilter =
    new ResponseFilter { def apply(response: HttpResponse) = predicate(response) }
}

