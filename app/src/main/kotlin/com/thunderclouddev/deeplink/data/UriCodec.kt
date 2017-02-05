/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.thunderclouddev.deeplink.data

import java.io.ByteArrayOutputStream
import java.net.URISyntaxException
import java.nio.charset.Charset

/**
 * Encodes and decodes `application/x-www-form-urlencoded` content.
 * Subclasses define exactly which characters are legal.
 *
 *
 *
 * By default, UTF-8 is used to encode escaped characters. A single input
 * character like "\u0080" may be encoded to multiple octets like %C2%80.
 */
abstract class UriCodec {
    /**
     * Returns true if `c` does not need to be escaped.
     */
    internal abstract fun isRetained(c: Char): Boolean

    /**
     * Throws if `s` is invalid according to this encoder.
     */
    @Throws(URISyntaxException::class)
    fun validate(uri: String, start: Int, end: Int, name: String): String {
        var i = start
        while (i < end) {
            val ch = uri[i]
            if (ch >= 'a' && ch <= 'z'
                    || ch >= 'A' && ch <= 'Z'
                    || ch >= '0' && ch <= '9'
                    || isRetained(ch)) {
                i++
            } else if (ch == '%') {
                if (i + 2 >= end) {
                    throw URISyntaxException(uri, "Incomplete % sequence in " + name, i)
                }
                val d1 = hexToInt(uri[i + 1])
                val d2 = hexToInt(uri[i + 2])
                if (d1 == -1 || d2 == -1) {
                    throw URISyntaxException(uri, "Invalid % sequence: "
                            + uri.substring(i, i + 3) + " in " + name, i)
                }
                i += 3
            } else {
                throw URISyntaxException(uri, "Illegal character in " + name, i)
            }
        }
        return uri.substring(start, end)
    }

    /**
     * Encodes `s` and appends the result to `builder`.

     * @param isPartiallyEncoded true to fix input that has already been
     * *                           partially or fully encoded. For example, input of "hello%20world" is
     * *                           unchanged with isPartiallyEncoded=true but would be double-escaped to
     * *                           "hello%2520world" otherwise.
     */
    private fun appendEncoded(builder: StringBuilder, s: String?, charset: Charset,
                              isPartiallyEncoded: Boolean) {
        if (s == null) {
            throw NullPointerException("s == null")
        }
        var escapeStart = -1
        var i = 0
        while (i < s.length) {
            val c = s[i]
            if (c >= 'a' && c <= 'z'
                    || c >= 'A' && c <= 'Z'
                    || c >= '0' && c <= '9'
                    || isRetained(c)
                    || c == '%' && isPartiallyEncoded) {
                if (escapeStart != -1) {
                    appendHex(builder, s.substring(escapeStart, i), charset)
                    escapeStart = -1
                }
                if (c == '%' && isPartiallyEncoded) {
                    // this is an encoded 3-character sequence like "%20"
                    builder.append(s, i, Math.min(i + 3, s.length))
                    i += 2
                } else if (c == ' ') {
                    builder.append('+')
                } else {
                    builder.append(c)
                }
            } else if (escapeStart == -1) {
                escapeStart = i
            }
            i++
        }
        if (escapeStart != -1) {
            appendHex(builder, s.substring(escapeStart, s.length), charset)
        }
    }

    fun encode(s: String, charset: Charset): String {
        // Guess a bit larger for encoded form
        val builder = StringBuilder(s.length + 16)
        appendEncoded(builder, s, charset, false)
        return builder.toString()
    }

    fun appendEncoded(builder: StringBuilder, s: String) {
        appendEncoded(builder, s, Charsets.UTF_8, false)
    }

    fun appendPartiallyEncoded(builder: StringBuilder, s: String) {
        appendEncoded(builder, s, Charsets.UTF_8, true)
    }

    companion object {

        /**
         * Throws if `s` contains characters that are not letters, digits or
         * in `legal`.
         */
        @Throws(URISyntaxException::class)
        fun validateSimple(s: String, legal: String) {
            for (i in 0..s.length - 1) {
                val ch = s[i]
                if (!(ch >= 'a' && ch <= 'z'
                        || ch >= 'A' && ch <= 'Z'
                        || ch >= '0' && ch <= '9'
                        || legal.indexOf(ch.toInt().toString()) > -1)) {
                    throw URISyntaxException(s, "Illegal character", i)
                }
            }
        }

        /**
         * @param convertPlus    true to convert '+' to ' '.
         * *
         * @param throwOnFailure true to throw an IllegalArgumentException on
         * *                       invalid escape sequences; false to replace them with the replacement
         * *                       character (U+fffd).
         */
        @JvmOverloads fun decode(s: String, convertPlus: Boolean = false, charset: Charset = Charsets.UTF_8,
                                 throwOnFailure: Boolean = true): String {
            if (s.indexOf('%') == -1 && (!convertPlus || s.indexOf('+') == -1)) {
                return s
            }
            val result = StringBuilder(s.length)
            val out = ByteArrayOutputStream()
            var i = 0
            while (i < s.length) {
                var c = s[i]
                if (c == '%') {
                    do {
                        val d1 = hexToInt(s[i + 1])
                        val d2 = hexToInt(s[i + 2])
                        if (i + 2 < s.length && d1 != -1 && d2 != -1) {
                            out.write(((d1 shl 4) + d2).toByte().toInt())
                        } else if (throwOnFailure) {
                            throw IllegalArgumentException("Invalid % sequence at $i: $s")
                        } else {
                            val replacement = "\ufffd".toByteArray(charset)
                            out.write(replacement, 0, replacement.size)
                        }
                        i += 3
                    } while (i < s.length && s[i] == '%')
                    result.append(String(out.toByteArray(), charset))
                    out.reset()
                } else {
                    if (convertPlus && c == '+') {
                        c = ' '
                    }
                    result.append(c)
                    i++
                }
            }
            return result.toString()
        }

        /**
         * Like [Character.digit], but without support for non-ASCII
         * characters.
         */
        private fun hexToInt(c: Char): Int {
            if ('0' <= c && c <= '9') {
                return c - '0'
            } else if ('a' <= c && c <= 'f') {
                return 10 + (c - 'a')
            } else if ('A' <= c && c <= 'F') {
                return 10 + (c - 'A')
            } else {
                return -1
            }
        }

        private fun appendHex(builder: StringBuilder, s: String, charset: Charset) {
            for (b in s.toByteArray(charset)) {
                appendHex(builder, b)
            }
        }

        private fun appendHex(sb: StringBuilder, b: Byte) {
            sb.append('%')
            sb.append(bytesToHex(byteArrayOf(b)))
        }

        private val hexArray = "0123456789ABCDEF".toCharArray()

        private fun bytesToHex(bytes: ByteArray): String {
            val hexChars = CharArray(bytes.size * 2)
            for (j in bytes.indices) {
                val v = bytes[j].toInt() and 0xFF
                hexChars[j * 2] = hexArray[v.ushr(4)]
                hexChars[j * 2 + 1] = hexArray[v and 0x0F]
            }
            return String(hexChars)
        }
    }
}