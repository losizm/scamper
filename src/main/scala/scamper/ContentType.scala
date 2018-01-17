package scamper

/**
 * Provides the primary type, subtype, and parameters of a content type (i.e.,
 * MIME type).
 */
case class ContentType private (primaryType: String, subtype: String, parameters: Map[String, String]) {
  /** Returns a canonically formatted MIME type. */
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
    if (ContentType.isToken(value)) value else '"' + value + '"'
}

/** ContentType factory */
object ContentType {
  import bantam.nx.lang.DefaultType

  private val token = """[\w!#$%&'*+.^`{}|~-]+"""
  private val value = s"""(?:$token|"([^"]+)")"""
  private val param = s"""\\s*;\\s*($token)=($value)\\s*"""
  private val ContentTypeRegex = s"""\\s*($token)/($token)(($param)*)\\s*""".r

  /** Creates a ContentType using the given attributes. */
  def apply(primaryType: String, subtype: String, parameters: (String, String)*): ContentType =
    new ContentType(primaryType, subtype, parameters.toMap)

  /** Creates a ContentType using the given attributes. */
  def apply(primaryType: String, subtype: String, parameters: Map[String, String]): ContentType = {
    require(isToken(primaryType), s"Invalid primary type: $primaryType")
    require(isToken(subtype), s"Invalid subtype: $subtype")
    require(parameters.forall { case (name, value) => isToken(name) && isValue(value) }, s"Invalid parameters: $parameters")

    new ContentType(primaryType, subtype, parameters)
  }

  /** Parses the content type. */
  def apply(contentType: String): ContentType =
    contentType match {
      case ContentTypeRegex(primaryType, subtype, params, _*) =>
        ContentType(primaryType, subtype, parseParams(params))
      case _ =>
        throw new IllegalArgumentException(s"Invalid content type: $contentType")
    }

  private def isToken(s: String) = s.matches(token)
  private def isValue(s: String) = s.matches("[^\"]+")

  private def parseParams(params: String): Map[String, String] =
    param.r.findAllMatchIn(params)
      .map(m => m.group(1) -> { m.group(3) ?: m.group(2) })
      .toMap
}

