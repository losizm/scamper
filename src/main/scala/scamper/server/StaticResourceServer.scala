/*
 * Copyright 2017-2020 Carlos Conyers
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
package scamper.server

import java.io.File
import java.nio.file.{ Files, Path, Paths }
import java.nio.file.attribute.BasicFileAttributes
import java.time.Instant

import scala.util.{ Failure, Success, Try }

import scamper.{ HttpRequest, HttpResponse }
import scamper.Auxiliary.{ InputStreamType, StringType }
import scamper.Implicits._
import scamper.ResponseStatus.Registry.Ok
import scamper.headers.{ Accept, Allow, ContentLength, ContentType, Date, IfModifiedSince, LastModified }
import scamper.types.{ MediaRange, MediaType }

private class StaticResourceServer(mountPath: Path, sourceDirectory: Path, classLoader: ClassLoader) extends StaticFileServer(mountPath, sourceDirectory) {
  override protected def exists(path: Path): Boolean =
    path.startsWith(sourceDirectory) &&
      path != Paths.get("") && {
        classLoader.getResource(path.toString) match {
          case null => false
          case url if url.getProtocol == "file" => new File(url.toString.stripPrefix("file:")).isFile
          case url => classLoader.getResource(path.toString + "/") == null
        }
      }

  override protected def getResponse(path: Path, mediaType: MediaType, ifModifiedSince: Instant, headOnly: Boolean): HttpResponse = {
    val bytes = getResource(path)
    val res = Ok().setContentType(mediaType)
      .setContentLength(bytes.size)
      .setLastModified(Instant.now())

    headOnly match {
      case true  => res
      case false => res.setBody(bytes)
    }
  }

  private def getResource(path: Path): Array[Byte] = {
    val in = classLoader.getResourceAsStream(path.toString)
    try in.getBytes()
    finally Try(in.close())
  }
}

private object StaticResourceServer {
  def apply(mountPath: String, sourceDirectory: String, classLoader: ClassLoader): StaticResourceServer = {
    val path = MountPath(mountPath)
    val directory = Paths.get(sourceDirectory).normalize()
    val loader = if (classLoader == null) Thread.currentThread.getContextClassLoader else classLoader

    if (directory != Paths.get(""))
      require(loader.getResource(s"$directory/") != null, s"Invalid source directory: $sourceDirectory")

    new StaticResourceServer(Paths.get(path.value), directory, loader)
  }
}
