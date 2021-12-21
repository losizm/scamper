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

import java.lang.Runtime.getRuntime as runtime
import java.lang.System.getProperty

import scala.util.Try

private object RuntimeProperties:
  object auxiliary:
    lazy val executorShowWarning      = getBooleanProperty("scamper.auxiliary.executor.showWarning", false)
    lazy val executorCorePoolSize     = getIntProperty("scamper.auxiliary.executor.corePoolSize", 0).max(0)
    lazy val executorMaxPoolSize      = getIntProperty("scamper.auxiliary.executor.maxPoolSize", runtime.availableProcessors * 2).max(1)
    lazy val executorKeepAliveSeconds = getLongProperty("scamper.auxiliary.executor.keepAliveSeconds", 60L).max(0L)
    lazy val executorQueueSize        = getIntProperty("scamper.auxiliary.executor.queueSize", 0).max(0)

  object cookies:
    lazy val getRemotePublicSuffixList = getBooleanProperty("scamper.http.cookies.getRemotePublicSuffixList", false)
    lazy val publicSuffixListUrl       = getProperty("scamper.http.cookies.publicSuffixListUrl", "https://publicsuffix.org/list/public_suffix_list.dat")

  object client:
    lazy val connectionEvictionInterval = getIntProperty("scamper.http.client.connection.evictionInterval", 5000).max(1000)
    lazy val connectionIdleTimeout      = getIntProperty("scamper.http.client.connection.idleTimeout", 60000).max(0)
    lazy val connectionQueueSize        = getIntProperty("scamper.http.client.connection.queueSize", 50).max(1)
    lazy val connectionTestTimeout      = getIntProperty("scamper.http.client.connection.testTimeout", 100).max(10)

  object server:
    lazy val keepAlivePoolSizeFactor = getIntProperty("scamper.http.server.keepAlive.poolSizeFactor", 2).max(1)
    lazy val upgradePoolSizeFactor   = getIntProperty("scamper.http.server.upgrade.poolSizeFactor", 2).max(1)
    lazy val encoderPoolSizeFactor   = getIntProperty("scamper.http.server.encoder.poolSizeFactor", 2).max(1)
    lazy val closerQueueSizeFactor   = getIntProperty("scamper.http.server.closer.queueSizeFactor", 2).max(0)

  private def getBooleanProperty(name: String, default: => Boolean): Boolean =
    Try(sys.props(name).toBoolean)
      .getOrElse(default)

  private def getIntProperty(name: String, default: => Int): Int =
    Try(sys.props(name).toInt)
      .getOrElse(default)

  private def getLongProperty(name: String, default: => Long): Long =
    Try(sys.props(name).toLong)
      .getOrElse(default)
