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
import scamper.auxiliary.{ InputStreamType, StringType }
import scamper.headers.{ Accept, Allow, ContentLength, ContentType, Date, IfModifiedSince, LastModified }
import scamper.types.{ MediaRange, MediaType }

private class StaticResourceServer private (baseName: Path, pathPrefix: Path, loader: ClassLoader) extends RequestHandler {
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
    val mediaType = getMediaType(path)

    accept.exists(range => range.matches(mediaType)) match {
      case true =>
        val bytes = getResource(path)
        val res = Ok().withContentType(mediaType)
          .withContentLength(bytes.size)
          .withLastModified(Instant.now())

        headOnly match {
          case true  => res
          case false => res.withBody(bytes)
        }
      case false => NotAcceptable()
    }
  }

  private def getRealPath(path: String): Path =
    baseName.resolve(pathPrefix.relativize(Paths.get(path))).normalize()

  private def getExists(path: Path): Boolean =
    path.startsWith(baseName) &&
      path != Paths.get("") && {
        loader.getResource(path.toString) match {
          case null => false
          case url if url.getProtocol == "file" => new File(url.toString.stripPrefix("file:")).isFile
          case url => loader.getResource(path.toString + "/") == null
        }
      }

  private def getAccept(req: HttpRequest): Seq[MediaRange] =
    Try(req.accept) match {
      case Success(accept) if accept.nonEmpty => accept
      case _ => Seq(`*/*`)
    }

  private def getIfModifiedSince(req: HttpRequest): Instant =
    Try(req.ifModifiedSince).getOrElse(Instant.MIN)

  private def getResource(path: Path): Array[Byte] = {
    val in = loader.getResourceAsStream(path.toString)
    try in.getBytes()
    finally Try(in.close())
  }

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

private object StaticResourceServer {
  def apply(baseName: String, pathPrefix: String, loader: ClassLoader): StaticResourceServer = {
    val normBaseName = Paths.get(baseName).normalize()
    val normPathPrefix = Paths.get(pathPrefix).normalize()

    if (normBaseName != Paths.get(""))
      require(loader.getResource(normBaseName + "/") != null, s"Invalid base name: $baseName")

    require(pathPrefix.startsWith("/"), s"Invalid path prefix: $pathPrefix")

    new StaticResourceServer(normBaseName, normPathPrefix, loader)
  }
}
