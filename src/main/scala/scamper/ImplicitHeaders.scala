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

import java.net.URI
import java.time.OffsetDateTime

import scala.util.Try

import scamper.types._

/** Includes type classes for standardized access to message headers. */
object ImplicitHeaders {
  /** Provides standardized access to Accept header. */
  implicit class Accept[T <: HttpRequest](val request: T) extends AnyVal {
    /**
     * Gets Accept header values.
     *
     * @return header values or empty sequence if Accept is not present
     */
    def accept: Seq[MediaRange] =
      getAccept.getOrElse(Nil)

    /** Gets Accept header values if present. */
    def getAccept: Option[Seq[MediaRange]] =
      request.getHeaderValue("Accept")
        .map(ListParser.apply)
        .map(_.map(MediaRange.parse))

    /** Creates new request setting Accept header to supplied values. */
    def withAccept(values: MediaRange*): T#MessageType =
      request.withHeader(Header("Accept", values.mkString(", ")))

    /** Creates new request removing Accept header. */
    def removeAccept: T#MessageType =
      request.removeHeaders("Accept")
  }

  /** Provides standardized access to Accept-Charset header. */
  implicit class AcceptCharset[T <: HttpRequest](val request: T) extends AnyVal {
    /**
     * Gets Accept-Charset header values.
     *
     * @return header values or empty sequence if Accept-Charset is not present
     */
    def acceptCharset: Seq[CharsetRange] =
      getAcceptCharset.getOrElse(Nil)

    /** Gets Accept-Charset header values if present. */
    def getAcceptCharset: Option[Seq[CharsetRange]] =
      request.getHeaderValue("Accept-Charset")
        .map(ListParser.apply)
        .map(_.map(CharsetRange.parse))

    /** Creates new request setting Accept-Charset header to supplied values. */
    def withAcceptCharset(values: CharsetRange*): T#MessageType =
      request.withHeader(Header("Accept-Charset", values.mkString(", ")))

    /** Creates new request removing Accept-Charset header. */
    def removeAcceptCharset: T#MessageType =
      request.removeHeaders("Accept-Charset")
  }

  /** Provides standardized access to Accept-Encoding header. */
  implicit class AcceptEncoding[T <: HttpRequest](val request: T) extends AnyVal {
    /**
     * Gets Accept-Encoding header values.
     *
     * @return header values or empty sequence if Accept-Encoding is not present
     */
    def acceptEncoding: Seq[ContentCodingRange] =
      getAcceptEncoding.getOrElse(Nil)

    /** Gets Accept-Encoding header values if present. */
    def getAcceptEncoding: Option[Seq[ContentCodingRange]] =
      request.getHeaderValue("Accept-Encoding")
        .map(ListParser.apply)
        .map(_.map(ContentCodingRange.parse))

    /**
     * Creates new request setting Accept-Encoding header to supplied values.
     */
    def withAcceptEncoding(values: ContentCodingRange*): T#MessageType =
      request.withHeader(Header("Accept-Encoding", values.mkString(", ")))

    /** Creates new request removing Accept-Encoding header. */
    def removeAcceptEncoding: T#MessageType =
      request.removeHeaders("Accept-Encoding")
  }

  /** Provides standardized access to Accept-Language header. */
  implicit class AcceptLanguage[T <: HttpRequest](val request: T) extends AnyVal {
    /**
     * Gets Accept-Language header values.
     *
     * @return header values or empty sequence if Accept-Language is not present
     */
    def acceptLanguage: Seq[LanguageRange] =
      getAcceptLanguage.getOrElse(Nil)

    /** Gets Accept-Language header values if present. */
    def getAcceptLanguage: Option[Seq[LanguageRange]] =
      request.getHeaderValue("Accept-Language")
        .map(ListParser.apply)
        .map(_.map(LanguageRange.parse))

    /**
     * Creates new request setting Accept-Language header to supplied values.
     */
    def withAcceptLanguage(values: LanguageRange*): T#MessageType =
      request.withHeader(Header("Accept-Language", values.mkString(", ")))

    /** Creates new request removing Accept-Language header. */
    def removeAcceptLanguage: T#MessageType =
      request.removeHeaders("Accept-Language")
  }

  /** Provides standardized access to Accept-Patch header. */
  implicit class AcceptPatch[T <: HttpResponse](val response: T) extends AnyVal {
    /**
     * Gets Accept-Patch header values.
     *
     * @return header values or empty sequence if Accept-Patch is not present
     */
    def acceptPatch: Seq[MediaType] =
      getAcceptPatch.getOrElse(Nil)

    /** Gets Accept-Patch header values if present. */
    def getAcceptPatch: Option[Seq[MediaType]] =
      response.getHeaderValue("Accept-Patch")
        .map(ListParser.apply)
        .map(_.map(MediaType.parse))

    /** Creates new response setting Accept-Patch header to supplied values. */
    def withAcceptPatch(values: MediaType*): T#MessageType =
      response.withHeader(Header("Accept-Patch", values.mkString(", ")))

    /** Creates new response removing Accept-Patch header. */
    def removeAcceptPatch: T#MessageType =
      response.removeHeaders("Accept-Patch")
  }

