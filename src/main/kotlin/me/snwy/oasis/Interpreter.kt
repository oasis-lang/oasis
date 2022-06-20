package me.snwy.oasis

import me.snwy.oasis.standardLibrary.StandardLibrary
import me.snwy.oasis.standardLibrary.base
import me.snwy.oasis.standardLibrary.createHashMap

var line: Int = 0

class Interpreter : Expr.Visitor<Any?>, Stmt.Visitor<Any?> {

    private var globals = Environment()
    var environment: Environment = globals

    private var retInstance = Return(null)

    init {
        StandardLibrary.generateLib(environment, this)
    }

    inline fun eval(expr: Expr): Any? {
        return expr.accept(this)
    }
    inline fun execute(stmt: Stmt) {
        line = stmt.line
        stmt.accept(this)
    }

    private fun execute(stmtList: StmtList) {
        executeBlock(stmtList, environment)
    }

    fun executeBlock(stmtList: StmtList, env: Environment) {
        val previous: Environment = environment
        try {
            environment = env
            stmtList.accept(this)
        } finally {
            environment = previous
        }
    }

    override fun visitLiteral(literal: Literal): Any? {
        return literal.value
    }

    override fun visitAssignment(assignment: AssignmentExpr): Any? {
        val a = eval(assignment.value)
        when(assignment.left) {
            is Property -> (eval((assignment.left as Property).obj) as OasisPrototype).set((assignment.left as Property).indexer.lexeme, a)
            is Variable -> environment.assign(assignment.left.hashCode(), a)
            is Precomputed -> environment.assign((assignment.left as Precomputed).hash, a)
            is Indexer -> {
                when(val indexer = eval((assignment.left as Indexer).expr)) {
                    is ArrayList<*> -> (indexer as ArrayList<Any?>)[
                            (eval((assignment.left as Indexer).index) as Double).toInt()
                    ] = a
                    is OasisPrototype -> {
                        (indexer.get("__setIndex") as OasisCallable).call(this,
                            listOf(eval((assignment.left as Indexer).index), eval(assignment.value)))
                    }
                    else -> throw RuntimeError(assignment.line, "Cannot index")
                }
            }

            else -> {
                throw RuntimeError(assignment.line, "Cannot assign")
            }
        }
        return a
    }


    override fun visitProperty(property: Property): Any? {
        val propertyVal = eval(property.obj)
        return when(propertyVal) {
            is String -> {
                PartialFunc((environment.get("string".hashCode()) as OasisPrototype)
                                    .get(property.indexer.lexeme) as OasisCallable,
                    arrayListOf(propertyVal) as ArrayList<Any?>
                )
            }
            is ArrayList<*> -> {
                PartialFunc((environment.get("list".hashCode()) as OasisPrototype)
                                    .get(property.indexer.lexeme) as OasisCallable,
                    arrayListOf(propertyVal) as ArrayList<Any?>
                )
            }
            is Func -> {
                PartialFunc((environment.get("func".hashCode()) as OasisPrototype)
                    .get(property.indexer.lexeme) as OasisCallable,
                    arrayListOf(propertyVal) as ArrayList<Any?>
                )
            }
            is Double -> {
                PartialFunc((environment.get("math".hashCode()) as OasisPrototype)
                    .get(property.indexer.lexeme) as OasisCallable,
                    arrayListOf(propertyVal) as ArrayList<Any?>
                )
            }
            is OasisPrototype -> propertyVal.get(property.indexer.lexeme)
            else -> throw RuntimeError(line, "${propertyVal} is not a fielded object")
        }
    }

    override fun visitFunc(func: Func): Any {
        return OasisFunction(func, environment)
    }

    override fun visitFcall(fcall: FCallExpr): Any? {
        val callee = (eval(fcall.func) ?: throw RuntimeError(fcall.line, "cannot call null function")) as OasisCallable
        val arguments: ArrayList<Any?> = (if(fcall.splat) eval(fcall.operands[0]) else fcall.operands.map { eval(it) }) as ArrayList<Any?>
        return callee.call(this, arguments)
    }

    private fun isTruthy(thing: Any?): Boolean {
        if(thing == null) return false
        if(thing == 0.0) return false
        if(thing == false) return false
        return true
    }

