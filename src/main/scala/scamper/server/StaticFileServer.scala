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

import scamper.{ HttpRequest, HttpResponse }
import scamper.ImplicitConverters._
import scamper.RequestMethods.{ GET, HEAD }
import scamper.ResponseStatuses.{ MethodNotAllowed, NotAcceptable, NotModified, Ok }
import scamper.auxiliary.StringType
import scamper.headers.{ Accept, Allow, ContentLength, ContentType, Date, IfModifiedSince, LastModified }
import scamper.types.{ MediaRange, MediaType }

private class StaticFileServer private (val baseDirectory: Path, val pathPrefix: Path) extends RequestHandler {
  private val `application/octet-stream` = MediaType("application", "octet-stream")
  private val `*/*` = MediaRange("*", "*")

  def apply(req: HttpRequest): Either[HttpRequest, HttpResponse] = {
    val path = Paths.get(req.path.toUrlDecoded("utf-8")).normalize()

    if (path.startsWith(pathPrefix))
      handle(req)
    else
      Left(req)
  }

  private def handle(req: HttpRequest): Either[HttpRequest, HttpResponse] = {
    val path = getRealPath(req.path.toUrlDecoded("utf-8"))

    if (getExists(path))
      req.method match {
        case GET  => Right(getResponse(path, false, getAccept(req), getIfModifiedSince(req)))
        case HEAD => Right(getResponse(path, true, getAccept(req), getIfModifiedSince(req)))
        case _    => Right(MethodNotAllowed().withAllow(GET, HEAD))
      }
    else
      Left(req)
  }

  private def getResponse(path: Path, headOnly: Boolean, accept: Seq[MediaRange], ifModifiedSince: Instant): HttpResponse = {
    val attrs = Files.readAttributes(path, classOf[BasicFileAttributes])
    val lastModified = attrs.lastModifiedTime.toInstant
    val size = attrs.size
    val mediaType = getMediaType(path)

    accept.exists(range => range.matches(mediaType)) match {
      case true =>
        ifModifiedSince.isBefore(lastModified) match {
          case true =>
            val res = Ok().withDate(Instant.now())
              .withContentType(mediaType)
              .withContentLength(size)
              .withLastModified(lastModified)

            headOnly match {
              case true  => res
              case false => res.withBody(path.toFile)
            }
          case false =>
            NotModified().withDate(Instant.now())
              .withLastModified(lastModified)
        }
      case false => NotAcceptable().withDate(Instant.now())
    }
  }

  private def getRealPath(path: String): Path =
    baseDirectory.resolve(pathPrefix.relativize(Paths.get(path))).normalize()

  private def getExists(path: Path): Boolean =
    path.startsWith(baseDirectory) && Files.isRegularFile(path) && !Files.isHidden(path)

  private def getAccept(req: HttpRequest): Seq[MediaRange] =
    Try(req.accept) match {
      case Success(accept) if accept.nonEmpty => accept
      case _ => Seq(`*/*`)
    }

  private def getIfModifiedSince(req: HttpRequest): Instant =
    Try(req.ifModifiedSince).getOrElse(Instant.MIN)

  private def getMediaType(path: Path): MediaType =
    getFileNameExtension(path)
      .flatMap(MediaType.get)
      .getOrElse(`application/octet-stream`)

  private def getFileNameExtension(path: Path): Option[String] = {
    val namePattern = ".+\\.(\\w+)".r

    path.getFileName().toString match {
      case namePattern(ext) => Some(ext)
      case _ => None
    }
  }
}

private object StaticFileServer {
  def apply(baseDirectory: String, pathPrefix: String): StaticFileServer =
    apply(Paths.get(baseDirectory), Paths.get(pathPrefix))

  def apply(baseDirectory: File, pathPrefix: String): StaticFileServer =
    apply(baseDirectory.toPath, Paths.get(pathPrefix))

  def apply(baseDirectory: Path, pathPrefix: Path): StaticFileServer = {
    if (!Files.isDirectory(baseDirectory))
      throw new IllegalArgumentException(s"Not a directory ($baseDirectory)")

    if (!pathPrefix.startsWith("/"))
      throw new IllegalArgumentException(s"Invalid path prefix ($pathPrefix)")

    new StaticFileServer(baseDirectory.toAbsolutePath().normalize(), pathPrefix)
  }
}
