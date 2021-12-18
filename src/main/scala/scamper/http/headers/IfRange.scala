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

import java.time.Instant

import scala.util.Try

import scamper.http.types.EntityTag

/** Provides standardized access to If-Range header. */
implicit class IfRange(request: HttpRequest) extends AnyVal:
  /** Tests for If-Range header. */
  def hasIfRange: Boolean =
    request.hasHeader("If-Range")

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
      Try { Left(EntityTag.parse(header.value)) }
        .orElse { Try(Right(header.instantValue)) }
        .get
    }

  /** Creates new request with If-Range header set to supplied value. */
  def setIfRange(value: Either[EntityTag, Instant]): HttpRequest =
    value.fold(setIfRange, setIfRange)

  /** Creates new request with If-Range header set to supplied value. */
  def setIfRange(value: EntityTag): HttpRequest =
    request.putHeaders(Header("If-Range", value.toString))

  /** Creates new request with If-Range header set to supplied value. */
  def setIfRange(value: Instant): HttpRequest =
    request.putHeaders(Header("If-Range", value))

  /** Creates new request with If-Range header removed. */
  def removeIfRange: HttpRequest =
    request.removeHeaders("If-Range")
