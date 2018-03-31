# Scamper - HTTP API for Scala

Scamper provides an API for reading and writing HTTP messages. It defines a set
of general interfaces, and it extends the standard feature set using the _Type
Class Pattern_ for specialized access to HTTP headers and their associated value
types.

## Building HTTP Requests
The simplest way to build a request is to make use of the API's implicit
headers and type converters.

```scala
import scamper.ImplicitHeaders._
import scamper.RequestMethods._
import scamper.types.ImplicitConverters._

object RequestFactory {
  def get = GET("/index.html")
    .withHost("localhost:8080")
    .withUserAgent("Scamper/1.0")
    .withAccept("text/html", "*/*; q=0.5")
}
```

## Building HTTP Responses
The same applies when building a response. You can use of the API's implicit
headers and type converters.

```scala
import scamper.ImplicitConverters._
import scamper.ImplicitHeaders._
import scamper.ResponseStatuses._
import scamper.types.ImplicitConverters._

object ResponseFactory {
  def get = Ok("Hello, world!")
    .withContentType("text/plain")
    .withServer("Scamper/1.0")
    .withConnection("close")
}
```

## Using HTTP Client Extension
Scamper comes with a client extension for sending a request and handling the
response.

```scala
import scamper.ImplicitConverters._
import scamper.ImplicitHeaders._
import scamper.RequestMethods._
import scamper.ResponseStatuses._
import scamper.extensions._
import scamper.types.ImplicitConverters._
import scamper.util.ResponseFilters._

object AdminApiClient {
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
````
