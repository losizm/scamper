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
package multipart

import java.io.File

import scala.collection.mutable.ListBuffer

import scamper.http.types.MediaType

/** Defines multipart builder. */
class MultipartBuilder():
  private val parts = ListBuffer[Part]()

  /**
   * Creates multipart builder and initializes it with supplied multipart.
   *
   * @param multipart initializing multipart
   */
  def this(multipart: Multipart) =
    this()
    multipart.parts.foreach(add)

  /**
   * Adds part.
   *
   * @param part new part
   *
   * @return this builder
   *
   * @note Same as [[add add(Part)]].
   */
  def +=(part: Part): this.type =
    add(part)

  /**
   * Adds part.
   *
   * @param part new part
   *
   * @return this builder
   */
  def add(part: Part): this.type =
    parts += part
    this


  /**
   * Adds part.
   *
   * @param name sets name
   * @param content sets content
   * @param contentType sets content type, which defaults based on content
   *   * if content is string, then defaults to `text/plain`
   *   * if content is byte array, then defaults to `application/octet-stream`
   *   * if content is file, then defaults based on content file name or
   *     `fileName` if provided
   * @param fileName sets file name, which defaults to `None`
   * @param params sets parameters, which defaults to `Map.empty`
   *
   * @return this builder
   */
  def add(
    name: String,
    content: PartContent,
    contentType: Option[MediaType] = None,
    fileName: Option[String] = None,
    params: Map[String, String] = Map.empty[String, String]
  ): this.type =
    parts += Part(name, content, contentType, fileName, params)
    this

  /**
   * Resets builder by removing all parts.
   *
   * @return this builder
   */
  def reset(): this.type =
    parts.clear()
    this

  /**
   * Creates multipart from added parts.
   *
   * @return multipart
   */
  def toMultipart(): Multipart =
    Multipart(parts.toSeq)
