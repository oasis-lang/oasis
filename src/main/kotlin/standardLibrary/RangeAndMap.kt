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

fun range(base: Double, ceil: Double): ArrayList<Double> {
    return if(base > ceil) {
        ArrayList(
            (ceil.toInt() .. base.toInt())
                .iterator()
                .asSequence()
                .toList()
                .reversed()).map { x -> x.toDouble() } as ArrayList<Double>
    } else {
        ArrayList(
            (base.toInt() .. ceil.toInt())
                .iterator()
                .asSequence()
                .toList()).map { x -> x.toDouble() } as ArrayList<Double>
    }
}

fun createRange(vals: ArrayList<Double>) : OasisPrototype {
    val rangeProto = OasisPrototype(base, -1)
    rangeProto.set("iter", KotlinFunction1<OasisPrototype, OasisCallable> {
        func -> createRange(vals.map {x -> func.call(globalInterpreter!!, listOf(x))} as ArrayList<Double>)
    })
    rangeProto.set("list", vals)
    rangeProto.set("toString", KotlinFunction0 {rangeProto.get("list").toString()})
    return rangeProto
}

fun rangeFun(base: Double, ceil: Double) = createRange(range(base, ceil))