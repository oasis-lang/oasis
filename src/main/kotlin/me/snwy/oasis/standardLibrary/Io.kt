package me.snwy.oasis.standardLibrary

import me.snwy.oasis.KotlinFunction0
import me.snwy.oasis.KotlinFunction1
import me.snwy.oasis.KotlinFunction2
import me.snwy.oasis.OasisCallable
import me.snwy.oasis.OasisPrototype
import java.io.File
import java.io.FileReader
import java.io.RandomAccessFile
import java.nio.charset.Charset
import java.nio.file.Files
import java.nio.file.Path

val io = Module("io") { it, interpreter ->
    val io = OasisPrototype(base, -1, interpreter)
    val socket = OasisPrototype(base, -1, interpreter)

    io.set("print", KotlinFunction1<Unit, Any?> { z -> println(z.toString()) })
    io.set("read", KotlinFunction0(::readLine))
    io.set("readc", KotlinFunction0 { _ -> return@KotlinFunction0 Char(System.`in`.read()) })
    io.set("open", KotlinFunction2<OasisPrototype, String, String> { interpreter, s, r ->
        return@KotlinFunction2 OasisPrototype(base, -1, interpreter).apply {
            set("__file", RandomAccessFile(s, r))
            set("readStr", KotlinFunction0 { _ ->
                val result = ByteArray((get("__file") as RandomAccessFile).length().toInt())
                (get("__file") as RandomAccessFile).readFully(result)
                result.toString(Charset.defaultCharset())
            })
            set("readBytes", KotlinFunction0 { _ ->
                val result = ByteArray((get("__file") as RandomAccessFile).length().toInt())
                (get("__file") as RandomAccessFile).readFully(result)
                result
            })
            set("seek", KotlinFunction1<Unit, Double> { x ->
                (get("__file") as RandomAccessFile).seek(x.toLong())
            })
            set("readAtHead", KotlinFunction0 { _ ->
                (get("__file") as RandomAccessFile).read().toUByte()
            })
            set("writeAtHead", KotlinFunction1<Unit, UByte> { x ->
                (get("__file") as RandomAccessFile).write(x.toInt())
            })
            set("writeStr", KotlinFunction1<Unit, String> { x ->
                (get("__file") as RandomAccessFile).write(x.toByteArray(Charset.defaultCharset()))
            })
            set("writeBytes", KotlinFunction1<Unit, ByteArray> { x ->
                (get("__file") as RandomAccessFile).write(x)
            })
            set("close", KotlinFunction0 { _ ->
                (get("__file") as RandomAccessFile).close()
            })
        }
    })
    io.set("printf", KotlinFunction2<Unit, String, OasisCallable> { interpreter, z, y -> print(interpreter.let { it1 ->
        y.call(
            it1, listOf(z))
    })})
    socket.set("open", KotlinFunction1(::constructSocket))
    socket.set("connect", KotlinFunction2(::socketConnect))
    io.set("socket", socket)


    it.define("io", io)
}