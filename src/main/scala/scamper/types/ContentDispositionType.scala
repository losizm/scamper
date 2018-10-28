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
package scamper.types

import ContentDispositionTypeHelper._

/**
 * Standardized type for Content-Disposition header value.
 *
 * @see [[scamper.ImplicitHeaders.ContentDisposition]]
 */
trait ContentDispositionType {
  /** Disposition type name */
  def name: String

  /** Disposition parameters */
  def params: Map[String, String]

  /** Tests whether disposition type is attachment. */
  def isAttachment: Boolean = name == "attachment"

  /** Tests whether disposition type is inline. */
  def isInline: Boolean = name == "inline"

  /** Returns formatted content disposition type and parameters. */
  override lazy val toString: String = name + FormatParams(params)
}

/** ContentDispositionType factory */
object ContentDispositionType {
  /** Parses formatted content disposition type and optional parameters. */
  def parse(disposition: String): ContentDispositionType =
    ParseContentDisposition(disposition) match {
      case (name, params) => apply(name, params)
    }

  /** Creates ContentDispositionType with supplied name and parameters. */
  def apply(name: String, params: Map[String, String]): ContentDispositionType =
    ContentDispositionTypeImpl(Name(name), Params(params))

  /** Creates ContentDispositionType with supplied name and paramaters. */
  def apply(name: String, params: (String, String)*): ContentDispositionType =
    apply(name, params.toMap)

  /** Destructures ContentDispositionType. */
  def unapply(disposition: ContentDispositionType): Option[(String, Map[String, String])] =
    Some((disposition.name, disposition.params))
}

private case class ContentDispositionTypeImpl(name: String, params: Map[String, String]) extends ContentDispositionType
