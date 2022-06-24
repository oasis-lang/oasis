package me.snwy.oasis

import me.snwy.oasis.standardLibrary.base

class OasisPrototype(var inherit: OasisPrototype? = base, val line: Int, var interpreter: Interpreter? = null) :
    Cloneable {
    var body: HashMap<String, Any?> = HashMap()

    fun get(name: String): Any? {
        if (body.containsKey(name)) {
            return body[name]
        } else if (inherit != null && inherit!!.body.containsKey(name)) {
            return inherit!!.body[name]
        }
        return null
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