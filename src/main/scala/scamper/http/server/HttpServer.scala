/*
 * Copyright 2021 Carlos Conyers
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
package scamper
package http
package server

import java.io.File
import java.net.InetAddress

import scamper.http.types.KeepAliveParameters

/**
 * Defines handle to server instance.
 *
 * @see [[HttpServer$.app HttpServer.app()]], [[ServerApplication]]
 */
trait HttpServer extends AutoCloseable:
  /** Gets backlog size. */
  def backlogSize: Int

  /** Gets pool size. */
  def poolSize: Int

  /** Gets queue size. */
  def queueSize: Int

  /** Gets buffer size. */
  def bufferSize: Int

  /** Gets read timeout. */
  def readTimeout: Int

  /** Gets header limit. */
  def headerLimit: Int

  /** Gets keep-alive parameters. */
  def keepAlive: Option[KeepAliveParameters]

  /** Gets host address. */
  def host: InetAddress

  /** Gets port number. */
  def port: Int

  /**
   * Tests whether server is secure.
   *
   * @return `true` if server uses SSL/TLS; `false` otherwise
   */
  def isSecure: Boolean

  /**
   * Tests whether server is closed.
   *
   * @return `true` if server is closed; `false` otherwise
   */
  def isClosed: Boolean

  /** Closes server. */
  def close(): Unit

/** Provides factory for `HttpServer`. */
object HttpServer:
  /** Gets new instance of server application. */
  def app(): ServerApplication = ServerApplication()
