class Environment(private val enclosing: Environment? = null) {
    var values: HashMap<String, Any?> = HashMap()

    fun define(name: String, value: Any?) {
        values[name] = value
    }

    fun get(name: Token): Any? {
        if(values.containsKey(name.lexeme)) {
            return values[name.lexeme]
        }
        if (enclosing != null) {
            return enclosing.get(name)
        }
        throw RuntimeError(name.line,"Undefined variable '${name.lexeme}'")
    }

    fun assign(name: Token, value: Any?) {
        if(values.containsKey(name.lexeme)) {
            values[name.lexeme] = value
            return
        }
        if(enclosing != null) {
            enclosing.assign(name, value)
            return
        }
        throw RuntimeError(name.line, "Undefined variable '${name.lexeme}'")
    }

    override fun toString(): String {
        return values.toString()
    }
}