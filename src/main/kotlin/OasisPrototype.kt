class OasisPrototype(var inherit: OasisPrototype?, val line: Int): Cloneable{
    var body: HashMap<String, Any?> = HashMap()

    fun get(name: String): Any? {
        if (body.containsKey(name)) {
            val value = body[name]
            return value
        } else if(inherit != null && inherit!!.body.containsKey(name)) {
            return inherit!!.body[name]
        }
        throw RuntimeError(line, "Prototype does not contain key '$name'")
    }

    fun set(name: String, value: Any?) {
        body[name] = value
        if(value is OasisFunction)
            value.closure.define("this", this)
    }

    override fun toString(): String {
        return globalInterpreter?.let { (get("toString") as OasisCallable).call(it, ArrayList()) } as String
    }

    override fun clone(): Any {
        return OasisPrototype(inherit, line).let { body.map { x -> it.set(x.key, x.value) }; it }
    }
}