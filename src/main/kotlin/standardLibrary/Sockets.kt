package standardLibrary

import KotlinFunction0
import KotlinFunction1
import OasisPrototype
import java.net.*

fun constructSocket(port: Double): OasisPrototype {
    val proto = OasisPrototype(base, -1)
    proto.set("__socket", ServerSocket(port.toInt()))
    proto.set("port", port)
    proto.set("accepting", true)
    proto.set("waitForConnection", KotlinFunction0 {
        val clientSocket = (proto.get("__socket") as ServerSocket).accept()
        val connectionProto = OasisPrototype(base, -1).apply {
            set("address", clientSocket.inetAddress.toString())
            set("bsend", KotlinFunction1<Unit, ByteArray>(clientSocket.getOutputStream()::write))
            set("send", KotlinFunction1<Unit, String>{ clientSocket.getOutputStream().write(it.toByteArray())})
            set("read", KotlinFunction0{clientSocket.getInputStream().readBytes().asList()})
            set("sread", KotlinFunction0{String(clientSocket.getInputStream().readBytes())})
            set("close", KotlinFunction0{clientSocket.close(); set("accepting", false)})
        }
        connectionProto
    })
    return proto
}

fun socketConnect(port: Double, ip: String): OasisPrototype {
    val connectionProto = OasisPrototype(base, -1)
    connectionProto.set("__socket", Socket(ip, port.toInt()))
    connectionProto.set("accepting", true)
    val sock = connectionProto.get("__socket") as Socket
    connectionProto.apply {
        set("address", sock.inetAddress.toString())
        set("bsend", KotlinFunction1<Unit, ByteArray>(sock.getOutputStream()::write))
        set("send", KotlinFunction1<Unit, String>{ sock.getOutputStream().write(it.toByteArray())})
        set("read", KotlinFunction0{sock.getInputStream().readBytes().asList()})
        set("sread", KotlinFunction0{String(sock.getInputStream().readBytes())})
        set("close", KotlinFunction0{sock.close(); connectionProto.set("accepting", false)})
    }
    return connectionProto
}