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

private class DefaultRouter(app: ServerApplication, rawMountPath: String) extends Router {
  val mountPath = normalize(rawMountPath, true)

  def incoming(handler: RequestHandler): this.type = synchronized {
    app.incoming(TargetedRequestHandler(handler, mountPath + "/*subpath", None))
    this
  }

  def incoming(filter: RequestFilter): this.type = synchronized {
    app.incoming(TargetedRequestHandler(filter, mountPath + "/*subpath", None))
    this
  }

  def incoming(processor: RequestProcessor): this.type = synchronized {
    app.incoming(mountPath + "/*subpath")(processor)
    this
  }

  def incoming(path: String)(processor: RequestProcessor): this.type = synchronized {
    app.incoming(mountPath + normalize(path))(processor)
    this
  }

  def incoming(method: RequestMethod, path: String)(processor: RequestProcessor): this.type = synchronized {
    app.incoming(method, mountPath + normalize(path))(processor)
    this
  }

  def head(path: String)(processor: RequestProcessor): this.type = synchronized {
    app.head(mountPath + normalize(path))(processor)
    this
  }

  def get(path: String)(processor: RequestProcessor): this.type = synchronized {
    app.get(mountPath + normalize(path))(processor)
    this
  }

  def post(path: String)(processor: RequestProcessor): this.type = synchronized {
    app.post(mountPath + normalize(path))(processor)
    this
  }

  def put(path: String)(processor: RequestProcessor): this.type = synchronized {
    app.put(mountPath + normalize(path))(processor)
    this
  }

  def patch(path: String)(processor: RequestProcessor): this.type = synchronized {
    app.patch(mountPath + normalize(path))(processor)
    this
  }

  def delete(path: String)(processor: RequestProcessor): this.type = synchronized {
    app.delete(mountPath + normalize(path))(processor)
    this
  }

  def options(path: String)(processor: RequestProcessor): this.type = synchronized {
    app.options(mountPath + normalize(path))(processor)
    this
  }

  def trace(path: String)(processor: RequestProcessor): this.type = synchronized {
    app.trace(mountPath + normalize(path))(processor)
    this
  }

  def files(mountPath: String, sourceDirectory: File): this.type = synchronized {
    app.files(this.mountPath + normalize(mountPath), sourceDirectory)
    this
  }

  def resources(mountPath: String, baseName: String, loader: Option[ClassLoader] = None): this.type = synchronized {
    app.resources(this.mountPath + normalize(mountPath), baseName, loader)
    this
  }

  private def normalize(path: String, isMountPath: Boolean = false): String =
    path.toUri.normalize.toString match {
      case "/" => if (isMountPath) "/" else ""
      case path if path.matches("/\\.\\.(/.*)?") => throw new IllegalArgumentException(s"Invalid path: $path")
      case path if path.matches("//+.*") => throw new IllegalArgumentException(s"Invalid path: $path")
      case path if path.startsWith("/") => path
      case path => throw new IllegalArgumentException(s"Invalid path: $path")
    }
}
