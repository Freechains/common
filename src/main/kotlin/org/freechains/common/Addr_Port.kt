package org.freechains.common

typealias Addr_Port = Pair<String,Int>

fun String.to_Addr_Port () : Addr_Port {
    val lst = this.split(":")
    return when (lst.size) {
        0    -> Addr_Port("localhost", PORT_8330)
        1    -> Addr_Port(lst[0],      PORT_8330)
        else -> Addr_Port(lst[0],      lst[1].toInt())
    }
}

fun Addr_Port.toColon () : String {
    return "$first:$second"
}