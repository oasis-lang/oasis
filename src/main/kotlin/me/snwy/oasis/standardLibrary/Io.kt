package me.snwy.oasis.standardLibrary

import com.fazecast.jSerialComm.SerialPort
import me.snwy.oasis.*
import java.io.RandomAccessFile
import java.nio.charset.Charset

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
    io.set("printf", KotlinFunction2<Unit, String, OasisCallable> { interpreter, z, y ->
        print(interpreter.let { it1 ->
            y.call(
                it1, listOf(z)
            )
        })
    })
    io.set("serial", KotlinFunction0 { interpreter ->
        val result = arrayListOf<Any?>()
        SerialPort.getCommPorts().forEach {
            result.add(OasisPrototype(base, line, interpreter).apply {
                set("__port", it)
                set("name", it.systemPortName)
                set("description", it.portDescription)
                set("isOpen", KotlinFunction0(it::isOpen))
                set("open", KotlinFunction0(it::openPort))
                set("close", KotlinFunction0(it::closePort))
                set(
                    "writeStr",
                    KotlinFunction1<Unit, String> { x ->
                        it.writeBytes(
                            x.toByteArray(Charset.defaultCharset()),
                            x.length.toLong()
                        )
                    })
                set("writeBytes", KotlinFunction1<Unit, ByteArray> { x -> it.writeBytes(x, x.size.toLong()) })
                set("readBytes", KotlinFunction1<ByteArray, Double> { x ->
                    val result = ByteArray(x.toInt())
                    it.readBytes(result, x.toLong())
                    result
                })
                set("readStr", KotlinFunction1<String, Double> { x ->
                    val result = ByteArray(x.toInt())
                    it.readBytes(result, x.toLong())
                    result.toString(Charset.defaultCharset())
                })
                set("writeByte", KotlinFunction1<Unit, UByte> { x -> it.writeBytes(byteArrayOf(x.toByte()), 1) })
                set("readByte", KotlinFunction0 { _ ->
                    val result = ByteArray(1)
                    it.readBytes(result, 1)
                    result[0].toUByte()
                })
            })
        }
        result
    })
    socket.set("open", KotlinFunction1(::constructSocket))
    socket.set("connect", KotlinFunction2(::socketConnect))
    io.set("socket", socket)


    it.define("io", io)
}