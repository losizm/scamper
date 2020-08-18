/*
 * Copyright 2020 Carlos Conyers
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

import scala.util.Try

private object RuntimeProperties {
  object auxiliary {
    lazy val executorShowWarning = getBooleanProperty("scamper.auxiliary.executor.showWarning", false)
    lazy val executorCorePoolSize = getIntProperty("scamper.auxiliary.executor.corePoolSize", 0)
    lazy val executorMaxPoolSize = getIntProperty("scamper.auxiliary.executor.maxPoolSize", Runtime.getRuntime.availableProcessors * 2)
    lazy val executorKeepAliveSeconds = getLongProperty("scamper.auxiliary.executor.keepAliveSeconds", 60)
    lazy val executorQueueSize =  getIntProperty("scamper.auxiliary.executor.queueSize", 0)
  }

  object cookies {
    lazy val getRemotePublicSuffixList = getBooleanProperty("scamper.cookies.getRemotePublicSuffixList", false)
    lazy val publicSuffixListUrl = System.getProperty("scamper.cookies.publicSuffixListUrl", "https://publicsuffix.org/list/public_suffix_list.dat")
  }

  object server {
    lazy val keepAlivePoolSizeFactor = getIntProperty("scamper.server.keepAlive.poolSizeFactor", 2).max(1)
    lazy val upgradePoolSizeFactor = getIntProperty("scamper.server.upgrade.poolSizeFactor", 2).max(1)
    lazy val encoderPoolSizeFactor = getIntProperty("scamper.server.encoder.poolSizeFactor", 2).max(1)
    lazy val closerPoolSizeFactor = getIntProperty("scamper.server.closer.poolSizeFactor", 2).max(1)
  }

  private def getBooleanProperty(name: String, default: => Boolean): Boolean =
    Try(sys.props(name).toBoolean).getOrElse(default)

  private def getIntProperty(name: String, default: => Int): Int =
    Try(sys.props(name).toInt).getOrElse(default)

  private def getLongProperty(name: String, default: => Long): Long =
    Try(sys.props(name).toLong).getOrElse(default)
}