/*
 * Copyright 2021 Carlos Conyers
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

/**
 * Defines types for specialized access to authentication headers.
 *
 * ### Challenges and Credentials
 *
 * When working with authentication, a `Challenge` is presented in the response,
 * and `Credentials` in the request. Each of these has an assigned scheme, which
 * is associated with either a token or a set of parameters.
 *
 * {{{
 * import scala.language.implicitConversions
 *
 * import scamper.stringToUri
 * import scamper.RequestMethod.Registry.Get
 * import scamper.ResponseStatus.Registry.Unauthorized
 * import scamper.auth.{ Authorization, Challenge, Credentials, WwwAuthenticate }
 *
 * // Present response challenge (scheme and parameters)
 * val challenge = Challenge("Bearer", "realm" -> "developer")
 * val res = Unauthorized().setWwwAuthenticate(challenge)
 *
 * // Present request credentials (scheme and token)
 * val credentials = Credentials("Bearer", "QWxsIEFjY2VzcyEhIQo=")
 * val req = Get("/dev/projects").setAuthorization(credentials)
 * }}}
 *
 * ### Basic Authentication
 *
 * There are subclasses defined for Basic authentication: `BasicChallenge` and
 * `BasicCredentials`.
 *
 * {{{
 * import scala.language.implicitConversions
 *
 * import scamper.stringToUri
 * import scamper.RequestMethod.Registry.Get
 * import scamper.ResponseStatus.Registry.Unauthorized
 * import scamper.auth.{ Authorization, BasicChallenge, BasicCredentials, WwwAuthenticate }
 *
 * // Provide realm and optional parameters
 * val challenge = BasicChallenge("admin", "title" -> "Admin Console")
 * val res = Unauthorized().setWwwAuthenticate(challenge)
 *
 * // Provide user and password
 * val credentials = BasicCredentials("sa", "l3tm31n")
 * val req = Get("/admin/users").setAuthorization(credentials)
 * }}}
 *
 * In addition, there are methods for Basic authentication defined in the header
 * classes.
 *
 * {{{
 * import scala.language.implicitConversions
 *
 * import scamper.stringToUri
 * import scamper.RequestMethod.Registry.Get
 * import scamper.ResponseStatus.Registry.Unauthorized
 * import scamper.auth.{ Authorization, WwwAuthenticate }
 *
 * // Provide realm and optional parameters
 * val res = Unauthorized().setBasic("admin", "title" -> "Admin Console")
 *
 * // Access basic auth in response
 * printf(s"Realm: %s%n", res.basic.realm)
 * printf(s"Title: %s%n", res.basic.params("title"))
 *
 * // Provide user and password
 * val req = Get("/admin/users").setBasic("sa", "l3tm3m1n")
 *
 * // Access basic auth in request
 * printf(s"User: %s%n", req.basic.user)
 * printf(s"Password: %s%n", req.basic.password)
 * }}}
 *
 * ### Bearer Authentication
 *
 * There are subclasses defined for Bearer authentication: `BearerChallenge` and
 * `BearerCredentials`. In addition, there are Bearer-specific methods available
 * in the header classes.
 *
 * {{{
 * import scala.language.implicitConversions
 *
 * import scamper.stringToUri
 * import scamper.RequestMethod.Registry.Get
 * import scamper.ResponseStatus.Registry.Unauthorized
 * import scamper.auth.{ Authorization, WwwAuthenticate }
 *
 * // Provide challenge parameters
 * val res = Unauthorized().setBearer(
 *   "scope" -> "user profile",
 *   "error" -> "invalid_token",
 *   "error_description" -> "Expired access token"
 * )
 *
 * // Print optional realm parameter
 * res.bearer.realm.foreach(println)
 *
 * // Print scope from space-delimited parameter
 * val scope: Seq[String] = res.bearer.scope
 * scope.foreach(println)
 *
 * // Print error parameters
 * res.bearer.error.foreach(println)
 * res.bearer.errorDescription.foreach(println)
 * res.bearer.errorUri.foreach(println)
 *
 * // Test for error conditions
 * println(res.bearer.isInvalidToken)
 * println(res.bearer.isInvalidRequest)
 * println(res.bearer.isInsufficientScope)
 *
 * // Create request with Bearer token
 * val req = Get("/users").setBearer("R290IDUgb24gaXQhCg==")
 *
 * // Access bearer auth in request
 * printf("Token: %s%n", req.bearer.token)
 * }}}
 */
