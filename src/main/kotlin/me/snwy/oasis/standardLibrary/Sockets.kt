package me.snwy.oasis.standardLibrary

import me.snwy.oasis.Interpreter
import me.snwy.oasis.KotlinFunction0
import me.snwy.oasis.KotlinFunction1
import me.snwy.oasis.OasisPrototype
import java.net.ServerSocket
import java.net.Socket

fun constructSocket(interpreter: Interpreter, port: Double): OasisPrototype {
    val proto = OasisPrototype(base, -1, interpreter)
    proto.set("__socket", ServerSocket(port.toInt()))
    proto.set("port", port)
    proto.set("accepting", true)
    proto.set("waitForConnection", KotlinFunction0 { interpreter ->
        val clientSocket = (proto.get("__socket") as ServerSocket).accept()
        val connectionProto = OasisPrototype(base, -1, interpreter).apply {
            set("address", clientSocket.inetAddress.toString())
            set("bsend", KotlinFunction1<Unit, ByteArray>(clientSocket.getOutputStream()::write))
            set("send", KotlinFunction1<Unit, String> { it -> clientSocket.getOutputStream().write(it.toByteArray()) })
            set("read", KotlinFunction0 { _ -> clientSocket.getInputStream().readBytes().asList() })
            set("sread", KotlinFunction0 { _ -> String(clientSocket.getInputStream().readBytes()) })
            set("close", KotlinFunction0 { _ -> clientSocket.close(); set("accepting", false) })
        }
        connectionProto
    })
    return proto
}

fun socketConnect(interpreter: Interpreter, port: Double, ip: String): OasisPrototype {
    val connectionProto = OasisPrototype(base, -1, interpreter)
    connectionProto.set("__socket", Socket(ip, port.toInt()))
    connectionProto.set("accepting", true)
    val sock = connectionProto.get("__socket") as Socket
    connectionProto.apply {
        set("address", sock.inetAddress.toString())
        set("bsend", KotlinFunction1<Unit, ByteArray>(sock.getOutputStream()::write))
        set("send", KotlinFunction1<Unit, String> { it -> sock.getOutputStream().write(it.toByteArray()) })
        set("read", KotlinFunction0 { _ -> sock.getInputStream().readBytes().asList() })
        set("sread", KotlinFunction0 { _ -> String(sock.getInputStream().readBytes()) })
        set("close", KotlinFunction0 { _ -> sock.close(); connectionProto.set("accepting", false) })
    }
    return connectionProto
}