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

import java.net.URI

private implicit class UriExtensions(uri: URI) extends AnyVal:
  def toTarget: URI =
    buildUri(null, null, uri.getRawPath, uri.getRawQuery, null)

  def setScheme(scheme: String): URI =
    buildUri(scheme, uri.getRawAuthority, uri.getRawPath, uri.getRawQuery, uri.getRawFragment)

  def setAuthority(authority: String): URI =
    buildUri(uri.getScheme, authority, uri.getRawPath, uri.getRawQuery, uri.getRawFragment)

  def setPath(path: String): URI =
    buildUri(uri.getScheme, uri.getRawAuthority, path, uri.getRawQuery, uri.getRawFragment)

  def setQuery(query: String): URI =
    buildUri(uri.getScheme, uri.getRawAuthority, uri.getRawPath, query, uri.getRawFragment)

  def setFragment(fragment: String): URI =
    buildUri(uri.getScheme, uri.getRawAuthority, uri.getRawPath, uri.getRawQuery, fragment)

  private def buildUri(scheme: String, authority: String, path: String, query: String, fragment: String): URI =
    val uri = StringBuilder()

    if scheme != null then
      uri.append(scheme).append(":")

    if authority != null then
      uri.append("//").append(authority)

    if path != null && path != "" then
      uri.append('/').append(path.dropWhile(_ == '/'))

    if query != null && query != "" then
      uri.append('?').append(query)

    if fragment != null && fragment != "" then
      uri.append('#').append(fragment)

    URI(uri.toString).normalize()
