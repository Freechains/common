package org.freechains.common

import java.net.Socket
import java.io.DataInputStream
import java.io.DataOutputStream
import java.net.ConnectException
import java.net.SocketTimeoutException

typealias HKey = String

const val MAJOR    = 0
const val MINOR    = 6
const val REVISION = 2
const val VERSION  = "v$MAJOR.$MINOR.$REVISION"
const val PRE      = "FC $VERSION"

const val PORT_8330 = 8888  // TODO: back to 8330

const val ms   = 1.toLong()
const val sec  = 1000* ms
const val min  = 60* sec
const val hour = 60* min
const val day  = 24* hour

fun Socket_5s (addr: String, port: Int) : Socket {
    val s = Socket(addr,port)
    s.soTimeout = 5000
    return s
}

inline fun assert_ (value: Boolean, lazyMessage: () -> Any = {"Assertion failed"}) {
    if (!value) {
        val message = lazyMessage()
        throw AssertionError(message)
    }
}

fun catch_all (def: String, f: ()->Pair<Boolean,String>): Pair<Boolean,String> {
    try {
        return f()
    } catch (e: AssertionError) {
        if (e.message.equals("Assertion failed")) {
            return Pair(false, "! TODO - $e - ${e.message} - $def")
        } else {
            return Pair(false, "! " + e.message!!)
        }
    } catch (e: ConnectException) {
        assert_(e.message == "Connection refused (Connection refused)")
        return Pair(false, "! connection refused")
    } catch (e: SocketTimeoutException) {
        return Pair(false, "! connection timeout")
    } catch (e: Throwable) {
        return Pair(false, "! TODO - $e - ${e.message} - $def")
    }
}

/*
 * (\n) = (0xA)
 * (\r) = (0xD)
 * Windows = (\r\n)
 * Unix = (\n)
 * Mac = (\r)
 */

fun DataInputStream.readLineX () : String {
    val ret = mutableListOf<Byte>()
    while (true) {
        val c = this.readByte()
        if (c == '\r'.toByte()) {
            assert_(this.readByte() == '\n'.toByte())
            break
        }
        if (c == '\n'.toByte()) {
            break
        }
        ret.add(c)
    }
    return ret.toByteArray().toString(Charsets.UTF_8)
}

fun DataOutputStream.writeLineX (v: String) {
    this.writeBytes(v)
    this.writeByte('\n'.toInt())
}

fun Array<String>.cmds_opts () : Pair<List<String>,Map<String,String?>> {
    val cmds = this.filter { !it.startsWith("--") }
    val opts = this
            .filter { it.startsWith("--") }
            .map {
                if (it.contains('=')) {
                    val (k,v) = Regex("(--.*)=(.*)").find(it)!!.destructured
                    Pair(k,v)
                } else {
                    Pair(it, null)
                }
            }
            .toMap()
    return Pair(cmds,opts)
}
