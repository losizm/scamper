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
package scamper

import java.time.Instant

import scala.util.Try

import scamper.types._

/**
 * Provides specialized access to message headers.
 *
 * === Using Header Classes ===
 *
 * Specialized header access is provided by type classes. Some headers are
 * available to both requests and responses, and others are available only to a
 * specific message type. This behavior is driven by the HTTP specification.
 *
 * {{{
 * import scamper.Implicits.stringToUri
 * import scamper.RequestMethod.Registry.Get
 * import scamper.headers.{ Accept, Host }
 * import scamper.types.Implicits.stringToMediaRange
 *
 * // Build request using 'Host' and 'Accept' headers
 * val req = Get("/motd").withHost("localhost:8080").withAccept("text/plain")
 *
 * // Access and print header values
 * printf("Host: %s%n", req.host)
 * printf("Accept: %s%n", req.accept.mkString(", "))
 * }}}
 */
package object headers {
  /** Provides standardized access to Accept header. */
  implicit class Accept(private val request: HttpRequest) extends AnyVal {
    /**
     * Gets Accept header values.
     *
     * @return header values or empty sequence if Accept is not present
     */
    def accept: Seq[MediaRange] = getAccept.getOrElse(Nil)

    /** Gets Accept header values if present. */
    def getAccept: Option[Seq[MediaRange]] =
      request.getHeaderValue("Accept")
        .map(ListParser.apply)
        .map(_.map(MediaRange.apply))

    /** Tests for Accept header. */
    def hasAccept: Boolean = request.hasHeader("Accept")

    /** Creates new request setting Accept header to supplied values. */
    def withAccept(values: Seq[MediaRange]): HttpRequest =
      request.withHeader(Header("Accept", values.mkString(", ")))

    /** Creates new request setting Accept header to supplied values. */
    def withAccept(one: MediaRange, more: MediaRange*): HttpRequest =
      withAccept(one +: more)

    /** Creates new request removing Accept header. */
    def removeAccept(): HttpRequest = request.removeHeaders("Accept")
  }

  /** Provides standardized access to Accept-Charset header. */
  implicit class AcceptCharset(private val request: HttpRequest) extends AnyVal {
    /**
     * Gets Accept-Charset header values.
     *
     * @return header values or empty sequence if Accept-Charset is not present
     */
    def acceptCharset: Seq[CharsetRange] = getAcceptCharset.getOrElse(Nil)

    /** Gets Accept-Charset header values if present. */
    def getAcceptCharset: Option[Seq[CharsetRange]] =
      request.getHeaderValue("Accept-Charset")
        .map(ListParser.apply)
        .map(_.map(CharsetRange.parse))

    /** Tests for Accept-Charset header. */
    def hasAcceptCharset: Boolean = request.hasHeader("AcceptCharset")

    /** Creates new request setting Accept-Charset header to supplied values. */
    def withAcceptCharset(values: Seq[CharsetRange]): HttpRequest =
      request.withHeader(Header("Accept-Charset", values.mkString(", ")))

    /** Creates new request setting Accept-Charset header to supplied values. */
    def withAcceptCharset(one: CharsetRange, more: CharsetRange*): HttpRequest =
      withAcceptCharset(one +: more)

    /** Creates new request removing Accept-Charset header. */
    def removeAcceptCharset(): HttpRequest = request.removeHeaders("Accept-Charset")
  }

  /** Provides standardized access to Accept-Encoding header. */
  implicit class AcceptEncoding[T <: HttpMessage](private val message: T) extends AnyVal {
    /**
     * Gets Accept-Encoding header values.
     *
     * @return header values or empty sequence if Accept-Encoding is not present
     */
    def acceptEncoding: Seq[ContentCodingRange] = getAcceptEncoding.getOrElse(Nil)

    /** Gets Accept-Encoding header values if present. */
    def getAcceptEncoding: Option[Seq[ContentCodingRange]] =
      message.getHeaderValue("Accept-Encoding")
        .map(ListParser.apply)
        .map(_.map(ContentCodingRange.parse))

    /** Tests for Accept-Encoding header. */
    def hasAcceptEncoding: Boolean = message.hasHeader("Accept-Encoding")

    /** Creates new message setting Accept-Encoding header to supplied values. */
    def withAcceptEncoding(values: Seq[ContentCodingRange])(implicit ev: <:<[T, MessageBuilder[T]]): T =
      message.withHeader(Header("Accept-Encoding", values.mkString(", ")))

    /** Creates new message setting Accept-Encoding header to supplied values. */
    def withAcceptEncoding(one: ContentCodingRange, more: ContentCodingRange*)(implicit ev: <:<[T, MessageBuilder[T]]): T =
      withAcceptEncoding(one +: more)

    /** Creates new message removing Accept-Encoding header. */
    def removeAcceptEncoding()(implicit ev: <:<[T, MessageBuilder[T]]): T =
      message.removeHeaders("Accept-Encoding")
  }

  /** Provides standardized access to Accept-Language header. */
  implicit class AcceptLanguage(private val request: HttpRequest) extends AnyVal {
    /**
     * Gets Accept-Language header values.
     *
     * @return header values or empty sequence if Accept-Language is not present
     */
    def acceptLanguage: Seq[LanguageRange] = getAcceptLanguage.getOrElse(Nil)

    /** Gets Accept-Language header values if present. */
    def getAcceptLanguage: Option[Seq[LanguageRange]] =
      request.getHeaderValue("Accept-Language")
        .map(ListParser.apply)
        .map(_.map(LanguageRange.parse))

    /** Tests for Accept-Language header. */
    def hasAcceptLanguage: Boolean = request.hasHeader("Accept-Language")

    /** Creates new request setting Accept-Language header to supplied values. */
    def withAcceptLanguage(values: Seq[LanguageRange]): HttpRequest =
      request.withHeader(Header("Accept-Language", values.mkString(", ")))

    /** Creates new request setting Accept-Language header to supplied values. */
    def withAcceptLanguage(one: LanguageRange, more: LanguageRange*): HttpRequest =
      withAcceptLanguage(one +: more)

    /** Creates new request removing Accept-Language header. */
    def removeAcceptLanguage(): HttpRequest = request.removeHeaders("Accept-Language")
  }

  /** Provides standardized access to Accept-Patch header. */
  implicit class AcceptPatch(private val response: HttpResponse) extends AnyVal {
    /**
     * Gets Accept-Patch header values.
     *
     * @return header values or empty sequence if Accept-Patch is not present
     */
    def acceptPatch: Seq[MediaType] = getAcceptPatch.getOrElse(Nil)

    /** Gets Accept-Patch header values if present. */
    def getAcceptPatch: Option[Seq[MediaType]] =
      response.getHeaderValue("Accept-Patch")
        .map(ListParser.apply)
        .map(_.map(MediaType.apply))

    /** Tests for Accept-Patch header. */
    def hasAcceptPatch: Boolean = response.hasHeader("Accept-Patch")

    /** Creates new response setting Accept-Patch header to supplied values. */
    def withAcceptPatch(values: Seq[MediaType]): HttpResponse =
      response.withHeader(Header("Accept-Patch", values.mkString(", ")))

    /** Creates new response setting Accept-Patch header to supplied values. */
    def withAcceptPatch(one: MediaType, more: MediaType*): HttpResponse =
      withAcceptPatch(one +: more)

    /** Creates new response removing Accept-Patch header. */
    def removeAcceptPatch(): HttpResponse = response.removeHeaders("Accept-Patch")
  }

  /** Provides standardized access to Accept-Ranges header. */
  implicit class AcceptRanges(private val response: HttpResponse) extends AnyVal {
    /**
     * Gets Accept-Ranges header values.
     *
     * @return header values or empty sequence if Accept-Ranges is not present
     */
    def acceptRanges: Seq[String] = getAcceptRanges.getOrElse(Nil)

    /** Gets Accept-Ranges header values if present. */
    def getAcceptRanges: Option[Seq[String]] =
      response.getHeaderValue("Accept-Ranges").map(ListParser.apply)

    /** Tests for Accept-Ranges header. */
    def hasAcceptRanges: Boolean = response.hasHeader("Accept-Ranges")

    /** Creates new response setting Accept-Ranges header to supplied values. */
    def withAcceptRanges(values: Seq[String]): HttpResponse =
      response.withHeader(Header("Accept-Ranges", values.mkString(", ")))

    /** Creates new response setting Accept-Ranges header to supplied values. */
    def withAcceptRanges(one: String, more: String*): HttpResponse =
      withAcceptRanges(one +: more)

    /** Creates new response removing Accept-Ranges header. */
    def removeAcceptRanges(): HttpResponse = response.removeHeaders("Accept-Ranges")
  }

  /** Provides standardized access to Age header. */
  implicit class Age(private val response: HttpResponse) extends AnyVal {
    /**
     * Gets Age header value.
     *
     * @throws HeaderNotFound if Age is not present
     */
    def age: Long = getAge.getOrElse(throw HeaderNotFound("Age"))

    /** Gets Age header value if present. */
    def getAge: Option[Long] =
      response.getHeader("Age").map(_.longValue)

    /** Tests for Age header. */
    def hasAge: Boolean = response.hasHeader("Age")

    /** Creates new response setting Age header to supplied value. */
    def withAge(value: Long): HttpResponse =
      response.withHeader(Header("Age", value))

    /** Creates new response removing Age header. */
    def removeAge(): HttpResponse = response.removeHeaders("Age")
  }

  /** Provides standardized access to Allow header. */
  implicit class Allow(private val response: HttpResponse) extends AnyVal {
    /**
     * Gets Allow header values.
     *
     * @return header values or empty sequence if Allow is not present
     */
    def allow: Seq[RequestMethod] = getAllow.getOrElse(Nil)

    /** Gets Allow header values if present. */
    def getAllow: Option[Seq[RequestMethod]] =
      response.getHeaderValue("Allow")
        .map(ListParser.apply)
        .map(_.map(RequestMethod.apply))

    /** Tests for Allow header. */
    def hasAllow: Boolean = response.hasHeader("Allow")

    /** Creates new response setting Allow header to supplied values. */
    def withAllow(values: Seq[RequestMethod]): HttpResponse =
      response.withHeader(Header("Allow", values.mkString(", ")))

    /** Creates new response setting Allow header to supplied values. */
    def withAllow(one: RequestMethod, more: RequestMethod*): HttpResponse =
      withAllow(one +: more)

    /** Creates new response removing Allow header. */
    def removeAllow(): HttpResponse = response.removeHeaders("Allow")
  }

  /** Provides standardized access to Cache-Control header. */
  implicit class CacheControl[T <: HttpMessage](private val message: T) extends AnyVal {
    /**
     * Gets Cache-Control header values.
     *
     * @return header values or empty sequence if Cache-Control is not present
     */
    def cacheControl: Seq[CacheDirective] = getCacheControl.getOrElse(Nil)

    /** Gets Cache-Control header values if present. */
    def getCacheControl: Option[Seq[CacheDirective]] =
      message.getHeaderValue("Cache-Control").map(CacheDirective.parseAll)

    /** Tests for Cache-Control header. */
    def hasCacheControl: Boolean = message.hasHeader("Cache-Control")

    /** Creates new message setting Cache-Control header to supplied values. */
    def withCacheControl(values: Seq[CacheDirective])(implicit ev: <:<[T, MessageBuilder[T]]): T =
      message.withHeader(Header("Cache-Control", values.mkString(", ")))

    /** Creates new message setting Cache-Control header to supplied values. */
    def withCacheControl(one: CacheDirective, more: CacheDirective*)(implicit ev: <:<[T, MessageBuilder[T]]): T =
      withCacheControl(one +: more)

    /** Creates new message removing Cache-Control header. */
    def removeCacheControl()(implicit ev: <:<[T, MessageBuilder[T]]): T =
      message.removeHeaders("Cache-Control")
  }

  /** Provides standardized access to Content-Disposition header. */
  implicit class ContentDisposition(private val response: HttpResponse) extends AnyVal {
    /**
     * Gets Content-Disposition header value.
     *
     * @throws HeaderNotFound if Content-Disposition is not present
     */
    def contentDisposition: DispositionType =
      getContentDisposition.getOrElse(throw HeaderNotFound("Content-Disposition"))

    /** Gets Content-Disposition header value if present. */
    def getContentDisposition: Option[DispositionType] =
      response.getHeaderValue("Content-Disposition").map(DispositionType.parse)

    /** Tests for Content-Disposition header. */
    def hasContentDisposition: Boolean = response.hasHeader("Content-Disposition")

    /** Creates new response setting Content-Disposition header to supplied value. */
    def withContentDisposition(value: DispositionType): HttpResponse =
      response.withHeader(Header("Content-Disposition", value.toString))

    /** Creates new response removing Content-Disposition header. */
    def removeContentDisposition(): HttpResponse = response.removeHeaders("Content-Disposition")
  }

  /** Provides standardized access to Content-Encoding header. */
  implicit class ContentEncoding[T <: HttpMessage](private val message: T) extends AnyVal {
    /**
     * Gets Content-Encoding header values.
     *
     * @return header values or empty sequence if Content-Encoding is not present
     */
    def contentEncoding: Seq[ContentCoding] = getContentEncoding.getOrElse(Nil)

    /** Gets Content-Encoding header values if present. */
    def getContentEncoding: Option[Seq[ContentCoding]] =
      message.getHeaderValue("Content-Encoding")
        .map(ListParser.apply)
        .map(_.map(ContentCoding.apply))

    /** Tests for Content-Encoding header. */
    def hasContentEncoding: Boolean = message.hasHeader("Content-Encoding")

    /** Creates new message setting Content-Encoding header to supplied values. */
    def withContentEncoding(values: Seq[ContentCoding])(implicit ev: <:<[T, MessageBuilder[T]]): T =
      message.withHeader(Header("Content-Encoding", values.mkString(", ")))

    /** Creates new message setting Content-Encoding header to supplied values. */
    def withContentEncoding(one: ContentCoding, more: ContentCoding*)(implicit ev: <:<[T, MessageBuilder[T]]): T =
      withContentEncoding(one +: more)

    /** Creates new message removing Content-Encoding header. */
    def removeContentEncoding()(implicit ev: <:<[T, MessageBuilder[T]]): T =
      message.removeHeaders("Content-Encoding")
  }

  /** Provides standardized access to Content-Language header. */
  implicit class ContentLanguage[T <: HttpMessage](private val message: T) extends AnyVal {
    /**
     * Gets Content-Language header values.
     *
     * @return header values or empty sequence if Content-Language is not present
     */
    def contentLanguage: Seq[LanguageTag] = getContentLanguage.getOrElse(Nil)

    /** Gets Content-Language header values if present. */
    def getContentLanguage: Option[Seq[LanguageTag]] =
      message.getHeaderValue("Content-Language")
        .map(ListParser.apply)
        .map(_.map(LanguageTag.parse))

    /** Tests for Content-Language header. */
    def hasContentLanguage: Boolean = message.hasHeader("Content-Language")

    /** Creates new message setting Content-Language header to supplied values. */
    def withContentLanguage(values: Seq[LanguageTag])(implicit ev: <:<[T, MessageBuilder[T]]): T =
      message.withHeader(Header("Content-Language", values.mkString(", ")))

    /** Creates new message setting Content-Language header to supplied values. */
    def withContentLanguage(one: LanguageTag, more: LanguageTag*)(implicit ev: <:<[T, MessageBuilder[T]]): T =
      withContentLanguage(one +: more)

    /** Creates new message removing Content-Language header. */
    def removeContentLanguage()(implicit ev: <:<[T, MessageBuilder[T]]): T =
      message.removeHeaders("Content-Language")
  }

  /** Provides standardized access to Content-Length header. */
  implicit class ContentLength[T <: HttpMessage](private val message: T) extends AnyVal {
    /**
     * Gets Content-Length header value.
     *
     * @throws HeaderNotFound if Content-Length is not present
     */
    def contentLength: Long = getContentLength.getOrElse(throw HeaderNotFound("Content-Length"))

    /** Gets Content-Length header value if present. */
    def getContentLength: Option[Long] =
      message.getHeader("Content-Length").map(_.longValue)

    /** Tests for Content-Length header. */
    def hasContentLength: Boolean = message.hasHeader("Content-Length")

    /** Creates new message setting Content-Length header to supplied value. */
    def withContentLength(value: Long)(implicit ev: <:<[T, MessageBuilder[T]]): T =
      message.withHeader(Header("Content-Length", value))

    /** Creates new message removing Content-Length header. */
    def removeContentLength()(implicit ev: <:<[T, MessageBuilder[T]]): T =
      message.removeHeaders("Content-Length")
  }

  /** Provides standardized access to Content-Location header. */
  implicit class ContentLocation[T <: HttpMessage](private val message: T) extends AnyVal {
    /**
     * Gets Content-Location header value.
     *
     * @throws HeaderNotFound if Content-Location is not present
     */
    def contentLocation: Uri =
      getContentLocation.getOrElse(throw HeaderNotFound("Content-Location"))

    /** Gets Content-Location header value if present. */
    def getContentLocation: Option[Uri] =
      message.getHeaderValue("Content-Location").map(Uri(_))

    /** Tests for Content-Location header. */
    def hasContentLocation: Boolean = message.hasHeader("Content-Location")

    /** Creates new message setting Content-Location header to supplied value. */
    def withContentLocation(value: Uri)(implicit ev: <:<[T, MessageBuilder[T]]): T =
      message.withHeader(Header("Content-Location", value.toString))

    /** Creates new message removing Content-Location header. */
    def removeContentLocation()(implicit ev: <:<[T, MessageBuilder[T]]): T =
      message.removeHeaders("Content-Location")
  }

  /** Provides standardized access to Content-Range header. */
  implicit class ContentRange[T <: HttpMessage](private val message: T) extends AnyVal {
    /**
     * Gets Content-Range header value.
     *
     * @throws HeaderNotFound if Content-Range is not present
     */
    def contentRange: ByteContentRange =
      getContentRange.getOrElse(throw HeaderNotFound("Content-Range"))

    /** Gets Content-Range header value if present. */
    def getContentRange: Option[ByteContentRange] =
      message.getHeaderValue("Content-Range").map(ByteContentRange.parse)

    /** Tests for Content-Range header. */
    def hasContentRange: Boolean = message.hasHeader("Content-Range")

    /** Creates new message setting Content-Range header to supplied value. */
    def withContentRange(value: ByteContentRange)(implicit ev: <:<[T, MessageBuilder[T]]): T =
      message.withHeader(Header("Content-Range", value.toString))

    /** Creates new message removing Content-Range header. */
    def removeContentRange()(implicit ev: <:<[T, MessageBuilder[T]]): T =
      message.removeHeaders("Content-Range")
  }

  /** Provides standardized access to Content-Type header. */
  implicit class ContentType[T <: HttpMessage](private val message: T) extends AnyVal {
    /**
     * Gets Content-Type header value.
     *
     * @throws HeaderNotFound if Content-Type is not present
     */
    def contentType: MediaType = getContentType.getOrElse(throw HeaderNotFound("Content-Type"))

    /** Gets Content-Type header value if present. */
    def getContentType: Option[MediaType] =
      message.getHeaderValue("Content-Type").map(MediaType.apply)

    /** Tests for Content-Type header. */
    def hasContentType: Boolean = message.hasHeader("Content-Type")

    /** Creates new message setting Content-Type header to supplied value. */
    def withContentType(value: MediaType)(implicit ev: <:<[T, MessageBuilder[T]]): T =
      message.withHeader(Header("Content-Type", value.toString))

    /** Creates new message removing Content-Type header. */
    def removeContentType()(implicit ev: <:<[T, MessageBuilder[T]]): T =
      message.removeHeaders("Content-Type")
  }

  /** Provides standardized access to Connection header. */
  implicit class Connection[T <: HttpMessage](private val message: T) extends AnyVal {
    /**
     * Gets Connection header values.
     *
     * @return header values or empty sequence if Connection is not present
     */
    def connection: Seq[String] = getConnection.getOrElse(Nil)

    /** Gets Connection header values if present. */
    def getConnection: Option[Seq[String]] =
      message.getHeaderValue("Connection").map(ListParser.apply)

    /** Tests for Connection header. */
    def hasConnection: Boolean = message.hasHeader("Connection")

    /** Creates new message setting Connection header to supplied values. */
    def withConnection(values: Seq[String])(implicit ev: <:<[T, MessageBuilder[T]]): T =
      message.withHeader(Header("Connection", values.mkString(", ")))

    /** Creates new message setting Connection header to supplied values. */
    def withConnection(one: String, more: String*)(implicit ev: <:<[T, MessageBuilder[T]]): T =
      withConnection(one +: more)

    /** Creates new message removing Connection header. */
    def removeConnection()(implicit ev: <:<[T, MessageBuilder[T]]): T =
      message.removeHeaders("Connection")
  }

  /** Provides standardized access to Date header. */
  implicit class Date[T <: HttpMessage](private val message: T) extends AnyVal {
    /**
     * Gets Date header value.
     *
     * @throws HeaderNotFound if Date is not present
     */
    def date: Instant = getDate.getOrElse(throw HeaderNotFound("Date"))

    /** Gets Date header value if present. */
    def getDate: Option[Instant] =
      message.getHeader("Date").map(_.dateValue)

    /** Tests for Date header. */
    def hasDate: Boolean = message.hasHeader("Date")

    /** Creates new message setting Date header to supplied value. */
    def withDate(value: Instant)(implicit ev: <:<[T, MessageBuilder[T]]): T =
      message.withHeader(Header("Date", value))

    /** Creates new message removing Date header. */
    def removeDate()(implicit ev: <:<[T, MessageBuilder[T]]): T =
      message.removeHeaders("Date")
  }

  /** Provides standardized access to ETag header. */
  implicit class ETag(private val response: HttpResponse) extends AnyVal {
    /**
     * Gets ETag header value.
     *
     * @throws HeaderNotFound if ETag is not present
     */
    def eTag: EntityTag = getETag.getOrElse(throw HeaderNotFound("ETag"))

    /** Gets ETag header value if present. */
    def getETag: Option[EntityTag] =
      response.getHeaderValue("ETag").map(EntityTag.parse)

    /** Tests for ETag header. */
    def hasETag: Boolean = response.hasHeader("ETag")

    /** Creates new response setting ETag header to supplied value. */
    def withETag(value: EntityTag): HttpResponse =
      response.withHeader(Header("ETag", value.toString))

    /** Creates new response removing ETag header. */
    def removeETag(): HttpResponse = response.removeHeaders("ETag")
  }

  /** Provides standardized access to Early-Data header. */
  implicit class EarlyData(private val request: HttpRequest) extends AnyVal {
    /**
     * Gets Early-Data header value.
     *
     * @throws HeaderNotFound if Early-Data is not present
     */
    def earlyData: Int = getEarlyData.getOrElse(throw HeaderNotFound("Early-Data"))

    /** Gets Early-Data header value if present. */
    def getEarlyData: Option[Int] =
      request.getHeaderValue("Early-Data").map(_.toInt)

    /** Tests for Early-Data header. */
    def hasEarlyData: Boolean = request.hasHeader("Early-Data")

    /** Creates new request setting Early-Data header to supplied value. */
    def withEarlyData(value: Int): HttpRequest =
      request.withHeader(Header("Early-Data", value))

    /** Creates new request removing Early-Data header. */
    def removeEarlyData(): HttpRequest = request.removeHeaders("Early-Data")
  }

  /** Provides standardized access to Expect header. */
  implicit class Expect(private val request: HttpRequest) extends AnyVal {
    /**
     * Gets Expect header value.
     *
     * @throws HeaderNotFound if Expect is not present
     */
    def expect: String = getExpect.getOrElse(throw HeaderNotFound("Expect"))

    /** Gets Expect header value if present. */
    def getExpect: Option[String] =
      request.getHeaderValue("Expect")

    /** Tests for Expect header. */
    def hasExpect: Boolean = request.hasHeader("Expect")

    /** Creates new request setting Expect header to supplied value. */
    def withExpect(value: String): HttpRequest =
      request.withHeader(Header("Expect", value))

    /** Creates new request removing Expect header. */
    def removeExpect(): HttpRequest = request.removeHeaders("Expect")
  }

  /** Provides standardized access to Expires header. */
  implicit class Expires(private val response: HttpResponse) extends AnyVal {
    /**
     * Gets Expires header value.
     *
     * @throws HeaderNotFound if Expires is not present
     */
    def expires: Instant = getExpires.getOrElse(throw HeaderNotFound("Expires"))

    /** Gets Expires header value if present. */
    def getExpires: Option[Instant] =
      response.getHeader("Expires").map(_.dateValue)

    /** Tests for Expires header. */
    def hasExpires: Boolean = response.hasHeader("Expires")

    /** Creates new response setting Expires header to supplied value. */
    def withExpires(value: Instant): HttpResponse =
      response.withHeader(Header("Expires", value))

    /** Creates new response removing Expires header. */
    def removeExpires(): HttpResponse = response.removeHeaders("Expires")
  }

  /** Provides standardized access to From header. */
  implicit class From(private val request: HttpRequest) extends AnyVal {
    /**
     * Gets From header value.
     *
     * @throws HeaderNotFound if From is not present
     */
    def from: String = getFrom.getOrElse(throw HeaderNotFound("From"))

    /** Gets From header value if present. */
    def getFrom: Option[String] =
      request.getHeaderValue("From")

    /** Tests for From header. */
    def hasFrom: Boolean = request.hasHeader("From")

    /** Creates new request setting From header to supplied value. */
    def withFrom(value: String): HttpRequest =
      request.withHeader(Header("From", value))

    /** Creates new request removing From header. */
    def removeFrom(): HttpRequest = request.removeHeaders("From")
  }

  /** Provides standardized access to Host header. */
  implicit class Host(private val request: HttpRequest) extends AnyVal {
    /**
     * Gets Host header value.
     *
     * @throws HeaderNotFound if Host is not present
     */
    def host: String = getHost.getOrElse(throw HeaderNotFound("Host"))

    /** Gets Host header value if present. */
    def getHost: Option[String] =
      request.getHeaderValue("Host")

    /** Tests for Host header. */
    def hasHost: Boolean = request.hasHeader("Host")

    /** Creates new request setting Host header to supplied value. */
    def withHost(value: String): HttpRequest =
      request.withHeader(Header("Host", value))

    /** Creates new request removing Host header. */
    def removeHost(): HttpRequest = request.removeHeaders("Host")
  }

  /** Provides standardized access to If-Match header. */
  implicit class IfMatch(private val request: HttpRequest) extends AnyVal {
    /**
     * Gets If-Match header values.
     *
     * @return header values or empty sequence if If-Match is not present
     */
    def ifMatch: Seq[EntityTag] = getIfMatch.getOrElse(Nil)

    /** Gets If-Match header values if present. */
    def getIfMatch: Option[Seq[EntityTag]] =
      request.getHeaderValue("If-Match")
        .map(ListParser.apply)
        .map(_.map(EntityTag.parse))

    /** Tests for If-Match header. */
    def hasIfMatch: Boolean = request.hasHeader("If-Match")

    /** Creates new request setting If-Match header to supplied values. */
    def withIfMatch(values: Seq[EntityTag]): HttpRequest =
      request.withHeader(Header("If-Match", values.mkString(", ")))

    /** Creates new request setting If-Match header to supplied values. */
    def withIfMatch(one: EntityTag, more: EntityTag*): HttpRequest =
      withIfMatch(one +: more)

    /** Creates new request removing If-Match header. */
    def removeIfMatch(): HttpRequest = request.removeHeaders("If-Match")
  }

  /** Provides standardized access to If-Modified-Since header. */
  implicit class IfModifiedSince(private val request: HttpRequest) extends AnyVal {
    /**
     * Gets If-Modified-Since header value.
     *
     * @throws HeaderNotFound if If-Modified-Since is not present
     */
    def ifModifiedSince: Instant =
      getIfModifiedSince.getOrElse(throw HeaderNotFound("If-Modified-Since"))

    /** Gets If-Modified-Since header value if present. */
    def getIfModifiedSince: Option[Instant] =
      request.getHeader("If-Modified-Since").map(_.dateValue)

    /** Tests for If-Modified-Since header. */
    def hasIfModifiedSince: Boolean = request.hasHeader("If-Modified-Since")

    /** Creates new request setting If-Modified-Since header to supplied value. */
    def withIfModifiedSince(value: Instant): HttpRequest =
      request.withHeader(Header("If-Modified-Since", value))

    /** Creates new request removing If-Modified-Since header. */
    def removeIfModifiedSince(): HttpRequest = request.removeHeaders("If-Modified-Since")
  }

  /** Provides standardized access to If-None-Match header. */
  implicit class IfNoneMatch(private val request: HttpRequest) extends AnyVal {
    /**
     * Gets If-None-Match header values.
     *
     * @return header values or empty sequence if If-None-Match is not present
     */
    def ifNoneMatch: Seq[EntityTag] = getIfNoneMatch.getOrElse(Nil)

    /** Gets If-None-Match header values if present. */
    def getIfNoneMatch: Option[Seq[EntityTag]] =
      request.getHeaderValue("If-None-Match")
        .map(ListParser.apply)
        .map(_.map(EntityTag.parse))

    /** Tests for If-None-Match header. */
    def hasIfNoneMatch: Boolean = request.hasHeader("If-None-Match")

    /** Creates new request setting If-None-Match header to supplied values. */
    def withIfNoneMatch(values: Seq[EntityTag]): HttpRequest =
      request.withHeader(Header("If-None-Match", values.mkString(", ")))

    /** Creates new request setting If-None-Match header to supplied values. */
    def withIfNoneMatch(one: EntityTag, more: EntityTag*): HttpRequest =
      withIfNoneMatch(one +: more)

    /** Creates new request removing If-None-Match header. */
    def removeIfNoneMatch(): HttpRequest = request.removeHeaders("If-None-Match")
  }

  /** Provides standardized access to If-Range header. */
  implicit class IfRange(private val request: HttpRequest) extends AnyVal {
    /**
     * Gets If-Range header value.
     *
     * @throws HeaderNotFound if If-Range is not present
     */
    def ifRange: Either[EntityTag, Instant] =
      getIfRange.getOrElse(throw HeaderNotFound("If-Range"))

    /** Gets If-Range header value if present. */
    def getIfRange: Option[Either[EntityTag, Instant]] =
      request.getHeader("If-Range").map { header =>
        Try {
          Left(EntityTag.parse(header.value))
        }.orElse {
          Try(Right(header.dateValue))
        }.get
      }

    /** Tests for If-Range header. */
    def hasIfRange: Boolean = request.hasHeader("If-Range")

    /** Creates new request setting If-Range header to supplied value. */
    def withIfRange(value: Either[EntityTag, Instant]): HttpRequest =
      value.fold(withIfRange, withIfRange)

    /** Creates new request setting If-Range header to supplied value. */
    def withIfRange(value: EntityTag): HttpRequest =
      request.withHeader(Header("If-Range", value.toString))

    /** Creates new request setting If-Range header to supplied value. */
    def withIfRange(value: Instant): HttpRequest =
      request.withHeader(Header("If-Range", value))

    /** Creates new request removing If-Range header. */
    def removeIfRange(): HttpRequest = request.removeHeaders("If-Range")
  }

  /** Provides standardized access to If-Unmodified-Since header. */
  implicit class IfUnmodifiedSince(private val request: HttpRequest) extends AnyVal {
    /**
     * Gets If-Unmodified-Since header value.
     *
     * @throws HeaderNotFound if If-Unmodified-Since is not present
     */
    def ifUnmodifiedSince: Instant =
      getIfUnmodifiedSince.getOrElse(throw HeaderNotFound("If-Unmodified-Since"))

    /** Gets If-Unmodified-Since header value if present. */
    def getIfUnmodifiedSince: Option[Instant] =
      request.getHeader("If-Unmodified-Since").map(_.dateValue)

    /** Tests for If-Unmodified-Since header. */
    def hasIfUnmodifiedSince: Boolean = request.hasHeader("If-Unmodified-Since")

    /**
     * Creates new request setting If-Unmodified-Since header to supplied value.
     */
    def withIfUnmodifiedSince(value: Instant): HttpRequest =
      request.withHeader(Header("If-Unmodified-Since", value))

    /** Creates new request removing If-Unmodified-Since header. */
    def removeIfUnmodifiedSince(): HttpRequest = request.removeHeaders("If-Unmodified-Since")
  }

  /** Provides standardized access to Keep-Alive header. */
  implicit class KeepAlive[T <: HttpMessage](private val message: T) extends AnyVal {
    /**
     * Gets Keep-Alive header value.
     *
     * @throws HeaderNotFound if Keep-Alive is not present
     */
    def keepAlive: KeepAliveParameters =
      getKeepAlive.getOrElse(throw HeaderNotFound("Keep-Alive"))

    /** Gets Keep-Alive header value if present. */
    def getKeepAlive: Option[KeepAliveParameters] =
      message.getHeaderValue("Keep-Alive").map(KeepAliveParameters.parse)

    /** Tests for Keep-Alive header. */
    def hasKeepAlive: Boolean = message.hasHeader("Keep-Alive")

    /** Creates new message setting Keep-Alive header to supplied value. */
    def withKeepAlive(value: KeepAliveParameters)(implicit ev: <:<[T, MessageBuilder[T]]): T =
      message.withHeader(Header("Keep-Alive", value.toString))

    /** Creates new message removing Keep-Alive header. */
    def removeKeepAlive()(implicit ev: <:<[T, MessageBuilder[T]]): T =
      message.removeHeaders("Keep-Alive")
  }

  /** Provides standardized access to Last-Modified header. */
  implicit class LastModified(private val response: HttpResponse) extends AnyVal {
    /**
     * Gets Last-Modified header value.
     *
     * @throws HeaderNotFound if Last-Modified is not present
     */
    def lastModified: Instant =
      getLastModified.getOrElse(throw HeaderNotFound("Last-Modified"))

    /** Gets Last-Modified header value if present. */
    def getLastModified: Option[Instant] =
      response.getHeader("Last-Modified").map(_.dateValue)

    /** Tests for Last-Modified header. */
    def hasLastModified: Boolean = response.hasHeader("Last-Modified")

    /** Creates new response setting Last-Modified header to supplied value. */
    def withLastModified(value: Instant): HttpResponse =
      response.withHeader(Header("Last-Modified", value))

    /** Creates new response removing Last-Modified header. */
    def removeLastModified(): HttpResponse = response.removeHeaders("Last-Modified")
  }

  /** Provides standardized access to Link header. */
  implicit class Link(private val response: HttpResponse) extends AnyVal {
    /**
     * Gets Link header values.
     *
     * @return header values or empty sequence if Link is not present
     */
    def link: Seq[LinkValue] = getLink.getOrElse(Nil)

    /** Gets Link header values if present. */
    def getLink: Option[Seq[LinkValue]] =
      response.getHeaderValue("Link").map(LinkValue.parseAll)

    /** Tests for Link header. */
    def hasLink: Boolean = response.hasHeader("Link")

    /** Creates new response setting Link header to supplied values. */
    def withLink(values: Seq[LinkValue]): HttpResponse =
      response.withHeader(Header("Link", values.mkString(", ")))

    /** Creates new response setting Link header to supplied values. */
    def withLink(one: LinkValue, more: LinkValue*): HttpResponse =
      withLink(one +: more)

    /** Creates new response removing Link header. */
    def removeLink(): HttpResponse = response.removeHeaders("Link")
  }

  /** Provides standardized access to Location header. */
  implicit class Location(private val response: HttpResponse) extends AnyVal {
    /**
     * Gets Location header value.
     *
     * @throws HeaderNotFound if Location is not present
     */
    def location: Uri = getLocation.getOrElse(throw HeaderNotFound("Location"))

    /** Gets Location header value if present. */
    def getLocation: Option[Uri] =
      response.getHeaderValue("Location").map(Uri(_))

    /** Tests for Location header. */
    def hasLocation: Boolean = response.hasHeader("Location")

    /** Creates new response setting Location header to supplied value. */
    def withLocation(value: Uri): HttpResponse =
      response.withHeader(Header("Location", value.toString))

    /** Creates new response removing Location header. */
    def removeLocation(): HttpResponse = response.removeHeaders("Location")
  }

  /** Provides standardized access to Max-Forwards header. */
  implicit class MaxForwards(private val request: HttpRequest) extends AnyVal {
    /**
     * Gets Max-Forwards header value.
     *
     * @throws HeaderNotFound if Max-Forwards is not present
     */
    def maxForwards: Long = getMaxForwards.getOrElse(throw HeaderNotFound("Max-Forwards"))

    /** Gets Max-Forwards header value if present. */
    def getMaxForwards: Option[Long] =
      request.getHeader("Max-Forwards").map(_.longValue)

    /** Tests for Max-Forwards header. */
    def hasMaxForwards: Boolean = request.hasHeader("Max-Forwards")

    /** Creates new request setting Max-Forwards header to supplied value. */
    def withMaxForwards(value: Long): HttpRequest =
      request.withHeader(Header("Max-Forwards", value))

    /** Creates new request removing Max-Forwards header. */
    def removeMaxForwards(): HttpRequest = request.removeHeaders("Max-Forwards")
  }

  /** Provides standardized access to Pragma header. */
  implicit class Pragma(private val request: HttpRequest) extends AnyVal {
    /**
     * Gets Pragma header values.
     *
     * @return header values or empty sequence if Pragma is not present
     */
    def pragma: Seq[PragmaDirective] = getPragma.getOrElse(Nil)

    /** Gets Pragma header values if present. */
    def getPragma: Option[Seq[PragmaDirective]] =
      request.getHeaderValue("Pragma").map(PragmaDirective.parseAll)

    /** Tests for Pragma header. */
    def hasPragma: Boolean = request.hasHeader("Pragma")

    /** Creates new request setting Pragma header to supplied values. */
    def withPragma(values: Seq[PragmaDirective]): HttpRequest =
      request.withHeader(Header("Pragma", values.mkString(", ")))

    /** Creates new request setting Pragma header to supplied values. */
    def withPragma(one: PragmaDirective, more: PragmaDirective*): HttpRequest =
      withPragma(one +: more)

    /** Creates new request removing Pragma header. */
    def removePragma(): HttpRequest = request.removeHeaders("Pragma")
  }

  /** Provides standardized access to Prefer header. */
  implicit class Prefer(private val request: HttpRequest) extends AnyVal {
    /**
     * Gets Prefer header values.
     *
     * @return header values or empty sequence if Prefer is not present
     */
    def prefer: Seq[Preference] = getPrefer.getOrElse(Nil)

    /** Gets Prefer header values if present. */
    def getPrefer: Option[Seq[Preference]] =
      request.getHeaderValues("Prefer")
        .flatMap(Preference.parseAll) match {
          case Nil => None
          case seq => Some(seq)
        }

    /** Tests for Prefer header. */
    def hasPrefer: Boolean = request.hasHeader("Prefer")

    /** Creates new request setting Prefer header to supplied values. */
    def withPrefer(values: Seq[Preference]): HttpRequest =
      request.withHeader(Header("Prefer", values.mkString(", ")))

    /** Creates new request setting Prefer header to supplied values. */
    def withPrefer(one: Preference, more: Preference*): HttpRequest =
      withPrefer(one +: more)

    /** Creates new request removing Prefer header. */
    def removePrefer(): HttpRequest = request.removeHeaders("Prefer")
  }

  /** Provides standardized access to Preference-Applied header. */
  implicit class PreferenceApplied(private val response: HttpResponse) extends AnyVal {
    /**
     * Gets Preference-Applied header values.
     *
     * @return header values or empty sequence if Preference-Applied is not present
     */
    def preferenceApplied: Seq[Preference] = getPreferenceApplied.getOrElse(Nil)

    /** Gets Preference-Applied header values if present. */
    def getPreferenceApplied: Option[Seq[Preference]] =
      response.getHeaderValue("Preference-Applied")
        .map(ListParser.apply)
        .map(_.map(Preference.apply))

    /** Tests for Preference-Applied header. */
    def hasPreferenceApplied: Boolean = response.hasHeader("Preference-Applied")

    /** Creates new response setting Preference-Applied header to supplied values. */
    def withPreferenceApplied(values: Seq[Preference]): HttpResponse =
      response.withHeader(Header("Preference-Applied", values.mkString(", ")))

    /** Creates new response setting Preference-Applied header to supplied values. */
    def withPreferenceApplied(one: Preference, more: Preference*): HttpResponse =
      withPreferenceApplied(one +: more)

    /** Creates new response removing Preference-Applied header. */
    def removePreferenceApplied(): HttpResponse = response.removeHeaders("Preference-Applied")
  }

  /** Provides standardized access to Range header. */
  implicit class Range(private val request: HttpRequest) extends AnyVal {
    /**
     * Gets Range header value.
     *
     * @throws HeaderNotFound if Range is not present
     */
    def range: ByteRange = getRange.getOrElse(throw HeaderNotFound("Range"))

    /** Gets Range header value if present. */
    def getRange: Option[ByteRange] =
      request.getHeaderValue("Range").map(ByteRange.parse)

    /** Tests for Range header. */
    def hasRange: Boolean = request.hasHeader("Range")

    /** Creates new request setting Range header to supplied value. */
    def withRange(value: ByteRange): HttpRequest =
      request.withHeader(Header("Range", value.toString))

    /** Creates new request removing Range header. */
    def removeRange(): HttpRequest = request.removeHeaders("Range")
  }

  /** Provides standardized access to Referer header. */
  implicit class Referer(private val request: HttpRequest) extends AnyVal {
    /**
     * Gets Referer header value.
     *
     * @throws HeaderNotFound if Referer is not present
     */
    def referer: Uri = getReferer.getOrElse(throw HeaderNotFound("Referer"))

    /** Gets Referer header value if present. */
    def getReferer: Option[Uri] =
      request.getHeaderValue("Referer").map(Uri(_))

    /** Tests for Referer header. */
    def hasReferer: Boolean = request.hasHeader("Referer")

    /** Creates new request setting Referer header to supplied value. */
    def withReferer(value: Uri): HttpRequest =
      request.withHeader(Header("Referer", value.toString))

    /** Creates new request removing Referer header. */
    def removeReferer(): HttpRequest = request.removeHeaders("Referer")
  }

  /** Provides standardized access to Retry-After header. */
  implicit class RetryAfter(private val response: HttpResponse) extends AnyVal {
    /**
     * Gets Retry-After header value.
     *
     * @throws HeaderNotFound if Retry-After is not present
     */
    def retryAfter: Instant = getRetryAfter.getOrElse(throw HeaderNotFound("Retry-After"))

    /** Gets Retry-After header value if present. */
    def getRetryAfter: Option[Instant] =
      response.getHeader("Retry-After").map(_.dateValue)

    /** Tests for Retry-After header. */
    def hasRetryAfter: Boolean = response.hasHeader("Retry-After")

    /** Creates new response setting Retry-After header to supplied value. */
    def withRetryAfter(value: Instant): HttpResponse =
      response.withHeader(Header("Retry-After", value))

    /** Creates new response removing Retry-After header. */
    def removeRetryAfter(): HttpResponse = response.removeHeaders("Retry-After")
  }

  /** Provides standardized access to Server header. */
  implicit class Server(private val response: HttpResponse) extends AnyVal {
    /**
     * Gets Server header values.
     *
     * @return header values or empty sequence if Server is not present
     */
    def server: Seq[ProductType] = getServer.getOrElse(Nil)

    /** Gets Server header values if present. */
    def getServer: Option[Seq[ProductType]] =
      response.getHeaderValue("Server").map(ProductType.parseAll)

    /** Tests for Server header. */
    def hasServer: Boolean = response.hasHeader("Server")

    /** Creates new response setting Server header to supplied values. */
    def withServer(values: Seq[ProductType]): HttpResponse =
      response.withHeader(Header("Server", values.mkString(" ")))

    /** Creates new response setting Server header to supplied values. */
    def withServer(one: ProductType, more: ProductType*): HttpResponse =
      withServer(one +: more)

    /** Creates new response removing Server header. */
    def removeServer(): HttpResponse = response.removeHeaders("Server")
  }

  /** Provides standardized access to TE header. */
  implicit class TE(private val request: HttpRequest) extends AnyVal {
    /**
     * Gets TE header values.
     *
     * @return header values or empty sequence if TE is not present
     */
    def te: Seq[TransferCodingRange] = getTE.getOrElse(Nil)

    /** Gets TE header values if present. */
    def getTE: Option[Seq[TransferCodingRange]] =
      request.getHeaderValue("TE")
        .map(ListParser.apply)
        .map(_.map(TransferCodingRange.parse))

    /** Tests for TE header. */
    def hasTE: Boolean = request.hasHeader("TE")

    /** Creates new request setting TE header to supplied values. */
    def withTE(values: Seq[TransferCodingRange]): HttpRequest =
      request.withHeader(Header("TE", values.mkString(", ")))

    /** Creates new request setting TE header to supplied values. */
    def withTE(one: TransferCodingRange, more: TransferCodingRange*): HttpRequest =
      withTE(one +: more)

    /** Creates new request removing TE header. */
    def removeTE(): HttpRequest = request.removeHeaders("TE")
  }

  /** Provides standardized access to Trailer header. */
  implicit class Trailer[T <: HttpMessage](private val message: T) extends AnyVal {
    /**
     * Gets Trailer header values.
     *
     * @return header values or empty sequence if Trailer is not present
     */
    def trailer: Seq[String] = getTrailer.getOrElse(Nil)

    /** Gets Trailer header values if present. */
    def getTrailer: Option[Seq[String]] =
      message.getHeaderValue("Trailer").map(ListParser.apply)

    /** Tests for Trailer header. */
    def hasTrailer: Boolean = message.hasHeader("Trailer")

    /** Creates new message setting Trailer header to supplied values. */
    def withTrailer(values: Seq[String])(implicit ev: <:<[T, MessageBuilder[T]]): T =
      message.withHeader(Header("Trailer", values.mkString(", ")))

    /** Creates new message setting Trailer header to supplied values. */
    def withTrailer(one: String, more: String*)(implicit ev: <:<[T, MessageBuilder[T]]): T =
      withTrailer(one +: more)

    /** Creates new message removing Trailer header. */
    def removeTrailer()(implicit ev: <:<[T, MessageBuilder[T]]): T =
      message.removeHeaders("Trailer")
  }

  /** Provides standardized access to Transfer-Encoding header. */
  implicit class TransferEncoding[T <: HttpMessage](private val message: T) extends AnyVal {
    /**
     * Gets Transfer-Encoding header values.
     *
     * @return header values or empty sequence if Transfer-Encoding is not present
     */
    def transferEncoding: Seq[TransferCoding] = getTransferEncoding.getOrElse(Nil)

    /** Gets Transfer-Encoding header values if present. */
    def getTransferEncoding: Option[Seq[TransferCoding]] =
      message.getHeaderValue("Transfer-Encoding")
        .map(ListParser.apply)
        .map(_.map(TransferCoding.parse))

    /** Tests for Transfer-Encoding header. */
    def hasTransferEncoding: Boolean = message.hasHeader("Transfer-Encoding")

    /** Creates new message setting Transfer-Encoding header to supplied values. */
    def withTransferEncoding(values: Seq[TransferCoding])(implicit ev: <:<[T, MessageBuilder[T]]): T =
      message.withHeader(Header("Transfer-Encoding", values.mkString(", ")))

    /** Creates new message setting Transfer-Encoding header to supplied values. */
    def withTransferEncoding(one: TransferCoding, more: TransferCoding*)(implicit ev: <:<[T, MessageBuilder[T]]): T =
      withTransferEncoding(one +: more)

    /** Creates new message removing Transfer-Encoding header. */
    def removeTransferEncoding()(implicit ev: <:<[T, MessageBuilder[T]]): T =
      message.removeHeaders("Transfer-Encoding")
  }

  /** Provides standardized access to Upgrade header. */
  implicit class Upgrade[T <: HttpMessage](private val message: T) extends AnyVal {
    /**
     * Gets Upgrade header values.
     *
     * @return header values or empty sequence if Upgrade is not present
     */
    def upgrade: Seq[Protocol] = getUpgrade.getOrElse(Nil)

    /** Gets Upgrade header values if present. */
    def getUpgrade: Option[Seq[Protocol]] =
      message.getHeaderValue("Upgrade")
        .map(ListParser.apply)
        .map(_.map(Protocol.parse))

    /** Tests for Upgrade header. */
    def hasUpgrade: Boolean = message.hasHeader("Upgrade")

    /** Creates new message setting Upgrade header to supplied values. */
    def withUpgrade(values: Seq[Protocol])(implicit ev: <:<[T, MessageBuilder[T]]): T =
      message.withHeader(Header("Upgrade", values.mkString(", ")))

    /** Creates new message setting Upgrade header to supplied values. */
    def withUpgrade(one: Protocol, more: Protocol*)(implicit ev: <:<[T, MessageBuilder[T]]): T =
      withUpgrade(one +: more)

    /** Creates new message removing Upgrade header. */
    def removeUpgrade()(implicit ev: <:<[T, MessageBuilder[T]]): T =
      message.removeHeaders("Upgrade")
  }

  /** Provides standardized access to User-Agent header. */
  implicit class UserAgent(private val request: HttpRequest) extends AnyVal {
    /**
     * Gets User-Agent header values.
     *
     * @return header values or empty sequence if User-Agent is not present
     */
    def userAgent: Seq[ProductType] = getUserAgent.getOrElse(Nil)

    /** Gets User-Agent header values if present. */
    def getUserAgent: Option[Seq[ProductType]] =
      request.getHeaderValue("User-Agent").map(ProductType.parseAll)

    /** Tests for User-Agent header. */
    def hasUserAgent: Boolean = request.hasHeader("User-Agent")

    /** Creates new request setting User-Agent header to supplied value. */
    def withUserAgent(values: Seq[ProductType]): HttpRequest =
      request.withHeader(Header("User-Agent", values.mkString(" ")))

    /** Creates new request setting User-Agent header to supplied value. */
    def withUserAgent(one: ProductType, more: ProductType*): HttpRequest =
      withUserAgent(one +: more)

    /** Creates new request removing User-Agent header. */
    def removeUserAgent(): HttpRequest = request.removeHeaders("User-Agent")
  }

  /** Provides standardized access to Vary header. */
  implicit class Vary(private val response: HttpResponse) extends AnyVal {
    /**
     * Gets Vary header values.
     *
     * @return header values or empty sequence if Vary is not present
     */
    def vary: Seq[String] = getVary.getOrElse(Nil)

    /** Gets Vary header values if present. */
    def getVary: Option[Seq[String]] =
      response.getHeaderValue("Vary").map(ListParser.apply)

    /** Tests for Vary header. */
    def hasVary: Boolean = response.hasHeader("Vary")

    /** Creates new response setting Vary header to supplied values. */
    def withVary(values: Seq[String]): HttpResponse =
      response.withHeader(Header("Vary", values.mkString(", ")))

    /** Creates new response setting Vary header to supplied values. */
    def withVary(one: String, more: String*): HttpResponse =
      withVary(one +: more)

    /** Creates new response removing Vary header. */
    def removeVary(): HttpResponse = response.removeHeaders("Vary")
  }

  /** Provides standardized access to Via header. */
  implicit class Via(private val response: HttpResponse) extends AnyVal {
    /**
     * Gets Via header values.
     *
     * @return header values or empty sequence if Via is not present
     */
    def via: Seq[ViaType] = getVia.getOrElse(Nil)

    /** Gets Via header values if present. */
    def getVia: Option[Seq[ViaType]] =
      response.getHeaderValue("Via").map(ViaType.parseAll)

    /** Tests for Via header. */
    def hasVia: Boolean = response.hasHeader("Via")

    /** Creates new response setting Via header to supplied values. */
    def withVia(values: Seq[ViaType]): HttpResponse =
      response.withHeader(Header("Via", values.mkString(", ")))

    /** Creates new response setting Via header to supplied values. */
    def withVia(one: ViaType, more: ViaType*): HttpResponse =
      withVia(one +: more)

    /** Creates new response removing Via header. */
    def removeVia(): HttpResponse = response.removeHeaders("Via")
  }

  /** Provides standardized access to Warning header. */
  implicit class Warning(private val response: HttpResponse) extends AnyVal {
    /**
     * Gets Warning header values.
     *
     * @return header values or empty sequence if Warning is not present
     */
    def warning: Seq[WarningType] = getWarning.getOrElse(Nil)

    /** Gets Warning header values if present. */
    def getWarning: Option[Seq[WarningType]] =
      response.getHeaderValue("Warning").map(WarningType.parseAll)

    /** Tests for Warning header. */
    def hasWarning: Boolean = response.hasHeader("Warning")

    /** Creates new response setting Warning header to supplied values. */
    def withWarning(values: Seq[WarningType]): HttpResponse =
      response.withHeader(Header("Warning", values.mkString(", ")))

    /** Creates new response setting Warning header to supplied values. */
    def withWarning(one: WarningType, more: WarningType*): HttpResponse =
      withWarning(one +: more)

    /** Creates new response removing Warning header. */
    def removeWarning(): HttpResponse = response.removeHeaders("Warning")
  }
}
