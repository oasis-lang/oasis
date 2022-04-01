package standardLibrary

import KotlinFunction0
import KotlinFunction1
import KotlinFunction2
import OasisCallable
import OasisPrototype
import globalInterpreter

val range = Module("range") {
    it.define("range", KotlinFunction2(::rangeFun))
}

fun range(base: Double, ceil: Double): ArrayList<Int> {
    return if(base > ceil) {
        ArrayList(
            (ceil.toInt() .. base.toInt())
                .iterator()
                .asSequence()
                .toList()
                .reversed())
    } else {
        ArrayList(
            (base.toInt() .. ceil.toInt())
                .iterator()
                .asSequence()
                .toList())
    }
}

fun createRange(vals: ArrayList<Int>) : OasisPrototype {
    var rangeProto = OasisPrototype(base, -1)
    rangeProto.set("iter", KotlinFunction1<OasisPrototype, OasisCallable> {
        func -> createRange(vals.map {x -> func.call(globalInterpreter!!, listOf(x))} as ArrayList<Int>)
    })
    rangeProto.set("list", vals)
    rangeProto.set("toString", KotlinFunction0 {rangeProto.get("list").toString()})
    return rangeProto
}

fun rangeFun(base: Double, ceil: Double) = createRange(range(base, ceil))