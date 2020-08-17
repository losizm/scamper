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
package scamper

import scala.util.Try

import Validate.notNull

/** Defines HTTP message start line. */
sealed trait StartLine {
  /** Gets HTTP version. */
  def version: HttpVersion
}

/**
 * Defines HTTP request line.
 *
 * @see [[StatusLine]]
 */
trait RequestLine extends StartLine {
  /** Gets request method. */
  def method: RequestMethod

  /** Gets request target. */
  def target: Uri

  /** Returns formatted request line. */
  override lazy val toString: String = s"$method $target HTTP/$version"
}

/** Provides factory for `RequestLine`. */
object RequestLine {
  private val syntax = """([\w!#$%&'*+.^`|~-]+)\h+(\p{Graph}+)\h+HTTP/(\d+(?:\.\d+)?)\h*""".r

  /** Parses formatted request line. */
  def apply(line: String): RequestLine =
    Try {
      line match {
        case syntax(method, target, version) => RequestLineImpl(RequestMethod(method), Uri(target), HttpVersion(version))
      }
    } getOrElse {
      throw new IllegalArgumentException(s"Malformed request line: $line")
    }

  /** Creates request line with supplied parts. */
  def apply(method: RequestMethod, target: Uri, version: HttpVersion = HttpVersion(1, 1)): RequestLine =
    RequestLineImpl(notNull(method), adjustTarget(notNull(target), method.name), notNull(version))

  /** Destructures request line. */
  def unapply(line: RequestLine): Option[(RequestMethod, Uri, HttpVersion)] =
    Some((line.method, line.target, line.version))

  private def adjustTarget(target: Uri, method: String): Uri =
    target.isAbsolute match {
      case true  => target
      case false =>
        target.toString match {
          case "" =>
            if (method == "OPTIONS")
              Uri("*")
            else
              Uri("/")

          case uri if uri.startsWith("/") => target
          case uri if uri.startsWith("*") => target
          case uri =>
            if (method == "OPTIONS" && (uri.startsWith("?") || uri.startsWith("#")))
              Uri("*" + uri)
            else
              Uri("/" + uri)
        }
    }
}

private case class RequestLineImpl(method: RequestMethod, target: Uri, version: HttpVersion) extends RequestLine

/**
 * Defines HTTP status line.
 *
 * @see [[RequestLine]]
 */
trait StatusLine extends StartLine {
  /** Gets response status. */
  def status: ResponseStatus

  /** Returns formatted status line. */
  override lazy val toString: String = s"HTTP/$version ${status.code} ${status.reason}"
}

/** Provides factory for `StatusLine`. */
object StatusLine {
  private val syntax = """HTTP/(\d+(?:\.\d+)?)\h+(\d+)(?:\h+(\p{Print}*?))?\h*""".r

  /** Parses formatted status line. */
  def apply(line: String): StatusLine =
    Try {
      line match {
        case syntax(version, code, null | "") => StatusLineImpl(ResponseStatus(code.toInt), HttpVersion(version))
        case syntax(version, code, reason)    => StatusLineImpl(ResponseStatus(code.toInt, reason), HttpVersion(version))
      }
    } getOrElse {
      throw new IllegalArgumentException(s"Malformed status line: $line")
    }

  /** Creates status line with supplied parts. */
  def apply(status: ResponseStatus, version: HttpVersion = HttpVersion(1, 1)): StatusLine =
    StatusLineImpl(notNull(status), notNull(version))

  /** Destructures status line. */
  def unapply(line: StatusLine): Option[(ResponseStatus, HttpVersion)] =
    Some((line.status, line.version))
}

private case class StatusLineImpl(status: ResponseStatus, version: HttpVersion) extends StatusLine