package auth

/** Converts string to [[Challenge]]. */
given stringToChallenge: Conversion[String, Challenge] with
  def apply(challenge: String) = Challenge.parse(challenge)

/** Converts string to [[Credentials]]. */
given stringToCredentials: Conversion[String, Credentials] with
  def apply(credentials: String) = Credentials.parse(credentials)

/** Provides standardized access to Authentication-Info header. */
implicit class AuthenticationInfo(response: HttpResponse) extends AnyVal:
  /** Tests for Authentication-Info header. */
  def hasAuthenticationInfo: Boolean =
    response.hasHeader("Authentication-Info")

  /**
   * Gets Authentication-Info header values.
   *
   * @return header value or empty map if Authentication-Info is not present
   */
  def authenticationInfo: Map[String, String] =
    getAuthenticationInfo.getOrElse(Map.empty)

  /** Gets Authentication-Info header value if present. */
  def getAuthenticationInfo: Option[Map[String, String]] =
    response.getHeaderValue("Authentication-Info").map(AuthParams.parse)

  /** Creates new response setting Authentication-Info header to supplied values. */
  def setAuthenticationInfo(values: Map[String, String]): HttpResponse =
    response.putHeaders(Header("Authentication-Info", AuthParams.format(values.toMap).trim))

  /** Creates new response setting Authentication-Info header to supplied values. */
  def setAuthenticationInfo(one: (String, String), more: (String, String)*): HttpResponse =
    setAuthenticationInfo((one +: more).toMap)

  /** Creates new response removing Authentication-Info header. */
  def removeAuthenticationInfo: HttpResponse =
    response.removeHeaders("Authentication-Info")

/** Provides standardized access to Authorization header. */
implicit class Authorization(request: HttpRequest) extends AnyVal:
  /** Tests for Authorization header. */
  def hasAuthorization: Boolean =
    request.hasHeader("Authorization")

  /**
   * Gets Authorization header value.
   *
   * @throws HeaderNotFound if Authorization is not present
   */
  def authorization: Credentials =
    getAuthorization.getOrElse(throw HeaderNotFound("Authorization"))

  /** Gets Authorization header value if present. */
  def getAuthorization: Option[Credentials] =
    request.getHeaderValue("Authorization").map(Credentials.parse)

  /** Creates new request setting Authorization header to supplied value. */
  def setAuthorization(value: Credentials): HttpRequest =
    request.putHeaders(Header("Authorization", value.toString))

  /** Creates new request removing Authorization header. */
  def removeAuthorization: HttpRequest =
    request.removeHeaders("Authorization")

  /** Tests for basic authorization. */
  def hasBasic: Boolean =
    getBasic.isDefined

  /**
   * Gets basic authorization.
   *
   * @throws HttpException if basic authorization is not present
   */
  def basic: BasicCredentials =
    getBasic.getOrElse(throw HttpException("Basic authorization not found"))

  /** Gets basic authorization if present. */
  def getBasic: Option[BasicCredentials] =
    getAuthorization.collect { case credentials: BasicCredentials => credentials }

  /** Creates new request with basic authorization. */
  def setBasic(token: String): HttpRequest =
    setBasic(BasicCredentials(token))

  /** Creates new request with basic authorization. */
  def setBasic(user: String, password: String): HttpRequest =
    setBasic(BasicCredentials(user, password))

  /** Creates new request with basic authorization. */
  def setBasic(credentials: BasicCredentials): HttpRequest =
    setAuthorization(credentials)

  /** Tests for bearer authorization. */
  def hasBearer: Boolean =
    getBearer.isDefined

  /**
   * Gets bearer authorization.
   *
   * @throws HttpException if bearer authorization is not present
   */
  def bearer: BearerCredentials =
    getBearer.getOrElse(throw HttpException("Bearer authorization not found"))

  /** Gets bearer authorization if present. */
  def getBearer: Option[BearerCredentials] =
    getAuthorization.collect { case credentials: BearerCredentials => credentials }

  /** Creates new request with bearer authorization. */
  def setBearer(token: String): HttpRequest =
    setBearer(BearerCredentials(token))

  /** Creates new request with bearer authorization. */
  def setBearer(credentials: BearerCredentials): HttpRequest =
    setAuthorization(credentials)

