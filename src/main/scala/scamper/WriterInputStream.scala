/*
 * Copyright 2021 Carlos Conyers
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package scamper

import java.io.{ InputStream, IOException, OutputStream, PipedInputStream, PipedOutputStream }
import java.util.concurrent.atomic.AtomicReference

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
 * val in = WriterInputStream(out => {
 *   val data = "Hello, world!"
 *   out.write(data.getBytes())
 * })
 *
 * try
 *   val buffer = new Array[Byte](32)
 *   val length = in.read(buffer)
 *
 *   assert(String(buffer, 0, length) == "Hello, world!")
 * finally
 *   in.close()
 * }}}
 *
 * @constructor Creates WriterInputStream using supplied writer and buffer size.
 *
 * @param writer output stream handler
 * @param bufferSize buffer size used by underlying input stream.
 * @param executor execution context
 */
private class WriterInputStream(bufferSize: Int, writer: OutputStream => Unit)(using executor: ExecutionContext) extends InputStream:
  /**
   * Creates WriterInputStream using supplied writer.
   *
   * @param writer output stream handler
   * @param executor execution context
   */
  def this(writer: OutputStream => Unit)(using executor: ExecutionContext) = this(8192, writer)

  private val in    = PipedInputStream(bufferSize)
  private val out   = PipedOutputStream(in)
  private val error = AtomicReference[Throwable]()

  Future {
    try writer(out)
    catch case t: Throwable => error.set(t)
    finally Try(out.close())
  }

  /**
   * Gets number of bytes available in input stream.
   *
   * @throws IOException if I/O error occurs
   */
  override def available(): Int = propose { in.available() }

  /**
   * Skips over `length` bytes from input stream.
   *
   * @param length number of bytes to skip
   *
   * @return actually number of bytes skipped
   *
   * @throws IOException if I/O error occurs
   */
  override def skip(length: Long): Long = propose { in.skip(length) }

  /**
   * Mark/reset is not supported.
   *
   * @throws IOException always
   */
  override def reset(): Unit = throw IOException("mark/reset not supported")

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
   * @throws IOException if I/O error occurs
   */
  override def read(): Int = propose { in.read() }

  /**
   * Reads bytes from input stream into supplied buffer.
   *
   * @return number of bytes read or `-1` if end of stream
   *
   * @throws IOException if I/O error occurs
   */
  override def read(buffer: Array[Byte]): Int = read(buffer, 0, buffer.size)

  /**
   * Reads bytes from input stream into supplied buffer starting at given
   * offset.
   *
   * @return number of bytes read or `-1` if end of stream
   *
   * @throws IOException if I/O error occurs
   */
  override def read(buffer: Array[Byte], offset: Int, length: Int): Int = propose {
    var eof   = false
    var total = 0

    while !eof && total < length do
      in.read(buffer, offset + total, length - total) match
        case -1 => eof = propose(true)
        case n  => total += propose(n)

    eof && total == 0 match
      case true  => -1
      case false => total
  }

  /** Closes input stream. */
  override def close(): Unit =
    Try(out.close())
    Try(in.close())

  private def propose[T](value: => T): T =
    error.get match
      case null  => value
      case cause => throw IOException("Writer exception", cause)
