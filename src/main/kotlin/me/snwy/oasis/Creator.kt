package me.snwy.oasis

object Creator {
    fun create(code: String): OasisEnvironment {
        val env = OasisEnvironment(Interpreter(), null)
        env.eval = {
            val scanner = Scanner(it)
            env.scanner = scanner
            val parser = Parser(scanner.scanTokens())
            env.parser = parser
            val program = Optimizer().optimize(parser.parse())
            env.ast = program
        }
        env.get = {
            env.interpreter.environment.get(it.hashCode())
        }
        env.define = { x, y ->
            env.interpreter.environment.define(x.hashCode(), y)
        }
        env.eval(code)
        return env
    }
}

class OasisEnvironment(var interpreter: Interpreter, var ast: Stmt?) {
    lateinit var eval: (String) -> Unit
    lateinit var define: (String, Any) -> Unit
    lateinit var get: (String) -> Any?

    lateinit var parser: Parser
    lateinit var scanner: Scanner

    fun run() {
        interpreter.execute(ast!!)
    }
}