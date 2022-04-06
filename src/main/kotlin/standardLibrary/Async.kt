package standardLibrary

import KotlinFunction0
import KotlinFunction1
import KotlinFunction2
import OasisCallable
import OasisPrototype
import globalInterpreter
import kotlin.concurrent.thread

val async = Module("async") {
    val async = OasisPrototype(base, -1).apply {
        set("run", KotlinFunction2<OasisPrototype, OasisCallable, ArrayList<Any?>> { x, y ->
            OasisPrototype(base, -1).apply {
                set("__thread", thread {
                    globalInterpreter?.let { it1 -> x.call(it1, y) }
                })
                set("run", KotlinFunction0((get("__thread") as Thread)::run))
                set("stop", KotlinFunction0((get("__thread") as Thread)::interrupt))
                set("alive", KotlinFunction0((get("__thread") as Thread)::isAlive))
            }
        })
        set("sleep", KotlinFunction1<Unit, Double> {
            Thread.sleep(it.toLong())
        })
    }
    it.define("async", async)
}