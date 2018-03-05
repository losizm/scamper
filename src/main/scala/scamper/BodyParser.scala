package scamper

import java.io.{ File, FileOutputStream, InputStream }

import scala.collection.mutable.ArrayBuffer
import scala.util.Try

import scamper.types._

/** Provides utility for parsing HTTP message body. */
trait BodyParser[T] {
  /**
   * Parses body of supplied HTTP message and returns instance of defined type.
   */
  def apply(message: HttpMessage): T
}

/** Provides body parser implementations. */
object BodyParsers {
  /**
   * Gets body parser to collect raw bytes of message body.
   *
   * @param maxLength maximum length in bytes allowed
   */
  def bytes(maxLength: Int = 4 * 1024 * 1024): BodyParser[Array[Byte]] =
    new ByteArrayBodyParser(maxLength)

  /**
   * Gets body parser to collect text content.
   *
   * @param maxLength maximum length in bytes allowed
   */
  def text(maxLength: Int = 4 * 1024 * 1024): BodyParser[String] =
    new TextBodyParser(maxLength)

  /**
   * Gets body parser to collect form data.
   *
   * @param maxLength maximum length in bytes allowed
   */
  def form(maxLength: Int = 4 * 1024 * 1024): BodyParser[Map[String, Seq[String]]] =
    new FormBodyParser(maxLength)

  /**
   * Gets body parser to store message body to file.
   *
   * If {@code dest} is a directory, then the parser creates a new file in the
   * specified directory on each parsing invocation. Otherwise, the parser
   * overwrites the specified file on each invocation.
   *
   * @param dest destination to which message body is stored
   * @param maxLength maximum length in bytes allowed
   */
  def file(dest: File = new File(sys.props("java.io.tmpdir")), maxLength: Long = 4 * 1024 * 1024): BodyParser[File] =
    new FileBodyParser(dest, maxLength, maxLength.min(Int.MaxValue).toInt)
}

private class ByteArrayBodyParser(val maxLength: Long) extends BodyParser[Array[Byte]] with BodyParsing {
  val maxBufferSize = maxLength.toInt
  private val bufferSize = maxBufferSize.min(8192)

  def apply(message: HttpMessage): Array[Byte] =
    withInputStream(message)(toByteArray)

  private def toByteArray(in: InputStream): Array[Byte] = {
    val out = new ArrayBuffer[Byte](bufferSize)
    val buf = new Array[Byte](bufferSize)
    var len = 0
    var tot = 0

    while ({ len = in.read(buf); len != -1 }) {
      tot += len
      if (tot > maxLength) throw new HttpException(s"Entity too large: length > $maxLength")
      out ++= buf.take(len)
    }

    out.toArray
  }
}

private class TextBodyParser(maxLength: Int) extends BodyParser[String] {
  private val bodyParser = new ByteArrayBodyParser(maxLength)

  def apply(message: HttpMessage): String =
    message.getHeaderValue("Content-Type")
      .map(MediaType.apply)
      .flatMap(_.params.get("charset"))
      .orElse(Some("UTF-8"))
      .map(new String(bodyParser(message), _)).get
}

private class FormBodyParser(maxLength: Int) extends BodyParser[Map[String, Seq[String]]] {
  private val bodyParser = new TextBodyParser(maxLength)

  def apply(message: HttpMessage): Map[String, Seq[String]] =
    QueryParams.parse(bodyParser(message))
}

private class FileBodyParser(dest: File, val maxLength: Long, val maxBufferSize: Int) extends BodyParser[File] with BodyParsing {
  def apply(message: HttpMessage): File =
    withInputStream(message) { in =>
      val destFile = getDestFile
      val out = new FileOutputStream(destFile)

      try {
        val buf = new Array[Byte](maxBufferSize.min(8192))
        var len = 0

        while ({ len = in.read(buf); len != -1 })
          out.write(buf, 0, len)

        destFile
      } finally Try(out.close())
    }

  private def getDestFile(): File =
    if (dest.isDirectory) File.createTempFile("scamper-dest-file-", ".tmp", dest)
    else dest
}

