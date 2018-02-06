package scamper

import java.io.{ FilterInputStream, InputStream }

private class BoundInputStream(in: InputStream, maxLength: Long) extends FilterInputStream(in) {
  private var position: Long = 0

  override def read(): Int =
    if (position >= maxLength) -1
    else
      in.read() match {
        case -1   => -1
        case byte => position += 1; byte
      }

  override def read(buffer: Array[Byte], offset: Int, length: Int): Int =
    if (position >= maxLength) -1
    else
      in.read(buffer, offset, length.min(maxRead)) match {
        case -1   => -1
        case byte => position += 1; byte
      }

  private def maxRead: Int =
    (maxLength - position).min(Int.MaxValue).toInt
}

