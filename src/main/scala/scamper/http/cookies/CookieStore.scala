/*
 * Copyright 2023 Carlos Conyers
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
package scamper
package http
package cookies

import java.time.Instant

import scala.collection.mutable.ArrayBuffer
import scala.util.Try

/**
 * Provides utilities for persistent cookies.
 *
 * @see [[CookieStore$.apply CookieStore.apply]],
 *  [[CookieStore$.Null CookieStore.Null]]
 */
sealed trait CookieStore:
  /** Gets number of cookies in cookie store. */
  def size: Int

  /** Lists all persistent cookies. */
  def list: Seq[PersistentCookie]

  /**
   * Clears all persistent cookies.
   *
   * @param expiredOnly specifies whether expired cookies only are cleared
   *
   * @return this cookie store
   */
  def clear(expiredOnly: Boolean = false): this.type

  /**
   * Gets cookies that should be used in request to supplied target.
   *
   * @param target request URI
   *
   * @throws java.lang.IllegalArgumentException if target is not HTTP or
   * WebSocket URI
   */
  def get(target: Uri): Seq[PlainCookie]

  /**
   * Adds cookies that are in response to request of supplied target.
   *
   * @param target request URI
   * @param cookies response cookies
   *
   * @throws java.lang.IllegalArgumentException if target is not HTTP or
   * WebSocket URI
   *
   * @return this cookie store
   */
  def put(target: Uri, cookies: Seq[SetCookie]): this.type

  /**
   * Adds cookies that are in response to request of supplied target.
   *
   * @param target request URI
   * @param one response cookie
   * @param more additional response cookies
   *
   * @throws java.lang.IllegalArgumentException if target is not HTTP or
   * WebSocket URI
   *
   * @return this cookie store
   */
  def put(target: Uri, one: SetCookie, more: SetCookie*): this.type =
    put(target, one +: more)

/** Provides factory for `CookieStore`. */
object CookieStore:
  /**
   * A cookie store that effectively does nothing.
   *
   * It does not add cookies with `put`, nor does it retrieve any with `get`.
   */
  object Null extends CookieStore:
    /** Returns `0`. */
    def size = 0

    /** Returns `Nil`. */
    def list: Seq[PersistentCookie] = Nil

    /** Does nothing; returns `this`. */
    def clear(expiredOnly: Boolean) =  this

    /** Returns `Nil`. */
    def get(target: Uri): Seq[PlainCookie] = Nil

    /** Does nothing; returns `this`. */
    def put(target: Uri, cookies: Seq[SetCookie]) = this

    /** Does nothing; returns `this`. */
    override def put(target: Uri, one: SetCookie, more: SetCookie*) = this

  /**
   * Creates cookie store with initial collection of cookies.
   *
   * @param cookies initial collection of cookies
   */
  def apply(cookies: Seq[PersistentCookie] = Nil): CookieStore =
    DefaultCookieStore(new ArrayBuffer ++= cookies)

private class DefaultCookieStore(private var collection: ArrayBuffer[PersistentCookie]) extends CookieStore:
  private type Key = Tuple3[String, String, String]

  private val ordering: Ordering[PersistentCookie] =
    Ordering.by(cookie => (-cookie.path.size, cookie.creation))

  def size: Int = synchronized { collection.size }

  def list: Seq[PersistentCookie] = synchronized {
    collection.toSeq
  }

  def clear(expiredOnly: Boolean = false): this.type = synchronized {
    expiredOnly match
      case true  => collection = collection.filter(checkExpiry)
      case false => collection.clear()
    this
  }

  def get(target: Uri): Seq[PlainCookie] = synchronized {
    validate(target)

    collection
      .filter(checkExpiry)
      .filter(checkDomain(_, target))
      .filter(checkPath(_, target))
      .filter(checkSecure(_, target))
      .sorted(ordering)
      .map(_.touch().toPlainCookie)
      .toSeq
  }

  def put(target: Uri, cookies: Seq[SetCookie]): this.type = synchronized {
    validate(target)

    cookies.foreach { cookie =>
      create(cookie, target)
        .filter(checkNotPublicSuffix)
        .filter(it => domainMatches(it.domain, target.host))
        .foreach(store)
    }

    this
  }

  private def validate(target: Uri): Uri =
    require(target.isAbsolute, "target is not absolute")
    require(target.scheme.matches("(http|ws)s?"), "invalid target scheme")
    target

  private def key(cookie: PersistentCookie): Key =
    (cookie.name, cookie.domain, cookie.path)

  private def create(cookie: SetCookie, target: Uri): Try[PersistentCookie] =
    Try {
      PersistentCookieImpl(
        cookie.name,
        cookie.value,
        hostOnly = cookie.domain
          .map(_.stripPrefix("."))
          .forall(_ == ""),
        domain = cookie.domain
          .map(_.stripPrefix("."))
          .getOrElse(target.host)
          .toLowerCase,
        path = cookie.path
          .filter(_.startsWith("/"))
          .getOrElse(target.path),
        secureOnly = cookie.secure,
        httpOnly = cookie.httpOnly,
        persistent = cookie.maxAge.isDefined || cookie.expires.isDefined,
        expiry = cookie.maxAge
          .map(Instant.now().plusSeconds)
          .orElse(cookie.expires)
          .getOrElse(Instant.MAX)
      )
    }

  private def store(cookie: PersistentCookie): Unit =
    remove(cookie)
      .map(update(_, cookie))
      .orElse(Some(cookie))
      .foreach(collection.+=)

  private def remove(cookie: PersistentCookie): Option[PersistentCookie] =
    collection.indexWhere(key(_) == key(cookie)) match
      case -1 => None
      case i  => Some(collection.remove(i))

  private def update(oldCookie: PersistentCookie, newCookie: PersistentCookie): PersistentCookie =
    newCookie.asInstanceOf[PersistentCookieImpl].copy(creation = oldCookie.creation)

  private def checkNotPublicSuffix(cookie: PersistentCookie): Boolean =
    !PublicSuffixList.check(cookie.domain)

  private def checkDomain(cookie: PersistentCookie, target: Uri): Boolean =
    cookie.hostOnly match
      case true  => cookie.domain.equalsIgnoreCase(target.host)
      case false => domainMatches(cookie.domain, target.host)

  private def checkPath(cookie: PersistentCookie, target: Uri): Boolean =
    pathMatches(cookie.path, if target.path == "" then "/" else target.path)

  private def checkSecure(cookie: PersistentCookie, target: Uri): Boolean =
    cookie.secureOnly match
      case true  => target.scheme.matches("(http|ws)s")
      case false => true

  private def checkExpiry(cookie: PersistentCookie): Boolean =
    cookie.expiry.isAfter(Instant.now())

  private def domainMatches(cookieDomain: String, requestDomain: String): Boolean =
    cookieDomain == requestDomain ||
      (requestDomain.endsWith("." + cookieDomain) &&
        !requestDomain.matches("\\d{1,3}+(.\\d{1,3}+){3}"))

  private def pathMatches(cookiePath: String, requestPath: String): Boolean =
    cookiePath == requestPath ||
      (cookiePath.endsWith("/") match
        case true  => requestPath.startsWith(cookiePath)
        case false => requestPath.startsWith(cookiePath + "/")
      )
