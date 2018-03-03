package scamper

import java.time.OffsetDateTime

/** Contains type classes for accessing message headers. */
object ImplicitHeaders {
  /** Supports Accept header. */
  implicit class Accept[T <: HttpRequest](val request: T) {
    /**
     * Gets Accept header value.
     *
     * @throws HeaderNotFound if Accept is not present
     */
    def accept: Seq[MediaRange] =
      getAccept.getOrElse(throw new HeaderNotFound("Accept"))

    /** Gets Accept header value if present. */
    def getAccept: Option[Seq[MediaRange]] =
      request.getHeaderValue("Accept")
        .map(ListParser.apply)
        .map(_.map(MediaRange.apply))

    /** Creates new request setting Accept header to supplied values. */
    def withAccept(values: MediaRange*): request.MessageType =
      request.withHeader(Header("Accept", values.mkString(", ")))

    /** Creates new request removing Accept header. */
    def removeAccept: request.MessageType =
      request.removeHeaders("Accept")
  }

  /** Supports Accept-Charset header. */
  implicit class AcceptCharset[T <: HttpRequest](val request: T) {
    /**
     * Gets Accept-Charset header value.
     *
     * @throws HeaderNotFound if Accept-Charset is not present
     */
    def acceptCharset: String =
      getAcceptCharset.getOrElse(throw new HeaderNotFound("Accept-Charset"))

    /** Gets Accept-Charset header value if present. */
    def getAcceptCharset: Option[String] =
      request.getHeaderValue("Accept-Charset")

    /** Creates new request setting Accept-Charset header to supplied value. */
    def withAcceptCharset(value: String): request.MessageType =
      request.withHeader(Header("Accept-Charset", value))

    /** Creates new request removing Accept-Charset header. */
    def removeAcceptCharset: request.MessageType =
      request.removeHeaders("Accept-Charset")
  }

  /** Supports Accept-Encoding header. */
  implicit class AcceptEncoding[T <: HttpRequest](val request: T) {
    /**
     * Gets Accept-Encoding header value.
     *
     * @throws HeaderNotFound if Accept-Encoding is not present
     */
    def acceptEncoding: Seq[ContentCodingRange] =
      getAcceptEncoding.getOrElse(throw new HeaderNotFound("Accept-Encoding"))

    /** Gets Accept-Encoding header value if present. */
    def getAcceptEncoding: Option[Seq[ContentCodingRange]] =
      request.getHeaderValue("Accept-Encoding")
        .map(ListParser.apply)
        .map(_.map(ContentCodingRange.apply))

    /**
     * Creates new request setting Accept-Encoding header to supplied values.
     */
    def withAcceptEncoding(values: ContentCodingRange*): request.MessageType =
      request.withHeader(Header("Accept-Encoding", values.mkString(", ")))

    /** Creates new request removing Accept-Encoding header. */
    def removeAcceptEncoding: request.MessageType =
      request.removeHeaders("Accept-Encoding")
  }

  /** Supports Accept-Language header. */
  implicit class AcceptLanguage[T <: HttpRequest](val request: T) {
    /**
     * Gets Accept-Language header value.
     *
     * @throws HeaderNotFound if Accept-Language is not present
     */
    def acceptLanguage: Seq[String] =
      getAcceptLanguage.getOrElse(throw new HeaderNotFound("Accept-Language"))

    /** Gets Accept-Language header value if present. */
    def getAcceptLanguage: Option[Seq[String]] =
      request.getHeaderValue("Accept-Language").map(ListParser.apply)

    /** Creates new request setting Accept-Language header to supplied values. */
    def withAcceptLanguage(values: String*): request.MessageType =
      request.withHeader(Header("Accept-Language", values.mkString(", ")))

    /** Creates new request removing Accept-Language header. */
    def removeAcceptLanguage: request.MessageType =
      request.removeHeaders("Accept-Language")
  }

