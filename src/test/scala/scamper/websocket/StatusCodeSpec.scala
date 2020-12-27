/*
 * Copyright 2020 Carlos Conyers
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
package scamper.websocket

import StatusCode.Registry._

class StatusCodeSpec extends org.scalatest.flatspec.AnyFlatSpec {
  it should "check registered status codes" in {
    assert(StatusCode(1000) == NormalClosure)
    assert(StatusCode.get(1000).contains(NormalClosure))
    assert(NormalClosure.value == 1000)
    assert(NormalClosure.meaning == "Normal Closure")
    assert(!NormalClosure.isReserved)
    assert(BigInt(1, NormalClosure.toData).intValue == 1000)

    assert(StatusCode(1001) == GoingAway)
    assert(StatusCode.get(1001).contains(GoingAway))
    assert(GoingAway.value == 1001)
    assert(GoingAway.meaning == "Going Away")
    assert(!GoingAway.isReserved)
    assert(BigInt(1, GoingAway.toData).intValue == 1001)

    assert(StatusCode(1002) == ProtocolError)
    assert(StatusCode.get(1002).contains(ProtocolError))
    assert(ProtocolError.value == 1002)
    assert(ProtocolError.meaning == "Protocol Error")
    assert(!ProtocolError.isReserved)
    assert(BigInt(1, ProtocolError.toData).intValue == 1002)

    assert(StatusCode(1003) == UnsupportedData)
    assert(StatusCode.get(1003).contains(UnsupportedData))
    assert(UnsupportedData.value == 1003)
    assert(UnsupportedData.meaning == "Unsupported Data")
    assert(!UnsupportedData.isReserved)
    assert(BigInt(1, UnsupportedData.toData).intValue == 1003)

    assert(StatusCode(1004) == Reserved)
    assert(StatusCode.get(1004).contains(Reserved))
    assert(Reserved.value == 1004)
    assert(Reserved.meaning == "Reserved")
    assert(Reserved.isReserved)
    assert(BigInt(1, Reserved.toData).intValue == 1004)

    assert(StatusCode(1005) == NoStatusReceived)
    assert(StatusCode.get(1005).contains(NoStatusReceived))
    assert(NoStatusReceived.value == 1005)
    assert(NoStatusReceived.meaning == "No Status Received")
    assert(NoStatusReceived.isReserved)
    assert(BigInt(1, NoStatusReceived.toData).intValue == 1005)

    assert(StatusCode(1006) == AbnormalClosure)
    assert(StatusCode.get(1006).contains(AbnormalClosure))
    assert(AbnormalClosure.value == 1006)
    assert(AbnormalClosure.meaning == "Abnormal Closure")
    assert(AbnormalClosure.isReserved)
    assert(BigInt(1, AbnormalClosure.toData).intValue == 1006)

    assert(StatusCode(1007) == InvalidFramePayload)
    assert(StatusCode.get(1007).contains(InvalidFramePayload))
    assert(InvalidFramePayload.value == 1007)
    assert(InvalidFramePayload.meaning == "Invalid Frame Payload Data")
    assert(!InvalidFramePayload.isReserved)
    assert(BigInt(1, InvalidFramePayload.toData).intValue == 1007)

    assert(StatusCode(1008) == PolicyViolation)
    assert(StatusCode.get(1008).contains(PolicyViolation))
    assert(PolicyViolation.value == 1008)
    assert(PolicyViolation.meaning == "Policy Violation")
    assert(!PolicyViolation.isReserved)
    assert(BigInt(1, PolicyViolation.toData).intValue == 1008)

    assert(StatusCode(1009) == MessageTooBig)
    assert(StatusCode.get(1009).contains(MessageTooBig))
    assert(MessageTooBig.value == 1009)
    assert(MessageTooBig.meaning == "Message Too Big")
    assert(!MessageTooBig.isReserved)
    assert(BigInt(1, MessageTooBig.toData).intValue == 1009)

    assert(StatusCode(1010) == MandatoryExtension)
    assert(StatusCode.get(1010).contains(MandatoryExtension))
    assert(MandatoryExtension.value == 1010)
    assert(MandatoryExtension.meaning == "Mandatory Extension")
    assert(!MandatoryExtension.isReserved)
    assert(BigInt(1, MandatoryExtension.toData).intValue == 1010)

    assert(StatusCode(1011) == InternalError)
    assert(StatusCode.get(1011).contains(InternalError))
    assert(InternalError.value == 1011)
    assert(InternalError.meaning == "Internal Server Error")
    assert(!InternalError.isReserved)
    assert(BigInt(1, InternalError.toData).intValue == 1011)

    assert(StatusCode(1015) == TlsHandshake)
    assert(StatusCode.get(1015).contains(TlsHandshake))
    assert(TlsHandshake.value == 1015)
    assert(TlsHandshake.meaning == "TLS Handshake")
    assert(TlsHandshake.isReserved)
    assert(BigInt(1, TlsHandshake.toData).intValue == 1015)
  }

  it should "not create invalid status codes" in {
    assertThrows[NoSuchElementException](StatusCode(-1))
    assertThrows[NoSuchElementException](StatusCode(999))
    assertThrows[NoSuchElementException](StatusCode(1012))
    assertThrows[NoSuchElementException](StatusCode(1013))
    assertThrows[NoSuchElementException](StatusCode(1014))
    assertThrows[NoSuchElementException](StatusCode(1016))

    assert(StatusCode.get(-1).isEmpty)
    assert(StatusCode.get(999).isEmpty)
    assert(StatusCode.get(1012).isEmpty)
    assert(StatusCode.get(1013).isEmpty)
    assert(StatusCode.get(1014).isEmpty)
    assert(StatusCode.get(1016).isEmpty)
  }
}
