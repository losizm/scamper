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
import scamper.aux.{ InputStreamType, StringType }
import scamper.headers.{ Accept, Allow, ContentLength, ContentType, Date, IfModifiedSince, LastModified }
import scamper.types.{ MediaRange, MediaType }

private class StaticResourceServer(baseName: Path, pathPrefix: Path, loader: ClassLoader) extends StaticFileServer(baseName, pathPrefix) {
  override protected def exists(path: Path): Boolean =
    path.startsWith(baseName) &&
      path != Paths.get("") && {
        loader.getResource(path.toString) match {
          case null => false
          case url if url.getProtocol == "file" => new File(url.toString.stripPrefix("file:")).isFile
          case url => loader.getResource(path.toString + "/") == null
        }
      }

  override protected def getResponse(path: Path, mediaType: MediaType, ifModifiedSince: Instant, headOnly: Boolean): HttpResponse = {
    val bytes = getResource(path)
    val res = Ok().withContentType(mediaType)
      .withContentLength(bytes.size)
      .withLastModified(Instant.now())

    headOnly match {
      case true  => res
      case false => res.withBody(bytes)
    }
  }

  private def getResource(path: Path): Array[Byte] = {
    val in = loader.getResourceAsStream(path.toString)
    try in.getBytes()
    finally Try(in.close())
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