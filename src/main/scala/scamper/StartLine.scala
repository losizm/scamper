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
sealed trait RequestLine extends StartLine {
  /** Gets request method. */
  def method: RequestMethod

  /** Gets request target. */
  def target: Uri
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

  /**
   * Creates request line.
   *
   * @note Request line is created as HTTP/1.1.
   */
  def apply(method: RequestMethod, target: Uri): RequestLine =
    RequestLineImpl(notNull(method), adjustTarget(notNull(target), method.name), HttpVersion(1, 1))

  /** Creates request line. */
  def apply(method: RequestMethod, target: Uri, version: HttpVersion): RequestLine =
    RequestLineImpl(notNull(method), adjustTarget(notNull(target), method.name), notNull(version))

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

private case class RequestLineImpl(method: RequestMethod, target: Uri, version: HttpVersion) extends RequestLine {
  override lazy val toString: String = s"$method $target HTTP/$version"
}

/**
 * Defines HTTP status line.
 *
 * @see [[RequestLine]]
 */
sealed trait StatusLine extends StartLine {
  /** Gets response status. */
  def status: ResponseStatus
}

/** Provides factory for `StatusLine`. */
object StatusLine {
  private val syntax = """HTTP/(\d+(?:\.\d+)?)\h+(\d+)(?:\h+(\p{Print}*?))?\h*""".r

  /** Parses formatted status line. */
  def apply(line: String): StatusLine =
    Try {
      line match {
        case syntax(version, statusCode, null | "")    =>
          StatusLineImpl(HttpVersion(version), ResponseStatus(statusCode.toInt))

        case syntax(version, statusCode, reasonPhrase) =>
          StatusLineImpl(HttpVersion(version), ResponseStatus(statusCode.toInt, reasonPhrase))
      }
    } getOrElse {
      throw new IllegalArgumentException(s"Malformed status line: $line")
    }

  /**
   * Creates status line.
   *
   * @note Status line is created as HTTP/1.1.
   */
  def apply(status: ResponseStatus): StatusLine =
    StatusLineImpl(HttpVersion(1, 1), notNull(status))

  /** Creates status line. */
  def apply(version: HttpVersion, status: ResponseStatus): StatusLine =
    StatusLineImpl(notNull(version), notNull(status))
}

private case class StatusLineImpl(version: HttpVersion, status: ResponseStatus) extends StatusLine {
  override lazy val toString = s"HTTP/$version ${status.statusCode} ${status.reasonPhrase}"
}
