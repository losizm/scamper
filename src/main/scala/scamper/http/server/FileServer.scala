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
package http
package server

import java.io.File
import java.nio.file.{ Files, NotDirectoryException, Path }
import java.nio.file.attribute.BasicFileAttributes
import java.time.Instant

import scala.util.Try
import scala.language.implicitConversions

import scamper.http.headers.given
import scamper.http.types.{ MediaRange, MediaType }

import RequestMethod.Registry.{ Get, Head }
import ResponseStatus.Registry.*

private class FileServer private (sourceDirectory: Path, defaults: Seq[String]) extends RouterApplication:
  def this(sourceDirectory: File, defaults: Seq[String]) =
    this(sourceDirectory.toPath.toAbsolutePath.normalize(), noNulls(defaults))

  if !Files.isDirectory(sourceDirectory) then
    throw NotDirectoryException(s"$sourceDirectory")

  private val `*/*` = MediaRange("*/*")

  def apply(router: Router): Unit =
    router.incoming("/*path", Get, Head) { req =>
      val sourceFile = toSourceFile(req.pathParams.getString("path"))

      isSafe(sourceFile) match
        case true  => getSourceFile(req, sourceFile)
        case false => req
    }

    router.incoming("/", Get, Head) { req =>
      getDefaultFile(req, sourceDirectory)
    }

  protected def isFile(path: Path): Boolean = Files.isRegularFile(path)
  protected def isDirectory(path: Path): Boolean = Files.isDirectory(path)
  protected def isSafe(path: Path): Boolean = path.startsWith(sourceDirectory) && !Files.isHidden(path)
  protected def isSafeFile(path: Path): Boolean = isFile(path) && isSafe(path)
  protected def isSafeDirectory(path: Path): Boolean = isDirectory(path) && isSafe(path)

  protected def createResponse(path: Path, mediaType: MediaType, ifModifiedSince: Instant, headOnly: Boolean): HttpResponse =
    val attrs = Files.readAttributes(path, classOf[BasicFileAttributes])
    val lastModified = attrs.lastModifiedTime.toInstant
    val size = attrs.size

    ifModifiedSince.isBefore(lastModified) match
      case true =>
        val res = Ok().setContentType(mediaType)
          .setContentLength(size)
          .setLastModified(lastModified)

        headOnly match
          case true  => res
          case false => res.setBody(path.toFile)

      case false => NotModified().setLastModified(lastModified)

  private def toSourceFile(path: String): Path =
    sourceDirectory.resolve(path.toUrlDecoded).normalize()

  private def getSourceFile(req: HttpRequest, sourceFile: Path): HttpMessage =
    if isFile(sourceFile) then
      val mediaType = getMediaType(sourceFile)

      getMediaRanges(req).exists(_.matches(mediaType)) match
        case true  => createResponse(sourceFile, mediaType, getIfModifiedSince(req), req.isHead)
        case false => NotAcceptable()

    else if isDirectory(sourceFile) then
      getDefaultFile(req, sourceFile)

    else
      req

  private def getDefaultFile(req: HttpRequest, sourceFile: Path): HttpMessage =
    defaults.find(name => isSafeFile(sourceFile.resolve(name)))
      .map(name => NormalizePath(req.path + "/" + name))
      .map(uri  => SeeOther(s"See other: $uri").setLocation(uri))
      .getOrElse(req)

  private def getMediaType(path: Path): MediaType =
    MediaType.forFileName(path.getFileName.toString)
      .getOrElse(MediaType.octetStream)

  private def getMediaRanges(req: HttpRequest): Seq[MediaRange] =
    Try(req.accept)
      .filter(_.nonEmpty)
      .getOrElse(Seq(`*/*`))

  private def getIfModifiedSince(req: HttpRequest): Instant =
    Try(req.ifModifiedSince).getOrElse(Instant.MIN)
