package me.snwy.oasis.standardLibrary
import me.snwy.oasis.KotlinFunction1
import me.snwy.oasis.OasisPrototype
import com.google.gson.Gson
import com.google.gson.GsonBuilder

var json = Module("json") { it, interpreter ->
    val json = OasisPrototype(base, -1, interpreter).apply {
        set("parse", KotlinFunction1<OasisPrototype, String> { interpreter, it ->
            createHashMap(Gson().fromJson(it, HashMap::class.java) as HashMap<Any?, Any?>, interpreter)
        })
        set("parseProto", KotlinFunction1<OasisPrototype, String> { interpreter, it ->
            GsonBuilder()
                .registerTypeAdapter(OasisPrototype::class.java, PrototypeDeserializer(interpreter))
                .create()
                .fromJson(it, OasisPrototype::class.java)
        })
        set("dump", KotlinFunction1<String, Any?> { it ->
            GsonBuilder()
                .registerTypeAdapter(OasisPrototype::class.java, PrototypeSerializer())
                .setPrettyPrinting()
                .create()
                .toJson(it)
        })
    }
    it.define("json", json)
}