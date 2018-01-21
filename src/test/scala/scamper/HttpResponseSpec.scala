package scamper

import org.scalatest.FlatSpec
import HttpResponses._
import Implicits._

class HttpResponseSpec extends FlatSpec {
  "An HttpResponse" should "be created" in {
    val response = Ok.withHeaders("Transfer-Encoding" -> "chunked")
    assert(response.status == Ok.status)
    assert(response.isChunked)
  }
}

