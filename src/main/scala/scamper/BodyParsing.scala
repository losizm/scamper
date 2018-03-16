package scamper

import java.io.{ InputStream, SequenceInputStream }
import java.util.zip.{ GZIPInputStream, InflaterInputStream }

import scamper.ImplicitHeaders.{ ContentEncoding, ContentLength, TransferEncoding }

/** A mixin that provides access to decoded message body. */
trait BodyParsing {
  /** Maximum body length allowed */
  def maxLength: Long

  /** Maximum buffer size allowed */
  def maxBufferSize: Int

  /**
   * Provides input stream to decoded message body.
   *
   * @param message HTTP message
   * @param f input stream handler
   *
   * @return value returned from handler
   */
  def withInputStream[T](message: HttpMessage)(f: InputStream => T): T =
    message.body.withInputStream { in =>
      val dechunked =
        if (isChunked(message)) dechunkInputStream(in)
        else new BoundInputStream(in, message.getContentLength.getOrElse(maxLength))

      message.contentEncoding.headOption.map(_.name).getOrElse("identity") match {
        case "gzip"     => f(new GZIPInputStream(dechunked))
        case "deflate"  => f(new InflaterInputStream(dechunked))
        case "identity" => f(dechunked)
        case encoding   => throw new HttpException(s"Unsupported content encoding: $encoding")
      }
    }

  private def dechunkInputStream(in: InputStream) =
    new SequenceInputStream(new ChunkEnumeration(in, maxBufferSize, maxLength))

  private def isChunked(message: HttpMessage): Boolean =
    message.transferEncoding.exists(_.name == "chunked") &&
      !message.getHeaderValue("X-Scamper-Transfer-Decoding").contains("chunked")
}

