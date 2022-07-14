package me.snwy.oasis

class OasisFunction(@JvmField val declaration: Func, @JvmField val closure: Environment) : OasisCallable {
    override fun call(interpreter: Interpreter, arguments: List<Any?>): Any? {
        val environment = Environment(closure)
        if (arguments.size < arity()) throw RuntimeError(line, "Expected ${arity()} arguments, got ${arguments.size}")
        (0 until declaration.operands.size).map {
            environment.define(
                declaration.operands[it].lexeme.hashCode(),
                arguments[it]
            )
        }
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
        return "me.snwy.OasisFunction(${declaration.operands.map { x -> x.lexeme }})"
    }
}