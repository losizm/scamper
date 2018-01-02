package scamper

import scala.util.Try

/** Provides a utility for parsing the body of an HTTP message. */
trait BodyParser[T] {
  /**
   * Parses the body of supplied HTTP message as an instance of defined type.
   *
   * @return the result of parsing computation -- i.e., <code>Success(T)</code>
   * or <code>Failure(Throwable)</code>
   */
  def apply(message: HttpMessage): Try[T]
}

/** Provides default implementation of various body parsers. */
object BodyParser {
  /** Provides a string body parser. */
  def string: BodyParser[String] = StringBodyParser
}

private object StringBodyParser extends BodyParser[String] {
  import java.io.{ ByteArrayOutputStream, InputStream }
  import java.util.zip.{ GZIPInputStream, InflaterInputStream }
  import bantam.nx.io._

  def apply(message: HttpMessage): Try[String] =
    Try {
      message.body.map { entity =>
        entity.withInputStream { in =>
          message.contentEncoding.getOrElse("identity") match {
            case "gzip" =>
              val gzipIn = new GZIPInputStream(in)
              try toString(gzipIn)
              finally gzipIn.close()

            case "deflate" =>
              val deflateIn = new InflaterInputStream(in)
              try toString(deflateIn)
              finally deflateIn.close()

            case "identity" => toString(in)

            case unsupported =>
              throw new RuntimeException(s"Unsupported content encoding: $unsupported")
          }
        }
      } getOrElse ""
    }

  private def toString(in: InputStream): String = {
    val out = new ByteArrayOutputStream(1024)
    out << in
    out.toString("UTF-8")
  }
}

