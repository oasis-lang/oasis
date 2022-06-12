package me.snwy.oasis.standardLibrary

import me.snwy.oasis.KotlinFunction1
import me.snwy.oasis.OasisPrototype
import kotlin.system.exitProcess

val sys = Module("sys") { it, interpreter ->
    val sys = OasisPrototype(base, -1, interpreter)
    sys.set("exit", KotlinFunction1(::exitProcess))
    it.define("sys", sys)
}