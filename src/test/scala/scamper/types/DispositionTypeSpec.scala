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

class DispositionTypeSpec extends org.scalatest.flatspec.AnyFlatSpec {
  "DispositionType" should "be created without parameters" in {
    var disposition = DispositionType.parse("INLINE")
    assert(disposition.name == "inline")
    assert(disposition.isInline)
    assert(!disposition.isAttachment)
    assert(disposition.params.isEmpty)
    assert(disposition.toString == "inline")

    disposition = DispositionType.parse("Attachment")
    assert(disposition.name == "attachment")
    assert(disposition.isAttachment)
    assert(!disposition.isInline)
    assert(disposition.params.isEmpty)
    assert(disposition.toString == "attachment")
  }

  it should "be created with parameters" in {
    var disposition = DispositionType.parse("Inline; Filename=\"a text file.txt\"")
    assert(disposition.name == "inline")
    assert(disposition.isInline)
    assert(!disposition.isAttachment)
    assert(disposition.params.size == 1)
    assert(disposition.params.get("filename").contains("a text file.txt"))
    assert(disposition.toString == "inline; filename=\"a text file.txt\"")

    disposition = DispositionType.parse("ATTACHMENT; FILENAME*=\"example.txt\"")
    assert(disposition.name == "attachment")
    assert(disposition.isAttachment)
    assert(!disposition.isInline)
    assert(disposition.params.size == 1)
    assert(disposition.params.get("filename*").contains("example.txt"))
    assert(disposition.toString == "attachment; filename*=example.txt")
  }

  it should "be destructured" in {
    val disposition = DispositionType.parse("""extended-type; a=1; b="t w o"; c=trois""")

    disposition match {
      case DispositionType(name, params) =>
        assert(name == disposition.name)
        assert(params == disposition.params)
    }
  }

  it should "not be created with malformed value" in {
    assertThrows[IllegalArgumentException](DispositionType.parse("inline; filename"))
    assertThrows[IllegalArgumentException](DispositionType.parse("inline; filename="))
    assertThrows[IllegalArgumentException](DispositionType.parse("inline; =0.1"))
  }
}
