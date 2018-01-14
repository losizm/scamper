package scamper

/** Provides a utility for parsing the body of an HTTP message. */
trait BodyParser[T] {
  /**
   * Parses the body of supplied HTTP message as an instance of defined type.
   *
   * @return the result of parsing computation -- i.e., <code>Success(T)</code>
   * or <code>Failure(Throwable)</code>
   */
  def apply(message: HttpMessage): T
}

/** Provides default implementation of various body parsers. */
object BodyParser {
  /** Provides a byte array body parser. */
  def bytes: BodyParser[Array[Byte]] = ByteArrayBodyParser

  /** Provides a string body parser. */
  def string: BodyParser[String] = StringBodyParser
}

private object ByteArrayBodyParser extends BodyParser[Array[Byte]] {
  import java.io.{ ByteArrayOutputStream, InputStream }
  import java.util.zip.{ GZIPInputStream, InflaterInputStream }
  import bantam.nx.io._

  def apply(message: HttpMessage): Array[Byte] =
    message.body.map { entity =>
      entity.withInputStream { in =>
        message.contentEncoding.getOrElse("identity") match {
          case "gzip" =>
            val gzipIn = new GZIPInputStream(in)
            try toByteArray(gzipIn)
            finally gzipIn.close()

          case "deflate" =>
            val deflateIn = new InflaterInputStream(in)
            try toByteArray(deflateIn)
            finally deflateIn.close()

          case "identity" => toByteArray(in)

          case unsupported =>
            throw new RuntimeException(s"Unsupported content encoding: $unsupported")
        }
      }
    } getOrElse Array.empty

  private def toByteArray(in: InputStream): Array[Byte] = {
    val out = new ByteArrayOutputStream(1024)
    out << in
    out.toByteArray
  }
}

private object StringBodyParser extends BodyParser[String] {
  def apply(message: HttpMessage): String =
    message.contentType
      .flatMap(_.parameters.get("charset"))
      .orElse(Some("UTF-8"))
      .map(new String(ByteArrayBodyParser(message), _)).get
}

