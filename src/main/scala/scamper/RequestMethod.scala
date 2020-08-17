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

/**
 * Defines HTTP request method.
 *
 * @see [[RequestMethod.Registry]]
 */
trait RequestMethod {
  /** Gets method name. */
  def name: String

  /** Creates `HttpRequest` with this request method and supplied target. */
  def apply(target: Uri): HttpRequest =
    HttpRequest(this, target, Nil, Entity.empty)

  /** Returns formatted request method. */
  override lazy val toString: String = name
}

/**
 * Provides factory for `RequestMethod`.
 *
 * @see [[RequestMethod.Registry]]
 */
object RequestMethod {
  /** Contains registered request methods. */
  object Registry {
    /** GET */
    val GET: RequestMethod = RequestMethodImpl("GET")

    /** HEAD */
    val HEAD: RequestMethod = RequestMethodImpl("HEAD")

    /** POST */
    val POST: RequestMethod = RequestMethodImpl("POST")

    /** PUT */
    val PUT: RequestMethod = RequestMethodImpl("PUT")

    /** PATCH */
    val PATCH: RequestMethod = RequestMethodImpl("PATCH")

    /** DELETE */
    val DELETE: RequestMethod = RequestMethodImpl("DELETE")

    /** OPTIONS */
    val OPTIONS: RequestMethod = RequestMethodImpl("OPTIONS")

    /** TRACE */
    val TRACE: RequestMethod = RequestMethodImpl("TRACE")

    /** CONNECT */
    val CONNECT: RequestMethod = RequestMethodImpl("CONNECT")
  }
  import Registry._

  /** Gets request method for given name. */
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
      case "CONNECT" => CONNECT
      case _         => Grammar.Token(name).map(RequestMethodImpl).getOrElse {
        throw new IllegalArgumentException(s"Invalid request method name: $name")
      }
    }

  /** Destructures request method. */
  def unapply(method: RequestMethod): Option[String] = Some(method.name)
}

private case class RequestMethodImpl(name: String) extends RequestMethod
