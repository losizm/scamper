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

/**
 * Defines HTTP request method.
 *
 * @see [[RequestMethod.Registry]]
 */
sealed trait RequestMethod:
  /** Gets method name. */
  def name: String

  /** Creates `HttpRequest` with this request method and supplied target. */
  def apply(target: Uri): HttpRequest =
    HttpRequest(this, target)

  /** Creates `HttpRequest` with this request method and supplied target. */
  def apply(target: String): HttpRequest =
    apply(Uri(target))

/**
 * Provides factory for `RequestMethod`.
 *
 * @see [[RequestMethod.Registry]]
 */
object RequestMethod:
  /** Contains registered request methods. */
  object Registry:
    /** Request method for GET. */
    val Get: RequestMethod = RequestMethodImpl("GET")

    /** Request method for HEAD. */
    val Head: RequestMethod = RequestMethodImpl("HEAD")

    /** Request method for POST. */
    val Post: RequestMethod = RequestMethodImpl("POST")

    /** Request method for PUT. */
    val Put: RequestMethod = RequestMethodImpl("PUT")

    /** Request method for PATCH. */
    val Patch: RequestMethod = RequestMethodImpl("PATCH")

    /** Request method for DELETE. */
    val Delete: RequestMethod = RequestMethodImpl("DELETE")

    /** Request method for OPTIONS. */
    val Options: RequestMethod = RequestMethodImpl("OPTIONS")

    /** Request method for TRACE. */
    val Trace: RequestMethod = RequestMethodImpl("TRACE")

    /** Request method for CONNECT. */
    val Connect: RequestMethod = RequestMethodImpl("CONNECT")

  import Registry.*

  /** Gets request method for given name. */
  def apply(name: String): RequestMethod =
    name match
      case "GET"     => Get
      case "HEAD"    => Head
      case "POST"    => Post
      case "PUT"     => Put
      case "PATCH"   => Patch
      case "DELETE"  => Delete
      case "OPTIONS" => Options
      case "TRACE"   => Trace
      case "CONNECT" => Connect
      case _         => Grammar.Token(name).map(RequestMethodImpl(_)).getOrElse {
        throw IllegalArgumentException(s"Invalid request method name: $name")
      }

private case class RequestMethodImpl(name: String) extends RequestMethod:
  override val toString = name