  /** Supports Accept-Ranges header. */
  implicit class AcceptRanges[T <: HttpResponse](val response: T) {
    /**
     * Gets Accept-Ranges header value.
     *
     * @throws HeaderNotFound if Accept-Ranges is not present
     */
    def acceptRanges: Seq[String] =
      getAcceptRanges.getOrElse(throw new HeaderNotFound("Accept-Ranges"))

    /** Gets Accept-Ranges header value if present. */
    def getAcceptRanges: Option[Seq[String]] =
      response.getHeaderValue("Accept-Ranges").map(ListParser.apply)

    /** Creates new response setting Accept-Ranges header to supplied values. */
    def withAcceptRanges(values: String*): response.MessageType =
      response.withHeader(Header("Accept-Ranges", values.mkString(", ")))

    /** Creates new response removing Accept-Ranges header. */
    def removeAcceptRanges: response.MessageType =
      response.removeHeaders("Accept-Ranges")
  }

  /** Supports Age header. */
  implicit class Age[T <: HttpResponse](val response: T) {
    /**
     * Gets Age header value.
     *
     * @throws HeaderNotFound if Age is not present
     */
    def age: Long =
      getAge.getOrElse(throw new HeaderNotFound("Age"))

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

  /** Supports Allow header. */
  implicit class Allow[T <: HttpResponse](val response: T) {
    /**
     * Gets Allow header value.
     *
     * @throws HeaderNotFound if Allow is not present
     */
    def allow: Seq[String] =
      getAllow.getOrElse(throw new HeaderNotFound("Allow"))

    /** Gets Allow header value if present. */
    def getAllow: Option[Seq[String]] =
      response.getHeaderValue("Allow").map(ListParser.apply)

    /** Creates new response setting Allow header to supplied values. */
    def withAllow(values: String*): response.MessageType =
      response.withHeader(Header("Allow", values.mkString(", ")))

    /** Creates new response removing Allow header. */
    def removeAllow: response.MessageType =
      response.removeHeaders("Allow")
  }

  /** Supports Authentication-Info header. */
  implicit class AuthenticationInfo[T <: HttpResponse](val response: T) {
    /**
     * Gets Authentication-Info header value.
     *
     * @throws HeaderNotFound if Authentication-Info is not present
     */
    def authenticationInfo: String =
      getAuthenticationInfo.getOrElse(throw new HeaderNotFound("Authentication-Info"))

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

  /** Supports Authorization header. */
  implicit class Authorization[T <: HttpRequest](val request: T) {
    /**
     * Gets Authorization header value.
     *
     * @throws HeaderNotFound if Authorization is not present
     */
    def authorization: String =
      getAuthorization.getOrElse(throw new HeaderNotFound("Authorization"))

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

  /** Supports Cache-Control header. */
  implicit class CacheControl[T <: HttpMessage](val message: T) {
    /**
     * Gets Cache-Control header value.
     *
     * @throws HeaderNotFound if Cache-Control is not present
     */
    def cacheControl: String =
      getCacheControl.getOrElse(throw new HeaderNotFound("Cache-Control"))

    /** Gets Cache-Control header value if present. */
    def getCacheControl: Option[String] =
      message.getHeaderValue("Cache-Control")

    /** Creates new message setting Cache-Control header to supplied value. */
    def withCacheControl(value: String): message.MessageType =
      message.withHeader(Header("Cache-Control", value))

    /** Creates new message removing Cache-Control header. */
    def removeCacheControl: message.MessageType =
      message.removeHeaders("Cache-Control")
  }

  /** Supports Content-Disposition header. */
  implicit class ContentDisposition[T <: HttpResponse](val response: T) {
    /**
     * Gets Content-Disposition header value.
     *
     * @throws HeaderNotFound if Content-Disposition is not present
     */
    def contentDisposition: String =
      getContentDisposition.getOrElse(throw new HeaderNotFound("Content-Disposition"))

    /** Gets Content-Disposition header value if present. */
    def getContentDisposition: Option[String] =
      response.getHeaderValue("Content-Disposition")

    /**
     * Creates new response setting Content-Disposition header to supplied
     * value.
     */
    def withContentDisposition(value: String): response.MessageType =
      response.withHeader(Header("Content-Disposition", value))

    /** Creates new response removing Content-Disposition header. */
    def removeContentDisposition: response.MessageType =
      response.removeHeaders("Content-Disposition")
  }

  /** Supports Content-Encoding header. */
  implicit class ContentEncoding[T <: HttpMessage](val message: T) {
    /**
     * Gets Content-Encoding header value.
     *
     * @throws HeaderNotFound if Content-Encoding is not present
     */
    def contentEncoding: Seq[ContentCoding] =
      getContentEncoding.getOrElse(throw new HeaderNotFound("Content-Encoding"))

    /** Gets Content-Encoding header value if present. */
    def getContentEncoding: Option[Seq[ContentCoding]] =
      message.getHeaderValue("Content-Encoding")
        .map(ListParser.apply)
        .map(_.map(ContentCoding.apply))

    /**
     * Creates new message setting Content-Encoding header to supplied values.
     */
    def withContentEncoding(values: ContentCoding*): message.MessageType =
      message.withHeader(Header("Content-Encoding", values.mkString(", ")))

    /** Creates new message removing Content-Encoding header. */
    def removeContentEncoding: message.MessageType =
      message.removeHeaders("Content-Encoding")
  }

  /** Supports Content-Language header. */
  implicit class ContentLanguage[T <: HttpMessage](val message: T) {
    /**
     * Gets Content-Language header value.
     *
     * @throws HeaderNotFound if Content-Language is not present
     */
    def contentLanguage: String =
      getContentLanguage.getOrElse(throw new HeaderNotFound("Content-Language"))

    /** Gets Content-Language header value if present. */
    def getContentLanguage: Option[String] =
      message.getHeaderValue("Content-Language")

    /** Creates new message setting Content-Language header to supplied value. */
    def withContentLanguage(value: String): message.MessageType =
      message.withHeader(Header("Content-Type", value))

    /** Creates new message removing Content-Language header. */
    def removeContentLanguage: message.MessageType =
      message.removeHeaders("Content-Language")
  }

  /** Supports Content-Length header. */
  implicit class ContentLength[T <: HttpMessage](val message: T) {
    /**
     * Gets Content-Length header value.
     *
     * @throws HeaderNotFound if Content-Length is not present
     */
    def contentLength: Long =
      getContentLength.getOrElse(throw new HeaderNotFound("Content-Length"))

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

  /** Supports Content-Range header. */
  implicit class ContentRange[T <: HttpMessage](val message: T) {
    /**
     * Gets Content-Range header value.
     *
     * @throws HeaderNotFound if Content-Range is not present
     */
    def contentRange: String =
      getContentRange.getOrElse(throw new HeaderNotFound("Content-Range"))

    /** Gets Content-Range header value if present. */
    def getContentRange: Option[String] =
      message.getHeaderValue("Content-Range")

    /** Creates new message setting Content-Range header to supplied value. */
    def withContentRange(value: String): message.MessageType =
      message.withHeader(Header("Content-Range", value))

    /** Creates new message removing Content-Range header. */
    def removeContentRange: message.MessageType =
      message.removeHeaders("Content-Range")
  }

  /** Supports Content-Type header. */
  implicit class ContentType[T <: HttpMessage](val message: T) {
    /**
     * Gets Content-Type header value.
     *
     * @throws HeaderNotFound if Content-Type is not present
     */
    def contentType: MediaType =
      getContentType.getOrElse(throw new HeaderNotFound("Content-Type"))

    /** Gets Content-Type header value if present. */
    def getContentType: Option[MediaType] =
      message.getHeaderValue("Content-Type").map(MediaType.apply)

    /** Creates new message setting Content-Type header to supplied value. */
    def withContentType(value: MediaType): message.MessageType =
      message.withHeader(Header("Content-Type", value.toString))

    /** Creates new message removing Content-Type header. */
    def removeContentType: message.MessageType =
      message.removeHeaders("Content-Type")
  }

  /** Supports Connection header. */
  implicit class Connection[T <: HttpMessage](val message: T) {
    /**
     * Gets Connection header value.
     *
     * @throws HeaderNotFound if Connection is not present
     */
    def connection: String =
      getConnection.getOrElse(throw new HeaderNotFound("Connection"))

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

  /** Supports Date header. */
  implicit class Date[T <: HttpResponse](val response: T) {
    /**
     * Gets Date header value.
     *
     * @throws HeaderNotFound if Date is not present
     */
    def date: OffsetDateTime =
      getDate.getOrElse(throw new HeaderNotFound("Date"))

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

  /** Supports ETag header. */
  implicit class ETag[T <: HttpResponse](val response: T) {
    /**
     * Gets ETag header value.
     *
     * @throws HeaderNotFound if ETag is not present
     */
    def etag: String =
      getETag.getOrElse(throw new HeaderNotFound("ETag"))

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

  /** Supports Expect header. */
  implicit class Expect[T <: HttpRequest](val request: T) {
    /**
     * Gets Expect header value.
     *
     * @throws HeaderNotFound if Expect is not present
     */
    def expect: String =
      getExpect.getOrElse(throw new HeaderNotFound("Expect"))

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

  /** Supports Expires header. */
  implicit class Expires[T <: HttpResponse](val response: T) {
    /**
     * Gets Expires header value.
     *
     * @throws HeaderNotFound if Expires is not present
     */
    def expires: OffsetDateTime =
      getExpires.getOrElse(throw new HeaderNotFound("Expires"))

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

  /** Supports Host header. */
  implicit class Host[T <: HttpRequest](val request: T) {
    /**
     * Gets Host header value.
     *
     * @throws HeaderNotFound if Host is not present
     */
    def host: String =
      getHost.getOrElse(throw new HeaderNotFound("Host"))

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

  /** Supports If-Match header. */
  implicit class IfMatch[T <: HttpRequest](val request: T) {
    /**
     * Gets If-Match header value.
     *
     * @throws HeaderNotFound if If-Match is not present
     */
    def ifMatch: String =
      getIfMatch.getOrElse(throw new HeaderNotFound("If-Match"))

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

  /** Supports If-Modified-Since header. */
  implicit class IfModifiedSince[T <: HttpRequest](val request: T) {
    /**
     * Gets If-Modified-Since header value.
     *
     * @throws HeaderNotFound if If-Modified-Since is not present
     */
    def ifModifiedSince: OffsetDateTime =
      getIfModifiedSince.getOrElse(throw new HeaderNotFound("If-Modified-Since"))

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

  /** Supports If-Range header. */
  implicit class IfRange[T <: HttpRequest](val request: T) {
    /**
     * Gets If-Range header value.
     *
     * @throws HeaderNotFound if If-Range is not present
     */
    def ifRange: String =
      getIfRange.getOrElse(throw new HeaderNotFound("If-Range"))

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

  /** Supports If-Unmodified-Since header. */
  implicit class IfUnmodifiedSince[T <: HttpRequest](val request: T) {
    /**
     * Gets If-Unmodified-Since header value.
     *
     * @throws HeaderNotFound if If-Unmodified-Since is not present
     */
    def ifUnmodifiedSince: OffsetDateTime =
      getIfUnmodifiedSince.getOrElse(throw new HeaderNotFound("If-Unmodified-Since"))

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

  /** Supports Last-Modified header. */
  implicit class LastModified[T <: HttpResponse](val response: T) {
    /**
     * Gets Last-Modified header value.
     *
     * @throws HeaderNotFound if Last-Modified is not present
     */
    def lastModified: OffsetDateTime =
      getLastModified.getOrElse(throw new HeaderNotFound("Last-Modified"))

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

  /** Supports Link header. */
  implicit class Link[T <: HttpResponse](val response: T) {
    /**
     * Gets Link header value.
     *
     * @throws HeaderNotFound if Link is not present
     */
    def link: Seq[String] =
      getLink.getOrElse(throw new HeaderNotFound("Link"))

    /** Gets Link header value if present. */
    def getLink: Option[Seq[String]] =
      response.getHeaderValue("Link").map(ListParser.apply)

    /** Creates new response setting Link header to supplied values. */
    def withLink(values: String*): response.MessageType =
      response.withHeader(Header("Link", values.mkString(", ")))

    /** Creates new response removing Link header. */
    def removeLink: response.MessageType =
      response.removeHeaders("Link")
  }

  /** Supports Location header. */
  implicit class Location[T <: HttpResponse](val response: T) {
    /**
     * Gets Location header value.
     *
     * @throws HeaderNotFound if Location is not present
     */
    def location: String =
      getLocation.getOrElse(throw new HeaderNotFound("Location"))

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

  /** Supports Max-Forwards header. */
  implicit class MaxForwards[T <: HttpRequest](val request: T) {
    /**
     * Gets Max-Forwards header value.
     *
     * @throws HeaderNotFound if Max-Forwards is not present
     */
    def maxForwards: Long =
      getMaxForwards.getOrElse(throw new HeaderNotFound("Max-Forwards"))

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

  /** Supports Pragma header. */
  implicit class Pragma[T <: HttpRequest](val request: T) {
    /**
     * Gets Pragma header value.
     *
     * @throws HeaderNotFound if Pragma is not present
     */
    def pragma: String =
      getPragma.getOrElse(throw new HeaderNotFound("Pragma"))

    /** Gets Pragma header value if present. */
    def getPragma: Option[String] =
      request.getHeaderValue("Pragma")

    /** Creates new request setting Pragma header to supplied value. */
    def withPragma(value: String): request.MessageType =
      request.withHeader(Header("Pragma", value))

    /** Creates new request removing Pragma header. */
    def removePragma: request.MessageType =
      request.removeHeaders("Pragma")
  }

  /** Supports Proxy-Authentication header. */
  implicit class ProxyAuthentication[T <: HttpResponse](val response: T) {
    /**
     * Gets Proxy-Authentication header value.
     *
     * @throws HeaderNotFound if Proxy-Authentication is not present
     */
    def proxyAuthentication: String =
      getProxyAuthentication.getOrElse(throw new HeaderNotFound("Proxy-Authentication"))

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

  /** Supports Proxy-Authentication-Info header. */
  implicit class ProxyAuthenticationInfo[T <: HttpResponse](val response: T) {
    /**
     * Gets Proxy-Authentication-Info header value.
     *
     * @throws HeaderNotFound if Proxy-Authentication-Info is not present
     */
    def proxyAuthenticationInfo: String =
      getProxyAuthenticationInfo.getOrElse(throw new HeaderNotFound("Proxy-Authentication-Info"))

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

  /** Supports Proxy-Authorization header. */
  implicit class ProxyAuthorization[T <: HttpRequest](val request: T) {
    /**
     * Gets Proxy-Authorization header value.
     *
     * @throws HeaderNotFound if Proxy-Authorization is not present
     */
    def proxyAuthorization: String =
      getProxyAuthorization.getOrElse(throw new HeaderNotFound("Proxy-Authorization"))

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

  /** Supports Range header. */
  implicit class Range[T <: HttpRequest](val request: T) {
    /**
     * Gets Range header value.
     *
     * @throws HeaderNotFound if Range is not present
     */
    def range: String =
      getRange.getOrElse(throw new HeaderNotFound("Range"))

    /** Gets Range header value if present. */
    def getRange: Option[String] =
      request.getHeaderValue("Range")

    /** Creates new request setting Range header to supplied value. */
    def withRange(value: String): request.MessageType =
      request.withHeader(Header("Range", value))

    /** Creates new request removing Range header. */
    def removeRange: request.MessageType =
      request.removeHeaders("Range")
  }

  /** Supports Referer header. */
  implicit class Referer[T <: HttpRequest](val request: T) {
    /**
     * Gets Referer header value.
     *
     * @throws HeaderNotFound if Referer is not present
     */
    def referer: String =
      getReferer.getOrElse(throw new HeaderNotFound("Referer"))

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

  /** Supports Retry-After header. */
  implicit class RetryAfter[T <: HttpResponse](val response: T) {
    /**
     * Gets Retry-After header value.
     *
     * @throws HeaderNotFound if Retry-After is not present
     */
    def retryAfter: OffsetDateTime =
      getRetryAfter.getOrElse(throw new HeaderNotFound("Retry-After"))

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

  /** Supports Server header. */
  implicit class Server[T <: HttpResponse](val response: T) {
    /**
     * Gets Server header value.
     *
     * @throws HeaderNotFound if Server is not present
     */
    def server: String =
      getServer.getOrElse(throw new HeaderNotFound("Server"))

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

  /** Supports TE header. */
  implicit class TE[T <: HttpRequest](val request: T) {
    /**
     * Gets TE header value.
     *
     * @throws HeaderNotFound if TE is not present
     */
    def te: Seq[TCoding] =
      getTE.getOrElse(throw new HeaderNotFound("TE"))

    /** Gets TE header value if present. */
    def getTE: Option[Seq[TCoding]] =
      request.getHeaderValue("TE")
        .map(ListParser.apply)
        .map(_.map(TCoding.apply))

    /** Creates new request setting TE header to supplied values. */
    def withTE(values: TCoding*): request.MessageType =
      request.withHeader(Header("TE", values.mkString(", ")))

    /** Creates new request removing TE header. */
    def removeTE: request.MessageType =
      request.removeHeaders("TE")
  }

  /** Supports Trailer header. */
  implicit class Trailer[T <: HttpMessage](val message: T) {
    /**
     * Gets Trailer header value.
     *
     * @throws HeaderNotFound if Trailer is not present
     */
    def vary: Seq[String] =
      getTrailer.getOrElse(throw new HeaderNotFound("Trailer"))

    /** Gets Trailer header value if present. */
    def getTrailer: Option[Seq[String]] =
      message.getHeaderValue("Trailer").map(ListParser.apply)

    /** Creates new message setting Trailer header to supplied values. */
    def withTrailer(values: String*): message.MessageType =
      message.withHeader(Header("Trailer", values.mkString(", ")))

    /** Creates new message removing Trailer header. */
    def removeTrailer: message.MessageType =
      message.removeHeaders("Trailer")
  }

  /** Supports Transfer-Encoding header. */
  implicit class TransferEncoding[T <: HttpMessage](val message: T) {
    /**
     * Gets Transfer-Encoding header value.
     *
     * @throws HeaderNotFound if Transfer-Encoding is not present
     */
    def transferEncoding: Seq[TransferCoding] =
      getTransferEncoding.getOrElse(throw new HeaderNotFound("Transfer-Encoding"))

    /** Gets Transfer-Encoding header value if present. */
    def getTransferEncoding: Option[Seq[TransferCoding]] =
      message.getHeaderValue("Transfer-Encoding")
        .map(ListParser.apply)
        .map(_.map(TransferCoding.apply))

    /**
     * Creates new message setting Transfer-Encoding header to supplied values.
     */
    def withTransferEncoding(values: TransferCoding*): message.MessageType =
      message.withHeader(Header("Transfer-Encoding", values.mkString(", ")))

    /** Creates new message removing Transfer-Encoding header. */
    def removeTransferEncoding: message.MessageType =
      message.removeHeaders("Transfer-Encoding")
  }

  /** Supports User-Agent header. */
  implicit class UserAgent[T <: HttpRequest](val request: T) {
    /**
     * Gets User-Agent header value.
     *
     * @throws HeaderNotFound if User-Agent is not present
     */
    def userAgent: String =
      getUserAgent.getOrElse(throw new HeaderNotFound("User-Agent"))

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

  /** Supports Vary header. */
  implicit class Vary[T <: HttpResponse](val response: T) {
    /**
     * Gets Vary header value.
     *
     * @throws HeaderNotFound if Vary is not present
     */
    def vary: Seq[String] =
      getVary.getOrElse(throw new HeaderNotFound("Vary"))

    /** Gets Vary header value if present. */
    def getVary: Option[Seq[String]] =
      response.getHeaderValue("Vary").map(ListParser.apply)

    /** Creates new response setting Vary header to supplied values. */
    def withVary(values: String*): response.MessageType =
      response.withHeader(Header("Vary", values.mkString(", ")))

    /** Creates new response removing Vary header. */
    def removeVary: response.MessageType =
      response.removeHeaders("Vary")
  }

  /** Supports Via header. */
  implicit class Via[T <: HttpResponse](val response: T) {
    /**
     * Gets Via header value.
     *
     * @throws HeaderNotFound if Via is not present
     */
    def via: Seq[String] =
      getVia.getOrElse(throw new HeaderNotFound("Via"))

    /** Gets Via header value if present. */
    def getVia: Option[Seq[String]] =
      response.getHeaderValue("Via").map(ListParser.apply)

    /** Creates new response setting Via header to supplied values. */
    def withVia(values: String*): response.MessageType =
      response.withHeader(Header("Via", values.mkString(", ")))

    /** Creates new response removing Via header. */
    def removeVia: response.MessageType =
      response.removeHeaders("Via")
  }

  /** Supports Warning header. */
  implicit class Warning[T <: HttpResponse](val response: T) {
    /**
     * Gets Warning header value.
     *
     * @throws HeaderNotFound if Warning is not present
     */
    def warning: String =
      getWarning.getOrElse(throw new HeaderNotFound("Warning"))

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

  /** Supports WWW-Authentication header. */
  implicit class WWWAuthentication[T <: HttpResponse](val response: T) {
    /**
     * Gets WWW-Authentication header value.
     *
     * @throws HeaderNotFound if WWW-Authentication is not present
     */
    def wwwAuthentication: String =
      getWWWAuthentication.getOrElse(throw new HeaderNotFound("WWW-Authentication"))

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

