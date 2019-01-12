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
package scamper

/**
 * Provides access to authethentication types and headers.
 *
 * === Challenges and Credentials ===
 *
 * When working with authentication, you present a `Challenge` in the response
 * and `Credentials` in the request. Each of these has an assigned scheme, which
 * is associated with either a token or a set of parameters.
 *
 * {{{
 * import scamper.ImplicitConverters.stringToUri
 * import scamper.RequestMethods.GET
 * import scamper.ResponseStatuses.Unauthorized
 * import scamper.auth.{ Authorization, Challenge, Credentials, WwwAuthenticate }
 *
 * // Present response challenge (scheme and parameters)
 * val challenge = Challenge("Bearer", "realm" -> "developer")
 * val res = Unauthorized().withWwwAuthenticate(challenge)
 *
 * // Present request credentials (scheme and token)
 * val credentials = Credentials("Bearer", "QWxsIEFjY2VzcyEhIQo=")
 * val req = GET("/dev/projects").withAuthorization(credentials)
 * }}}
 *
 * === Basic Authentication ===
 *
 * There are subclasses defined for Basic authentication: `BasicChallenge` and
 * `BasicCredentials`.
 *
 * {{{
 * import scamper.ImplicitConverters.stringToUri
 * import scamper.RequestMethods.GET
 * import scamper.ResponseStatuses.Unauthorized
 * import scamper.auth.{ Authorization, BasicChallenge, BasicCredentials, WwwAuthenticate }
 *
 * // Provide realm and optional parameters
 * val challenge = BasicChallenge("admin", "title" -> "Admin Console")
 * val res = Unauthorized().withWwwAuthenticate(challenge)
 *
 * // Provide user and password
 * val credentials = BasicCredentials("sa", "l3tm31n")
 * val req = GET("/admin/users").withAuthorization(credentials)
 * }}}
 *
 * In addition, there are convenience methods available for Basic authentication.
 *
 * {{{
 * import scamper.ImplicitConverters.stringToUri
 * import scamper.RequestMethods.GET
 * import scamper.ResponseStatuses.Unauthorized
 * import scamper.auth.{ Authorization, WwwAuthenticate }
 *
 * // Provide realm and optional parameters
 * val res = Unauthorized().withBasic("admin", "title" -> "Admin Console")
 *
 * // Access basic auth in response
 * printf(s"Realm: %s%n", res.basic.realm)
 * printf(s"Title: %s%n", res.basic.params("title"))
 *
 * // Provide user and password
 * val req = GET("/admin/users").withBasic("sa", "l3tm3m1n")
 *
 * // Access basic auth in request
 * printf(s"User: %s%n", req.basic.user)
 * printf(s"Password: %s%n", req.basic.password)
 * }}}
 *
 * === Bearer Authentication ===
 *
 * `BearerChallenge` and `BearerCredentials` are provided for Bearer
 * authentication.
 *
 * {{{
 * import scamper.ImplicitConverters.stringToUri
 * import scamper.RequestMethods.GET
 * import scamper.ResponseStatuses.Unauthorized
 * import scamper.auth.{ Authorization, BearerChallenge, WwwAuthenticate }
 *
 * // Provide parameters
 * val challenge = BearerChallenge(
 *   "scope" -> "user profile",
 *   "error" -> "invalid_token",
 *   "error_description" -> "Expired access token"
 * )
 * val res = Unauthorized().withWwwAuthenticate(challenge)
 *
 * // Print optional realm parameter
 * res.bearer.realm.foreach(println)
 *
 * // Print scope from space-delimited parameter
 * val scope: Seq[String] = res.bearer.scope
 * scope.foreach(println)
 *
 * // Print error parameters
 * res.bearer.realm.foreach(println)
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
 * val req = GET("/users").withBearer("R290IDUgb24gaXQhCg==")
 * }}}
 */