  /** Provides standardized access to Accept-Ranges header. */
  implicit class AcceptRanges[T <: HttpResponse](val response: T) extends AnyVal {
    /**
     * Gets Accept-Ranges header values.
     *
     * @return header values or empty sequence if Accept-Ranges is not present
     */
    def acceptRanges: Seq[String] =
      getAcceptRanges.getOrElse(Nil)

    /** Gets Accept-Ranges header values if present. */
    def getAcceptRanges: Option[Seq[String]] =
      response.getHeaderValue("Accept-Ranges").map(ListParser.apply)

    /** Creates new response setting Accept-Ranges header to supplied values. */
    def withAcceptRanges(values: String*): T#MessageType =
      response.withHeader(Header("Accept-Ranges", values.mkString(", ")))

    /** Creates new response removing Accept-Ranges header. */
    def removeAcceptRanges: T#MessageType =
      response.removeHeaders("Accept-Ranges")
  }

  /** Provides standardized access to Age header. */
  implicit class Age[T <: HttpResponse](val response: T) extends AnyVal {
    /**
     * Gets Age header value.
     *
     * @throws HeaderNotFound if Age is not present
     */
    def age: Long =
      getAge.getOrElse(throw HeaderNotFound("Age"))

    /** Gets Age header value if present. */
    def getAge: Option[Long] =
      response.getHeaderValue("Age").map(_.toLong)

    /** Creates new response setting Age header to supplied value. */
    def withAge(value: Long): T#MessageType =
      response.withHeader(Header("Age", value))

    /** Creates new response removing Age header. */
    def removeAge: T#MessageType =
      response.removeHeaders("Age")
  }

  /** Provides standardized access to Allow header. */
  implicit class Allow[T <: HttpResponse](val response: T) extends AnyVal {
    /**
     * Gets Allow header values.
     *
     * @return header values or empty sequence if Allow is not present
     */
    def allow: Seq[RequestMethod] =
      getAllow.getOrElse(Nil)

    /** Gets Allow header values if present. */
    def getAllow: Option[Seq[RequestMethod]] =
      response.getHeaderValue("Allow")
        .map(ListParser.apply)
        .map(_.map(RequestMethod.apply))

    /** Creates new response setting Allow header to supplied values. */
    def withAllow(values: RequestMethod*): T#MessageType =
      response.withHeader(Header("Allow", values.mkString(", ")))

    /** Creates new response removing Allow header. */
    def removeAllow: T#MessageType =
      response.removeHeaders("Allow")
  }

  /** Provides standardized access to Authentication-Info header. */
  implicit class AuthenticationInfo[T <: HttpResponse](val response: T) extends AnyVal {
    /**
     * Gets Authentication-Info header values.
     *
     * @return header values or empty sequence if Authentication-Info is not present
     */
    def authenticationInfo: Map[String, String] =
      getAuthenticationInfo.getOrElse(Map.empty)

    /** Gets Authentication-Info header values if present. */
    def getAuthenticationInfo: Option[Map[String, String]] =
      response.getHeaderValue("Authentication-Info").map(AuthParams.parse)

    /**
     * Creates new response setting Authentication-Info header to supplied
     * values.
     */
    def withAuthenticationInfo(values: (String, String)*): T#MessageType =
      response.withHeader(Header("Authentication-Info", AuthParams.format(values.toMap).trim))

    /** Creates new response removing Authentication-Info header. */
    def removeAuthenticationInfo: T#MessageType =
      response.removeHeaders("Authentication-Info")
  }

  /** Provides standardized access to Authorization header. */
  implicit class Authorization[T <: HttpRequest](val request: T) extends AnyVal {
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
    def withAuthorization(value: Credentials): T#MessageType =
      request.withHeader(Header("Authorization", value.toString))

    /** Creates new request removing Authorization header. */
    def removeAuthorization: T#MessageType =
      request.removeHeaders("Authorization")
  }

  /** Provides standardized access to Cache-Control header. */
  implicit class CacheControl[T <: HttpMessage](val message: T) extends AnyVal {
    /**
     * Gets Cache-Control header values.
     *
     * @return header values or empty sequence if Cache-Control is not present
     */
    def cacheControl: Seq[CacheDirective] =
      getCacheControl.getOrElse(Nil)

    /** Gets Cache-Control header values if present. */
    def getCacheControl: Option[Seq[CacheDirective]] =
      message.getHeaderValue("Cache-Control").map(CacheDirective.parseAll)

    /** Creates new message setting Cache-Control header to supplied values. */
    def withCacheControl(values: CacheDirective*): T#MessageType =
      message.withHeader(Header("Cache-Control", values.mkString(", ")))

    /** Creates new message removing Cache-Control header. */
    def removeCacheControl: T#MessageType =
      message.removeHeaders("Cache-Control")
  }

  /** Provides standardized access to Content-Disposition header. */
  implicit class ContentDisposition[T <: HttpResponse](val response: T) extends AnyVal {
    /**
     * Gets Content-Disposition header value.
     *
     * @throws HeaderNotFound if Content-Disposition is not present
     */
    def contentDisposition: ContentDispositionType =
      getContentDisposition.getOrElse(throw HeaderNotFound("Content-Disposition"))

    /** Gets Content-Disposition header value if present. */
    def getContentDisposition: Option[ContentDispositionType] =
      response.getHeaderValue("Content-Disposition").map(ContentDispositionType.parse)

    /**
     * Creates new response setting Content-Disposition header to supplied
     * value.
     */
    def withContentDisposition(value: ContentDispositionType): T#MessageType =
      response.withHeader(Header("Content-Disposition", value.toString))

    /** Creates new response removing Content-Disposition header. */
    def removeContentDisposition: T#MessageType =
      response.removeHeaders("Content-Disposition")
  }

