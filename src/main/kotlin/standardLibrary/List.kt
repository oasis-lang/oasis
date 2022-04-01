package standardLibrary

import KotlinFunction1
import KotlinFunction2
import OasisPrototype

val list = Module("list") {
    val list = OasisPrototype(base, -1)
    list.set("add", KotlinFunction2<Unit, ArrayList<Any?>, Any?> { z, y -> z.add(y) })
    list.set("size", KotlinFunction1<Double, ArrayList<Any?>> { z -> z.size.toDouble() })
    list.set("remove",
        KotlinFunction2<Any?, ArrayList<Any?>, Double> { z, y ->
            val tZ = z[y.toInt()]; z.remove(y); return@KotlinFunction2 tZ
        })
    it.define("list", list)
}