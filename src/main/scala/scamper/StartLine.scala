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
package scamper

import java.net.URI

import scala.util.Try

/** HTTP message start line */
trait StartLine {
  /** Gets HTTP version. */
  def version: HttpVersion
}

/**
 * HTTP request line
 *
 * @see [[StatusLine]]
 */
trait RequestLine extends StartLine {
  /** Gets request method. */
  def method: RequestMethod

  /** Gets request target. */
  def target: URI

  /** Returns formatted request line. */
  override lazy val toString: String = s"$method $target HTTP/$version"
}

/** RequestLine factory */
object RequestLine {
  private val syntax = """([\w!#$%&'*+.^`|~-]+)\h+(\p{Graph}+)\h+HTTP/(\d+(?:\.\d+)?)\h*""".r

  /** Parses formatted request line. */
  def parse(line: String): RequestLine =
    Try {
      line match {
        case syntax(method, target, version) => RequestLineImpl(RequestMethod(method), new URI(target), HttpVersion.parse(version))
      }
    } getOrElse {
      throw new IllegalArgumentException(s"Malformed request line: $line")
    }

  /** Creates RequestLine with supplied attributes. */
  def apply(method: RequestMethod, target: URI, version: HttpVersion = HttpVersion(1, 1)): RequestLine =
    RequestLineImpl(method, target, version)

  /** Destructures RequestLine. */
  def unapply(line: RequestLine): Option[(RequestMethod, URI, HttpVersion)] =
    Some((line.method, line.target, line.version))
}

private case class RequestLineImpl(method: RequestMethod, target: URI, version: HttpVersion) extends RequestLine

/**
 * HTTP status line
 *
 * @see [[RequestLine]]
 */
trait StatusLine extends StartLine {
  /** Gets response status. */
  def status: ResponseStatus

  /** Returns formatted status line. */
  override lazy val toString: String = s"HTTP/$version ${status.code} ${status.reason}"
}

/** StatusLine factory */
object StatusLine {
  private val syntax = """HTTP/(\d+(?:\.\d+)?)\h+(\d+)(?:\h+(\p{Print}*?))?\h*""".r

  /** Parses formatted status line. */
  def parse(line: String): StatusLine =
    Try {
      line match {
        case syntax(version, code, null | "") => StatusLineImpl(ResponseStatus(code.toInt), HttpVersion.parse(version))
        case syntax(version, code, reason)    => StatusLineImpl(ResponseStatus(code.toInt, reason), HttpVersion.parse(version))
      }
    } getOrElse {
      throw new IllegalArgumentException(s"Malformed status line: $line")
    }

  /** Creates StatusLine with supplied attributes. */
  def apply(status: ResponseStatus, version: HttpVersion = HttpVersion(1, 1)): StatusLine =
    StatusLineImpl(status, version)

  /** Destructures StatusLine. */
  def unapply(line: StatusLine): Option[(ResponseStatus, HttpVersion)] =
    Some((line.status, line.version))
}

private case class StatusLineImpl(status: ResponseStatus, version: HttpVersion) extends StatusLine
