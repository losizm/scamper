# Scamper
**Scamper** is the HTTP library for Scala. It defines an API for reading and
writing HTTP messages, and it includes HTTP [client](#HTTP-Client)
and [server](#HTTP-Server) implementations.

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
import scamper.ImplicitConverters.stringToUri
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
case-insensitive, and the header value is also a `String`.

```scala
import scamper.ImplicitConverters.{ stringToUri, tupleToHeader }
import scamper.RequestMethods.POST

val req = POST("/api/users").withHeader("Content-Type" -> "application/json")

val contentType: Option[String] = req.getHeaderValue("Content-Type")
```

_But that's not all there is._

The interface to `HttpMessage` can be extended for specialized header access.
The extension methods are provided by the many type classes defined in
`scamper.headers`.

For example, `ContentType` adds the following methods:

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
import scamper.ImplicitConverters.stringToUri
import scamper.RequestMethods.POST
import scamper.headers.ContentType
import scamper.types.MediaType

val req = POST("/api/users").withContentType(MediaType("application", "json"))
println(req.contentType.mainType) // application
println(req.contentType.subtype) // json
```

And with `stringToMediaType` in scope, you can implicitly convert `String` to
`MediaType`.

```scala
import scamper.ImplicitConverters.stringToUri
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
import scamper.ImplicitConverters.stringToUri
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
`getHeaderValues()` collects all header values into `Seq[String]`; whereas,
`getHeaderValue()` retrieves first header value only._

## Message Body
The message body is represented as `Entity`, which encapsulates content in
`java.io.InputStream`.

### Creating Message Body
When building a message, use one of the `Entity` factory methods to create the
body. For example, you can create a message body using text content.

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

Or create a message body using file content.

```scala
import java.io.File
import scamper.Entity
import scamper.ResponseStatuses.Ok
import scamper.headers.ContentType
import scamper.types.ImplicitConverters.stringToMediaType

val body = Entity(new File("./index.html"))
val res = Ok(body).withContentType("text/html; charset=utf-8")
```

There are implicit converters available for common entity types, so you aren't
required to create them explicitly.

```scala
import java.io.File
import scamper.ImplicitConverters.fileToEntity
import scamper.ResponseStatuses.Ok
import scamper.headers.ContentType
import scamper.types.ImplicitConverters.stringToMediaType

val res = Ok(new File("./index.html")).withContentType("text/html; charset=utf-8")
```

### Parsing Message Body

When handling an incoming message, use an appropriate `BodyParser` to parse the
message body. There is a set of standard parsers available in `BodyParsers`,
such as the one used for parsing text content.

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
The `HttpClient` object is used for sending a request and handling the response.

```scala
import scamper.HttpClient
import scamper.ImplicitConverters.{ stringToEntity, stringToUri }
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
import scamper.ImplicitConverters.stringToUri
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

## HTTP Server

**Scamper** includes an extensible server framework.

To demonstrate, let's begin with a simple example.

```scala
import scamper.ImplicitConverters.stringToEntity
import scamper.ResponseStatuses.Ok
import scamper.server.HttpServer

val server = HttpServer.create(8080) { req =>
  Ok("Hello, world!")
}
```

This is as bare-bones as it gets. We create a server at port 8080, and, on each
incoming request, we send _Hello, world!_ back to the client. Although trite, it
shows how easy it is to get going. What it doesn't show, however, are the pieces
being put together to create the server. Minus imports, here's the semantic
equivalent in long form.

```scala
val server = HttpServer.config().request(req => Ok("Hello, world!")).create(8080)
```

We'll use the remainder of this documentation to describe what goes into
creating more practical applications.

### Server Configuration

To build a server, you begin with `ServerConfiguration`. This is a mutable
structure to which you apply changes to configure the server. Once the desired
settings are applied, you invoke one of several methods to create the server.

You can obtain an instance of `ServerConfiguration` from the `HttpServer`
object.

```scala
val config = HttpServer.config()
```

This gives you the default configuration as a starting point. With this in hand,
you can override the location of the server log.

```scala
config.log(new File("/tmp/server.log"))
```

And there are peformance-related settings that can be tweaked as well.

```scala
config.poolSize(10)
config.queueSize(25)
config.readTimeout(3000)
```

The **poolSize** specifies the maximum number of requests that are processed
concurrently, and **queueSize** sets the number of requests that are permitted
to wait for processing &mdash; _incoming requests that would exceed this limit
are discarded_.

Note **queueSize** is also used to configure server backlog (i.e., backlog of
incoming connections), so technically there can be up to double **queueSize**
waiting to be processed if both request queue and server backlog are filled.

The **readTimeout** controls how long a read from a socket blocks before it
times out. At which point, the socket is closed, and its associated request is
discarded.

### Request Handlers

You define application-specific logic in instances of `RequestHandler`, and add
them to the server configuration.

An `HttpRequest` is passed to the `RequestHandler`, and the handler returns
`Either[HttpRequest, HttpResponse]`. If the handler is unable to satisfy the
request, it returns an `HttpRequest` so that the next handler can have its turn.
Otherwise, it returns an `HttpResponse`, and any remaining handlers are
effectively ignored.

```scala
import scamper.RequestMethods.{ GET, HEAD }
import scamper.ResponseStatuses.MethodNotAllowed
import scamper.headers.Allow

// Add handler to log request line and headers to stdout
config.request { req =>
  println(req.startLine)
  req.headers.foreach(println)
  println()

  // Return request for next handler
  Left(req)
}

// Add handler to allow GET and HEAD requests only
config.request { req =>
  if (req.method == GET || req.method == HEAD)
    // Return request for next handler
    Left(req)
  else
    // Otherwise return response to end request chain
    Right(MethodNotAllowed().withAllow(GET, HEAD))
}
```

Note the order in which handlers are applied matters. For instance, in the
example above, you'd swap the order of handlers if you wanted to log GET and
HEAD requests only, meaning all other requests would immediately be sent **405
Method Not Allowed** and never make it to the handler that logs requests.

Also note a request handler is not restricted to returning the same request it
was passed.

```scala
import scamper.BodyParser
import scamper.ImplicitConverters.stringToEntity
import scamper.RequestMethods.POST
import scamper.headers.ContentLanguage
import scamper.types.LanguageTag
import scamper.types.ImplicitConverters.stringToLanguageTag

// Translates message body from French (Oui, oui.)
config.request { req =>
  val translator: BodyParser[String] = ...

  if (req.method == POST && req.contentLanguage.contains("fr"))
    Left(req.withBody(translator.parse(req)).withContentLanguage("en"))
  else
    Left(req)
}
```

### Filtering vs. Processing

There are two subclasses of `RequestHandler` reserved for instances where it's
known the handler always returns the same type: `RequestFilter` always returns
an `HttpRequest`, and `RequestProcessor` always returns an `HttpResponse`. These
are _filtering_ and _processing_, respectively.

The request logger can be rewritten as `RequestFilter`.

```scala
config.request { req =>
  println(req.startLine)
  req.headers.foreach(println)
  println()

  req // Not wrapped in Left
}
```

And we used `RequestProcessor` in our _"Hello World"_ server, but here's one
that would do something more meaningful.

```scala
import scamper.ImplicitConverters.fileToEntity
import scamper.ResponseStatuses.{ NotFound, Ok }

config.request { req =>
  def findFile(path: String): Option[File] = {
    ...
  }

  findFile(req.path).map(Ok(_)).getOrElse(NotFound())
}
```

### Targeted Request Processing

A request processor can be included for a targeted path with or without a
targeted request method.

```scala
import scamper.ImplicitConverters.stringToEntity
import scamper.RequestMethods.{ GET }
import scamper.ResponseStatuses.{ Forbidden, Ok }

// Match request method and exact path
config.request(GET, "/about") { req =>
  Ok("This server is powered by Scamper.")
}

// Match exact path and any method
config.request("/private") { req =>
  Forbidden()
}
```

Parameters can be specified in the path and their resolved values made available
to the processor. When a parameter is specified as __:param__, it matches a
single path component; whereas, __*param__ matches the path component along with
any remaining components including path separators (i.e., **/**).

```scala
import scamper.ImplicitConverters.fileToEntity
import scamper.RequestMethods.DELETE
import scamper.ResponseStatuses.{ Accepted, NotFound, Ok }
import scamper.server.Implicits.HttpRequestType

// Match request method and parameterized path
config.request(DELETE, "/orders/:id") { req =>
  def deleteOrder(id: Int): Boolean = {
    ...
  }

  // Get resolved parameter
  val id = req.params.getInt("id")

  if (deleteOrder(id))
    Accepted()
  else
    NotFound()
}

// Match prefixed path with any request method
config.request("/archive/*file") { req =>
  def findFile(path: String): Option[File] = {
    ...
  }

  // Get resolved parameter
  val file = req.params.getString("file")

  findFile(req.path).map(Ok(_)).getOrElse(NotFound())
}
```

There can be multiple __:param__ instances specified in the path.

```scala
import scamper.BodyParser
import scamper.ImplicitConverters.stringToEntity
import scamper.RequestMethods.POST
import scamper.ResponseStatuses.Ok
import scamper.server.Implicits.HttpRequestType

// Match path with two parameters
config.request(POST, "/translate/:in/to/:out") { req =>
  def translator(from: String, to: String): BodyParser[String] = {
    ...
  }

  val from = req.params.getString("in")
  val to = req.params.getString("out")

  Ok(translator(from, to).parse(req))
}
```

Note there can be at most one __*param__, which must be specified as the the
last component in the path.

### Serving Static Files

You can include a specialized request handler to serve static files.

```scala
// Serve static files from given directory
config.request(new File("/path/to/public"))
```

This adds a request handler to serve files from the directory at _/path/to/public_.
The files are mapped based on the request path. For example,
_http://localhost:8080/images/logo.png_ would map to _/path/to/public/images/logo.png_.

Or, you can map a path prefix to a directory.

```scala
config.request("/app/main", new File("/path/to/public"))
```

In this case, _http://localhost:8080/app/main/images/logo.png_ would map to
_/path/to/public/images/logo.png_.

### Response Filters

In much the same way requests can be filtered, so too can responses. Response
filtering is performed by including instances of `ResponseFilter`. They are
applied, in order, after one of the request handlers generates a response.

```scala
config.response { res =>
  println(res.startLine)
  res.headers.foreach(println)
  println()

  // Return response for next filter
  res
}
```

This is pretty much the same as the request logger from earlier, only instead of
`HttpRequest`, it consumes and produces `HttpResponse`.

And, similar to a request filter, the response filter is not restricted to
returning the same response it consumed.

```scala
import scamper.headers.TransferEncoding
import scamper.types.ImplicitConverters.stringToTransferCoding

config.response { res =>
  res.withBody(new DeflaterInputStream(res.body.getInputStream))
    .withTransferEncoding("deflate", "chunked")
}
```

### Securing the Server

The last piece of configuration is whether to secure the server using SSL/TLS.
To use a secure transport, you must supply an appropriate key and certificate.

```scala
config.secure(new File("/path/to/private.key"), new File("/path/to/public.cert"))
```

Or, if you have them tucked away in a key store, you can supply the key store
location.

```scala
// Supply location, password, and store type (i.e., JKS, JCEKS, PCKS12)
config.secure(new File("/path/to/keystore"), "s3cr3t", "pkcs12")
```

### Creating the Server

When the desired configuration is in place, you're ready to create the server.

```scala
val server = config.create(8080)
```

If the server must bind to a particular host, you can provide the host name or
IP address.

```scala
val server = config.create("192.168.0.2", 8080)
```

When created, an instance of `HttpServer` is returned, which can be used to
query a few server details.

```scala
printf("Host: %s%n", server.host)
printf("Port: %d%n", server.port)
printf("Secure: %s%n", server.isSecure)
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
**Scamper** is licensed under the Apache license, version 2. See LICENSE file
for more information.
