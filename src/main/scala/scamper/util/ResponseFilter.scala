package scamper.util

import scamper._

/** Provides utility for filtering HTTP response. */
trait ResponseFilter {
  /** Tests whether response matches filter condition. */
  def apply(response: HttpResponse): Boolean

  /**
   * Returns {@code Some(response)} if response matches filter condition,
   * and {@code None} otherwise.
   */
  def unapply(response: HttpResponse): Option[HttpResponse] =
    if (apply(response)) Some(response) else None
}

/** Provides status-based {@code ResponseFilter} implementations. */
object ResponseFilters {
  /**
   * Filters informational responses.
   *
   * See [[ResponseStatus.isInformational]].
   */
  val Informational: ResponseFilter = (res => res.status.isInformational)

  /**
   * Filters successful responses.
   *
   * See [[ResponseStatus.isSuccessful]].
   */
  val Successful: ResponseFilter = (res => res.status.isSuccessful)

  /**
   * Filters redirection responses.
   *
   * See [[ResponseStatus.isRedirection]].
   */
  val Redirection: ResponseFilter = (res => res.status.isRedirection)

  /**
   * Filters client error responses.
   *
   * See [[ResponseStatus.isClientError]].
   */
  val ClientError: ResponseFilter = (res => res.status.isClientError)

  /**
   * Filters server error responses.
   *
   * See [[ResponseStatus.isServerError]].
   */
  val ServerError: ResponseFilter = (res => res.status.isServerError)
}

