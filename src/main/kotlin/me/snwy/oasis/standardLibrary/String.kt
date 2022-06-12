package me.snwy.oasis.standardLibrary

import me.snwy.oasis.KotlinFunction1
import me.snwy.oasis.KotlinFunction2
import me.snwy.oasis.KotlinFunction3
import me.snwy.oasis.OasisPrototype
import java.util.*

val string = Module("string") { it, interpreter ->
    val string = OasisPrototype(base, -1, interpreter)
    string.set("size", KotlinFunction1<Double, String> { z -> z.length.toDouble() })
    string.set("substring", KotlinFunction3<String, String, Double, Double> { x, y, z -> x.substring(y.toInt(), z.toInt()) })
    string.set("toUpperCase", KotlinFunction1<String, String> { z -> z.uppercase(Locale.getDefault()) })
    string.set("toLowerCase", KotlinFunction1<String, String> { z -> z.lowercase(Locale.getDefault()) })
    string.set("trim", KotlinFunction1<String, String> { z -> z.trim() })
    string.set("trimStart", KotlinFunction1<String, String> { z -> z.trimStart() })
    string.set("trimEnd", KotlinFunction1<String, String> { z -> z.trimEnd() })
    string.set("replace", KotlinFunction3<String, String, String, String> {x, y, z -> x.replace(y, z) })
    string.set("split", KotlinFunction2<ArrayList<Any?>, String, String> { x, y -> x.split(y).filter { it != y } as ArrayList<Any?> })
    string.set("contains", KotlinFunction2<Boolean, String, String> { x, y -> x.contains(y) })
    string.set("startsWith", KotlinFunction2<Boolean, String, String> { x, y -> x.startsWith(y) })
    string.set("endsWith", KotlinFunction2<Boolean, String, String> { x, y -> x.endsWith(y) })
    string.set("toCharArray", KotlinFunction1<ArrayList<Any?>, String> { z -> z.toCharArray().toList() as ArrayList<Any?> })
    string.set("isNum", KotlinFunction1<Boolean, String> { x ->
        try {
            val num = x.toDouble()
            return@KotlinFunction1 true
        } catch (e: NumberFormatException) {
            return@KotlinFunction1 false
        }
    })
    it.define("string", string)
}