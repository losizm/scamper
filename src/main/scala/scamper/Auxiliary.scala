/*
 * Copyright 2018 Carlos Conyers
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

import java.io.{ File, FileOutputStream, InputStream, OutputStream }
import java.net.{ Socket, URI, URLDecoder, URLEncoder }
import java.nio.file.{ Paths, Path }
import java.time.Instant
import java.util.concurrent.{ ArrayBlockingQueue, ThreadFactory, ThreadPoolExecutor, TimeUnit }
import java.util.concurrent.atomic.AtomicLong

import scala.collection.mutable.ArrayBuffer
import scala.concurrent.ExecutionContext
import scala.util.Try

private object Auxiliary {
  private val crlf = "\r\n".getBytes("UTF-8")

  val `application/octet-stream` = types.MediaType("application", "octet-stream")
  val `text/plain` = types.MediaType("text", "plain")

  implicit class FileType(val file: File) extends AnyVal {
    def withOutputStream[T](f: OutputStream => T): T = {
      val out = new FileOutputStream(file)
      try f(out)
      finally Try(out.close())
    }
  }

  implicit class InputStreamType(val in: InputStream) extends AnyVal {
    def getBytes(bufferSize: Int = 8192): Array[Byte] = {
      val bytes = new ArrayBuffer[Byte]
      val buffer = new Array[Byte](bufferSize.max(1024))
      var len = 0

      while ({ len = in.read(buffer); len != -1 })
        bytes ++= buffer.take(len)

      bytes.toArray
    }

    def getText(bufferSize: Int = 8192): String =
      new String(getBytes(bufferSize), "UTF-8")

    def getToken(delimiters: String, buffer: Array[Byte]): String = {
      var length = 0
      var byte = -1

      while ({ byte = in.read(); !delimiters.contains(byte) && byte != -1}) {
        buffer(length) = byte.toByte
        length += 1
      }

      new String(buffer, 0, length, "UTF-8")
    }

    def getLine(buffer: Array[Byte]): String = {
      var length = 0
      var byte = -1

      while ({ byte = in.read(); byte != '\n' && byte != -1}) {
        buffer(length) = byte.toByte
        length += 1
      }

      if (length > 0 && buffer(length - 1) == '\r')
        length -= 1

      new String(buffer, 0, length, "UTF-8")
    }

    def readLine(buffer: Array[Byte]): Int = {
      val bufferSize = buffer.size
      var length = 0
      var byte = -1
      var continue = length < bufferSize

      while (continue) {
        val byte = in.read()

        if (byte == -1)
          continue = false
        else {
          buffer(length) = byte.toByte
          length += 1
          continue = length < bufferSize && byte != '\n'
        }
      }

      length
    }
  }

  implicit class OutputStreamType(val out: OutputStream) extends AnyVal {
    def writeLine(text: String): Unit = {
      out.write(text.getBytes("UTF-8"))
      out.write(crlf)
    }

    def writeLine(): Unit = out.write(crlf)
  }

  implicit class SocketType(val socket: Socket) extends AnyVal {
    def read(): Int = socket.getInputStream().read()

    def read(buffer: Array[Byte]): Int = socket.getInputStream().read(buffer)

    def read(buffer: Array[Byte], offset: Int, length: Int): Int =
      socket.getInputStream().read(buffer, offset, length)

    def readLine(buffer: Array[Byte]): Int =
      socket.getInputStream().readLine(buffer)

    def getToken(delimiters: String, buffer: Array[Byte]): String =
      socket.getInputStream().getToken(delimiters, buffer)

    def getLine(buffer: Array[Byte]): String =
      socket.getInputStream().getLine(buffer)

    def write(byte: Int): Unit = socket.getOutputStream().write(byte)

    def write(buffer: Array[Byte]): Unit = socket.getOutputStream().write(buffer)

    def write(buffer: Array[Byte], offset: Int, length: Int): Unit =
      socket.getOutputStream().write(buffer, offset, length)

    def writeLine(text: String): Unit = socket.getOutputStream().writeLine(text)

    def writeLine(): Unit = socket.getOutputStream().writeLine()

    def flush(): Unit = socket.getOutputStream().flush()
  }

  implicit class StringType(val string: String) extends AnyVal {
    def matchesAny(regexes: String*): Boolean = regexes.exists(string.matches)

    def toInstant: Instant = Try(Instant.parse(string)).getOrElse(DateValue.parse(string))

    def toFile: File = new File(string)

    def toPath: Path = Paths.get(string)

    def toUri: URI = new URI(string)

    def toUrlEncoded: String = URLEncoder.encode(string, "UTF-8")

    def toUrlEncoded(encoding: String): String = URLEncoder.encode(string, encoding)

    def toUrlDecoded: String = URLDecoder.decode(string, "UTF-8")

    def toUrlDecoded(encoding: String): String = URLDecoder.decode(string, encoding)
  }

  implicit class UriType(val uri: URI) extends AnyVal {
    def withScheme(scheme: String): URI =
      buildUri(scheme, uri.getRawAuthority, uri.getRawPath, uri.getRawQuery, uri.getRawFragment)

    def withAuthority(authority: String): URI =
      buildUri(uri.getScheme, authority, uri.getRawPath, uri.getRawQuery, uri.getRawFragment)

    def withPath(path: String): URI =
      buildUri(uri.getScheme, uri.getRawAuthority, path, uri.getRawQuery, uri.getRawFragment)

    def withQuery(query: String): URI =
      buildUri(uri.getScheme, uri.getRawAuthority, uri.getRawPath, query, uri.getRawFragment)

    def withFragment(fragment: String): URI =
      buildUri(uri.getScheme, uri.getRawAuthority, uri.getRawPath, uri.getRawQuery, fragment)

    private def buildUri(scheme: String, authority: String, path: String, query: String, fragment: String): URI = {
      val uri = new StringBuilder()

      if (scheme != null) uri.append(scheme).append(":")
      if (authority != null) uri.append("//").append(authority)

      if (path != null && path != "")
        uri.append('/').append(path.dropWhile(_ == '/'))

      if (query != null && query != "") uri.append('?').append(query)
      if (fragment != null && fragment != "") uri.append('#').append(fragment)

      new URI(uri.toString)
    }
  }

  lazy val executor = ExecutionContext.fromExecutorService {
    val threadGroup = new ThreadGroup(s"scamper-auxiliary")
    val threadCount = new AtomicLong(0)
    val maxPoolSize = Try(sys.props("scamper.auxiliary.executor.maxPoolSize").toInt)
      .getOrElse(Runtime.getRuntime.availableProcessors + 2)
      .max(8)

    object ServiceThreadFactory extends ThreadFactory {
      def newThread(task: Runnable) = {
        val thread = new Thread(threadGroup, task, s"scamper-auxiliary-${threadCount.incrementAndGet()}")
        thread.setDaemon(true)
        thread
      }
    }

    new ThreadPoolExecutor(
      2,
      maxPoolSize,
      60,
      TimeUnit.SECONDS,
      new ArrayBlockingQueue[Runnable](maxPoolSize * 4),
      ServiceThreadFactory
    )
  }
}
