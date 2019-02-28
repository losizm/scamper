/*
 * Copyright 2018 Carlos Conyers
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package scamper.server

import java.io.File

import scala.util.Try

import scamper.Auxiliary.StringType
import scamper.RequestMethod

private class DefaultRouter(app: ServerApplication, path: String) extends Router {
  private val basePath = normalize(path, true)

  def request(handler: RequestHandler): this.type = synchronized {
    app.request(TargetedRequestHandler(handler, basePath + "/*subpath", None))
    this
  }

  def request(filter: RequestFilter): this.type = synchronized {
    app.request(TargetedRequestHandler(filter, basePath + "/*subpath", None))
    this
  }

  def request(processor: RequestProcessor): this.type = synchronized {
    app.request(basePath + "/*subpath")(processor)
    this
  }

  def request(path: String)(processor: RequestProcessor): this.type = synchronized {
    app.request(basePath + normalize(path))(processor)
    this
  }

  def request(method: RequestMethod, path: String)(processor: RequestProcessor): this.type = synchronized {
    app.request(method, basePath + normalize(path))(processor)
    this
  }

  def head(path: String)(processor: RequestProcessor): this.type = synchronized {
    app.head(basePath + normalize(path))(processor)
    this
  }

  def get(path: String)(processor: RequestProcessor): this.type = synchronized {
    app.get(basePath + normalize(path))(processor)
    this
  }

  def post(path: String)(processor: RequestProcessor): this.type = synchronized {
    app.post(basePath + normalize(path))(processor)
    this
  }

  def put(path: String)(processor: RequestProcessor): this.type = synchronized {
    app.put(basePath + normalize(path))(processor)
    this
  }

  def patch(path: String)(processor: RequestProcessor): this.type = synchronized {
    app.patch(basePath + normalize(path))(processor)
    this
  }

  def delete(path: String)(processor: RequestProcessor): this.type = synchronized {
    app.delete(basePath + normalize(path))(processor)
    this
  }

  def options(path: String)(processor: RequestProcessor): this.type = synchronized {
    app.options(basePath + normalize(path))(processor)
    this
  }

  def trace(path: String)(processor: RequestProcessor): this.type = synchronized {
    app.trace(basePath + normalize(path))(processor)
    this
  }

  def files(path: String, baseDirectory: File): this.type = synchronized {
    app.files(basePath + normalize(path), baseDirectory)
    this
  }

  def resources(path: String, baseName: String, loader: Option[ClassLoader] = None): this.type = synchronized {
    app.resources(basePath + normalize(path), baseName, loader)
    this
  }

  private def normalize(path: String, isBase: Boolean = false): String =
    path.toUri.normalize.toString match {
      case "/" => if (isBase) "/" else ""
      case path if path.matches("/\\.\\.(/.*)?") => throw new IllegalArgumentException(s"Invalid path: $path")
      case path if path.startsWith("/") => path
      case path => throw new IllegalArgumentException(s"Invalid path: $path")
    }
}
