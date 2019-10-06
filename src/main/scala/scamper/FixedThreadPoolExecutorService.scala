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
package scamper

import java.util.concurrent._
import java.util.concurrent.atomic.AtomicLong

import scala.concurrent.{ ExecutionContext, ExecutionContextExecutorService }

private object FixedThreadPoolExecutorService {
  def apply(name: String, poolSize: Int, queueSize: Int, threadGroup: Option[ThreadGroup] = None)
      (rejectedExecutionHandler: RejectedExecutionHandler): ExecutionContextExecutorService =
    ExecutionContext.fromExecutorService {
      val threadFactory = new ThreadFactory {
        private val group = threadGroup.getOrElse(new ThreadGroup(name))
        private val count = new AtomicLong(0)

        def newThread(task: Runnable) = {
          val thread = new Thread(group, task, s"$name-${count.incrementAndGet()}")
          thread.setDaemon(true)
          thread
        }
      }

      new ThreadPoolExecutor(
        poolSize.max(1),
        poolSize.max(1),
        60,
        TimeUnit.SECONDS,
        queueSize.max(0) match {
          case 0 => new SynchronousQueue()
          case n => new ArrayBlockingQueue(n)
        },
        threadFactory,
        rejectedExecutionHandler
      )
    }
}
