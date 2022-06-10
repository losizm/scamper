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

import RuntimeProperties.auxiliary.*

private object Auxiliary:
  lazy val logger   = org.slf4j.LoggerFactory.getLogger(getClass)
  lazy val executor =
    ThreadPoolExecutorService
      .dynamic(
        name             = "scamper-auxiliary",
        corePoolSize     = executorCorePoolSize,
        maxPoolSize      = executorMaxPoolSize,
        keepAliveSeconds = executorKeepAliveSeconds,
        queueSize        = executorQueueSize
      ) { (task, executor) =>
        if executorShowWarning then
          logger.warn("Running rejected scamper-auxiliary task on dedicated thread.")
        executor.getThreadFactory.newThread(task).start()
      }
