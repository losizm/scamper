# Scamper
**Scamper** is an HTTP library for Scala. It defines an API for reading and writing
HTTP messages.

[![Maven Central](https://img.shields.io/maven-central/v/com.github.losizm/scamper_2.12.svg?label=Maven%20Central)](https://search.maven.org/search?q=g:%22com.github.losizm%22%20AND%20a:%22scamper_2.12%22)

## HTTP Messages
At the core of **Scamper** is `HttpMessage`, which is a trait that defines the
fundamental characteristics of an HTTP message. `HttpRequest` and `HttpResponse`
extend the specification to define characteristics specific to their respective
message types.

### Building Requests
An `HttpRequest` can be created using one of the factory methods defined in its
companion object. Or you can start with a `RequestMethod` and use builder
methods to further define the request.

```scala
import scamper.ImplicitConverters.stringToURI
import scamper.RequestMethods.GET
import scamper.headers.{ Accept, Host, UserAgent }
import scamper.types.ImplicitConverters.{ stringToMediaRange, stringToProductType }

val request = GET("/motd")
  .withHost("localhost:8080")
  .withUserAgent("Scamper/2.0")
  .withAccept("text/plain", "*/*; q=0.5")
```

### Building Responses
An `HttpResponse` can be created using one of the factory methods defined in its
companion object. Or you can start with a `ResponseStatus` and use builder
methods to further define the response.

```scala
import scamper.ImplicitConverters.stringToEntity
import scamper.ResponseStatuses.Ok
import scamper.headers.{ Connection, ContentType, Server }
import scamper.types.ImplicitConverters.{ stringToMediaType, stringToProductType }

val response = Ok("There is an answer.")
  .withContentType("text/plain")
  .withServer("Scamper/2.0")
  .withConnection("close")
```

## Generalized and Specialized Header Access

There are a set of methods in `HttpMessage` that provide generalized header
access. You provide a `String` for the header field name, which is
case-insensitive, and the header value is also a `String`.

```scala
import scamper.ImplicitConverters.{ stringToURI, tupleToHeader }
import scamper.RequestMethods.POST

val req = POST("/api/users").withHeader("Content-Type" -> "application/json")

val contentType: Option[String] = req.getHeaderValue("Content-Type")
```

_But that's not all there is._

The interface to `HttpMessage` can be extended to include specialized header
acces. These extensions are provided by the many type classes defined in
`scamper.headers`.

For example, `ContentType` includes the following methods:

```scala
/** Tests whether Content-Type header is present. */
def hasContentType: MediaType

/** Gets Content-Type header value if present. */
def getContentType: Option[MediaType]

/** Gets Content-Type header value. */
def contentType: MediaType

/** Creates message with Content-Type header. */
def withContentType(value: MediaType): HttpMessage

/** Creates message without Content-Type header. */
def removeContentType: HttpMessage
```

So you can work with the header in a type-safe manner.

```scala
import scamper.ImplicitConverters.stringToURI
import scamper.RequestMethods.POST
import scamper.headers.ContentType
import scamper.types.MediaType

val req = POST("/api/users").withContentType(MediaType.parse("application/json"))
println(req.contentType.mainType) // application
println(req.contentType.subtype) // json
```

And with `stringToMediaType` in scope, you can implicitly convert a `String` to
a `MediaType`.

```scala
import scamper.ImplicitConverters.stringToURI
import scamper.RequestMethods.POST
import scamper.headers.ContentType
import scamper.types.ImplicitConverters.stringToMediaType

val req = POST("/api/users").withContentType("application/json")
println(req.contentType.mainType) // application
println(req.contentType.subtype) // json
```
## Message Body
The message body is represented as an instance of `Entity`, which provides
access to a `java.io.InputStream`.

### Creating Message Body
When building a message, use one of the `Entity` factory methods to create the
body. For example, you can create a text body.

```scala
import scamper.Entity
import scamper.ResponseStatuses.Ok
import scamper.headers.ContentType
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

Or create a message body from file content.

```scala
import java.io.File
import scamper.Entity
import scamper.ResponseStatuses.Ok
import scamper.headers.ContentType
import scamper.types.ImplicitConverters.stringToMediaType

val body = Entity(new File("./index.html"))
val res = Ok(body).withContentType("text/html; charset=utf-8")
```

### Parsing Message Body

When handling an incoming message, use an appropriate `BodyParser` to parse the
message body. There is a set of standard parsers available in `BodyParsers`,
such as the one used for parsing text content.

```scala
import scamper.{ BodyParsers, HttpMessage }

// Creates a text body parser
implicit val textBodyParser = BodyParsers.text(maxLength = 1024)

def printText(message: HttpMessage): Unit = {
  // Parses message body to String using implicit textBodyParser
  val text = message.parse[String]

  println(text)
}
```

You can also implement custom body parsers. Here's one that exploits the power
of [little-json](https://github.com/losizm/little-json):

```scala
import javax.json.JsonObject
import little.json.{ Json, FromJson }
import little.json.Implicits._
import scamper.{ BodyParser, BodyParsers, HttpMessage }

case class User(id: Int, name: String)

implicit object UserBodyParser extends BodyParser[User] {
  // Create standard text body parser
  implicit val textBodyParser = BodyParsers.text(maxLength = 1024)

  // Define how to convert JSON to User
  implicit val userFromJson: FromJson[User] = {
    case json: JsonObject => User(json.getInt("id"), json.getString("name"))
  }

  // Parses JSON message body to User
  def parse(message: HttpMessage): User =
    Json.parse(message.parse[String]).as[User]
}

def printUser(message: HttpMessage): Unit = {
  // Parses message body to User using implicit UserBodyParser
  val user = message.parse[User]

  println(s"uid=${user.id}(${user.name})")
}
```

## HTTP Client
**Scamper** provides a client for sending requests and handling the responses.

In the following example, an `HttpRequest` is sent, and the `HttpResponse`
handler prints a message in accordance to the response status.

```scala
import scamper.HttpClient
import scamper.ImplicitConverters.{ stringToEntity, stringToURI }
import scamper.RequestMethods.POST
import scamper.ResponseFilters._
import scamper.headers.{ ContentType, Host, Location }
import scamper.types.ImplicitConverters.stringToMediaType

object UserAdminClient {
  def createUser(id: Int, name: String): Unit = {
    val req = POST("/users")
      .withHost("localhost:9000")
      .withContentType("application/json")
      .withBody(s"""{"id":$id, "name":"$name"}""")

    HttpClient.send(req, secure = true) {
      case Successful(_)    => println("Successful")
      case Redirection(res) => println(s"Redirection: ${res.location}")
      case ClientError(res) => println(s"Client error: ${res.status}")
      case ServerError(res) => println(s"Server error: ${res.status}")
      case Informational(_) => println("Informational")
    }
  }
}
```

## API Documentation

See [scaladoc](https://losizm.github.io/scamper/latest/api/scamper/index.html)
for additional details.

## License
Scamper is licensed under the Apache license, version 2. See LICENSE file for
more information.
