package me.snwy.oasis.standardLibrary

import me.snwy.oasis.KotlinFunction1
import me.snwy.oasis.KotlinFunction2
import me.snwy.oasis.OasisPrototype

var prototype = Module("prototype") { it, interpreter ->
    val prototype = OasisPrototype(base, -1, interpreter).apply {
        set("setPrototypeOf", KotlinFunction2<Unit, OasisPrototype, OasisPrototype> { x, y ->
            x.inherit = y
        })
        set("getPrototypeOf", KotlinFunction1<OasisPrototype, OasisPrototype> { it ->
            it.inherit!!
        })
        set("toMap", KotlinFunction1<OasisPrototype, OasisPrototype> { interpreter, it ->
            createHashMap(it.body as HashMap<Any?, Any?>, interpreter)
        })
        set("fromMap", KotlinFunction1<OasisPrototype, OasisPrototype> { interpreter, it ->
            OasisPrototype(base, line, interpreter).apply {
                body = it.get("__map") as HashMap<String, Any?>
            }
        })
    }
    it.define("prototype", prototype)
}