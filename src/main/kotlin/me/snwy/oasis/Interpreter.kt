package me.snwy.oasis

import me.snwy.oasis.standardLibrary.StandardLibrary
import me.snwy.oasis.standardLibrary.base
import me.snwy.oasis.standardLibrary.createHashMap

var line: Int = 0

class Interpreter : Expr.Visitor<Any?>, Stmt.Visitor<Any?> {
    @JvmField
    var environment: Environment = Environment()
    @JvmField
    var retInstance = Return(null)

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

    private inline fun execute(stmtList: StmtList) {
        executeBlock(stmtList, Environment(environment))
    }

    inline fun executeBlock(stmtList: StmtList, env: Environment) {
        val previous: Environment = environment
        try {
            environment = env
            stmtList.stmts.forEach {
                execute(it)
            }
        } finally {
            environment = previous
        }
    }

    override fun visitLiteral(literal: Literal): Any? {
        return literal.value
    }

    override fun visitAssignment(assignment: AssignmentExpr): Any? {
        var a: Any? = null
        when (assignment.left) {
            is Property -> {
                a = eval(assignment.value); (eval((assignment.left as Property).obj) as OasisPrototype).set(
                    (assignment.left as Property).indexer.lexeme,
                    a
                )
            }
            is Variable -> {
                a = eval(assignment.value)
                environment.assign(assignment.left.hashCode(), a)
            }
            is Precomputed -> {
                val left = environment.get((assignment.left as Precomputed).hash)
                if (left is RelativeExpression) {
                    left.expr = assignment.value
                } else {
                    a = eval(assignment.value)
                    environment.assign((assignment.left as Precomputed).hash, a)
                }
            }
            is Indexer -> {
                a = eval(assignment.value)
                when (val indexer = eval((assignment.left as Indexer).expr)) {
                    is ArrayList<*> -> (indexer as ArrayList<Any?>) [
                            (eval((assignment.left as Indexer).index) as Number).toInt()
                    ] = a
                    is OasisPrototype -> {
                        (indexer.get("__setIndex") as OasisCallable).call(
                            this,
                            listOf(eval((assignment.left as Indexer).index), eval(assignment.value))
                        )
                    }
                    else -> throw RuntimeError(assignment.line, "Cannot index")
                }
            }
            is Tuple -> {
                if(!(assignment.left as Tuple).exprs.all { it is Variable || it is Precomputed }) {
                    throw RuntimeError(assignment.line, "Cannot assign to tuple")
                }
                val right = eval(assignment.value)
                if (right !is Iterable<*>) {
                    throw RuntimeError(assignment.line, "Right side of tuple assignment must be an iterable")
                }
                for((i, expr) in (assignment.left as Tuple).exprs.withIndex()) {
                    environment.assign((expr as? Precomputed)?.hash ?: (expr as Variable).name.lexeme.hashCode(), right.elementAt(i))
                }
            }
            else -> {
                throw RuntimeError(assignment.line, "Cannot assign")
            }
        }
        return a
    }


    override fun visitProperty(property: Property): Any? {
        return when (val propertyVal = eval(property.obj)) {
            is String -> {
                PartialFunc(
                    (environment.get("string".hashCode()) as OasisPrototype)
                        .get(property.indexer.lexeme) as OasisCallable,
                    arrayListOf(propertyVal)
                )
            }
            is ArrayList<*> -> {
                PartialFunc(
                    (environment.get("list".hashCode()) as OasisPrototype)
                        .get(property.indexer.lexeme) as OasisCallable,
                    arrayListOf(propertyVal)
                )
            }
            is Func -> {
                PartialFunc(
                    (environment.get("func".hashCode()) as OasisPrototype)
                        .get(property.indexer.lexeme) as OasisCallable,
                    arrayListOf<Any?>(propertyVal)
                )
            }
            is Number -> {
                PartialFunc(
                    (environment.get("math".hashCode()) as OasisPrototype)
                        .get(property.indexer.lexeme) as OasisCallable,
                    arrayListOf<Any?>(propertyVal)
                )
            }
            is OasisPrototype -> propertyVal.get(property.indexer.lexeme)
            else -> throw RuntimeError(line, "$propertyVal is not a fielded object")
        }
    }

    override fun visitFunc(func: Func): Any {
        return OasisFunction(func, environment)
    }

    override fun visitFcall(fcall: FCallExpr): Any? {
        val callee = (eval(fcall.func) ?: throw RuntimeError(fcall.line, "cannot call null function")) as OasisCallable
        val arguments = ArrayList<Any?>()
        if (fcall.splat) {
            val splat = eval(fcall.operands[0]) as Iterable<*>
            arguments.addAll(splat)
        } else {
            arguments.addAll(fcall.operands.map { eval(it) })
        }
        return callee.call(this, arguments)
    }

