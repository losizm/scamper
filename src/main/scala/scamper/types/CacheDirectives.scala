package scamper.types

/** Registered cache directives */
case object CacheDirectives {
  /** Cache directive for max-age */
  final case class `max-age`(deltaSeconds: Long) extends CacheDirective {
    val name: String = "max-age"
    val value: Option[String] = Some(deltaSeconds.toString)
  }

  /** Cache directive for max-stale */
  final case class `max-stale`(deltaSeconds: Long) extends CacheDirective {
    val name: String = "max-stale"
    val value: Option[String] = Some(deltaSeconds.toString)
  }

  /** Cache directive for min-fresh */
  final case class `min-fresh`(deltaSeconds: Long) extends CacheDirective {
    val name: String = "min-fresh"
    val value: Option[String] = Some(deltaSeconds.toString)
  }

  /** Cache directive for must-revalidate */
  case object `must-revalidate` extends CacheDirective {
    val name: String = "must-revalidate"
    val value: Option[String] = None
  }

  /** Cache directive for no-cache */
  case object `no-cache` extends CacheDirective {
    val name: String = "no-cache"
    val value: Option[String] = None
  }

  /** Cache directive for no-store */
  case object `no-store` extends CacheDirective {
    val name: String = "no-store"
    val value: Option[String] = None
  }

  /** Cache directive for no-transform */
  case object `no-transform` extends CacheDirective {
    val name: String = "no-transform"
    val value: Option[String] = None
  }

  /** Cache directive for only-if-cached */
  case object `only-if-cached` extends CacheDirective {
    val name: String = "only-if-cached"
    val value: Option[String] = None
  }

  /** Cache directive for private */
  case object `private` extends CacheDirective {
    val name: String = "private"
    val value: Option[String] = None
  }

  /** Cache directive for proxy-revalidate */
  case object `proxy-revalidate` extends CacheDirective {
    val name: String = "proxy-revalidate"
    val value: Option[String] = None
  }

  /** Cache directive for public */
  case object `public` extends CacheDirective {
    val name: String = "public"
    val value: Option[String] = None
  }

  /** Cache directive for s-maxage */
  final case class `s-maxage`(deltaSeconds: Long) extends CacheDirective {
    val name: String = "s-maxage"
    val value: Option[String] = Some(deltaSeconds.toString)
  }

  /** Cache directive for stale-if-error */
  final case class `stale-if-error`(deltaSeconds: Long) extends CacheDirective {
    val name: String = "stale-if-error"
    val value: Option[String] = Some(deltaSeconds.toString)
  }

  /** Cache directive for stale-while-revalidate */
  final case class `stale-while-revalidate`(deltaSeconds: Long) extends CacheDirective {
    val name: String = "stale-while-revalidate"
    val value: Option[String] = Some(deltaSeconds.toString)
  }
}

