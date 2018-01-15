package scamper

import java.io.{ ByteArrayInputStream, ByteArrayOutputStream, InputStream, SequenceInputStream }

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
  import bantam.nx.io._
  import java.util.zip.{ GZIPInputStream, InflaterInputStream }

  implicit class HttpMessageType(message: HttpMessage) {
    def isChunkedManaged: Boolean =
      message.getHeaderValue("X-Scamper-Chunked-Managed").map(_.toBoolean).getOrElse(false)
  }

  def apply(message: HttpMessage): Array[Byte] =
    message.body.map { entity =>
      entity.withInputStream { in =>
        message.contentEncoding.getOrElse("identity") match {
          case "gzip" =>
            val gzipIn = if (message.isChunked && !message.isChunkedManaged)
                new GZIPInputStream(new SequenceInputStream(new ChunkEnumeration(in)))
              else new GZIPInputStream(in)

            try toByteArray(gzipIn)
            finally gzipIn.close()

          case "deflate" =>
            val deflateIn = if (message.isChunked && !message.isChunkedManaged)
                new InflaterInputStream(new SequenceInputStream(new ChunkEnumeration(in)))
              else new InflaterInputStream(in)

            try toByteArray(deflateIn)
            finally deflateIn.close()

          case "identity" =>
            if (message.isChunked && !message.isChunkedManaged)
              toByteArray(new SequenceInputStream(new ChunkEnumeration(in)))
            else toByteArray(in)

          case encoding =>
            throw new HttpException(s"Unsupported content encoding: $encoding")
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

private class ChunkEnumeration(in: InputStream) extends java.util.Enumeration[InputStream] {
  private var chunkSize = readChunkSize

  def hasMoreElements(): Boolean =
    chunkSize > 0

  def nextElement(): InputStream = {
    if (!hasMoreElements) throw new NoSuchElementException("No more chunks")

    val buffer = new Array[Byte](chunkSize)
    var length = 0

    while (length < chunkSize)
      in.read(buffer, length, chunkSize - length) match {
        case -1    => throw new HttpException("Unexpected end of entity body")
        case count => length += count
      }

    // discard CRLF
    if (readLine.length != 0) throw new HttpException("Invalid chunked encoding")

    chunkSize = readChunkSize

    new ByteArrayInputStream(buffer)
  }

  private def readChunkSize: Int =
    readLine.split("\\s+", 2) match {
      case Array(size, _*) => Integer.parseInt(size, 16)
    }

  private def readLine: String = {
    def read: Int =
      in.read() match {
        case '\r' =>
          if (in.read() != '\n') throw new HttpException("Invalid chunked encoding")
          else -1

        case '\n' => -1
        case byte => byte
      }

    val buffer = new scala.collection.mutable.ArrayBuffer[Byte](8)
    var byte = 0

    while ({ byte = read; byte != -1 })
      buffer += byte.toByte

    new String(buffer.toArray, "ASCII")
  }
}

