/*
 * Copyright 2019 Carlos Conyers
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

private class DefaultRouter(app: ServerApplication, rawMountPoint: String) extends Router {
  val mountPoint = normalize(rawMountPoint, true)

  def request(handler: RequestHandler): this.type = synchronized {
    app.request(TargetedRequestHandler(handler, mountPoint + "/*subpath", None))
    this
  }

  def request(filter: RequestFilter): this.type = synchronized {
    app.request(TargetedRequestHandler(filter, mountPoint + "/*subpath", None))
    this
  }

  def request(processor: RequestProcessor): this.type = synchronized {
    app.request(mountPoint + "/*subpath")(processor)
    this
  }

  def request(path: String)(processor: RequestProcessor): this.type = synchronized {
    app.request(mountPoint + normalize(path))(processor)
    this
  }

  def request(method: RequestMethod, path: String)(processor: RequestProcessor): this.type = synchronized {
    app.request(method, mountPoint + normalize(path))(processor)
    this
  }

  def head(path: String)(processor: RequestProcessor): this.type = synchronized {
    app.head(mountPoint + normalize(path))(processor)
    this
  }

  def get(path: String)(processor: RequestProcessor): this.type = synchronized {
    app.get(mountPoint + normalize(path))(processor)
    this
  }

  def post(path: String)(processor: RequestProcessor): this.type = synchronized {
    app.post(mountPoint + normalize(path))(processor)
    this
  }

  def put(path: String)(processor: RequestProcessor): this.type = synchronized {
    app.put(mountPoint + normalize(path))(processor)
    this
  }

  def patch(path: String)(processor: RequestProcessor): this.type = synchronized {
    app.patch(mountPoint + normalize(path))(processor)
    this
  }

  def delete(path: String)(processor: RequestProcessor): this.type = synchronized {
    app.delete(mountPoint + normalize(path))(processor)
    this
  }

  def options(path: String)(processor: RequestProcessor): this.type = synchronized {
    app.options(mountPoint + normalize(path))(processor)
    this
  }

  def trace(path: String)(processor: RequestProcessor): this.type = synchronized {
    app.trace(mountPoint + normalize(path))(processor)
    this
  }

  def files(mountPoint: String, sourceDirectory: File): this.type = synchronized {
    app.files(this.mountPoint + normalize(mountPoint), sourceDirectory)
    this
  }

  def resources(mountPoint: String, baseName: String, loader: Option[ClassLoader] = None): this.type = synchronized {
    app.resources(this.mountPoint + normalize(mountPoint), baseName, loader)
    this
  }

  private def normalize(path: String, isMountPoint: Boolean = false): String =
    path.toUri.normalize.toString match {
      case "/" => if (isMountPoint) "/" else ""
      case path if path.matches("/\\.\\.(/.*)?") => throw new IllegalArgumentException(s"Invalid path: $path")
      case path if path.startsWith("/") => path
      case path => throw new IllegalArgumentException(s"Invalid path: $path")
    }
}
