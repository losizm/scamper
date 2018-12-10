/*
 * Copyright 2018 Carlos Conyers
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
package scamper.client

import java.io.{ File, FileInputStream }
import java.security.KeyStore
import javax.net.ssl.{ SSLContext, SSLSocketFactory, TrustManagerFactory }

import scala.util.Try

private object SecureSocketFactory {
  def create(keyStore: KeyStore): SSLSocketFactory = {
    val trustManagerFactory = TrustManagerFactory.getInstance("SunX509")
    trustManagerFactory.init(keyStore)

    val sslContext = SSLContext.getInstance("TLS")
    sslContext.init(null, trustManagerFactory.getTrustManagers(), null)
    sslContext.getSocketFactory()
  }

  def create(storeFile: File): SSLSocketFactory = {
    val storeStream = new FileInputStream(storeFile)

    try {
      val keyStore = KeyStore.getInstance("JKS")
      keyStore.load(storeStream, null)
      create(keyStore)
    } finally {
      Try(storeStream.close())
    }
  }
}
