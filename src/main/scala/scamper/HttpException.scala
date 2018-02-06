package scamper

/**
 * Indicates exception in HTTP processing.
 *
 * @constructor Constructs HttpException with supplied detail message and cause.
 *
 * @param message detail message
 * @param cause underlying cause
 */
class HttpException(message: String, cause: Throwable) extends RuntimeException(message, cause) {
  /** Constructs HttpException. */
  def this() = this(null, null)

  /**
   * Constructs HttpException with supplied detail message.
   *
   * @param message detail message
   */
  def this(message: String) = this(message, null)

  /**
   * Constructs HttpException with supplied cause.
   *
   * @param cause underlying cause
   */
  def this(cause: Throwable) = this(null, cause)
}

/** Indicates absence of specified header. */
case class HeaderNotFound(key: String) extends HttpException(key)

/**
 * Indicates exception in entity processing.
 *
 * @constructor Constructs EntityException with supplied detail message and cause.
 *
 * @param message detail message
 * @param cause underlying cause
 */
class EntityException(message: String, cause: Throwable) extends RuntimeException(message, cause) {
  /** Constructs EntityException. */
  def this() = this(null, null)

  /**
   * Constructs EntityException with supplied detail message.
   *
   * @param message detail message
   */
  def this(message: String) = this(message, null)

  /**
   * Constructs EntityException with supplied cause.
   *
   * @param cause underlying cause
   */
  def this(cause: Throwable) = this(null, cause)
}

/**
 * Indicates exception in chunk processing.
 *
 * @constructor Constructs ChunkException with supplied detail message and cause.
 *
 * @param message detail message
 * @param cause underlying cause
 */
class ChunkException(message: String, cause: Throwable) extends RuntimeException(message, cause) {
  /** Constructs ChunkException. */
  def this() = this(null, null)

  /**
   * Constructs ChunkException with supplied detail message.
   *
   * @param message detail message
   */
  def this(message: String) = this(message, null)

  /**
   * Constructs ChunkException with supplied cause.
   *
   * @param cause underlying cause
   */
  def this(cause: Throwable) = this(null, cause)
}

