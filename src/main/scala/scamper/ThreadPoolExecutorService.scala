/*
 * Copyright 2017-2020 Carlos Conyers
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

import java.util.concurrent._
import java.util.concurrent.atomic.AtomicLong

import scala.concurrent.{ ExecutionContext, ExecutionContextExecutorService }

private object ThreadPoolExecutorService {
  def fixed(
    name:        String,
    poolSize:    Int,
    queueSize:   Int = 0,
    threadGroup: Option[ThreadGroup] = None
  )(rejectedExecutionHandler: RejectedExecutionHandler): ExecutionContextExecutorService =
    createExecutorService(
      name               = name,
      inCorePoolSize     = poolSize,
      inMaxPoolSize      = poolSize,
      inKeepAliveSeconds = 0,
      inQueueSize        = queueSize,
      inThreadGroup      = threadGroup,
      rejectedHandler    = rejectedExecutionHandler
    )

  def dynamic(
    name:             String,
    corePoolSize:     Int,
    maxPoolSize:      Int,
    keepAliveSeconds: Long = 60L,
    queueSize:        Int = 0,
    threadGroup:      Option[ThreadGroup] = None
  )(rejectedExecutionHandler: RejectedExecutionHandler): ExecutionContextExecutorService =
    createExecutorService(
      name               = name,
      inCorePoolSize     = corePoolSize,
      inMaxPoolSize      = maxPoolSize,
      inKeepAliveSeconds = keepAliveSeconds,
      inQueueSize        = queueSize,
      inThreadGroup      = threadGroup,
      rejectedHandler    = rejectedExecutionHandler
    )

  private def createExecutorService(
    name:               String,
    inCorePoolSize:     Int,
    inMaxPoolSize:      Int,
    inKeepAliveSeconds: Long,
    inQueueSize:        Int,
    inThreadGroup:      Option[ThreadGroup],
    rejectedHandler:    RejectedExecutionHandler
  ): ExecutionContextExecutorService =
    ExecutionContext.fromExecutorService {
      val corePoolSize     = inCorePoolSize.max(0)
      val maxPoolSize      = inMaxPoolSize.max(corePoolSize).max(1)
      val keepAliveSeconds = inKeepAliveSeconds.max(0)
      val queueSize        = inQueueSize.max(0)
      val threadGroup      = inThreadGroup.getOrElse(new ThreadGroup(name))
      val threadCount      = new AtomicLong(0)

      new ThreadPoolExecutor(
        corePoolSize,
        maxPoolSize,
        keepAliveSeconds,
        TimeUnit.SECONDS,
        queueSize match {
          case 0 => new SynchronousQueue()
          case n => new ArrayBlockingQueue(n)
        },
        new ThreadFactory {
          def newThread(task: Runnable) = {
            val thread = new Thread(threadGroup, task, s"$name-${threadCount.incrementAndGet()}")
            thread.setDaemon(true)
            thread
          }
        },
        rejectedHandler
      )
    }
}
