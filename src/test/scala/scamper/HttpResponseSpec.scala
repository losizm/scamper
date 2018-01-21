package scamper

import org.scalatest.FlatSpec
import HttpResponses._
import Implicits._

class HttpResponseSpec extends FlatSpec {
  "An HttpResponse" should "be created" in {
    val response = Ok.withChunked
    assert(response.status == Ok.status)
    assert(response.isChunked)
  }
}

