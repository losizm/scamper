/*
 * Copyright 2019 Carlos Conyers
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

private class CaseInsensitiveKeyMap[V](params: Seq[(String, V)]) extends Map[String, V] {
  def get(key: String): Option[V] =
    params.collectFirst { case (k, value) if k.equalsIgnoreCase(key) => value }

  def iterator: Iterator[(String, V)] =
    params.groupBy(_._1.toLowerCase)
      .map { case (key, values) => key -> values.head._2 }
      .toIterator

  def -(key: String): Map[String, V] =
    new CaseInsensitiveKeyMap(params.filterNot { case (k, _) => k.equalsIgnoreCase(key) })

  def +[V1 >: V](pair: (String, V1)): Map[String, V1] =
    new CaseInsensitiveKeyMap(params :+ pair)

  override def empty: Map[String, V] =
    new CaseInsensitiveKeyMap(Nil)
}
