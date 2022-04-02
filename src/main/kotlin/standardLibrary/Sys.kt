package standardLibrary

import KotlinFunction1
import OasisPrototype
import kotlin.system.exitProcess

val sys = Module("sys") {
    var sys = OasisPrototype(base, -1)
    sys.set("exit", KotlinFunction1(::exitProcess))
    it.define("sys", sys)
}