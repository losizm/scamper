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

import scala.util.{ Failure, Success, Try }

import scamper._
import scamper.ImplicitConverters._
import scamper.RequestMethods._
import scamper.ResponseStatuses._
import scamper.auxiliary._
import scamper.cookies._
import scamper.headers._
import scamper.types._
import scamper.types._
import scamper.types.ImplicitConverters._

private class FileServer private (val baseDirectory: Path, val pathPrefix: Path) extends RequestHandler {
  private val `application/octet-stream` = MediaType("application", "octet-stream")

  def apply(req: HttpRequest): Either[HttpRequest, HttpResponse] = {
    val path = Paths.get(req.path).normalize()

    if (path.startsWith(pathPrefix))
      handle(req)
    else
      Left(req)
  }

  private def handle(req: HttpRequest): Either[HttpRequest, HttpResponse] = {
    val path = getRealPath(req.path)

    if (getExists(path))
      req.method match {
        case GET  => Right(getResponse(path, false, getIfModifiedSince(req)))
        case HEAD => Right(getResponse(path, true, getIfModifiedSince(req)))
        case _    => Right(MethodNotAllowed().withAllow(GET, HEAD))
      }
    else
      Left(req)
  }

  private def getResponse(path: Path, headOnly: Boolean, ifModifiedSince: Instant): HttpResponse = {
    val attrs = Files.readAttributes(path, classOf[BasicFileAttributes])

    ifModifiedSince.isBefore(attrs.lastModifiedTime.toInstant) match {
      case true =>
        val res = Ok().withDate(Instant.now())
          .withContentType(getMediaType(path))
          .withContentLength(attrs.size)
          .withLastModified(attrs.lastModifiedTime.toInstant)

        headOnly match {
          case true  => res
          case false => res.withBody(path.toFile)
        }
      case false =>
        NotModified().withDate(Instant.now())
          .withLastModified(attrs.lastModifiedTime.toInstant)
    }
  }

  private def getIfModifiedSince(req: HttpRequest): Instant =
    Try(req.ifModifiedSince).getOrElse(Instant.MIN)

  private def getMediaType(path: Path): MediaType =
    getFileNameExtension(path)
      .flatMap(MediaType.get)
      .getOrElse(`application/octet-stream`)

  private def getExists(path: Path): Boolean =
    path.startsWith(baseDirectory) && Files.isRegularFile(path) && !Files.isHidden(path)

  private def getRealPath(path: String): Path = {
    val realPath = getRealPath(Paths.get(path))

    if (Files.isDirectory(realPath))
      realPath.resolve("index.html")
    else
      realPath
  }

  private def getRealPath(path: Path): Path =
    baseDirectory.resolve(pathPrefix.relativize(path)).normalize()

  private def getFileNameExtension(path: Path): Option[String] = {
    val namePattern = ".+\\.(\\w+)".r

    path.getFileName().toString match {
      case namePattern(ext) => Some(ext)
      case _ => None
    }
  }
}

private object FileServer {
  def apply(baseDirectory: String, pathPrefix: String): FileServer =
    apply(Paths.get(baseDirectory), Paths.get(pathPrefix))

  def apply(baseDirectory: File, pathPrefix: String): FileServer =
    apply(baseDirectory.toPath, Paths.get(pathPrefix))

  def apply(baseDirectory: Path, pathPrefix: Path): FileServer = {
    if (!Files.isDirectory(baseDirectory))
      throw new IllegalArgumentException("Not a directory")

    if (!pathPrefix.startsWith("/"))
      throw new IllegalArgumentException(s"Invalid path prefix: $pathPrefix")

    new FileServer(baseDirectory.toAbsolutePath().normalize(), pathPrefix)
  }
}
