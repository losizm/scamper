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

/**
 * Implementation of `RequestProcessor` that serves static files from
 * a base directory.
 */
class FileServer private (val baseDirectory: Path) extends RequestProcessor {
  private val `application/octet-stream` = MediaType("application", "octet-stream")

  def process(req: HttpRequest): HttpResponse = {
    val path = getEffectivePath(req.path)

    if (getExists(path))
      req.method match {
        case GET  => getResponse(path, false, getIfModifiedSince(req))
        case HEAD => getResponse(path, true, getIfModifiedSince(req))
        case _    => MethodNotAllowed().withAllow(GET, HEAD)
      }
    else
      NotFound()
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

  private def getEffectivePath(path: String): Path = {
    val realPath = getRealPath(Paths.get(path))

    if (Files.isDirectory(realPath))
      realPath.resolve("index.html")
    else
      realPath
  }

  private def getRealPath(path: Path): Path =
    path.getNameCount match {
      case 0 => baseDirectory
      case n => baseDirectory.resolve(path.subpath(0, n)).normalize()
    }

  private def getFileNameExtension(path: Path): Option[String] = {
    val namePattern = ".+\\.(\\w+)".r

    path.getFileName().toString match {
      case namePattern(ext) => Some(ext)
      case _ => None
    }
  }
}

/** Provides factory for `FileServer`. */
object FileServer {
  /** Creates `FileServer` at given base directory. */
  def apply(baseDirectory: String): FileServer =
    apply(Paths.get(baseDirectory))

  /** Creates `FileServer` at given base directory. */
  def apply(baseDirectory: File): FileServer =
    apply(baseDirectory.toPath)

  /** Creates `FileServer` at given base directory. */
  def apply(baseDirectory: Path): FileServer =
    Files.isDirectory(baseDirectory) match {
      case true  => new FileServer(baseDirectory.toAbsolutePath().normalize())
      case false => throw new IllegalArgumentException("Not a directory")
    }
}
