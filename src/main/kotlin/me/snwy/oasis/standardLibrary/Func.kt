package me.snwy.oasis.standardLibrary

import me.snwy.oasis.*

val func = Module("func") { it, interpreter ->
    val func = OasisPrototype(base, -1, interpreter).apply {
        set("partial", KotlinFunction2<PartialFunc, OasisCallable, ArrayList<Any?>> { x, y ->
            return@KotlinFunction2 PartialFunc(x, y)
        })
        set("call", KotlinFunction2<Any?, OasisCallable, ArrayList<Any?>> { interpreter, x, y ->
            return@KotlinFunction2 x.call(interpreter, y)
        })

    }
    it.define("func", func)
}