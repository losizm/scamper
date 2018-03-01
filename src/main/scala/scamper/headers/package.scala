package scamper

import java.time.OffsetDateTime

package object headers {
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
      request.getHeaderValue("Authorization").map(_.toString)

    /** Creates new request setting Authorization header to supplied value. */
    def withAuthorization(value: String): request.MessageType =
      request.withHeader(Header("Authorization", value))
  }

  /** Supports Accept header. */
  implicit class Accept[T <: HttpRequest](val request: T) {
    /**
     * Gets Accept header value.
     *
     * @throws HeaderNotFound if Accept is not present
     */
    def accept: String =
      getAccept.getOrElse(throw new HeaderNotFound("Accept"))

    /** Gets Accept header value if present. */
    def getAccept: Option[String] =
      request.getHeaderValue("Accept")

    /** Creates new request setting Accept header to supplied value. */
    def withAccept(value: String): request.MessageType =
      request.withHeader(Header("Accept", value))
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
  }

  /** Supports Accept-Encoding header. */
  implicit class AcceptEncoding[T <: HttpRequest](val request: T) {
    /**
     * Gets Accept-Encoding header value.
     *
     * @throws HeaderNotFound if Accept-Encoding is not present
     */
    def acceptEncoding: String =
      getAcceptEncoding.getOrElse(throw new HeaderNotFound("Accept-Encoding"))

    /** Gets Accept-Encoding header value if present. */
    def getAcceptEncoding: Option[String] =
      request.getHeaderValue("Accept-Encoding")

    /** Creates new request setting Accept-Encoding header to supplied value. */
    def withAcceptEncoding(values: String): request.MessageType =
      request.withHeader(Header("Accept-Encoding", values))
  }

  /** Supports Accept-Language header. */
  implicit class AcceptLanguage[T <: HttpRequest](val request: T) {
    /**
     * Gets Accept-Language header value.
     *
     * @throws HeaderNotFound if Accept-Language is not present
     */
    def acceptLanguage: String =
      getAcceptLanguage.getOrElse(throw new HeaderNotFound("Accept-Language"))

    /** Gets Accept-Language header value if present. */
    def getAcceptLanguage: Option[String] =
      request.getHeaderValue("Accept-Language")

    /** Creates new request setting Accept-Language header to supplied value. */
    def withAcceptLanguage(value: String): request.MessageType =
      request.withHeader(Header("Accept-Language", value))
  }

  /** Supports Content-Encoding header. */
  implicit class ContentEncoding[T <: HttpMessage](val message: T) {
    /**
     * Gets Content-Encoding header value.
     *
     * @throws HeaderNotFound if Content-Encoding is not present
     */
    def contentEncoding: Seq[String] =
      getContentEncoding.getOrElse(throw new HeaderNotFound("Content-Encoding"))

    /** Gets Content-Encoding header value if present. */
    def getContentEncoding: Option[Seq[String]] =
      message.getHeaderValue("Content-Encoding").map(_.split(",").map(_.trim).toSeq)

    /**
     * Creates new message setting Content-Encoding header to supplied value.
     */
    def withContentEncoding(values: Seq[String]): message.MessageType =
      message.withHeader(Header("Content-Encoding", values.mkString(", ")))
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
      message.getHeaderValue("Content-Language").map(_.toString)

    /** Creates new message setting Content-Language header to supplied value. */
    def withContentLanguage(value: String): message.MessageType =
      message.withHeader(Header("Content-Type", value))
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
      message.getHeaderValue("Content-Range").map(_.toString)

    /** Creates new message setting Content-Range header to supplied value. */
    def withContentRange(value: String): message.MessageType =
      message.withHeader(Header("Content-Type", value))
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
  }

  /** Supports ETag header. */
  implicit class ETag[T <: HttpResponse](val response: T) {
    /**
     * Gets ETag header value.
     *
     * @throws HeaderNotFound if ETag is not present
     */
    def eTag: String =
      getETag.getOrElse(throw new HeaderNotFound("ETag"))

    /** Gets ETag header value if present. */
    def getETag: Option[String] =
      response.getHeaderValue("ETag").map(_.toString)

    /** Creates new response setting ETag header to supplied value. */
    def withETag(value: String): response.MessageType =
      response.withHeader(Header("ETag", value))
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
      request.getHeaderValue("Host").map(_.toString)

    /** Creates new request setting Host header to supplied value. */
    def withHost(value: String): request.MessageType =
      request.withHeader(Header("Host", value))
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
      request.getHeaderValue("If-Match").map(_.toString)

    /** Creates new request setting If-Match header to supplied value. */
    def withIfMatch(value: String): request.MessageType =
      request.withHeader(Header("If-Match", value))
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
      request.getHeaderValue("If-Range").map(_.toString)

    /** Creates new request setting If-Range header to supplied value. */
    def withIfRange(value: String): request.MessageType =
      request.withHeader(Header("If-Range", value))
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

    /** Creates new request setting If-Unmodified-Since header to supplied value. */
    def withIfUnmodifiedSince(value: OffsetDateTime): request.MessageType =
      request.withHeader(Header("If-Unmodified-Since", value))
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
      response.getHeaderValue("Location").map(_.toString)

    /** Creates new response setting Location header to supplied value. */
    def withLocation(value: String): response.MessageType =
      response.withHeader(Header("Location", value))
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
      response.getHeaderValue("Proxy-Authentication").map(_.toString)

    /** Creates new response setting Proxy-Authentication header to supplied value. */
    def withProxyAuthentication(value: String): response.MessageType =
      response.withHeader(Header("Proxy-Authentication", value))
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
      request.getHeaderValue("Proxy-Authorization").map(_.toString)

    /** Creates new request setting Proxy-Authorization header to supplied value. */
    def withProxyAuthorization(value: String): request.MessageType =
      request.withHeader(Header("Proxy-Authorization", value))
  }

  /** Supports Range header. */
  implicit class Range[T <: HttpRequest](val message: T) {
    /**
     * Gets Range header value.
     *
     * @throws HeaderNotFound if Range is not present
     */
    def range: String =
      getRange.getOrElse(throw new HeaderNotFound("Range"))

    /** Gets Range header value if present. */
    def getRange: Option[String] =
      message.getHeaderValue("Range").map(_.toString)

    /** Creates new request setting Range header to supplied value. */
    def withRange(value: String): message.MessageType =
      message.withHeader(Header("Range", value))
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
      request.getHeaderValue("Referer").map(_.toString)

    /** Creates new request setting Referer header to supplied value. */
    def withReferer(value: String): request.MessageType =
      request.withHeader(Header("Referer", value))
  }

  /** Supports TE header. */
  implicit class TE[T <: HttpRequest](val request: T) {
    /**
     * Gets TE header value.
     *
     * @throws HeaderNotFound if TE is not present
     */
    def te: String =
      getTE.getOrElse(throw new HeaderNotFound("TE"))

    /** Gets TE header value if present. */
    def getTE: Option[String] =
      request.getHeaderValue("TE").map(_.toString)

    /** Creates new request setting TE header to supplied value. */
    def withTE(value: String): request.MessageType =
      request.withHeader(Header("TE", value))
  }

  /** Supports Trailer header. */
  implicit class Trailer[T <: HttpMessage](val message: T) {
    /**
     * Gets Trailer header value.
     *
     * @throws HeaderNotFound if Trailer is not present
     */
    def trailer: String =
      getTrailer.getOrElse(throw new HeaderNotFound("Trailer"))

    /** Gets Trailer header value if present. */
    def getTrailer: Option[String] =
      message.getHeaderValue("Trailer").map(_.toString)

    /** Creates new message setting Trailer header to supplied value. */
    def withTrailer(value: String): message.MessageType =
      message.withHeader(Header("Trailer", value))
  }

  /** Supports Transfer-Encoding header. */
  implicit class TransferEncoding[T <: HttpRequest](val message: T) {
    /**
     * Gets Transfer-Encoding header value.
     *
     * @throws HeaderNotFound if Transfer-Encoding is not present
     */
    def transferEncoding: String =
      getTransferEncoding.getOrElse(throw new HeaderNotFound("Transfer-Encoding"))

    /** Gets Transfer-Encoding header value if present. */
    def getTransferEncoding: Option[String] =
      message.getHeaderValue("Transfer-Encoding").map(_.toString)

    /** Creates new request setting Transfer-Encoding header to supplied value. */
    def withTransferEncoding(value: String): message.MessageType =
      message.withHeader(Header("Transfer-Encoding", value))
  }

  /** Supports User-Agent header. */
  implicit class UserAgent[T <: HttpRequest](val message: T) {
    /**
     * Gets User-Agent header value.
     *
     * @throws HeaderNotFound if User-Agent is not present
     */
    def userAgent: String =
      getUserAgent.getOrElse(throw new HeaderNotFound("User-Agent"))

    /** Gets User-Agent header value if present. */
    def getUserAgent: Option[String] =
      message.getHeaderValue("User-Agent").map(_.toString)

    /** Creates new request setting User-Agent header to supplied value. */
    def withUserAgent(value: String): message.MessageType =
      message.withHeader(Header("User-Agent", value))
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
      response.getHeaderValue("WWW-Authentication").map(_.toString)

    /** Creates new response setting WWW-Authentication header to supplied value. */
    def withWWWAuthentication(value: String): response.MessageType =
      response.withHeader(Header("WWW-Authentication", value))
  }
}

