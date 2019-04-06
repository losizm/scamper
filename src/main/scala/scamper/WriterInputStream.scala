package scamper

import java.io.{ InputStream, IOException, OutputStream, PipedInputStream, PipedOutputStream }

import scala.concurrent.{ ExecutionContext, Future }
import scala.util.{ Failure, Try }

/**
 * Converts writer function to input stream.
 *
 * A piped input stream is created, and its associated piped output stream is
 * passed to the writer. The writer function is run on a separate thread using a
 * supplied execution context.
 *
 * {{{
 * import scala.concurrent.ExecutionContext.Implicits.global
 * import scamper.WriterInputStream
 *
 * val in = new WriterInputStream(out => {
 *   val data = "Hello, world!"
 *   out.write(data.getBytes())
 * })
 *
 * try {
 *   val buffer = new Array[Byte](32)
 *   val length = in.read(buffer)
 *
 *   assert(new String(buffer, 0, length) == "Hello, world!")
 * } finally {
 *   in.close()
 * }
 * }}}
 *
 * @constructor Creates WriterInputStream using supplied writer and buffer size.
 *
 * @param writer output stream handler
 * @param bufferSize buffer size used by underlying input stream.
 * @param executor execution context
 */
private class WriterInputStream(writer: OutputStream => Unit, bufferSize: Int)(implicit executor: ExecutionContext) extends InputStream {
  /**
   * Creates WriterInputStream using supplied writer.
   *
   * @param writer output stream handler
   * @param executor execution context
   */
  def this(writer: OutputStream => Unit)(implicit executor: ExecutionContext) = this(writer, 8192)

  private val in = new PipedInputStream(bufferSize)
  private val out = new PipedOutputStream(in)
  private val result = Future(try writer(out) finally Try(out.close()))

  /**
   * Gets number of bytes available in input stream.
   *
   * @throws IOException if an I/O error occurs
   */
  override def available(): Int = {
    checkForError()
    in.available()
  }

  /**
   * Skips over `length` bytes from input stream.
   *
   * @param length number of bytes to skip
   *
   * @return actually number of bytes skipped
   *
   * @throws IOException if an I/O error occurs
   */
  override def skip(length: Long): Long = {
    checkForError()
    in.skip(length)
  }

  /**
   * Mark/reset is not supported.
   *
   * @throws IOException
   */
  override def reset(): Unit = throw new IOException("mark/reset not supported")

  /** Mark/reset is not supported. */
  override def mark(readLimit: Int): Unit = ()

  /**
   * Mark/reset is not supported.
   *
   * @return false
   */
  override def markSupported(): Boolean = false

  /**
   * Reads next byte from input stream.
   *
   * @return next byte or `-1` if end of stream
   *
   * @throws IOException if an I/O error occurs
   */
  override def read(): Int = {
    checkForError()
    in.read()
  }

  /**
   * Reads bytes from input stream into supplied buffer.
   *
   * @return number of bytes read or `-1` if end of stream
   *
   * @throws IOException if an I/O error occurs
   */
  override def read(buffer: Array[Byte]): Int = {
    checkForError()
    in.read(buffer)
  }

  /**
   * Reads bytes from input stream into supplied buffer starting at given
   * offset.
   *
   * @return number of bytes read or `-1` if end of stream
   *
   * @throws IOException if an I/O error occurs
   */
  override def read(buffer: Array[Byte], offset: Int, length: Int): Int = {
    checkForError()
    in.read(buffer, offset, length)
  }

  /** Closes input stream. */
  override def close(): Unit = {
    Try(out.close())
    Try(in.close())
  }

  private def checkForError(): Unit =
    result.value.collect {
      case Failure(cause) => throw new IOException("Writer exception", cause)
    }
}
