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

/** Includes registered request methods. */
object RequestMethods {
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
