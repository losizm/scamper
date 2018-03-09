package scamper

import java.time.OffsetDateTime
import scamper.types._

/** Contains type classes for standardized access to message headers. */
package object headers {
  /** Provides standardized access to Accept header. */
  implicit class Accept[T <: HttpRequest](val request: T) {
    /**
     * Gets Accept header value.
     *
     * @throws HeaderNotFound if Accept is not present
     */
    def accept: Seq[MediaRange] =
      getAccept.getOrElse(throw HeaderNotFound("Accept"))

    /** Gets Accept header value if present. */
    def getAccept: Option[Seq[MediaRange]] =
      request.getHeaderValue("Accept")
        .map(ListParser(_))
        .map(_.map(MediaRange(_)))

    /** Creates new request setting Accept header to supplied values. */
    def withAccept(values: MediaRange*): request.MessageType =
      request.withHeader(Header("Accept", values.mkString(", ")))

    /** Creates new request removing Accept header. */
    def removeAccept: request.MessageType =
      request.removeHeaders("Accept")
  }

  /** Provides standardized access to Accept-Charset header. */
  implicit class AcceptCharset[T <: HttpRequest](val request: T) {
    /**
     * Gets Accept-Charset header value.
     *
     * @throws HeaderNotFound if Accept-Charset is not present
     */
    def acceptCharset: Seq[CharsetRange] =
      getAcceptCharset.getOrElse(throw HeaderNotFound("Accept-Charset"))

    /** Gets Accept-Charset header value if present. */
    def getAcceptCharset: Option[Seq[CharsetRange]] =
      request.getHeaderValue("Accept-Charset")
        .map(ListParser(_))
        .map(_.map(CharsetRange(_)))

    /** Creates new request setting Accept-Charset header to supplied values. */
    def withAcceptCharset(values: CharsetRange*): request.MessageType =
      request.withHeader(Header("Accept-Charset", values.mkString(", ")))

    /** Creates new request removing Accept-Charset header. */
    def removeAcceptCharset: request.MessageType =
      request.removeHeaders("Accept-Charset")
  }

  /** Provides standardized access to Accept-Encoding header. */
  implicit class AcceptEncoding[T <: HttpRequest](val request: T) {
    /**
     * Gets Accept-Encoding header value.
     *
     * @throws HeaderNotFound if Accept-Encoding is not present
     */
    def acceptEncoding: Seq[ContentCodingRange] =
      getAcceptEncoding.getOrElse(throw HeaderNotFound("Accept-Encoding"))

    /** Gets Accept-Encoding header value if present. */
    def getAcceptEncoding: Option[Seq[ContentCodingRange]] =
      request.getHeaderValue("Accept-Encoding")
        .map(ListParser(_))
        .map(_.map(ContentCodingRange(_)))

    /**
     * Creates new request setting Accept-Encoding header to supplied values.
     */
    def withAcceptEncoding(values: ContentCodingRange*): request.MessageType =
      request.withHeader(Header("Accept-Encoding", values.mkString(", ")))

    /** Creates new request removing Accept-Encoding header. */
    def removeAcceptEncoding: request.MessageType =
      request.removeHeaders("Accept-Encoding")
  }

  /** Provides standardized access to Accept-Language header. */
  implicit class AcceptLanguage[T <: HttpRequest](val request: T) {
    /**
     * Gets Accept-Language header value.
     *
     * @throws HeaderNotFound if Accept-Language is not present
     */
    def acceptLanguage: Seq[LanguageRange] =
      getAcceptLanguage.getOrElse(throw HeaderNotFound("Accept-Language"))

    /** Gets Accept-Language header value if present. */
    def getAcceptLanguage: Option[Seq[LanguageRange]] =
      request.getHeaderValue("Accept-Language")
        .map(ListParser(_))
        .map(_.map(LanguageRange(_)))

    /** Creates new request setting Accept-Language header to supplied values. */
    def withAcceptLanguage(values: LanguageRange*): request.MessageType =
      request.withHeader(Header("Accept-Language", values.mkString(", ")))

    /** Creates new request removing Accept-Language header. */
    def removeAcceptLanguage: request.MessageType =
      request.removeHeaders("Accept-Language")
  }

  /** Provides standardized access to Accept-Ranges header. */
  implicit class AcceptRanges[T <: HttpResponse](val response: T) {
    /**
     * Gets Accept-Ranges header value.
     *
     * @throws HeaderNotFound if Accept-Ranges is not present
     */
    def acceptRanges: Seq[String] =
      getAcceptRanges.getOrElse(throw HeaderNotFound("Accept-Ranges"))

    /** Gets Accept-Ranges header value if present. */
    def getAcceptRanges: Option[Seq[String]] =
      response.getHeaderValue("Accept-Ranges").map(ListParser(_))

    /** Creates new response setting Accept-Ranges header to supplied values. */
    def withAcceptRanges(values: String*): response.MessageType =
      response.withHeader(Header("Accept-Ranges", values.mkString(", ")))

    /** Creates new response removing Accept-Ranges header. */
    def removeAcceptRanges: response.MessageType =
      response.removeHeaders("Accept-Ranges")
  }

  /** Provides standardized access to Age header. */
  implicit class Age[T <: HttpResponse](val response: T) {
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
    def withAge(value: Long): response.MessageType =
      response.withHeader(Header("Age", value))

    /** Creates new response removing Age header. */
    def removeAge: response.MessageType =
      response.removeHeaders("Age")
  }

  /** Provides standardized access to Allow header. */
  implicit class Allow[T <: HttpResponse](val response: T) {
    /**
     * Gets Allow header value.
     *
     * @throws HeaderNotFound if Allow is not present
     */
    def allow: Seq[String] =
      getAllow.getOrElse(throw HeaderNotFound("Allow"))

    /** Gets Allow header value if present. */
    def getAllow: Option[Seq[String]] =
      response.getHeaderValue("Allow").map(ListParser(_))

    /** Creates new response setting Allow header to supplied values. */
    def withAllow(values: String*): response.MessageType =
      response.withHeader(Header("Allow", values.mkString(", ")))

    /** Creates new response removing Allow header. */
    def removeAllow: response.MessageType =
      response.removeHeaders("Allow")
  }

  /** Provides standardized access to Authentication-Info header. */
  implicit class AuthenticationInfo[T <: HttpResponse](val response: T) {
    /**
     * Gets Authentication-Info header value.
     *
     * @throws HeaderNotFound if Authentication-Info is not present
     */
    def authenticationInfo: String =
      getAuthenticationInfo.getOrElse(throw HeaderNotFound("Authentication-Info"))

    /** Gets Authentication-Info header value if present. */
    def getAuthenticationInfo: Option[String] =
      response.getHeaderValue("Authentication-Info")

    /**
     * Creates new response setting Authentication-Info header to supplied
     * value.
     */
    def withAuthenticationInfo(value: String): response.MessageType =
      response.withHeader(Header("Authentication-Info", value))

    /** Creates new response removing Authentication-Info header. */
    def removeAuthenticationInfo: response.MessageType =
      response.removeHeaders("Authentication-Info")
  }

  /** Provides standardized access to Authorization header. */
  implicit class Authorization[T <: HttpRequest](val request: T) {
    /**
     * Gets Authorization header value.
     *
     * @throws HeaderNotFound if Authorization is not present
     */
    def authorization: String =
      getAuthorization.getOrElse(throw HeaderNotFound("Authorization"))

    /** Gets Authorization header value if present. */
    def getAuthorization: Option[String] =
      request.getHeaderValue("Authorization")

    /** Creates new request setting Authorization header to supplied value. */
    def withAuthorization(value: String): request.MessageType =
      request.withHeader(Header("Authorization", value))

    /** Creates new request removing Authorization header. */
    def removeAuthorization: request.MessageType =
      request.removeHeaders("Authorization")
  }

  /** Provides standardized access to Cache-Control header. */
  implicit class CacheControl[T <: HttpMessage](val message: T) {
    /**
     * Gets Cache-Control header value.
     *
     * @throws HeaderNotFound if Cache-Control is not present
     */
    def cacheControl: Seq[String] =
      getCacheControl.getOrElse(throw HeaderNotFound("Cache-Control"))

    /** Gets Cache-Control header value if present. */
    def getCacheControl: Option[Seq[String]] =
      message.getHeaderValue("Cache-Control").map(ListParser(_))

    /** Creates new message setting Cache-Control header to supplied values. */
    def withCacheControl(values: String*): message.MessageType =
      message.withHeader(Header("Cache-Control", values.mkString(", ")))

    /** Creates new message removing Cache-Control header. */
    def removeCacheControl: message.MessageType =
      message.removeHeaders("Cache-Control")
  }

  /** Provides standardized access to Content-Disposition header. */
  implicit class ContentDisposition[T <: HttpResponse](val response: T) {
    /**
     * Gets Content-Disposition header value.
     *
     * @throws HeaderNotFound if Content-Disposition is not present
     */
    def contentDisposition: ContentDispositionType =
      getContentDisposition.getOrElse(throw HeaderNotFound("Content-Disposition"))

    /** Gets Content-Disposition header value if present. */
    def getContentDisposition: Option[ContentDispositionType] =
      response.getHeaderValue("Content-Disposition").map(ContentDispositionType(_))

    /**
     * Creates new response setting Content-Disposition header to supplied
     * value.
     */
    def withContentDisposition(value: ContentDispositionType): response.MessageType =
      response.withHeader(Header("Content-Disposition", value.toString))

    /** Creates new response removing Content-Disposition header. */
    def removeContentDisposition: response.MessageType =
      response.removeHeaders("Content-Disposition")
  }

  /** Provides standardized access to Content-Encoding header. */
  implicit class ContentEncoding[T <: HttpMessage](val message: T) {
    /**
     * Gets Content-Encoding header value.
     *
     * @throws HeaderNotFound if Content-Encoding is not present
     */
    def contentEncoding: Seq[ContentCoding] =
      getContentEncoding.getOrElse(throw HeaderNotFound("Content-Encoding"))

    /** Gets Content-Encoding header value if present. */
    def getContentEncoding: Option[Seq[ContentCoding]] =
      message.getHeaderValue("Content-Encoding")
        .map(ListParser(_))
        .map(_.map(ContentCoding(_)))

    /**
     * Creates new message setting Content-Encoding header to supplied values.
     */
    def withContentEncoding(values: ContentCoding*): message.MessageType =
      message.withHeader(Header("Content-Encoding", values.mkString(", ")))

    /** Creates new message removing Content-Encoding header. */
    def removeContentEncoding: message.MessageType =
      message.removeHeaders("Content-Encoding")
  }

  /** Provides standardized access to Content-Language header. */
  implicit class ContentLanguage[T <: HttpMessage](val message: T) {
    /**
     * Gets Content-Language header value.
     *
     * @throws HeaderNotFound if Content-Language is not present
     */
    def contentLanguage: Seq[LanguageTag] =
      getContentLanguage.getOrElse(throw HeaderNotFound("Content-Language"))

    /** Gets Content-Language header value if present. */
    def getContentLanguage: Option[Seq[LanguageTag]] =
      message.getHeaderValue("Content-Language")
        .map(ListParser(_))
        .map(_.map(LanguageTag(_)))

    /** Creates new message setting Content-Language header to supplied value. */
    def withContentLanguage(values: LanguageTag*): message.MessageType =
      message.withHeader(Header("Content-Language", values.mkString(", ")))

    /** Creates new message removing Content-Language header. */
    def removeContentLanguage: message.MessageType =
      message.removeHeaders("Content-Language")
  }

  /** Provides standardized access to Content-Length header. */
  implicit class ContentLength[T <: HttpMessage](val message: T) {
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
    def withContentLength(value: Long): message.MessageType =
      message.withHeader(Header("Content-Length", value))

    /** Creates new message removing Content-Length header. */
    def removeContentLength: message.MessageType =
      message.removeHeaders("Content-Length")
  }

  /** Provides standardized access to Content-Location header. */
  implicit class ContentLocation[T <: HttpResponse](val response: T) {
    /**
     * Gets Content-Location header value.
     *
     * @throws HeaderNotFound if Content-Location is not present
     */
    def contentLocation: String =
      getContentLocation.getOrElse(throw HeaderNotFound("Content-Location"))

    /** Gets Content-Location header value if present. */
    def getContentLocation: Option[String] =
      response.getHeaderValue("Content-Location")

    /**
     * Creates new response setting Content-Location header to supplied value.
     */
    def withContentLocation(value: String): response.MessageType =
      response.withHeader(Header("Content-Location", value))

    /** Creates new response removing Content-Location header. */
    def removeContentLocation: response.MessageType =
      response.removeHeaders("Content-Location")
  }

  /** Provides standardized access to Content-Range header. */
  implicit class ContentRange[T <: HttpMessage](val message: T) {
    /**
     * Gets Content-Range header value.
     *
     * @throws HeaderNotFound if Content-Range is not present
     */
    def contentRange: ByteContentRange =
      getContentRange.getOrElse(throw HeaderNotFound("Content-Range"))

    /** Gets Content-Range header value if present. */
    def getContentRange: Option[ByteContentRange] =
      message.getHeaderValue("Content-Range").map(ByteContentRange(_))

    /** Creates new message setting Content-Range header to supplied value. */
    def withContentRange(value: ByteContentRange): message.MessageType =
      message.withHeader(Header("Content-Range", value.toString))

    /** Creates new message removing Content-Range header. */
    def removeContentRange: message.MessageType =
      message.removeHeaders("Content-Range")
  }

  /** Provides standardized access to Content-Type header. */
  implicit class ContentType[T <: HttpMessage](val message: T) {
    /**
     * Gets Content-Type header value.
     *
     * @throws HeaderNotFound if Content-Type is not present
     */
    def contentType: MediaType =
      getContentType.getOrElse(throw HeaderNotFound("Content-Type"))

    /** Gets Content-Type header value if present. */
    def getContentType: Option[MediaType] =
      message.getHeaderValue("Content-Type").map(MediaType(_))

    /** Creates new message setting Content-Type header to supplied value. */
    def withContentType(value: MediaType): message.MessageType =
      message.withHeader(Header("Content-Type", value.toString))

    /** Creates new message removing Content-Type header. */
    def removeContentType: message.MessageType =
      message.removeHeaders("Content-Type")
  }

  /** Provides standardized access to Connection header. */
  implicit class Connection[T <: HttpMessage](val message: T) {
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
    def withConnection(value: String): message.MessageType =
      message.withHeader(Header("Connection", value))

    /** Creates new message removing Connection header. */
    def removeConnection: message.MessageType =
      message.removeHeaders("Connection")
  }

  /** Provides standardized access to Date header. */
  implicit class Date[T <: HttpResponse](val response: T) {
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
    def withDate(value: OffsetDateTime): response.MessageType =
      response.withHeader(Header("Date", value))

    /** Creates new response removing Date header. */
    def removeDate: response.MessageType =
      response.removeHeaders("Date")
  }

  /** Provides standardized access to ETag header. */
  implicit class ETag[T <: HttpResponse](val response: T) {
    /**
     * Gets ETag header value.
     *
     * @throws HeaderNotFound if ETag is not present
     */
    def etag: String =
      getETag.getOrElse(throw HeaderNotFound("ETag"))

    /** Gets ETag header value if present. */
    def getETag: Option[String] =
      response.getHeaderValue("ETag")

    /** Creates new response setting ETag header to supplied value. */
    def withETag(value: String): response.MessageType =
      response.withHeader(Header("ETag", value))

    /** Creates new response removing ETag header. */
    def removeETag: response.MessageType =
      response.removeHeaders("ETag")
  }

  /** Provides standardized access to Expect header. */
  implicit class Expect[T <: HttpRequest](val request: T) {
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
    def withExpect(value: String): request.MessageType =
      request.withHeader(Header("Expect", value))

    /** Creates new request removing Expect header. */
    def removeExpect: request.MessageType =
      request.removeHeaders("Expect")
  }

  /** Provides standardized access to Expires header. */
  implicit class Expires[T <: HttpResponse](val response: T) {
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
    def withExpires(value: OffsetDateTime): response.MessageType =
      response.withHeader(Header("Expires", value))

    /** Creates new response removing Expires header. */
    def removeExpires: response.MessageType =
      response.removeHeaders("Expires")
  }

  /** Provides standardized access to From header. */
  implicit class From[T <: HttpRequest](val request: T) {
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
    def withFrom(value: String): request.MessageType =
      request.withHeader(Header("From", value))

    /** Creates new request removing From header. */
    def removeFrom: request.MessageType =
      request.removeHeaders("From")
  }

  /** Provides standardized access to Host header. */
  implicit class Host[T <: HttpRequest](val request: T) {
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
    def withHost(value: String): request.MessageType =
      request.withHeader(Header("Host", value))

    /** Creates new request removing Host header. */
    def removeHost: request.MessageType =
      request.removeHeaders("Host")
  }

  /** Provides standardized access to If-Match header. */
  implicit class IfMatch[T <: HttpRequest](val request: T) {
    /**
     * Gets If-Match header value.
     *
     * @throws HeaderNotFound if If-Match is not present
     */
    def ifMatch: String =
      getIfMatch.getOrElse(throw HeaderNotFound("If-Match"))

    /** Gets If-Match header value if present. */
    def getIfMatch: Option[String] =
      request.getHeaderValue("If-Match")

    /** Creates new request setting If-Match header to supplied value. */
    def withIfMatch(value: String): request.MessageType =
      request.withHeader(Header("If-Match", value))

    /** Creates new request removing If-Match header. */
    def removeIfMatch: request.MessageType =
      request.removeHeaders("If-Match")
  }

  /** Provides standardized access to If-Modified-Since header. */
  implicit class IfModifiedSince[T <: HttpRequest](val request: T) {
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
    def withIfModifiedSince(value: OffsetDateTime): request.MessageType =
      request.withHeader(Header("If-Modified-Since", value))

    /** Creates new request removing If-Modified-Since header. */
    def removeIfModifiedSince: request.MessageType =
      request.removeHeaders("If-Modified-Since")
  }

  /** Provides standardized access to If-None-Match header. */
  implicit class IfNoneMatch[T <: HttpRequest](val request: T) {
    /**
     * Gets If-None-Match header value.
     *
     * @throws HeaderNotFound if If-None-Match is not present
     */
    def ifNoneMatch: String =
      getIfNoneMatch.getOrElse(throw HeaderNotFound("If-None-Match"))

    /** Gets If-None-Match header value if present. */
    def getIfNoneMatch: Option[String] =
      request.getHeaderValue("If-None-Match")

    /** Creates new request setting If-None-Match header to supplied value. */
    def withIfNoneMatch(value: String): request.MessageType =
      request.withHeader(Header("If-None-Match", value))

    /** Creates new request removing If-None-Match header. */
    def removeIfNoneMatch: request.MessageType =
      request.removeHeaders("If-None-Match")
  }

  /** Provides standardized access to If-Range header. */
  implicit class IfRange[T <: HttpRequest](val request: T) {
    /**
     * Gets If-Range header value.
     *
     * @throws HeaderNotFound if If-Range is not present
     */
    def ifRange: String =
      getIfRange.getOrElse(throw HeaderNotFound("If-Range"))

    /** Gets If-Range header value if present. */
    def getIfRange: Option[String] =
      request.getHeaderValue("If-Range")

    /** Creates new request setting If-Range header to supplied value. */
    def withIfRange(value: String): request.MessageType =
      request.withHeader(Header("If-Range", value))

    /** Creates new request removing If-Range header. */
    def removeIfRange: request.MessageType =
      request.removeHeaders("If-Range")
  }

  /** Provides standardized access to If-Unmodified-Since header. */
  implicit class IfUnmodifiedSince[T <: HttpRequest](val request: T) {
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
    def withIfUnmodifiedSince(value: OffsetDateTime): request.MessageType =
      request.withHeader(Header("If-Unmodified-Since", value))

    /** Creates new request removing If-Unmodified-Since header. */
    def removeIfUnmodifiedSince: request.MessageType =
      request.removeHeaders("If-Unmodified-Since")
  }

  /** Provides standardized access to Last-Modified header. */
  implicit class LastModified[T <: HttpResponse](val response: T) {
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
    def withLastModified(value: OffsetDateTime): response.MessageType =
      response.withHeader(Header("Last-Modified", value))

    /** Creates new response removing Last-Modified header. */
    def removeLastModified: response.MessageType =
      response.removeHeaders("Last-Modified")
  }

  /** Provides standardized access to Link header. */
  implicit class Link[T <: HttpResponse](val response: T) {
    /**
     * Gets Link header value.
     *
     * @throws HeaderNotFound if Link is not present
     */
    def link: Seq[String] =
      getLink.getOrElse(throw HeaderNotFound("Link"))

    /** Gets Link header value if present. */
    def getLink: Option[Seq[String]] =
      response.getHeaderValue("Link").map(ListParser(_))

    /** Creates new response setting Link header to supplied values. */
    def withLink(values: String*): response.MessageType =
      response.withHeader(Header("Link", values.mkString(", ")))

    /** Creates new response removing Link header. */
    def removeLink: response.MessageType =
      response.removeHeaders("Link")
  }

  /** Provides standardized access to Location header. */
  implicit class Location[T <: HttpResponse](val response: T) {
    /**
     * Gets Location header value.
     *
     * @throws HeaderNotFound if Location is not present
     */
    def location: String =
      getLocation.getOrElse(throw HeaderNotFound("Location"))

    /** Gets Location header value if present. */
    def getLocation: Option[String] =
      response.getHeaderValue("Location")

    /** Creates new response setting Location header to supplied value. */
    def withLocation(value: String): response.MessageType =
      response.withHeader(Header("Location", value))

    /** Creates new response removing Location header. */
    def removeLocation: response.MessageType =
      response.removeHeaders("Location")
  }

  /** Provides standardized access to Max-Forwards header. */
  implicit class MaxForwards[T <: HttpRequest](val request: T) {
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
    def withMaxForwards(value: Long): request.MessageType =
      request.withHeader(Header("Max-Forwards", value))

    /** Creates new request removing Max-Forwards header. */
    def removeMaxForwards: request.MessageType =
      request.removeHeaders("Max-Forwards")
  }

  /** Provides standardized access to Pragma header. */
  implicit class Pragma[T <: HttpRequest](val request: T) {
    /**
     * Gets Pragma header value.
     *
     * @throws HeaderNotFound if Pragma is not present
     */
    def pragma: Seq[String] =
      getPragma.getOrElse(throw HeaderNotFound("Pragma"))

    /** Gets Pragma header value if present. */
    def getPragma: Option[Seq[String]] =
      request.getHeaderValue("Pragma").map(ListParser(_))

    /** Creates new request setting Pragma header to supplied values. */
    def withPragma(values: String*): request.MessageType =
      request.withHeader(Header("Pragma", values.mkString(", ")))

    /** Creates new request removing Pragma header. */
    def removePragma: request.MessageType =
      request.removeHeaders("Pragma")
  }

  /** Provides standardized access to Proxy-Authentication header. */
  implicit class ProxyAuthentication[T <: HttpResponse](val response: T) {
    /**
     * Gets Proxy-Authentication header value.
     *
     * @throws HeaderNotFound if Proxy-Authentication is not present
     */
    def proxyAuthentication: String =
      getProxyAuthentication.getOrElse(throw HeaderNotFound("Proxy-Authentication"))

    /** Gets Proxy-Authentication header value if present. */
    def getProxyAuthentication: Option[String] =
      response.getHeaderValue("Proxy-Authentication")

    /**
     * Creates new response setting Proxy-Authentication header to supplied
     * value.
     */
    def withProxyAuthentication(value: String): response.MessageType =
      response.withHeader(Header("Proxy-Authentication", value))

    /** Creates new response removing Date header. */
    def removeProxyAuthentication: response.MessageType =
      response.removeHeaders("Proxy-Authentication")
  }

  /** Provides standardized access to Proxy-Authentication-Info header. */
  implicit class ProxyAuthenticationInfo[T <: HttpResponse](val response: T) {
    /**
     * Gets Proxy-Authentication-Info header value.
     *
     * @throws HeaderNotFound if Proxy-Authentication-Info is not present
     */
    def proxyAuthenticationInfo: String =
      getProxyAuthenticationInfo.getOrElse(throw HeaderNotFound("Proxy-Authentication-Info"))

    /** Gets Proxy-Authentication-Info header value if present. */
    def getProxyAuthenticationInfo: Option[String] =
      response.getHeaderValue("Proxy-Authentication-Info")

    /**
     * Creates new response setting Proxy-Authentication-Info header to supplied
     * value.
     */
    def withProxyAuthenticationInfo(value: String): response.MessageType =
      response.withHeader(Header("Proxy-Authentication-Info", value))

    /** Creates new response removing Date header. */
    def removeProxyAuthenticationInfo: response.MessageType =
      response.removeHeaders("Proxy-Authentication-Info")
  }

  /** Provides standardized access to Proxy-Authorization header. */
  implicit class ProxyAuthorization[T <: HttpRequest](val request: T) {
    /**
     * Gets Proxy-Authorization header value.
     *
     * @throws HeaderNotFound if Proxy-Authorization is not present
     */
    def proxyAuthorization: String =
      getProxyAuthorization.getOrElse(throw HeaderNotFound("Proxy-Authorization"))

    /** Gets Proxy-Authorization header value if present. */
    def getProxyAuthorization: Option[String] =
      request.getHeaderValue("Proxy-Authorization")

    /**
     * Creates new request setting Proxy-Authorization header to supplied value.
     */
    def withProxyAuthorization(value: String): request.MessageType =
      request.withHeader(Header("Proxy-Authorization", value))

    /** Creates new request removing Proxy-Authorization header. */
    def removeProxyAuthorization: request.MessageType =
      request.removeHeaders("Proxy-Authorization")
  }

  /** Provides standardized access to Range header. */
  implicit class Range[T <: HttpRequest](val request: T) {
    /**
     * Gets Range header value.
     *
     * @throws HeaderNotFound if Range is not present
     */
    def range: ByteRange =
      getRange.getOrElse(throw HeaderNotFound("Range"))

    /** Gets Range header value if present. */
    def getRange: Option[ByteRange] =
      request.getHeaderValue("Range").map(ByteRange(_))

    /** Creates new request setting Range header to supplied value. */
    def withRange(value: ByteRange): request.MessageType =
      request.withHeader(Header("Range", value.toString))

    /** Creates new request removing Range header. */
    def removeRange: request.MessageType =
      request.removeHeaders("Range")
  }

  /** Provides standardized access to Referer header. */
  implicit class Referer[T <: HttpRequest](val request: T) {
    /**
     * Gets Referer header value.
     *
     * @throws HeaderNotFound if Referer is not present
     */
    def referer: String =
      getReferer.getOrElse(throw HeaderNotFound("Referer"))

    /** Gets Referer header value if present. */
    def getReferer: Option[String] =
      request.getHeaderValue("Referer")

    /** Creates new request setting Referer header to supplied value. */
    def withReferer(value: String): request.MessageType =
      request.withHeader(Header("Referer", value))

    /** Creates new request removing Referer header. */
    def removeReferer: request.MessageType =
      request.removeHeaders("Referer")
  }

  /** Provides standardized access to Retry-After header. */
  implicit class RetryAfter[T <: HttpResponse](val response: T) {
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
    def withRetryAfter(value: OffsetDateTime): response.MessageType =
      response.withHeader(Header("Retry-After", value))

    /** Creates new response removing Retry-After header. */
    def removeRetryAfter: response.MessageType =
      response.removeHeaders("Retry-After")
  }

  /** Provides standardized access to Server header. */
  implicit class Server[T <: HttpResponse](val response: T) {
    /**
     * Gets Server header value.
     *
     * @throws HeaderNotFound if Server is not present
     */
    def server: String =
      getServer.getOrElse(throw HeaderNotFound("Server"))

    /** Gets Server header value if present. */
    def getServer: Option[String] =
      response.getHeaderValue("Server")

    /** Creates new response setting Server header to supplied value. */
    def withServer(value: String): response.MessageType =
      response.withHeader(Header("Server", value))

    /** Creates new response removing Server header. */
    def removeServer: response.MessageType =
      response.removeHeaders("Server")
  }

  /** Provides standardized access to TE header. */
  implicit class TE[T <: HttpRequest](val request: T) {
    /**
     * Gets TE header value.
     *
     * @throws HeaderNotFound if TE is not present
     */
    def te: Seq[TransferCodingRange] =
      getTE.getOrElse(throw HeaderNotFound("TE"))

    /** Gets TE header value if present. */
    def getTE: Option[Seq[TransferCodingRange]] =
      request.getHeaderValue("TE")
        .map(ListParser(_))
        .map(_.map(TransferCodingRange(_)))

    /** Creates new request setting TE header to supplied values. */
    def withTE(values: TransferCodingRange*): request.MessageType =
      request.withHeader(Header("TE", values.mkString(", ")))

    /** Creates new request removing TE header. */
    def removeTE: request.MessageType =
      request.removeHeaders("TE")
  }

  /** Provides standardized access to Trailer header. */
  implicit class Trailer[T <: HttpMessage](val message: T) {
    /**
     * Gets Trailer header value.
     *
     * @throws HeaderNotFound if Trailer is not present
     */
    def vary: Seq[String] =
      getTrailer.getOrElse(throw HeaderNotFound("Trailer"))

    /** Gets Trailer header value if present. */
    def getTrailer: Option[Seq[String]] =
      message.getHeaderValue("Trailer").map(ListParser(_))

    /** Creates new message setting Trailer header to supplied values. */
    def withTrailer(values: String*): message.MessageType =
      message.withHeader(Header("Trailer", values.mkString(", ")))

    /** Creates new message removing Trailer header. */
    def removeTrailer: message.MessageType =
      message.removeHeaders("Trailer")
  }

  /** Provides standardized access to Transfer-Encoding header. */
  implicit class TransferEncoding[T <: HttpMessage](val message: T) {
    /**
     * Gets Transfer-Encoding header value.
     *
     * @throws HeaderNotFound if Transfer-Encoding is not present
     */
    def transferEncoding: Seq[TransferCoding] =
      getTransferEncoding.getOrElse(throw HeaderNotFound("Transfer-Encoding"))

    /** Gets Transfer-Encoding header value if present. */
    def getTransferEncoding: Option[Seq[TransferCoding]] =
      message.getHeaderValue("Transfer-Encoding")
        .map(ListParser(_))
        .map(_.map(TransferCoding(_)))

    /**
     * Creates new message setting Transfer-Encoding header to supplied values.
     */
    def withTransferEncoding(values: TransferCoding*): message.MessageType =
      message.withHeader(Header("Transfer-Encoding", values.mkString(", ")))

    /** Creates new message removing Transfer-Encoding header. */
    def removeTransferEncoding: message.MessageType =
      message.removeHeaders("Transfer-Encoding")
  }

  /** Provides standardized access to User-Agent header. */
  implicit class UserAgent[T <: HttpRequest](val request: T) {
    /**
     * Gets User-Agent header value.
     *
     * @throws HeaderNotFound if User-Agent is not present
     */
    def userAgent: String =
      getUserAgent.getOrElse(throw HeaderNotFound("User-Agent"))

    /** Gets User-Agent header value if present. */
    def getUserAgent: Option[String] =
      request.getHeaderValue("User-Agent")

    /** Creates new request setting User-Agent header to supplied value. */
    def withUserAgent(value: String): request.MessageType =
      request.withHeader(Header("User-Agent", value))

    /** Creates new request removing User-Agent header. */
    def removeUserAgent: request.MessageType =
      request.removeHeaders("User-Agent")
  }

  /** Provides standardized access to Vary header. */
  implicit class Vary[T <: HttpResponse](val response: T) {
    /**
     * Gets Vary header value.
     *
     * @throws HeaderNotFound if Vary is not present
     */
    def vary: Seq[String] =
      getVary.getOrElse(throw HeaderNotFound("Vary"))

    /** Gets Vary header value if present. */
    def getVary: Option[Seq[String]] =
      response.getHeaderValue("Vary").map(ListParser(_))

    /** Creates new response setting Vary header to supplied values. */
    def withVary(values: String*): response.MessageType =
      response.withHeader(Header("Vary", values.mkString(", ")))

    /** Creates new response removing Vary header. */
    def removeVary: response.MessageType =
      response.removeHeaders("Vary")
  }

  /** Provides standardized access to Via header. */
  implicit class Via[T <: HttpResponse](val response: T) {
    /**
     * Gets Via header value.
     *
     * @throws HeaderNotFound if Via is not present
     */
    def via: Seq[String] =
      getVia.getOrElse(throw HeaderNotFound("Via"))

    /** Gets Via header value if present. */
    def getVia: Option[Seq[String]] =
      response.getHeaderValue("Via").map(ListParser(_))

    /** Creates new response setting Via header to supplied values. */
    def withVia(values: String*): response.MessageType =
      response.withHeader(Header("Via", values.mkString(", ")))

    /** Creates new response removing Via header. */
    def removeVia: response.MessageType =
      response.removeHeaders("Via")
  }

  /** Provides standardized access to Warning header. */
  implicit class Warning[T <: HttpResponse](val response: T) {
    /**
     * Gets Warning header value.
     *
     * @throws HeaderNotFound if Warning is not present
     */
    def warning: String =
      getWarning.getOrElse(throw HeaderNotFound("Warning"))

    /** Gets Warning header value if present. */
    def getWarning: Option[String] =
      response.getHeaderValue("Warning")

    /** Creates new response setting Warning header to supplied value. */
    def withWarning(value: String): response.MessageType =
      response.withHeader(Header("Warning", value))

    /** Creates new response removing Warning header. */
    def removeWarning: response.MessageType =
      response.removeHeaders("Warning")
  }

  /** Provides standardized access to WWW-Authentication header. */
  implicit class WWWAuthentication[T <: HttpResponse](val response: T) {
    /**
     * Gets WWW-Authentication header value.
     *
     * @throws HeaderNotFound if WWW-Authentication is not present
     */
    def wwwAuthentication: String =
      getWWWAuthentication.getOrElse(throw HeaderNotFound("WWW-Authentication"))

    /** Gets WWW-Authentication header value if present. */
    def getWWWAuthentication: Option[String] =
      response.getHeaderValue("WWW-Authentication")

    /**
     * Creates new response setting WWW-Authentication header to supplied value.
     */
    def withWWWAuthentication(value: String): response.MessageType =
      response.withHeader(Header("WWW-Authentication", value))

    /** Creates new response removing WWW-Authentication header. */
    def removeWWWAuthentication: response.MessageType =
      response.removeHeaders("WWW-Authentication")
  }
}

