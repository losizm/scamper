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
package scamper.types

/**
 * Standardized type for ETag, If-Match, If-None-Match, and If-Range header
 * value.
 *
 * @see [[scamper.headers.ETag]]
 * @see [[scamper.headers.IfMatch]]
 * @see [[scamper.headers.IfNoneMatch]]
 * @see [[scamper.headers.IfRange]]
 */
trait EntityTag {
  /** Gets entity tag's opaque value. */
  def opaque: String

  /** Tests for weak validator. */
  def weak: Boolean

  /** Returns formatted entity tag. */
  override lazy val toString: String =
    if (weak) "W/" + opaque else opaque
}

/** Provides factory for `EntityTag`. */
object EntityTag {
  private val syntax = """\s*(W/)?("[^"]*")\s*""".r

  /** Parses formatted tag. */
  def parse(tag: String): EntityTag =
    tag match {
      case syntax(weak, opaque) => EntityTagImpl(opaque, weak != null)
      case _ => throw new IllegalArgumentException(s"Malformed entity tag: $tag")
    }

  /**
   * Creates tag with supplied values.
   *
   * @note The opaque tag is automatically enclosed in double-quotes if not
   * already supplied as such.
   */
  def apply(opaque: String, weak: Boolean): EntityTag =
    if (opaque.matches("\"[^\"]*\"")) EntityTagImpl(opaque, weak)
    else if (opaque.matches("[^\"]*")) EntityTagImpl("\"" + opaque + "\"", weak)
    else throw new IllegalArgumentException(s"Invalid opaque tag: $opaque")

  /** Destructures tag. */
  def unapply(tag: EntityTag): Option[(String, Boolean)] =
    Some((tag.opaque, tag.weak))
}

private case class EntityTagImpl(opaque: String, weak: Boolean) extends EntityTag
