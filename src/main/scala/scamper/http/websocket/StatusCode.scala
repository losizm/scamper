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
package websocket

/**
 * Defines status code for WebSocket closure.
 *
 * @see [[StatusCode.Registry]]
 */
trait StatusCode:
  /** Gets value. */
  def value: Int

  /** Gets meaning. */
  def meaning: String

  /** Indicator for reserved value. */
  def isReserved: Boolean

  /** Converts value to 2-byte data array. */
  def toData: Array[Byte] =
    Array(
      { (0xff00 & value) >> 8 }.toByte,
      { (0x00ff & value) >> 0 }.toByte
    )

/**
 * Provides factory methods and registry for `StatusCode`.
 *
 * @see [[StatusCode.Registry]]
 */
object StatusCode:
  /**
   * Contains registered WebSocket status codes.
   *
   * @note The status code descriptions are taken in part from those provided in
   * [[https://tools.ietf.org/rfc/rfc6455.txt RFC 6455]].
   */
  object Registry:
    /**
     * 1000 indicates a normal closure, meaning that the purpose for which the
     * connection was established has been fulfilled.
     */
    val NormalClosure: StatusCode = StatusCodeImpl(1000, "Normal Closure", false)

    /**
     * 1001 indicates that an endpoint is "going away", such as a server going
     * down or a browser having navigated away from a page.
     */
    val GoingAway: StatusCode = StatusCodeImpl(1001, "Going Away", false)

    /**
     * 1002 indicates that an endpoint is terminating the connection due to a
     * protocol error.
     */
    val ProtocolError: StatusCode = StatusCodeImpl(1002, "Protocol Error", false)

    /**
     * 1003 indicates that an endpoint is terminating the connection because it
     * has received a type of data it cannot accept (e.g., an endpoint that
     * understands only text data MAY send this if it receives a binary
     * message).
     */
    val UnsupportedData: StatusCode = StatusCodeImpl(1003, "Unsupported Data", false)

    /**
     * 1004 is reserved. The specific meaning might be defined in the future.
     */
    val Reserved: StatusCode = StatusCodeImpl(1004, "Reserved", true)

    /**
     * 1005 is a reserved value and _must not_ be set as a status code in a
     * Close control frame by an endpoint. It is designated for use in
     * applications expecting a status code to indicate that no status code was
     * actually present.
     */
    val NoStatusReceived: StatusCode = StatusCodeImpl(1005, "No Status Received", true)

    /**
     * 1006 is a reserved value and _must not_ be set as a status code in a
     * Close control frame by an endpoint. It is designated for use in
     * applications expecting a status code to indicate that the connection was
     * closed abnormally, e.g., without sending or receiving a Close control
     * frame.
     */
    val AbnormalClosure: StatusCode = StatusCodeImpl(1006, "Abnormal Closure", true)

    /**
     * 1007 indicates that an endpoint is terminating the connection because it
     * has received data within a message that was not consistent with the type
     * of the message (e.g., non-UTF-8 data within a text message).
     */
    val InvalidFramePayload: StatusCode = StatusCodeImpl(1007, "Invalid Frame Payload Data", false)

    /**
     * 1008 indicates that an endpoint is terminating the connection because it
     * has received a message that violates its policy. This is a generic status
     * code that can be returned when there is no other more suitable status
     * code (e.g., 1003 or 1009) or if there is a need to hide specific details
     * about the policy.
     */
    val PolicyViolation: StatusCode = StatusCodeImpl(1008, "Policy Violation", false)

    /**
     * 1009 indicates that an endpoint is terminating the connection because it
     * has received a message that is too big for it to process.
     */
    val MessageTooBig: StatusCode = StatusCodeImpl(1009, "Message Too Big", false)

    /**
     * 1010 indicates that an endpoint (client) is terminating the connection
     * because it has expected the server to negotiate one or more extension,
     * but the server didn't return them in the response message of the
     * WebSocket handshake. The list of extensions that are needed SHOULD appear
     * in the reason part of the Close frame. Note that this status code is not
     * used by the server, because it can fail the WebSocket handshake instead.
     */
    val MandatoryExtension: StatusCode = StatusCodeImpl(1010, "Mandatory Extension", false)

    /**
     * 1011 indicates that a server is terminating the connection because it
     * encountered an unexpected condition that prevented it from fulfilling the
     * request.
     */
    val InternalError: StatusCode = StatusCodeImpl(1011, "Internal Server Error", false)

    /**
     * 1015 is a reserved value and _must not_ be set as a status code in a
     * Close control frame by an endpoint. It is designated for use in
     * applications expecting a status code to indicate that the connection was
     * closed due to a failure to perform a TLS handshake (e.g., the server
     * certificate can't be verified).
     */
    val TlsHandshake: StatusCode = StatusCodeImpl(1015, "TLS Handshake", true)

  /** Gets status code for given value, if registered. */
  def get(value: Int): Option[StatusCode] =
    try Some(apply(value))
    catch case _: NoSuchElementException => None

  /**
   * Gets status code for supplied data, if registered.
   *
   * @note The data is converted to two-byte unsigned integer, which is then
   *  used to obtain status code.
   */
  def get(data: Array[Byte]): Option[StatusCode] =
    try Some(apply(data))
    catch
      case _: NoSuchElementException   => None
      case _: IllegalArgumentException => None

  /**
   * Gets registered status code for given value.
   *
   * @throws java.util.NoSuchElementException if value not registered
   */
  def apply(value: Int): StatusCode =
    value match
      case 1000 => Registry.NormalClosure
      case 1001 => Registry.GoingAway
      case 1002 => Registry.ProtocolError
      case 1003 => Registry.UnsupportedData
      case 1004 => Registry.Reserved
      case 1005 => Registry.NoStatusReceived
      case 1006 => Registry.AbnormalClosure
      case 1007 => Registry.InvalidFramePayload
      case 1008 => Registry.PolicyViolation
      case 1009 => Registry.MessageTooBig
      case 1010 => Registry.MandatoryExtension
      case 1011 => Registry.InternalError
      case 1015 => Registry.TlsHandshake
      case _    => throw new NoSuchElementException()

  /**
   * Gets registered status code for supplied data.
   *
   * @throws java.util.NoSuchElementException if value not registered
   * @throws java.lang.IllegalArgumentException if `data.size != 2`
   *
   * @note The data is converted to two-byte unsigned integer, which is then
   *  used to obtain status code.
   */
  def apply(data: Array[Byte]): StatusCode =
    data.size match
      case 2 => apply(BigInt(1, data).toInt)
      case _ => throw IllegalArgumentException()

private case class StatusCodeImpl(value: Int, meaning: String, isReserved: Boolean) extends StatusCode:
  override lazy val toString: String = s"$value ($meaning)"