    override fun visitBinOp(binop: BinOp): Any? {
        val left = eval(binop.left)
        val right = eval(binop.right)
        return when(binop.operator.type) {
            TokenType.PLUS -> {
                when(left) {
                    is OasisPrototype -> (left.get("__plus") as OasisCallable).call(this, listOf(right))
                    is Double -> left + right as Double
                    is Int -> left + right as Int
                    is String -> left + right.toString()
                    else -> throw RuntimeError(binop.line, "Cannot add")
                }
            }
            TokenType.MINUS -> {
                when(left) {
                    is OasisPrototype -> (left.get("__sub") as OasisCallable).call(this, listOf(right))
                    is Double -> left - right as Double
                    is Int -> left - right as Int
                    else -> throw RuntimeError(binop.line, "Cannot subtract")
                }
            }
            TokenType.STAR -> {
                when(left) {
                    is OasisPrototype -> (left.get("__mul") as OasisCallable).call(this, listOf(right))
                    is Double -> left * right as Double
                    is Int -> left * right as Int
                    else -> throw RuntimeError(binop.line, "Cannot multiply")
                }
            }
            TokenType.SLASH -> {
                when(left) {
                    is OasisPrototype -> (left.get("__div") as OasisCallable).call(this, listOf(right))
                    is Double -> left / right as Double
                    is Int -> left / right as Int
                    else -> {println("$left / $right"); throw RuntimeError(binop.line, "Cannot divide")
                    }
                }
            }
            TokenType.EQUAL_EQUAL -> {
                if (left != null) {
                    left == right
                } else {
                    if (right == null) {
                        return true
                    }
                    false
                }
            }
            TokenType.BANG_EQUAL -> {
                if (left != null) {
                    left != right
                } else {
                    if (right != null) {
                        return true
                    }
                    false
                }
            }
            TokenType.GREATER -> {
                when(left) {
                    is Double -> left > right as Double
                    is Int -> left > right as Int
                    else -> throw RuntimeError(binop.line, "Cannot greater")
                }
            }
            TokenType.GREATER_EQUAL -> {
                when(left) {
                    is Double -> left >= right as Double
                    is Int -> left >= right as Int
                    else -> throw RuntimeError(binop.line, "Cannot greater equal")
                }
            }
            TokenType.LESS -> {
                when(left) {
                    is Double -> left < right as Double
                    is Int -> left < right as Int
                    else -> throw RuntimeError(binop.line, "Cannot less")
                }
            }
            TokenType.LESS_EQUAL -> {
                when(left) {
                    is Double -> left <= right as Double
                    is Int -> left <= right as Int
                    else -> throw RuntimeError(binop.line, "Cannot less equal")
                }
            }
            TokenType.MOD -> {
                when(left) {
                    is Double -> left % right as Double
                    is Int -> left % right as Int
                    else -> throw RuntimeError(binop.line, "Cannot mod")
                }
            }
            TokenType.AND -> {
                isTruthy(left) && isTruthy(right)
            }
            TokenType.OR -> {
                isTruthy(left) || isTruthy(right)
            }
            TokenType.LEFT_PIPE -> {
                return left
            }
            TokenType.RIGHT_PIPE -> {
                return right
            }
            TokenType.QUESTION -> {
                return left ?: right
            }
            else -> throw RuntimeError(binop.line, "Invalid operator")
        }
    }

    override fun visitGroup(group: Group): Any? {
        return group.expr.accept(this)
    }

    override fun visitVariable(variable: Variable): Any? {
        return environment.get(variable.name.lexeme.hashCode())
    }

    override fun visitProto(proto: Proto): Any {
        val protoType = OasisPrototype((if (proto.base != null) eval(proto.base!!) else base) as OasisPrototype?, proto.line, this)
        proto.body.stmts.map {
            if(it is Let)
                protoType.set(it.left.lexeme, eval(it.value))
            else
                protoType.set(((
                        (it as ExprStmt)
                            .expr as AssignmentExpr)
                            .left as Variable)
                            .name
                            .lexeme,
                        eval(
                            (it.expr as AssignmentExpr)
                                .value))
        }
        return protoType
    }

    override fun visitLet(let: Let) {
        environment.define((let.left.lexeme).hashCode(), eval(let.value))
    }

    override fun visitIfStmt(ifstmt: IfStmt): Boolean {
        return if(isTruthy(eval(ifstmt.expr))) {
            execute(ifstmt.stmtlist)
            true
        } else {
            ifstmt.elseBody?.let { execute(it) }
            false
        }
    }

    override fun visitWhileStmt(whilestmt: WhileStmt) {
        while(isTruthy(eval(whilestmt.expr))) {
            try {
                execute(whilestmt.body)
            } catch (internalException: InternalException) {
                when(internalException.type) {
                    ExceptionType.BREAK -> break
                    ExceptionType.CONTINUE -> continue
                    ExceptionType.ITERATOR_EMPTY -> throw internalException
                }
            }
        }
    }

    override fun visitStmtList(stmtlist: StmtList) {
        stmtlist.stmts.map {
            execute(it)
        }
    }

    override fun visitReturnStmt(retstmt: RetStmt) {
        retInstance.value = if (retstmt.expr != null) eval(retstmt.expr!!) else null
        throw retInstance
    }

    override fun visitExprStmt(exprStmt: ExprStmt) {
        if(repl) {
            repl = false
            eval(exprStmt.expr).let { if(it !is Unit) println(it) }
        } else {
            exprStmt.expr.accept(this)
        }
    }

    override fun visitIndexer(indexer: Indexer): Any? {
        return when(val x = eval(indexer.expr)) {
            is String -> x[(eval(indexer.index) as Double).toInt()]
            is ArrayList<*> -> x[(eval(indexer.index) as Double).toInt()]
            is OasisPrototype -> (x.get("__index") as OasisCallable).call(this, listOf(eval(indexer.index)))
            else -> throw RuntimeError(indexer.line, "Cannot index")
        }
    }