  /** Provides standardized access to Content-Encoding header. */
  implicit class ContentEncoding[T <: HttpMessage](val message: T) extends AnyVal {
    /**
     * Gets Content-Encoding header values.
     *
     * @return header values or empty sequence if Content-Encoding is not present
     */
    def contentEncoding: Seq[ContentCoding] =
      getContentEncoding.getOrElse(Nil)

    /** Gets Content-Encoding header values if present. */
    def getContentEncoding: Option[Seq[ContentCoding]] =
      message.getHeaderValue("Content-Encoding")
        .map(ListParser.apply)
        .map(_.map(ContentCoding.apply))

    /**
     * Creates new message setting Content-Encoding header to supplied values.
     */
    def withContentEncoding(values: ContentCoding*): T#MessageType =
      message.withHeader(Header("Content-Encoding", values.mkString(", ")))

    /** Creates new message removing Content-Encoding header. */
    def removeContentEncoding: T#MessageType =
      message.removeHeaders("Content-Encoding")
  }

  /** Provides standardized access to Content-Language header. */
  implicit class ContentLanguage[T <: HttpMessage](val message: T) extends AnyVal {
    /**
     * Gets Content-Language header values.
     *
     * @return header values or empty sequence if Content-Language is not present
     */
    def contentLanguage: Seq[LanguageTag] =
      getContentLanguage.getOrElse(Nil)

    /** Gets Content-Language header values if present. */
    def getContentLanguage: Option[Seq[LanguageTag]] =
      message.getHeaderValue("Content-Language")
        .map(ListParser.apply)
        .map(_.map(LanguageTag.parse))

    /** Creates new message setting Content-Language header to supplied values. */
    def withContentLanguage(values: LanguageTag*): T#MessageType =
      message.withHeader(Header("Content-Language", values.mkString(", ")))

    /** Creates new message removing Content-Language header. */
    def removeContentLanguage: T#MessageType =
      message.removeHeaders("Content-Language")
  }

  /** Provides standardized access to Content-Length header. */
  implicit class ContentLength[T <: HttpMessage](val message: T) extends AnyVal {
    /**
     * Gets Content-Length header value.
     *
     * @throws HeaderNotFound if Content-Length is not present
     */
    def contentLength: Long =
      getContentLength.getOrElse(throw HeaderNotFound("Content-Length"))

    /** Gets Content-Length header value if present. */
    def getContentLength: Option[Long] =
      message.getHeaderValue("Content-Length").map(_.toLong)

    /** Creates new message setting Content-Length header to supplied value. */
    def withContentLength(value: Long): T#MessageType =
      message.withHeader(Header("Content-Length", value))

    /** Creates new message removing Content-Length header. */
    def removeContentLength: T#MessageType =
      message.removeHeaders("Content-Length")
  }

  /** Provides standardized access to Content-Location header. */
  implicit class ContentLocation[T <: HttpMessage](val message: T) extends AnyVal {
    /**
     * Gets Content-Location header value.
     *
     * @throws HeaderNotFound if Content-Location is not present
     */
    def contentLocation: URI =
      getContentLocation.getOrElse(throw HeaderNotFound("Content-Location"))

    /** Gets Content-Location header value if present. */
    def getContentLocation: Option[URI] =
      message.getHeaderValue("Content-Location").map(new URI(_))

    /**
     * Creates new message setting Content-Location header to supplied value.
     */
    def withContentLocation(value: URI): T#MessageType =
      message.withHeader(Header("Content-Location", value.toString))

    /** Creates new message removing Content-Location header. */
    def removeContentLocation: T#MessageType =
      message.removeHeaders("Content-Location")
  }

  /** Provides standardized access to Content-Range header. */
  implicit class ContentRange[T <: HttpMessage](val message: T) extends AnyVal {
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

    /** Creates new message setting Content-Range header to supplied value. */
    def withContentRange(value: ByteContentRange): T#MessageType =
      message.withHeader(Header("Content-Range", value.toString))

    /** Creates new message removing Content-Range header. */
    def removeContentRange: T#MessageType =
      message.removeHeaders("Content-Range")
  }

  /** Provides standardized access to Content-Type header. */
  implicit class ContentType[T <: HttpMessage](val message: T) extends AnyVal {
    /**
     * Gets Content-Type header value.
     *
     * @throws HeaderNotFound if Content-Type is not present
     */
    def contentType: MediaType =
      getContentType.getOrElse(throw HeaderNotFound("Content-Type"))

    /** Gets Content-Type header value if present. */
    def getContentType: Option[MediaType] =
      message.getHeaderValue("Content-Type").map(MediaType.parse)

    /** Creates new message setting Content-Type header to supplied value. */
    def withContentType(value: MediaType): T#MessageType =
      message.withHeader(Header("Content-Type", value.toString))

    /** Creates new message removing Content-Type header. */
    def removeContentType: T#MessageType =
      message.removeHeaders("Content-Type")
  }

