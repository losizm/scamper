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
package cookies

private object CookieGrammar:
  private val CookieValue = Grammar("([\\x21-\\x7E&&[^\",;\\\\]]*)".r)
  private val QuotedCookieValue = Grammar("\"([\\x21-\\x7E&&[^\",;\\\\]]*)\"".r)

  def Name(name: String): String =
    Grammar.Token(name) getOrElse {
      throw IllegalArgumentException(s"Invalid cookie name: $name")
    }

  def Value(value: String): String =
    CookieValue(value) orElse QuotedCookieValue(value) getOrElse {
      throw IllegalArgumentException(s"Invalid cookie value: $value")
    }
