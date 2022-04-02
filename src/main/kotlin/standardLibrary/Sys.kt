package standardLibrary

import KotlinFunction1
import OasisPrototype
import kotlin.system.exitProcess

val sys = Module("sys") {
    val sys = OasisPrototype(base, -1)
    sys.set("exit", KotlinFunction1(::exitProcess))
    it.define("sys", sys)
}