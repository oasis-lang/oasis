package me.snwy.oasis.standardLibrary

import me.snwy.oasis.KotlinFunction0
import me.snwy.oasis.KotlinFunction1
import me.snwy.oasis.KotlinFunction2
import me.snwy.oasis.OasisCallable
import me.snwy.oasis.OasisPrototype
import java.nio.file.Files
import java.nio.file.Path

val io = Module("io") { it, interpreter ->
    val io = OasisPrototype(base, -1, interpreter)
    val socket = OasisPrototype(base, -1, interpreter)

    io.set("print", KotlinFunction1<Unit, Any?> { z -> println(z.toString()) })
    io.set("read", KotlinFunction0(::readLine))
    io.set("readc", KotlinFunction0 { _ -> return@KotlinFunction0 Char(System.`in`.read()) })
    io.set("open", KotlinFunction1<String, String>{ it -> Files.readString(Path.of(it))})
    io.set("write", KotlinFunction2<Unit, String, String>{ z, y -> Files.writeString(Path.of(z), y)})
    io.set("printf", KotlinFunction2<Unit, String, OasisCallable> { interpreter, z, y -> print(interpreter.let { it1 ->
        y.call(
            it1, listOf(z))
    })})
    socket.set("open", KotlinFunction1(::constructSocket))
    socket.set("connect", KotlinFunction2(::socketConnect))
    io.set("socket", socket)

    it.define("io", io)
}