/** Provides standardized access to Proxy-Authenticate header. */
implicit class ProxyAuthenticate(response: HttpResponse) extends AnyVal:
  /** Tests for Proxy-Authenticate. */
  def hasProxyAuthenticate: Boolean =
    response.hasHeader("Proxy-Authenticate")

  /**
   * Gets Proxy-Authenticate header values.
   *
   * @return header values or empty sequence if Proxy-Authenticate is not present
   */
  def proxyAuthenticate: Seq[Challenge] =
    getProxyAuthenticate.getOrElse(Nil)

  /** Gets Proxy-Authenticate header values if present. */
  def getProxyAuthenticate: Option[Seq[Challenge]] =
    response.getHeaderValues("Proxy-Authenticate")
      .flatMap(Challenge.parseAll) match
        case Nil => None
        case seq => Some(seq)

  /** Creates new response setting Proxy-Authenticate header to supplied values. */
  def setProxyAuthenticate(values: Seq[Challenge]): HttpResponse =
    response.putHeaders(Header("Proxy-Authenticate", values.mkString(", ")))

  /** Creates new response setting Proxy-Authenticate header to supplied values. */
  def setProxyAuthenticate(one: Challenge, more: Challenge*): HttpResponse =
    setProxyAuthenticate(one +: more)

  /** Creates new response removing Proxy-Authenticate header. */
  def removeProxyAuthenticate: HttpResponse =
    response.removeHeaders("Proxy-Authenticate")

  /** Tests for basic proxy authentication. */
  def hasProxyBasic: Boolean =
    getProxyBasic.isDefined

  /**
   * Gets basic proxy authentication.
   *
   * @throws HttpException if basic proxy authentication is not present
   */
  def proxyBasic: BasicChallenge =
    getProxyBasic.getOrElse(throw HttpException("Basic proxy authentication not found"))

  /** Gets basic proxy authentication if present. */
  def getProxyBasic: Option[BasicChallenge] =
    proxyAuthenticate.collectFirst { case challenge: BasicChallenge => challenge }

  /** Creates new response with basic proxy authentication. */
  def setProxyBasic(realm: String, params: Map[String, String]): HttpResponse =
    setProxyBasic(BasicChallenge(realm, params))

  /** Creates new response with basic proxy authentication. */
  def setProxyBasic(realm: String, params: (String, String)*): HttpResponse =
    setProxyBasic(BasicChallenge(realm, params*))

  /** Creates new response with basic proxy authentication. */
  def setProxyBasic(challenge: BasicChallenge): HttpResponse =
    setProxyAuthenticate(challenge)

  /** Tests for bearer proxy authentication. */
  def hasProxyBearer: Boolean =
    getProxyBearer.isDefined

  /**
   * Gets bearer proxy authentication.
   *
   * @throws HttpException if bearer proxy authentication is not present
   */
  def proxyBearer: BearerChallenge =
    getProxyBearer.getOrElse(throw HttpException("Bearer proxy authentication not found"))

  /** Gets bearer proxy authentication if present. */
  def getProxyBearer: Option[BearerChallenge] =
    proxyAuthenticate.collectFirst { case challenge: BearerChallenge => challenge }

  /** Creates new response with bearer proxy authentication. */
  def setProxyBearer(params: Map[String, String]): HttpResponse =
    setProxyBearer(BearerChallenge(params))

  /** Creates new response with bearer proxy authentication. */
  def setProxyBearer(params: (String, String)*): HttpResponse =
    setProxyBearer(BearerChallenge(params*))

  /** Creates new response with bearer proxy authentication. */
  def setProxyBearer(challenge: BearerChallenge): HttpResponse =
    setProxyAuthenticate(challenge)

