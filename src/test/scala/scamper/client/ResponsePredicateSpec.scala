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
package scamper.client

import org.scalatest.FlatSpec

import scamper.ResponseStatus.Registry._
import ResponsePredicate._

class ResponsePredicateSpec extends FlatSpec {
  it should "test response based on status code" in {
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
