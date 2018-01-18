package scamper

/** Filters responses conditionally. */
trait ResponseFilter {
  /** Tests whether response matches filter condition. */
  def apply(response: HttpResponse): Boolean

  /**
   * Returns Some(response) if response matches filter condition, None
   * otherwise.
   */
  def unapply(response: HttpResponse): Option[HttpResponse]
}

/** Provides a set of filters that are based on response status type. */
object ResponseFilter {
  /**
   * Filters informational responses.
   *
   * See [[Status.isInformational]].
   */
  object Informational extends ResponseFilter {
    def apply(response: HttpResponse): Boolean =
      response.status.isInformational

    def unapply(response: HttpResponse): Option[HttpResponse] =
      if (apply(response)) Some(response) else None
  }

  /**
   * Filters successful responses.
   *
   * See [[Status.isSuccessful]].
   */
  object Successful extends ResponseFilter {
    def apply(response: HttpResponse): Boolean =
      response.status.isSuccessful

    def unapply(response: HttpResponse): Option[HttpResponse] =
      if (apply(response)) Some(response) else None
  }

  /**
   * Filters redirection responses.
   *
   * See [[Status.isRedirection]].
   */
  object Redirection extends ResponseFilter {
    def apply(response: HttpResponse): Boolean =
      response.status.isRedirection

    def unapply(response: HttpResponse): Option[HttpResponse] =
      if (apply(response)) Some(response) else None
  }

  /**
   * Filters client error responses.
   *
   * See [[Status.isClientError]].
   */
  object ClientError extends ResponseFilter {
    def apply(response: HttpResponse): Boolean =
      response.status.isClientError

    def unapply(response: HttpResponse): Option[HttpResponse] =
      if (apply(response)) Some(response) else None
  }

  /**
   * Filters server error responses.
   *
   * See [[Status.isServerError]].
   */
  object ServerError extends ResponseFilter {
    def apply(response: HttpResponse): Boolean =
      response.status.isServerError

    def unapply(response: HttpResponse): Option[HttpResponse] =
      if (apply(response)) Some(response) else None
  }
}

