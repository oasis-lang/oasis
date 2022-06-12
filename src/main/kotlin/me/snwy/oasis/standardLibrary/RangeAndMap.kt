package me.snwy.oasis.standardLibrary

import me.snwy.oasis.*

val range = Module("range") {it, _ ->
    it.define("range", KotlinFunction2(::rangeFn))
}

fun rangeFn(base: Double, ceil: Double): ArrayList<Any?> {
    return if(base > ceil) {
        ArrayList(
            (ceil.toInt() .. base.toInt())
                .iterator()
                .asSequence()
                .toList()
                .reversed()).map { x -> x.toDouble() } as ArrayList<Any?>
    } else {
        ArrayList(
            (base.toInt() .. ceil.toInt())
                .iterator()
                .asSequence()
                .toList()).map { x -> x.toDouble() } as ArrayList<Any?>
    }
}