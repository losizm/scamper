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
package scamper

class UriSpec extends org.scalatest.flatspec.AnyFlatSpec {
  it should "create Uri from String" in {
    var uri = Uri("http://localhost:8080/index.html")
    assert { uri.getScheme == "http" }
    assert { uri.getRawAuthority == "localhost:8080" }
    assert { uri.getRawPath == "/index.html" }
    assert { uri.getQuery == null }
    assert { uri.getFragment == null }
    assert { uri.toString == "http://localhost:8080/index.html" }
  }

  it should "create Uri from scheme, scheme-specific part, and fragment" in {
    var uri = Uri("http", "//localhost:8080/index.html?a=1&b=2", "top")
    assert { uri.getScheme == "http" }
    assert { uri.getSchemeSpecificPart == "//localhost:8080/index.html?a=1&b=2" }
    assert { uri.getRawAuthority == "localhost:8080" }
    assert { uri.getRawPath == "/index.html" }
    assert { uri.getRawQuery == "a=1&b=2" }
    assert { uri.getRawFragment == "top" }
    assert { uri.toString == "http://localhost:8080/index.html?a=1&b=2#top" }

    uri = Uri("mailto", "someone@somewhere.com")
    assert { uri.getScheme == "mailto" }
    assert { uri.getSchemeSpecificPart == "someone@somewhere.com" }
    assert { uri.getRawAuthority == null }
    assert { uri.getRawPath == null }
    assert { uri.getRawQuery == null }
    assert { uri.getRawFragment == null }
    assert { uri.toString == "mailto:someone@somewhere.com" }
  }

  it should "create Uri with http scheme" in {
    var uri = Uri.http("localhost:8080", "/index.html", "a=1&b=2", "top")
    assert { uri.getScheme == "http" }
    assert { uri.getSchemeSpecificPart == "//localhost:8080/index.html?a=1&b=2" }
    assert { uri.getRawAuthority == "localhost:8080" }
    assert { uri.getRawPath == "/index.html" }
    assert { uri.getRawQuery == "a=1&b=2" }
    assert { uri.getRawFragment == "top" }
    assert { uri.toString == "http://localhost:8080/index.html?a=1&b=2#top" }

    uri = Uri.http("localhost:8080", "/index.html", "a=1&b=2")
    assert { uri.getScheme == "http" }
    assert { uri.getSchemeSpecificPart == "//localhost:8080/index.html?a=1&b=2" }
    assert { uri.getRawAuthority == "localhost:8080" }
    assert { uri.getRawPath == "/index.html" }
    assert { uri.getRawQuery == "a=1&b=2" }
    assert { uri.getRawFragment == null }
    assert { uri.toString == "http://localhost:8080/index.html?a=1&b=2" }

    uri = Uri.http("localhost:8080", "/index.html")
    assert { uri.getScheme == "http" }
    assert { uri.getSchemeSpecificPart == "//localhost:8080/index.html" }
    assert { uri.getRawAuthority == "localhost:8080" }
    assert { uri.getRawPath == "/index.html" }
    assert { uri.getRawQuery == null }
    assert { uri.getRawFragment == null }
    assert { uri.toString == "http://localhost:8080/index.html" }

    uri = Uri.http("localhost:8080")
    assert { uri.getScheme == "http" }
    assert { uri.getSchemeSpecificPart == "//localhost:8080" }
    assert { uri.getRawAuthority == "localhost:8080" }
    assert { uri.getRawPath == "" }
    assert { uri.getRawQuery == null }
    assert { uri.getRawFragment == null }
    assert { uri.toString == "http://localhost:8080" }

    uri = Uri.http("localhost:8080", "/index.html", fragment = "top")
    assert { uri.getScheme == "http" }
    assert { uri.getSchemeSpecificPart == "//localhost:8080/index.html" }
    assert { uri.getRawAuthority == "localhost:8080" }
    assert { uri.getRawPath == "/index.html" }
    assert { uri.getRawQuery == null }
    assert { uri.getRawFragment == "top" }
    assert { uri.toString == "http://localhost:8080/index.html#top" }

    uri = Uri.http("localhost:8080", query = "a=1&b=2", fragment = "top")
    assert { uri.getScheme == "http" }
    assert { uri.getSchemeSpecificPart == "//localhost:8080?a=1&b=2" }
    assert { uri.getRawAuthority == "localhost:8080" }
    assert { uri.getRawPath == "" }
    assert { uri.getRawQuery == "a=1&b=2" }
    assert { uri.getRawFragment == "top" }
    assert { uri.toString == "http://localhost:8080?a=1&b=2#top" }
  }

  it should "create Uri with https scheme" in {
    var uri = Uri.https("localhost:8080", "/index.html", "a=1&b=2", "top")
    assert { uri.getScheme == "https" }
    assert { uri.getSchemeSpecificPart == "//localhost:8080/index.html?a=1&b=2" }
    assert { uri.getRawAuthority == "localhost:8080" }
    assert { uri.getRawPath == "/index.html" }
    assert { uri.getRawQuery == "a=1&b=2" }
    assert { uri.getRawFragment == "top" }
    assert { uri.toString == "https://localhost:8080/index.html?a=1&b=2#top" }

    uri = Uri.https("localhost:8080", "/index.html", "a=1&b=2")
    assert { uri.getScheme == "https" }
    assert { uri.getSchemeSpecificPart == "//localhost:8080/index.html?a=1&b=2" }
    assert { uri.getRawAuthority == "localhost:8080" }
    assert { uri.getRawPath == "/index.html" }
    assert { uri.getRawQuery == "a=1&b=2" }
    assert { uri.getRawFragment == null }
    assert { uri.toString == "https://localhost:8080/index.html?a=1&b=2" }

    uri = Uri.https("localhost:8080", "/index.html")
    assert { uri.getScheme == "https" }
    assert { uri.getSchemeSpecificPart == "//localhost:8080/index.html" }
    assert { uri.getRawAuthority == "localhost:8080" }
    assert { uri.getRawPath == "/index.html" }
    assert { uri.getRawQuery == null }
    assert { uri.getRawFragment == null }
    assert { uri.toString == "https://localhost:8080/index.html" }

    uri = Uri.https("localhost:8080")
    assert { uri.getScheme == "https" }
    assert { uri.getSchemeSpecificPart == "//localhost:8080" }
    assert { uri.getRawAuthority == "localhost:8080" }
    assert { uri.getRawPath == "" }
    assert { uri.getRawQuery == null }
    assert { uri.getRawFragment == null }
    assert { uri.toString == "https://localhost:8080" }

    uri = Uri.https("localhost:8080", "/index.html", fragment = "top")
    assert { uri.getScheme == "https" }
    assert { uri.getSchemeSpecificPart == "//localhost:8080/index.html" }
    assert { uri.getRawAuthority == "localhost:8080" }
    assert { uri.getRawPath == "/index.html" }
    assert { uri.getRawQuery == null }
    assert { uri.getRawFragment == "top" }
    assert { uri.toString == "https://localhost:8080/index.html#top" }

    uri = Uri.https("localhost:8080", query = "a=1&b=2", fragment = "top")
    assert { uri.getScheme == "https" }
    assert { uri.getSchemeSpecificPart == "//localhost:8080?a=1&b=2" }
    assert { uri.getRawAuthority == "localhost:8080" }
    assert { uri.getRawPath == "" }
    assert { uri.getRawQuery == "a=1&b=2" }
    assert { uri.getRawFragment == "top" }
    assert { uri.toString == "https://localhost:8080?a=1&b=2#top" }
  }
}
