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

/**
 * HTTP request method
 *
 * @see [[RequestMethods]]
 */
trait RequestMethod {
  /** Gets method name. */
  def name: String

  /** Converts to HttpRequest with supplied URI and entity. */
  def apply(uri: URI, body: Entity = Entity.empty): HttpRequest =
    HttpRequest(this, uri, Nil, body)

  /** Returns method name */
  override def toString: String = name
}

/**
 * RequestMethod factory
 *
 * @see [[RequestMethods]]
 */
object RequestMethod {
  import Grammar.Token
  import RequestMethods._

  /** Gets RequestMethod for given name. */
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
      case _  =>
        Token(name).map(RequestMethodImpl).getOrElse {
          throw new IllegalArgumentException(s"Invalid request method name: $name")
        }
    }

  /** Destructures RequestMethod. */
  def unapply(method: RequestMethod): Option[String] =
    Some(method.name)
}

private case class RequestMethodImpl(name: String) extends RequestMethod
