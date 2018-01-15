package scamper

/**
 * Thrown when an error occurs during HTTP processing.
 *
 * @constructor Constructs a new HttpException with the specified detail message and cause.
 * 
 * @param message detail message
 * @param cause underlying cause
 */
class HttpException(message: String, cause: Throwable) extends RuntimeException(message, cause) {
  /** Constructs a new HttpException with no detail message. */
  def this() = this(null, null)

  /**
   * Constructs a new HttpException with the specified detail message.
   * 
   * @param message detail message
   */
  def this(message: String) = this(message, null)

  /**
   * Constructs a new HttpException with the specified cause.
   * 
   * @param cause underlying cause
   */
  def this(cause: Throwable) = this(null, cause)
}

