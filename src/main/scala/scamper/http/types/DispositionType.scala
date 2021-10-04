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
package types

import DispositionTypeHelper.*

/**
 * Standardized type for Content-Disposition header value.
 *
 * @see [[scamper.http.headers.ContentDisposition]]
 */
trait DispositionType:
  /** Gets name. */
  def name: String

  /** Gets parameters. */
  def params: Map[String, String]

  /** Tests for attachment. */
  def isAttachment: Boolean = name == "attachment"

  /** Tests for inline. */
  def isInline: Boolean = name == "inline"

  /** Tests for form-data. */
  def isFormData: Boolean = name == "form-data"

  /** Returns formatted disposition. */
  override lazy val toString: String = name + FormatParams(params)

/** Provides factory for `DispositionType`. */
object DispositionType:
  /** Parses formatted disposition. */
  def parse(disposition: String): DispositionType =
    ParseContentDisposition(disposition) match
      case (name, params) => apply(name, params)

  /** Creates disposition with supplied name and parameters. */
  def apply(name: String, params: Map[String, String]): DispositionType =
    DispositionTypeImpl(Name(name), Params(params))

  /** Creates disposition with supplied name and paramaters. */
  def apply(name: String, params: (String, String)*): DispositionType =
    apply(name, params.toMap)

private case class DispositionTypeImpl(name: String, params: Map[String, String]) extends DispositionType