  /** Provides standardized access to Connection header. */
  implicit class Connection[T <: HttpMessage](val message: T) extends AnyVal {
    /**
     * Gets Connection header value.
     *
     * @throws HeaderNotFound if Connection is not present
     */
    def connection: String =
      getConnection.getOrElse(throw HeaderNotFound("Connection"))

    /** Gets Connection header value if present. */
    def getConnection: Option[String] =
      message.getHeaderValue("Connection")

    /** Creates new message setting Connection header to supplied value. */
    def withConnection(value: String): T#MessageType =
      message.withHeader(Header("Connection", value))

    /** Creates new message removing Connection header. */
    def removeConnection: T#MessageType =
      message.removeHeaders("Connection")
  }

  /** Provides standardized access to Date header. */
  implicit class Date[T <: HttpResponse](val response: T) extends AnyVal {
    /**
     * Gets Date header value.
     *
     * @throws HeaderNotFound if Date is not present
     */
    def date: OffsetDateTime =
      getDate.getOrElse(throw HeaderNotFound("Date"))

    /** Gets Date header value if present. */
    def getDate: Option[OffsetDateTime] =
      response.getHeaderValue("Date").map(DateValue.parse)

    /** Creates new response setting Date header to supplied value. */
    def withDate(value: OffsetDateTime): T#MessageType =
      response.withHeader(Header("Date", value))

    /** Creates new response removing Date header. */
    def removeDate: T#MessageType =
      response.removeHeaders("Date")
  }

  /** Provides standardized access to ETag header. */
  implicit class ETag[T <: HttpResponse](val response: T) extends AnyVal {
    /**
     * Gets ETag header value.
     *
     * @throws HeaderNotFound if ETag is not present
     */
    def etag: EntityTag =
      getETag.getOrElse(throw HeaderNotFound("ETag"))

    /** Gets ETag header value if present. */
    def getETag: Option[EntityTag] =
      response.getHeaderValue("ETag").map(EntityTag.parse)

    /** Creates new response setting ETag header to supplied value. */
    def withETag(value: EntityTag): T#MessageType =
      response.withHeader(Header("ETag", value.toString))

    /** Creates new response removing ETag header. */
    def removeETag: T#MessageType =
      response.removeHeaders("ETag")
  }

  /** Provides standardized access to Expect header. */
  implicit class Expect[T <: HttpRequest](val request: T) extends AnyVal {
    /**
     * Gets Expect header value.
     *
     * @throws HeaderNotFound if Expect is not present
     */
    def expect: String =
      getExpect.getOrElse(throw HeaderNotFound("Expect"))

    /** Gets Expect header value if present. */
    def getExpect: Option[String] =
      request.getHeaderValue("Expect")

    /** Creates new request setting Expect header to supplied value. */
    def withExpect(value: String): T#MessageType =
      request.withHeader(Header("Expect", value))

    /** Creates new request removing Expect header. */
    def removeExpect: T#MessageType =
      request.removeHeaders("Expect")
  }

  /** Provides standardized access to Expires header. */
  implicit class Expires[T <: HttpResponse](val response: T) extends AnyVal {
    /**
     * Gets Expires header value.
     *
     * @throws HeaderNotFound if Expires is not present
     */
    def expires: OffsetDateTime =
      getExpires.getOrElse(throw HeaderNotFound("Expires"))

    /** Gets Expires header value if present. */
    def getExpires: Option[OffsetDateTime] =
      response.getHeaderValue("Expires").map(DateValue.parse)

    /** Creates new response setting Expires header to supplied value. */
    def withExpires(value: OffsetDateTime): T#MessageType =
      response.withHeader(Header("Expires", value))

    /** Creates new response removing Expires header. */
    def removeExpires: T#MessageType =
      response.removeHeaders("Expires")
  }

  /** Provides standardized access to From header. */
  implicit class From[T <: HttpRequest](val request: T) extends AnyVal {
    /**
     * Gets From header value.
     *
     * @throws HeaderNotFound if From is not present
     */
    def from: String =
      getFrom.getOrElse(throw HeaderNotFound("From"))

    /** Gets From header value if present. */
    def getFrom: Option[String] =
      request.getHeaderValue("From")

    /** Creates new request setting From header to supplied value. */
    def withFrom(value: String): T#MessageType =
      request.withHeader(Header("From", value))

    /** Creates new request removing From header. */
    def removeFrom: T#MessageType =
      request.removeHeaders("From")
  }

  /** Provides standardized access to Host header. */
  implicit class Host[T <: HttpRequest](val request: T) extends AnyVal {
    /**
     * Gets Host header value.
     *
     * @throws HeaderNotFound if Host is not present
     */
    def host: String =
      getHost.getOrElse(throw HeaderNotFound("Host"))

    /** Gets Host header value if present. */
    def getHost: Option[String] =
      request.getHeaderValue("Host")

    /** Creates new request setting Host header to supplied value. */
    def withHost(value: String): T#MessageType =
      request.withHeader(Header("Host", value))

    /** Creates new request removing Host header. */
    def removeHost: T#MessageType =
      request.removeHeaders("Host")
  }

  /** Provides standardized access to If-Match header. */
  implicit class IfMatch[T <: HttpRequest](val request: T) extends AnyVal {
    /**
     * Gets If-Match header value.
     *
     * @throws HeaderNotFound if If-Match is not present
     */
    def ifMatch: EntityTag =
      getIfMatch.getOrElse(throw HeaderNotFound("If-Match"))

    /** Gets If-Match header value if present. */
    def getIfMatch: Option[EntityTag] =
      request.getHeaderValue("If-Match").map(EntityTag.parse)

    /** Creates new request setting If-Match header to supplied value. */
    def withIfMatch(value: EntityTag): T#MessageType =
      request.withHeader(Header("If-Match", value.toString))

    /** Creates new request removing If-Match header. */
    def removeIfMatch: T#MessageType =
      request.removeHeaders("If-Match")
  }

