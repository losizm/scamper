package scamper

import java.net.{ URI, URL }
import org.scalatest.FlatSpec
import Implicits._

class ImplicitsSpec extends FlatSpec {
  val uri = new URI("/index.html")
  val url = new URL("http://localhost:8080/index.html")

  "A URI" should "be created with new path" in {
    assert(uri.withPath("/home.html") == new URI("/home.html"))
  }

  it should "be created with new query" in {
    val newURI = new URI("/index.html?name=guest")
    assert(uri.withQuery("name=guest") == newURI)
    assert(uri.withQuery("name" -> "guest") == newURI)
  }

  "A URL" should "be created with new path" in {
    assert(url.withPath("home.html") == new URL("http://localhost:8080/home.html"))
  }

  it should "be created with new query" in {
    val newURL = new URL("http://localhost:8080/index.html?name=guest")
    assert(url.withQuery("name=guest") == newURL)
    assert(url.withQuery("name" -> "guest") == newURL)
  }
}

