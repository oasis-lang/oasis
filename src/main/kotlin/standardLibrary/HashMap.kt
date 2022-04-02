package standardLibrary

import KotlinFunction0
import KotlinFunction1
import KotlinFunction2
import OasisPrototype

val hashmap = Module("map") { it ->
    val mapFactory = KotlinFunction0 {
        val map = OasisPrototype(base, -1)
        map.set("__map", HashMap<String, Any?>())
        map.apply {
            set("set", KotlinFunction2<OasisPrototype, String, Any?> { x, y ->
                (get("__map") as HashMap<String, Any?>).set(x, y)
                return@KotlinFunction2 this
            })
            set("del", KotlinFunction1<OasisPrototype, String> { x ->
                (get("__map") as HashMap<String, Any?>).remove(x)
                return@KotlinFunction1 this
            })
            set("__index", KotlinFunction1<Any?, String> { x ->
                (get("__map") as HashMap<String, Any?>)[x]
            })
        }
        return@KotlinFunction0 map
    }
    it.define("map", mapFactory)
}
