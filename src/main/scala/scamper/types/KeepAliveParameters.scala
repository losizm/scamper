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
package scamper.types

/**
 * Standardized type for Keep-Alive header value.
 *
 * @see [[scamper.headers.KeepAlive]]
 */
trait KeepAliveParameters {
  /** Gets connection timeout (in seconds). */
  def timeout: Int

  /** Gets max requests. */
  def max: Int

  /** Returns formatted Keep-Alive parameters. */
  override lazy val toString: String = s"timeout=$timeout, max=$max"
}

/** Provides factory methods for `KeepAliveParameters`. */
object KeepAliveParameters {
  /** Creates KeepLiveParameters with supplied timeout and max. */
  def apply(timeout: Int, max: Int): KeepAliveParameters =
    KeepAliveParametersImpl(timeout, max)

  /** Creates KeepLiveParameters from formatted parameters. */
  def parse(params: String): KeepAliveParameters = {
    val keepAlive = params.split(",")
      .map { _.split("=").map(_.trim) }
      .map {
        case Array("timeout", timeout) => "timeout" -> timeout.toInt
        case Array("max"    , max    ) => "max"     -> max.toInt
        case _                         => throw new IllegalArgumentException(s"Invalid Keep-Alive parameters: $params")
      }.toMap

    KeepAliveParametersImpl(
      keepAlive.getOrElse("timeout", throw new IllegalArgumentException("Missing Keep-Alive parameter: timeout")),
      keepAlive.getOrElse("max"    , throw new IllegalArgumentException("Missing Keep-Alive parameter: max"))
    )
  }

  /** Destructures KeepLiveParameters to `timeout` and `max`. */
  def unapply(keepAlive: KeepAliveParameters): Option[(Int, Int)] =
    Some(keepAlive.timeout, keepAlive.max)
}

private case class KeepAliveParametersImpl(timeout: Int, max: Int) extends KeepAliveParameters
