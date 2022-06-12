package me.snwy.oasis.standardLibrary

import me.snwy.oasis.OasisPrototype
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import me.snwy.oasis.Interpreter
import me.snwy.oasis.line
import java.lang.reflect.Type

class PrototypeDeserializer(var interpreter: Interpreter): JsonDeserializer<OasisPrototype> {
    override fun deserialize(json: JsonElement?, typeOfT: Type?, context: JsonDeserializationContext?): OasisPrototype {
        val result = OasisPrototype(base, line, interpreter)
        json?.asJsonObject?.entrySet()?.map {
            result.set(it.key, it.value)
        }
        return result
    }

}