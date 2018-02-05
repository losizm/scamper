package scamper

import java.io.{ ByteArrayInputStream, ByteArrayOutputStream, InputStream, SequenceInputStream }
import scala.util.Try

/** Provides utility for parsing HTTP message body. */
trait BodyParser[T] {
  /**
   * Parses body of supplied HTTP message returning instance of defined type.
   */
  def apply(message: HttpMessage): T
}

/** Provides body parser implementations. */
object BodyParser {
  private val config = com.typesafe.config.ConfigFactory.load()
  private val maxBufferSize = Math.min(config.getBytes("scamper.parser.maxBufferSize"), Int.MaxValue).toInt

  /** Provides binary data body parser. */
  def binary(maxLength: Option[Int] = None): BodyParser[Array[Byte]] =
    new BinaryBodyParser(maxLength.getOrElse(maxBufferSize))

  /** Provides text body parser. */
  def text(maxLength: Option[Int] = None): BodyParser[String] =
    new TextBodyParser(maxLength.getOrElse(maxBufferSize))

  /** Provides form body parser. */
  def form(maxLength: Option[Int] = None): BodyParser[Map[String, Seq[String]]] =
    new FormBodyParser(maxLength.getOrElse(maxBufferSize))
}

private class BinaryBodyParser(maxLength: Int) extends BodyParser[Array[Byte]] {
  import bantam.nx.io._
  import java.util.zip.{ GZIPInputStream, InflaterInputStream }

  def apply(message: HttpMessage): Array[Byte] =
    message.body.withInputStream { in =>
      val dechunked = if (isChunked(message)) new SequenceInputStream(new ChunkEnumeration(in, maxLength, maxLength)) else in

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
    val out = new scala.collection.mutable.ArrayBuffer[Byte](maxLength.min(1024))
    val buffer = new Array[Byte](maxLength.min(1024))
    var length = 0

    while ({ length = in.read(buffer); length != -1 })
      if (out.length + length > maxLength) throw new HttpException(s"Entity too large: ${out.length + length} > $maxLength")
      else out ++= buffer.take(length)

    out.toArray
  }
}

private class TextBodyParser(maxLength: Int) extends BodyParser[String] {
  private val bodyParser = new BinaryBodyParser(maxLength)

  def apply(message: HttpMessage): String =
    message.contentType
      .flatMap(_.parameters.get("charset"))
      .orElse(Some("UTF-8"))
      .map(new String(bodyParser(message), _)).get
}

private class FormBodyParser(maxLength: Int) extends BodyParser[Map[String, Seq[String]]] {
  private val bodyParser = new TextBodyParser(maxLength)

  def apply(message: HttpMessage): Map[String, Seq[String]] =
    QueryParser.parse(bodyParser(message))
}

private class BoundInputStream(in: InputStream, limit: Long) extends java.io.FilterInputStream(in) {
  private var position = 0L

  override def read(): Int =
    if (position >= limit) -1
    else
      in.read() match {
        case -1   => -1
        case byte => position += 1; byte
      }

  override def read(buffer: Array[Byte], offset: Int, length: Int): Int =
    if (position >= limit) -1
    else
      in.read(buffer, offset, length.min(remaining)) match {
        case -1   => -1
        case byte => position += 1; byte
      }

  private def remaining: Int =
    (limit - position).min(Int.MaxValue).toInt
}

private class ChunkEnumeration(in: InputStream, maxChunkSize: Int, maxTotalLength: Long) extends java.util.Enumeration[InputStream] {
  private var chunkSize = nextChunkSize
  private var totalLength = chunkSize

  def hasMoreElements(): Boolean =
    chunkSize > 0

  def nextElement(): InputStream = {
    if (!hasMoreElements) throw new NoSuchElementException("No more chunks")
    if (chunkSize > maxChunkSize) throw new HttpException(s"Chunk too large: $chunkSize > $maxChunkSize")
    if (totalLength > maxTotalLength) throw new HttpException(s"Entity too large: $totalLength > $maxTotalLength")

    val buffer = new Array[Byte](chunkSize)
    var length = 0

    while (length < chunkSize)
      in.read(buffer, length, chunkSize - length) match {
        case -1    => throw new HttpException("Unexpected end of entity body")
        case count => length += count
      }

    // discard CRLF
    if (nextLine.length != 0) throw new HttpException("Invalid chunked")

    chunkSize = nextChunkSize
    totalLength += chunkSize

    new ByteArrayInputStream(buffer)
  }

  private def nextChunkSize: Int = {
    val line = nextLine

    Try(Integer.parseInt(line)).getOrElse {
      throw new HttpException(s"Invalid chunk size: $line")
    }
  }

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

