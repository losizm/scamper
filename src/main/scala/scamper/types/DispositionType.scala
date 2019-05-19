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
package scamper.types

import DispositionTypeHelper._

/**
 * Standardized type for Content-Disposition header value.
 *
 * @see [[scamper.headers.ContentDisposition]]
 */
trait DispositionType {
  /** Gets disposition type name. */
  def name: String

  /** Gets disposition parameters. */
  def params: Map[String, String]

  /** Tests whether disposition type is attachment. */
  def isAttachment: Boolean = name == "attachment"

  /** Tests whether disposition type is inline. */
  def isInline: Boolean = name == "inline"

  /** Tests whether disposition type is form-data. */
  def isFormData: Boolean = name == "form-data"

  /** Returns formatted content disposition type and parameters. */
  override lazy val toString: String = name + FormatParams(params)
}

/** DispositionType factory */
object DispositionType {
  /** Parses formatted content disposition type and optional parameters. */
  def parse(disposition: String): DispositionType =
    ParseContentDisposition(disposition) match {
      case (name, params) => apply(name, params)
    }

  /** Creates DispositionType with supplied name and parameters. */
  def apply(name: String, params: Map[String, String]): DispositionType =
    DispositionTypeImpl(Name(name), Params(params))

  /** Creates DispositionType with supplied name and paramaters. */
  def apply(name: String, params: (String, String)*): DispositionType =
    apply(name, params.toMap)

  /** Destructures DispositionType. */
  def unapply(disposition: DispositionType): Option[(String, Map[String, String])] =
    Some((disposition.name, disposition.params))
}

private case class DispositionTypeImpl(name: String, params: Map[String, String]) extends DispositionType
