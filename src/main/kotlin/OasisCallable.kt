

interface OasisCallable {
    fun call(interpreter: Interpreter, arguments: List<Any?>): Any?

    fun arity(): Int
}