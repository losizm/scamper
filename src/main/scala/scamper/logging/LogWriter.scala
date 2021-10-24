/*
 * Copyright 2021 Carlos Conyers
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package scamper.logging

import java.io.{ Closeable, File, FileOutputStream, OutputStream, PrintWriter, Writer }
import java.nio.file.{ Files, Path, OpenOption }
import java.time.Instant

import scala.util.Try

/**
 * Provides logger to `java.io.PrintWriter`.
 *
 * @constructor Creates log writer.
 * @param writer writer to which logs are written
 */
class LogWriter(writer: PrintWriter) extends Logger with Closeable:
  require(writer != null)

  /** @inheritdoc */
  def trace(message: String): Unit =
    log("trace", message)

  /** @inheritdoc */
  def trace(format: String, args: Any*): Unit =
    log("trace", format.format(args*))

  /** @inheritdoc */
  def trace(message: String, cause: Throwable): Unit =
    log("trace", message, cause)

  /** @inheritdoc */
  def debug(message: String): Unit =
    log("debug", message)

  /** @inheritdoc */
  def debug(format: String, args: Any*): Unit =
    log("debug", format.format(args*))

  /** @inheritdoc */
  def debug(message: String, cause: Throwable): Unit =
    log("debug", message, cause)

  /** @inheritdoc */
  def info(message: String): Unit =
    log("info", message)

  /** @inheritdoc */
  def info(format: String, args: Any*): Unit =
    log("info", format.format(args*))

  /** @inheritdoc */
  def info(message: String, cause: Throwable): Unit =
    log("info", message, cause)

  /** @inheritdoc */
  def warn(message: String): Unit =
    log("warn", message)

  /** @inheritdoc */
  def warn(format: String, args: Any*): Unit =
    log("warn", format.format(args*))

  /** @inheritdoc */
  def warn(message: String, cause: Throwable): Unit =
    log("warn", message, cause)

  /** @inheritdoc */
  def error(message: String): Unit =
    log("error", message)

  /** @inheritdoc */
  def error(format: String, args: Any*): Unit =
    log("error", format.format(args*))

  /** @inheritdoc */
  def error(message: String, cause: Throwable): Unit =
    log("error", message, cause)

  /** Closes logger. */
  def close(): Unit = Try(writer.close())

  private def log(level: String, message: String): Unit =
    writer.println(s"${Instant.now()} [$level] $message")
    writer.flush()

  private def log(level: String, message: String, cause: Throwable): Unit =
    log(level, message)
    cause.printStackTrace(writer)

/** Provides factory for `LogWriter`. */
object LogWriter:
  /**
   * Creates log writer.
   *
   * @param writer writer to which logs are written
   */
  def apply(writer: Writer) =
    writer match
      case writer: PrintWriter => new LogWriter(writer)
      case _ => new LogWriter(PrintWriter(writer))

  /**
   * Creates log writer to given output stream.
   *
   * @param out output stream to which logs are written
   */
  def apply(out: OutputStream) =
    new LogWriter(PrintWriter(out))

  /**
   * Creates log writer to given file.
   *
   * @param file file to which logs are written
   */
  def apply(file: File) =
    new LogWriter(PrintWriter(FileOutputStream(file)))

  /**
   * Creates log writer to given file.
   *
   * @param file file to which logs are written
   * @param append specifies if file should be opened in append mode
   */
  def apply(file: File, append: Boolean) =
    new LogWriter(PrintWriter(FileOutputStream(file, append)))

  /**
   * Creates log writer to given file.
   *
   * @param fileName file name to which logs are written
   */
  def apply(fileName: String) =
    new LogWriter(PrintWriter(FileOutputStream(fileName)))

  /**
   * Creates log writer to given file.
   *
   * @param fileName file name to which logs are written
   * @param append specifies if file should be opened in append mode
   */
  def apply(fileName: String, append: Boolean) =
    new LogWriter(PrintWriter(FileOutputStream(fileName, append)))

  /**
   * Creates log writer to given path.
   *
   * @param path path to which logs are written
   * @param opts open options
   */
  def apply(path: Path, opts: OpenOption*) =
    new LogWriter(PrintWriter(Files.newOutputStream(path, opts*)))
