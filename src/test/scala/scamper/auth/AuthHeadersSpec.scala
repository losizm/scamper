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
package scamper.auth

import scala.language.implicitConversions

import scamper.{ HeaderNotFound, HttpException }
import scamper.Implicits.stringToUri
import scamper.RequestMethod.Registry.Get
import scamper.ResponseStatus.Registry.Unauthorized

class AuthHeadersSpec extends org.scalatest.flatspec.AnyFlatSpec:
  it should "create response with Authentication-Info header" in {
    val res1 = Unauthorized()
    assert(!res1.hasAuthenticationInfo)
    assert(res1.authenticationInfo.isEmpty)
    assert(res1.getAuthenticationInfo.isEmpty)
    assert(res1.removeAuthenticationInfo == res1)

    val res2 = res1.setAuthenticationInfo("realm" -> "test", "title" -> "Test Realm")
    assert(res2.hasAuthenticationInfo)
    assert(res2.authenticationInfo == Map("realm" -> "test", "title" -> "Test Realm"))
    assert(res2.getAuthenticationInfo.contains(Map("realm" -> "test", "title" -> "Test Realm")))
    assert(res2.removeAuthenticationInfo == res1)
    assert(res2.getHeaderValue("Authentication-Info").contains("realm=\"test\", title=\"Test Realm\""))
  }

  it should "create request with Authorization header" in {
    val req1 = Get("/")
    assert(!req1.hasAuthorization)
    assertThrows[HeaderNotFound](req1.authorization)
    assert(req1.getAuthorization.isEmpty)
    assert(req1.removeAuthorization == req1)
    assert(!req1.hasBasic)
    assertThrows[HttpException](req1.basic)
    assert(req1.getBasic.isEmpty)
    assert(!req1.hasBearer)
    assertThrows[HttpException](req1.bearer)
    assert(req1.getBearer.isEmpty)

    val req2 = req1.setAuthorization("Basic bHVwaXRhOmxldG1laW4=")
    assert(req2.hasAuthorization)
    assert(req2.authorization == BasicCredentials("lupita", "letmein"))
    assert(req2.getAuthorization.contains(BasicCredentials("lupita", "letmein")))
    assert(req2.removeAuthorization == req1)
    assert(req2.getHeaderValue("Authorization").contains("Basic bHVwaXRhOmxldG1laW4="))
    assert(req2.basic == BasicCredentials("lupita", "letmein"))
    assert(req2.getBasic.contains(BasicCredentials("lupita", "letmein")))
    assert(!req2.hasBearer)
    assertThrows[HttpException](req2.bearer)
    assert(req2.getBearer.isEmpty)

    val req3 = req1.setAuthorization("Bearer bHVwaXRhOmxldG1laW4=")
    assert(req3.hasAuthorization)
    assert(req3.authorization == BearerCredentials("bHVwaXRhOmxldG1laW4="))
    assert(req3.getAuthorization.contains(BearerCredentials("bHVwaXRhOmxldG1laW4=")))
    assert(req3.removeAuthorization == req1)
    assert(req3.getHeaderValue("Authorization").contains("Bearer bHVwaXRhOmxldG1laW4="))
    assertThrows[HttpException](req1.basic)
    assert(req1.getBasic.isEmpty)
    assert(req3.hasBearer)
    assert(req3.bearer == BearerCredentials("bHVwaXRhOmxldG1laW4="))
    assert(req3.getBearer.contains(BearerCredentials("bHVwaXRhOmxldG1laW4=")))

    val req4 = req1.setBasic("lupita", "letmein")
    assert(req4.hasAuthorization)
    assert(req4.authorization == BasicCredentials("lupita", "letmein"))
    assert(req4.getAuthorization.contains(BasicCredentials("lupita", "letmein")))
    assert(req4.removeAuthorization == req1)
    assert(req4.getHeaderValue("Authorization").contains("Basic bHVwaXRhOmxldG1laW4="))
    assert(req4.basic == BasicCredentials("lupita", "letmein"))
    assert(req4.getBasic.contains(BasicCredentials("lupita", "letmein")))
    assert(!req4.hasBearer)
    assertThrows[HttpException](req4.bearer)
    assert(req4.getBearer.isEmpty)

    val req5 = req1.setBearer("bHVwaXRhOmxldG1laW4=")
    assert(req5.hasAuthorization)
    assert(req5.authorization == BearerCredentials("bHVwaXRhOmxldG1laW4="))
    assert(req5.getAuthorization.contains(BearerCredentials("bHVwaXRhOmxldG1laW4=")))
    assert(req5.removeAuthorization == req1)
    assert(req5.getHeaderValue("Authorization").contains("Bearer bHVwaXRhOmxldG1laW4="))
    assertThrows[HttpException](req1.basic)
    assert(req1.getBasic.isEmpty)
    assert(req5.hasBearer)
    assert(req5.bearer == BearerCredentials("bHVwaXRhOmxldG1laW4="))
    assert(req5.getBearer.contains(BearerCredentials("bHVwaXRhOmxldG1laW4=")))
  }

  it should "create response with Proxy-Authenticate header" in {
    val res1 = Unauthorized()
    assert(!res1.hasProxyAuthenticate)
    assert(res1.proxyAuthenticate.isEmpty)
    assert(res1.getProxyAuthenticate.isEmpty)
    assert(res1.removeProxyAuthenticate == res1)
    assert(!res1.hasProxyBasic)
    assertThrows[HttpException](res1.proxyBasic)
    assert(res1.getProxyBasic.isEmpty)
    assert(!res1.hasProxyBearer)
    assertThrows[HttpException](res1.proxyBearer)
    assert(res1.getProxyBearer.isEmpty)

    val res2 = res1.setProxyAuthenticate("Basic realm=test, title=\"Test Realm\"")
    assert(res2.hasProxyAuthenticate)
    assert(res2.proxyAuthenticate == Seq(BasicChallenge("test", "title" -> "Test Realm")))
    assert(res2.getProxyAuthenticate.contains(Seq(BasicChallenge("test", "title" -> "Test Realm"))))
    assert(res2.removeProxyAuthenticate == res1)
    assert(res2.getHeaderValue("Proxy-Authenticate").contains("Basic realm=\"test\", title=\"Test Realm\""))
    assert(res2.proxyBasic == BasicChallenge("test", "title" -> "Test Realm"))
    assert(res2.getProxyBasic.contains(BasicChallenge("test", "title" -> "Test Realm")))
    assert(!res2.hasProxyBearer)
    assertThrows[HttpException](res2.proxyBearer)
    assert(res2.getProxyBearer.isEmpty)

    val res3 = res1.setProxyAuthenticate("Bearer realm=\"test\", title=\"Test Realm\"")
    assert(res3.hasProxyAuthenticate)
    assert(res3.proxyAuthenticate == Seq(BearerChallenge("realm" -> "test", "title" -> "Test Realm")))
    assert(res3.getProxyAuthenticate.contains(Seq(BearerChallenge("realm" -> "test", "title" -> "Test Realm"))))
    assert(res3.removeProxyAuthenticate == res1)
    assert(res3.getHeaderValue("Proxy-Authenticate").contains("Bearer realm=\"test\", title=\"Test Realm\""))
    assertThrows[HttpException](res3.proxyBasic)
    assert(res3.getProxyBasic.isEmpty)
    assert(res3.hasProxyBearer)
    assert(res3.proxyBearer == BearerChallenge("realm" -> "test", "title" -> "Test Realm"))
    assert(res3.getProxyBearer.contains(BearerChallenge("realm" -> "test", "title" -> "Test Realm")))

    val res4 = res1.setProxyBasic("test", "title" -> "Test Realm")
    assert(res4.hasProxyAuthenticate)
    assert(res4.proxyAuthenticate == Seq(BasicChallenge("test", "title" -> "Test Realm")))
    assert(res4.getProxyAuthenticate.contains(Seq(BasicChallenge("test", "title" -> "Test Realm"))))
    assert(res4.removeProxyAuthenticate == res1)
    assert(res4.getHeaderValue("Proxy-Authenticate").contains("Basic realm=\"test\", title=\"Test Realm\""))
    assert(res4.proxyBasic == BasicChallenge("test", "title" -> "Test Realm"))
    assert(res4.getProxyBasic.contains(BasicChallenge("test", "title" -> "Test Realm")))
    assert(!res4.hasProxyBearer)
    assertThrows[HttpException](res4.proxyBearer)
    assert(res4.getProxyBearer.isEmpty)

    val res5 = res1.setProxyBearer("realm" -> "test", "title" -> "Test Realm")
    assert(res5.hasProxyAuthenticate)
    assert(res5.proxyAuthenticate == Seq(BearerChallenge("realm" -> "test", "title" -> "Test Realm")))
    assert(res5.getProxyAuthenticate.contains(Seq(BearerChallenge("realm" -> "test", "title" -> "Test Realm"))))
    assert(res5.removeProxyAuthenticate == res1)
    assert(res5.getHeaderValue("Proxy-Authenticate").contains("Bearer realm=\"test\", title=\"Test Realm\""))
    assertThrows[HttpException](res5.proxyBasic)
    assert(res5.getProxyBasic.isEmpty)
    assert(res5.hasProxyBearer)
    assert(res5.proxyBearer == BearerChallenge("realm" -> "test", "title" -> "Test Realm"))
    assert(res5.getProxyBearer.contains(BearerChallenge("realm" -> "test", "title" -> "Test Realm")))
  }

  it should "create response with Proxy-Authentication-Info header" in {
    val res1 = Unauthorized()
    assert(!res1.hasProxyAuthenticationInfo)
    assert(res1.proxyAuthenticationInfo.isEmpty)
    assert(res1.getProxyAuthenticationInfo.isEmpty)
    assert(res1.removeProxyAuthenticationInfo == res1)

    val res2 = res1.setProxyAuthenticationInfo("realm" -> "test", "title" -> "Test Realm")
    assert(res2.hasProxyAuthenticationInfo)
    assert(res2.proxyAuthenticationInfo == Map("realm" -> "test", "title" -> "Test Realm"))
    assert(res2.getProxyAuthenticationInfo.contains(Map("realm" -> "test", "title" -> "Test Realm")))
    assert(res2.removeProxyAuthenticationInfo == res1)
    assert(res2.getHeaderValue("Proxy-Authentication-Info").contains("realm=\"test\", title=\"Test Realm\""))
  }

  it should "create request with Proxy-Authorization header" in {
    val req1 = Get("/")
    assert(!req1.hasProxyAuthorization)
    assertThrows[HeaderNotFound](req1.proxyAuthorization)
    assert(req1.getProxyAuthorization.isEmpty)
    assert(req1.removeProxyAuthorization == req1)
    assert(!req1.hasProxyBasic)
    assertThrows[HttpException](req1.proxyBasic)
    assert(req1.getProxyBasic.isEmpty)
    assert(!req1.hasProxyBearer)
    assertThrows[HttpException](req1.proxyBearer)
    assert(req1.getProxyBearer.isEmpty)

    val req2 = req1.setProxyAuthorization("Basic bHVwaXRhOmxldG1laW4=")
    assert(req2.hasProxyAuthorization)
    assert(req2.proxyAuthorization == BasicCredentials("lupita", "letmein"))
    assert(req2.getProxyAuthorization.contains(BasicCredentials("lupita", "letmein")))
    assert(req2.removeProxyAuthorization == req1)
    assert(req2.getHeaderValue("Proxy-Authorization").contains("Basic bHVwaXRhOmxldG1laW4="))
    assert(req2.proxyBasic == BasicCredentials("lupita", "letmein"))
    assert(req2.getProxyBasic.contains(BasicCredentials("lupita", "letmein")))
    assert(!req2.hasProxyBearer)
    assertThrows[HttpException](req2.proxyBearer)
    assert(req2.getProxyBearer.isEmpty)

    val req3 = req1.setProxyAuthorization("Bearer bHVwaXRhOmxldG1laW4=")
    assert(req3.hasProxyAuthorization)
    assert(req3.proxyAuthorization == BearerCredentials("bHVwaXRhOmxldG1laW4="))
    assert(req3.getProxyAuthorization.contains(BearerCredentials("bHVwaXRhOmxldG1laW4=")))
    assert(req3.removeProxyAuthorization == req1)
    assert(req3.getHeaderValue("Proxy-Authorization").contains("Bearer bHVwaXRhOmxldG1laW4="))
    assertThrows[HttpException](req1.proxyBasic)
    assert(req1.getProxyBasic.isEmpty)
    assert(req3.hasProxyBearer)
    assert(req3.proxyBearer == BearerCredentials("bHVwaXRhOmxldG1laW4="))
    assert(req3.getProxyBearer.contains(BearerCredentials("bHVwaXRhOmxldG1laW4=")))

    val req4 = req1.setProxyBasic("lupita", "letmein")
    assert(req4.hasProxyAuthorization)
    assert(req4.proxyAuthorization == BasicCredentials("lupita", "letmein"))
    assert(req4.getProxyAuthorization.contains(BasicCredentials("lupita", "letmein")))
    assert(req4.removeProxyAuthorization == req1)
    assert(req4.getHeaderValue("Proxy-Authorization").contains("Basic bHVwaXRhOmxldG1laW4="))
    assert(req4.proxyBasic == BasicCredentials("lupita", "letmein"))
    assert(req4.getProxyBasic.contains(BasicCredentials("lupita", "letmein")))
    assert(!req4.hasProxyBearer)
    assertThrows[HttpException](req4.proxyBearer)
    assert(req4.getProxyBearer.isEmpty)

    val req5 = req1.setProxyBearer("bHVwaXRhOmxldG1laW4=")
    assert(req5.hasProxyAuthorization)
    assert(req5.proxyAuthorization == BearerCredentials("bHVwaXRhOmxldG1laW4="))
    assert(req5.getProxyAuthorization.contains(BearerCredentials("bHVwaXRhOmxldG1laW4=")))
    assert(req5.removeProxyAuthorization == req1)
    assert(req5.getHeaderValue("Proxy-Authorization").contains("Bearer bHVwaXRhOmxldG1laW4="))
    assertThrows[HttpException](req1.proxyBasic)
    assert(req1.getProxyBasic.isEmpty)
    assert(req5.hasProxyBearer)
    assert(req5.proxyBearer == BearerCredentials("bHVwaXRhOmxldG1laW4="))
    assert(req5.getProxyBearer.contains(BearerCredentials("bHVwaXRhOmxldG1laW4=")))
  }

  it should "create response with WWW-Authenticate header" in {
    val res1 = Unauthorized()
    assert(!res1.hasWwwAuthenticate)
    assert(res1.wwwAuthenticate.isEmpty)
    assert(res1.getWwwAuthenticate.isEmpty)
    assert(res1.removeWwwAuthenticate == res1)
    assert(!res1.hasBasic)
    assertThrows[HttpException](res1.basic)
    assert(res1.getBasic.isEmpty)
    assert(!res1.hasBearer)
    assertThrows[HttpException](res1.bearer)
    assert(res1.getBearer.isEmpty)

    val res2 = res1.setWwwAuthenticate("Basic realm=test, title=\"Test Realm\"")
    assert(res2.hasWwwAuthenticate)
    assert(res2.wwwAuthenticate == Seq(BasicChallenge("test", "title" -> "Test Realm")))
    assert(res2.getWwwAuthenticate.contains(Seq(BasicChallenge("test", "title" -> "Test Realm"))))
    assert(res2.removeWwwAuthenticate == res1)
    assert(res2.getHeaderValue("WWW-Authenticate").contains("Basic realm=\"test\", title=\"Test Realm\""))
    assert(res2.basic == BasicChallenge("test", "title" -> "Test Realm"))
    assert(res2.getBasic.contains(BasicChallenge("test", "title" -> "Test Realm")))
    assert(!res2.hasBearer)
    assertThrows[HttpException](res2.bearer)
    assert(res2.getBearer.isEmpty)

    val res3 = res1.setWwwAuthenticate("Bearer realm=\"test\", title=\"Test Realm\"")
    assert(res3.hasWwwAuthenticate)
    assert(res3.wwwAuthenticate == Seq(BearerChallenge("realm" -> "test", "title" -> "Test Realm")))
    assert(res3.getWwwAuthenticate.contains(Seq(BearerChallenge("realm" -> "test", "title" -> "Test Realm"))))
    assert(res3.removeWwwAuthenticate == res1)
    assert(res3.getHeaderValue("WWW-Authenticate").contains("Bearer realm=\"test\", title=\"Test Realm\""))
    assertThrows[HttpException](res3.basic)
    assert(res3.getBasic.isEmpty)
    assert(res3.hasBearer)
    assert(res3.bearer == BearerChallenge("realm" -> "test", "title" -> "Test Realm"))
    assert(res3.getBearer.contains(BearerChallenge("realm" -> "test", "title" -> "Test Realm")))

    val res4 = res1.setBasic("test", "title" -> "Test Realm")
    assert(res4.hasWwwAuthenticate)
    assert(res4.wwwAuthenticate == Seq(BasicChallenge("test", "title" -> "Test Realm")))
    assert(res4.getWwwAuthenticate.contains(Seq(BasicChallenge("test", "title" -> "Test Realm"))))
    assert(res4.removeWwwAuthenticate == res1)
    assert(res4.getHeaderValue("WWW-Authenticate").contains("Basic realm=\"test\", title=\"Test Realm\""))
    assert(res4.basic == BasicChallenge("test", "title" -> "Test Realm"))
    assert(res4.getBasic.contains(BasicChallenge("test", "title" -> "Test Realm")))
    assert(!res4.hasBearer)
    assertThrows[HttpException](res4.bearer)
    assert(res4.getBearer.isEmpty)

    val res5 = res1.setBearer("realm" -> "test", "title" -> "Test Realm")
    assert(res5.hasWwwAuthenticate)
    assert(res5.wwwAuthenticate == Seq(BearerChallenge("realm" -> "test", "title" -> "Test Realm")))
    assert(res5.getWwwAuthenticate.contains(Seq(BearerChallenge("realm" -> "test", "title" -> "Test Realm"))))
    assert(res5.removeWwwAuthenticate == res1)
    assert(res5.getHeaderValue("WWW-Authenticate").contains("Bearer realm=\"test\", title=\"Test Realm\""))
    assertThrows[HttpException](res5.basic)
    assert(res5.getBasic.isEmpty)
    assert(res5.hasBearer)
    assert(res5.bearer == BearerChallenge("realm" -> "test", "title" -> "Test Realm"))
    assert(res5.getBearer.contains(BearerChallenge("realm" -> "test", "title" -> "Test Realm")))
  }
