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
import scala.util.matching.Regex

private class Grammar(syntax: Regex) {
  def apply(value: String): Option[String] =
    Try(value match { case syntax(first, _*) => first }).toOption
}

private object Grammar {
  val Token = new Grammar("([\\w!#$%&'*+.^`|~-]+)".r)
  val Token68 = new Grammar("([\\w.~/+-]+=*)".r)
  val QuotedString = new Grammar("\"([\\x20-\\x7E&&[^\"]]*)\"".r)
  val QuotableString = new Grammar("([\\x20-\\x7E&&[^\"]]*)".r)
  val HeaderValue = new Grammar("(\\p{Print}*)".r)
  val FoldedHeaderValue = new Grammar("((?:\\p{Print}*(?:\r\n|\r|\n)[ \t]+\\p{Print}*)*)".r)
}
