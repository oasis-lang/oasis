package standardLibrary

import OasisCallable
import OasisPrototype
import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import globalInterpreter
import java.lang.reflect.Type

class PrototypeSerializer : JsonSerializer<OasisPrototype> {
    override fun serialize(src: OasisPrototype?, typeOfSrc: Type?, context: JsonSerializationContext?): JsonElement {
        if (src != null) {
            return if(src.body.containsKey("__serialize")) {
                Gson().toJsonTree(globalInterpreter?.let {
                    (src.get("__serialize") as OasisCallable).call(
                        it,
                        listOf())
                })
            } else {
                Gson().toJsonTree(src.body)
            }
        } else {
            throw Exception("Can't serialize null.")
        }
    }

}