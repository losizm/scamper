package scamper

import java.io.{ ByteArrayInputStream, File, FileInputStream, InputStream }
import java.nio.file.Path
import java.nio.charset.Charset
import scala.util.Try

/** A representation of an HTTP entity. */
trait Entity {
  /** The size in bytes, if known. */
  def size: Option[Long]

  /** Gets an input stream to this entity. */
  def getInputStream: InputStream

  /**
   * Provides access to this entity's input stream with automatic resource
   * management.
   *
   * The input stream is passed to the supplied function, and the stream is
   * closed upon function's return.
   *
   * @return the value returned from supplied function
   */
  def withInputStream[T](f: InputStream => T): T = {
    val in = getInputStream
    try f(in)
    finally Try(in.close())
  }
}

/** An Entity factory for various content types. */
object Entity {
  /** Creates an entity whose content is the supplied bytes. */
  def apply(bytes: Array[Byte]): Entity =
    apply(bytes, 0, bytes.length)

  /** Creates an entity whose content is the supplied bytes. */
  def apply(bytes: Array[Byte], start: Int, length: Int): Entity = {
    require(start >= 0, "Start must be nonnegative")
    require(start + length <= bytes.length, "Applied start and length must not exceed data length")

    val copy = new Array[Byte](length)
    bytes.copyToArray(copy, start, length)
    ByteArrayEntity(copy)
  }

  /** Creates an entity from the input stream returned from supplied function. */
  def apply(f: () => InputStream): Entity =
    InputStreamEntity(f)

  /** Creates an entity whose content is the data in file at supplied path. */
  def apply(path: Path): Entity =
    FileEntity(path.toFile)

  /** Creates an entity whose content is the data in supplied file. */
  def apply(file: File): Entity =
    FileEntity(file)

  /** Creates an entity whose content is the encoded bytes from supplied string. */
  def apply(string: String, charset: String): Entity =
    ByteArrayEntity(string.getBytes(charset))

  /** Creates an entity whose content is the encoded bytes from supplied string. */
  def apply(string: String, charset: Charset): Entity =
    ByteArrayEntity(string.getBytes(charset))
}

private case class InputStreamEntity(f: () => InputStream) extends Entity {
  def size = None
  def getInputStream = f()
}

private case class ByteArrayEntity(bytes: Array[Byte]) extends Entity {
  def size = Some(bytes.length)
  def getInputStream = new ByteArrayInputStream(bytes)
}

private case class FileEntity(file: File) extends Entity {
  def size = Some(file.length)
  def getInputStream = new FileInputStream(file)
}

