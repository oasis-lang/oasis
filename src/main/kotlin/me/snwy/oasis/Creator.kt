package me.snwy.oasis

object Creator {
    fun create(code: String): OasisEnvironment {
        val env = OasisEnvironment(Interpreter(), null)
        env.eval = {
            val scanner = Scanner(it)
            val parser = Parser(scanner.scanTokens())
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

    fun run() {
        interpreter.execute(ast!!)
    }
}