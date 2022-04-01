package standardLibrary

import KotlinFunction0
import KotlinFunction1
import KotlinFunction2
import OasisCallable
import OasisPrototype
import globalInterpreter
import java.nio.file.Files
import java.nio.file.Path

val io = Module("io") {
    val io = OasisPrototype(base, -1)
    val socket = OasisPrototype(base, -1)

    io.set("print", KotlinFunction1<kotlin.Unit, kotlin.Any?> { z -> kotlin.io.println(z.toString()) })
    io.set("read", KotlinFunction0(::readLine))
    io.set("readc", KotlinFunction0 { return@KotlinFunction0 Char(System.`in`.read()) })
    io.set("open", KotlinFunction1<String, String>{ Files.readString(Path.of(it))})
    io.set("open", KotlinFunction2<Unit, String, String>{ z, y -> Files.writeString(Path.of(z), y)})
    io.set("printf", KotlinFunction2<Unit, String, OasisCallable> { z, y -> print(globalInterpreter?.let { it1 ->
        y.call(
            it1, listOf(z))
    })})
    socket.set("open", KotlinFunction1(::constructSocket))
    socket.set("connect", KotlinFunction2(::socketConnect))
    io.set("socket", socket)

    it.define("io", io)
}