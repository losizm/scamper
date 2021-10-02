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
package scamper

import java.io.{ File, FileOutputStream, FileInputStream, InputStream, OutputStream }
import java.net.{ Socket, URLDecoder, URLEncoder }
import java.nio.file.{ Paths, Path }
import java.time.Instant

import scala.collection.mutable.ArrayBuffer
import scala.util.Try

import scamper.types.MediaType
import RuntimeProperties.auxiliary.*

private object Auxiliary:
  private val crlf = "\r\n".getBytes("UTF-8")

  lazy val executor =
    ThreadPoolExecutorService
      .dynamic(
        name             = "scamper-auxiliary",
        corePoolSize     = executorCorePoolSize,
        maxPoolSize      = executorMaxPoolSize,
        keepAliveSeconds = executorKeepAliveSeconds,
        queueSize        = executorQueueSize
      ) { (task, executor) =>
        if executorShowWarning then
          System.err.println(s"[WARNING] Running rejected scamper-auxiliary task on dedicated thread.")
        executor.getThreadFactory.newThread(task).start()
      }

  implicit class FileType(file: File) extends AnyVal:
    def withOutputStream[T](f: OutputStream => T): T =
      val out = FileOutputStream(file)
      try f(out)
      finally Try(out.close())

    def withInputStream[T](f: InputStream => T): T =
      val in = FileInputStream(file)
      try f(in)
      finally Try(in.close())

  implicit class InputStreamType(val in: InputStream) extends AnyVal:
    def getBytes(bufferSize: Int = 8192): Array[Byte] =
      val bytes = ArrayBuffer[Byte]()
      val buffer = new Array[Byte](bufferSize.max(1024))
      var len = 0

      while { len = in.read(buffer); len != -1 } do
        bytes ++= buffer.take(len)

      bytes.toArray

    def getText(bufferSize: Int = 8192): String =
      String(getBytes(bufferSize), "UTF-8")

    def getToken(delimiters: String, buffer: Array[Byte], offset: Int = 0): String =
      var length = offset
      var byte = -1

      while { byte = in.read(); !delimiters.contains(byte) && byte != -1 } do
        buffer(length) = byte.toByte
        length += 1

      String(buffer, 0, length, "UTF-8")

    def getLine(buffer: Array[Byte], offset: Int = 0): String =
      var length = offset
      var byte = -1

      while { byte = in.read(); byte != '\n' && byte != -1} do
        buffer(length) = byte.toByte
        length += 1

      if length > 0 && buffer(length - 1) == '\r' then
        length -= 1

      String(buffer, 0, length, "UTF-8")

    def readLine(buffer: Array[Byte], offset: Int = 0): Int =
      val bufferSize = buffer.size
      var length = offset
      var continue = length < bufferSize

      while continue do
        in.read() match
          case -1 =>
            continue = false

          case byte =>
            buffer(length) = byte.toByte
            length += 1
            continue = length < bufferSize && byte != '\n'

      length

    def readMostly(buffer: Array[Byte]): Int =
      readMostly(buffer, 0, buffer.length)

    def readMostly(buffer: Array[Byte], offset: Int, length: Int): Int =
      var total = in.read(buffer, offset, length)

      if total != -1 && total < length then
        var count = 0

        while count != -1 && total < length do
          total += count
          count = in.read(buffer, offset + total, length - total)

      total

  implicit class OutputStreamType(val out: OutputStream) extends AnyVal:
    def writeLine(text: String): Unit =
      out.write(text.getBytes("UTF-8"))
      out.write(crlf)

    def writeLine(): Unit =
      out.write(crlf)

  implicit class SocketType(val socket: Socket) extends AnyVal:
    def read(): Int =
      socket.getInputStream().read()

    def read(buffer: Array[Byte]): Int =
      socket.getInputStream().read(buffer)

    def read(buffer: Array[Byte], offset: Int, length: Int): Int =
      socket.getInputStream().read(buffer, offset, length)

    def readLine(buffer: Array[Byte], offset: Int = 0): Int =
      socket.getInputStream().readLine(buffer, offset)

    def getToken(delimiters: String, buffer: Array[Byte], offset: Int = 0): String =
      socket.getInputStream().getToken(delimiters, buffer, offset)

    def getLine(buffer: Array[Byte], offset: Int = 0): String =
      socket.getInputStream().getLine(buffer, offset)

    def write(byte: Int): Unit =
      socket.getOutputStream().write(byte)

    def write(buffer: Array[Byte]): Unit =
      socket.getOutputStream().write(buffer)

    def write(buffer: Array[Byte], offset: Int, length: Int): Unit =
      socket.getOutputStream().write(buffer, offset, length)

    def writeLine(text: String): Unit =
      socket.getOutputStream().writeLine(text)

    def writeLine(): Unit =
      socket.getOutputStream().writeLine()

    def flush(): Unit =
      socket.getOutputStream().flush()

  implicit class StringType(val string: String) extends AnyVal:
    def matchesAny(regexes: String*): Boolean =
      regexes.exists(string.matches)

    def toUrlEncoded(charset: String): String =
      URLEncoder.encode(string, charset)

    def toUrlDecoded(charset: String): String =
      URLDecoder.decode(string, charset)

  implicit class UriType(val uri: Uri) extends AnyVal:
    def toTarget: Uri =
      buildUri(null, null, uri.getRawPath, uri.getRawQuery, null)

    def setScheme(scheme: String): Uri =
      buildUri(scheme, uri.getRawAuthority, uri.getRawPath, uri.getRawQuery, uri.getRawFragment)

    def setAuthority(authority: String): Uri =
      buildUri(uri.getScheme, authority, uri.getRawPath, uri.getRawQuery, uri.getRawFragment)

    def setPath(path: String): Uri =
      buildUri(uri.getScheme, uri.getRawAuthority, path, uri.getRawQuery, uri.getRawFragment)

    def setQuery(query: String): Uri =
      buildUri(uri.getScheme, uri.getRawAuthority, uri.getRawPath, query, uri.getRawFragment)

    def setFragment(fragment: String): Uri =
      buildUri(uri.getScheme, uri.getRawAuthority, uri.getRawPath, uri.getRawQuery, fragment)

    private def buildUri(scheme: String, authority: String, path: String, query: String, fragment: String): Uri =
      val uri = StringBuilder()

      if scheme != null then
        uri.append(scheme).append(":")

      if authority != null then
        uri.append("//").append(authority)

      if path != null && path != "" then
        uri.append('/').append(path.dropWhile(_ == '/'))

      if query != null && query != "" then
        uri.append('?').append(query)

      if fragment != null && fragment != "" then
        uri.append('#').append(fragment)

      Uri(uri.toString)
