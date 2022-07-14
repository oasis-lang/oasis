package me.snwy.oasis

import me.snwy.oasis.standardLibrary.base
import java.util.*

class OasisPrototype(@JvmField var inherit: OasisPrototype? = base, @JvmField val line: Int, @JvmField var interpreter: Interpreter? = null) :
    Cloneable {
    @JvmField
    var body: MutableMap<String, Any?> = TreeMap()

    fun get(name: String): Any? {
        return body[name] ?: (inherit?.get(name))
    }

    fun set(name: String, value: Any?) {
        body[name] = value
        if (value is OasisFunction)
            value.closure.define("this".hashCode(), this)
    }

    override fun toString(): String {
        return (interpreter?.let {
            (get("toString") as OasisCallable).call(it, listOf())
        } ?: return "OasisPrototype <${hashCode()}>") as String
    }

    override fun hashCode(): Int {
        return interpreter?.let {
            ((get("hashCode") as OasisCallable).call(it, listOf()) as Double).toInt()
        } ?: return super.hashCode()
    }

    override fun clone(): Any {
        return OasisPrototype(inherit, line, interpreter).let { body.map { x -> it.set(x.key, x.value) }; it }
    }

    override fun equals(other: Any?): Boolean {
        if (get("__equals") != null) {
            return (get("__equals") as OasisCallable).call(interpreter!!, listOf(other)) as Boolean
        }
        return super.equals(other)
    }
}