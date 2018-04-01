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

/**
 * Standardized type for ETag, If-Match, If-None-Match, and If-Range header
 * value.
 *
 * @see [[scamper.ImplicitHeaders.ETag]]
 * @see [[scamper.ImplicitHeaders.IfMatch]]
 * @see [[scamper.ImplicitHeaders.IfNoneMatch]]
 * @see [[scamper.ImplicitHeaders.IfRange]]
 */
trait EntityTag {
  /** Entity tag's opaque value */
  def opaque: String

  /** Test whether entity tag is weak validator. */
  def weak: Boolean

  /** Returns formatted entity tag. */
  override lazy val toString: String =
    if (weak) "W/" + opaque else opaque
}

/** EntityTag factory */
object EntityTag {
  private val syntax = """\s*(W/)?("[^"]*")\s*""".r

  /** Parse formatted entity tag. */
  def apply(tag: String): EntityTag =
    tag match {
      case syntax(weak, opaque) => EntityTagImpl(opaque, weak != null)
      case _ => throw new IllegalArgumentException(s"Malformed entity tag: $tag")
    }

  /**
   * Creates EntityTag with supplied values.
   *
   * <strong>Note:</strong> The opaque tag is automatically enclosed in
   * double-quotes if not already supplied as such.
   */
  def apply(opaque: String, weak: Boolean): EntityTag =
    if (opaque.matches("\"[^\"]*\"")) EntityTagImpl(opaque, weak)
    else if (opaque.matches("[^\"]*")) EntityTagImpl("\"" + opaque + "\"", weak)
    else throw new IllegalArgumentException(s"Invalid opaque tag: $opaque")

  /** Destructures EntityTag. */
  def unapply(tag: EntityTag): Option[(String, Boolean)] =
    Some((tag.opaque, tag.weak))
}

private case class EntityTagImpl(opaque: String, weak: Boolean) extends EntityTag

