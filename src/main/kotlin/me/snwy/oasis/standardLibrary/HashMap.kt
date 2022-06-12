package me.snwy.oasis.standardLibrary
import me.snwy.oasis.*

val hashmap = Module("map") { it, _ ->
    val mapFactory = KotlinFunction0 { interpreter ->
        createHashMap(HashMap(), interpreter)
    }
    it.define("map", mapFactory)
}

fun createHashMap(hashMap: HashMap<Any?, Any?>, interpreter: Interpreter): OasisPrototype {
    val map = OasisPrototype(base, line, interpreter)
    map.set("__map", hashMap)
    map.apply {
        set("set", KotlinFunction2<OasisPrototype, String, Any?> { x, y ->
            (get("__map") as HashMap<Any?, Any?>)[x] = y
            return@KotlinFunction2 this
        })
        set("del", KotlinFunction1<OasisPrototype, String> { x ->
            (get("__map") as HashMap<Any?, Any?>).remove(x)
            return@KotlinFunction1 this
        })
        set("__index", KotlinFunction1<Any?, String> { x ->
            (get("__map") as HashMap<Any?, Any?>)[x]
        })
        set("__setIndex", KotlinFunction2<Any?, String, Any?> { x, y ->
            (get("__map") as HashMap<Any?, Any?>)[x] = y
            return@KotlinFunction2 this
        })
        set("toString", KotlinFunction0 { _ ->
            (get("__map").toString())
        })
        set("__serialize", KotlinFunction0 { _ ->
            get("__map") as HashMap<String, Any?>
        })
        set("__iterator", KotlinFunction1<Any?, Double> { index ->
            val map = get("__map") as HashMap<Any?, Any?>
            map.iterator().withIndex().forEach {
                if (it.index == index.toInt()) {
                    return@KotlinFunction1 it.value
                }
            }
        })
    }
    return map
}
