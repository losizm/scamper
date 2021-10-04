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
package http
package headers

import java.time.Instant.parse as Instant

import scala.language.implicitConversions

import scamper.http.types.{ *, given }
import scamper.http.types.ByteContentRange.Satisfied
import scamper.http.types.ByteRange.Slice

import RequestMethod.Registry.{ Get, Post }
import ResponseStatus.Registry.Ok

class HeadersSpec extends org.scalatest.flatspec.AnyFlatSpec:
  it should "create request with Accept header" in {
    val req1 = Get("/")
    assert(!req1.hasAccept)
    assert(req1.accept.isEmpty)
    assert(req1.getAccept.isEmpty)
    assert(req1.removeAccept == req1)

    val req2 = req1.setAccept("text/html; q=0.9", "text/plain; q=0.1")
    assert(req2.hasAccept)
    assert(req2.accept == Seq[MediaRange]("text/html; q=0.9", "text/plain; q=0.1"))
    assert(req2.getAccept.contains(Seq[MediaRange]("text/html; q=0.9", "text/plain; q=0.1")))
    assert(req2.removeAccept == req1)
    assert(req2.getHeaderValue("Accept").contains("text/html; q=0.9, text/plain; q=0.1"))
  }

  it should "create request with Accept-Charset header" in {
    val req1 = Get("/")
    assert(!req1.hasAcceptCharset)
    assert(req1.acceptCharset.isEmpty)
    assert(req1.getAcceptCharset.isEmpty)
    assert(req1.removeAcceptCharset == req1)

    val req2 = req1.setAcceptCharset("utf-8; q=0.9", "ascii; q=0.1")
    assert(req2.hasAcceptCharset)
    assert(req2.acceptCharset == Seq[CharsetRange]("utf-8; q=0.9", "ascii; q=0.1"))
    assert(req2.getAcceptCharset.contains(Seq[CharsetRange]("utf-8; q=0.9", "ascii; q=0.1")))
    assert(req2.removeAcceptCharset == req1)
    assert(req2.getHeaderValue("Accept-Charset").contains("utf-8; q=0.9, ascii; q=0.1"))
  }

  it should "create request with Accept-Encoding header" in {
    val req1 = Get("/")
    assert(!req1.hasAcceptEncoding)
    assert(req1.acceptEncoding.isEmpty)
    assert(req1.getAcceptEncoding.isEmpty)
    assert(req1.removeAcceptEncoding == req1)

    val req2 = req1.setAcceptEncoding("gzip; q=0.9", "deflate; q=0.1")
    assert(req2.hasAcceptEncoding)
    assert(req2.acceptEncoding == Seq[ContentCodingRange]("gzip; q=0.9", "deflate; q=0.1"))
    assert(req2.getAcceptEncoding.contains(Seq[ContentCodingRange]("gzip; q=0.9", "deflate; q=0.1")))
    assert(req2.removeAcceptEncoding == req1)
    assert(req2.getHeaderValue("Accept-Encoding").contains("gzip; q=0.9, deflate; q=0.1"))
  }

  it should "create request with Accept-Language header" in {
    val req1 = Get("/")
    assert(!req1.hasAcceptLanguage)
    assert(req1.acceptLanguage.isEmpty)
    assert(req1.getAcceptLanguage.isEmpty)
    assert(req1.removeAcceptLanguage == req1)

    val req2 = req1.setAcceptLanguage("en-US; q=0.9", "en-GB; q=0.1")
    assert(req2.hasAcceptLanguage)
    assert(req2.acceptLanguage == Seq[LanguageRange]("en-US; q=0.9", "en-GB; q=0.1"))
    assert(req2.getAcceptLanguage.contains(Seq[LanguageRange]("en-US; q=0.9", "en-GB; q=0.1")))
    assert(req2.removeAcceptLanguage == req1)
    assert(req2.getHeaderValue("Accept-Language").contains("en-US; q=0.9, en-GB; q=0.1"))
  }

  it should "create response with Accept-Patch header" in {
    val res1 = Ok()
    assert(!res1.hasAcceptPatch)
    assert(res1.acceptPatch.isEmpty)
    assert(res1.getAcceptPatch.isEmpty)
    assert(res1.removeAcceptPatch == res1)

    val res2 = res1.setAcceptPatch("text/plain", "application/octet-stream")
    assert(res2.hasAcceptPatch)
    assert(res2.acceptPatch == Seq[MediaType]("text/plain", "application/octet-stream"))
    assert(res2.getAcceptPatch.contains(Seq[MediaType]("text/plain", "application/octet-stream")))
    assert(res2.removeAcceptPatch == res1)
    assert(res2.getHeaderValue("Accept-Patch").contains("text/plain, application/octet-stream"))
  }

  it should "create response with Accept-Ranges header" in {
    val res1 = Ok()
    assert(!res1.hasAcceptRanges)
    assert(res1.acceptRanges.isEmpty)
    assert(res1.getAcceptRanges.isEmpty)
    assert(res1.removeAcceptRanges == res1)

    val res2 = res1.setAcceptRanges("text/plain", "application/octet-stream")
    assert(res2.hasAcceptRanges)
    assert(res2.acceptRanges == Seq("text/plain", "application/octet-stream"))
    assert(res2.getAcceptRanges.contains(Seq("text/plain", "application/octet-stream")))
    assert(res2.removeAcceptRanges == res1)
    assert(res2.getHeaderValue("Accept-Ranges").contains("text/plain, application/octet-stream"))
  }

  it should "create response with Age header" in {
    val res1 = Ok()
    assert(!res1.hasAge)
    assertThrows[HeaderNotFound](res1.age)
    assert(res1.getAge.isEmpty)
    assert(res1.removeAge == res1)

    val res2 = res1.setAge(600)
    assert(res2.hasAge)
    assert(res2.age == 600)
    assert(res2.getAge.contains(600))
    assert(res2.removeAge == res1)
    assert(res2.getHeaderValue("Age").contains("600"))
  }

  it should "create response with Allow header" in {
    val res1 = Ok()
    assert(!res1.hasAllow)
    assert(res1.allow.isEmpty)
    assert(res1.getAllow.isEmpty)
    assert(res1.removeAllow == res1)

    val res2 = res1.setAllow(Get, Post)
    assert(res2.hasAllow)
    assert(res2.allow == Seq(Get, Post))
    assert(res2.getAllow.contains(Seq(Get, Post)))
    assert(res2.removeAllow == res1)
    assert(res2.getHeaderValue("Allow").contains("GET, POST"))
  }

  it should "create message with Cache-Control header" in {
    val req1 = Get("/")
    assert(!req1.hasCacheControl)
    assert(req1.cacheControl.isEmpty)
    assert(req1.getCacheControl.isEmpty)
    assert(req1.removeCacheControl == req1)

    val req2 = req1.setCacheControl("max-age=600", "no-cache")
    assert(req2.hasCacheControl)
    assert(req2.cacheControl == Seq[CacheDirective]("max-age=600", "no-cache"))
    assert(req2.getCacheControl.contains(Seq[CacheDirective]("max-age=600", "no-cache")))
    assert(req2.removeCacheControl == req1)
    assert(req2.getHeaderValue("Cache-Control").contains("max-age=600, no-cache"))

    val res1 = Ok()
    assert(!res1.hasCacheControl)
    assert(res1.cacheControl.isEmpty)
    assert(res1.getCacheControl.isEmpty)
    assert(res1.removeCacheControl == res1)

    val res2 = res1.setCacheControl("no-store", "no-transform")
    assert(res2.hasCacheControl)
    assert(res2.cacheControl == Seq[CacheDirective]("no-store", "no-transform"))
    assert(res2.getCacheControl.contains(Seq[CacheDirective]("no-store", "no-transform")))
    assert(res2.removeCacheControl == res1)
    assert(res2.getHeaderValue("Cache-Control").contains("no-store, no-transform"))
  }

  it should "create message with Connection header" in {
    val req1 = Get("/")
    assert(!req1.hasConnection)
    assert(req1.connection.isEmpty)
    assert(req1.getConnection.isEmpty)
    assert(req1.removeConnection == req1)

    val req2 = req1.setConnection("TE", "close")
    assert(req2.hasConnection)
    assert(req2.connection == Seq("TE", "close"))
    assert(req2.getConnection.contains(Seq("TE", "close")))
    assert(req2.removeConnection == req1)
    assert(req2.getHeaderValue("Connection").contains("TE, close"))

    val res1 = Ok()
    assert(!res1.hasConnection)
    assert(res1.connection.isEmpty)
    assert(res1.getConnection.isEmpty)
    assert(res1.removeConnection == res1)

    val res2 = res1.setConnection("TE", "close")
    assert(res2.hasConnection)
    assert(res2.connection == Seq("TE", "close"))
    assert(res2.getConnection.contains(Seq("TE", "close")))
    assert(res2.removeConnection == res1)
    assert(res2.getHeaderValue("Connection").contains("TE, close"))
  }

  it should "create response with Content-Disposition header" in {
    val res1 = Ok()
    assert(!res1.hasContentDisposition)
    assertThrows[HeaderNotFound](res1.contentDisposition)
    assert(res1.getContentDisposition.isEmpty)
    assert(res1.removeContentDisposition == res1)

    val res2 = res1.setContentDisposition("attachment; filename=file.txt")
    assert(res2.hasContentDisposition)
    assert(res2.contentDisposition == DispositionType("attachment", "filename" -> "file.txt"))
    assert(res2.getContentDisposition.contains(DispositionType("attachment", "filename" -> "file.txt")))
    assert(res2.removeContentDisposition == res1)
    assert(res2.getHeaderValue("Content-Disposition").contains("attachment; filename=file.txt"))
  }

  it should "create message with Content-Encoding header" in {
    val req1 = Get("/")
    assert(!req1.hasContentEncoding)
    assert(req1.contentEncoding.isEmpty)
    assert(req1.getContentEncoding.isEmpty)
    assert(req1.removeContentEncoding == req1)

    val req2 = req1.setContentEncoding("deflate", "gzip")
    assert(req2.hasContentEncoding)
    assert(req2.contentEncoding == Seq[ContentCoding]("deflate", "gzip"))
    assert(req2.getContentEncoding.contains(Seq[ContentCoding]("deflate", "gzip")))
    assert(req2.removeContentEncoding == req1)
    assert(req2.getHeaderValue("Content-Encoding").contains("deflate, gzip"))

    val res1 = Ok()
    assert(!res1.hasContentEncoding)
    assert(res1.contentEncoding.isEmpty)
    assert(res1.getContentEncoding.isEmpty)
    assert(res1.removeContentEncoding == res1)

    val res2 = res1.setContentEncoding("deflate", "gzip")
    assert(res2.hasContentEncoding)
    assert(res2.contentEncoding == Seq[ContentCoding]("deflate", "gzip"))
    assert(res2.getContentEncoding.contains(Seq[ContentCoding]("deflate", "gzip")))
    assert(res2.removeContentEncoding == res1)
    assert(res2.getHeaderValue("Content-Encoding").contains("deflate, gzip"))
  }

  it should "create message with Content-Language header" in {
    val req1 = Get("/")
    assert(!req1.hasContentLanguage)
    assert(req1.contentLanguage.isEmpty)
    assert(req1.getContentLanguage.isEmpty)
    assert(req1.removeContentLanguage == req1)

    val req2 = req1.setContentLanguage("en-US", "en-GB")
    assert(req2.hasContentLanguage)
    assert(req2.contentLanguage == Seq[LanguageTag]("en-US", "en-GB"))
    assert(req2.getContentLanguage.contains(Seq[LanguageTag]("en-US", "en-GB")))
    assert(req2.removeContentLanguage == req1)
    assert(req2.getHeaderValue("Content-Language").contains("en-US, en-GB"))

    val res1 = Ok()
    assert(!res1.hasContentLanguage)
    assert(res1.contentLanguage.isEmpty)
    assert(res1.getContentLanguage.isEmpty)
    assert(res1.removeContentLanguage == res1)

    val res2 = res1.setContentLanguage("en-US", "en-GB")
    assert(res2.hasContentLanguage)
    assert(res2.contentLanguage == Seq[LanguageTag]("en-US", "en-GB"))
    assert(res2.getContentLanguage.contains(Seq[LanguageTag]("en-US", "en-GB")))
    assert(res2.removeContentLanguage == res1)
    assert(res2.getHeaderValue("Content-Language").contains("en-US, en-GB"))
  }

  it should "create message with Content-Length header" in {
    val req1 = Get("/")
    assert(!req1.hasContentLength)
    assertThrows[HeaderNotFound](req1.contentLength)
    assert(req1.getContentLength.isEmpty)
    assert(req1.removeContentLength == req1)

    val req2 = req1.setContentLength(1024)
    assert(req2.hasContentLength)
    assert(req2.contentLength == 1024)
    assert(req2.getContentLength.contains(1024))
    assert(req2.removeContentLength == req1)
    assert(req2.getHeaderValue("Content-Length").contains("1024"))

    val res1 = Ok()
    assert(!res1.hasContentLength)
    assertThrows[HeaderNotFound](res1.contentLength)
    assert(res1.getContentLength.isEmpty)
    assert(res1.removeContentLength == res1)

    val res2 = res1.setContentLength(1024)
    assert(res2.hasContentLength)
    assert(res2.contentLength == 1024)
    assert(res2.getContentLength.contains(1024))
    assert(res2.removeContentLength == res1)
    assert(res2.getHeaderValue("Content-Length").contains("1024"))
  }

  it should "create message with Content-Location header" in {
    val req1 = Get("/")
    assert(!req1.hasContentLocation)
    assertThrows[HeaderNotFound](req1.contentLocation)
    assert(req1.getContentLocation.isEmpty)
    assert(req1.removeContentLocation == req1)

    val req2 = req1.setContentLocation("/path/to/file.txt")
    assert(req2.hasContentLocation)
    assert(req2.contentLocation == Uri("/path/to/file.txt"))
    assert(req2.getContentLocation.contains(Uri("/path/to/file.txt")))
    assert(req2.removeContentLocation == req1)
    assert(req2.getHeaderValue("Content-Location").contains("/path/to/file.txt"))

    val res1 = Ok()
    assert(!res1.hasContentLocation)
    assertThrows[HeaderNotFound](res1.contentLocation)
    assert(res1.getContentLocation.isEmpty)
    assert(res1.removeContentLocation == res1)

    val res2 = res1.setContentLocation("/path/to/file.txt")
    assert(res2.hasContentLocation)
    assert(res2.contentLocation == Uri("/path/to/file.txt"))
    assert(res2.getContentLocation.contains(Uri("/path/to/file.txt")))
    assert(res2.removeContentLocation == res1)
    assert(res2.getHeaderValue("Content-Location").contains("/path/to/file.txt"))
  }

  it should "create message with Content-Range header" in {
    val req1 = Get("/")
    assert(!req1.hasContentRange)
    assertThrows[HeaderNotFound](req1.contentRange)
    assert(req1.getContentRange.isEmpty)
    assert(req1.removeContentRange == req1)

    val req2 = req1.setContentRange("bytes 1024-8191/*")
    assert(req2.hasContentRange)
    assert(req2.contentRange == ByteContentRange(Satisfied(1024, 8191, None)))
    assert(req2.getContentRange.contains(ByteContentRange(Satisfied(1024, 8191, None))))
    assert(req2.removeContentRange == req1)
    assert(req2.getHeaderValue("Content-Range").contains("bytes 1024-8191/*"))

    val res1 = Ok()
    assert(!res1.hasContentRange)
    assertThrows[HeaderNotFound](res1.contentRange)
    assert(res1.getContentRange.isEmpty)
    assert(res1.removeContentRange == res1)

    val res2 = res1.setContentRange("bytes 1024-8191/*")
    assert(res2.hasContentRange)
    assert(res2.contentRange == ByteContentRange(Satisfied(1024, 8191, None)))
    assert(res2.getContentRange.contains(ByteContentRange(Satisfied(1024, 8191, None))))
    assert(res2.removeContentRange == res1)
    assert(res2.getHeaderValue("Content-Range").contains("bytes 1024-8191/*"))
  }

  it should "create message with Content-Type header" in {
    val req1 = Get("/")
    assert(!req1.hasContentType)
    assertThrows[HeaderNotFound](req1.contentType)
    assert(req1.getContentType.isEmpty)
    assert(req1.removeContentType == req1)

    val req2 = req1.setContentType("text/plain")
    assert(req2.hasContentType)
    assert(req2.contentType == MediaType("text/plain"))
    assert(req2.getContentType.contains(MediaType("text/plain")))
    assert(req2.removeContentType == req1)
    assert(req2.getHeaderValue("Content-Type").contains("text/plain"))

    val res1 = Ok()
    assert(!res1.hasContentType)
    assertThrows[HeaderNotFound](res1.contentType)
    assert(res1.getContentType.isEmpty)
    assert(res1.removeContentType == res1)

    val res2 = res1.setContentType("text/plain")
    assert(res2.hasContentType)
    assert(res2.contentType == MediaType("text/plain"))
    assert(res2.getContentType.contains(MediaType("text/plain")))
    assert(res2.removeContentType == res1)
    assert(res2.getHeaderValue("Content-Type").contains("text/plain"))
  }

  it should "create response with Date header" in {
    val res1 = Ok()
    assert(!res1.hasDate)
    assertThrows[HeaderNotFound](res1.date)
    assert(res1.getDate.isEmpty)
    assert(res1.removeDate == res1)

    val res2 = res1.setDate(Instant("2020-12-25T07:34:16Z"))
    assert(res2.hasDate)
    assert(res2.date == Instant("2020-12-25T07:34:16Z"))
    assert(res2.getDate.contains(Instant("2020-12-25T07:34:16Z")))
    assert(res2.removeDate == res1)
    assert(res2.getHeaderValue("Date").contains("Fri, 25 Dec 2020 07:34:16 GMT"))
  }

  it should "create response with ETag header" in {
    val res1 = Ok()
    assert(!res1.hasETag)
    assertThrows[HeaderNotFound](res1.eTag)
    assert(res1.getETag.isEmpty)
    assert(res1.removeETag == res1)

    val res2 = res1.setETag("W/\"abc\"")
    assert(res2.hasETag)
    assert(res2.eTag == EntityTag("abc", true))
    assert(res2.getETag.contains(EntityTag("abc", true)))
    assert(res2.removeETag == res1)
    assert(res2.getHeaderValue("ETag").contains("W/\"abc\""))
  }

  it should "create request with Early-Data header" in {
    val req1 = Get("/")
    assert(!req1.hasEarlyData)
    assertThrows[HeaderNotFound](req1.earlyData)
    assert(req1.getEarlyData.isEmpty)
    assert(req1.removeEarlyData == req1)

    val req2 = req1.setEarlyData(1)
    assert(req2.hasEarlyData)
    assert(req2.earlyData == 1)
    assert(req2.getEarlyData.contains(1))
    assert(req2.removeEarlyData == req1)
    assert(req2.getHeaderValue("Early-Data").contains("1"))
  }

  it should "create request with Expect header" in {
    val req1 = Get("/")
    assert(!req1.hasExpect)
    assertThrows[HeaderNotFound](req1.expect)
    assert(req1.getExpect.isEmpty)
    assert(req1.removeExpect == req1)

    val req2 = req1.setExpect("100-Continue")
    assert(req2.hasExpect)
    assert(req2.expect == "100-Continue")
    assert(req2.getExpect.contains("100-Continue"))
    assert(req2.removeExpect == req1)
    assert(req2.getHeaderValue("Expect").contains("100-Continue"))
  }

  it should "create response with Expires header" in {
    val res1 = Ok()
    assert(!res1.hasExpires)
    assertThrows[HeaderNotFound](res1.expires)
    assert(res1.getExpires.isEmpty)
    assert(res1.removeExpires == res1)

    val res2 = res1.setExpires(Instant("2020-12-25T07:34:16Z"))
    assert(res2.hasExpires)
    assert(res2.expires == Instant("2020-12-25T07:34:16Z"))
    assert(res2.getExpires.contains(Instant("2020-12-25T07:34:16Z")))
    assert(res2.removeExpires == res1)
    assert(res2.getHeaderValue("Expires").contains("Fri, 25 Dec 2020 07:34:16 GMT"))
  }

  it should "create request with From header" in {
    val req1 = Get("/")
    assert(!req1.hasFrom)
    assertThrows[HeaderNotFound](req1.from)
    assert(req1.getFrom.isEmpty)
    assert(req1.removeFrom == req1)

    val req2 = req1.setFrom("lupita@xyz.com")
    assert(req2.hasFrom)
    assert(req2.from == "lupita@xyz.com")
    assert(req2.getFrom.contains("lupita@xyz.com"))
    assert(req2.removeFrom == req1)
    assert(req2.getHeaderValue("From").contains("lupita@xyz.com"))
  }

  it should "create request with Host header" in {
    val req1 = Get("/")
    assert(!req1.hasHost)
    assertThrows[HeaderNotFound](req1.host)
    assert(req1.getHost.isEmpty)
    assert(req1.removeHost == req1)

    val req2 = req1.setHost("localhost:8080")
    assert(req2.hasHost)
    assert(req2.host == "localhost:8080")
    assert(req2.getHost.contains("localhost:8080"))
    assert(req2.removeHost == req1)
    assert(req2.getHeaderValue("Host").contains("localhost:8080"))
  }

  it should "create request with If-Match header" in {
    val req1 = Get("/")
    assert(!req1.hasIfMatch)
    assert(req1.ifMatch.isEmpty)
    assert(req1.getIfMatch.isEmpty)
    assert(req1.removeIfMatch == req1)

    val req2 = req1.setIfMatch("W/\"abc\"", "W/\"xyz\"")
    assert(req2.hasIfMatch)
    assert(req2.ifMatch == Seq(EntityTag("abc", true), EntityTag("xyz", true)))
    assert(req2.getIfMatch.contains(Seq(EntityTag("abc", true), EntityTag("xyz", true))))
    assert(req2.removeIfMatch == req1)
    assert(req2.getHeaderValue("If-Match").contains("W/\"abc\", W/\"xyz\""))
  }

  it should "create request with If-Modified-Since header" in {
    val req1 = Get("/")
    assert(!req1.hasIfModifiedSince)
    assertThrows[HeaderNotFound](req1.ifModifiedSince)
    assert(req1.getIfModifiedSince.isEmpty)
    assert(req1.removeIfModifiedSince == req1)

    val req2 = req1.setIfModifiedSince(Instant("2020-12-25T07:34:16Z"))
    assert(req2.hasIfModifiedSince)
    assert(req2.ifModifiedSince == Instant("2020-12-25T07:34:16Z"))
    assert(req2.getIfModifiedSince.contains(Instant("2020-12-25T07:34:16Z")))
    assert(req2.removeIfModifiedSince == req1)
    assert(req2.getHeaderValue("If-Modified-Since").contains("Fri, 25 Dec 2020 07:34:16 GMT"))
  }

  it should "create request with If-None-Match header" in {
    val req1 = Get("/")
    assert(!req1.hasIfNoneMatch)
    assert(req1.ifNoneMatch.isEmpty)
    assert(req1.getIfNoneMatch.isEmpty)
    assert(req1.removeIfNoneMatch == req1)

    val req2 = req1.setIfNoneMatch("W/\"abc\"", "W/\"xyz\"")
    assert(req2.hasIfNoneMatch)
    assert(req2.ifNoneMatch == Seq(EntityTag("abc", true), EntityTag("xyz", true)))
    assert(req2.getIfNoneMatch.contains(Seq(EntityTag("abc", true), EntityTag("xyz", true))))
    assert(req2.removeIfNoneMatch == req1)
    assert(req2.getHeaderValue("If-None-Match").contains("W/\"abc\", W/\"xyz\""))
  }

  it should "create request with If-Range header" in {
    val req1 = Get("/")
    assert(!req1.hasIfRange)
    assertThrows[HeaderNotFound](req1.ifRange)
    assert(req1.getIfRange.isEmpty)
    assert(req1.removeIfRange == req1)

    val req2 = req1.setIfRange("W/\"abc\"")
    assert(req2.hasIfRange)
    assert(req2.ifRange == Left(EntityTag("abc", true)))
    assert(req2.getIfRange.contains(Left(EntityTag("abc", true))))
    assert(req2.removeIfRange == req1)
    assert(req2.getHeaderValue("If-Range").contains("W/\"abc\""))

    val req3 = req1.setIfRange(Instant("2020-12-25T07:34:16Z"))
    assert(req3.hasIfRange)
    assert(req3.ifRange == Right(Instant("2020-12-25T07:34:16Z")))
    assert(req3.getIfRange.contains(Right(Instant("2020-12-25T07:34:16Z"))))
    assert(req3.removeIfRange == req1)
    assert(req3.getHeaderValue("If-Range").contains("Fri, 25 Dec 2020 07:34:16 GMT"))
  }

  it should "create request with If-Unmodified-Since header" in {
    val req1 = Get("/")
    assert(!req1.hasIfUnmodifiedSince)
    assertThrows[HeaderNotFound](req1.ifUnmodifiedSince)
    assert(req1.getIfUnmodifiedSince.isEmpty)
    assert(req1.removeIfUnmodifiedSince == req1)

    val req2 = req1.setIfUnmodifiedSince(Instant("2020-12-25T07:34:16Z"))
    assert(req2.hasIfUnmodifiedSince)
    assert(req2.ifUnmodifiedSince == Instant("2020-12-25T07:34:16Z"))
    assert(req2.getIfUnmodifiedSince.contains(Instant("2020-12-25T07:34:16Z")))
    assert(req2.removeIfUnmodifiedSince == req1)
    assert(req2.getHeaderValue("If-Unmodified-Since").contains("Fri, 25 Dec 2020 07:34:16 GMT"))
  }

  it should "create message with Keep-Alive header" in {
    val req1 = Get("/")
    assert(!req1.hasKeepAlive)
    assertThrows[HeaderNotFound](req1.keepAlive)
    assert(req1.getKeepAlive.isEmpty)
    assert(req1.removeKeepAlive == req1)

    val req2 = req1.setKeepAlive("timeout=5, max=10")
    assert(req2.hasKeepAlive)
    assert(req2.keepAlive == KeepAliveParameters(5, 10))
    assert(req2.getKeepAlive.contains(KeepAliveParameters(5, 10)))
    assert(req2.removeKeepAlive == req1)
    assert(req2.getHeaderValue("Keep-Alive").contains("timeout=5, max=10"))

    val res1 = Ok()
    assert(!res1.hasKeepAlive)
    assertThrows[HeaderNotFound](res1.keepAlive)
    assert(res1.getKeepAlive.isEmpty)
    assert(res1.removeKeepAlive == res1)

    val res2 = res1.setKeepAlive("timeout=5, max=10")
    assert(res2.hasKeepAlive)
    assert(res2.keepAlive == KeepAliveParameters(5, 10))
    assert(res2.getKeepAlive.contains(KeepAliveParameters(5, 10)))
    assert(res2.removeKeepAlive == res1)
    assert(res2.getHeaderValue("Keep-Alive").contains("timeout=5, max=10"))
  }

  it should "create response with Last-Modified header" in {
    val res1 = Ok()
    assert(!res1.hasLastModified)
    assertThrows[HeaderNotFound](res1.lastModified)
    assert(res1.getLastModified.isEmpty)
    assert(res1.removeLastModified == res1)

    val res2 = res1.setLastModified(Instant("2020-12-25T07:34:16Z"))
    assert(res2.hasLastModified)
    assert(res2.lastModified == Instant("2020-12-25T07:34:16Z"))
    assert(res2.getLastModified.contains(Instant("2020-12-25T07:34:16Z")))
    assert(res2.removeLastModified == res1)
    assert(res2.getHeaderValue("Last-Modified").contains("Fri, 25 Dec 2020 07:34:16 GMT"))
  }

  it should "create response with Link header" in {
    val res1 = Ok()
    assert(!res1.hasLink)
    assert(res1.link.isEmpty)
    assert(res1.getLink.isEmpty)
    assert(res1.removeLink == res1)

    val res2 = res1.setLink("</Chapter1>; rel=previous", "</Chapter3>; rel=next")
    assert(res2.hasLink)
    assert(res2.link == Seq[LinkType](LinkType("/Chapter1", "rel" -> Some("previous")), LinkType("/Chapter3", "rel" -> Some("next"))))
    assert(res2.getLink.contains(Seq[LinkType](LinkType("/Chapter1", "rel" -> Some("previous")), LinkType("/Chapter3", "rel" -> Some("next")))))
    assert(res2.removeLink == res1)
    assert(res2.getHeaderValue("Link").contains("</Chapter1>; rel=previous, </Chapter3>; rel=next"))
  }

  it should "create response with Location header" in {
    val res1 = Ok()
    assert(!res1.hasLocation)
    assertThrows[HeaderNotFound](res1.location)
    assert(res1.getLocation.isEmpty)
    assert(res1.removeLocation == res1)

    val res2 = res1.setLocation("/path/to/file.txt")
    assert(res2.hasLocation)
    assert(res2.location == Uri("/path/to/file.txt"))
    assert(res2.getLocation.contains(Uri("/path/to/file.txt")))
    assert(res2.removeLocation == res1)
    assert(res2.getHeaderValue("Location").contains("/path/to/file.txt"))
  }

  it should "create request with Max-Forwards header" in {
    val req1 = Get("/")
    assert(!req1.hasMaxForwards)
    assertThrows[HeaderNotFound](req1.maxForwards)
    assert(req1.getMaxForwards.isEmpty)
    assert(req1.removeMaxForwards == req1)

    val req2 = req1.setMaxForwards(5)
    assert(req2.hasMaxForwards)
    assert(req2.maxForwards == 5)
    assert(req2.getMaxForwards.contains(5))
    assert(req2.removeMaxForwards == req1)
    assert(req2.getHeaderValue("Max-Forwards").contains("5"))
  }

  it should "create request with Pragma header" in {
    val req1 = Get("/")
    assert(!req1.hasPragma)
    assert(req1.pragma.isEmpty)
    assert(req1.getPragma.isEmpty)
    assert(req1.removePragma == req1)

    val req2 = req1.setPragma("no-cache")
    assert(req2.hasPragma)
    assert(req2.pragma == Seq[PragmaDirective]("no-cache"))
    assert(req2.getPragma.contains(Seq[PragmaDirective]("no-cache")))
    assert(req2.removePragma == req1)
    assert(req2.getHeaderValue("Pragma").contains("no-cache"))
  }

  it should "create request with Prefer header" in {
    val req1 = Get("/")
    assert(!req1.hasPrefer)
    assert(req1.prefer.isEmpty)
    assert(req1.getPrefer.isEmpty)
    assert(req1.removePrefer == req1)

    val req2 = req1.setPrefer("lenient", "respond-async", "wait=10")
    assert(req2.hasPrefer)
    assert(req2.prefer == Seq[Preference]("lenient", "respond-async", "wait=10"))
    assert(req2.getPrefer.contains(Seq[Preference]("lenient", "respond-async", "wait=10")))
    assert(req2.removePrefer == req1)
    assert(req2.getHeaderValue("Prefer").contains("lenient, respond-async, wait=10"))
  }

  it should "create response with Preference-Applied header" in {
    val res1 = Ok()
    assert(!res1.hasPreferenceApplied)
    assert(res1.preferenceApplied.isEmpty)
    assert(res1.getPreferenceApplied.isEmpty)
    assert(res1.removePreferenceApplied == res1)

    val res2 = res1.setPreferenceApplied("lenient", "respond-async", "wait=10")
    assert(res2.hasPreferenceApplied)
    assert(res2.preferenceApplied == Seq[Preference]("lenient", "respond-async", "wait=10"))
    assert(res2.getPreferenceApplied.contains(Seq[Preference]("lenient", "respond-async", "wait=10")))
    assert(res2.removePreferenceApplied == res1)
    assert(res2.getHeaderValue("Preference-Applied").contains("lenient, respond-async, wait=10"))
  }

  it should "create request with Range header" in {
    val req1 = Get("/")
    assert(!req1.hasRange)
    assertThrows[HeaderNotFound](req1.range)
    assert(req1.getRange.isEmpty)
    assert(req1.removeRange == req1)

    val req2 = req1.setRange("bytes=1024-8191")
    assert(req2.hasRange)
    assert(req2.range == ByteRange(Slice(1024, Some(8191))))
    assert(req2.getRange.contains(ByteRange(Slice(1024, Some(8191)))))
    assert(req2.removeRange == req1)
    assert(req2.getHeaderValue("Range").contains("bytes=1024-8191"))
  }

  it should "create request with Referer header" in {
    val req1 = Get("/")
    assert(!req1.hasReferer)
    assertThrows[HeaderNotFound](req1.referer)
    assert(req1.getReferer.isEmpty)
    assert(req1.removeReferer == req1)

    val req2 = req1.setReferer("/path/to/file.html")
    assert(req2.hasReferer)
    assert(req2.referer == Uri("/path/to/file.html"))
    assert(req2.getReferer.contains(Uri("/path/to/file.html")))
    assert(req2.removeReferer == req1)
    assert(req2.getHeaderValue("Referer").contains("/path/to/file.html"))
  }

  it should "create response with Retry-After header" in {
    val res1 = Ok()
    assert(!res1.hasRetryAfter)
    assertThrows[HeaderNotFound](res1.retryAfter)
    assert(res1.getRetryAfter.isEmpty)
    assert(res1.removeRetryAfter == res1)

    val res2 = res1.setRetryAfter(Instant("2020-12-25T07:34:16Z"))
    assert(res2.hasRetryAfter)
    assert(res2.retryAfter == Instant("2020-12-25T07:34:16Z"))
    assert(res2.getRetryAfter.contains(Instant("2020-12-25T07:34:16Z")))
    assert(res2.removeRetryAfter == res1)
    assert(res2.getHeaderValue("Retry-After").contains("Fri, 25 Dec 2020 07:34:16 GMT"))
  }

  it should "create response with Server header" in {
    val res1 = Ok()
    assert(!res1.hasServer)
    assert(res1.server.isEmpty)
    assert(res1.getServer.isEmpty)
    assert(res1.removeServer == res1)

    val res2 = res1.setServer("Scamper/20.0.0", "OpenJDK/1.8.275")
    assert(res2.hasServer)
    assert(res2.server == Seq(ProductType("Scamper", Some("20.0.0")), ProductType("OpenJDK", Some("1.8.275"))))
    assert(res2.getServer.contains(Seq(ProductType("Scamper", Some("20.0.0")), ProductType("OpenJDK", Some("1.8.275")))))
    assert(res2.removeServer == res1)
    assert(res2.getHeaderValue("Server").contains("Scamper/20.0.0 OpenJDK/1.8.275"))
  }

  it should "create request with TE header" in {
    val req1 = Get("/")
    assert(!req1.hasTE)
    assert(req1.te.isEmpty)
    assert(req1.getTE.isEmpty)
    assert(req1.removeTE == req1)

    val req2 = req1.setTE("gzip; q=0.9", "deflate; q=0.1")
    assert(req2.hasTE)
    assert(req2.te == Seq[TransferCodingRange]("gzip; q=0.9", "deflate; q=0.1"))
    assert(req2.getTE.contains(Seq[TransferCodingRange]("gzip; q=0.9", "deflate; q=0.1")))
    assert(req2.removeTE == req1)
    assert(req2.getHeaderValue("TE").contains("gzip; q=0.9, deflate; q=0.1"))
  }

  it should "create message with Trailer header" in {
    val req1 = Get("/")
    assert(!req1.hasTrailer)
    assert(req1.trailer.isEmpty)
    assert(req1.getTrailer.isEmpty)
    assert(req1.removeTrailer == req1)

    val req2 = req1.setTrailer("Content-Length", "Content-Language")
    assert(req2.hasTrailer)
    assert(req2.trailer == Seq("Content-Length", "Content-Language"))
    assert(req2.getTrailer.contains(Seq("Content-Length", "Content-Language")))
    assert(req2.removeTrailer == req1)
    assert(req2.getHeaderValue("Trailer").contains("Content-Length, Content-Language"))

    val res1 = Ok()
    assert(!res1.hasTrailer)
    assert(res1.trailer.isEmpty)
    assert(res1.getTrailer.isEmpty)
    assert(res1.removeTrailer == res1)

    val res2 = res1.setTrailer("Content-Length", "Content-Language")
    assert(res2.hasTrailer)
    assert(res2.trailer == Seq("Content-Length", "Content-Language"))
    assert(res2.getTrailer.contains(Seq("Content-Length", "Content-Language")))
    assert(res2.removeTrailer == res1)
    assert(res2.getHeaderValue("Trailer").contains("Content-Length, Content-Language"))
  }

  it should "create message with Transfer-Encoding header" in {
    val req1 = Get("/")
    assert(!req1.hasTransferEncoding)
    assert(req1.transferEncoding.isEmpty)
    assert(req1.getTransferEncoding.isEmpty)
    assert(req1.removeTransferEncoding == req1)

    val req2 = req1.setTransferEncoding("deflate", "gzip")
    assert(req2.hasTransferEncoding)
    assert(req2.transferEncoding == Seq[TransferCoding]("deflate", "gzip"))
    assert(req2.getTransferEncoding.contains(Seq[TransferCoding]("deflate", "gzip")))
    assert(req2.removeTransferEncoding == req1)
    assert(req2.getHeaderValue("Transfer-Encoding").contains("deflate, gzip"))

    val res1 = Ok()
    assert(!res1.hasTransferEncoding)
    assert(res1.transferEncoding.isEmpty)
    assert(res1.getTransferEncoding.isEmpty)
    assert(res1.removeTransferEncoding == res1)

    val res2 = res1.setTransferEncoding("deflate", "gzip")
    assert(res2.hasTransferEncoding)
    assert(res2.transferEncoding == Seq[TransferCoding]("deflate", "gzip"))
    assert(res2.getTransferEncoding.contains(Seq[TransferCoding]("deflate", "gzip")))
    assert(res2.removeTransferEncoding == res1)
    assert(res2.getHeaderValue("Transfer-Encoding").contains("deflate, gzip"))
  }

  it should "create message with Upgrade header" in {
    val req1 = Get("/")
    assert(!req1.hasUpgrade)
    assert(req1.upgrade.isEmpty)
    assert(req1.getUpgrade.isEmpty)
    assert(req1.removeUpgrade == req1)

    val req2 = req1.setUpgrade("websocket", "h2c")
    assert(req2.hasUpgrade)
    assert(req2.upgrade == Seq[Protocol]("websocket", "h2c"))
    assert(req2.getUpgrade.contains(Seq[Protocol]("websocket", "h2c")))
    assert(req2.removeUpgrade == req1)
    assert(req2.getHeaderValue("Upgrade").contains("websocket, h2c"))

    val res1 = Ok()
    assert(!res1.hasUpgrade)
    assert(res1.upgrade.isEmpty)
    assert(res1.getUpgrade.isEmpty)
    assert(res1.removeUpgrade == res1)

    val res2 = res1.setUpgrade("websocket", "h2c")
    assert(res2.hasUpgrade)
    assert(res2.upgrade == Seq[Protocol]("websocket", "h2c"))
    assert(res2.getUpgrade.contains(Seq[Protocol]("websocket", "h2c")))
    assert(res2.removeUpgrade == res1)
    assert(res2.getHeaderValue("Upgrade").contains("websocket, h2c"))
  }

  it should "create request with User-Agent header" in {
    val req1 = Get("/")
    assert(!req1.hasUserAgent)
    assert(req1.userAgent.isEmpty)
    assert(req1.getUserAgent.isEmpty)
    assert(req1.removeUserAgent == req1)

    val req2 = req1.setUserAgent("Scamper/20.0.0", "OpenJDK/1.8.275")
    assert(req2.hasUserAgent)
    assert(req2.userAgent == Seq(ProductType("Scamper", Some("20.0.0")), ProductType("OpenJDK", Some("1.8.275"))))
    assert(req2.getUserAgent.contains(Seq(ProductType("Scamper", Some("20.0.0")), ProductType("OpenJDK", Some("1.8.275")))))
    assert(req2.removeUserAgent == req1)
    assert(req2.getHeaderValue("User-Agent").contains("Scamper/20.0.0 OpenJDK/1.8.275"))
  }

  it should "create response with Vary header" in {
    val res1 = Ok()
    assert(!res1.hasVary)
    assert(res1.vary.isEmpty)
    assert(res1.getVary.isEmpty)
    assert(res1.removeVary == res1)

    val res2 = res1.setVary("Accept-Encoding", "Accept-Language")
    assert(res2.hasVary)
    assert(res2.vary == Seq("Accept-Encoding", "Accept-Language"))
    assert(res2.getVary.contains(Seq("Accept-Encoding", "Accept-Language")))
    assert(res2.removeVary == res1)
    assert(res2.getHeaderValue("Vary").contains("Accept-Encoding, Accept-Language"))
  }

  it should "create message with Via header" in {
    val req1 = Get("/")
    assert(!req1.hasVia)
    assert(req1.via.isEmpty)
    assert(req1.getVia.isEmpty)
    assert(req1.removeVia == req1)

    val req2 = req1.setVia("1.0 abc", "1.1 xyz")
    assert(req2.hasVia)
    assert(req2.via == Seq[ViaType]("1.0 abc", "1.1 xyz"))
    assert(req2.getVia.contains(Seq[ViaType]("1.0 abc", "1.1 xyz")))
    assert(req2.removeVia == req1)
    assert(req2.getHeaderValue("Via").contains("1.0 abc, 1.1 xyz"))

    val res1 = Ok()
    assert(!res1.hasVia)
    assert(res1.via.isEmpty)
    assert(res1.getVia.isEmpty)
    assert(res1.removeVia == res1)

    val res2 = res1.setVia("1.0 abc", "1.1 xyz")
    assert(res2.hasVia)
    assert(res2.via == Seq[ViaType]("1.0 abc", "1.1 xyz"))
    assert(res2.getVia.contains(Seq[ViaType]("1.0 abc", "1.1 xyz")))
    assert(res2.removeVia == res1)
    assert(res2.getHeaderValue("Via").contains("1.0 abc, 1.1 xyz"))
  }

  it should "create message with Warning header" in {
    val req1 = Get("/")
    assert(!req1.hasWarning)
    assert(req1.warning.isEmpty)
    assert(req1.getWarning.isEmpty)
    assert(req1.removeWarning == req1)

    val req2 = req1.setWarning("110 - \"reqponse is Stale\"", "113 - \"Heuristic Expiration\"")
    assert(req2.hasWarning)
    assert(req2.warning == Seq(WarningType(110, "-", "reqponse is Stale"), WarningType(113, "-", "Heuristic Expiration")))
    assert(req2.getWarning.contains(Seq(WarningType(110, "-", "reqponse is Stale"), WarningType(113, "-", "Heuristic Expiration"))))
    assert(req2.removeWarning == req1)
    assert(req2.getHeaderValue("Warning").contains("110 - \"reqponse is Stale\", 113 - \"Heuristic Expiration\""))

    val res1 = Ok()
    assert(!res1.hasWarning)
    assert(res1.warning.isEmpty)
    assert(res1.getWarning.isEmpty)
    assert(res1.removeWarning == res1)

    val res2 = res1.setWarning("110 - \"Response is Stale\"", "113 - \"Heuristic Expiration\"")
    assert(res2.hasWarning)
    assert(res2.warning == Seq(WarningType(110, "-", "Response is Stale"), WarningType(113, "-", "Heuristic Expiration")))
    assert(res2.getWarning.contains(Seq(WarningType(110, "-", "Response is Stale"), WarningType(113, "-", "Heuristic Expiration"))))
    assert(res2.removeWarning == res1)
    assert(res2.getHeaderValue("Warning").contains("110 - \"Response is Stale\", 113 - \"Heuristic Expiration\""))
  }
