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
package scamper

import ResponseStatus.Registry._

class ResponseStatusSpec extends org.scalatest.flatspec.AnyFlatSpec {
  it should "get registered status" in {
    assert { ResponseStatus(100) == Continue }
    assert { ResponseStatus.get(100).contains(Continue) }

    assert { ResponseStatus(200) == Ok }
    assert { ResponseStatus.get(200).contains(Ok) }

    assert { ResponseStatus(300) == MultipleChoices }
    assert { ResponseStatus.get(300).contains(MultipleChoices) }

    assert { ResponseStatus(400) == BadRequest }
    assert { ResponseStatus.get(400).contains(BadRequest) }

    assert { ResponseStatus(500) == InternalServerError }
    assert { ResponseStatus.get(500).contains(InternalServerError) }
  }

  it should "not get registered status" in {
    assertThrows[NoSuchElementException] { ResponseStatus(600) }
    assert { ResponseStatus.get(600).isEmpty }
  }
}