  /** Provides standardized access to If-Modified-Since header. */
  implicit class IfModifiedSince[T <: HttpRequest](val request: T) extends AnyVal {
    /**
     * Gets If-Modified-Since header value.
     *
     * @throws HeaderNotFound if If-Modified-Since is not present
     */
    def ifModifiedSince: OffsetDateTime =
      getIfModifiedSince.getOrElse(throw HeaderNotFound("If-Modified-Since"))

    /** Gets If-Modified-Since header value if present. */
    def getIfModifiedSince: Option[OffsetDateTime] =
      request.getHeaderValue("If-Modified-Since").map(DateValue.parse)

    /** Creates new request setting If-Modified-Since header to supplied value. */
    def withIfModifiedSince(value: OffsetDateTime): T#MessageType =
      request.withHeader(Header("If-Modified-Since", value))

    /** Creates new request removing If-Modified-Since header. */
    def removeIfModifiedSince: T#MessageType =
      request.removeHeaders("If-Modified-Since")
  }

  /** Provides standardized access to If-None-Match header. */
  implicit class IfNoneMatch[T <: HttpRequest](val request: T) extends AnyVal {
    /**
     * Gets If-None-Match header value.
     *
     * @throws HeaderNotFound if If-None-Match is not present
     */
    def ifNoneMatch: EntityTag =
      getIfNoneMatch.getOrElse(throw HeaderNotFound("If-None-Match"))

    /** Gets If-None-Match header value if present. */
    def getIfNoneMatch: Option[EntityTag] =
      request.getHeaderValue("If-None-Match").map(EntityTag.parse)

    /** Creates new request setting If-None-Match header to supplied value. */
    def withIfNoneMatch(value: EntityTag): T#MessageType =
      request.withHeader(Header("If-None-Match", value.toString))

    /** Creates new request removing If-None-Match header. */
    def removeIfNoneMatch: T#MessageType =
      request.removeHeaders("If-None-Match")
  }

  /** Provides standardized access to If-Range header. */
  implicit class IfRange[T <: HttpRequest](val request: T) extends AnyVal {
    /**
     * Gets If-Range header value.
     *
     * @throws HeaderNotFound if If-Range is not present
     */
    def ifRange: Either[EntityTag, OffsetDateTime] =
      getIfRange.getOrElse(throw HeaderNotFound("If-Range"))

    /** Gets If-Range header value if present. */
    def getIfRange: Option[Either[EntityTag, OffsetDateTime]] =
      request.getHeaderValue("If-Range").map { value =>
        Try {
          Left(EntityTag.parse(value))
        }.orElse {
          Try(Right(DateValue.parse(value)))
        }.get
      }

    /** Creates new request setting If-Range header to supplied value. */
    def withIfRange(value: Either[EntityTag, OffsetDateTime]): T#MessageType =
      request.withHeader(Header("If-Range", value.fold(_.toString, DateValue.format)))

    /** Creates new request setting If-Range header to supplied value. */
    def withIfRange(value: EntityTag): T#MessageType =
      request.withHeader(Header("If-Range", value.toString))

    /** Creates new request setting If-Range header to supplied value. */
    def withIfRange(value: OffsetDateTime): T#MessageType =
      request.withHeader(Header("If-Range", value))

    /** Creates new request removing If-Range header. */
    def removeIfRange: T#MessageType =
      request.removeHeaders("If-Range")
  }

  /** Provides standardized access to If-Unmodified-Since header. */
  implicit class IfUnmodifiedSince[T <: HttpRequest](val request: T) extends AnyVal {
    /**
     * Gets If-Unmodified-Since header value.
     *
     * @throws HeaderNotFound if If-Unmodified-Since is not present
     */
    def ifUnmodifiedSince: OffsetDateTime =
      getIfUnmodifiedSince.getOrElse(throw HeaderNotFound("If-Unmodified-Since"))

    /** Gets If-Unmodified-Since header value if present. */
    def getIfUnmodifiedSince: Option[OffsetDateTime] =
      request.getHeaderValue("If-Unmodified-Since").map(DateValue.parse)

    /**
     * Creates new request setting If-Unmodified-Since header to supplied value.
     */
    def withIfUnmodifiedSince(value: OffsetDateTime): T#MessageType =
      request.withHeader(Header("If-Unmodified-Since", value))

    /** Creates new request removing If-Unmodified-Since header. */
    def removeIfUnmodifiedSince: T#MessageType =
      request.removeHeaders("If-Unmodified-Since")
  }

  /** Provides standardized access to Last-Modified header. */
  implicit class LastModified[T <: HttpResponse](val response: T) extends AnyVal {
    /**
     * Gets Last-Modified header value.
     *
     * @throws HeaderNotFound if Last-Modified is not present
     */
    def lastModified: OffsetDateTime =
      getLastModified.getOrElse(throw HeaderNotFound("Last-Modified"))

    /** Gets Last-Modified header value if present. */
    def getLastModified: Option[OffsetDateTime] =
      response.getHeaderValue("Last-Modified").map(DateValue.parse)

    /** Creates new response setting Last-Modified header to supplied value. */
    def withLastModified(value: OffsetDateTime): T#MessageType =
      response.withHeader(Header("Last-Modified", value))

    /** Creates new response removing Last-Modified header. */
    def removeLastModified: T#MessageType =
      response.removeHeaders("Last-Modified")
  }

