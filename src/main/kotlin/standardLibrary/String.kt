package standardLibrary

import KotlinFunction1
import OasisPrototype

val string = Module("string") {
    val string = OasisPrototype(base, -1)
    string.set("size", KotlinFunction1<Double, String> { z -> z.length.toDouble() })
    it.define("string", string)
}