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

import scala.language.implicitConversions

import scamper.http.headers.{ toConnection, toUpgrade }
import scamper.http.types.KeepAliveParameters

import ResponseStatus.Registry.SwitchingProtocols

private class ConnectionManager(keepAlive: Option[KeepAliveParameters]): //
  val keepAliveEnabled = keepAlive.isDefined
  val keepAliveMax     = keepAlive.map(_.max).getOrElse(1)
  val keepAliveTimeout = keepAlive.map(_.timeout).getOrElse(0)

  private val keepAliveHeader     = Header("Keep-Alive", s"timeout=$keepAliveTimeout, max=$keepAliveMax")
  private val connectionKeepAlive = Header("Connection", "keep-alive")
  private val connectionClose     = Header("Connection", "close")

  def filter(req: HttpRequest, res: HttpResponse): HttpResponse =
    if !keepAliveEnabled || isUpgrade(res) then
      res
    else
      doKeepAlive(req, res) match
        case true  => res.putHeaders(connectionKeepAlive, keepAliveHeader)
        case false => res.putHeaders(connectionClose)

  def evaluate(res: HttpResponse): ConnectionManagement =
    val connection = res.connection

    if connection.exists("upgrade".equalsIgnoreCase) then
      UpgradeConnection(res.getAttribute("scamper.http.server.connection.upgrade").get)
    else if connection.exists("keep-alive".equalsIgnoreCase) then
      PersistConnection
    else
      CloseConnection

  private def doKeepAlive(req: HttpRequest, res: HttpResponse): Boolean =
    isKeepAliveRequested(req) &&
    isKeepAliveMaxLeft(req) &&
    isKeepAliveSafe(req, res)

  private def isKeepAliveRequested(req: HttpRequest): Boolean =
    req.connection.exists("keep-alive".equalsIgnoreCase)

  private def isKeepAliveMaxLeft(req: HttpRequest): Boolean =
    req.getAttribute[Int]("scamper.http.server.message.requestCount")
      .exists(_ < keepAliveMax)

  private def isKeepAliveSafe(req: HttpRequest, res: HttpResponse): Boolean =
    res.isSuccessful || ((req.isGet || req.isHead) && res.isRedirection)

  private def isUpgrade(res: HttpResponse): Boolean =
    res.status == SwitchingProtocols &&
    res.hasUpgrade &&
    res.connection.exists("upgrade".equalsIgnoreCase)
