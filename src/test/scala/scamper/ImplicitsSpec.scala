package scamper

import java.net.{ URI, URL }
import org.scalatest.FlatSpec
import ImplicitConverters._
import ImplicitExtensions._

class ImplicitsSpec extends FlatSpec {
  val uri = new URI("/index.html")
  val url = new URL("http://localhost:8080/index.html")

  "URI" should "be created with new path" in {
    assert(uri.withPath("/home.html") == new URI("/home.html"))
  }

  it should "be created with new query" in {
    val newURI = new URI("/index.html?name=guest")
    assert(uri.withQuery("name=guest") == newURI)
    assert(uri.withQueryParams("name" -> "guest") == newURI)
  }

  it should "be converted to URL" in {
    val newURL = uri.toURL("http", "localhost:8080")
    assert(url == newURL)
  }

  "URL" should "be created with new path" in {
    assert(url.withPath("home.html") == new URL("http://localhost:8080/home.html"))
  }

  it should "be created with new query" in {
    val newURL = new URL("http://localhost:8080/index.html?name=guest")
    assert(url.withQuery("name=guest") == newURL)
    assert(url.withQueryParams("name" -> "guest") == newURL)
  }
}

