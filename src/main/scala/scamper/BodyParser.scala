package scamper

import java.io.{ ByteArrayInputStream, ByteArrayOutputStream, InputStream, SequenceInputStream }

/** Provides a utility for parsing the body of an HTTP message. */
trait BodyParser[T] {
  /**
   * Parses the body of supplied HTTP message as an instance of defined type.
   */
  def apply(message: HttpMessage): T
}

/** Provides default implementation of various body parsers. */
object BodyParser {
  /** Provides a body parser of binary data. */
  def binary: BodyParser[Array[Byte]] = BinaryBodyParser

  /** Provides a text body parser. */
  def text: BodyParser[String] = TextBodyParser
}

private object BinaryBodyParser extends BodyParser[Array[Byte]] {
  import bantam.nx.io._
  import java.util.zip.{ GZIPInputStream, InflaterInputStream }

  def apply(message: HttpMessage): Array[Byte] =
    message.body.withInputStream { in =>
      val dechunked = if (isChunked(message)) new SequenceInputStream(new ChunkEnumeration(in)) else in

      message.contentEncoding.getOrElse("identity") match {
        case "gzip" =>
          val gzipIn = new GZIPInputStream(dechunked)
          try toByteArray(gzipIn)
          finally gzipIn.close()

        case "deflate" =>
          val deflateIn =  new InflaterInputStream(dechunked)
          try toByteArray(deflateIn)
          finally deflateIn.close()

        case "identity" => toByteArray(dechunked)

        case encoding => throw new HttpException(s"Unsupported content encoding: $encoding")
      }
    }

  private def isChunked(message: HttpMessage): Boolean =
    message.isChunked && !message.getHeaderValue("X-Scamper-Decoding").exists(_.contains("chunked"))

  private def toByteArray(in: InputStream): Array[Byte] = {
    val out = new ByteArrayOutputStream(1024)
    out << in
    out.toByteArray
  }
}

private object TextBodyParser extends BodyParser[String] {
  def apply(message: HttpMessage): String =
    message.contentType
      .flatMap(_.parameters.get("charset"))
      .orElse(Some("UTF-8"))
      .map(new String(BinaryBodyParser(message), _)).get
}

private class ChunkEnumeration(in: InputStream) extends java.util.Enumeration[InputStream] {
  private var chunkSize = nextChunkSize

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
    if (nextLine.length != 0) throw new HttpException("Invalid chunked encoding")

    chunkSize = nextChunkSize

    new ByteArrayInputStream(buffer)
  }

  private def nextChunkSize: Int =
    Integer.parseInt(nextLine.split("\\s+", 2).head)

  private def nextLine: String = {
    def nextByte: Int =
      in.read() match {
        case '\r' =>
          if (in.read() != '\n') throw new HttpException("Invalid chunked encoding")
          else -1

        case '\n' => -1
        case byte => byte
      }

    val buffer = new scala.collection.mutable.ArrayBuffer[Byte](8)
    var byte = 0

    while ({ byte = nextByte; byte != -1 })
      buffer += byte.toByte

    new String(buffer.toArray, "ASCII")
  }
}

