package me.snwy.oasis.standardLibrary

import me.snwy.oasis.*

val type = Module("type") { it, interpreter ->
    val type = OasisPrototype(base, -1, interpreter)
    type.set("string", KotlinFunction1<String, Any?> { z -> z.toString() })
    type.set("char", KotlinFunction1<Char, Any?> { z -> z as Char })
    type.set("num", KotlinFunction1<Double, Any?> { z -> z.toString().toDouble() })
    type.set("bytes", KotlinFunction1<ByteArray, Any?> { z ->
        when (z) {
            is List<*> -> {
                val r = ByteArray(z.size)
                var li = 0
                z.map {v -> r[li] = when (v) {
                    is Double -> v.toRawBits().toByte()
                    is Int -> v.toByte()
                    is Boolean -> if (v) 0x1 else 0x0
                    is Char -> v.code.toByte()
                    else -> throw RuntimeError(line,"'${v} cannot be converted to byte")
                }; li += 1}
                return@KotlinFunction1 r
            }
            is String -> {
                val r = ByteArray(z.length)
                var li = 0
                z.map {v -> r[li] = v.code.toByte(); li += 1}
                return@KotlinFunction1 r
            }
            else -> {
                throw RuntimeError(line,"'${z.toString()}' cannot be represented as a bytes-object")
            }
        }
    })
    type.set("isString", KotlinFunction1<Boolean, Any?> { x ->
        return@KotlinFunction1 x is String
    })
    type.set("isNum", KotlinFunction1<Boolean, Any?> { x ->
        return@KotlinFunction1 x is Double || x is Int || x is Long || x is UInt
    })
    type.set("isChar", KotlinFunction1<Boolean, Any?> { x ->
        return@KotlinFunction1 x is Char
    })
    type.set("isBool", KotlinFunction1<Boolean, Any?> { x ->
        return@KotlinFunction1 x is Boolean
    })
    type.set("isList", KotlinFunction1<Boolean, Any?> { x ->
        return@KotlinFunction1 x is List<*> || x is ArrayList<*> || x is MutableList<*>
    })
    type.set("isBytes", KotlinFunction1<Boolean, Any?> { x ->
        return@KotlinFunction1 x is ByteArray
    })
    type.set("isNull", KotlinFunction1<Boolean, Any?> { x ->
        return@KotlinFunction1 x == null
    })
    type.set("isObject", KotlinFunction1<Boolean, Any?> { x ->
        return@KotlinFunction1 x is OasisPrototype
    })
    type.set("isFunction", KotlinFunction1<Boolean, Any?> { x ->
        return@KotlinFunction1 x is OasisCallable
    })
    type.set("typeName", KotlinFunction1<String, Any?> { x ->
        if (x != null) {
            return@KotlinFunction1 x.javaClass.simpleName
        } else {
            return@KotlinFunction1 "null"
        }
    })
    it.define("type", type)
}