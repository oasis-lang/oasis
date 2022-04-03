package standardLibrary
import KotlinFunction1
import OasisPrototype
import com.google.gson.Gson
import com.google.gson.GsonBuilder

var json = Module("json") { it ->
    val json = OasisPrototype(base, -1).apply {
        set("parse", KotlinFunction1<OasisPrototype, String> {
            createHashMap(Gson().fromJson(it, HashMap::class.java) as HashMap<Any?, Any?>)
        })
        set("parseProto", KotlinFunction1<OasisPrototype, String> {
            GsonBuilder()
                .registerTypeAdapter(OasisPrototype::class.java, PrototypeDeserializer())
                .create()
                .fromJson(it, OasisPrototype::class.java)
        })
        set("dump", KotlinFunction1<String, Any?> {
            GsonBuilder()
                .registerTypeAdapter(OasisPrototype::class.java, PrototypeSerializer())
                .setPrettyPrinting()
                .create()
                .toJson(it)
        })
    }
    it.define("json", json)
}