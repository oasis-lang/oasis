package me.snwy.oasis.standardLibrary

import me.snwy.oasis.KotlinFunction2

val range = Module("range") { it, _ ->
    it.define("range", KotlinFunction2(::rangeFn))
}

fun rangeFn(base: Number, ceil: Number): ArrayList<Any?> {
    return if (base.toInt() > ceil.toInt()) {
        ArrayList(
            (ceil.toInt()..base.toInt())
                .iterator()
                .asSequence()
                .toList()
                .reversed()
        )
    } else {
        ArrayList(
            (base.toInt()..ceil.toInt())
                .iterator()
                .asSequence()
                .toList()
        )
    }
}