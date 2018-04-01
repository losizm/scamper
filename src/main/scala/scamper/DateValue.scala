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

import java.time.{ OffsetDateTime, ZoneOffset }
import java.time.format.DateTimeFormatter.{ RFC_1123_DATE_TIME => DateFormatter }

private object DateValue {
  private val z = ZoneOffset.of("Z")

  def format(value: OffsetDateTime): String =
    DateFormatter.format(value.atZoneSameInstant(z))

  def parse(value: String): OffsetDateTime =
    OffsetDateTime.parse(value, DateFormatter)
}

