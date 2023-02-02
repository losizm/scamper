/*
 * Copyright 2023 Carlos Conyers
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

import java.net.URI

import Validate.{ noNulls, notNull }

/** Defines URI. */
sealed trait Uri:
  /** Tests whether URI is absolute. */
  def isAbsolute: Boolean

  /**
   * Gets scheme.
   *
   * @throws NoSuchElementException if scheme not specified (i.e., URI is not absolute)
   */
  def scheme: String

  /** Gets optional scheme. */
  def schemeOption: Option[String]

  /**
   * Gets authority.
   *
   * @throws NoSuchElementException if authority not specified (i.e., URI is not absolute)
   */
  def authority: String

  /** Gets optional authority. */
  def authorityOption: Option[String]

  /**
   * Gets host.
   *
   * @throws NoSuchElementException if host not specified (i.e., URI is not absolute)
   */
  def host: String

  /** Gets optional host. */
  def hostOption: Option[String]

  /**
   * Gets port.
   *
   * @throws NoSuchElementException if port not specified
   */
  def port: Int

  /** Gets optional port. */
  def portOption: Option[Int]

  /** Gets path. */
  def path: String

  /** Gets query. */
  def query: QueryString

  /** Gets fragment. */
  def fragment: String

  /** Gets fragment. */
  def fragmentOption: Option[String]

  /** Recreates URI with new path. */
  def setPath(path: String): Uri

  /** Recreates URI with new query. */
  def setQuery(query: QueryString): Uri

  /** Recreates URI with new fragment. */
  def setFragment(fragment: Option[String]): Uri

  /** Recreates URI with new fragment. */
  def setFragment(fragment: String): Uri

  /** Recreates URI with supplied scheme and authority. */
  def toAbsoluteUri(scheme: String, authority: String): Uri

  /** Recreates URI with supplied scheme, host, and port. */
  def toAbsoluteUri(scheme: String, host: String, port: Int): Uri

  /** Recreates URI with supplied scheme, host, and port. */
  def toAbsoluteUri(scheme: String, host: String, port: Option[Int]): Uri

  /** Recreates URI without scheme and authority. */
  def toRelativeUri: Uri

  /** Recreates URI without scheme, authority, and fragment. */
  def toTargetUri: Uri

  /** Converts URI to `java.net.URI`. */
  def toURI: URI

/** Provides factory for `Uri`. */
object Uri:
  /**
   * Creates normalized URI with supplied string.
   *
   * @throws IllegalArgumentException if absolute and scheme not one of http, https, wss, or ws.
   */
  def apply(uri: String): Uri =
    UriImpl(URI(uri).normalize())

private case class UriImpl(toURI: URI) extends Uri:
  val isAbsolute = toURI.isAbsolute

  val schemeOption = toURI.getScheme match
    case null    => None
    case "http"  => Some("http")
    case "https" => Some("https")
    case "ws"    => Some("ws")
    case "wss"   => Some("wss")
    case value   => throw IllegalArgumentException(s"Unsupported scheme: $value")

  if toURI.getRawUserInfo != null then
    throw IllegalArgumentException("Unsupported user information")

  val authorityOption = Option(toURI.getRawAuthority).map(_.toLowerCase)

  if schemeOption.nonEmpty && authorityOption.isEmpty then
    throw IllegalArgumentException("Supplied scheme with no authority")

  if schemeOption.isEmpty && authorityOption.nonEmpty then
    throw IllegalArgumentException("No scheme with supplied authority")

  val (hostOption, portOption) = authorityOption.map(parseAuthority).getOrElse((None, None))

  portOption.foreach { value =>
    if value < 1 || value > 65535 then
      throw IllegalArgumentException(s"Invalid port number: $value")
  }

  lazy val path = toURI.getRawPath match
    case null  => ""
    case value => value

  lazy val query = toURI.getRawQuery match
    case null | "" => QueryString.empty
    case value     => QueryString(value)

  lazy val fragmentOption = Option(toURI.getRawFragment)

  def scheme    = schemeOption.get
  def authority = authorityOption.get
  def host      = hostOption.get
  def port      = portOption.get
  def fragment  = fragmentOption.get

  def setPath(path: String) =
    buildUri(schemeOption, authorityOption, notNull(path), query, fragmentOption)

  def setQuery(query: QueryString) =
    buildUri(schemeOption, authorityOption, path, notNull(query), fragmentOption)

  def setFragment(fragment: String) =
    setFragment(Some(fragment))

  def setFragment(fragmentOption: Option[String]) =
    buildUri(schemeOption, authorityOption, path, query, noNulls(fragmentOption))

  def toAbsoluteUri(scheme: String, authority: String) =
    (notNull(scheme, "scheme").trim, notNull(authority, "authority").trim) match
      case ("", _)             => throw IllegalArgumentException("scheme is blank")
      case (_, "")             => throw IllegalArgumentException("authority is blank")
      case (scheme, authority) => buildUri(Some(scheme), Some(authority), path, query, fragmentOption)

  def toAbsoluteUri(scheme: String, host: String, port: Int) =
    toAbsoluteUri(scheme, host, Some(port))

  def toAbsoluteUri(scheme: String, host: String, portOption: Option[Int]) =
    (notNull(scheme, "scheme").trim, notNull(host, "host").trim) match
      case ("", _)        => throw IllegalArgumentException("scheme is blank")
      case (_, "")        => throw IllegalArgumentException("host is blank")
      case (scheme, host) =>
        portOption match
          case None    => buildUri(Some(scheme), Some(host), path, query, fragmentOption)
          case Some(n) => buildUri(Some(scheme), Some(s"$host:$n"), path, query, fragmentOption)

  def toRelativeUri =
    isAbsolute match
      case true  => buildUri(None, None, path, query, fragmentOption)
      case false => this

  def toTargetUri =
    isAbsolute || fragment.nonEmpty match
      case true  => buildUri(None, None, path, query, None)
      case false => this

  override lazy val toString = toURI.toASCIIString

  private def parseAuthority(authority: String): (Option[String], Option[Int]) =
    authority.split(":", 2) match
      case Array(host)       => (Some(host), None)
      case Array(host, port) => (Some(host), Some(port.toInt).filterNot(-1.==))

  private def buildUri(schemeOption: Option[String], authorityOption: Option[String], path: String, query: QueryString, fragmentOption: Option[String]): Uri =
    UriBuilder()
      .scheme(schemeOption)
      .authority(authorityOption)
      .path(path)
      .query(query)
      .fragment(fragmentOption)
      .toUri
