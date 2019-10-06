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
import java.net.InetAddress

import scamper.logging.Logger

/**
 * Provides handle to server instance.
 *
 * @see [[HttpServer$ HttpServer]], [[ServerApplication]]
 */
trait HttpServer {
  /** Gets host address. */
  def host: InetAddress

  /** Gets port number. */
  def port: Int

  /** Gets logger. */
  def logger: Logger

  /** Gets pool size. */
  def poolSize: Int

  /** Gets queue size. */
  def queueSize: Int

  /** Gets buffer size. */
  def bufferSize: Int

  /** Gets read timeout. */
  def readTimeout: Int

  /** Tests whether server is secure. */
  def isSecure: Boolean

  /** Closes server. */
  def close(): Unit

  /**
   * Tests whether server is closed.
   *
   * @return `true` if server is closed; `false` otherwise
   */
  def isClosed: Boolean
}

/** Provides factory methods for creating `HttpServer`. */
object HttpServer {
  /** Gets default server application. */
  def app(): ServerApplication = new ServerApplication()

  /**
   * Creates `HttpServer` at given port using supplied handler.
   *
   * @param port port number
   * @param handler request handler
   *
   * @return server
   */
  def create(port: Int)(handler: RequestHandler): HttpServer =
    create("0.0.0.0", port)(handler)

  /**
   * Creates `HttpServer` at given host and port using supplied handler.
   *
   * @param host host address
   * @param port port number
   * @param handler request handler
   *
   * @return server
   */
  def create(host: String, port: Int)(handler: RequestHandler): HttpServer =
    create(InetAddress.getByName(host), port)(handler)

  /**
   * Creates `HttpServer` at given host and port using supplied handler.
   *
   * @param host host address
   * @param port port number
   * @param handler request handler
   *
   * @return server
   */
  def create(host: InetAddress, port: Int)(handler: RequestHandler): HttpServer =
    app().incoming(handler).create(host, port)

  /**
   * Creates `HttpServer` at given port using supplied handler.
   *
   * The server is secured with key and certificate.
   *
   * @param port port number
   * @param key private key
   * @param certificate public key certificate
   * @param handler request handler
   *
   * @return server
   */
  def create(port: Int, key: File, certificate: File)(handler: RequestHandler): HttpServer =
    create("0.0.0.0", port, key, certificate)(handler)

  /**
   * Creates `HttpServer` at given host and port using supplied handler.
   *
   * The server is secured with key and certificate.
   *
   * @param host host address
   * @param port port number
   * @param key private key
   * @param certificate public key certificate
   * @param handler request handler
   *
   * @return server
   */
  def create(host: String, port: Int, key: File, certificate: File)(handler: RequestHandler): HttpServer =
    create(InetAddress.getByName(host), port, key, certificate)(handler)

  /**
   * Creates `HttpServer` at given host and port using supplied handler.
   *
   * The server is secured with key and certificate.
   *
   * @param host host address
   * @param port port number
   * @param key private key
   * @param certificate public key certificate
   * @param handler request handler
   *
   * @return server
   */
  def create(host: InetAddress, port: Int, key: File, certificate: File)(handler: RequestHandler): HttpServer =
    app().incoming(handler).secure(key, certificate).create(host, port)

  /**
   * Creates `HttpServer` at given port using supplied handler.
   *
   * The server is secured with keystore.
   *
   * @param port port number
   * @param keyStore server key store
   * @param password key store password
   * @param storeType key store type (i.e., JKS, JCEKS, etc.)
   * @param handler request handler
   *
   * @return server
   */
  def create(port: Int, keyStore: File, password: String, storeType: String)(handler: RequestHandler): HttpServer =
    create("0.0.0.0", port, keyStore, password, storeType)(handler)

  /**
   * Creates `HttpServer` at given host and port using supplied handler.
   *
   * The server is secured with keystore.
   *
   * @param host host address
   * @param port port number
   * @param keyStore server key store
   * @param password key store password
   * @param storeType key store type (i.e., JKS, JCEKS, etc.)
   * @param handler request handler
   *
   * @return server
   */
  def create(host: String, port: Int, keyStore: File, password: String, storeType: String)(handler: RequestHandler): HttpServer =
    create(InetAddress.getByName(host), port, keyStore, password, storeType)(handler)

  /**
   * Creates `HttpServer` at given host and port using supplied handler.
   *
   * The server is secured with keystore.
   *
   * @param host host address
   * @param port port number
   * @param keyStore server key store
   * @param password key store password
   * @param storeType key store type (i.e., JKS, JCEKS, etc.)
   * @param handler request handler
   *
   * @return server
   */
  def create(host: InetAddress, port: Int, keyStore: File, password: String, storeType: String)(handler: RequestHandler): HttpServer =
    app().incoming(handler).secure(keyStore, password, storeType).create(host, port)
}