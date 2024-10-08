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

import java.io.{ ByteArrayInputStream, File, FileInputStream }
import java.security.{ KeyFactory, KeyStore, PrivateKey, SecureRandom }
import java.security.cert.{ Certificate, CertificateFactory }
import java.security.spec.PKCS8EncodedKeySpec
import javax.net.ssl.{ KeyManagerFactory, SSLContext, SSLServerSocketFactory }

import scala.util.Try

private object SecureServerSocketFactory:
  def create(keyStore: KeyStore, password: Array[Char]): SSLServerSocketFactory =
    val keyManagerFactory = KeyManagerFactory.getInstance("SunX509")
    keyManagerFactory.init(keyStore, password)

    val sslContext = SSLContext.getInstance("TLS")
    sslContext.init(keyManagerFactory.getKeyManagers, null, null)
    sslContext.getServerSocketFactory

  def create(storeFile: File, password: Array[Char], storeType: String): SSLServerSocketFactory =
    val storeStream = FileInputStream(storeFile)

    try
      val keyStore = KeyStore.getInstance(storeType)
      keyStore.load(storeStream, password)

      create(keyStore, password)
    finally
      Try(storeStream.close())

  def create(key: PrivateKey, cert: Certificate):  SSLServerSocketFactory =
    val password = Passwords.create()

    val keyStore = KeyStore.getInstance("PKCS12")
    keyStore.load(null, password)
    keyStore.setKeyEntry("server", key, password, Array(cert))

    create(keyStore, password)

  def create(key: Array[Byte], cert: Array[Byte]): SSLServerSocketFactory =
    create(Keys.create(key), Certificates.create(cert))

  def create(key: File, cert: File):  SSLServerSocketFactory =
    create(Keys.create(key), Certificates.create(cert))

private object Keys:
  private val factory = KeyFactory.getInstance("RSA")

  def create(bytes: Array[Byte]): PrivateKey =
    val spec = PKCS8EncodedKeySpec(PemDecoder.decode(bytes).getOrElse(bytes))
    factory.generatePrivate(spec)

  def create(file: File): PrivateKey =
    if file.length > 8388608 then
      throw IllegalArgumentException(s"Key file too large: ${file.length} bytes")
    create(file.getBytes())

private object Certificates:
  private val factory = CertificateFactory.getInstance("X509")

  def create(bytes: Array[Byte]): Certificate =
    val in = ByteArrayInputStream(bytes)
    try factory.generateCertificate(in)
    finally Try(in.close())

  def create(file: File): Certificate =
    val in = FileInputStream(file)
    try factory.generateCertificate(in)
    finally Try(in.close())

private object PemDecoder:
  private val encoded = """(?s).*-{5}BEGIN(?: .+)?-{5}\s*([A-Za-z0-9+/=\s]+)\s*-{5}END(?: .+)?-{5}.*""".r

  def decode(bytes: Array[Byte]): Option[Array[Byte]] =
    String(bytes, "ASCII") match
      case encoded(text) => Some { Base64.decode(text.split("\\s+").mkString) }
      case _ => None

private object Passwords:
  private val random = SecureRandom()

  def create(length: Int = 12): Array[Char] =
    random.ints(length, 'A', '~' + 1).toArray.map(_.toChar)
