/*
 * Copyright 2019 Carlos Conyers
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

/**
 * HTTP request method
 *
 * @see [[RequestMethod.Registry]]
 */
trait RequestMethod {
  /** Gets method name. */
  def name: String

  /** Creates `HttpRequest` with request method and supplied target. */
  def apply(target: URI): HttpRequest =
    HttpRequest(this, target, Nil, Entity.empty)

  /** Returns method name. */
  override def toString(): String = name
}

/**
 * Provided factory for `RequestMethod`.
 *
 * @see [[RequestMethod.Registry]]
 */
object RequestMethod {
  /** Contains registered HTTP request methods. */
  object Registry {
    /** GET request method */
    val GET: RequestMethod = RequestMethodImpl("GET")

    /** HEAD request method */
    val HEAD: RequestMethod = RequestMethodImpl("HEAD")

    /** POST request method */
    val POST: RequestMethod = RequestMethodImpl("POST")

    /** PUT request method */
    val PUT: RequestMethod = RequestMethodImpl("PUT")

    /** PATCH request method */
    val PATCH: RequestMethod = RequestMethodImpl("PATCH")

    /** DELETE request method */
    val DELETE: RequestMethod = RequestMethodImpl("DELETE")

    /** OPTIONS request method */
    val OPTIONS: RequestMethod = RequestMethodImpl("OPTIONS")

    /** TRACE request method */
    val TRACE: RequestMethod = RequestMethodImpl("TRACE")
  }
  import Registry._

  /** Gets `RequestMethod` for given name. */
  def apply(name: String): RequestMethod =
    name match {
      case "GET"     => GET
      case "HEAD"    => HEAD
      case "POST"    => POST
      case "PUT"     => PUT
      case "PATCH"   => PATCH
      case "DELETE"  => DELETE
      case "OPTIONS" => OPTIONS
      case "TRACE"   => TRACE
      case _         => Grammar.Token(name).map(RequestMethodImpl).getOrElse {
        throw new IllegalArgumentException(s"Invalid request method name: $name")
      }
    }

  /** Destructures `RequestMethod`. */
  def unapply(method: RequestMethod): Option[String] = Some(method.name)
}

private case class RequestMethodImpl(name: String) extends RequestMethod
