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
package types

/**
 * Standardized type for Keep-Alive header value.
 *
 * @see [[scamper.http.headers.KeepAlive]]
 */
trait KeepAliveParameters:
  /** Gets idle timeout seconds. */
  def timeout: Int

  /** Gets max requests. */
  def max: Int

  /** Returns formatted parameters. */
  override lazy val toString: String = s"timeout=$timeout, max=$max"

/** Provides factory for `KeepAliveParameters`. */
object KeepAliveParameters:
  /** Creates parameters with supplied timeout and max. */
  def apply(timeout: Int, max: Int): KeepAliveParameters =
    KeepAliveParametersImpl(timeout, max)

  /** Parse formatted parameters. */
  def parse(params: String): KeepAliveParameters =
    val keepAlive = params.split(",")
      .map { _.split("=").map(_.trim) }
      .map {
        case Array("timeout", timeout) => "timeout" -> timeout.toInt
        case Array("max"    , max    ) => "max"     -> max.toInt
        case _                         => throw IllegalArgumentException(s"Invalid Keep-Alive parameters: $params")
      }.toMap

    KeepAliveParametersImpl(
      keepAlive.getOrElse("timeout", throw IllegalArgumentException("Missing Keep-Alive parameter: timeout")),
      keepAlive.getOrElse("max"    , throw IllegalArgumentException("Missing Keep-Alive parameter: max"))
    )

private case class KeepAliveParametersImpl(timeout: Int, max: Int) extends KeepAliveParameters
