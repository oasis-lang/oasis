package me.snwy.oasis

import java.util.*

class Environment(private val enclosing: Environment? = null) {
    @JvmField
    var values: MutableMap<Int, Any?> = HashMap()

    fun define(name: Int, value: Any?) {
        if (values.containsKey(name)) {
            throw RuntimeError(line, "Variable with this name already defined")
        }
        values[name] = value
    }

    fun define(name: String, value: Any?) {
        if (values.containsKey(name.hashCode())) {
            throw RuntimeError(line, "Variable with this name already defined")
        }
        values[name.hashCode()] = value
    }

    fun get(name: Int): Any? {
        if (name in values) {
            return values[name]
        }
        if (enclosing != null) {
            return enclosing.get(name)
        }
        throw RuntimeError(line, "Undefined variable '${Optimizer.nameMap[name]}'")
    }

    fun assign(name: Int, value: Any?) {
        if (name in Optimizer.immutables) {
            throw RuntimeError(line, "Cannot assign to immutable variable")
        }
        if (values.containsKey(name)) {
            values[name] = value
            return
        }
        if (enclosing != null) {
            enclosing.assign(name, value)
            return
        }
        throw RuntimeError(line, "Undefined variable '${Optimizer.nameMap[name]}'")
    }

    private fun assign(name: String, value: Any?) {
        if (name.hashCode() in Optimizer.immutables) {
            throw RuntimeError(line, "Cannot assign to immutable variable")
        }
        if (values.containsKey(name.hashCode())) {
            values[name.hashCode()] = value
            return
        }
        if (enclosing != null) {
            enclosing.assign(name, value)
            return
        }
        throw RuntimeError(line, "Undefined variable '${name}'")
    }

    override fun toString(): String {
        return values.toString()
    }
}