  /** Provides standardized access to Link header. */
  implicit class Link[T <: HttpResponse](val response: T) extends AnyVal {
    /**
     * Gets Link header values.
     *
     * @return header values or empty sequence if Link is not present
     */
    def link: Seq[LinkValue] =
      getLink.getOrElse(Nil)

    /** Gets Link header values if present. */
    def getLink: Option[Seq[LinkValue]] =
      response.getHeaderValue("Link").map(LinkValue.parseAll)

    /** Creates new response setting Link header to supplied values. */
    def withLink(values: LinkValue*): T#MessageType =
      response.withHeader(Header("Link", values.mkString(", ")))

    /** Creates new response removing Link header. */
    def removeLink: T#MessageType =
      response.removeHeaders("Link")
  }

  /** Provides standardized access to Location header. */
  implicit class Location[T <: HttpResponse](val response: T) extends AnyVal {
    /**
     * Gets Location header value.
     *
     * @throws HeaderNotFound if Location is not present
     */
    def location: URI =
      getLocation.getOrElse(throw HeaderNotFound("Location"))

    /** Gets Location header value if present. */
    def getLocation: Option[URI] =
      response.getHeaderValue("Location").map(new URI(_))

    /** Creates new response setting Location header to supplied value. */
    def withLocation(value: URI): T#MessageType =
      response.withHeader(Header("Location", value.toString))

    /** Creates new response removing Location header. */
    def removeLocation: T#MessageType =
      response.removeHeaders("Location")
  }

  /** Provides standardized access to Max-Forwards header. */
  implicit class MaxForwards[T <: HttpRequest](val request: T) extends AnyVal {
    /**
     * Gets Max-Forwards header value.
     *
     * @throws HeaderNotFound if Max-Forwards is not present
     */
    def maxForwards: Long =
      getMaxForwards.getOrElse(throw HeaderNotFound("Max-Forwards"))

    /** Gets Max-Forwards header value if present. */
    def getMaxForwards: Option[Long] =
      request.getHeaderValue("Max-Forwards").map(_.toLong)

    /** Creates new request setting Max-Forwards header to supplied value. */
    def withMaxForwards(value: Long): T#MessageType =
      request.withHeader(Header("Max-Forwards", value))

    /** Creates new request removing Max-Forwards header. */
    def removeMaxForwards: T#MessageType =
      request.removeHeaders("Max-Forwards")
  }

  /** Provides standardized access to Pragma header. */
  implicit class Pragma[T <: HttpRequest](val request: T) extends AnyVal {
    /**
     * Gets Pragma header values.
     *
     * @return header values or empty sequence if Pragma is not present
     */
    def pragma: Seq[PragmaDirective] =
      getPragma.getOrElse(Nil)

    /** Gets Pragma header values if present. */
    def getPragma: Option[Seq[PragmaDirective]] =
      request.getHeaderValue("Pragma").map(PragmaDirective.parseAll)

    /** Creates new request setting Pragma header to supplied values. */
    def withPragma(values: PragmaDirective*): T#MessageType =
      request.withHeader(Header("Pragma", values.mkString(", ")))

    /** Creates new request removing Pragma header. */
    def removePragma: T#MessageType =
      request.removeHeaders("Pragma")
  }

  /** Provides standardized access to Proxy-Authenticate header. */
  implicit class ProxyAuthenticate[T <: HttpResponse](val response: T) extends AnyVal {
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
        .flatMap(Challenge.parseAll) match {
          case Nil => None
          case seq => Some(seq)
        }

    /**
     * Creates new response setting Proxy-Authenticate header to supplied
     * values.
     */
    def withProxyAuthenticate(values: Challenge*): T#MessageType =
      response.withHeader(Header("Proxy-Authenticate", values.mkString(", ")))

    /** Creates new response removing Date header. */
    def removeProxyAuthenticate: T#MessageType =
      response.removeHeaders("Proxy-Authenticate")
  }

  /** Provides standardized access to Proxy-Authentication-Info header. */
  implicit class ProxyAuthenticationInfo[T <: HttpResponse](val response: T) extends AnyVal {
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

    /**
     * Creates new response setting Proxy-Authentication-Info header to supplied
     * value.
     */
    def withProxyAuthenticationInfo(values: (String, String)*): T#MessageType =
      response.withHeader(Header("Proxy-Authentication-Info", AuthParams.format(values.toMap).trim))

    /** Creates new response removing Date header. */
    def removeProxyAuthenticationInfo: T#MessageType =
      response.removeHeaders("Proxy-Authentication-Info")
  }

  /** Provides standardized access to Proxy-Authorization header. */
  implicit class ProxyAuthorization[T <: HttpRequest](val request: T) extends AnyVal {
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
    def withProxyAuthorization(value: Credentials): T#MessageType =
      request.withHeader(Header("Proxy-Authorization", value.toString))

    /** Creates new request removing Proxy-Authorization header. */
    def removeProxyAuthorization: T#MessageType =
      request.removeHeaders("Proxy-Authorization")
  }

