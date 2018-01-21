package scamper

import java.io.{ ByteArrayInputStream, File, FileInputStream, InputStream }
import java.nio.file.Path
import java.nio.charset.Charset
import scala.util.Try

/** A representation of an HTTP entity. */
trait Entity {
  /** The size in bytes, if known. */
  def size: Option[Long]

  /** Tests whether the entity is known to be empty. */
  def isKnownEmpty: Boolean =
    size.contains(0)

  /** Gets an input stream to the entity. */
  def getInputStream: InputStream

  /**
   * Provides access to the entity's input stream with automatic resource
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

/** Provides Entity factory methods. */
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

  /** Creates an entity whose content is the encoded bytes of supplied text. */
  def apply(text: String, charset: String): Entity =
    ByteArrayEntity(text.getBytes(charset))

  /** Creates an entity whose content is the encoded bytes of supplied text. */
  def apply(text: String, charset: Charset): Entity =
    ByteArrayEntity(text.getBytes(charset))

  /** Creates an empty entity. */
  def empty: Entity = ByteArrayEntity(Array.empty)
}

private case class InputStreamEntity(f: () => InputStream) extends Entity {
  val size = None
  def getInputStream = f()
}

private case class ByteArrayEntity(bytes: Array[Byte]) extends Entity {
  val size = Some(bytes.length)
  def getInputStream = new ByteArrayInputStream(bytes)
}

private case class FileEntity(file: File) extends Entity {
  def size = Some(file.length)
  def getInputStream = new FileInputStream(file)
}