package object auth {
  /** Converts string to [[Challenge]]. */
  implicit val stringToChallenge = (challenge: String) => Challenge.parse(challenge)

  /** Converts string to [[Credentials]]. */
  implicit val stringToCredentials = (credentials: String) => Credentials.parse(credentials)

  /** Provides standardized access to Authentication-Info header. */
  implicit class AuthenticationInfo(val response: HttpResponse) extends AnyVal {
    /**
     * Gets Authentication-Info header values.
     *
     * @return header values or empty sequence if Authentication-Info is not present
     */
    def authenticationInfo: Map[String, String] = getAuthenticationInfo.getOrElse(Map.empty)

    /** Gets Authentication-Info header values if present. */
    def getAuthenticationInfo: Option[Map[String, String]] =
      response.getHeaderValue("Authentication-Info").map(AuthParams.parse)

    /** Tests whether Authentication-Info header is present. */
    def hasAuthenticationInfo: Boolean = response.hasHeader("Authentication-Info")

    /**
     * Creates new response setting Authentication-Info header to supplied
     * values.
     */
    def withAuthenticationInfo(values: (String, String)*): HttpResponse =
      response.withHeader(Header("Authentication-Info", AuthParams.format(values.toMap).trim))

    /** Creates new response removing Authentication-Info header. */
    def removeAuthenticationInfo(): HttpResponse = response.removeHeaders("Authentication-Info")
  }

  /** Provides standardized access to Authorization header. */
  implicit class Authorization(val request: HttpRequest) extends AnyVal {
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

    /** Tests whether Authorization header is present. */
    def hasAuthorization: Boolean = request.hasHeader("Authorization")

    /** Creates new request setting Authorization header to supplied value. */
    def withAuthorization(value: Credentials): HttpRequest =
      request.withHeader(Header("Authorization", value.toString))

    /** Creates new request removing Authorization header. */
    def removeAuthorization(): HttpRequest = request.removeHeaders("Authorization")

    /**
     * Gets basic authorization.
     *
     * @throws HttpException if basic authorization is not present
     */
    def basic: BasicCredentials =
      getBasic.getOrElse(throw new HttpException("Basic authorization not found"))

    /** Gets basic authorization if present. */
    def getBasic: Option[BasicCredentials] =
      getAuthorization.collect { case challenge: BasicCredentials => challenge }

    /** Tests whether basic authorization is present. */
    def hasBasic: Boolean = getBasic.isDefined

    /** Creates new request with basic authorization. */
    def withBasic(token: String): HttpRequest =
      withBasic(BasicCredentials(token))

    /** Creates new request with basic authorization. */
    def withBasic(user: String, password: String): HttpRequest =
      withBasic(BasicCredentials(user, password))

    /** Creates new request with basic authorization. */
    def withBasic(credentials: BasicCredentials): HttpRequest =
      withAuthorization(credentials)

    /**
     * Gets bearer authorization.
     *
     * @throws HttpException if bearer authorization is not present
     */
    def bearer: BearerCredentials =
      getBearer.getOrElse(throw new HttpException("Bearer authorization not found"))

    /** Gets bearer authorization if present. */
    def getBearer: Option[BearerCredentials] =
      getAuthorization.collect { case challenge: BearerCredentials => challenge }

    /** Tests whether bearer authorization is present. */
    def hasBearer: Boolean = getBearer.isDefined

    /** Creates new request with bearer authorization. */
    def withBearer(token: String): HttpRequest =
      withBearer(BearerCredentials(token))

    /** Creates new request with bearer authorization. */
    def withBearer(credentials: BearerCredentials): HttpRequest =
      withAuthorization(credentials)
  }

  /** Provides standardized access to Proxy-Authenticate header. */
  implicit class ProxyAuthenticate(val response: HttpResponse) extends AnyVal {
    /**
     * Gets Proxy-Authenticate header values.
     *
     * @return header values or empty sequence if Proxy-Authenticate is not present
     */
    def proxyAuthenticate: Seq[Challenge] = getProxyAuthenticate.getOrElse(Nil)

    /** Gets Proxy-Authenticate header values if present. */
    def getProxyAuthenticate: Option[Seq[Challenge]] =
      response.getHeaderValues("Proxy-Authenticate")
        .flatMap(Challenge.parseAll) match {
          case Nil => None
          case seq => Some(seq)
        }

    /** Tests whether Proxy-Authenticate header is present. */
    def hasProxyAuthenticate: Boolean = response.hasHeader("Proxy-Authenticate")

    /**
     * Creates new response setting Proxy-Authenticate header to supplied
     * values.
     */
    def withProxyAuthenticate(values: Challenge*): HttpResponse =
      response.withHeader(Header("Proxy-Authenticate", values.mkString(", ")))

    /** Creates new response removing Proxy-Authenticate header. */
    def removeProxyAuthenticate(): HttpResponse = response.removeHeaders("Proxy-Authenticate")

    /**
     * Gets basic proxy authentication.
     *
     * @throws HttpException if basic proxy authentication is not present
     */
    def proxyBasic: BasicChallenge =
      getProxyBasic.getOrElse(throw new HttpException("Basic proxy authentication not found"))

    /** Gets basic proxy authentication if present. */
    def getProxyBasic: Option[BasicChallenge] =
      proxyAuthenticate.collectFirst { case challenge: BasicChallenge => challenge }

    /** Tests whether basic proxy authentication is present. */
    def hasProxyBasic: Boolean = getProxyBasic.isDefined

    /** Creates new response with basic proxy authentication. */
    def withProxyBasic(realm: String, params: (String, String)*): HttpResponse =
      withProxyBasic(BasicChallenge(realm, params : _*))

    /** Creates new response with basic proxy authentication. */
    def withProxyBasic(challenge: BasicChallenge): HttpResponse =
      withProxyAuthenticate(challenge)

    /**
     * Gets bearer proxy authentication.
     *
     * @throws HttpException if bearer proxy authentication is not present
     */
    def proxyBearer: BearerChallenge =
      getProxyBearer.getOrElse(throw new HttpException("Bearer proxy authentication not found"))

    /** Gets bearer proxy authentication if present. */
    def getProxyBearer: Option[BearerChallenge] =
      proxyAuthenticate.collectFirst { case challenge: BearerChallenge => challenge }

    /** Tests whether bearer proxy authentication is present. */
    def hasProxyBearer: Boolean = getProxyBearer.isDefined

    /** Creates new response with bearer proxy authentication. */
    def withProxyBearer(params: (String, String)*): HttpResponse =
      withProxyBearer(BearerChallenge(params : _*))

    /** Creates new response with bearer proxy authentication. */
    def withProxyBearer(challenge: BearerChallenge): HttpResponse =
      withProxyAuthenticate(challenge)
  }

  /** Provides standardized access to Proxy-Authentication-Info header. */
  implicit class ProxyAuthenticationInfo(val response: HttpResponse) extends AnyVal {
    /**
     * Gets Proxy-Authentication-Info header values.
     *
     * @return header values or empty sequence if Proxy-Authentication-Info is not present
     */
    def proxyAuthenticationInfo: Map[String, String] =
      getProxyAuthenticationInfo.getOrElse(Map.empty)

    /** Gets Proxy-Authentication-Info header values if present. */
    def getProxyAuthenticationInfo: Option[Map[String, String]] =
      response.getHeaderValue("Proxy-Authentication-Info").map(AuthParams.parse)

    /** Tests whether Proxy-Authentication-Info header is present. */
    def hasProxyAuthenticationInfo: Boolean = response.hasHeader("Proxy-Authentication-Info")

    /**
     * Creates new response setting Proxy-Authentication-Info header to supplied
     * value.
     */
    def withProxyAuthenticationInfo(values: (String, String)*): HttpResponse =
      response.withHeader(Header("Proxy-Authentication-Info", AuthParams.format(values.toMap).trim))

    /** Creates new response removing Proxy-Authentication-Info header. */
    def removeProxyAuthenticationInfo(): HttpResponse =
      response.removeHeaders("Proxy-Authentication-Info")
  }

  /** Provides standardized access to Proxy-Authorization header. */
  implicit class ProxyAuthorization(val request: HttpRequest) extends AnyVal {
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

    /** Tests whether Proxy-Authorization header is present. */
    def hasProxyAuthorization: Boolean = request.hasHeader("Proxy-Authorization")

    /**
     * Creates new request setting Proxy-Authorization header to supplied value.
     */
    def withProxyAuthorization(value: Credentials): HttpRequest =
      request.withHeader(Header("Proxy-Authorization", value.toString))

    /** Creates new request removing Proxy-Authorization header. */
    def removeProxyAuthorization(): HttpRequest = request.removeHeaders("Proxy-Authorization")

    /**
     * Gets basic proxy authorization.
     *
     * @throws HttpException if basic proxy authorization is not present
     */
    def proxyBasic: BasicCredentials =
      getProxyBasic.getOrElse(throw new HttpException("Basic proxy authorization not found"))

    /** Gets basic proxy authorization if present. */
    def getProxyBasic: Option[BasicCredentials] =
      getProxyAuthorization.collect { case challenge: BasicCredentials => challenge }

    /** Tests whether basic proxy authorization is present. */
    def hasProxyBasic: Boolean = getProxyBasic.isDefined

    /** Creates new request with basic proxy authorization. */
    def withProxyBasic(token: String): HttpRequest =
      withProxyBasic(BasicCredentials(token))

    /** Creates new request with basic proxy authorization. */
    def withProxyBasic(user: String, password: String): HttpRequest =
      withProxyBasic(BasicCredentials(user, password))

    /** Creates new request with basic proxy authorization. */
    def withProxyBasic(credentials: BasicCredentials): HttpRequest =
      withProxyAuthorization(credentials)

    /**
     * Gets bearer proxy authorization.
     *
     * @throws HttpException if bearer proxy authorization is not present
     */
    def proxyBearer: BearerCredentials =
      getProxyBearer.getOrElse(throw new HttpException("Bearer proxy authorization not found"))

    /** Gets bearer proxy authorization if present. */
    def getProxyBearer: Option[BearerCredentials] =
      getProxyAuthorization.collect { case challenge: BearerCredentials => challenge }

    /** Tests whether bearer proxy authorization is present. */
    def hasProxyBearer: Boolean = getProxyBearer.isDefined

    /** Creates new request with bearer proxy authorization. */
    def withProxyBearer(token: String): HttpRequest =
      withProxyBearer(BearerCredentials(token))

    /** Creates new request with bearer proxy authorization. */
    def withProxyBearer(credentials: BearerCredentials): HttpRequest =
      withProxyAuthorization(credentials)
  }

  /** Provides standardized access to WWW-Authenticate header. */
  implicit class WwwAuthenticate(val response: HttpResponse) extends AnyVal {
    /**
     * Gets WWW-Authenticate header values.
     *
     * @return header values or empty sequence if WWW-Authenticate is not present
     */
    def wwwAuthenticate: Seq[Challenge] = getWwwAuthenticate.getOrElse(Nil)

    /** Gets WWW-Authenticate header values if present. */
    def getWwwAuthenticate: Option[Seq[Challenge]] =
      response.getHeaderValues("WWW-Authenticate")
        .flatMap(Challenge.parseAll) match {
          case Nil => None
          case seq => Some(seq)
        }

    /** Tests whether WWW-Authenticate header is present. */
    def hasWwwAuthenticate: Boolean = response.hasHeader("WWW-Authenticate")

    /**
     * Creates new response setting WWW-Authenticate header to supplied values.
     */
    def withWwwAuthenticate(values: Challenge*): HttpResponse =
      response.withHeader(Header("WWW-Authenticate", values.mkString(", ")))

    /** Creates new response removing WWW-Authenticate header. */
    def removeWwwAuthenticate(): HttpResponse = response.removeHeaders("WWW-Authenticate")

    /**
     * Gets basic authentication.
     *
     * @throws HttpException if basic authentication is not present
     */
    def basic: BasicChallenge =
      getBasic.getOrElse(throw new HttpException("Basic authentication not found"))

    /** Gets basic authentication if present. */
    def getBasic: Option[BasicChallenge] =
      wwwAuthenticate.collectFirst { case challenge: BasicChallenge => challenge }

    /** Tests whether basic authentication is present. */
    def hasBasic: Boolean = getBasic.isDefined

    /** Creates new response with basic authentication. */
    def withBasic(realm: String, params: (String, String)*): HttpResponse =
      withBasic(BasicChallenge(realm, params : _*))

    /** Creates new response with basic authentication. */
    def withBasic(challenge: BasicChallenge): HttpResponse =
      withWwwAuthenticate(challenge)

    /**
     * Gets bearer authentication.
     *
     * @throws HttpException if bearer authentication is not present
     */
    def bearer: BearerChallenge =
      getBearer.getOrElse(throw new HttpException("Bearer authentication not found"))

    /** Gets bearer authentication if present. */
    def getBearer: Option[BearerChallenge] =
      wwwAuthenticate.collectFirst { case challenge: BearerChallenge => challenge }

    /** Tests whether bearer authentication is present. */
    def hasBearer: Boolean = getBearer.isDefined

    /** Creates new response with bearer authentication. */
    def withBearer(params: (String, String)*): HttpResponse =
      withBearer(BearerChallenge(params : _*))

    /** Creates new response with bearer authentication. */
    def withBearer(challenge: BearerChallenge): HttpResponse =
      withWwwAuthenticate(challenge)
  }
}
