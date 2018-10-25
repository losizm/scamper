# Scamper - HTTP Library for Scala
Scamper is an HTTP library for Scala. It defines an API for reading and writing
HTTP messages.

## HTTP Messages
At the core of Scamper is `HttpMessage`, which is a trait that defines the
fundamental characteristics of an HTTP message. `HttpRequest` and `HttpResponse`
extend the specification to define characteristics specific to their respective
message types.

### Building Requests
An `HttpRequest` can be created using one of the factory methods defined in its
companion object. Or you can start with a `RequestMethod` and build the request
using [implicit headers and type converters](#implicit-headers-and-type-converters).

```scala
import scamper.ImplicitHeaders.{ Accept, Host, UserAgent }
import scamper.RequestMethods.GET
import scamper.types.ImplicitConverters.{ stringToMediaRange, stringToProductType }

val request = GET("/index.html")
  .withHost("localhost:8080")
  .withUserAgent("Scamper/1.0")
  .withAccept("text/html", "*/*; q=0.5")
```

### Building Responses
An `HttpResponse` can be created using one of the factory methods defined in its
companion object. Or you can start with a `ResponseStatus` and build the
response using [implicit headers and type converters](#implicit-headers-and-type-converters).

```scala
import scamper.ImplicitConverters.stringToEntity
import scamper.ImplicitHeaders.{ Connection, ContentType, Server }
import scamper.ResponseStatuses.Ok
import scamper.types.ImplicitConverters.{ stringToMediaType, stringToProductType }

val response = Ok("Hello, world!")
  .withContentType("text/plain")
  .withServer("Scamper/1.0")
  .withConnection("close")
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
def withoutContentType: HttpMessage
```

So you can work with the message header in a type-safe manner.

```scala
import scamper.ImplicitHeaders.ContentType
import scamper.RequestMethods.POST
import scamper.types.MediaType

val req = POST("/api/users").withContentType(MediaType("application/json"))
println(req.contentType.mainType) // application
println(req.contentType.subtype) // json
```

And with `stringToMediaType` in scope, you can implicitly convert a `String` to
a `MediaType`.

```scala
import scamper.ImplicitHeaders.ContentType
import scamper.RequestMethods.POST
import scamper.types.ImplicitConverters.stringToMediaType

val req = POST("/api/users").withContentType("application/json")
println(req.contentType.mainType) // application
println(req.contentType.subtype) // json
```
## Message Body
The message body is represented as an instance of `Entity`, which provides
access to an input stream.

### Creating Message Body
When building a message, you can use one of the `Entity` factory methods to
create the message body. For example, you can create a text message body.

```scala
import scamper.Entity
import scamper.ImplicitHeaders.ContentType
import scamper.ResponseStatuses.Ok
import scamper.types.ImplicitConverters.stringToMediaType

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

Or you can create a message body from file content.

```scala
import java.io.File
import scamper.Entity
import scamper.ImplicitHeaders.ContentType
import scamper.ResponseStatuses.Ok
import scamper.types.ImplicitConverters.stringToMediaType

val body = Entity(new File("./index.html"))
val res = Ok(body).withContentType("text/html; charset=utf-8")
```

### Parsing Message Body

When handling an incoming message, use an appropriate `BodyParser` to parse the
message body. There is a set of standard parsers in `BodyParsers`, such as the
one used for parsing general text content.

```scala
import scamper.{ BodyParsers, HttpMessage }

// Creates a text body parser
implicit val textBodyParser = BodyParsers.text(maxLength = 1024)

def printText(message: HttpMessage): Unit = {
  // Parses message body to String using implicit textBodyParser
  val text = message.bodyAs[String]

  println(text)
}
```

You can also implement custom body parsers. Here's one that gets a little help
from a standard body parser and [play-json](https://github.com/playframework/play-json):

```scala
import play.api.libs.json._
import scamper.{ BodyParser, BodyParsers, HttpMessage }

case class User(id: Long, name: String)

implicit object UserBodyParser extends BodyParser[User] {
  // Create standard text body parser
  implicit val textBodyParser = BodyParsers.text(maxLength = 1024)
  // Create play-json parser
  implicit val userReads = Json.reads[User]

  // Parses JSON message body to User
  def parse(message: HttpMessage): User =
    Json.parse(message.bodyAs[String]).as[User]
}

def printUser(message: HttpMessage): Unit = {
  // Parses message body to User using implicit UserBodyParser
  val user = message.bodyAs[User]

  println(s"uid=${user.id}(${user.name})")
}
```

## HTTP Client Extensions
Scamper provides client extensions for sending requests and handling the
responses.

In this next example, an extension to `HttpRequest` is used to send the request,
and a `scamper.util.ResponseFilter` stack forms a pattern-matching expression to
handle the `HttpResponse`.

```scala
import scamper.ImplicitConverters.stringToEntity
import scamper.ImplicitHeaders.{ ContentType, Host, Location }
import scamper.RequestMethods.POST
import scamper.extensions.HttpRequestType
import scamper.types.ImplicitConverters.stringToMediaType
import scamper.util.ResponseFilters._

object UserAdminClient {
  def createUser(id: Int, name: String): Unit = {
    val req = POST("/users")
      .withHost("localhost:9000")
      .withContentType("application/json")
      .withBody(s"""{"id":$id, "name":"$name"}""")

    // The send method is added via HttpRequestType
    req.send(secure = true) {
      case Successful(_)    => println("Successful")
      case Redirection(res) => println(s"Redirection: ${res.location}")
      case ClientError(res) => println(s"Client error: ${res.status}")
      case ServerError(res) => println(s"Server error: ${res.status}")
      case Informational(_) => println("Informational")
    }
  }
}
```

There are also extension methods to `java.net.URL` corresponding to the standard
HTTP request methods (i.e., GET, POST, etc.). Here's a rewrite of the above
example using the URL extension:

```scala
import java.net.URL
import scamper.ImplicitConverters.{ stringToEntity, stringToHeader }
import scamper.ImplicitHeaders.Location
import scamper.extensions.URLType
import scamper.types.ImplicitConverters.stringToMediaType
import scamper.util.ResponseFilters._

object UserAdminClient {
  def createUser(id: Int, name: String): Unit = {
    val url = new URL("https://localhost:9000/users")

    // The post method is added via URLType
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

## License
Scamper is licensed under the Apache license, version 2. See LICENSE file for
more information.
