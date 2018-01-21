package scamper

import org.scalatest.FlatSpec
import scamper.Implicits._

class HttpResponseSpec extends FlatSpec {
  "An HttpResponse" should "be created" in {
    val response = HttpResponse(200).withHeaders("Transfer-Encoding" -> "chunked")
    assert(response.status == Status.Ok)
    assert(response.isChunked)
  }
}

