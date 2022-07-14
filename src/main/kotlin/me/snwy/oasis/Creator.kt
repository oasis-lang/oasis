package me.snwy.oasis

class OasisEnvironment(var interpreter: Interpreter = Interpreter(), var ast: StmtList? = null) {
    var parser: Parser? = null
    var scanner: Scanner? = null

    fun eval(code: String) {
        val scanner = Scanner(code)
        this.scanner = scanner
        val tokens = scanner.scanTokens()
        val parser = Parser(tokens)
        this.parser = parser
        val ast = parser.parse()
        val program = Optimizer().optimize(ast)
        this.ast = program as StmtList
    }
    fun doExpr(code: String): Any? {
        val scanner = Scanner(code)
        this.scanner = scanner
        val tokens = scanner.scanTokens()
        val parser = Parser(tokens)
        this.parser = parser
        val ast = parser.expression()
        val program = Optimizer().optimize(ast)
        return interpreter.eval(program)
    }
    fun define(name: String, value: Any) {
        interpreter.environment.define(name.hashCode(), value)
    }
    fun get(name: String): Any? {
        return interpreter.environment.get(name.hashCode())
    }
    fun run() {
        ast!!.stmts.forEach {
            interpreter.execute(it)
        }
    }
}