package me.snwy.oasis

import me.snwy.oasis.standardLibrary.base

class OasisPrototype(var inherit: OasisPrototype? = base, val line: Int, var interpreter: Interpreter? = null): Cloneable{
    var body: HashMap<String, Any?> = HashMap()

    fun get(name: String): Any? {
        if (body.containsKey(name)) {
            return body[name]
        } else if(inherit != null && inherit!!.body.containsKey(name)) {
            return inherit!!.body[name]
        }
        throw RuntimeError(line, "Prototype does not contain key '$name'")
    }

    fun set(name: String, value: Any?) {
        body[name] = value
        if(value is OasisFunction)
            value.closure.define("this".hashCode(), this)
    }

    override fun toString(): String {
        return (interpreter?.let {
            (get("toString") as OasisCallable).call(it, listOf())
        } ?: return "OasisPrototype <${hashCode()}>") as String
    }

    override fun clone(): Any {
        return OasisPrototype(inherit, line, interpreter).let { body.map { x -> it.set(x.key, x.value) }; it }
    }
}