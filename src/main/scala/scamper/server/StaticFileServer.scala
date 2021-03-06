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

import scala.util.{ Success, Try }

import scamper.{ HttpMessage, HttpRequest, HttpResponse }
import scamper.Auxiliary.{ StringType, applicationOctetStream }
import scamper.Implicits.fileToEntity
import scamper.RequestMethod.Registry.{ Get, Head, Options }
import scamper.ResponseStatus.Registry.{ MethodNotAllowed, NotAcceptable, NotModified, Ok }
import scamper.headers.{ Accept, Allow, ContentLength, ContentType, IfModifiedSince, LastModified }
import scamper.types.{ MediaRange, MediaType }

private class StaticFileServer(mountPath: Path, sourceDirectory: Path) extends RequestHandler {
  private val `*/*` = MediaRange("*", "*")

  def apply(req: HttpRequest): HttpMessage =
    getRealPath(req.path)
      .filter(exists)
      .map { path =>
        req.method match {
          case Get | Head =>
            val mediaType = getMediaType(path)

            if (getAccept(req).exists { range => range.matches(mediaType) })
              getResponse(path, mediaType, getIfModifiedSince(req), req.isHead)
            else
              NotAcceptable()
          case Options => Ok().setAllow(Get, Head, Options).setContentLength(0)
          case _ => MethodNotAllowed().setAllow(Get, Head, Options)
        }
      }.getOrElse(req)

  protected def exists(path: Path): Boolean =
    path.startsWith(sourceDirectory) && Files.isRegularFile(path) && !Files.isHidden(path)

  protected def getResponse(path: Path, mediaType: MediaType, ifModifiedSince: Instant, headOnly: Boolean): HttpResponse = {
    val attrs = Files.readAttributes(path, classOf[BasicFileAttributes])
    val lastModified = attrs.lastModifiedTime.toInstant
    val size = attrs.size

    ifModifiedSince.isBefore(lastModified) match {
      case true =>
        val res = Ok().setContentType(mediaType)
          .setContentLength(size)
          .setLastModified(lastModified)

        headOnly match {
          case true  => res
          case false => res.setBody(path.toFile)
        }
      case false => NotModified().setLastModified(lastModified)
    }
  }

  private def getRealPath(path: String): Option[Path] = {
    val toPath = Paths.get(path.toUrlDecoded("utf-8")).normalize()

    toPath.startsWith(mountPath) match {
      case true  => Some(sourceDirectory.resolve(mountPath.relativize(toPath)))
      case false => None
    }
  }

  private def getAccept(req: HttpRequest): Seq[MediaRange] =
    Try(req.accept)
      .filter(_.nonEmpty)
      .getOrElse(Seq(`*/*`))

  private def getIfModifiedSince(req: HttpRequest): Instant =
    Try(req.ifModifiedSince).getOrElse(Instant.MIN)

  private def getMediaType(path: Path): MediaType =
    MediaType.forFileName(path.getFileName.toString)
      .getOrElse(applicationOctetStream)
}

private object StaticFileServer {
  def apply(mountPath: String, sourceDirectory: File): StaticFileServer = {
    val path = MountPath(mountPath)
    val directory = sourceDirectory.toPath.toAbsolutePath.normalize()

    require(Files.isDirectory(directory), s"Not a directory ($directory)")

    new StaticFileServer(Paths.get(path.value), directory)
  }
}
