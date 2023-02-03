
# Scamper

[![Maven Central](https://img.shields.io/maven-central/v/com.github.losizm/scamper_3.svg?label=Maven%20Central)](https://search.maven.org/search?q=g:%22com.github.losizm%22%20AND%20a:%22scamper_3%22)

**Scamper** is the HTTP library for Scala.

It includes client and server implementations along with WebSockets.

## Getting Started

To get started, add **Scamper** to your sbt project&dagger;:

```scala
libraryDependencies += "com.github.losizm" %% "scamper" % "36.0.0"
```

**Scamper** uses SLF4J logging abstraction under the hood, so you'll need to
bind to an implementation if you wish to enable logging.

Here's how to bind to [Logback](https://logback.qos.ch):

```scala
libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.3.5"
```

See [SLF4J Documentation](https://www.slf4j.org/manual.html#projectDep) for
binding to other logging frameworks.

_<small>&dagger; Scamper 23.0.0 and above are Scala 3 compatible. See previous
releases for compatibility with Scala 2.12 and Scala 2.13.</small>_

## Let's Scamper!

As a quick introduction, the following two subsections provide examples on how
to use the library. In particular, the first section builds and runs an HTTP
server, and the next creates a client to send requests to it.

See also [Developer Guide](DeveloperGuide.md) for a systematic review of the
library.

### HTTP Server

First is an example of how to build and run an HTTP server. It's a bit trite and
only scratches the surface; however, it does capture the general programming
style.

```scala
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration.DurationInt
import scala.language.implicitConversions
import scamper.http.{ *, given }
import scamper.http.server.{ *, given }
import ResponseStatus.Registry.{ BadRequest, Ok }

// Define utility to log HTTP message
def logHttpMessage[T <: HttpMessage](msg: T): T =
  println()
  println(msg.startLine)
  msg.headers.foreach(println)
  println()
  msg

// Starting point to build HTTP server
val app = ServerApplication()

// Log incoming request
app.incoming(logHttpMessage)

// Handle GET request
app.get("/greet") { req =>
  req.query.get("name") match
    case Some(name) => Ok(s"Hello, $name!")
    case None       => Ok("Hello, world!")
}

// Handle POST request at different endpoint
app.post("/echo") { req =>
  given BodyParser[String] = BodyParser.string(maxLength = 1024)

  req.as[String] match
    case ""      => BadRequest("No message.")
    case message => Ok(message)
}

// Log outgoing response
app.outgoing(logHttpMessage)

Future {
  // Create HTTP server at port 8080
  val httpServer = app.toHttpServer(8080)

  try
    println("*** Server is up and running ***")
    Thread.sleep(5.minutes.toMillis)
  finally
    println("*** Shutting down server ***")
    httpServer.close()
}

// To restart server...
// val moreServer = app.toHttpServer(8080)

// Or to run it on another port...
// val copyServer = app.toHttpServer(9090)
```

### HTTP Client

Next are some examples of how to use the HTTP client. It sends requests to the
server created in the previous section, so make sure the server is running.

```scala
import scala.language.implicitConversions
import scamper.http.{ *, given }
import scamper.http.client.{ *, given }
import scamper.http.types.{ *, given }
import RequestMethod.Registry.{ Get, Post }

// Define parser for incoming message
given BodyParser[String] = BodyParser.string(maxLength = 1024)

// Create HTTP client using custom settings
val httpClient = ClientSettings()
  .resolveTo("localhost", 8080, secure = false)
  .accept("*/*")
  .acceptEncoding("deflate", "gzip")
  .toHttpClient()

// Send GET request
httpClient.get("http://localhost:8080/greet?name=developer") { res =>
  println(res.as[String])
}

// Manually create GET request and add query parameter
val greetRequest = Get("/greet").setQuery("name" -> "Big Dawg")
httpClient.send(greetRequest) { res => println(res.as[String]) }

// Send POST request with message body
httpClient.post("/echo", body = "Hello, it's me.") { res =>
  println(res.as[String])
}

// Manually create POST request and add message body
val echoRequest = Post("/echo").setPlainBody("Just me again.")
httpClient.send(echoRequest) { res => println(res.as[String]) }

// Send empty POST request and handle client error
httpClient.post("/echo", body = "") { res =>
  res.isSuccessful match
    case true  => println("This won't be printed.")
    case false => println(s"Oops! ${res.as[String]} (${res.status})")
}
```

## API Documentation

See [scaladoc](https://losizm.github.io/scamper/latest/api/index.html)
for additional details.

## License

**Scamper** is licensed under the Apache License, Version 2. See [LICENSE](LICENSE)
for more information.
