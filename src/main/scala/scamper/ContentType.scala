package scamper

/**
 * Provides primary type, subtype, and parameters of HTTP content type.
 */
case class ContentType private (primaryType: String, subtype: String, parameters: Map[String, String]) {
  /** Returns formatted content type. */
  override val toString: String = s"${primaryType}/$subtype$paramsToString"

  /** Tests whether primary type is text. */
  def isText: Boolean = primaryType == "text"

  /** Tests whether primary type is audio. */
  def isAudio: Boolean = primaryType == "audio"

  /** Tests whether primary type is video. */
  def isVideo: Boolean = primaryType == "video"

  /** Tests whether primary type is application. */
  def isApplication: Boolean = primaryType == "application"

  /** Tests whether primary type is multipart. */
  def isMultipart: Boolean = primaryType == "multipart"

  /** Tests whether primary type is message. */
  def isMessage: Boolean = primaryType == "message"

  private def paramsToString: String =
    parameters.map(param => s"; ${param._1}=${quote(param._2)}").mkString

  private def quote(value: String): String =
    if (Token(value)) value
    else '"' + value + '"'
}

/** Provides ContentType factory methods. */
object ContentType {
  import bantam.nx.lang.DefaultType

  private val value = s"""(?:${Token.regex}|"([^"]+)")"""
  private val param = s"""\\s*;\\s*(${Token.regex})=($value)\\s*"""
  private val ContentTypeRegex = s"""\\s*(${Token.regex})/(${Token.regex})(($param)*)\\s*""".r

  /** Creates ContentType using supplied attributes. */
  def apply(primaryType: String, subtype: String, parameters: (String, String)*): ContentType =
    new ContentType(primaryType, subtype, parameters.toMap)

  /** Creates ContentType using supplied attributes. */
  def apply(primaryType: String, subtype: String, parameters: Map[String, String]): ContentType = {
    require(Token(primaryType), s"Invalid primary type: $primaryType")
    require(Token(subtype), s"Invalid subtype: $subtype")
    require(parameters.forall { case (name, value) => Token(name) && isValue(value) }, s"Invalid parameters: $parameters")

    new ContentType(primaryType, subtype, parameters)
  }

  /** Parses formatted content type. */
  def apply(contentType: String): ContentType =
    contentType match {
      case ContentTypeRegex(primaryType, subtype, params, _*) =>
        new ContentType(primaryType, subtype, parseParams(params))
      case _ =>
        throw new IllegalArgumentException(s"Malformed content type: $contentType")
    }

  private def isValue(s: String) =
    if (s == null) false
    else s.matches("[^\"]+")

  private def parseParams(params: String): Map[String, String] =
    param.r.findAllMatchIn(params)
      .map(m => m.group(1) -> { m.group(3) ?: m.group(2) })
      .toMap
}

