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

class KeepAliveParametersSpec extends org.scalatest.flatspec.AnyFlatSpec {
  it should "parse String to KeepAliveParameters" in {
    var params = KeepAliveParameters.parse("timeout=5, max=10")
    assert { params.timeout == 5 }
    assert { params.max == 10 }
    assert { params.toString == "timeout=5, max=10" }

    params = KeepAliveParameters.parse("max=100, timeout=60")
    assert { params.timeout == 60 }
    assert { params.max == 100 }
    assert { params.toString == "timeout=60, max=100" }
  }

  it should "destructure KeepAliveParameters" in {
    KeepAliveParameters(60, 100) match {
      case KeepAliveParameters(timeout, max) =>
        assert { timeout == 60 }
        assert { max == 100 }
    }
  }

  it should "should not parse invalid parameters" in {
    assertThrows[IllegalArgumentException](KeepAliveParameters.parse("timeout=5; max=10"))
    assertThrows[IllegalArgumentException](KeepAliveParameters.parse("timeout=5"))
    assertThrows[IllegalArgumentException](KeepAliveParameters.parse("max=10"))
  }
}