    override fun visitList(list: OasisList): Any {
        val ev = ArrayList<Any?>()
        list.exprs.map { ev.add(eval(it)) }
        return ev
    }

    override fun visitNegate(negate: Negate): Any {
        return -(eval(negate.value) as Double)
    }

    override fun visitNew(ref: New): Any? {
        return eval(ref.expr)?.let { if(it is Cloneable) { it.javaClass.getMethod("clone").invoke(it) } else throw RuntimeError(ref.line, "Cannot clone object") }
    }

    override fun visitNot(not: Not): Any {
        return !isTruthy(eval(not.expr))
    }

    override fun visitIs(is_: Is) {
        is_.cases.stmts.map {
            if ((it as IfStmt).accept(this) as Boolean)
                return
        }
        is_.else_?.let {
            execute(it)
        }
    }

    override fun visitTest(test: Test) {
        try {
            test.block.accept(this)
        } catch (OasisException: OasisException) {
            environment.define(test.errorVar.lexeme.hashCode(), OasisException.value)
            test.errorBlock.accept(this)
        } catch (RuntimeError: RuntimeError) {
            environment.define(test.errorVar.lexeme.hashCode(), RuntimeError.s)
            test.errorBlock.accept(this)
        } catch (Exception: Exception) {
            throw Exception
        } finally {
            environment.values.remove(test.errorVar.lexeme.hashCode())
        }
    }

    override fun vistPrecomputed(precomputed: Precomputed): Any? {
        return environment.get(precomputed.hash)
    }

    override fun visitForLoopTriad(forLoopTriad: ForLoopTriad) {
        forLoopTriad.init.accept(this)
        while(isTruthy(eval(forLoopTriad.cond))) {
            try {
                execute(forLoopTriad.body)
                forLoopTriad.step.accept(this)
            } catch (internalException: InternalException) {
                when(internalException.type) {
                    ExceptionType.BREAK -> break
                    ExceptionType.CONTINUE -> continue
                    ExceptionType.ITERATOR_EMPTY -> throw internalException // keep it going, doesn't matter here
                }
            }
        }
    }

    override fun visitForLoopIterator(forLoopIterator: ForLoopIterator){
        when(val iteratorExpr = eval(forLoopIterator.iterable)) {
            is OasisPrototype -> {
                val iterator = iteratorExpr.get("__iterator") as OasisCallable
                var index = 0
                while (true) {
                    try { // Kill this with fire
                        environment.values[(forLoopIterator.varName as Variable).name.lexeme.hashCode()] = iterator.call(this, listOf(index))
                    } catch (internalException: InternalException) {
                        when(internalException.type) {
                            ExceptionType.BREAK -> break
                            ExceptionType.CONTINUE -> continue
                            ExceptionType.ITERATOR_EMPTY -> break
                        }
                    }
                    index++
                }
            }
            is Iterable<*> -> {
                for (element in iteratorExpr) {
                    environment.values[(forLoopIterator.varName as Variable).name.lexeme.hashCode()] = element
                    execute(forLoopIterator.body)
                }
            }
            else -> throw RuntimeError(forLoopIterator.line, "Cannot iterate")
        }
    }

    override fun visitBreakStmt(break_: BreakStmt) {
        throw InternalException(ExceptionType.BREAK)
    }

    override fun visitContinueStmt(continue_: ContinueStmt) {
        throw InternalException(ExceptionType.CONTINUE)
    }

    override fun visitListComprehension(listComprehension: ListComprehension): Any {
        when(val iteratorExpr = eval(listComprehension.inVal)) {
            is OasisPrototype -> {
                val iterator = iteratorExpr.get("__iterator") as OasisCallable
                var index = 0
                val list = ArrayList<Any?>()
                while (true) {
                    try { // Kill this with fire
                        list.add((eval(listComprehension.expr) as OasisCallable).call(this, listOf(iterator.call(this, listOf(index)))))
                    } catch (internalException: InternalException) {
                        when(internalException.type) {
                            ExceptionType.BREAK -> break
                            ExceptionType.CONTINUE -> continue
                            ExceptionType.ITERATOR_EMPTY -> break
                        }
                    }
                    index++
                }
                return list
            }
            is Iterable<*> -> {
                val list = ArrayList<Any?>()
                for (element in iteratorExpr) {
                    list.add((eval(listComprehension.expr) as OasisCallable).call(this, listOf(element)))
                }
                return list
            }
            else -> throw RuntimeError(listComprehension.line, "Cannot iterate")
        }
    }

    override fun visitMapLiteral(mapLiteral: MapLiteral): Any? {
        val map = HashMap<Any?, Any?>()
        mapLiteral.exprs.map {
            map[eval(it.first)] = eval(it.second)
        }
        return createHashMap(map, this)
    }

    override fun visitIfExpression(ifExpression: IfExpression): Any? {
        if (isTruthy(eval(ifExpression.expr))) {
            return eval(ifExpression.thenExpr)
        } else {
            return eval(ifExpression.elseExpr)
        }
    }
}