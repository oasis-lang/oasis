package me.snwy.oasis.standardLibrary

import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import me.snwy.oasis.OasisCallable
import me.snwy.oasis.OasisPrototype
import me.snwy.oasis.RuntimeError
import me.snwy.oasis.line
import java.lang.reflect.Type

class PrototypeSerializer : JsonSerializer<OasisPrototype> {
    override fun serialize(src: OasisPrototype?, typeOfSrc: Type?, context: JsonSerializationContext?): JsonElement {
        if (src != null) {
            return if (src.body.containsKey("__serialize")) {
                Gson().toJsonTree(src.interpreter?.let {
                    (src.get("__serialize") as OasisCallable).call(
                        it,
                        listOf()
                    )
                })
            } else {
                Gson().toJsonTree(src.body)
            }
        } else {
            throw RuntimeError(line, "Can't serialize null.")
        }
    }

}