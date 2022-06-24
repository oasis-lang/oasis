package me.snwy.oasis.standardLibrary

import me.snwy.oasis.*
import kotlin.concurrent.thread

val async = Module("async") { it, interpreter ->
    val async = OasisPrototype(base, -1, interpreter).apply {
        set("run", KotlinFunction2<OasisPrototype, OasisCallable, ArrayList<Any?>> { interpreter, x, y ->
            OasisPrototype(base, -1, interpreter).apply {
                set("__thread", thread {
                    interpreter.let { it1 -> x.call(it1, y) }
                })
                set("run", KotlinFunction0((get("__thread") as Thread)::run))
                set("stop", KotlinFunction0((get("__thread") as Thread)::interrupt))
                set("alive", KotlinFunction0((get("__thread") as Thread)::isAlive))
            }
        })
        set("sleep", KotlinFunction1<Unit, Double> { it ->
            Thread.sleep(it.toLong())
        })
    }
    it.define("async", async)
}