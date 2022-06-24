package me.snwy.oasis

class PartialFunc(val func: OasisCallable, private val partialArgs: ArrayList<Any?>) : OasisCallable {
    override fun call(interpreter: Interpreter, arguments: List<Any?>): Any? {
        val list: ArrayList<Any?> = ArrayList()
        list.addAll(partialArgs)
        list.addAll(arguments)
        if (list.size < arity()) throw RuntimeError(line, "Expected ${arity()} arguments, got ${list.size}")
        return func.call(interpreter, list)
    }

    override fun arity(): Int {
        return func.arity()
    }
}