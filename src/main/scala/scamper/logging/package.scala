/*
 * Copyright 2019 Carlos Conyers
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
package scamper

import java.io.{ Closeable, File, FileOutputStream, OutputStream, PrintWriter, Writer }
import java.nio.file.{ Files, Path, OpenOption }
import java.time.Instant

import scala.util.Try

/** Provides logging facilities. */
package object logging {
  /** Defines logger interface. */
  trait Logger {
    /**
     * Logs trace message.
     *
     * @param message log message
     */
    def trace(message: String): Unit

    /**
     * Logs trace message and stack trace of given cause.
     *
     * @param message log message
     * @param cause `Throwable` whose stack trace to log
     */
    def trace(message: String, cause: Throwable): Unit

    /**
     * Logs debug message.
     *
     * @param message log message
     */
    def debug(message: String): Unit

    /**
     * Logs debug message and stack trace of given cause.
     *
     * @param message log message
     * @param cause `Throwable` whose stack trace to log
     */
    def debug(message: String, cause: Throwable): Unit

    /**
     * Logs information message.
     *
     * @param message log message
     */
    def info(message: String): Unit

    /**
     * Logs information message and stack trace of given cause.
     *
     * @param message log message
     * @param cause `Throwable` whose stack trace to log
     */
    def info(message: String, cause: Throwable): Unit

    /**
     * Logs warning message.
     *
     * @param message log message
     */
    def warn(message: String): Unit

    /**
     * Logs warning message and stack trace of given cause.
     *
     * @param message log message
     * @param cause `Throwable` whose stack trace to log
     */
    def warn(message: String, cause: Throwable): Unit

    /**
     * Logs error message.
     *
     * @param message log message
     */
    def error(message: String): Unit

    /**
     * Logs error message and stack trace of given cause.
     *
     * @param message log message
     * @param cause `Throwable` whose stack trace to log
     */
    def error(message: String, cause: Throwable): Unit
  }

  /** Provides logger to console. */
  object ConsoleLogger extends Logger {
    def trace(message: String): Unit =
      log("trace", message)

    def trace(message: String, cause: Throwable): Unit =
      log("trace", message, cause)

    def debug(message: String): Unit =
      log("debug", message)

    def debug(message: String, cause: Throwable): Unit =
      log("debug", message, cause)

    def info(message: String): Unit =
      log("info", message)

    def info(message: String, cause: Throwable): Unit =
      log("info", message, cause)

    def warn(message: String): Unit =
      log("warn", message)

    def warn(message: String, cause: Throwable): Unit =
      log("warn", message, cause)

    def error(message: String): Unit =
      log("error", message)

    def error(message: String, cause: Throwable): Unit =
      log("error", message, cause)

    private def log(level: String, message: String): Unit =
      Console.println(s"${Instant.now()} [$level] $message")

    private def log(level: String, message: String, cause: Throwable): Unit = {
      log(level, message)
      cause.printStackTrace(Console.out)
    }
  }

  /**
   * Provides logger to `java.io.PrintWriter`.
   *
   * @constructor Creates log writer.
   * @param writer writer to which logs are written
   */
  class LogWriter(writer: PrintWriter) extends Logger with Closeable {
    def trace(message: String): Unit =
      log("trace", message)

    def trace(message: String, cause: Throwable): Unit =
      log("trace", message, cause)

    def debug(message: String): Unit =
      log("debug", message)

    def debug(message: String, cause: Throwable): Unit =
      log("debug", message, cause)

    def info(message: String): Unit =
      log("info", message)

    def info(message: String, cause: Throwable): Unit =
      log("info", message, cause)

    def warn(message: String): Unit =
      log("warn", message)

    def warn(message: String, cause: Throwable): Unit =
      log("warn", message, cause)

    def error(message: String): Unit =
      log("error", message)

    def error(message: String, cause: Throwable): Unit =
      log("error", message, cause)

    /** Closes logger. */
    def close(): Unit = Try(writer.close())

    private def log(level: String, message: String): Unit = {
      writer.println(s"${Instant.now()} [$level] $message")
      writer.flush()
    }

    private def log(level: String, message: String, cause: Throwable): Unit = {
      log(level, message)
      cause.printStackTrace(writer)
    }
  }

  /** LogWriter factory. */
  object LogWriter {
    /**
     * Creates log writer.
     *
     * @param writer writer to which log files are written
     */
    def apply(writer: Writer) =
      writer match {
        case writer: PrintWriter => new LogWriter(writer)
        case _ => new LogWriter(new PrintWriter(writer))
      }

    /**
     * Creates log writer to given output stream.
     *
     * @param file output stream to which logs are written
     */
    def apply(out: OutputStream) =
      new LogWriter(new PrintWriter(out))

    /**
     * Creates log writer to given file.
     *
     * @param file file to which logs are written
     */
    def apply(file: File) =
      new LogWriter(new PrintWriter(new FileOutputStream(file)))

    /**
     * Creates log writer to given file.
     *
     * @param file file to which logs are written
     * @param append specifies if file should be opened in append mode
     */
    def apply(file: File, append: Boolean) =
      new LogWriter(new PrintWriter(new FileOutputStream(file, append)))

    /**
     * Creates log writer to given file.
     *
     * @param fileName file name to which logs are written
     */
    def apply(fileName: String) =
      new LogWriter(new PrintWriter(new FileOutputStream(fileName)))

    /**
     * Creates log writer to given file.
     *
     * @param fileName file name to which logs are written
     * @param append specifies if file should be opened in append mode
     */
    def apply(fileName: String, append: Boolean) =
      new LogWriter(new PrintWriter(new FileOutputStream(fileName, append)))

    /**
     * Creates log writer to given path.
     *
     * @param path path to which logs are written
     * @param opts open options
     */
    def apply(path: Path, opts: OpenOption*) =
      new LogWriter(new PrintWriter(Files.newOutputStream(path, opts : _*)))
  }
}
