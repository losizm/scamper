# Scamper - HTTP Library for Scala
Scamper provides an API for reading and writing HTTP messages. It defines a set
of general interfaces, and it extends the feature set using the _Type Class
Pattern_ for specialized access to HTTP headers.

## HTTP Messages
The details of an HTTP message are defined in the `scamper.HttpMessage` trait.
The `HttpRequest` and `HttpResponse` traits extend the specification to define
additional characteristics for their respective message types.

### Building Requests
An easy way to build a request is to make use of the API's [implicit headers and
type converters](#implicit-headers-and-type-converters).

```scala
import scamper.ImplicitHeaders._
import scamper.RequestMethods._
import scamper.types.ImplicitConverters._

val request = GET("/index.html")
  .withHost("localhost:8080")
  .withUserAgent("Scamper/1.0")
  .withAccept("text/html", "*/*; q=0.5")
```

### Building Responses
Likewise, you can use the [implicit headers and type converters](#implicit-headers-and-type-converters)
to build a response.

```scala
import scamper.ImplicitConverters._
import scamper.ImplicitHeaders._
import scamper.ResponseStatuses._
import scamper.types.ImplicitConverters._

val response = Ok("Hello, world!")
  .withContentType("text/plain")
  .withServer("Scamper/1.0")
  .withConnection("close")
```

## Using HTTP Client Extensions
Scamper provides client extensions for sending requests and handling the
responses.

In this example, an extension to `HttpRequest` is used to send the request, and
a `scamper.util.ResponseFilter` stack forms a pattern-matching expression to
handle the `HttpResponse`:

```scala
import scamper.ImplicitConverters._
import scamper.ImplicitHeaders._
import scamper.RequestMethods._
import scamper.types.ImplicitConverters._
import scamper.util.ResponseFilters._
// Adds methods to HttpRequest
import scamper.extensions.HttpRequestExtension

object UserAdminClient {
  def createUser(id: Int, name: String): Unit = {
    // Build POST request
    val req = POST("/users")
      .withHost("localhost:9000")
      .withContentType("application/json")
      .withBody(s"""{"id":$id, "name":"$name"}""")

    // Send request over SSL
    req.send(secure = true) {
      // Handle different response types
      case Successful(_)    => println("Successful")
      case Redirection(res) => println(s"Redirection: ${res.location}")
      case ClientError(res) => println(s"Client error: ${res.status}")
      case ServerError(res) => println(s"Server error: ${res.status}")
      case Informational(_) => println("Informational")
    }
  }
}
```

There are also method extensions to `java.net.URL` corresponding to the standard
HTTP request methods (GET, POST, etc.). Here's a rewrite of the above example
using the URL extension:

```scala
import java.net.URL
import scamper.ImplicitConverters._
import scamper.ImplicitHeaders.Location
import scamper.types.ImplicitConverters._
import scamper.util.ResponseFilters._
// Adds methods to java.net.URL
import scamper.extensions.URLExtension

object UserAdminClient {
  def createUser(id: Int, name: String): Unit = {
    val url = new URL("https://localhost:9000/users")

    // The post method is added implicitly via URLExtension
    url.post(s"""{"id":$id, "name":"$name"}""", "Content-Type: application/json") {
      case Successful(_)    => println("Successful")
      case Redirection(res) => println(s"Redirection: ${res.location}")
      case ClientError(res) => println(s"Client error: ${res.status}")
      case ServerError(res) => println(s"Server error: ${res.status}")
      case Informational(_) => println("Informational")
    }
  }
}
```

## Working with Message Body
The message body is represented as an instance of `scamper.Entity`, which
provides access to an input stream.

### Creating Message Body
When building an `HttpRequest` or `HttpResponse`, you can use one of the Entity
factory methods to create the message body. For example:

```scala
val body = Entity("""
<!DOCTYPE html>
<html>
  <head>
    <title>Example</title>
  </head>
  <body>
    <p>Hello, world!</p>
  </body>
</html>
""")
val res = Ok(body).withContentType("text/html; charset=utf-8")
```

And here's another example using a file as the entity:

```scala
val body = Entity(new java.io.File("./db/weather-data.json"))
val res = Ok(body).withContentType("application/json")
```

### Parsing Message Body

When handling an incoming message, you need to use an appropriate
`scamper.BodyParser` to parse the message body. There is a set of standard
parser implementations in `scamper.BodyParsers`.

```scala
import java.net.URL
import scala.util.Try
import scamper.BodyParsers
import scamper.extensions.URLExtension

val url = new URL("http://localhost:8080/db/weather-data.json")

val jsonText: Try[String] = url.get() { res =>
  // Parses the body as text
  res.parse(BodyParsers.text(maxLength = 1024))
}
```

You can also create a custom `BodyParser`. Here's one that gets a little help
from a standard body parser and [play-json](https://github.com/playframework/play-json):

```scala
import java.net.URL
import play.api.libs.json._
import scamper.{ BodyParser, BodyParsers, HttpMessage }
import scamper.extensions.URLExtension

case class User(id: Long, name: String)

object UserBodyParser extends BodyParser[User] {
  // Create standard text body parser
  implicit val textBodyParser = BodyParsers.text(maxLength = 1024)
  // Create play-json parser
  implicit val userReads = Json.reads[User]

  // Parses JSON message body to User
  def apply(message: HttpMessage): User =
    Json.parse(textBodyParser(message)).as[User]
}

val url = new URL("http://localhost:9000/users/500")

url.get() { res =>
  res.parse(UserBodyParser).foreach {
    case User(id, name) => println(s"$id -> $name")
  }
}
```

## Implicit Headers and Type Converters
The implicit headers and type converters are defined in
`scamper.ImplicitHeaders` and `scamper.types.ImplicitConverters`. They allow
type-safe access to message headers.

For example, the `ContentType` header adds the following methods to
`HttpMessage`:

```scala
/** Gets Content-Type header value */
def contentType: MediaType
/** Gets Content-Type header value if present */
def getContentType: Option[MediaType]
/** Creates message with Content-Type header */
def withContentType(value: MediaType): HttpMessage
/** Creates message without Content-Type header */
def removeContentType: HttpMessage
```

So you can work with the message header in a type-safe manner:

```scala
val req = POST("/api/users").withContentType(MediaType("application/json"))
println(req.contentType.mainType) // application
println(req.contentType.subtype) // json
```

And with `stringToMediaType` in scope, you can implicitly convert `String` to
`MediaType`:

```scala
val req = POST("/api/users").withContentType("application/json")
println(req.contentType.mainType) // application
println(req.contentType.subtype) // json
```
