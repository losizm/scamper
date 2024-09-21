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
    assert(req1.acceptOption.isEmpty)
    assert(req1.acceptRemoved == req1)

    val req2 = req1.setAccept("text/html; q=0.9", "text/plain; q=0.1")
    assert(req2.hasAccept)
    assert(req2.accept == Seq[MediaRange]("text/html; q=0.9", "text/plain; q=0.1"))
    assert(req2.acceptOption.contains(Seq[MediaRange]("text/html; q=0.9", "text/plain; q=0.1")))
    assert(req2.acceptRemoved == req1)
    assert(req2.getHeaderValue("Accept").contains("text/html; q=0.9, text/plain; q=0.1"))
  }

  it should "create request with Accept-Charset header" in {
    val req1 = Get("/")
    assert(!req1.hasAcceptCharset)
    assert(req1.acceptCharset.isEmpty)
    assert(req1.acceptCharsetOption.isEmpty)
    assert(req1.acceptCharsetRemoved == req1)

    val req2 = req1.setAcceptCharset("utf-8; q=0.9", "ascii; q=0.1")
    assert(req2.hasAcceptCharset)
    assert(req2.acceptCharset == Seq[CharsetRange]("utf-8; q=0.9", "ascii; q=0.1"))
    assert(req2.acceptCharsetOption.contains(Seq[CharsetRange]("utf-8; q=0.9", "ascii; q=0.1")))
    assert(req2.acceptCharsetRemoved == req1)
    assert(req2.getHeaderValue("Accept-Charset").contains("utf-8; q=0.9, ascii; q=0.1"))
  }

  it should "create request with Accept-Encoding header" in {
    val req1 = Get("/")
    assert(!req1.hasAcceptEncoding)
    assert(req1.acceptEncoding.isEmpty)
    assert(req1.acceptEncodingOption.isEmpty)
    assert(req1.acceptEncodingRemoved == req1)

    val req2 = req1.setAcceptEncoding("gzip; q=0.9", "deflate; q=0.1")
    assert(req2.hasAcceptEncoding)
    assert(req2.acceptEncoding == Seq[ContentCodingRange]("gzip; q=0.9", "deflate; q=0.1"))
    assert(req2.acceptEncodingOption.contains(Seq[ContentCodingRange]("gzip; q=0.9", "deflate; q=0.1")))
    assert(req2.acceptEncodingRemoved == req1)
    assert(req2.getHeaderValue("Accept-Encoding").contains("gzip; q=0.9, deflate; q=0.1"))
  }

  it should "create request with Accept-Language header" in {
    val req1 = Get("/")
    assert(!req1.hasAcceptLanguage)
    assert(req1.acceptLanguage.isEmpty)
    assert(req1.acceptLanguageOption.isEmpty)
    assert(req1.acceptLanguageRemoved == req1)

    val req2 = req1.setAcceptLanguage("en-US; q=0.9", "en-GB; q=0.1")
    assert(req2.hasAcceptLanguage)
    assert(req2.acceptLanguage == Seq[LanguageRange]("en-US; q=0.9", "en-GB; q=0.1"))
    assert(req2.acceptLanguageOption.contains(Seq[LanguageRange]("en-US; q=0.9", "en-GB; q=0.1")))
    assert(req2.acceptLanguageRemoved == req1)
    assert(req2.getHeaderValue("Accept-Language").contains("en-US; q=0.9, en-GB; q=0.1"))
  }

  it should "create response with Accept-Patch header" in {
    val res1 = Ok()
    assert(!res1.hasAcceptPatch)
    assert(res1.acceptPatch.isEmpty)
    assert(res1.acceptPatchOption.isEmpty)
    assert(res1.acceptPatchRemoved == res1)

    val res2 = res1.setAcceptPatch("text/plain", "application/octet-stream")
    assert(res2.hasAcceptPatch)
    assert(res2.acceptPatch == Seq[MediaType]("text/plain", "application/octet-stream"))
    assert(res2.acceptPatchOption.contains(Seq[MediaType]("text/plain", "application/octet-stream")))
    assert(res2.acceptPatchRemoved == res1)
    assert(res2.getHeaderValue("Accept-Patch").contains("text/plain, application/octet-stream"))
  }

  it should "create response with Accept-Ranges header" in {
    val res1 = Ok()
    assert(!res1.hasAcceptRanges)
    assert(res1.acceptRanges.isEmpty)
    assert(res1.acceptRangesOption.isEmpty)
    assert(res1.acceptRangesRemoved == res1)

    val res2 = res1.setAcceptRanges("text/plain", "application/octet-stream")
    assert(res2.hasAcceptRanges)
    assert(res2.acceptRanges == Seq("text/plain", "application/octet-stream"))
    assert(res2.acceptRangesOption.contains(Seq("text/plain", "application/octet-stream")))
    assert(res2.acceptRangesRemoved == res1)
    assert(res2.getHeaderValue("Accept-Ranges").contains("text/plain, application/octet-stream"))
  }

  it should "create response with Age header" in {
    val res1 = Ok()
    assert(!res1.hasAge)
    assertThrows[HeaderNotFound](res1.age)
    assert(res1.ageOption.isEmpty)
    assert(res1.ageRemoved == res1)

    val res2 = res1.setAge(600)
    assert(res2.hasAge)
    assert(res2.age == 600)
    assert(res2.ageOption.contains(600))
    assert(res2.ageRemoved == res1)
    assert(res2.getHeaderValue("Age").contains("600"))
  }

  it should "create response with Allow header" in {
    val res1 = Ok()
    assert(!res1.hasAllow)
    assert(res1.allow.isEmpty)
    assert(res1.allowOption.isEmpty)
    assert(res1.allowRemoved == res1)

    val res2 = res1.setAllow(Get, Post)
    assert(res2.hasAllow)
    assert(res2.allow == Seq(Get, Post))
    assert(res2.allowOption.contains(Seq(Get, Post)))
    assert(res2.allowRemoved == res1)
    assert(res2.getHeaderValue("Allow").contains("GET, POST"))
  }

  it should "create message with Cache-Control header" in {
    val req1 = Get("/")
    assert(!req1.hasCacheControl)
    assert(req1.cacheControl.isEmpty)
    assert(req1.cacheControlOption.isEmpty)
    assert(req1.cacheControlRemoved == req1)

    val req2 = req1.setCacheControl("max-age=600", "no-cache")
    assert(req2.hasCacheControl)
    assert(req2.cacheControl == Seq[CacheDirective]("max-age=600", "no-cache"))
    assert(req2.cacheControlOption.contains(Seq[CacheDirective]("max-age=600", "no-cache")))
    assert(req2.cacheControlRemoved == req1)
    assert(req2.getHeaderValue("Cache-Control").contains("max-age=600, no-cache"))

    val res1 = Ok()
    assert(!res1.hasCacheControl)
    assert(res1.cacheControl.isEmpty)
    assert(res1.cacheControlOption.isEmpty)
    assert(res1.cacheControlRemoved == res1)

    val res2 = res1.setCacheControl("no-store", "no-transform")
    assert(res2.hasCacheControl)
    assert(res2.cacheControl == Seq[CacheDirective]("no-store", "no-transform"))
    assert(res2.cacheControlOption.contains(Seq[CacheDirective]("no-store", "no-transform")))
    assert(res2.cacheControlRemoved == res1)
    assert(res2.getHeaderValue("Cache-Control").contains("no-store, no-transform"))
  }

  it should "create message with Connection header" in {
    val req1 = Get("/")
    assert(!req1.hasConnection)
    assert(req1.connection.isEmpty)
    assert(req1.connectionOption.isEmpty)
    assert(req1.connectionRemoved == req1)

    val req2 = req1.setConnection("TE", "close")
    assert(req2.hasConnection)
    assert(req2.connection == Seq("TE", "close"))
    assert(req2.connectionOption.contains(Seq("TE", "close")))
    assert(req2.connectionRemoved == req1)
    assert(req2.getHeaderValue("Connection").contains("TE, close"))

    val res1 = Ok()
    assert(!res1.hasConnection)
    assert(res1.connection.isEmpty)
    assert(res1.connectionOption.isEmpty)
    assert(res1.connectionRemoved == res1)

    val res2 = res1.setConnection("TE", "close")
    assert(res2.hasConnection)
    assert(res2.connection == Seq("TE", "close"))
    assert(res2.connectionOption.contains(Seq("TE", "close")))
    assert(res2.connectionRemoved == res1)
    assert(res2.getHeaderValue("Connection").contains("TE, close"))
  }

  it should "create response with Content-Disposition header" in {
    val res1 = Ok()
    assert(!res1.hasContentDisposition)
    assertThrows[HeaderNotFound](res1.contentDisposition)
    assert(res1.contentDispositionOption.isEmpty)
    assert(res1.contentDispositionRemoved == res1)

    val res2 = res1.setContentDisposition("attachment; filename=file.txt")
    assert(res2.hasContentDisposition)
    assert(res2.contentDisposition == DispositionType("attachment", "filename" -> "file.txt"))
    assert(res2.contentDispositionOption.contains(DispositionType("attachment", "filename" -> "file.txt")))
    assert(res2.contentDispositionRemoved == res1)
    assert(res2.getHeaderValue("Content-Disposition").contains("attachment; filename=file.txt"))
  }

  it should "create message with Content-Encoding header" in {
    val req1 = Get("/")
    assert(!req1.hasContentEncoding)
    assert(req1.contentEncoding.isEmpty)
    assert(req1.contentEncodingOption.isEmpty)
    assert(req1.contentEncodingRemoved == req1)

    val req2 = req1.setContentEncoding("deflate", "gzip")
    assert(req2.hasContentEncoding)
    assert(req2.contentEncoding == Seq[ContentCoding]("deflate", "gzip"))
    assert(req2.contentEncodingOption.contains(Seq[ContentCoding]("deflate", "gzip")))
    assert(req2.contentEncodingRemoved == req1)
    assert(req2.getHeaderValue("Content-Encoding").contains("deflate, gzip"))

    val res1 = Ok()
    assert(!res1.hasContentEncoding)
    assert(res1.contentEncoding.isEmpty)
    assert(res1.contentEncodingOption.isEmpty)
    assert(res1.contentEncodingRemoved == res1)

    val res2 = res1.setContentEncoding("deflate", "gzip")
    assert(res2.hasContentEncoding)
    assert(res2.contentEncoding == Seq[ContentCoding]("deflate", "gzip"))
    assert(res2.contentEncodingOption.contains(Seq[ContentCoding]("deflate", "gzip")))
    assert(res2.contentEncodingRemoved == res1)
    assert(res2.getHeaderValue("Content-Encoding").contains("deflate, gzip"))
  }

  it should "create message with Content-Language header" in {
    val req1 = Get("/")
    assert(!req1.hasContentLanguage)
    assert(req1.contentLanguage.isEmpty)
    assert(req1.contentLanguageOption.isEmpty)
    assert(req1.contentLanguageRemoved == req1)

    val req2 = req1.setContentLanguage("en-US", "en-GB")
    assert(req2.hasContentLanguage)
    assert(req2.contentLanguage == Seq[LanguageTag]("en-US", "en-GB"))
    assert(req2.contentLanguageOption.contains(Seq[LanguageTag]("en-US", "en-GB")))
    assert(req2.contentLanguageRemoved == req1)
    assert(req2.getHeaderValue("Content-Language").contains("en-US, en-GB"))

    val res1 = Ok()
    assert(!res1.hasContentLanguage)
    assert(res1.contentLanguage.isEmpty)
    assert(res1.contentLanguageOption.isEmpty)
    assert(res1.contentLanguageRemoved == res1)

    val res2 = res1.setContentLanguage("en-US", "en-GB")
    assert(res2.hasContentLanguage)
    assert(res2.contentLanguage == Seq[LanguageTag]("en-US", "en-GB"))
    assert(res2.contentLanguageOption.contains(Seq[LanguageTag]("en-US", "en-GB")))
    assert(res2.contentLanguageRemoved == res1)
    assert(res2.getHeaderValue("Content-Language").contains("en-US, en-GB"))
  }

  it should "create message with Content-Length header" in {
    val req1 = Get("/")
    assert(!req1.hasContentLength)
    assertThrows[HeaderNotFound](req1.contentLength)
    assert(req1.contentLengthOption.isEmpty)
    assert(req1.contentLengthRemoved == req1)

    val req2 = req1.setContentLength(1024)
    assert(req2.hasContentLength)
    assert(req2.contentLength == 1024)
    assert(req2.contentLengthOption.contains(1024))
    assert(req2.contentLengthRemoved == req1)
    assert(req2.getHeaderValue("Content-Length").contains("1024"))

    val res1 = Ok()
    assert(!res1.hasContentLength)
    assertThrows[HeaderNotFound](res1.contentLength)
    assert(res1.contentLengthOption.isEmpty)
    assert(res1.contentLengthRemoved == res1)

    val res2 = res1.setContentLength(1024)
    assert(res2.hasContentLength)
    assert(res2.contentLength == 1024)
    assert(res2.contentLengthOption.contains(1024))
    assert(res2.contentLengthRemoved == res1)
    assert(res2.getHeaderValue("Content-Length").contains("1024"))
  }

  it should "create message with Content-Location header" in {
    val req1 = Get("/")
    assert(!req1.hasContentLocation)
    assertThrows[HeaderNotFound](req1.contentLocation)
    assert(req1.contentLocationOption.isEmpty)
    assert(req1.contentLocationRemoved == req1)

    val req2 = req1.setContentLocation("/path/to/file.txt")
    assert(req2.hasContentLocation)
    assert(req2.contentLocation == Uri("/path/to/file.txt"))
    assert(req2.contentLocationOption.contains(Uri("/path/to/file.txt")))
    assert(req2.contentLocationRemoved == req1)
    assert(req2.getHeaderValue("Content-Location").contains("/path/to/file.txt"))

    val res1 = Ok()
    assert(!res1.hasContentLocation)
    assertThrows[HeaderNotFound](res1.contentLocation)
    assert(res1.contentLocationOption.isEmpty)
    assert(res1.contentLocationRemoved == res1)

    val res2 = res1.setContentLocation("/path/to/file.txt")
    assert(res2.hasContentLocation)
    assert(res2.contentLocation == Uri("/path/to/file.txt"))
    assert(res2.contentLocationOption.contains(Uri("/path/to/file.txt")))
    assert(res2.contentLocationRemoved == res1)
    assert(res2.getHeaderValue("Content-Location").contains("/path/to/file.txt"))
  }

  it should "create message with Content-Range header" in {
    val req1 = Get("/")
    assert(!req1.hasContentRange)
    assertThrows[HeaderNotFound](req1.contentRange)
    assert(req1.contentRangeOption.isEmpty)
    assert(req1.contentRangeRemoved == req1)

    val req2 = req1.setContentRange("bytes 1024-8191/*")
    assert(req2.hasContentRange)
    assert(req2.contentRange == ByteContentRange(Satisfied(1024, 8191, None)))
    assert(req2.contentRangeOption.contains(ByteContentRange(Satisfied(1024, 8191, None))))
    assert(req2.contentRangeRemoved == req1)
    assert(req2.getHeaderValue("Content-Range").contains("bytes 1024-8191/*"))

    val res1 = Ok()
    assert(!res1.hasContentRange)
    assertThrows[HeaderNotFound](res1.contentRange)
    assert(res1.contentRangeOption.isEmpty)
    assert(res1.contentRangeRemoved == res1)

    val res2 = res1.setContentRange("bytes 1024-8191/*")
    assert(res2.hasContentRange)
    assert(res2.contentRange == ByteContentRange(Satisfied(1024, 8191, None)))
    assert(res2.contentRangeOption.contains(ByteContentRange(Satisfied(1024, 8191, None))))
    assert(res2.contentRangeRemoved == res1)
    assert(res2.getHeaderValue("Content-Range").contains("bytes 1024-8191/*"))
  }

  it should "create message with Content-Type header" in {
    val req1 = Get("/")
    assert(!req1.hasContentType)
    assertThrows[HeaderNotFound](req1.contentType)
    assert(req1.contentTypeOption.isEmpty)
    assert(req1.contentTypeRemoved == req1)

    val req2 = req1.setContentType("text/plain")
    assert(req2.hasContentType)
    assert(req2.contentType == MediaType("text/plain"))
    assert(req2.contentTypeOption.contains(MediaType("text/plain")))
    assert(req2.contentTypeRemoved == req1)
    assert(req2.getHeaderValue("Content-Type").contains("text/plain"))

    val res1 = Ok()
    assert(!res1.hasContentType)
    assertThrows[HeaderNotFound](res1.contentType)
    assert(res1.contentTypeOption.isEmpty)
    assert(res1.contentTypeRemoved == res1)

    val res2 = res1.setContentType("text/plain")
    assert(res2.hasContentType)
    assert(res2.contentType == MediaType("text/plain"))
    assert(res2.contentTypeOption.contains(MediaType("text/plain")))
    assert(res2.contentTypeRemoved == res1)
    assert(res2.getHeaderValue("Content-Type").contains("text/plain"))
  }

  it should "create response with Date header" in {
    val res1 = Ok()
    assert(!res1.hasDate)
    assertThrows[HeaderNotFound](res1.date)
    assert(res1.dateOption.isEmpty)
    assert(res1.dateRemoved == res1)

    val res2 = res1.setDate(Instant("2020-12-25T07:34:16Z"))
    assert(res2.hasDate)
    assert(res2.date == Instant("2020-12-25T07:34:16Z"))
    assert(res2.dateOption.contains(Instant("2020-12-25T07:34:16Z")))
    assert(res2.dateRemoved == res1)
    assert(res2.getHeaderValue("Date").contains("Fri, 25 Dec 2020 07:34:16 GMT"))
  }

  it should "create response with ETag header" in {
    val res1 = Ok()
    assert(!res1.hasETag)
    assertThrows[HeaderNotFound](res1.eTag)
    assert(res1.eTagOption.isEmpty)
    assert(res1.eTagRemoved == res1)

    val res2 = res1.setETag("W/\"abc\"")
    assert(res2.hasETag)
    assert(res2.eTag == EntityTag("abc", true))
    assert(res2.eTagOption.contains(EntityTag("abc", true)))
    assert(res2.eTagRemoved == res1)
    assert(res2.getHeaderValue("ETag").contains("W/\"abc\""))
  }

  it should "create request with Early-Data header" in {
    val req1 = Get("/")
    assert(!req1.hasEarlyData)
    assertThrows[HeaderNotFound](req1.earlyData)
    assert(req1.earlyDataOption.isEmpty)
    assert(req1.earlyDataRemoved == req1)

    val req2 = req1.setEarlyData(1)
    assert(req2.hasEarlyData)
    assert(req2.earlyData == 1)
    assert(req2.earlyDataOption.contains(1))
    assert(req2.earlyDataRemoved == req1)
    assert(req2.getHeaderValue("Early-Data").contains("1"))
  }

  it should "create request with Expect header" in {
    val req1 = Get("/")
    assert(!req1.hasExpect)
    assertThrows[HeaderNotFound](req1.expect)
    assert(req1.expectOption.isEmpty)
    assert(req1.expectRemoved == req1)

    val req2 = req1.setExpect("100-Continue")
    assert(req2.hasExpect)
    assert(req2.expect == "100-Continue")
    assert(req2.expectOption.contains("100-Continue"))
    assert(req2.expectRemoved == req1)
    assert(req2.getHeaderValue("Expect").contains("100-Continue"))
  }

  it should "create response with Expires header" in {
    val res1 = Ok()
    assert(!res1.hasExpires)
    assertThrows[HeaderNotFound](res1.expires)
    assert(res1.expiresOption.isEmpty)
    assert(res1.expiresRemoved == res1)

    val res2 = res1.setExpires(Instant("2020-12-25T07:34:16Z"))
    assert(res2.hasExpires)
    assert(res2.expires == Instant("2020-12-25T07:34:16Z"))
    assert(res2.expiresOption.contains(Instant("2020-12-25T07:34:16Z")))
    assert(res2.expiresRemoved == res1)
    assert(res2.getHeaderValue("Expires").contains("Fri, 25 Dec 2020 07:34:16 GMT"))
  }

  it should "create request with From header" in {
    val req1 = Get("/")
    assert(!req1.hasFrom)
    assertThrows[HeaderNotFound](req1.from)
    assert(req1.fromOption.isEmpty)
    assert(req1.fromRemoved == req1)

    val req2 = req1.setFrom("lupita@xyz.com")
    assert(req2.hasFrom)
    assert(req2.from == "lupita@xyz.com")
    assert(req2.fromOption.contains("lupita@xyz.com"))
    assert(req2.fromRemoved == req1)
    assert(req2.getHeaderValue("From").contains("lupita@xyz.com"))
  }

  it should "create request with Host header" in {
    val req1 = Get("/")
    assert(!req1.hasHost)
    assertThrows[HeaderNotFound](req1.host)
    assert(req1.hostOption.isEmpty)
    assert(req1.hostRemoved == req1)

    val req2 = req1.setHost("localhost:8080")
    assert(req2.hasHost)
    assert(req2.host == "localhost:8080")
    assert(req2.hostOption.contains("localhost:8080"))
    assert(req2.hostRemoved == req1)
    assert(req2.getHeaderValue("Host").contains("localhost:8080"))
  }

  it should "create request with If-Match header" in {
    val req1 = Get("/")
    assert(!req1.hasIfMatch)
    assert(req1.ifMatch.isEmpty)
    assert(req1.ifMatchOption.isEmpty)
    assert(req1.ifMatchRemoved == req1)

    val req2 = req1.setIfMatch("W/\"abc\"", "W/\"xyz\"")
    assert(req2.hasIfMatch)
    assert(req2.ifMatch == Seq(EntityTag("abc", true), EntityTag("xyz", true)))
    assert(req2.ifMatchOption.contains(Seq(EntityTag("abc", true), EntityTag("xyz", true))))
    assert(req2.ifMatchRemoved == req1)
    assert(req2.getHeaderValue("If-Match").contains("W/\"abc\", W/\"xyz\""))
  }

  it should "create request with If-Modified-Since header" in {
    val req1 = Get("/")
    assert(!req1.hasIfModifiedSince)
    assertThrows[HeaderNotFound](req1.ifModifiedSince)
    assert(req1.ifModifiedSinceOption.isEmpty)
    assert(req1.ifModifiedSinceRemoved == req1)

    val req2 = req1.setIfModifiedSince(Instant("2020-12-25T07:34:16Z"))
    assert(req2.hasIfModifiedSince)
    assert(req2.ifModifiedSince == Instant("2020-12-25T07:34:16Z"))
    assert(req2.ifModifiedSinceOption.contains(Instant("2020-12-25T07:34:16Z")))
    assert(req2.ifModifiedSinceRemoved == req1)
    assert(req2.getHeaderValue("If-Modified-Since").contains("Fri, 25 Dec 2020 07:34:16 GMT"))
  }

  it should "create request with If-None-Match header" in {
    val req1 = Get("/")
    assert(!req1.hasIfNoneMatch)
    assert(req1.ifNoneMatch.isEmpty)
    assert(req1.ifNoneMatchOption.isEmpty)
    assert(req1.ifNoneMatchRemoved == req1)

    val req2 = req1.setIfNoneMatch("W/\"abc\"", "W/\"xyz\"")
    assert(req2.hasIfNoneMatch)
    assert(req2.ifNoneMatch == Seq(EntityTag("abc", true), EntityTag("xyz", true)))
    assert(req2.ifNoneMatchOption.contains(Seq(EntityTag("abc", true), EntityTag("xyz", true))))
    assert(req2.ifNoneMatchRemoved == req1)
    assert(req2.getHeaderValue("If-None-Match").contains("W/\"abc\", W/\"xyz\""))
  }

  it should "create request with If-Range header" in {
    val req1 = Get("/")
    assert(!req1.hasIfRange)
    assertThrows[HeaderNotFound](req1.ifRange)
    assert(req1.ifRangeOption.isEmpty)
    assert(req1.ifRangeRemoved == req1)

    val req2 = req1.setIfRange("W/\"abc\"")
    assert(req2.hasIfRange)
    assert(req2.ifRange == Left(EntityTag("abc", true)))
    assert(req2.ifRangeOption.contains(Left(EntityTag("abc", true))))
    assert(req2.ifRangeRemoved == req1)
    assert(req2.getHeaderValue("If-Range").contains("W/\"abc\""))

    val req3 = req1.setIfRange(Instant("2020-12-25T07:34:16Z"))
    assert(req3.hasIfRange)
    assert(req3.ifRange == Right(Instant("2020-12-25T07:34:16Z")))
    assert(req3.ifRangeOption.contains(Right(Instant("2020-12-25T07:34:16Z"))))
    assert(req3.ifRangeRemoved == req1)
    assert(req3.getHeaderValue("If-Range").contains("Fri, 25 Dec 2020 07:34:16 GMT"))
  }

  it should "create request with If-Unmodified-Since header" in {
    val req1 = Get("/")
    assert(!req1.hasIfUnmodifiedSince)
    assertThrows[HeaderNotFound](req1.ifUnmodifiedSince)
    assert(req1.ifUnmodifiedSinceOption.isEmpty)
    assert(req1.ifUnmodifiedSinceRemoved == req1)

    val req2 = req1.setIfUnmodifiedSince(Instant("2020-12-25T07:34:16Z"))
    assert(req2.hasIfUnmodifiedSince)
    assert(req2.ifUnmodifiedSince == Instant("2020-12-25T07:34:16Z"))
    assert(req2.ifUnmodifiedSinceOption.contains(Instant("2020-12-25T07:34:16Z")))
    assert(req2.ifUnmodifiedSinceRemoved == req1)
    assert(req2.getHeaderValue("If-Unmodified-Since").contains("Fri, 25 Dec 2020 07:34:16 GMT"))
  }

  it should "create message with Keep-Alive header" in {
    val req1 = Get("/")
    assert(!req1.hasKeepAlive)
    assertThrows[HeaderNotFound](req1.keepAlive)
    assert(req1.keepAliveOption.isEmpty)
    assert(req1.keepAliveRemoved == req1)

    val req2 = req1.setKeepAlive("timeout=5, max=10")
    assert(req2.hasKeepAlive)
    assert(req2.keepAlive == KeepAliveParameters(5, 10))
    assert(req2.keepAliveOption.contains(KeepAliveParameters(5, 10)))
    assert(req2.keepAliveRemoved == req1)
    assert(req2.getHeaderValue("Keep-Alive").contains("timeout=5, max=10"))

    val res1 = Ok()
    assert(!res1.hasKeepAlive)
    assertThrows[HeaderNotFound](res1.keepAlive)
    assert(res1.keepAliveOption.isEmpty)
    assert(res1.keepAliveRemoved == res1)

    val res2 = res1.setKeepAlive("timeout=5, max=10")
    assert(res2.hasKeepAlive)
    assert(res2.keepAlive == KeepAliveParameters(5, 10))
    assert(res2.keepAliveOption.contains(KeepAliveParameters(5, 10)))
    assert(res2.keepAliveRemoved == res1)
    assert(res2.getHeaderValue("Keep-Alive").contains("timeout=5, max=10"))
  }

  it should "create response with Last-Modified header" in {
    val res1 = Ok()
    assert(!res1.hasLastModified)
    assertThrows[HeaderNotFound](res1.lastModified)
    assert(res1.lastModifiedOption.isEmpty)
    assert(res1.lastModifiedRemoved == res1)

    val res2 = res1.setLastModified(Instant("2020-12-25T07:34:16Z"))
    assert(res2.hasLastModified)
    assert(res2.lastModified == Instant("2020-12-25T07:34:16Z"))
    assert(res2.lastModifiedOption.contains(Instant("2020-12-25T07:34:16Z")))
    assert(res2.lastModifiedRemoved == res1)
    assert(res2.getHeaderValue("Last-Modified").contains("Fri, 25 Dec 2020 07:34:16 GMT"))
  }

  it should "create response with Link header" in {
    val res1 = Ok()
    assert(!res1.hasLink)
    assert(res1.link.isEmpty)
    assert(res1.linkOption.isEmpty)
    assert(res1.linkRemoved == res1)

    val res2 = res1.setLink("</Chapter1>; rel=previous", "</Chapter3>; rel=next")
    assert(res2.hasLink)
    assert(res2.link == Seq[LinkType](LinkType("/Chapter1", "rel" -> Some("previous")), LinkType("/Chapter3", "rel" -> Some("next"))))
    assert(res2.linkOption.contains(Seq[LinkType](LinkType("/Chapter1", "rel" -> Some("previous")), LinkType("/Chapter3", "rel" -> Some("next")))))
    assert(res2.linkRemoved == res1)
    assert(res2.getHeaderValue("Link").contains("</Chapter1>; rel=previous, </Chapter3>; rel=next"))
  }

  it should "create response with Location header" in {
    val res1 = Ok()
    assert(!res1.hasLocation)
    assertThrows[HeaderNotFound](res1.location)
    assert(res1.locationOption.isEmpty)
    assert(res1.locationRemoved == res1)

    val res2 = res1.setLocation("/path/to/file.txt")
    assert(res2.hasLocation)
    assert(res2.location == Uri("/path/to/file.txt"))
    assert(res2.locationOption.contains(Uri("/path/to/file.txt")))
    assert(res2.locationRemoved == res1)
    assert(res2.getHeaderValue("Location").contains("/path/to/file.txt"))
  }

  it should "create request with Max-Forwards header" in {
    val req1 = Get("/")
    assert(!req1.hasMaxForwards)
    assertThrows[HeaderNotFound](req1.maxForwards)
    assert(req1.maxForwardsOption.isEmpty)
    assert(req1.maxForwardsRemoved == req1)

    val req2 = req1.setMaxForwards(5)
    assert(req2.hasMaxForwards)
    assert(req2.maxForwards == 5)
    assert(req2.maxForwardsOption.contains(5))
    assert(req2.maxForwardsRemoved == req1)
    assert(req2.getHeaderValue("Max-Forwards").contains("5"))
  }

  it should "create request with Pragma header" in {
    val req1 = Get("/")
    assert(!req1.hasPragma)
    assert(req1.pragma.isEmpty)
    assert(req1.pragmaOption.isEmpty)
    assert(req1.pragmaRemoved == req1)

    val req2 = req1.setPragma("no-cache")
    assert(req2.hasPragma)
    assert(req2.pragma == Seq[PragmaDirective]("no-cache"))
    assert(req2.pragmaOption.contains(Seq[PragmaDirective]("no-cache")))
    assert(req2.pragmaRemoved == req1)
    assert(req2.getHeaderValue("Pragma").contains("no-cache"))
  }

  it should "create request with Prefer header" in {
    val req1 = Get("/")
    assert(!req1.hasPrefer)
    assert(req1.prefer.isEmpty)
    assert(req1.preferOption.isEmpty)
    assert(req1.preferRemoved == req1)

    val req2 = req1.setPrefer("lenient", "respond-async", "wait=10")
    assert(req2.hasPrefer)
    assert(req2.prefer == Seq[Preference]("lenient", "respond-async", "wait=10"))
    assert(req2.preferOption.contains(Seq[Preference]("lenient", "respond-async", "wait=10")))
    assert(req2.preferRemoved == req1)
    assert(req2.getHeaderValue("Prefer").contains("lenient, respond-async, wait=10"))
  }

  it should "create response with Preference-Applied header" in {
    val res1 = Ok()
    assert(!res1.hasPreferenceApplied)
    assert(res1.preferenceApplied.isEmpty)
    assert(res1.preferenceAppliedOption.isEmpty)
    assert(res1.preferenceAppliedRemoved == res1)

    val res2 = res1.setPreferenceApplied("lenient", "respond-async", "wait=10")
    assert(res2.hasPreferenceApplied)
    assert(res2.preferenceApplied == Seq[Preference]("lenient", "respond-async", "wait=10"))
    assert(res2.preferenceAppliedOption.contains(Seq[Preference]("lenient", "respond-async", "wait=10")))
    assert(res2.preferenceAppliedRemoved == res1)
    assert(res2.getHeaderValue("Preference-Applied").contains("lenient, respond-async, wait=10"))
  }

  it should "create request with Range header" in {
    val req1 = Get("/")
    assert(!req1.hasRange)
    assertThrows[HeaderNotFound](req1.range)
    assert(req1.rangeOption.isEmpty)
    assert(req1.rangeRemoved == req1)

    val req2 = req1.setRange("bytes=1024-8191")
    assert(req2.hasRange)
    assert(req2.range == ByteRange(Slice(1024, Some(8191))))
    assert(req2.rangeOption.contains(ByteRange(Slice(1024, Some(8191)))))
    assert(req2.rangeRemoved == req1)
    assert(req2.getHeaderValue("Range").contains("bytes=1024-8191"))
  }

  it should "create request with Referer header" in {
    val req1 = Get("/")
    assert(!req1.hasReferer)
    assertThrows[HeaderNotFound](req1.referer)
    assert(req1.refererOption.isEmpty)
    assert(req1.refererRemoved == req1)

    val req2 = req1.setReferer("/path/to/file.html")
    assert(req2.hasReferer)
    assert(req2.referer == Uri("/path/to/file.html"))
    assert(req2.refererOption.contains(Uri("/path/to/file.html")))
    assert(req2.refererRemoved == req1)
    assert(req2.getHeaderValue("Referer").contains("/path/to/file.html"))
  }

  it should "create response with Retry-After header" in {
    val res1 = Ok()
    assert(!res1.hasRetryAfter)
    assertThrows[HeaderNotFound](res1.retryAfter)
    assert(res1.retryAfterOption.isEmpty)
    assert(res1.retryAfterRemoved == res1)

    val res2 = res1.setRetryAfter(Instant("2020-12-25T07:34:16Z"))
    assert(res2.hasRetryAfter)
    assert(res2.retryAfter == Instant("2020-12-25T07:34:16Z"))
    assert(res2.retryAfterOption.contains(Instant("2020-12-25T07:34:16Z")))
    assert(res2.retryAfterRemoved == res1)
    assert(res2.getHeaderValue("Retry-After").contains("Fri, 25 Dec 2020 07:34:16 GMT"))
  }

  it should "create response with Server header" in {
    val res1 = Ok()
    assert(!res1.hasServer)
    assert(res1.server.isEmpty)
    assert(res1.serverOption.isEmpty)
    assert(res1.serverRemoved == res1)

    val res2 = res1.setServer("Scamper/20.0.0", "OpenJDK/1.8.275")
    assert(res2.hasServer)
    assert(res2.server == Seq(ProductType("Scamper", Some("20.0.0")), ProductType("OpenJDK", Some("1.8.275"))))
    assert(res2.serverOption.contains(Seq(ProductType("Scamper", Some("20.0.0")), ProductType("OpenJDK", Some("1.8.275")))))
    assert(res2.serverRemoved == res1)
    assert(res2.getHeaderValue("Server").contains("Scamper/20.0.0 OpenJDK/1.8.275"))
  }

  it should "create request with TE header" in {
    val req1 = Get("/")
    assert(!req1.hasTE)
    assert(req1.te.isEmpty)
    assert(req1.teOption.isEmpty)
    assert(req1.teRemoved == req1)

    val req2 = req1.setTE("gzip; q=0.9", "deflate; q=0.1")
    assert(req2.hasTE)
    assert(req2.te == Seq[TransferCodingRange]("gzip; q=0.9", "deflate; q=0.1"))
    assert(req2.teOption.contains(Seq[TransferCodingRange]("gzip; q=0.9", "deflate; q=0.1")))
    assert(req2.teRemoved == req1)
    assert(req2.getHeaderValue("TE").contains("gzip; q=0.9, deflate; q=0.1"))
  }

  it should "create message with Trailer header" in {
    val req1 = Get("/")
    assert(!req1.hasTrailer)
    assert(req1.trailer.isEmpty)
    assert(req1.trailerOption.isEmpty)
    assert(req1.trailerRemoved == req1)

    val req2 = req1.setTrailer("Content-Length", "Content-Language")
    assert(req2.hasTrailer)
    assert(req2.trailer == Seq("Content-Length", "Content-Language"))
    assert(req2.trailerOption.contains(Seq("Content-Length", "Content-Language")))
    assert(req2.trailerRemoved == req1)
    assert(req2.getHeaderValue("Trailer").contains("Content-Length, Content-Language"))

    val res1 = Ok()
    assert(!res1.hasTrailer)
    assert(res1.trailer.isEmpty)
    assert(res1.trailerOption.isEmpty)
    assert(res1.trailerRemoved == res1)

    val res2 = res1.setTrailer("Content-Length", "Content-Language")
    assert(res2.hasTrailer)
    assert(res2.trailer == Seq("Content-Length", "Content-Language"))
    assert(res2.trailerOption.contains(Seq("Content-Length", "Content-Language")))
    assert(res2.trailerRemoved == res1)
    assert(res2.getHeaderValue("Trailer").contains("Content-Length, Content-Language"))
  }

  it should "create message with Transfer-Encoding header" in {
    val req1 = Get("/")
    assert(!req1.hasTransferEncoding)
    assert(req1.transferEncoding.isEmpty)
    assert(req1.transferEncodingOption.isEmpty)
    assert(req1.transferEncodingRemoved == req1)

    val req2 = req1.setTransferEncoding("deflate", "gzip")
    assert(req2.hasTransferEncoding)
    assert(req2.transferEncoding == Seq[TransferCoding]("deflate", "gzip"))
    assert(req2.transferEncodingOption.contains(Seq[TransferCoding]("deflate", "gzip")))
    assert(req2.transferEncodingRemoved == req1)
    assert(req2.getHeaderValue("Transfer-Encoding").contains("deflate, gzip"))

    val res1 = Ok()
    assert(!res1.hasTransferEncoding)
    assert(res1.transferEncoding.isEmpty)
    assert(res1.transferEncodingOption.isEmpty)
    assert(res1.transferEncodingRemoved == res1)

    val res2 = res1.setTransferEncoding("deflate", "gzip")
    assert(res2.hasTransferEncoding)
    assert(res2.transferEncoding == Seq[TransferCoding]("deflate", "gzip"))
    assert(res2.transferEncodingOption.contains(Seq[TransferCoding]("deflate", "gzip")))
    assert(res2.transferEncodingRemoved == res1)
    assert(res2.getHeaderValue("Transfer-Encoding").contains("deflate, gzip"))
  }

  it should "create message with Upgrade header" in {
    val req1 = Get("/")
    assert(!req1.hasUpgrade)
    assert(req1.upgrade.isEmpty)
    assert(req1.upgradeOption.isEmpty)
    assert(req1.upgradeRemoved == req1)

    val req2 = req1.setUpgrade("websocket", "h2c")
    assert(req2.hasUpgrade)
    assert(req2.upgrade == Seq[Protocol]("websocket", "h2c"))
    assert(req2.upgradeOption.contains(Seq[Protocol]("websocket", "h2c")))
    assert(req2.upgradeRemoved == req1)
    assert(req2.getHeaderValue("Upgrade").contains("websocket, h2c"))

    val res1 = Ok()
    assert(!res1.hasUpgrade)
    assert(res1.upgrade.isEmpty)
    assert(res1.upgradeOption.isEmpty)
    assert(res1.upgradeRemoved == res1)

    val res2 = res1.setUpgrade("websocket", "h2c")
    assert(res2.hasUpgrade)
    assert(res2.upgrade == Seq[Protocol]("websocket", "h2c"))
    assert(res2.upgradeOption.contains(Seq[Protocol]("websocket", "h2c")))
    assert(res2.upgradeRemoved == res1)
    assert(res2.getHeaderValue("Upgrade").contains("websocket, h2c"))
  }

  it should "create request with User-Agent header" in {
    val req1 = Get("/")
    assert(!req1.hasUserAgent)
    assert(req1.userAgent.isEmpty)
    assert(req1.userAgentOption.isEmpty)
    assert(req1.userAgentRemoved == req1)

    val req2 = req1.setUserAgent("Scamper/20.0.0", "OpenJDK/1.8.275")
    assert(req2.hasUserAgent)
    assert(req2.userAgent == Seq(ProductType("Scamper", Some("20.0.0")), ProductType("OpenJDK", Some("1.8.275"))))
    assert(req2.userAgentOption.contains(Seq(ProductType("Scamper", Some("20.0.0")), ProductType("OpenJDK", Some("1.8.275")))))
    assert(req2.userAgentRemoved == req1)
    assert(req2.getHeaderValue("User-Agent").contains("Scamper/20.0.0 OpenJDK/1.8.275"))
  }

  it should "create response with Vary header" in {
    val res1 = Ok()
    assert(!res1.hasVary)
    assert(res1.vary.isEmpty)
    assert(res1.varyOption.isEmpty)
    assert(res1.varyRemoved == res1)

    val res2 = res1.setVary("Accept-Encoding", "Accept-Language")
    assert(res2.hasVary)
    assert(res2.vary == Seq("Accept-Encoding", "Accept-Language"))
    assert(res2.varyOption.contains(Seq("Accept-Encoding", "Accept-Language")))
    assert(res2.varyRemoved == res1)
    assert(res2.getHeaderValue("Vary").contains("Accept-Encoding, Accept-Language"))
  }

  it should "create message with Via header" in {
    val req1 = Get("/")
    assert(!req1.hasVia)
    assert(req1.via.isEmpty)
    assert(req1.viaOption.isEmpty)
    assert(req1.viaRemoved == req1)

    val req2 = req1.setVia("1.0 abc", "1.1 xyz")
    assert(req2.hasVia)
    assert(req2.via == Seq[ViaType]("1.0 abc", "1.1 xyz"))
    assert(req2.viaOption.contains(Seq[ViaType]("1.0 abc", "1.1 xyz")))
    assert(req2.viaRemoved == req1)
    assert(req2.getHeaderValue("Via").contains("1.0 abc, 1.1 xyz"))

    val res1 = Ok()
    assert(!res1.hasVia)
    assert(res1.via.isEmpty)
    assert(res1.viaOption.isEmpty)
    assert(res1.viaRemoved == res1)

    val res2 = res1.setVia("1.0 abc", "1.1 xyz")
    assert(res2.hasVia)
    assert(res2.via == Seq[ViaType]("1.0 abc", "1.1 xyz"))
    assert(res2.viaOption.contains(Seq[ViaType]("1.0 abc", "1.1 xyz")))
    assert(res2.viaRemoved == res1)
    assert(res2.getHeaderValue("Via").contains("1.0 abc, 1.1 xyz"))
  }

  it should "create message with Warning header" in {
    val req1 = Get("/")
    assert(!req1.hasWarning)
    assert(req1.warning.isEmpty)
    assert(req1.warningOption.isEmpty)
    assert(req1.warningRemoved == req1)

    val req2 = req1.setWarning("110 - \"response is Stale\"", "113 - \"Heuristic Expiration\"")
    assert(req2.hasWarning)
    assert(req2.warning == Seq(WarningType(110, "-", "response is Stale"), WarningType(113, "-", "Heuristic Expiration")))
    assert(req2.warningOption.contains(Seq(WarningType(110, "-", "response is Stale"), WarningType(113, "-", "Heuristic Expiration"))))
    assert(req2.warningRemoved == req1)
    assert(req2.getHeaderValue("Warning").contains("110 - \"response is Stale\", 113 - \"Heuristic Expiration\""))

    val res1 = Ok()
    assert(!res1.hasWarning)
    assert(res1.warning.isEmpty)
    assert(res1.warningOption.isEmpty)
    assert(res1.warningRemoved == res1)

    val res2 = res1.setWarning("110 - \"Response is Stale\"", "113 - \"Heuristic Expiration\"")
    assert(res2.hasWarning)
    assert(res2.warning == Seq(WarningType(110, "-", "Response is Stale"), WarningType(113, "-", "Heuristic Expiration")))
    assert(res2.warningOption.contains(Seq(WarningType(110, "-", "Response is Stale"), WarningType(113, "-", "Heuristic Expiration"))))
    assert(res2.warningRemoved == res1)
    assert(res2.getHeaderValue("Warning").contains("110 - \"Response is Stale\", 113 - \"Heuristic Expiration\""))
  }
