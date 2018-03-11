package scamper.types

import org.scalatest.FlatSpec

class ProductTypeSpec extends FlatSpec {
  "ProductType" should "be created" in {
    var product = ProductType("CERN-LineMode/2.15")
    assert(product.name == "CERN-LineMode")
    assert(product.version.contains("2.15"))
    assert(product.toString == "CERN-LineMode/2.15")
    assert(product == ProductType("CERN-LineMode", Some("2.15")))

    product = ProductType("libwww/2.17b3")
    assert(product.name == "libwww")
    assert(product.version.contains("2.17b3"))
    assert(product.toString == "libwww/2.17b3")
    assert(product == ProductType("libwww", Some("2.17b3")))

    product = ProductType("libwww")
    assert(product.name == "libwww")
    assert(product.version == None)
    assert(product.toString == "libwww")
    assert(product == ProductType("libwww", None))
  }

  it should "be destructured" in {
    ProductType("CERN-LineMode/2.15") match {
      case ProductType(name, Some(version)) => assert(name == "CERN-LineMode" && version == "2.15")
    }

    ProductType("libwww/2.17b3") match {
      case ProductType(name, Some(version)) => assert(name == "libwww" && version == "2.17b3")
    }

    ProductType("libwww") match {
      case ProductType(name, None) => assert(name == "libwww")
    }
  }

  it should "not be created with malformed value" in {
    assertThrows[IllegalArgumentException](ProductType("CERN-LineMode / 2.15"))
    assertThrows[IllegalArgumentException](ProductType("CERN-LineMode / 2.15,2"))
    assertThrows[IllegalArgumentException](ProductType("CERN-LineMode/"))
  }
}

