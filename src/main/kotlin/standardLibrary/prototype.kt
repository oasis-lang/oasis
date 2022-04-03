package standardLibrary

import KotlinFunction1
import KotlinFunction2
import OasisPrototype

var prototype = Module("prototype") {
    val prototype = OasisPrototype(base, -1).apply {
        set("setPrototypeOf", KotlinFunction2<Unit, OasisPrototype, OasisPrototype> { x, y ->
            x.inherit = y
        })
        set("getPrototypeOf", KotlinFunction1<OasisPrototype, OasisPrototype> {
            it.inherit!!
        })
        set("toMap", KotlinFunction1<OasisPrototype, OasisPrototype> {
            createHashMap(it.body as HashMap<Any?, Any?>)
        })
        set("fromMap", KotlinFunction1<OasisPrototype, OasisPrototype> {
            OasisPrototype(base, line).apply {
                body = it.get("__map") as HashMap<String, Any?>
            }
        })
    }
    it.define("prototype", prototype)
}