/*
 * Copyright 2024 Carlos Conyers
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

import java.util.Properties

import scala.util.Try

private object Product:
  private val product = getProperties()

  val name: String = product.getProperty("product.name")

  val version: String = product.getProperty("product.version")

  override val toString: String = s"$name/$version"

  private def getProperties(): Properties =
    val in = classOf[Product.type].getResourceAsStream("/product.properties")
    try
      val props = Properties()
      props.load(in)
      props
    finally
      Try(in.close())
