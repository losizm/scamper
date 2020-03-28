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

/** Provides registered cache directives. */
case object CacheDirectives {
  /** Cache directive for `immutable`. */
  case object `immutable` extends CacheDirective {
    val name: String = "immutable"
    val value: Option[String] = None
  }

  /** Cache directive for `max-age`. */
  final case class `max-age`(seconds: Long) extends CacheDirective {
    val name: String = "max-age"
    val value: Option[String] = Some(seconds.toString)
  }

  /** Cache directive for `max-stale`. */
  final case class `max-stale`(seconds: Long) extends CacheDirective {
    val name: String = "max-stale"
    val value: Option[String] = Some(seconds.toString)
  }

  /** Cache directive for `min-fresh`. */
  final case class `min-fresh`(seconds: Long) extends CacheDirective {
    val name: String = "min-fresh"
    val value: Option[String] = Some(seconds.toString)
  }

  /** Cache directive for `must-revalidate`. */
  case object `must-revalidate` extends CacheDirective {
    val name: String = "must-revalidate"
    val value: Option[String] = None
  }

  /** Cache directive for `no-cache`. */
  case object `no-cache` extends CacheDirective {
    val name: String = "no-cache"
    val value: Option[String] = None
  }

  /** Cache directive for `no-store`. */
  case object `no-store` extends CacheDirective {
    val name: String = "no-store"
    val value: Option[String] = None
  }

  /** Cache directive for `no-transform`. */
  case object `no-transform` extends CacheDirective {
    val name: String = "no-transform"
    val value: Option[String] = None
  }

  /** Cache directive for `only-if-cached`. */
  case object `only-if-cached` extends CacheDirective {
    val name: String = "only-if-cached"
    val value: Option[String] = None
  }

  /** Cache directive for `private`. */
  case object `private` extends CacheDirective {
    val name: String = "private"
    val value: Option[String] = None
  }

  /** Cache directive for `proxy-revalidate`. */
  case object `proxy-revalidate` extends CacheDirective {
    val name: String = "proxy-revalidate"
    val value: Option[String] = None
  }

  /** Cache directive for `public`. */
  case object `public` extends CacheDirective {
    val name: String = "public"
    val value: Option[String] = None
  }

  /** Cache directive for `s-maxage`. */
  final case class `s-maxage`(seconds: Long) extends CacheDirective {
    val name: String = "s-maxage"
    val value: Option[String] = Some(seconds.toString)
  }

  /** Cache directive for `stale-if-error`. */
  final case class `stale-if-error`(seconds: Long) extends CacheDirective {
    val name: String = "stale-if-error"
    val value: Option[String] = Some(seconds.toString)
  }

  /** Cache directive for `stale-while-revalidate`. */
  final case class `stale-while-revalidate`(seconds: Long) extends CacheDirective {
    val name: String = "stale-while-revalidate"
    val value: Option[String] = Some(seconds.toString)
  }
}
