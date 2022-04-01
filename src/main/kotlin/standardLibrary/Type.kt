package standardLibrary

import KotlinFunction1
import OasisPrototype

val type = Module("type") {
    val type = OasisPrototype(base, -1)
    type.set("string", KotlinFunction1<kotlin.String, kotlin.Any?> { z -> z.toString() })
    type.set("char", KotlinFunction1<Char, Any?> { z -> z as Char })
    type.set("num", KotlinFunction1<Double, Any?> { z -> z.toString().toDouble() })
    type.set("bytes", KotlinFunction1<ByteArray, Any?> { z ->
        if (z is List<*>) {
            val r = ByteArray(z.size)
            var li = 0
            z.map {v -> r[li] = when (v) {
                is Double -> v.toInt().toByte()
                is Int -> v.toByte()
                is Boolean -> if (v) 0x1 else 0x0
                is Char -> v.code.toByte()
                else -> throw Exception("'${v} cannot be converted to byte")
            }; li += 1}
            return@KotlinFunction1 r
        } else if (z is String) {
            val r = ByteArray(z.length)
            var li = 0
            z.map {v -> r[li] = v.code.toByte(); li += 1}
            return@KotlinFunction1 r
        } else {
            throw Exception("'${z.toString()}' cannot be represented as a bytes-object")
        }
    })
    it.define("type", type)
}