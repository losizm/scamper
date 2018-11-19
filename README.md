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

## Specialized Header Access

There is a set of methods in `HttpMessage` that provides generalized header
access. With these methods, the header field name is a `String`, which is
case-insensitive, and the header value is also represented as `String`.

```scala
import scamper.ImplicitConverters.{ stringToURI, tupleToHeader }
import scamper.RequestMethods.POST

val req = POST("/api/users").withHeader("Content-Type" -> "application/json")

val contentType: Option[String] = req.getHeaderValue("Content-Type")
```

_But that's not all there is._

The interface to `HttpMessage` can be extended for specialized header access.
The extension methods are provided by the many type classes defined in
`scamper.headers`.

For example, `ContentType` includes the following methods:

```scala
/** Tests whether Content-Type header is present. */
def hasContentType: Boolean

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

And with `stringToMediaType` in scope, you can implicitly convert `String` to
`MediaType`.

```scala
import scamper.ImplicitConverters.stringToURI
import scamper.RequestMethods.POST
import scamper.headers.ContentType
import scamper.types.ImplicitConverters.stringToMediaType

val req = POST("/api/users").withContentType("application/json")
println(req.contentType.mainType) // application
println(req.contentType.subtype) // json
```

## Specialized Cookie Access

In much the same way specialized access to headers is available, so too is
the case for cookies. Specialized access is provided by classes in
`scamper.cookies`.

### Request Cookies

In `HttpRequest`, cookies are stringed together in the **Cookie** header. You
may, of course, access the cookies in their _unbaked_ form using generalized
header access. Or you can access them using the extension methods provided by
`RequestCookies`, with each cookie represented as `PlainCookie`.

```scala
import scamper.ImplicitConverters.stringToURI
import scamper.RequestMethods.GET
import scamper.cookies.{ PlainCookie, RequestCookies }

val req = GET("https://localhost:8080/motd")
  .withCookies(PlainCookie("ID", "bG9zCg"), PlainCookie("Region", "SE-US"))

// Print name and value of both cookies
req.cookies.foreach(cookie => println(s"${cookie.name}=${cookie.value}"))

// Get cookies by name
val id: Option[PlainCookie] = req.getCookie("ID")
val region: Option[PlainCookie] = req.getCookie("Region")

// Get cookie values by name
assert(req.getCookieValue("ID").contains("bG9zCg"))
assert(req.getCookieValue("Region").contains("SE-US"))

// Get unbaked cookies
assert(req.getHeaderValue("Cookie").contains("ID=bG9zCg; Region=SE-US"))
```

### Response Cookies

In `HttpResponse`, the cookies are a collection of **Set-Cookie** header values.
Specialized access is provided by `ResponseCookies`, with each cookie
represented as `SetCookie`.

Along with name and value, `SetCookie` provides additional attributes, such as
the path for which the cookie is valid, when the cookie expires, whether the
cookie should be sent over secure channels only, and a few others.

```scala
import scamper.ImplicitConverters.stringToEntity
import scamper.ResponseStatuses.Ok
import scamper.cookies.{ ResponseCookies, SetCookie }

val res = Ok("There is an answer.").withCookies(
  SetCookie("ID", "bG9zCg", path = Some("/motd"), secure = true),
  SetCookie("Region", "SE-US")
)

// Print both cookies
res.cookies.foreach(println)

// Get cookies by name
val id: Option[SetCookie] = res.getCookie("ID")
val region: Option[SetCookie] = res.getCookie("Region")

// Get attributes of ID cookie
val path: String = id.flatMap(_.path).getOrElse("/")
val secure: Boolean = id.map(_.secure).getOrElse(false)

// Get cookie values by name
assert(res.getCookieValue("ID").contains("bG9zCg"))
assert(res.getCookieValue("Region").contains("SE-US"))

// Get unbaked cookies
assert(res.getHeaderValue("Set-Cookie").contains("ID=bG9zCg; Path=/motd; Secure"))
assert(res.getHeaderValues("Set-Cookie").size == 2)
```

_**Note:** Each response cookie is presented in its own **Set-Cookie** header.
`getHeaderValues()` collects all of the header values into `Seq[String]`;
whereas, `getHeaderValue()` retrieves the first header value only._

## Message Body
The message body is represented as an `Entity`, which provides access to a
`java.io.InputStream`.

### Creating Message Body
When building a message, use one of the `Entity` factory methods to create the
body. For example, you can create a message body with text content.

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
such as one used for parsing text content.

```scala
import scamper.{ BodyParsers, HttpMessage }

// Create text body parser
implicit val textBodyParser = BodyParsers.text(maxLength = 1024)

def printText(message: HttpMessage): Unit = {
  // Parse message body to String using textBodyParser implicitly
  val text = message.as[String]

  println(text)
}
```

And you can implement your own body parsers. Here's one that exploits the power
of [little-json](https://github.com/losizm/little-json):

```scala
import javax.json.{ JsonObject, JsonValue }
import little.json.{ Json, FromJson }
import little.json.Implicits._
import scamper.{ BodyParser, HttpMessage }

case class User(id: Int, name: String)

implicit object UserBodyParser extends BodyParser[User] {
  // Define how to convert JsonObject to User
  implicit val userFromJson: FromJson[User] = {
    case json: JsonObject => User(json.getInt("id"), json.getString("name"))
    case json: JsonValue  => throw new IllegalArgumentException("Not an OBJECT")
  }

  // Parses JSON message body to User
  def parse(message: HttpMessage): User =
    Json.parse(message.body.getInputStream).as[User]
}

def printUser(message: HttpMessage): Unit = {
  // Parse message body to User using UserBodyParser implicitly
  val user = message.as[User]

  println(s"uid=${user.id}(${user.name})")
}
```

## HTTP Client
The `HttpClient` object can be used for sending a request and handling the
response.

```scala
import scamper.HttpClient
import scamper.ImplicitConverters.{ stringToEntity, stringToURI }
import scamper.RequestMethods.POST
import scamper.ResponseFilters._
import scamper.headers.{ ContentType, Host, Location }
import scamper.types.ImplicitConverters.stringToMediaType

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
```

In fact, `HttpClient.send()` returns the value returned by the response handler.
So you can process the response and return whatever information warranted.

```scala
import scamper.{ BodyParsers, HttpClient }
import scamper.ImplicitConverters.stringToURI
import scamper.RequestMethods.GET
import scamper.headers.Host

implicit val bodyParser = BodyParsers.text()

def getMessageOfTheDay(): Either[Int, String] = {
  val req = GET("/motd").withHost("localhost:8080")

  HttpClient.send(req) { res =>
    res.status.isSuccessful match {
      case true  => Right(res.as[String])
      case false => Left(res.status.code)
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
