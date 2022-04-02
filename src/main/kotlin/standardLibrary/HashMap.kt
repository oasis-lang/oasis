package standardLibrary
import KotlinFunction0
import KotlinFunction1
import KotlinFunction2
import OasisPrototype
import line

val hashmap = Module("map") {
    val mapFactory = KotlinFunction0 {
        createHashMap(HashMap())
    }
    it.define("map", mapFactory)
}

fun createHashMap(hashMap: HashMap<Any?, Any?>): OasisPrototype {
    val map = OasisPrototype(base, line)
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
        set("toString", KotlinFunction0 {
            (get("__map").toString())
        })
        set("__serialize", KotlinFunction0 {
            get("__map") as HashMap<String, Any?>
        })
    }
    return map
}
