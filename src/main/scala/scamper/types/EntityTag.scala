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
  private val syntax = """(W/)?("[^"]*")""".r

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

