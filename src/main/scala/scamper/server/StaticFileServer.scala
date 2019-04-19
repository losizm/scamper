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
package scamper.server

import java.io.File
import java.nio.file.{ Files, Path, Paths }
import java.nio.file.attribute.BasicFileAttributes
import java.time.Instant

import scala.util.{ Success, Try }

import scamper.{ HttpRequest, HttpResponse }
import scamper.Auxiliary.{ StringType, `application/octet-stream` }
import scamper.Implicits.fileToEntity
import scamper.RequestMethods.{ GET, HEAD, OPTIONS }
import scamper.ResponseStatuses.{ MethodNotAllowed, NotAcceptable, NotModified, Ok }
import scamper.headers.{ Accept, Allow, ContentLength, ContentType, IfModifiedSince, LastModified }
import scamper.types.{ MediaRange, MediaType }

private class StaticFileServer(mointPoint: Path, sourceDirectory: Path) extends RequestHandler {
  private val `*/*` = MediaRange("*", "*")

  def apply(req: HttpRequest): Either[HttpRequest, HttpResponse] =
    getRealPath(req.path)
      .filter(exists)
      .map { path =>
        req.method match {
          case method @ (GET | HEAD) =>
            val mediaType = getMediaType(path)

            if (getAccept(req).exists { range => range.matches(mediaType) })
              getResponse(path, mediaType, getIfModifiedSince(req), method == HEAD)
            else
              NotAcceptable()
          case OPTIONS => Ok().withAllow(GET, HEAD, OPTIONS).withContentLength(0)
          case _ => MethodNotAllowed().withAllow(GET, HEAD, OPTIONS)
        }
      }.map(Right(_)).getOrElse(Left(req))

  protected def exists(path: Path): Boolean =
    path.startsWith(sourceDirectory) && Files.isRegularFile(path) && !Files.isHidden(path)

  protected def getResponse(path: Path, mediaType: MediaType, ifModifiedSince: Instant, headOnly: Boolean): HttpResponse = {
    val attrs = Files.readAttributes(path, classOf[BasicFileAttributes])
    val lastModified = attrs.lastModifiedTime.toInstant
    val size = attrs.size

    ifModifiedSince.isBefore(lastModified) match {
      case true =>
        val res = Ok().withContentType(mediaType)
          .withContentLength(size)
          .withLastModified(lastModified)

        headOnly match {
          case true  => res
          case false => res.withBody(path.toFile)
        }
      case false => NotModified().withLastModified(lastModified)
    }
  }

  private def getRealPath(path: String): Option[Path] = {
    val toPath = Paths.get(path.toUrlDecoded("utf-8")).normalize()

    toPath.startsWith(mointPoint) match {
      case true  => Some(sourceDirectory.resolve(mointPoint.relativize(toPath)))
      case false => None
    }
  }

  private def getAccept(req: HttpRequest): Seq[MediaRange] =
    Try(req.accept) match {
      case Success(accept) if accept.nonEmpty => accept
      case _ => Seq(`*/*`)
    }

  private def getIfModifiedSince(req: HttpRequest): Instant =
    Try(req.ifModifiedSince).getOrElse(Instant.MIN)

  private def getMediaType(path: Path): MediaType =
    MediaType.fromFileName(path.getFileName.toString)
      .getOrElse(`application/octet-stream`)
}

private object StaticFileServer {
  def apply(mointPoint: String, sourceDirectory: File): StaticFileServer = {
    val path = Paths.get(mointPoint).normalize()
    val directory = sourceDirectory.toPath.toAbsolutePath.normalize()

    require(path.startsWith("/"), s"Invalid mount point ($path)")
    require(Files.isDirectory(directory), s"Not a directory ($directory)")

    new StaticFileServer(path, directory)
  }
}
