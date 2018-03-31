# Scamper - HTTP Library for Scala

Scamper provides an API for reading and writing HTTP messages. It defines a set
of general interfaces, and it extends the feature set using the _Type Class
Pattern_ for specialized access to HTTP headers.

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
Likewise, you can use the implicit headers and type converters to build a
response.

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
Scamper comes equipped with client extensions for sending a request and handling
the response.

```scala
import scamper.ImplicitConverters._
import scamper.ImplicitHeaders._
import scamper.RequestMethods._
import scamper.ResponseStatuses._
import scamper.extensions._
import scamper.types.ImplicitConverters._
import scamper.util.ResponseFilters._

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
````
## Working with Message Body
The message body is represented as an instance of `scamper.Entity`, which
provides access to an input stream.

### Creating message body
When creating an HTTP message (i.e., `HttpRequest` or `HttpResponse`), you can
use either of the Entity factory methods to create the message body.
For example:

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

### Parsing message body

When handling an incoming message, you need to use an appropriate
`scamper.BodyParser` to parse the message body. There is a set of standard
parser implementations in `scamper.BodyParsers`.

```scala
import java.net.URL
import scala.util.Try
import scamper._
// Adds extra methods to java.net.URL
import scamper.extensions.URLExtension

val url = new URL("http://localhost:8080/db/weather-data.json")

val jsonText: Try[String] = url.get() { res =>
  // Parses the body as text
  res.parse(BodyParsers.text(maxLength = 1024))
}
```

You can also create a custom `BodyParser`. Here's one that gets a little help
from the standard text body parser and [play-json](https://github.com/playframework/play-json):

```scala
import java.net.URL
import play.api.libs.json._
import scamper._
// Adds extra methods to java.net.URL
import scamper.extensions.URLExtension

case class User(id: Long, name: String)

// Converts JSON body to User
object UserBodyParser extends BodyParser[User] {
  // Create standard text body parser
  implicit val textBodyParser = BodyParsers.text(maxLength = 1024)
  // Create play-json parser
  implicit val userReads = Json.reads[User]

  def apply(message: HttpMessage) =
    Json.parse(textBodyParser(message)).as[User]
}

val url = new URL("http://localhost:9000/users/500")

url.get() { response =>
  response.parse(UserBodyParser).foreach {
    case User(id, name) => println(s"$id -> $name")
  }
}
```

