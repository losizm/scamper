package scamper

import ContentDispositionTypeHelper._

/**
 * Content Disposition
 *
 * @see [[ImplicitHeaders.ContentDisposition]]
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
  
  /** Returns formatted content disposition. */
  override lazy val toString: String = name + FormatParams(params)
}

/** ContentDispositionType factory */
object ContentDispositionType {
  /** Parses formatted content disposition. */
  def apply(disposition: String): ContentDispositionType =
    ParseContentDisposition(disposition) match {
      case (name, params) => apply(name, params)
    }

  /** Creates ContentDispositionType with supplied values. */
  def apply(name: String, params: Map[String, String]): ContentDispositionType =
    new ContentDispositionTypeImpl(Name(name), Params(params))

  /** Creates ContentDispositionType with supplied values. */
  def apply(name: String, params: (String, String)*): ContentDispositionType =
    apply(name, params.toMap)

  /** Destructures ContentDispositionType. */
  def unapply(disposition: ContentDispositionType): Option[(String, Map[String, String])] =
    Some((disposition.name, disposition.params))
}

private class ContentDispositionTypeImpl(val name: String, val params: Map[String, String]) extends ContentDispositionType