  /** Provides standardized access to Range header. */
  implicit class Range[T <: HttpRequest](val request: T) extends AnyVal {
    /**
     * Gets Range header value.
     *
     * @throws HeaderNotFound if Range is not present
     */
    def range: ByteRange =
      getRange.getOrElse(throw HeaderNotFound("Range"))

    /** Gets Range header value if present. */
    def getRange: Option[ByteRange] =
      request.getHeaderValue("Range").map(ByteRange.parse)

    /** Creates new request setting Range header to supplied value. */
    def withRange(value: ByteRange): T#MessageType =
      request.withHeader(Header("Range", value.toString))

    /** Creates new request removing Range header. */
    def removeRange: T#MessageType =
      request.removeHeaders("Range")
  }

  /** Provides standardized access to Referer header. */
  implicit class Referer[T <: HttpRequest](val request: T) extends AnyVal {
    /**
     * Gets Referer header value.
     *
     * @throws HeaderNotFound if Referer is not present
     */
    def referer: URI =
      getReferer.getOrElse(throw HeaderNotFound("Referer"))

    /** Gets Referer header value if present. */
    def getReferer: Option[URI] =
      request.getHeaderValue("Referer").map(new URI(_))

    /** Creates new request setting Referer header to supplied value. */
    def withReferer(value: URI): T#MessageType =
      request.withHeader(Header("Referer", value.toString))

    /** Creates new request removing Referer header. */
    def removeReferer: T#MessageType =
      request.removeHeaders("Referer")
  }

  /** Provides standardized access to Retry-After header. */
  implicit class RetryAfter[T <: HttpResponse](val response: T) extends AnyVal {
    /**
     * Gets Retry-After header value.
     *
     * @throws HeaderNotFound if Retry-After is not present
     */
    def retryAfter: OffsetDateTime =
      getRetryAfter.getOrElse(throw HeaderNotFound("Retry-After"))

    /** Gets Retry-After header value if present. */
    def getRetryAfter: Option[OffsetDateTime] =
      response.getHeaderValue("Retry-After").map(DateValue.parse)

    /** Creates new response setting Retry-After header to supplied value. */
    def withRetryAfter(value: OffsetDateTime): T#MessageType =
      response.withHeader(Header("Retry-After", value))

    /** Creates new response removing Retry-After header. */
    def removeRetryAfter: T#MessageType =
      response.removeHeaders("Retry-After")
  }

  /** Provides standardized access to Server header. */
  implicit class Server[T <: HttpResponse](val response: T) extends AnyVal {
    /**
     * Gets Server header values.
     *
     * @return header values or empty sequence if Server is not present
     */
    def server: Seq[ProductType] =
      getServer.getOrElse(Nil)

    /** Gets Server header values if present. */
    def getServer: Option[Seq[ProductType]] =
      response.getHeaderValue("Server").map(ProductType.parseAll)

    /** Creates new response setting Server header to supplied values. */
    def withServer(values: ProductType*): T#MessageType =
      response.withHeader(Header("Server", values.mkString(" ")))

    /** Creates new response removing Server header. */
    def removeServer: T#MessageType =
      response.removeHeaders("Server")
  }

  /** Provides standardized access to TE header. */
  implicit class TE[T <: HttpRequest](val request: T) extends AnyVal {
    /**
     * Gets TE header values.
     *
     * @return header values or empty sequence if TE is not present
     */
    def te: Seq[TransferCodingRange] =
      getTE.getOrElse(Nil)

    /** Gets TE header values if present. */
    def getTE: Option[Seq[TransferCodingRange]] =
      request.getHeaderValue("TE")
        .map(ListParser.apply)
        .map(_.map(TransferCodingRange.parse))

    /** Creates new request setting TE header to supplied values. */
    def withTE(values: TransferCodingRange*): T#MessageType =
      request.withHeader(Header("TE", values.mkString(", ")))

    /** Creates new request removing TE header. */
    def removeTE: T#MessageType =
      request.removeHeaders("TE")
  }

  /** Provides standardized access to Trailer header. */
  implicit class Trailer[T <: HttpMessage](val message: T) extends AnyVal {
    /**
     * Gets Trailer header values.
     *
     * @return header values or empty sequence if Trailer is not present
     */
    def vary: Seq[String] =
      getTrailer.getOrElse(Nil)

    /** Gets Trailer header values if present. */
    def getTrailer: Option[Seq[String]] =
      message.getHeaderValue("Trailer").map(ListParser.apply)

    /** Creates new message setting Trailer header to supplied values. */
    def withTrailer(values: String*): T#MessageType =
      message.withHeader(Header("Trailer", values.mkString(", ")))

    /** Creates new message removing Trailer header. */
    def removeTrailer: T#MessageType =
      message.removeHeaders("Trailer")
  }

  /** Provides standardized access to Transfer-Encoding header. */
  implicit class TransferEncoding[T <: HttpMessage](val message: T) extends AnyVal {
    /**
     * Gets Transfer-Encoding header values.
     *
     * @return header values or empty sequence if Transfer-Encoding is not present
     */
    def transferEncoding: Seq[TransferCoding] =
      getTransferEncoding.getOrElse(Nil)

    /** Gets Transfer-Encoding header values if present. */
    def getTransferEncoding: Option[Seq[TransferCoding]] =
      message.getHeaderValue("Transfer-Encoding")
        .map(ListParser.apply)
        .map(_.map(TransferCoding.parse))

    /**
     * Creates new message setting Transfer-Encoding header to supplied values.
     */
    def withTransferEncoding(values: TransferCoding*): T#MessageType =
      message.withHeader(Header("Transfer-Encoding", values.mkString(", ")))

    /** Creates new message removing Transfer-Encoding header. */
    def removeTransferEncoding: T#MessageType =
      message.removeHeaders("Transfer-Encoding")
  }

