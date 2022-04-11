package standardLibrary

import KotlinFunction2
import OasisCallable
import OasisPrototype
import globalInterpreter

val func = Module("func") {
    val func = OasisPrototype(base, -1).apply {
        set("map", KotlinFunction2<Collection<Any?>, Collection<Any?>, OasisCallable> { x, y ->
            x.map {
                    z -> globalInterpreter?.let {
                        it1 -> y.call(it1, listOf(z))
                    }
            }
        })
        set("reduce", KotlinFunction2<Any?, Collection<Any?>, OasisCallable> { x, y ->
            x.reduce { a, b ->
                globalInterpreter?.let {
                        it1 -> y.call(it1, listOf(a, b))
                }
            }
        })
    }
    it.define("func", func)
}