    private fun isTruthy(thing: Any?): Boolean {
        if (thing == null) return false
        if (thing == 0.0 || thing == 0) return false
        if (thing == false) return false
        return true
    }

    override fun visitBinOp(binop: BinOp): Any? {
        val left = eval(binop.left)
        val right = eval(binop.right)
        return when (binop.operator.type) {
            TokenType.PLUS -> {
                when (left) {
                    is OasisPrototype -> (left.get("__plus") as OasisCallable).call(this, listOf(right))
                    is Double -> left + (right as Number).toDouble()
                    is Int -> left + (right as Number).toInt()
                    is String -> left + right.toString()
                    else -> throw RuntimeError(binop.line, "Cannot add")
                }
            }
            TokenType.MINUS -> {
                when (left) {
                    is OasisPrototype -> (left.get("__sub") as OasisCallable).call(this, listOf(right))
                    is Double -> left - (right as Number).toDouble()
                    is Int -> left - (right as Number).toInt()
                    else -> throw RuntimeError(binop.line, "Cannot subtract")
                }
            }
            TokenType.STAR -> {
                when (left) {
                    is OasisPrototype -> (left.get("__mul") as OasisCallable).call(this, listOf(right))
                    is Double -> left * (right as Number).toDouble()
                    is Int -> left * (right as Number).toInt()
                    else -> throw RuntimeError(binop.line, "Cannot multiply")
                }
            }
            TokenType.SLASH -> {
                when (left) {
                    is OasisPrototype -> (left.get("__div") as OasisCallable).call(this, listOf(right))
                    is Double -> left / (right as Number).toDouble()
                    is Int -> left / (right as Number).toInt()
                    else -> {
                        throw RuntimeError(binop.line, "Cannot divide")
                    }
                }
            }
            TokenType.EQUAL_EQUAL -> when (left) {
                is Class<*> -> left.isAssignableFrom(right!! as Class<*>) || (right as Class<*>).isAssignableFrom(left)
                else -> if (left != null) {
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
                when (left) {
                    is Double -> left > (right as Number).toDouble()
                    is Int -> left > (right as Number).toInt()
                    else -> throw RuntimeError(binop.line, "Cannot greater")
                }
            }
            TokenType.GREATER_EQUAL -> {
                when (left) {
                    is Double -> left >= (right as Number).toDouble()
                    is Int -> left >= (right as Number).toInt()
                    else -> throw RuntimeError(binop.line, "Cannot greater equal")
                }
            }
            TokenType.LESS -> {
                when (left) {
                    is Double -> left < (right as Number).toDouble()
                    is Int -> left < (right as Number).toInt()
                    else -> throw RuntimeError(binop.line, "Cannot less")
                }
            }
            TokenType.LESS_EQUAL -> {
                when (left) {
                    is Double -> left <= (right as Number).toDouble()
                    is Int -> left <= (right as Number).toInt()
                    else -> throw RuntimeError(binop.line, "Cannot less equal")
                }
            }
            TokenType.MOD -> {
                when (left) {
                    is Number -> oasisMod(left.toInt(), (right as Number).toInt())
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
        val result = environment.get(variable.name.lexeme.hashCode())
        if (result is RelativeExpression) {
            return result.expr.accept(this)
        }
        return result
    }

    override fun visitProto(proto: Proto): Any {
        val protoType =
            OasisPrototype((if (proto.base != null) eval(proto.base!!) else base) as OasisPrototype?, proto.line, this)
        proto.body.stmts.map {
            if (it is Let) {
                if (it.left.size > 1) throw RuntimeError(it.line, "Multiple let definitions not allowed in prototype")
                protoType.set(it.left[0].lexeme, eval(it.value))
            } else
                protoType.set(
                    ((((it as ExprStmt).expr as AssignmentExpr).left as? Variable) ?: throw RuntimeError(it.line, "Invalid prototype definition")).name.lexeme,
                    eval((it.expr as AssignmentExpr).value)
                )
        }
        return protoType
    }

    override fun visitLet(let: Let) {
        if (let.left.size > 1) {
            val right = eval(let.value)
            if (right !is Iterable<*>) {
                throw RuntimeError(let.line, "Right side of multiple let must be iterable")
            }
            for (i in 0 until  let.left.size) {
                environment.define(let.left[i].lexeme.hashCode(), right.elementAt(i))
            }
        } else {
            environment.define((let.left[0].lexeme).hashCode(), eval(let.value))
        }
    }

    override fun visitIfStmt(ifstmt: IfStmt): Boolean {
        return if (isTruthy(eval(ifstmt.expr))) {
            ifstmt.stmtlist.accept(this)
            true
        } else {
            ifstmt.elseBody?.accept(this)
            false
        }
    }

    override fun visitWhileStmt(whilestmt: WhileStmt) {
        while (isTruthy(eval(whilestmt.expr))) {
            try {
                whilestmt.body.accept(this)
            } catch (internalException: InternalException) {
                when (internalException.type) {
                    ExceptionType.BREAK -> break
                    ExceptionType.CONTINUE -> continue
                    ExceptionType.ITERATOR_EMPTY -> throw internalException
                }
            }
        }
    }

    override fun visitStmtList(stmtlist: StmtList) {
        execute(stmtlist)
    }

    override fun visitReturnStmt(retstmt: RetStmt) {
        retInstance.value = if (retstmt.expr != null) eval(retstmt.expr!!) else null
        throw retInstance
    }

    override fun visitExprStmt(exprStmt: ExprStmt) {
        if (repl) {
            repl = false
            eval(exprStmt.expr).let { if (it !is Unit) println(it) }
        } else {
            exprStmt.expr.accept(this)
        }
    }

    override fun visitIndexer(indexer: Indexer): Any? {
        return when (val x = eval(indexer.expr)) {
            is String -> x[(eval(indexer.index) as Number).toInt()]
            is ArrayList<*> -> x[(eval(indexer.index) as Number).toInt()]
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
        return when (val x = eval(negate.value)) {
            is Double -> -x
            is Int -> -x
            is Long -> -x
            else -> (x as? Number)?.toInt() ?: throw RuntimeError(negate.line, "Cannot negate")
        }
    }

    override fun visitNew(ref: New): Any? {
        return eval(ref.expr)?.let {
            if (it is Cloneable) {
                it.javaClass.getMethod("clone").invoke(it)
            } else throw RuntimeError(ref.line, "Cannot clone object")
        }
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
            it.accept(this)
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
        environment.get(precomputed.hash).let {
            if (it is RelativeExpression) {
                return it.expr.accept(this)
            }
            return it
        }
    }

    override fun visitForLoopTriad(forLoopTriad: ForLoopTriad) {
        forLoopTriad.init.accept(this)
        while (isTruthy(eval(forLoopTriad.cond))) {
            try {
                forLoopTriad.body.accept(this)
                forLoopTriad.step.accept(this)
            } catch (internalException: InternalException) {
                when (internalException.type) {
                    ExceptionType.BREAK -> break
                    ExceptionType.CONTINUE -> continue
                    ExceptionType.ITERATOR_EMPTY -> throw internalException // keep it going, doesn't matter here
                }
            }
        }
    }

    override fun visitForLoopIterator(forLoopIterator: ForLoopIterator) {
        when (val iteratorExpr = eval(forLoopIterator.iterable)) {
            is OasisPrototype -> {
                val iterator = iteratorExpr.get("__iterator") as OasisCallable
                var index = 0
                while (true) {
                    try { // Kill this with fire
                        environment.values[(forLoopIterator.varName as Variable).name.lexeme.hashCode()] =
                            iterator.call(this, listOf(index))
                    } catch (internalException: InternalException) {
                        when (internalException.type) {
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
                    try {
                        environment.values[(forLoopIterator.varName as Variable).name.lexeme.hashCode()] = element
                        forLoopIterator.body.accept(this)
                    } catch (internalException: InternalException) {
                        when (internalException.type) {
                            ExceptionType.BREAK -> break
                            ExceptionType.CONTINUE -> continue
                            ExceptionType.ITERATOR_EMPTY -> throw internalException // keep it going, doesn't matter here
                        }
                    }
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
        when (val iteratorExpr = eval(listComprehension.inVal)) {
            is OasisPrototype -> {
                val iterator = iteratorExpr.get("__iterator") as OasisCallable
                var index = 0
                val list = ArrayList<Any?>()
                while (true) {
                    try { // Kill this with fire
                        list.add(
                            (eval(listComprehension.expr) as OasisCallable).call(
                                this,
                                listOf(iterator.call(this, listOf(index)))
                            )
                        )
                    } catch (internalException: InternalException) {
                        when (internalException.type) {
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

    override fun visitMapLiteral(mapLiteral: MapLiteral): Any {
        val map = HashMap<Any?, Any?>()
        mapLiteral.exprs.map {
            map[eval(it.first)] = eval(it.second)
        }
        return createHashMap(map, this)
    }

    override fun visitIfExpression(ifExpression: IfExpression): Any? {
        return if (isTruthy(eval(ifExpression.expr))) {
            eval(ifExpression.thenExpr)
        } else {
            eval(ifExpression.elseExpr)
        }
    }

    override fun visitTuple(tuple: Tuple): Any {
        return OasisTuple(tuple.exprs.size, ArrayList(tuple.exprs.map { eval(it) }))
    }

    override fun visitRelStmt(relstmt: RelStmt) {
        environment.define(relstmt.name.lexeme.hashCode(), RelativeExpression(relstmt.expr))
    }

    override fun visitDoBlock(doblock: DoBlock) {
        doblock.body.accept(this)
    }
}