  /** Provides standardized access to Upgrade header. */
  implicit class Upgrade[T <: HttpRequest](val request: T) extends AnyVal {
    /**
     * Gets Upgrade header values.
     *
     * @return header values or empty sequence if Upgrade is not present
     */
    def upgrade: Seq[Protocol] =
      getUpgrade.getOrElse(Nil)

    /** Gets Upgrade header values if present. */
    def getUpgrade: Option[Seq[Protocol]] =
      request.getHeaderValue("Upgrade")
        .map(ListParser.apply)
        .map(_.map(Protocol.parse))

    /** Creates new request setting Upgrade header to supplied values. */
    def withUpgrade(values: Protocol*): T#MessageType =
      request.withHeader(Header("Upgrade", values.mkString(", ")))

    /** Creates new request removing Upgrade header. */
    def removeUpgrade: T#MessageType =
      request.removeHeaders("Upgrade")
  }

  /** Provides standardized access to User-Agent header. */
  implicit class UserAgent[T <: HttpRequest](val request: T) extends AnyVal {
    /**
     * Gets User-Agent header values.
     *
     * @return header values or empty sequence if User-Agent is not present
     */
    def userAgent: Seq[ProductType] =
      getUserAgent.getOrElse(Nil)

    /** Gets User-Agent header values if present. */
    def getUserAgent: Option[Seq[ProductType]] =
      request.getHeaderValue("User-Agent").map(ProductType.parseAll)

    /** Creates new request setting User-Agent header to supplied value. */
    def withUserAgent(values: ProductType*): T#MessageType =
      request.withHeader(Header("User-Agent", values.mkString(" ")))

    /** Creates new request removing User-Agent header. */
    def removeUserAgent: T#MessageType =
      request.removeHeaders("User-Agent")
  }

  /** Provides standardized access to Vary header. */
  implicit class Vary[T <: HttpResponse](val response: T) extends AnyVal {
    /**
     * Gets Vary header values.
     *
     * @return header values or empty sequence if Vary is not present
     */
    def vary: Seq[String] =
      getVary.getOrElse(Nil)

    /** Gets Vary header values if present. */
    def getVary: Option[Seq[String]] =
      response.getHeaderValue("Vary").map(ListParser.apply)

    /** Creates new response setting Vary header to supplied values. */
    def withVary(values: String*): T#MessageType =
      response.withHeader(Header("Vary", values.mkString(", ")))

    /** Creates new response removing Vary header. */
    def removeVary: T#MessageType =
      response.removeHeaders("Vary")
  }

  /** Provides standardized access to Via header. */
  implicit class Via[T <: HttpResponse](val response: T) extends AnyVal {
    /**
     * Gets Via header values.
     *
     * @return header values or empty sequence if Via is not present
     */
    def via: Seq[ViaType] =
      getVia.getOrElse(Nil)

    /** Gets Via header values if present. */
    def getVia: Option[Seq[ViaType]] =
      response.getHeaderValue("Via").map(ViaType.parseAll)

    /** Creates new response setting Via header to supplied values. */
    def withVia(values: ViaType*): T#MessageType =
      response.withHeader(Header("Via", values.mkString(", ")))

    /** Creates new response removing Via header. */
    def removeVia: T#MessageType =
      response.removeHeaders("Via")
  }

  /** Provides standardized access to Warning header. */
  implicit class Warning[T <: HttpResponse](val response: T) extends AnyVal {
    /**
     * Gets Warning header values.
     *
     * @return header values or empty sequence if Warning is not present
     */
    def warning: Seq[WarningType] =
      getWarning.getOrElse(Nil)

    /** Gets Warning header values if present. */
    def getWarning: Option[Seq[WarningType]] =
      response.getHeaderValue("Warning").map(WarningType.parseAll)

    /** Creates new response setting Warning header to supplied values. */
    def withWarning(values: WarningType*): T#MessageType =
      response.withHeader(Header("Warning", values.mkString(", ")))

    /** Creates new response removing Warning header. */
    def removeWarning: T#MessageType =
      response.removeHeaders("Warning")
  }

  /** Provides standardized access to WWW-Authenticate header. */
  implicit class WWWAuthenticate[T <: HttpResponse](val response: T) extends AnyVal {
    /**
     * Gets WWW-Authenticate header values.
     *
     * @return header values or empty sequence if WWW-Authenticate is not present
     */
    def wwwAuthenticate: Seq[Challenge] =
      getWWWAuthenticate.getOrElse(Nil)

    /** Gets WWW-Authenticate header values if present. */
    def getWWWAuthenticate: Option[Seq[Challenge]] =
      response.getHeaderValues("WWW-Authenticate")
        .flatMap(Challenge.parseAll) match {
          case Nil => None
          case seq => Some(seq)
        }

    /**
     * Creates new response setting WWW-Authenticate header to supplied values.
     */
    def withWWWAuthenticate(values: Challenge*): T#MessageType =
      response.withHeader(Header("WWW-Authenticate", values.mkString(", ")))

    /** Creates new response removing WWW-Authenticate header. */
    def removeWWWAuthenticate: T#MessageType =
      response.removeHeaders("WWW-Authenticate")
  }
}
