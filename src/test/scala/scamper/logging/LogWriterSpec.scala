/*
 * Copyright 2020 Carlos Conyers
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

import java.io.{ PrintWriter, StringWriter }
import java.time.Instant

class LogWriterSpec extends org.scalatest.flatspec.AnyFlatSpec {
  private case class LogEntry(time: Instant, level: String, message: String) {
    def append(message: String): LogEntry =
      copy(message = this.message + "\n" + message)
  }

  it should "log trace messages" in {
    val output = new StringWriter(8192)
    val logger = new LogWriter(new PrintWriter(output))
    logger.trace("This is message #1")
    logger.trace("This is %s #%d", "message", 2)
    logger.trace("This is message #3", new Exception("Oops!"))
    logger.trace("This is message #4")

    doAssertions("trace", parseLog(output.toString))
  }

  it should "log debug messages" in {
    val output = new StringWriter(8192)
    val logger = new LogWriter(new PrintWriter(output))
    logger.debug("This is message #1")
    logger.debug("This is %s #%d", "message", 2)
    logger.debug("This is message #3", new Exception("Oops!"))
    logger.debug("This is message #4")

    doAssertions("debug", parseLog(output.toString))
  }

  it should "log info messages" in {
    val output = new StringWriter(8192)
    val logger = new LogWriter(new PrintWriter(output))
    logger.info("This is message #1")
    logger.info("This is %s #%d", "message", 2)
    logger.info("This is message #3", new Exception("Oops!"))
    logger.info("This is message #4")

    doAssertions("info", parseLog(output.toString))
  }

  it should "log warn messages" in {
    val output = new StringWriter(8192)
    val logger = new LogWriter(new PrintWriter(output))
    logger.warn("This is message #1")
    logger.warn("This is %s #%d", "message", 2)
    logger.warn("This is message #3", new Exception("Oops!"))
    logger.warn("This is message #4")

    doAssertions("warn", parseLog(output.toString))
  }

  it should "log error messages" in {
    val output = new StringWriter(8192)
    val logger = new LogWriter(new PrintWriter(output))
    logger.error("This is message #1")
    logger.error("This is %s #%d", "message", 2)
    logger.error("This is message #3", new Exception("Oops!"))
    logger.error("This is message #4")

    doAssertions("error", parseLog(output.toString))
  }

  private def doAssertions(level: String, entries: Seq[LogEntry]): Unit = {
    assert(entries.size == 4)
    assert(entries.forall(_.time.isAfter(Instant.now().minusSeconds(3))))
    assert(entries.forall(_.time.isBefore(Instant.now().plusSeconds(1))))
    assert(entries.forall(_.level == level))
    assert(entries(0).message == "This is message #1")
    assert(entries(1).message == "This is message #2")
    assert(entries(2).message.startsWith("This is message #3"))
    assert(entries(2).message.contains("java.lang.Exception: Oops!"))
    assert(entries(2).message.contains(s"at ${this.getClass.getName}"))
    assert(entries(3).message.startsWith("This is message #4"))
  }

  private def parseLog(output: String): Seq[LogEntry] = {
    val entry = """(\S+) \[(\S+)\] (.*)""".r

    output
      .split("(\r\n|\r|\n)")
      .foldLeft(Seq.empty[LogEntry]) { (entries, line) =>
        line match {
          case entry(time, lvl, msg) => entries :+ LogEntry(Instant.parse(time), lvl, msg)
          case message               => entries.init :+ entries.last.append(message)
        }
      }
  }
}