/** Provides standardized access to Proxy-Authentication-Info header. */
implicit class ProxyAuthenticationInfo(response: HttpResponse) extends AnyVal:
  /** Tests for Proxy-Authentication-Info header. */
  def hasProxyAuthenticationInfo: Boolean =
    response.hasHeader("Proxy-Authentication-Info")

  /**
   * Gets Proxy-Authentication-Info header values.
   *
   * @return header value or empty map if Proxy-Authentication-Info is not present
   */
  def proxyAuthenticationInfo: Map[String, String] =
    getProxyAuthenticationInfo.getOrElse(Map.empty)

  /** Gets Proxy-Authentication-Info header value if present. */
  def getProxyAuthenticationInfo: Option[Map[String, String]] =
    response.getHeaderValue("Proxy-Authentication-Info").map(AuthParams.parse)

  /** Creates new response setting Proxy-Authentication-Info header to supplied values. */
  def setProxyAuthenticationInfo(values: Map[String, String]): HttpResponse =
    response.putHeaders(Header("Proxy-Authentication-Info", AuthParams.format(values.toMap).trim))

  /** Creates new response setting Proxy-Authentication-Info header to supplied values. */
  def setProxyAuthenticationInfo(one: (String, String), more: (String, String)*): HttpResponse =
    setProxyAuthenticationInfo((one +: more).toMap)

  /** Creates new response removing Proxy-Authentication-Info header. */
  def removeProxyAuthenticationInfo: HttpResponse =
    response.removeHeaders("Proxy-Authentication-Info")

/** Provides standardized access to Proxy-Authorization header. */
implicit class ProxyAuthorization(request: HttpRequest) extends AnyVal:
  /** Tests for Proxy-Authorization header. */
  def hasProxyAuthorization: Boolean =
    request.hasHeader("Proxy-Authorization")

  /**
   * Gets Proxy-Authorization header value.
   *
   * @throws HeaderNotFound if Proxy-Authorization is not present
   */
  def proxyAuthorization: Credentials =
    getProxyAuthorization.getOrElse(throw HeaderNotFound("Proxy-Authorization"))

  /** Gets Proxy-Authorization header value if present. */
  def getProxyAuthorization: Option[Credentials] =
    request.getHeaderValue("Proxy-Authorization").map(Credentials.parse)

  /**
   * Creates new request setting Proxy-Authorization header to supplied value.
   */
  def setProxyAuthorization(value: Credentials): HttpRequest =
    request.putHeaders(Header("Proxy-Authorization", value.toString))

  /** Creates new request removing Proxy-Authorization header. */
  def removeProxyAuthorization: HttpRequest =
    request.removeHeaders("Proxy-Authorization")

  /** Tests for basic proxy authorization. */
  def hasProxyBasic: Boolean =
    getProxyBasic.isDefined

  /**
   * Gets basic proxy authorization.
   *
   * @throws HttpException if basic proxy authorization is not present
   */
  def proxyBasic: BasicCredentials =
    getProxyBasic.getOrElse(throw HttpException("Basic proxy authorization not found"))

  /** Gets basic proxy authorization if present. */
  def getProxyBasic: Option[BasicCredentials] =
    getProxyAuthorization.collect { case credentials: BasicCredentials => credentials }

  /** Creates new request with basic proxy authorization. */
  def setProxyBasic(token: String): HttpRequest =
    setProxyBasic(BasicCredentials(token))

  /** Creates new request with basic proxy authorization. */
  def setProxyBasic(user: String, password: String): HttpRequest =
    setProxyBasic(BasicCredentials(user, password))

  /** Creates new request with basic proxy authorization. */
  def setProxyBasic(credentials: BasicCredentials): HttpRequest =
    setProxyAuthorization(credentials)

  /** Tests for bearer proxy authorization. */
  def hasProxyBearer: Boolean =
    getProxyBearer.isDefined

  /**
   * Gets bearer proxy authorization.
   *
   * @throws HttpException if bearer proxy authorization is not present
   */
  def proxyBearer: BearerCredentials =
    getProxyBearer.getOrElse(throw HttpException("Bearer proxy authorization not found"))

  /** Gets bearer proxy authorization if present. */
  def getProxyBearer: Option[BearerCredentials] =
    getProxyAuthorization.collect { case credentials: BearerCredentials => credentials }

  /** Creates new request with bearer proxy authorization. */
  def setProxyBearer(token: String): HttpRequest =
    setProxyBearer(BearerCredentials(token))

  /** Creates new request with bearer proxy authorization. */
  def setProxyBearer(credentials: BearerCredentials): HttpRequest =
    setProxyAuthorization(credentials)

