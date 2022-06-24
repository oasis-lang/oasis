package me.snwy.oasis.standardLibrary

import me.snwy.oasis.*

val list = Module("list") { it, interpreter ->
    val list = OasisPrototype(base, -1, interpreter)
    list.set("add", KotlinFunction2<Unit, ArrayList<Any?>, Any?> { z, y -> z.add(y) })
    list.set("size", KotlinFunction1<Double, ArrayList<Any?>> { z -> z.size.toDouble() })
    list.set("remove",
        KotlinFunction2<Any?, ArrayList<Any?>, Double> { z, y ->
            return@KotlinFunction2 z.removeAt(y.toInt())
        })
    list.set("slice", KotlinFunction3<ArrayList<*>, ArrayList<Any?>, Double, Double> { x, y, z ->
        ArrayList(x.subList(y.toInt(), z.toInt()))
    })
    list.set("sliceFrom", KotlinFunction2<ArrayList<*>, ArrayList<Any?>, Double> { x, y ->
        ArrayList(x.subList(y.toInt(), x.size))
    })
    list.set("sliceTo", KotlinFunction2<ArrayList<*>, ArrayList<Any?>, Double> { x, y ->
        ArrayList(x.subList(0, y.toInt()))
    })
    list.set("last", KotlinFunction1<Any?, ArrayList<Any?>> { z -> z.last() })
    list.set("first", KotlinFunction1<Any?, ArrayList<Any?>> { z -> z.first() })
    list.set("indexOf", KotlinFunction2<Double, ArrayList<Any?>, Any?> { z, y ->
        z.indexOf(y).toDouble()
    })
    list.set("contains", KotlinFunction2<Boolean, ArrayList<Any?>, Any?> { z, y ->
        z.contains(y)
    })
    list.set("clear", KotlinFunction1<Unit, ArrayList<Any?>> { z -> z.clear() })
    list.set("isEmpty", KotlinFunction1<Boolean, ArrayList<Any?>> { z -> z.isEmpty() })
    list.set("filter", KotlinFunction2<Collection<Any?>, Collection<Any?>, OasisCallable> { interpreter, x, y ->
        x.filter { z ->
            y.call(interpreter, listOf(z)) as? Boolean
                ?: throw RuntimeException("'filter' function must return boolean")
        }
    })
    list.set("find", KotlinFunction2<Any?, Collection<Any?>, OasisCallable> { interpreter, x, y ->
        x.find { z ->
            y.call(interpreter, listOf(z)) as? Boolean ?: throw RuntimeException("'find' function must return boolean")
        }
    })
    list.set("findIndex", KotlinFunction2<Any?, Collection<Any?>, OasisCallable> { interpreter, x, y ->
        x.indexOfFirst { z ->
            y.call(interpreter, listOf(z)) as? Boolean
                ?: throw RuntimeException("'findIndex' function must return boolean")
        }
    })
    list.set("findLast", KotlinFunction2<Any?, Collection<Any?>, OasisCallable> { interpreter, x, y ->
        x.findLast { z ->
            y.call(interpreter, listOf(z)) as? Boolean
                ?: throw RuntimeException("'findLast' function must return boolean")
        }
    })
    list.set("findLastIndex", KotlinFunction2<Any?, Collection<Any?>, OasisCallable> { interpreter, x, y ->
        x.indexOfLast { z ->
            y.call(interpreter, listOf(z)) as? Boolean
                ?: throw RuntimeException("'findLastIndex' function must return boolean")
        }
    })
    list.set("map", KotlinFunction2<Collection<Any?>, Collection<Any?>, OasisCallable> { interpreter, x, y ->
        x.map { z ->
            y.call(interpreter, listOf(z))
        }
    })
    list.set("reduce", KotlinFunction3<Any?, Collection<Any?>, OasisCallable, OasisCallable> { interpreter, x, y, z ->
        x.reduce { a, b ->
            y.call(interpreter, listOf(a, b))
        }
    })
    list.set("every", KotlinFunction2<Boolean, Collection<Any?>, OasisCallable> { interpreter, x, y ->
        x.all { z ->
            y.call(interpreter, listOf(z)) as? Boolean ?: throw RuntimeException("'every' function must return boolean")
        }
    })
    list.set("some", KotlinFunction2<Boolean, Collection<Any?>, OasisCallable> { interpreter, x, y ->
        x.any { z ->
            y.call(interpreter, listOf(z)) as? Boolean ?: throw RuntimeException("'some' function must return boolean")
        }
    })
    it.define("list", list)
}
