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
package client

import java.net.SocketTimeoutException
import java.util.concurrent.atomic.AtomicBoolean

import scala.collection.mutable.Queue
import scala.concurrent.{ ExecutionContext, Future }
import scala.util.Try

import RuntimeProperties.client.*
import System.currentTimeMillis as now

private object ConnectionManager:
  private case class Entry(
    secure:     Boolean,
    host:       String,
    port:       Int,
    connection: HttpClientConnection,
    queueTime:  Long = now()
  ): //
    connection.setCloseGuard(true)

    def matches(secure: Boolean, host: String, port: Int): Boolean =
      this.secure == secure && this.host == host && this.port == port

  private val queue     = Queue[Entry]()
  private val activated = AtomicBoolean(false)

  def size: Int = queue.size

  def isEmpty: Boolean = queue.isEmpty

  def truncate(newSize: Int = 0): this.type =
    synchronized {
      queue.dequeueWhile(_ => queue.size > newSize.max(0)).foreach(dispose)
      this
    }

  def get(secure: Boolean, host: String, port: Int): Option[HttpClientConnection] =
    synchronized {
      find(secure, host, port)
    }

  def add(secure: Boolean, host: String, port: Int, connection: HttpClientConnection): this.type =
    synchronized {
      queue.enqueue(Entry(secure, host, port, connection))
      if activated.compareAndSet(false, true) then
        scheduleEviction()
      this
    }

  @annotation.tailrec
  private def find(secure: Boolean, host: String, port: Int): Option[HttpClientConnection] =
    val entry = queue.dequeueFirst(_.matches(secure, host, port))

    entry.forall(check) match
      case true  =>
        entry.map(extract)

      case false =>
        entry.foreach(dispose)
        find(secure, host, port)

  private def check(entry: Entry): Boolean =
    Try(checkQueueTime(entry) && checkSocket(entry))
      .getOrElse(false)

  private def checkQueueTime(entry: Entry): Boolean =
    entry.queueTime >= now() - connectionIdleTimeout

  private def checkSocket(entry: Entry): Boolean =
    val socket  = entry.connection.getSocket()
    val timeout = socket.getSoTimeout()

    socket.setSoTimeout(connectionTestTimeout)

    try
      socket.read()
      false
    catch case _: SocketTimeoutException =>
      true
    finally
      socket.setSoTimeout(timeout)

  private def scheduleEviction(): Unit =
    given ExecutionContext = Auxiliary.executor

    Future {
      Thread.sleep(connectionEvictionInterval)
      evict()
    }

  private def evict(): Unit =
    synchronized {
      if queue.nonEmpty then
        evictByIdleTimeout()
        evictByQueueSize()

      queue.isEmpty match
        case true  => activated.set(false)
        case false => scheduleEviction()
    }

  private def evictByIdleTimeout(): Unit =
    val minTime = now() - connectionIdleTimeout
    queue.dequeueWhile(_.queueTime < minTime).foreach(dispose)

  private def evictByQueueSize(): Unit =
    truncate(connectionQueueSize)

  private def extract(entry: Entry): HttpClientConnection =
    entry.connection.setCloseGuard(false)

  private def dispose(entry: Entry): Unit =
    Try(extract(entry).close())
