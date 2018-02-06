package scamper

import bantam.nx.io._

import com.typesafe.config.ConfigFactory

import java.io.{ File, InputStream, SequenceInputStream }
import java.util.zip.{ GZIPInputStream, InflaterInputStream }

import scala.collection.mutable.ArrayBuffer

/** Provides utility for parsing HTTP message body. */
trait BodyParser[T] {
  /**
   * Parses body of supplied HTTP message and returns instance of defined type.
   */
  def apply(message: HttpMessage): T
}

/** Provides body parser implementations. */
object BodyParser {
  private val config = ConfigFactory.load()
  private val maxBufferSize = Math.min(config.getBytes("scamper.parser.maxBufferSize"), Int.MaxValue).toInt
  private val maxFileSize = config.getBytes("scamper.parser.maxFileSize")

  /** Gets body parser to collect raw bytes of message body. */
  def bytes: BodyParser[Array[Byte]] =
    bytes(maxBufferSize)

  /**
   * Gets body parser to collect raw bytes of message body.
   *
   * @param maxLength maximum length in bytes allowed
   */
  def bytes(maxLength: Int): BodyParser[Array[Byte]] =
    new ByteArrayBodyParser(maxLength)

  /** Gets body parser to collect text content. */
  def text: BodyParser[String] =
    text(maxBufferSize)

  /**
   * Gets body parser to collect text content.
   *
   * @param maxLength maximum length in bytes allowed
   */
  def text(maxLength: Int): BodyParser[String] =
    new TextBodyParser(maxLength)

  /** Gets body parser to collect form data. */
  def form: BodyParser[Map[String, Seq[String]]] =
    form(maxBufferSize)

  /**
   * Gets body parser to collect form data.
   *
   * @param maxLength maximum length in bytes allowed
   */
  def form(maxLength: Int): BodyParser[Map[String, Seq[String]]] =
    new FormBodyParser(maxLength)

  /**
   * Gets body parser to store message body to file.
   *
   * @param dest destination file to which message body is stored
   */
  def file(dest: File): BodyParser[File] =
    file(dest, maxFileSize)

  /**
   * Gets body parser to store message body to file.
   *
   * @param dest destination file to which message body is stored
   * @param maxLength maximum length in bytes allowed
   */
  def file(dest: File, maxLength: Long): BodyParser[File] =
    new FileBodyParser(dest, maxLength, maxBufferSize)
}

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
        if (isChunked(message)) chunkInputStream(in)
        else new BoundInputStream(in, message.headerValue("Content-Length").toLong)

      message.contentEncoding.getOrElse("identity") match {
        case "gzip"     => f(new GZIPInputStream(dechunked))
        case "deflate"  => f(new InflaterInputStream(dechunked))
        case "identity" => f(dechunked)
        case encoding   => throw new HttpException(s"Unsupported content encoding: $encoding")
      }
    }

  private def chunkInputStream(in: InputStream) =
    new SequenceInputStream(new ChunkEnumeration(in, maxBufferSize, maxLength))

  private def isChunked(message: HttpMessage): Boolean =
    message.isChunked && !message.getHeaderValue("X-Scamper-Decoding").exists(_.contains("chunked"))
}

private class ByteArrayBodyParser(val maxLength: Long) extends BodyParser[Array[Byte]] with BodyParsing {
  val maxBufferSize = maxLength.toInt
  private val bufferSize = maxBufferSize.min(8192)

  def apply(message: HttpMessage): Array[Byte] =
    withInputStream(message)(toByteArray)

  private def toByteArray(in: InputStream): Array[Byte] = {
    val out = new ArrayBuffer[Byte](bufferSize)
    val buffer = new Array[Byte](bufferSize)
    var length = 0

    while ({ length = in.read(buffer); length != -1 }) {
      val totalLength = out.length + length
      if (totalLength > maxLength)
        throw new EntityException(s"Entity too large: $totalLength > $maxLength")
      out ++= buffer.take(length)
    }

    out.toArray
  }
}

private class TextBodyParser(maxLength: Int) extends BodyParser[String] {
  private val bodyParser = new ByteArrayBodyParser(maxLength)

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

private class FileBodyParser(dest: File, val maxLength: Long, val maxBufferSize: Int) extends BodyParser[File] with BodyParsing {
  def apply(message: HttpMessage): File =
    withInputStream(message) { in =>
      dest.withOutputStream(out => out << in)
      dest
    }
}

