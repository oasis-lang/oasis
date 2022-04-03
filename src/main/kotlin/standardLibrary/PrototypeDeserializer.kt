package standardLibrary

import OasisPrototype
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import line
import java.lang.reflect.Type

class PrototypeDeserializer: JsonDeserializer<OasisPrototype> {
    override fun deserialize(json: JsonElement?, typeOfT: Type?, context: JsonDeserializationContext?): OasisPrototype {
        val result = OasisPrototype(base, line)
        json?.asJsonObject?.entrySet()?.map {
            result.set(it.key, it.value)
        }
        return result
    }

}