class OasisFunction(private val declaration: Func, val closure: Environment) : OasisCallable {
    override fun call(interpreter: Interpreter, arguments: List<Any?>): Any? {
        val environment= Environment(closure)
        (0 until declaration.operands.size).map { environment.define(declaration.operands[it].lexeme, arguments[it]) }
        try {
            interpreter.executeBlock(declaration.body, environment)
        } catch (returnValue: Return) {
            return returnValue.value
        }
        return null
    }

    override fun arity(): Int {
        return declaration.operands.size
    }

    override fun toString(): String {
        return "OasisFunction(${declaration.operands.map { x -> x.lexeme }})"
    }
}