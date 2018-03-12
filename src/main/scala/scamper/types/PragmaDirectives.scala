package scamper.types

/** Registered pragma directives */
case object PragmaDirectives {
  /** Pragma directive for no-cache */
  case object `no-cache` extends PragmaDirective {
    val name: String = "no-cache"
    val value: Option[String] = None
  }
}

