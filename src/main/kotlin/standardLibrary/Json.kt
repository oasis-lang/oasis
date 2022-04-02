package standardLibrary
import KotlinFunction1
import OasisCallable
import OasisPrototype
import com.google.gson.Gson
import globalInterpreter

var json = Module("json") { it ->
    val json = OasisPrototype(base, -1).apply {
        set("parse", KotlinFunction1<OasisPrototype, String> {
            createHashMap(Gson().fromJson(it, HashMap::class.java) as HashMap<Any?, Any?>)
        })
        set("parseObj", KotlinFunction1<OasisPrototype, String> {
            val result = OasisPrototype(base, line)
            Gson().fromJson(it, HashMap::class.java).map {
                result.set(it.key as String, it.value)
            }
            return@KotlinFunction1 result
        })
        set("dump", KotlinFunction1<String, Any?> {
            return@KotlinFunction1 when(it) {
                is OasisPrototype -> if(it.body.containsKey("__serialize"))
                    Gson().toJson(globalInterpreter?.let { it1 ->
                        (it.get("__serialize") as OasisCallable)
                            .call(it1, listOf())
                    }) else
                        Gson().toJson(it.body)
                else -> Gson().toJson(it)
            }
        })
    }
    it.define("json", json)
}