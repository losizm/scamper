package scamper.util

import org.scalatest.FlatSpec
import scamper.Statuses
import scamper.util.ResponseFilters._

class ResponseFilterSpec extends FlatSpec with Statuses {
  "ResponseFilter" should "filter response based on status code" in {
    assert { Informational(Continue()) }
    assert { Successful(Created()) }
    assert { Redirection(MovedPermanently()) }
    assert { ClientError(Forbidden()) }
    assert { ServerError(NotImplemented()) }

    Continue() match {
      case ClientError(res)   => throw new Exception("Not Client Error")
      case Redirection(res)   => throw new Exception("Not Redirection")
      case Successful(res)    => throw new Exception("Not Successful")
      case ServerError(res)   => throw new Exception("Not Server Error")
      case Informational(res) => res
    }

    Ok() match {
      case ClientError(res)   => throw new Exception("Not Client Error")
      case Informational(res) => throw new Exception("Not Informational")
      case Redirection(res)   => throw new Exception("Not Redirection")
      case ServerError(res)   => throw new Exception("Not Server Error")
      case Successful(res)    => res
    }

    SeeOther() match {
      case ClientError(res)   => throw new Exception("Not Client Error")
      case Informational(res) => throw new Exception("Not Informational")
      case Successful(res)    => throw new Exception("Not Successful")
      case ServerError(res)   => throw new Exception("Not Server Error")
      case Redirection(res)   => res
    }

    BadRequest() match {
      case Informational(res) => throw new Exception("Not Informational")
      case Redirection(res)   => throw new Exception("Not Redirection")
      case ServerError(res)   => throw new Exception("Not Server Error")
      case Successful(res)    => throw new Exception("Not Successful")
      case ClientError(res)   => res
    }

    InternalServerError() match {
      case ClientError(res)   => throw new Exception("Not Client Error")
      case Informational(res) => throw new Exception("Not Informational")
      case Redirection(res)   => throw new Exception("Not Redirection")
      case Successful(res)    => throw new Exception("Not Successful")
      case ServerError(res)   => res
    }
  }
}

