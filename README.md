# Scamper
**Scamper** is the HTTP library for Scala. It defines the API for reading and
writing HTTP messages, and it includes [client](#HTTP-Client) and
[server](#HTTP-Server) implementations.

[![Maven Central](https://img.shields.io/maven-central/v/com.github.losizm/scamper_2.13.svg?label=Maven%20Central)](https://search.maven.org/search?q=g:%22com.github.losizm%22%20AND%20a:%22scamper_2.13%22)

## Table of Contents
- [Getting Started](#Getting-Started)
- [HTTP Messages](#HTTP-Messages)
  - [Building Requests](#Building-Requests)
  - [Building Responses](#Building-Responses)
- [Specialized Header Access](#Specialized-Header-Access)
- [Specialized Cookie Access](#Specialized-Cookie-Access)
  - [Request Cookies](#Request-Cookies)
  - [Response Cookies](#Response-Cookies)
- [Message Body](#Message-Body)
  - [Creating Body](#Creating-Body)
  - [Parsing Body](#Parsing-Body)
- [Multipart Message Body](#Multipart-Message-Body)
- [Message Attributes](#Message-Attributes)
- [HTTP Authentication](#HTTP-Authentication)
  - [Basic Authentication](#Basic-Authentication)
  - [Bearer Authentication](#Bearer-Authentication)
- [HTTP Client](#HTTP-Client)
  - [Creating Client](#Creating-Client)
  - [Configuring Client](#Configuring-Client)
  - [Adding Request and Response Filters](#Adding-Request-and-Response-Filters)
  - [Using WebSocket Client](#Using-WebSocket-Client)
- [HTTP Server](#HTTP-Server)
  - [Server Application](#Server-Application)
  - [Request Handlers](#Request-Handlers)
    - [Targeted Handling](#Targeted-Handling)
    - [Path Parameters](#Path-Parameters)
    - [Serving Static Files](#Serving-Static-Files)
    - [Serving Static Resources](#Serving-Static-Resources)
    - [Aborting Response](#Aborting-Response)
  - [WebSocket Session](#WebSocket-Session)
  - [Error Handler](#Error-Handler)
  - [Router](#Router)
  - [Response Filters](#Response-Filters)
  - [Securing Server](#Securing-Server)
  - [Creating Server](#Creating-Server)
- [API Documentation](#API-Documentation)
- [License](#License)

## Getting Started
To use **Scamper**, start by adding it as a dependency to your project:

```scala
libraryDependencies += "com.github.losizm" %% "scamper" % "14.1.1"
```

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
import scamper.Implicits.stringToUri
import scamper.RequestMethod.Registry.GET
import scamper.headers.{ Accept, Host}
import scamper.types.Implicits.stringToMediaRange

val req = GET("/motd")
  .withHost("localhost:8080")
  .withAccept("text/plain", "*/*; q=0.5")
```

### Building Responses
An `HttpResponse` can be created using one of the factory methods defined in its
companion object. Or you can start with a `ResponseStatus` and use builder
methods to further define the response.

```scala
import scamper.Implicits.stringToEntity
import scamper.ResponseStatus.Registry.Ok
import scamper.headers.{ Connection, ContentType }
import scamper.types.Implicits.stringToMediaType

val res = Ok("There is an answer.")
  .withContentType("text/plain")
  .withConnection("close")
```

## Specialized Header Access

There is a set of methods in `HttpMessage` that provides generalized header
access. With these methods, the header name is a `String`, which is
case-insensitive, and the header value is a `String`.

```scala
import scamper.Implicits.{ stringToUri, tupleToHeader }
import scamper.RequestMethod.Registry.POST

val req = POST("/api/users").withHeader("Content-Type" -> "application/json")

val contentType: Option[String] = req.getHeaderValue("Content-Type")
```

This gets the job done in many cases; however, `HttpMessage` can be extended for
specialized header access. There are extension methods provided by type classes
defined in `scamper.headers`.

For example, `ContentType` adds the following methods:

```scala
/** Gets Content-Type header value. */
def contentType: MediaType

/** Gets Content-Type header value if present. */
def getContentType: Option[MediaType]

/** Tests for Content-Type header. */
def hasContentType: Boolean

/** Creates message with Content-Type header set to supplied value. */
def withContentType(value: MediaType): HttpMessage

/** Creates message removing Content-Type header. */
def removeContentType(): HttpMessage
```

So you can work with the header in a type-safe manner.

```scala
import scamper.Implicits.stringToUri
import scamper.RequestMethod.Registry.POST
import scamper.headers.ContentType
import scamper.types.MediaType

val req = POST("/api/users").withContentType(MediaType("application", "json"))
println(req.contentType.mainType) // application
println(req.contentType.subtype) // json
```

And with utilities defined in `scamper.types.Implicits`, you can implicitly
convert to specialized header types.

```scala
import scamper.Implicits.stringToUri
import scamper.RequestMethod.Registry.POST
import scamper.headers.ContentType
import scamper.types.Implicits.stringToMediaType

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
may access the cookies in their _unbaked_ form using generalized header access.
Or you can access them using the extension methods provided by `RequestCookies`,
with each cookie represented as `PlainCookie`.

```scala
import scamper.Implicits.stringToUri
import scamper.RequestMethod.Registry.GET
import scamper.cookies.{ PlainCookie, RequestCookies }

val req = GET("https://localhost:8080/motd").withCookies(
  PlainCookie("ID", "bG9zCg"), PlainCookie("Region", "SE-US")
)

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
the path to which the cookie is valid, when the cookie expires, whether the
cookie should be sent over secure channels only, and a few others.

```scala
import scamper.Implicits.stringToEntity
import scamper.ResponseStatus.Registry.Ok
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
`getHeaderValues()` collects all header values into `Seq[String]`; whereas,
`getHeaderValue()` retrieves first header value only._

## Message Body
The message body is represented as an `Entity`, which provides access to a
`java.io.InputStream`.

### Creating Body
When building a message, use the `Entity` factory to create the body. For
example, you can create a message with text content.

```scala
import scamper.Entity
import scamper.ResponseStatus.Registry.Ok
import scamper.headers.ContentType
import scamper.types.Implicits.stringToMediaType

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

Or create a message using file content.

```scala
import java.io.File
import scamper.Entity
import scamper.ResponseStatus.Registry.Ok
import scamper.headers.ContentType
import scamper.types.Implicits.stringToMediaType

val body = Entity(new File("./index.html"))
val res = Ok(body).withContentType("text/html; charset=utf-8")
```

There are implicit converters available for common entity types, so you aren't
required to create them explicitly.

```scala
import java.io.File
import scamper.Implicits.fileToEntity
import scamper.ResponseStatus.Registry.Ok
import scamper.headers.ContentType
import scamper.types.Implicits.stringToMediaType

val res = Ok(new File("./index.html")).withContentType("text/html; charset=utf-8")
```

### Parsing Body

When handling an incoming message, use an appropriate `BodyParser` to parse the
message body. There are factory methods available, such as one used for creating
a text body parser.

```scala
import scamper.{ BodyParser, HttpMessage }

// Create text body parser
implicit val parser = BodyParser.text(maxLength = 1024)

def printText(message: HttpMessage): Unit = {
  // Parse message body to String using implicit parser
  val text = message.as[String]

  println(text)
}
```

And you can implement your own parser. Here's one power by [little-json](https://github.com/losizm/little-json):

```scala
import javax.json.{ JsonObject, JsonValue }
import little.json.{ Json, JsonInput }
import little.json.Implicits._
import scamper.{ BodyParser, HttpMessage }

case class User(id: Int, name: String)

implicit object UserParser extends BodyParser[User] {
  // Define how to convert JsonObject to User
  implicit val userInput: JsonInput[User] = {
    case json: JsonObject => User(json.getInt("id"), json.getString("name"))
    case json: JsonValue  => throw new IllegalArgumentException("Not an OBJECT")
  }

  // Parses JSON message body to User
  def parse(message: HttpMessage): User =
    Json.parse(message.body.getInputStream).as[User]
}

def printUser(message: HttpMessage): Unit = {
  // Parse message body to User (implicitly using UserParser)
  val user = message.as[User]

  println(s"uid=${user.id}(${user.name})")
}
```

## Multipart Message Body

You can create a message with multipart form-data, which is generally required for
form submission containing file content. When the multipart body is added to the
message, the **Content-Type** header is set to `multipart/form-data` with a
boundary parameter whose value is used to delimit parts in the encoded body.

```scala
import java.io.File
import scamper.{ Multipart, TextPart, FilePart }
import scamper.Implicits.{ HttpMessageType, stringToUri }
import scamper.RequestMethod.Registry.POST

// Build multipart form-data with text and file content
val formData = Multipart(
  TextPart("title", "Form Of Intellect"),
  TextPart("artist", "Gang Starr"),
  TextPart("album", "Step In The Arena"),
  FilePart("media", new File("/music/gang_starr/form_of_intellect.m4a"))
)

// Create request with multipart body
val req = POST("https://upload.musiclibrary.com/songs").withMultipartBody(formData)
```

And for an incoming message with multipart form-data, there's a standard
`BodyParser` for parsing the message content.

```scala
import scamper.{ BodyParser, HttpRequest, Multipart }

def saveTrack(req: HttpRequest): Unit = {
  // Get parser for multipart message body
  implicit val parser = BodyParser.multipart()

  // Parse message to Multipart instance
  val multipart = req.as[Multipart]

  // Extracts content from the parts
  val title = multipart.getText("title")
  val artist = multipart.getText("artist")
  val album = multipart.getText("album")
  val track = multipart.getFile("media")

  ...
}
```

## Message Attributes

Attributes are arbitrary key-value pairs associated with a message.

```scala
import scala.concurrent.duration.{ Deadline, DurationInt }
import scamper.Implicits.stringToUri
import scamper.{ HttpRequest, HttpResponse }
import scamper.RequestMethod.Registry.GET

def send(req: HttpRequest): HttpResponse = ???

val req = GET("/motd").withAttribute("send-before" -> (Deadline.now + 1.minute))

val res = req.getAttribute[Deadline]("send-before")
  .filter(_.hasTimeLeft)
  .map(_ => send(req))
```

Note attributes are not included in the transmitted message.

## HTTP Authentication

**Scamper** includes a separate package (i.e., `scamper.auth`) for working with
authentication types and headers.

### Challenges and Credentials

When working with authentication, a `Challenge` is presented in the response,
and `Credentials` in the request. Each of these has an assigned scheme, which is
associated with either a token or a set of parameters.

```scala
import scamper.Implicits.stringToUri
import scamper.RequestMethod.Registry.GET
import scamper.ResponseStatus.Registry.Unauthorized
import scamper.auth.{ Authorization, Challenge, Credentials, WwwAuthenticate }

// Present response challenge (scheme and parameters)
val challenge = Challenge("Bearer", "realm" -> "developer")
val res = Unauthorized().withWwwAuthenticate(challenge)

// Present request credentials (scheme and token)
val credentials = Credentials("Bearer", "QWxsIEFjY2VzcyEhIQo=")
val req = GET("/dev/projects").withAuthorization(credentials)
```

_**Note:** The `Authorization` and `WwwAuthenticate` header classes are for
authentication between user agent and origin server. There are other header
classes available for proxy authentication &ndash; see
[scaladoc](https://losizm.github.io/scamper/latest/api/scamper/index.html) for
details._


### Basic Authentication

There are subclasses defined for Basic authentication: `BasicChallenge` and
`BasicCredentials`.

```scala
import scamper.Implicits.stringToUri
import scamper.RequestMethod.Registry.GET
import scamper.ResponseStatus.Registry.Unauthorized
import scamper.auth.{ Authorization, BasicChallenge, BasicCredentials, WwwAuthenticate }

// Provide realm and optional parameters
val challenge = BasicChallenge("admin", "title" -> "Admin Console")
val res = Unauthorized().withWwwAuthenticate(challenge)

// Provide user and password
val credentials = BasicCredentials("sa", "l3tm31n")
val req = GET("/admin/users").withAuthorization(credentials)
```

In addition, there are methods for Basic authentication defined in the header
classes.

```scala
import scamper.Implicits.stringToUri
import scamper.RequestMethod.Registry.GET
import scamper.ResponseStatus.Registry.Unauthorized
import scamper.auth.{ Authorization, WwwAuthenticate }

// Provide realm and optional parameters
val res = Unauthorized().withBasic("admin", "title" -> "Admin Console")

// Access basic auth in response
printf(s"Realm: %s%n", res.basic.realm)
printf(s"Title: %s%n", res.basic.params("title"))

// Provide user and password
val req = GET("/admin/users").withBasic("sa", "l3tm3m1n")

// Access basic auth in request
printf(s"User: %s%n", req.basic.user)
printf(s"Password: %s%n", req.basic.password)
```

### Bearer Authentication

There are subclasses defined for Bearer authentication: `BearerChallenge` and
`BearerCredentials`. And, similar to Basic, there are Bearer-specific methods
available in the header classes.

```scala
import scamper.Implicits.stringToUri
import scamper.RequestMethod.Registry.GET
import scamper.ResponseStatus.Registry.Unauthorized
import scamper.auth.{ Authorization, WwwAuthenticate }

// Provide challenge parameters
val res = Unauthorized().withBearer(
  "scope" -> "user profile",
  "error" -> "invalid_token",
  "error_description" -> "Expired access token"
)

// Print optional realm parameter
res.bearer.realm.foreach(println)

// Print scope from space-delimited parameter
val scope: Seq[String] = res.bearer.scope
scope.foreach(println)

// Print error parameters
res.bearer.error.foreach(println)
res.bearer.errorDescription.foreach(println)
res.bearer.errorUri.foreach(println)

// Test for error conditions
println(res.bearer.isInvalidToken)
println(res.bearer.isInvalidRequest)
println(res.bearer.isInsufficientScope)

// Create request with Bearer token
val req = GET("/users").withBearer("R290IDUgb24gaXQhCg==")

// Access bearer auth in request
printf("Token: %s%n", req.bearer.token)
```

## HTTP Client

**Scamper** provides `HttpClient` for sending requests and handling the
responses.

```scala
import scamper.Implicits.{ stringToEntity, stringToUri }
import scamper.RequestMethod.Registry.POST
import scamper.client.HttpClient
import scamper.client.ResponsePredicate._
import scamper.headers.{ ContentType, Location }
import scamper.types.Implicits.stringToMediaType

val req = POST("https://localhost:8080/users")
  .withContentType("application/json")
  .withBody(s"""{ "id": 500, "name": "guest" }""")

// Send request and print response status
HttpClient.send(req) {
  case Successful(_)    => println("Successful")
  case Redirection(res) => println(s"Redirection: ${res.location}")
  case ClientError(res) => println(s"Client error: ${res.status}")
  case ServerError(res) => println(s"Server error: ${res.status}")
  case Informational(_) => println("Informational")
}
```

Note the outgoing request must be created with an absolute URI to make effective
use of the client.

### Creating Client

In the previous example, the `HttpClient` object is used as the client. Behind
the scenes, this creates an `HttpClient` instance for one-time usage.

If you plan to send multiple requests, you can create and maintain a reference
to a client instance. With it, you also get access to methods corresponding to
standard HTTP request methods.

```scala
import scamper.BodyParser
import scamper.Implicits.stringToUri
import scamper.client.HttpClient

implicit val parser = BodyParser.text()

// Create client instance
val client = HttpClient()

def getMessageOfTheDay(): Either[Int, String] = {
  // Use client instance
  client.get("http://localhost:8080/motd") { res =>
    res.status.isSuccessful match {
      case true  => Right(res.as[String])
      case false => Left(res.status.code)
    }
  }
}
```

And if an implicit client is in scope, you can make use of `send()` on the
request itself.

```scala
import scamper.BodyParser
import scamper.Implicits.stringToUri
import scamper.RequestMethod.Registry.GET
import scamper.client.HttpClient
import scamper.client.Implicits.ClientHttpRequestType // Adds send method to request
import scamper.headers.{ Accept, AcceptLanguage }
import scamper.types.Implicits.{ stringToMediaRange, stringToLanguageRange }

implicit val client = HttpClient(bufferSize = 8192, readTimeout = 1000)
implicit val parser = BodyParser.text(4096)

GET("http://localhost:8080/motd")
  .withAccept("text/plain")
  .withAcceptLanguage("en-US; q=0.6", "fr-CA; q=0.4")
  .send(res => println(res.as[String])) // Send request and print response
```

### Configuring Client

You can also create a client using `ClientSettings`, which allows you to
configure the client before creating it.

```scala
import java.io.File
import scamper.Implicits.{ stringToEntity, stringToUri }
import scamper.client.HttpClient
import scamper.cookies.CookieStore
import scamper.types.Implicits.stringToContentCodingRange

// Build client from settings
val client = HttpClient.settings()
  .acceptEncodings("gzip", "deflate")
  .bufferSize(8192)
  .readTimeout(3000)
  .continueTimeout(1000)
  .cookieStore(CookieStore())
  .trust(new File("/path/to/truststore"))
  .create()

client.post("https://localhost:3000/messages", body = "Hello there!") { res =>
  if (!res.status.isSuccessful)
    throw new Exception(s"Message not posted: ${res.status.code}")
}
```

The `acceptEncodings` provides a list of content coding ranges, which are used
to set the `Accept-Encoding` header on each outgoing request.

The `bufferSize` is the size in bytes used for the client socket's send and
receive buffers.

The `readTimeout` sets how long (in milliseconds) a read on the client socket
blocks before a `SocketTimeoutException` is thrown.

The `continueTimeout` specifies how long (in milliseconds) the client waits
for a `100 Continue` response from the server before the client sends the
request body. This behavior is effected only if the request includes an
`Expect` header set to `100-Continue`.

The `cookieStore` is used to store cookies included in HTTP responses. Using the
cookie store, the client automatically adds the appropriate cookies to each
outgoing request.

You can supply a truststore using `trust`. Or, if greater control is required
for verifying secure connections, you can supply a trust manager instead.

```scala
import javax.net.ssl.TrustManager
import scamper.Implicits.stringToUri
import scamper.client.HttpClient

class SingleSiteTrustManager(address: String) extends TrustManager {
  ???
}

// Build client from settings
val client = HttpClient.settings()
  .readTimeout(5000)
  // Use supplied trust manager
  .trust(new SingleSiteTrustManager("192.168.0.2"))
  .create()

client.get("https://192.168.0.2:3000/messages") { res =>
  ???
}
```

### Adding Request and Response Filters

To perform common operations on client requests and their responses, you can add
filters to the client.

```scala
import scamper.Uri
import scamper.client.HttpClient
import scamper.client.Implicits._
import scamper.cookies._

val settings = HttpClient.settings()

settings.readTimeout(30 * 1000)

// Add request filter
settings.outgoing { req =>
  def findCookies(target: Uri): Seq[PlainCookie] = ???

  // Add cookies to request
  req.withCookies { findCookies(req.absoluteTarget) }
}

// Add response filter
settings.incoming { res =>
  def storeCookies(target: Uri, cookies: Seq[SetCookie]): Unit = ???

  // Store cookies from response
  storeCookies(res.absoluteTarget, res.cookies)
  res
}

// Create client
val client = settings.create()
```

Note you can add multiple request and response filters. If multiple filters are
added, each is executed in the order it is added. That is, request filters are
executed in order, and response filters are executed in order.

### Using WebSocket Client

The client instance can also be used as a WebSocket client.

```scala
HttpClient().websocket("ws://localhost:9090/hello") { session =>
  session.onText { message =>
    println(s"Received text message: $message")

    if (message.equalsIgnoreCase("bye"))
      session.close()
  }

  session.onPing { data =>
    println(s"Received ping message.")
    session.pong()
  }

  session.onPong { data =>
    println(s"Received pong message.")
  }

  session.onError { err =>
    println(s"Encountered error: $err")
    err.printStackTrace()
  }

  session.onClose { statusCode =>
    println(s"WebSocket connection closed: $statusCode")
  }

  session.idleTimeout(5000)
  session.open()
  session.send("Hello, server!")
}
```

In the above example, the client establishes a WebSocket connection to the
specified target URI. _(Note the `ws` scheme. For a secure connection, use `wss`
instead.)_

After the client and server perform the opening handshake, a `WebSocketSession`
is passed to the supplied handler. The handler then applies subsequent handlers
for various message types along with an error handler.

It then sets the session's idle timeout. If no messages are received in any 5
second span, the session will be closed automatically.

Before the session begins reading incoming messages, it must first be opened.
And, to kick things off, a simple text message is sent to the server.

See [WebSocketSession](https://losizm.github.io/scamper/latest/api/scamper/websocket/WebSocketSession.html)
in scaladoc for additional details.

## HTTP Server

**Scamper** includes an extensible server framework. To demonstrate, let's begin
with a simple example.

```scala
import scamper.Implicits.stringToEntity
import scamper.ResponseStatus.Registry.Ok
import scamper.server.HttpServer

val server = HttpServer.create(8080) { req =>
  Ok("Hello, world!")
}
```

This is as bare-bones as it gets. We create a server at port 8080, and on each
incoming request, we send a simple text message back to the client. Although
trite, it demonstrates how easy it is to get going.

We'll use the remainder of this documentation to describe what goes into
creating more practical applications.

### Server Application

To build a server, you begin with `ServerApplication`. This is a mutable
structure to which you apply changes to configure the server. Once the desired
settings are applied, you invoke one of several methods to create the server.

You can obtain an instance of `ServerApplication` from the `HttpServer`
object.

```scala
val app = HttpServer.app()
```

This gives you the default application as a starting point. With this in hand,
you can set the location of the server log.

```scala
app.logger(new File("/tmp/server.log"))
```

And there are performance-related settings that can be tweaked as well.

```scala
app.backlogSize(50)
app.poolSize(10)
app.queueSize(25)
app.bufferSize(8192)
app.readTimeout(3000)
app.headerLimit(100)
app.keepAlive(5, 10)
```

The **backlogSize** specifies the maximum number of incoming connections that
can wait for acceptance. Incoming connections that exceed this limit are
refused.

The **poolSize** specifies the maximum number of requests processed
concurrently.

The **queueSize** specifies the maximum number of requests permitted to wait for
processing. Incoming requests that exceed this limit are sent **503 Service
Unavailable**.

The **bufferSize** is the length in bytes of the buffer used when reading from
and writing to a socket.

The **readTimeout** controls how long a read from a socket blocks before it
times out, whereafter **408 Request Timeout** is sent to client.

The **headerLimit** sets the maximum number of request headers allowed. Incoming
requests that exceed this limit are sent **431 Request Header Fields Too Large**.

The **keepAlive** settings enable persistent connections using the specified
timeout (in seconds) and max requests per connection.

### Request Handlers

You define application-specific logic in instances of `RequestHandler` and add
them to the application. The request handler accepts an `HttpRequest` and
returns either an `HttpRequest` or an `HttpResponse`. If the handler does not
satisfy the request, it returns an `HttpRequest` so that the next handler has
its turn. Otherwise, it returns an `HttpResponse`, and any remaining handlers
are effectively ignored.

```scala
import scamper.RequestMethod.Registry.{ GET, HEAD }
import scamper.ResponseStatus.Registry.MethodNotAllowed
import scamper.headers.Allow

// Add handler to log request line and headers to stdout
app.incoming { req =>
  println(req.startLine)
  req.headers.foreach(println)
  println()
  req // Return request for next handler
}

// Add handler to allow GET and HEAD requests only
app.incoming { req =>
  (req.method == GET || req.method == HEAD) match {
    // Return request for next handler
    case true  => req
    // Otherwise return response to end request chain
    case false => MethodNotAllowed().withAllow(GET, HEAD)
  }
}
```

Note the order in which handlers are applied matters. For instance, in the
example above, you'd swap the order of handlers if you wanted to log GET and
HEAD requests only, and all other requests would immediately be sent
**405 Method Not Allowed** and never make it to the request logger.

Also note a request handler is not restricted to returning the same request it
accepted.

```scala
import scamper.BodyParser
import scamper.Implicits.stringToEntity
import scamper.RequestMethod.Registry.POST
import scamper.headers.ContentLanguage
import scamper.types.LanguageTag
import scamper.types.Implicits.stringToLanguageTag

// Translates message body from French (Oui, oui.)
app.incoming { req =>
  val translator: BodyParser[String] = ???

  (req.method == POST && req.contentLanguage.contains("fr")) match {
    case true  => req.withBody(translator.parse(req)).withContentLanguage("en")
    case false => req
  }
}
```

#### Targeted Handling

A handler can be added to a targeted path with or without a targeted request
method.

```scala
import scamper.Implicits.stringToEntity
import scamper.RequestMethod.Registry.GET
import scamper.ResponseStatus.Registry.{ Forbidden, Ok }

// Match request method and exact path
app.incoming(GET, "/about") { req =>
  Ok("This server is powered by Scamper.")
}

// Match exact path and any method
app.incoming("/private") { req =>
  Forbidden()
}
```

And handlers can be added using methods corresponding to the standard HTTP
request methods.

```scala
import scamper.Implicits.stringToUri
import scamper.ResponseStatus.Registry.{ Created, Ok }
import scamper.headers.Location

// Match GET requests to given path
app.get("/about") { req =>
  Ok("This server is powered by Scamper.")
}

// Match POST requests to given path
app.post("/messages") { req =>
  def post(message: String): Int = ???

  implicit val parser = BodyParser.text()

  val id = post(req.as[String])
  Created().withLocation(s"/messages/$id")
}
```

#### Path Parameters

Parameters can be specified in the path and their resolved values made available
to the handler. When a parameter is specified as __:param__, it matches a single
path segment; whereas, __*param__ matches the path segment along with any
remaining segments, including intervening path separators (i.e., **/**).

```scala
import scamper.Implicits.fileToEntity
import scamper.ResponseStatus.Registry.{ Accepted, NotFound, Ok }
import scamper.server.Implicits.ServerHttpRequestType

// Match request method and parameterized path
app.delete("/orders/:id") { req =>
  def deleteOrder(id: Int): Boolean = ???

  // Get resolved parameter
  val id = req.params.getInt("id")

  deleteOrder(id) match {
    case true  => Accepted()
    case false => NotFound()
  }
}

// Match prefixed path with any request method
app.get("/archive/*path") { req =>
  def findFile(path: String): Option[File] = ???

  // Get resolved parameter
  val path = req.params.getString("path")

  findFile(path).map(Ok(_)).getOrElse(NotFound())
}
```

Note there can be at most one __*param__, which must be specified as the the
last segment in the path; however, there can be multiple __:param__ instances
specified.

```scala
import scamper.BodyParser
import scamper.Implicits.stringToEntity
import scamper.ResponseStatus.Registry.Ok
import scamper.server.Implicits.ServerHttpRequestType

// Match path with two parameters
app.post("/translate/:in/to/:out") { req =>
  def translator(from: String, to: String): BodyParser[String] = ???

  val from = req.params.getString("in")
  val to = req.params.getString("out")

  Ok(translator(from, to).parse(req))
}
```

#### Serving Static Files

You can add a request handler at a mount path to serve static files from a
source directory.

```scala
app.files("/app/main", new File("/path/to/public"))
```

This adds a handler to serve files from the directory at _/path/to/public_. The
files are mapped based on the request path excluding the mount path. For example,
_http://localhost:8080/app/main/images/logo.png_ would map to
_/path/to/public/images/logo.png_.

#### Serving Static Resources

If your web assets are bundled in a jar file, just drop the jar on the
classpath, and you can configure the application to serve its contents.

```scala
app.resources("/app/main", "assets")
```

In the above configuration, requests prefixed with _/app/main_ are served
resources from the _assets_ directory. The mapping works similiar to static
files, only the resources are located using a class loader. _(See
[ServerApplication.resources()](https://losizm.github.io/scamper/latest/api/scamper/server/ServerApplication.html#resources(mountPath:String,sourceDirectory:String,classLoader:ClassLoader):ServerApplication.this.type)
in scaladoc for additional details.)_

#### Aborting Response

At times, you may wish to omit a response for a particular request. On such
occassions, you'd throw `ResponseAborted` from the request handler.

```scala
import scamper.headers.Referer
import scamper.server.ResponseAborted

// Ignore requests originating from evil site
app.incoming { req =>
  if (req.referer.getHost == "www.phishing.com")
    throw ResponseAborted("Not trusted")
  req
}
```

### WebSocket Session

As a special case of request handling, you can define a WebSocket endpoint and
manage the session. The server takes care of the opening handshake and passes
the session to your handler.

```scala
app.websocket("/hello") { session =>
  // Log ping message and send corresponding pong
  session.onPing { data =>
    session.logger.info("Received ping message.")
    session.pong()
  }

  // Log pong message
  session.onPong { data =>
    session.logger.info("Received pong message.")
  }

  // Log text message and close session after sending reply
  session.onText { message =>
    session.logger.info(s"Received text message: $message")
    session.send("Goodbye.")
    session.close()
  }

  // Log status code when session is closed
  session.onClose { status =>
    session.logger.info(s"Session closed: $status")
  }

  // Open session to incoming messages
  session.open()

  // Send ping message
  session.ping()
}
```

See [WebSocketSession](https://losizm.github.io/scamper/latest/api/scamper/websocket/WebSocketSession.html)
in scaladoc for additional details.

### Error Handler

You can define an `ErrorHandler` to handle exceptions thrown from your request
handlers.

```scala
import scamper.ResponseStatus.Registry.{ BadRequest, InternalServerError }

// Accepts Throwable and HttpRequest; returns HttpResponse
app.error { (err, req) =>
  def isClientError(err: Throwable): Boolean = ???

  isClientError(err) match {
    case true  => BadRequest("Your bad.")
    case false => InternalServerError("My bad.")
  }
}
```

### Router

Use `Router` to structure the application routes hierarchically. `Router` works
in much the same way as `ServerApplication`, except it is configured for request
handling only, and all router paths are relative to its mount path.

```scala
import scamper.Implicits.stringToEntity
import scamper.ResponseStatus.Registry.{ NotFound, Ok }
import scamper.server.HttpServer
import scamper.server.Implicits.ServerHttpRequestType

val app = HttpServer.app()

// Mount router to /api
app.use("/api") { router =>
  val messages = Map(1 -> "Hello, world!", 2 -> "Goodbye, cruel world!")

  // Map handler to /api/messages
  router.get("/messages") { req =>
    Ok(messages.mkString("\r\n"))
  }

  // Map handler to /api/messages/:id
  router.get("/messages/:id") { req =>
    val id = req.params.getInt("id")
    messages.get(id)
     .map(Ok(_))
     .getOrElse(NotFound())
  }
}
```

### Response Filters

Response filtering is performed by adding instances of `ResponseFilter` to the
application. They are applied, in order, after one of the request handlers
generates a response.

```scala
app.outgoing { res =>
  println(res.startLine)
  res.headers.foreach(println)
  println()
  res // Return response for next filter
}
```

This is pretty much the same as the request logger from earlier, only instead of
`HttpRequest`, it accepts and returns `HttpResponse`.

And the filter is not restricted to returning the same response it accepts.

```scala
import scamper.server.Implicits.ServerHttpResponseType

// Gzip response body if not empty
app.outgoing { res =>
  res.body.isKnownEmpty match {
    case true  => res
    case false => res.withGzipContentEncoding()
  }
}
```

### Securing Server

The last piece of configuration is whether to secure the server using SSL/TLS.
To use a secure transport, you must supply an appropriate key and certificate.

```scala
app.secure(new File("/path/to/private.key"), new File("/path/to/public.cert"))
```

Or, if you have them tucked away in a keystore, you can supply the keystore
location.

```scala
// Supply location, password, and store type (i.e., JKS, JCEKS, PCKS12)
app.secure(new File("/path/to/keystore"), "s3cr3t", "pkcs12")
```

### Creating Server

When the desired application has been configured, you're ready to create the
server.

```scala
val server = app.create(8080)
```

If the server must bind to a particular host, you can provide the host name or
IP address.

```scala
val server = app.create("192.168.0.2", 8080)
```

An instance of `HttpServer` is returned, which can be used to query server
details.

```scala
printf("Host: %s%n", server.host)
printf("Port: %d%n", server.port)
printf("Secure: %s%n", server.isSecure)
printf("Logger: %s%n", server.logger)
printf("Backlog Size: %d%n", server.backlogSize)
printf("Pool Size: %d%n", server.poolSize)
printf("Queue Size: %d%n", server.queueSize)
printf("Buffer Size: %d%n", server.bufferSize)
printf("Read Timeout: %d%n", server.readTimeout)
printf("Header Limit: %d%n", server.headerLimit)
printf("Keep-Alive: %s%n", server.keepAlive.getOrElse("disabled"))
printf("Closed: %s%n", server.isClosed)
```

And, ultimately, it is used to gracefully shut down the server.

```scala
server.close() // Good-bye, world.
```

## API Documentation

See [scaladoc](https://losizm.github.io/scamper/latest/api/scamper/index.html)
for additional details.

## License
**Scamper** is licensed under the Apache License, Version 2. See LICENSE file
for more information.
