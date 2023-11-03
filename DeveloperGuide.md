# Scamper &ndash; Developer Guide

## Table of Contents

- [Introduction](#Introduction)
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
  - [Configuring Client](#Configuring-Client)
  - [Adding Request and Response Filters](#Adding-Request-and-Response-Filters)
  - [Using WebSocket Client](#Using-WebSocket-Client)
- [HTTP Server](#HTTP-Server)
  - [Server Application](#Server-Application)
  - [Request Handlers](#Request-Handlers)
  - [WebSocket Session](#WebSocket-Session)
  - [Error Handler](#Error-Handler)
  - [Router](#Router)
  - [Response Filters](#Response-Filters)
  - [Securing Server](#Securing-Server)
  - [Lifecycle Hooks](#Lifecycle-Hooks)
  - [More Lifecycle Hooks](#More-Lifecycle-Hooks)
  - [Creating Server](#Creating-Server)
- [API Documentation](#API-Documentation)
- [License](#License)

## Introduction

In here, we survey Scamper's core features in top to bottom fashion.

See [README](README.md) for details on getting started and for a quick view at
what the library offers.

## HTTP Messages

At the core of **Scamper** is `HttpMessage`, which defines the fundamental
characteristics of an HTTP message. `HttpRequest` and `HttpResponse`
extend the specification to define characteristics specific to their respective
message types.

### Building Requests

An `HttpRequest` can be created using a factory method defined in the companion
object, or you can start building from a `RequestMethod`.

```scala
import scala.language.implicitConversions

import scamper.http.RequestMethod.Registry.Get
import scamper.http.headers.{ toAccept, toHost }
import scamper.http.stringToUri
import scamper.http.types.stringToMediaRange

val req = Get("/motd")
  .setHost("localhost:8080")
  .setAccept("text/plain", "*/*; q=0.5")
```

### Building Responses

An `HttpResponse` can be created using a factory method defined in the companion
object, or you can start building from a `ResponseStatus`.

```scala
import scala.language.implicitConversions

import scamper.http.ResponseStatus.Registry.Ok
import scamper.http.headers.{ toConnection, toContentType }
import scamper.http.stringToEntity
import scamper.http.types.stringToMediaType

val res = Ok("There is an answer.")
  .setContentType("text/plain")
  .setConnection("close")
```

## Specialized Header Access

`HttpMessage` provides a set of methods for generalized header access. Using
these methods, the header name and value are each `String` values.

```scala
import scala.language.implicitConversions

import scamper.http.RequestMethod.Registry.Post
import scamper.http.{ stringToUri, tupleToHeader }

val req = Post("/api/users").setHeaders("Content-Type" -> "application/json")

val contentType: Option[String] = req.getHeaderValue("Content-Type")
```

Although this gets the job done, there are classes and conversions defined in
`scamper.http.headers` for specialized header access.

For example, here is `ContentType` with implementation details elided:

```scala
/** Adds standardized access to Content-Type header. */
class ContentType[T <: HttpMessage](message: T) extends AnyVal:
  /** Tests for Content-Type header. */
  def hasContentType: Boolean

  /** Gets Content-Type header value. */
  def contentType: MediaType

  /** Gets optional Content-Type header value. */
  def contentTypeOption: Option[MediaType]

  /** Creates new message with Content-Type header set to supplied value. */
  def setContentType(value: MediaType): T

  /** Creates new message with Content-Type header removed. */
  def contentTypeRemoved: T
```

By applying the `toContentType` conversion, you can work with the header using
its specialized header type.

```scala
import scala.language.implicitConversions

import scamper.http.RequestMethod.Registry.Post
import scamper.http.headers.toContentType
import scamper.http.stringToUri
import scamper.http.types.MediaType

val req = Post("/api/users").setContentType(MediaType("application/json"))
println(req.contentType.typeName) // application
println(req.contentType.subtypeName) // json
```

And, with conversions in `scamper.http.types`, you can implicitly convert values
to header types.

```scala
import scala.language.implicitConversions

import scamper.http.RequestMethod.Registry.Post
import scamper.http.headers.toContentType
import scamper.http.stringToUri
import scamper.http.types.stringToMediaType

val req = Post("/api/users").setContentType("application/json")
println(req.contentType.typeName) // application
println(req.contentType.subtypeName) // json
```

## Specialized Cookie Access

In much the same way specialized access to headers is available, so too is
the case for cookies. Specialized access is provided by classes and conversions
in `scamper.http.cookies`.

### Request Cookies

In `HttpRequest`, cookies are stringed together in the **Cookie** header. You
can access the cookies in their _unbaked_ form using generalized header access.
Or, you can access them using extension methods provided by `RequestCookies`
with each cookie represented as `PlainCookie`.

```scala
import scala.language.implicitConversions

import scamper.http.RequestMethod.Registry.Get
import scamper.http.cookies.{ PlainCookie, toRequestCookies }
import scamper.http.stringToUri

val req = Get("https://localhost:8080/motd").setCookies(
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
Specialized access is provided by `ResponseCookies` with each cookie represented
as `SetCookie`.

```scala
import scala.language.implicitConversions

import scamper.http.ResponseStatus.Registry.Ok
import scamper.http.cookies.{ SetCookie, toResponseCookies }
import scamper.http.stringToEntity

val res = Ok("There is an answer.").setCookies(
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
Whereas `getHeaderValue()` retrieves the first header value only,
`getHeaderValues()` collects all header values into a `Seq[String]`._

## Message Body

The message body is represented as an `Entity`, which provides access to its
data via an `InputStream`.

### Creating Body

When building a message, use an `Entity` factory to create the body. For
example, you can create a message with text content.

```scala
import scala.language.implicitConversions

import scamper.http.Entity
import scamper.http.ResponseStatus.Registry.Ok
import scamper.http.headers.toContentType
import scamper.http.types.stringToMediaType

val htmlBody = Entity("""
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
val res = Ok(htmlBody).setContentType("text/html; charset=utf-8")
```

Or, create a message using file content.

```scala
import scala.language.implicitConversions

import java.io.File
import scamper.http.Entity
import scamper.http.ResponseStatus.Registry.Ok
import scamper.http.headers.toContentType
import scamper.http.types.stringToMediaType

val fileBody = Entity(File("./index.html"))
val res = Ok(fileBody).setContentType("text/html; charset=utf-8")
```

There are implicit conversions available for common entity types, so you aren't
required to create them explicitly.

```scala
import scala.language.implicitConversions

import java.io.File
import scamper.http.ResponseStatus.Registry.Ok
import scamper.http.fileToEntity
import scamper.http.headers.toContentType
import scamper.http.types.stringToMediaType

val res = Ok(File("./index.html")).setContentType("text/html; charset=utf-8")
```

### Parsing Body

When handling an incoming message, use an appropriate `BodyParser` to parse the
message body. There are factory methods available, such as one used for creating
a parser that parses the message body to a `String`.

```scala
import scamper.http.{ BodyParser, HttpMessage }

// Create text body parser
given BodyParser[String] = BodyParser.string(maxLength = 1024)

def printText(message: HttpMessage): Unit =
  // Parse message as String using given parser
  val text = message.as[String]

  println(text)
```

And, you can implement your own. Here's one for JSON powered by
[Grapple](https://github.com/losizm/grapple):

```scala
import scala.language.implicitConversions

import grapple.json.{ Json, JsonInput, given }

import scamper.http.{ BodyParser, HttpMessage }
import scamper.http.RequestMethod.Registry.Post

case class User(id: Int, name: String)

// Define how to parse JSON body to user
given BodyParser[User] with
  given JsonInput[User] = json => User(json("id"), json("name"))

  def parse(message: HttpMessage): User =
    Json.parse(message.body.data)

// Create test message with JSON body
val request = Post("/users").setBody("""{ "id": 1000, "name": "lupita" }""")

// Parse message as User
val user = request.as[User]

assert(user.id == 1000)
assert(user.name == "lupita")
```

## Multipart Message Body

You can create a message with multipart form data, which is generally required for
form submission containing file content.

```scala
import java.io.File

import scamper.http.RequestMethod.Registry.Post
import scamper.http.multipart.*

// Build multipart form data with text and file content
val song = Multipart(
  Part("title", "Form Of Intellect"),
  Part("artist", "Gang Starr"),
  Part("album", "Step In The Arena"),
  Part("media", File("/music/gang_starr/form_of_intellect.mp3"))
)

// Create request with multipart body
val req = Post("https://musiclibrary.example.com/songs").setMultipartBody(song)
```

And, there's a standard `BodyParser` for reading an incoming message with a
multipart body.

```scala
import scamper.http.{ BodyParser, HttpRequest }
import scamper.http.multipart.Multipart

def save(req: HttpRequest): Unit =
  // Get parser for multipart message body
  given BodyParser[Multipart] = Multipart.bodyParser()

  // Parse message to Multipart instance
  val song = req.as[Multipart]

  // Extracts content from parts
  val title  = song.getString("title")
  val artist = song.getString("artist")
  val album  = song.getString("album")
  val media  = song.getFile("media")

  ...
```

## Message Attributes

Attributes are arbitrary key/value pairs associated with a message. They can be
used in message processing; however, they are not included in the transmitted
message.

```scala
import scala.concurrent.duration.{ Deadline, DurationInt }
import scamper.http.{ HttpRequest, HttpResponse }
import scamper.http.RequestMethod.Registry.Get

def send(req: HttpRequest): HttpResponse = ???

val req = Get("/motd").setAttributes("send-before" -> (Deadline.now + 1.minute))

val res = req.getAttribute[Deadline]("send-before")
  .filter(_.hasTimeLeft())
  .map(_ => send(req))
```

## HTTP Authentication

The `scamper.http.auth` package is included for working with authentication
headers.

### Challenges and Credentials

When working with authentication, a `Challenge` is presented in the response,
and `Credentials` in the request. Each of these has an assigned scheme, which is
associated with either a token or a set of parameters.

```scala
import scala.language.implicitConversions

import scamper.http.RequestMethod.Registry.Get
import scamper.http.ResponseStatus.Registry.Unauthorized
import scamper.http.auth.{ *, given }

// Present response challenge (scheme and parameters)
val challenge = Challenge("Bearer", "realm" -> "developer")
val res = Unauthorized().setWwwAuthenticate(challenge)

// Present request credentials (scheme and token)
val credentials = Credentials("Bearer", "QWxsIEFjY2VzcyEhIQo=")
val req = Get("/dev/projects").setAuthorization(credentials)
```

### Basic Authentication

There are subclasses defined for Basic authentication: `BasicChallenge` and
`BasicCredentials`.

```scala
import scala.language.implicitConversions

import scamper.http.RequestMethod.Registry.Get
import scamper.http.ResponseStatus.Registry.Unauthorized
import scamper.http.auth.{ *, given }

// Provide realm and optional parameters
val challenge = BasicChallenge("admin", "title" -> "Admin Console")
val res = Unauthorized().setWwwAuthenticate(challenge)

// Provide user and password
val credentials = BasicCredentials("sa", "l3tm31n")
val req = Get("/admin/users").setAuthorization(credentials)
```

In addition, there are methods for Basic authentication defined in the header
classes.

```scala
import scala.language.implicitConversions

import scamper.http.RequestMethod.Registry.Get
import scamper.http.ResponseStatus.Registry.Unauthorized
import scamper.http.auth.{ *, given }

// Provide realm and optional parameters
val res = Unauthorized().setBasic("admin", "title" -> "Admin Console")

// Access basic auth in response
printf(s"Realm: %s%n", res.basic.realm)
printf(s"Title: %s%n", res.basic.params("title"))

// Provide user and password
val req = Get("/admin/users").setBasic("sa", "l3tm3m1n")

// Access basic auth in request
printf(s"User: %s%n", req.basic.user)
printf(s"Password: %s%n", req.basic.password)
```

### Bearer Authentication

There are subclasses defined for Bearer authentication: `BearerChallenge` and
`BearerCredentials`. In addition, there are Bearer-specific methods available in
the header classes.

```scala
import scala.language.implicitConversions

import scamper.http.RequestMethod.Registry.Get
import scamper.http.ResponseStatus.Registry.Unauthorized
import scamper.http.auth.{ *, given }

// Provide challenge parameters
val res = Unauthorized().setBearer(
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
val req = Get("/users").setBearer("R290IDUgb24gaXQhCg==")

// Access bearer auth in request
printf("Token: %s%n", req.bearer.token)
```

## HTTP Client

**Scamper** provides `HttpClient` for sending requests and handling responses.

```scala
import scala.language.implicitConversions

import scamper.http.RequestMethod.Registry.Post
import scamper.http.client.HttpClient
import scamper.http.headers.toContentType
import scamper.http.types.stringToMediaType

val httpClient = HttpClient()

val req = Post("https://localhost:8080/users")
  .setContentType("application/json")
  .setBody(s"""{ "id": 65534, "name": "nobody" }""")

// Send request and print response status
httpClient.send(req) { res => println(res.status) }
```

In the previous example, an instance of `HttpRequest` is explicitly created and
passed to the client. However, `HttpClient` provides methods corresponding to
standard HTTP request methods that can be used instead of explicitly building
the request first.

```scala
import scala.language.implicitConversions

import scamper.http.{ BodyParser, stringToUri }
import scamper.http.client.HttpClient

given BodyParser[String] = BodyParser.string()

// Create client instance
val httpClient = HttpClient()

def messageOfTheDay: Either[Int, String] =
  httpClient.get("http://localhost:8080/motd") {
    case res if res.isSuccessful => Right(res.as[String])
    case res                     => Left(res.statusCode)
  }
```

### Configuring Client

You can also create a client using `ClientSettings`, which allows you to
configure the client before creating it.

```scala
import scala.language.implicitConversions

import java.io.File
import scamper.http.{ stringToEntity, stringToUri }
import scamper.http.client.HttpClient
import scamper.http.cookies.CookieStore
import scamper.http.types.{ stringToContentCodingRange, stringToMediaRange }

// Build client from settings
val httpClient = HttpClient.settings()
  .resolveTo("localhost", 8080, secure = false)
  .accept("text/plain; q=0.9", "application/json; q=0.1")
  .acceptEncoding("gzip", "deflate")
  .bufferSize(8192)
  .readTimeout(3000)
  .continueTimeout(1000)
  .cookies(CookieStore())
  .keepAlive(true)
  .trust(File("/path/to/truststore"))
  .toHttpClient()

httpClient.post("/messages", body = "Hello there!") { res =>
  assert(res.isSuccessful, s"Message not posted: ${res.statusCode}")
}
```

The `resolveTo` setting provides a default authority to resolve relative request
URIs.

The `accept` setting provides a list of accepted media types, which are used to
set the **Accept** header on each outgoing request.

The `acceptEncoding` provides a list of accepted content encodings, which are
used to set the **Accept-Encoding** header on each outgoing request.

The `bufferSize` is the size in bytes used for the client send and receive
buffers.

The `readTimeout` sets how many milliseconds a read on the client socket blocks
before a `SocketTimeoutException` is thrown.

The `continueTimeout` specifies how many milliseconds the client waits for a
_100 (Continue)_ response from the server before the client sends the
request body. This behavior is effected only if the request includes an
**Expect** header set to _100-Continue_.

The cookie store supplied to `cookies` is used to store cookies included in HTTP
responses. Using the cookie store, the client automatically adds the appropriate
cookies to each outgoing request.

The `keepAlive` setting is used to enable persistence connections, which are
disabled by default.

You can supply a truststore in `trust`, as demonstrated in the previous example.
Or, if greater control is required, you can supply a trust manager instead.

```scala
import scala.language.implicitConversions

import javax.net.ssl.TrustManager
import scamper.http.stringToUri
import scamper.http.client.ClientSettings

class SingleSiteTrustManager(address: String) extends TrustManager {
  // Provide TrustManager implementation
  ???
}

// Build client from settings
val httpClient = ClientSettings()
  .readTimeout(5000)
  // Use supplied trust manager
  .trust(SingleSiteTrustManager("192.168.0.2"))
  .toHttpClient()

httpClient.get("https://192.168.0.2:3000/messages") { res =>
  res.drain(System.out, 16 * 1024)
}
```

### Adding Request and Response Filters

To perform common operations on client requests and their responses, you can add
filters to the client.

```scala
import scala.language.implicitConversions

import scamper.http.Uri
import scamper.http.client.{ *, given }
import scamper.http.cookies.{ *, given }

val settings = HttpClient.settings()

settings.readTimeout(30 * 1000)

// Add request filter
settings.outgoing { req =>
  def findCookies(target: Uri): Seq[PlainCookie] = ???

  // Add cookies to request
  req.setCookies { findCookies(req.absoluteTarget) }
}

// Add response filter
settings.incoming { res =>
  def storeCookies(target: Uri, cookies: Seq[SetCookie]): Unit = ???

  // Store cookies from response
  storeCookies(res.absoluteTarget, res.cookies)
  res
}

// Create client
val httpClient = settings.toHttpClient()
```

You can add multiple request and response filters. If multiple filters are
added, each is executed in the order it is added. That is, request filters are
executed in order, and response filters are executed in order.

### Using WebSocket Client

The client instance can also be used as a WebSocket client.

```scala
import scala.language.implicitConversions

import scamper.http.stringToUri
import scamper.http.client.HttpClient

HttpClient().websocket("ws://localhost:9090/hello") { session =>
  session.onText { message =>
    println(s"Received text message: $message")

    if message.equalsIgnoreCase("bye") then
      session.close()
  }

  session.onPing { data =>
    println(s"Received ping message.")
    session.pong(data)
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
specified target URI. _(Note use of "ws" scheme. For secure connections, use
"wss" instead.)_

After the client and server perform the opening handshake, a `WebSocketSession`
is passed to the supplied handler. The handler then applies subsequent handlers
for various message types along with an error handler.

It then sets the session's idle timeout. If no messages are received in any 5
second span, the session will be closed automatically.

Before the session begins reading incoming messages, it must first be opened.

And, to kick things off, a simple text message is sent to the server.

See [WebSocketSession](https://losizm.github.io/scamper/latest/api/scamper/http/websocket.html)
in scaladoc for additional details.

## HTTP Server

**Scamper** includes an extensible server framework. To demonstrate, let's begin
with a venerable _Hello World_:

```scala
import scamper.http.ResponseStatus.Registry.Ok
import scamper.http.server.ServerApplication

val server = ServerApplication()
  .incoming { req => Ok("Hello, world!") }
  .toHttpServer(8080)
```

This is as bare-bones as it gets. We create a server at port 8080 that sends a
simple message back to the client for each incoming request. Although trite, it
shows how easy it is to get going.

We'll use the remainder of this documentation to describe what goes into
creating more practical applications.

### Quick Housekeeping

For the remaining code snippets, the following imports are assumed:

```scala
import java.io.File

import scala.language.implicitConversions

import scamper.http.{ *, given }
import scamper.http.RequestMethod.*
import scamper.http.ResponseStatus.*
import scamper.http.headers.given
import scamper.http.server.{ *, given }
import scamper.http.types.{ *, given }
```

### Server Application

To build a server, you begin with `ServerApplication`. This is a mutable
structure to which you apply changes to configure the server. Once the desired
settings are applied, you invoke one of several methods to create the server.

```scala
val app = ServerApplication()
```

This gives you the default application as a starting point. With this in hand,
you can adjust the server settings.

```scala
app.backlogSize(50)
app.poolSize(10)
app.queueSize(25)
app.bufferSize(8192)
app.readTimeout(3000)
app.headerLimit(100)
app.keepAlive(5, 10)
```

The `backlogSize` specifies the maximum number of incoming connections that
can wait for acceptance. Incoming connections that exceed this limit are
refused.

The `poolSize` specifies the maximum number of requests processed
concurrently.

The `queueSize` specifies the maximum number of requests permitted to wait for
processing. Incoming requests that exceed this limit are sent _503 (Service
Unavailable)_.

The `bufferSize` is the length in bytes of the buffer used when reading from
and writing to a socket.

The `readTimeout` controls how many milliseconds a read from a socket blocks
before it times out, whereafter _408 (Request Timeout)_ is sent to client.

The `headerLimit` sets the maximum number of request headers allowed. Incoming
requests that exceed this limit are sent _431 (Request Header Fields Too Large)_.

The `keepAlive` settings enable persistent connections using the specified
idle timeout seconds and max requests per connection.

### Request Handlers

You define application-specific logic in instances of `RequestHandler` and add
them to the application. The request handler accepts an `HttpRequest` and
returns either an `HttpRequest` or an `HttpResponse`. The handler returns an
`HttpRequest` if it doesn't satisfy the incoming request, allowing the next
handler to have its turn. Otherwise, it returns an `HttpResponse`, and any
remaining handlers are effectively ignored.

```scala
// Add handler to log request line and headers to stdout
app.incoming { req =>
  println(req.startLine)
  req.headers.foreach(println)
  println()
  req // Return request for next handler
}

// Add handler to allow GET and HEAD requests only
app.incoming { req =>
  req.isGet || req.isHead match
    // Return request for next handler
    case true  => req
    // Return response to end request chain
    case false => MethodNotAllowed().setAllow(Get, Head)
}
```

The order in which handlers are applied matters. For instance, in the example
above, you'd swap the order of handlers if you wanted to log GET and HEAD
requests only, and all other requests would immediately be sent
_405 (Method Not Allowed)_ and never make it to the request logger.

And, a request handler is not restricted to returning the same request it
accepted.

```scala
// Translate message body from French (Oui, oui.)
app.incoming { req =>
  given BodyParser[String] with
    val parser = BodyParser.string()

    def parse(msg: HttpMessage) =
      msg.as(using parser).replaceAll("\boui\b", "yes")

  req.isPost && req.contentLanguage.contains("fr") match
    case true  => req.setBody(req.as[String]).setContentLanguage("en")
    case false => req
}
```

#### Target Handling

A handler can be added to a target path with or without a target request method.

```scala
// Match request method and exact path
app.incoming("/about", Get) { req =>
  Ok("This server is powered by Scamper.")
}

// Match exact path and any method
app.incoming("/private") { req =>
  Forbidden()
}
```

And, handlers can be added using methods corresponding to the standard HTTP
request methods.

```scala
import java.util.concurrent.atomic.AtomicInteger

import scala.collection.concurrent.TrieMap

// Match GET requests to given path
app.get("/motd") { req =>
  Ok("She who laughs last laughs best.")
}

// Match POST requests to given path
app.post("/messages") { req =>
  val messages = TrieMap[Int, String]()
  val count    = AtomicInteger(0)

  def post(message: String): Int =
    val id = count.incrementAndGet()
    messages += id -> message
    id

  given BodyParser[String] = BodyParser.string()

  val id = post(req.as[String])
  Created().setLocation(s"/messages/$id")
}
```

#### Path Parameters

Parameters can be specified in the path and their resolved values made available
to the handler. When a parameter is specified as __:param__, it matches a single
path segment; whereas, __*param__ matches the path segment along with any
remaining segments, including intervening path separators (i.e., **/**).

```scala
// Match request method and parameterized path
app.delete("/orders/:id") { req =>
  def deleteOrder(id: Int): Boolean = ???

  // Get resolved parameter
  val id = req.pathParams.getInt("id")

  deleteOrder(id) match
    case true  => Accepted()
    case false => NotFound()
}

// Match prefixed path for GET requests
app.get("/archive/*path") { req =>
  def findFile(path: String): Option[File] = ???

  // Get resolved parameter
  val path = req.pathParams.getString("path")

  findFile(path).map(Ok(_)).getOrElse(NotFound())
}
```

There can be at most one __*param__, which must be specified as the the last
segment in the path; however, there can be multiple __:param__ instances
specified.

```scala
// Match path with two parameters
app.post("/translate/:in/to/:out") { req =>
  def translate(from: String, to: String): BodyParser[String] = ???

  val from   = req.pathParams.getString("in")
  val to     = req.pathParams.getString("out")
  val result = req.as(using translate(from, to))

  Ok(result)
}
```

#### Serving Files

You can mount a file server as a specialized request handler.

```scala
app.files("/app/main", File("/path/to/public"))
```

This adds a handler to serve files from the directory at _/path/to/public_.
Files are located in the source directory by stripping the mount path from the
request path. For example, _http://localhost:8080/app/main/images/logo.png_
would map to _/path/to/public/images/logo.png_.

#### Aborting Response

At times, you may wish to omit a response to a particular request. On such
occassions, you'd throw `ResponseAborted` from the request handler.

```scala
// Ignore requests originating from evil site
app.incoming { req =>
  if req.referer.host == "www.phishing.com" then
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
    println("Received ping message.")
    session.pong(data)
  }

  // Log pong message
  session.onPong { data =>
    println("Received pong message.")
  }

  // Log text message and close session after sending reply
  session.onText { message =>
    println(s"Received text message: $message")
    session.send("Goodbye.")
    session.close()
  }

  // Log status code when session is closed
  session.onClose { status =>
    println(s"Session closed: $status")
  }

  // Open session to incoming messages
  session.open()

  // Send ping message
  session.ping()
}
```

See [WebSocketSession](https://losizm.github.io/scamper/latest/api/scamper/http/websocket/WebSocketSession.html)
in scaladoc for additional details.

### Error Handler

You can define an `ErrorHandler` to handle exceptions thrown from your request
handlers.

```scala
app.recover {
  def isClientError(err: Throwable) =
    err.isInstanceOf[NumberFormatException]

  // Define implementation of ErrorHandler
  req => isClientError(_) match
    case true  => BadRequest("Your bad.")
    case false => InternalServerError("My bad.")
}
```

### Router

Use `Router` to structure the application routes hierarchically. `Router` works
in much the same way as `ServerApplication` with all routes relative to its
mount path.

```scala
// Mount router to /api
val app = ServerApplication()

app.route("/api") { router =>
  val messages = Map(1 -> "Hello, world!", 2 -> "Goodbye, cruel world!")

  // Map handler to /api/messages
  router.get("/messages") { req =>
    Ok(messages.mkString("\n"))
  }

  // Map handler to /api/messages/:id
  router.get("/messages/:id") { req =>
    val id = req.pathParams.getInt("id")
    messages.get(id)
     .map(Ok(_))
     .getOrElse(NotFound())
  }

  // Filter responses generated from router
  router.outgoing { res =>
    res.setContentType("text/plain")
  }

  // Recover from errors generated from router
  router.recover { req =>
    { case _: ParameterNotConvertible => BadRequest(req.target.toString) }
  }
}
```

### Response Filters

Response filtering is performed by adding instances of `ResponseFilter` to the
application. They are applied, in order, after a request handler generates a
response.

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

And, the filter is not restricted to returning the same response it accepts.

```scala
// Gzip response body if not empty
app.outgoing { res =>
  res.body.isKnownEmpty match
    case true  => res
    case false => res.setGzipContentEncoding()
}
```
### Lifecycle Hooks

A `LifecycleHook` is notified when the server is started and when it's stopped.

Here's an example of a simple hook:

```scala
import java.lang.System.currentTimeMillis as now

class UptimeService extends LifecycleHook:
  private var startTime: Option[Long] = None
  private var stopTime: Option[Long]  = None

  // Process lifecycle event
  def process(event: LifecycleEvent) =
    event match
      case LifecycleEvent.Start(server) =>
        startTime = Some(now())

      case LifecycleEvent.Stop(server)  =>
        stopTime  = Some(now())
        println(s"Server was running for ${uptime / 1000} seconds")

  // Get server's current uptime
  def uptime =
    startTime.map { time => stopTime.getOrElse(now()) - time }.getOrElse(0L)

// Add lifecycle hook to application
app.trigger(UptimeService())
```

There isn't much happening inside this hook, so it's likely nothing goes wrong
when it processes the events. However, if an exception were in fact thrown from
the event handler, the server would simply log a warning and continue its
normal operations.

If a lifecycle hook is critical to server operations, you should tag it as such
by also extending `CriticalService`:

```scala
class CriticalUptimeService extends UptimeService, CriticalService
```

Server creation is halted if a critical service fails to process the start
event.

### More Lifecycle Hooks

A request handler is also added as lifecycle hook if it extends `LifecycleHook`.

```scala
class UptimeRequestHandler extends UptimeService with RequestHandler:
  def apply(req: HttpRequest) =
    Ok(s"Uptime: $uptime")

// Added as request handler and lifecycle hook
app.get("/uptime")(UptimeRequestHandler())
```

This behavior applies to `RequestHandler`, `ResponseFilter`, `WebSocketApplication`,
`RouterApplication`, and `ErrorHandler`.

### Securing Server

The last piece of configuration is whether to secure the server using SSL/TLS.
To use a secure transport, you must supply an appropriate key and certificate.

```scala
app.secure(File("/path/to/private.key"), File("/path/to/public.crt"))
```

Or, if you have them tucked away in a keystore, you can supply the keystore
location.

```scala
// Supply location, password, and store type (i.e., JKS, JCEKS, PCKS12)
app.secure(File("/path/to/keystore"), "s3cr3t", "pkcs12")
```

### Creating Server

When the application has been configured, you can create the server.

```scala
val httpServer = app.toHttpServer(8080)
```

If the server must bind to a particular host, you can provide the host name or
IP address.

```scala
val httpServer = app.toHttpServer("192.168.0.2", 8080)
```

An instance of `HttpServer` is returned, which can be used to query server
details.

```scala
printf("Host: %s%n", httpServer.host)
printf("Port: %d%n", httpServer.port)
printf("Secure: %s%n", httpServer.isSecure)
printf("Backlog Size: %d%n", httpServer.backlogSize)
printf("Pool Size: %d%n", httpServer.poolSize)
printf("Queue Size: %d%n", httpServer.queueSize)
printf("Buffer Size: %d%n", httpServer.bufferSize)
printf("Read Timeout: %d%n", httpServer.readTimeout)
printf("Header Limit: %d%n", httpServer.headerLimit)
printf("Keep-Alive: %s%n", httpServer.keepAlive.getOrElse("disabled"))
printf("Closed: %s%n", httpServer.isClosed)
```

And, ultimately, it is used to gracefully shut down the server.

```scala
httpServer.close() // Goodbye, cruel world.
```

## API Documentation

See [scaladoc](https://losizm.github.io/scamper/latest/api/index.html)
for additional details.

## License

**Scamper** is licensed under the Apache License, Version 2. See [LICENSE](LICENSE)
for more information.
