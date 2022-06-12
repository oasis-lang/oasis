package me.snwy.oasis

interface OasisCallable {
    fun call(interpreter: Interpreter, arguments: List<Any?>): Any?

    fun arity(): Int
}