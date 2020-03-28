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

class ProductTypeSpec extends org.scalatest.flatspec.AnyFlatSpec {
  "ProductType" should "be created" in {
    var product = ProductType.parse("CERN-LineMode/2.15")
    assert(product.name == "CERN-LineMode")
    assert(product.version.contains("2.15"))
    assert(product.toString == "CERN-LineMode/2.15")
    assert(product == ProductType("CERN-LineMode", Some("2.15")))

    product = ProductType.parse("libwww/2.17b3")
    assert(product.name == "libwww")
    assert(product.version.contains("2.17b3"))
    assert(product.toString == "libwww/2.17b3")
    assert(product == ProductType("libwww", Some("2.17b3")))

    product = ProductType.parse("libwww")
    assert(product.name == "libwww")
    assert(product.version == None)
    assert(product.toString == "libwww")
    assert(product == ProductType("libwww", None))
  }

  it should "be destructured" in {
    ProductType.parse("CERN-LineMode/2.15") match {
      case ProductType(name, Some(version)) => assert(name == "CERN-LineMode" && version == "2.15")
    }

    ProductType.parse("libwww/2.17b3") match {
      case ProductType(name, Some(version)) => assert(name == "libwww" && version == "2.17b3")
    }

    ProductType.parse("libwww") match {
      case ProductType(name, None) => assert(name == "libwww")
    }
  }

  it should "not be created with malformed value" in {
    assertThrows[IllegalArgumentException](ProductType.parse("CERN-LineMode / 2.15"))
    assertThrows[IllegalArgumentException](ProductType.parse("CERN-LineMode / 2.15,2"))
    assertThrows[IllegalArgumentException](ProductType.parse("CERN-LineMode/"))
  }
}
