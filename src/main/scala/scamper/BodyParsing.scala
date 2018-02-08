package scamper

import java.io.{ InputStream, SequenceInputStream }
import java.util.zip.{ GZIPInputStream, InflaterInputStream }

/** A mixin that provides access to decoded message body. */
trait BodyParsing {
  /** Maximum body length allowed */
  def maxLength: Long

  /** Maximum buffer size allowed */
  def maxBufferSize: Int

  /**
   * Provides decoded input stream to message body. That is, the input stream
   * passed to handler is uncompressed and dechunked.
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
        else new BoundInputStream(in, message.headerValue("Content-Length").toLong)

      message.contentEncoding.getOrElse("identity") match {
        case "gzip" | "x-gzip" => f(new GZIPInputStream(dechunked))
        case "deflate"         => f(new InflaterInputStream(dechunked))
        case "identity"        => f(dechunked)
        case encoding          => throw new HttpException(s"Unsupported content encoding: $encoding")
      }
    }

  private def dechunkInputStream(in: InputStream) =
    new SequenceInputStream(new ChunkEnumeration(in, maxBufferSize, maxLength))

  private def isChunked(message: HttpMessage): Boolean =
    message.isChunked && !message.getHeaderValue("X-Scamper-Decoding").exists(_.contains("chunked"))
}