/** Provides standardized access to WWW-Authenticate header. */
implicit class WwwAuthenticate(response: HttpResponse) extends AnyVal:
  /** Tests for WWW-Authenticate header. */
  def hasWwwAuthenticate: Boolean =
    response.hasHeader("WWW-Authenticate")

  /**
   * Gets WWW-Authenticate header values.
   *
   * @return header values or empty sequence if WWW-Authenticate is not present
   */
  def wwwAuthenticate: Seq[Challenge] =
    getWwwAuthenticate.getOrElse(Nil)

  /** Gets WWW-Authenticate header values if present. */
  def getWwwAuthenticate: Option[Seq[Challenge]] =
    response.getHeaderValues("WWW-Authenticate")
      .flatMap(Challenge.parseAll) match
        case Nil => None
        case seq => Some(seq)

  /** Creates new response setting WWW-Authenticate header to supplied values. */
  def setWwwAuthenticate(values: Seq[Challenge]): HttpResponse =
    response.putHeaders(Header("WWW-Authenticate", values.mkString(", ")))

  /** Creates new response setting WWW-Authenticate header to supplied values. */
  def setWwwAuthenticate(one: Challenge, more: Challenge*): HttpResponse =
    setWwwAuthenticate(one +: more)

  /** Creates new response removing WWW-Authenticate header. */
  def removeWwwAuthenticate: HttpResponse =
    response.removeHeaders("WWW-Authenticate")

  /** Tests for basic authentication. */
  def hasBasic: Boolean =
    getBasic.isDefined

  /**
   * Gets basic authentication.
   *
   * @throws HttpException if basic authentication is not present
   */
  def basic: BasicChallenge =
    getBasic.getOrElse(throw HttpException("Basic authentication not found"))

  /** Gets basic authentication if present. */
  def getBasic: Option[BasicChallenge] =
    wwwAuthenticate.collectFirst { case challenge: BasicChallenge => challenge }

  /** Creates new response with basic authentication. */
  def setBasic(realm: String, params: Map[String, String]): HttpResponse =
    setBasic(BasicChallenge(realm, params))

  /** Creates new response with basic authentication. */
  def setBasic(realm: String, params: (String, String)*): HttpResponse =
    setBasic(BasicChallenge(realm, params*))

  /** Creates new response with basic authentication. */
  def setBasic(challenge: BasicChallenge): HttpResponse =
    setWwwAuthenticate(challenge)

  /** Tests for bearer authentication. */
  def hasBearer: Boolean =
    getBearer.isDefined

  /**
   * Gets bearer authentication.
   *
   * @throws HttpException if bearer authentication is not present
   */
  def bearer: BearerChallenge =
    getBearer.getOrElse(throw HttpException("Bearer authentication not found"))

  /** Gets bearer authentication if present. */
  def getBearer: Option[BearerChallenge] =
    wwwAuthenticate.collectFirst { case challenge: BearerChallenge => challenge }

  /** Creates new response with bearer authentication. */
  def setBearer(params: Map[String, String]): HttpResponse =
    setBearer(BearerChallenge(params))

  /** Creates new response with bearer authentication. */
  def setBearer(params: (String, String)*): HttpResponse =
    setBearer(BearerChallenge(params*))

  /** Creates new response with bearer authentication. */
  def setBearer(challenge: BearerChallenge): HttpResponse =
    setWwwAuthenticate(challenge)
