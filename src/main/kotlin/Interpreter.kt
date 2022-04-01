import standardLibrary.StandardLibrary
import standardLibrary.base

class Interpreter : Expr.Visitor<Any?>, Stmt.Visitor<Unit> {

    private var globals = Environment()
    var environment: Environment = globals

    private var retInstance = Return(null)

    init {
        StandardLibrary.generateLib(environment)
        //AtomicReference(Oasis.getGlobalInterpreter).value = this
    }

    inline fun eval(expr: Expr): Any? {
//        try {
            return expr.accept(this)
  /*      } catch (e: Return) {
            throw e
        } catch (e: Exception )  {
            if(e is RuntimeError) {
                throw e
            }
            e.printStackTrace()
            throw RuntimeError(expr.line, e.toString())
        }
    */}
    inline fun execute(stmt: Stmt) {
 //       try {
            stmt.accept(this)
/*        } catch (e: Return) {
            throw e
        } catch (e: Exception )  {
            if(e is RuntimeError) {
                throw e
            }
            e.printStackTrace()
            throw RuntimeError(stmt.line, e.toString())
        }
*/
    }

    inline fun execute(stmtList: StmtList) {
        executeBlock(stmtList, environment)
    }

    inline fun executeBlock(stmtList: StmtList, env: Environment) {
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
            is Property -> (eval(assignment.left.obj) as OasisPrototype).set(assignment.left.indexer.lexeme, a)
            is Variable -> environment.assign(assignment.left.name, a)
            is Indexer -> (eval(assignment.left.expr) as ArrayList<Any?>)[(eval(assignment.left.index) as Double).toInt()] =
                a
            else -> {
                throw RuntimeError(assignment.line, "Cannot assign")
            }
        }
        return a
    }

    override fun visitProperty(property: Property): Any? {
        return (eval(property.obj) as OasisPrototype).get(property.indexer.lexeme)
    }

    override fun visitFunc(func: Func): Any {
        return OasisFunction(func, environment)
    }

    override fun visitFcall(fcall: FCallExpr): Any? {
        val callee = (eval(fcall.func) ?: throw RuntimeError(fcall.line, "cannot call null function")) as OasisCallable
        val arguments: ArrayList<Any?> = fcall.operands.map { eval(it) } as ArrayList<Any?>
        if (callee.arity() != fcall.operands.size) throw RuntimeError(fcall.line, "function call required ${callee.arity()} arguments, got ${fcall.operands.size}")
        return callee.call(this, arguments)
    }

    override fun visitBinOp(binop: BinOp): Any? {
        val left = eval(binop.left)
        val right = eval(binop.right)
        when(binop.operator.type) {
            TokenType.PLUS -> {
                return when(left) {
                    is OasisPrototype -> (left.get("__plus") as OasisCallable).call(this, listOf(right))
                    is Double -> left + right as Double
                    is String -> left + right.toString()
                    else -> throw RuntimeError(binop.line, "Cannot add")
                }
            }
            TokenType.MINUS -> {
                return when(left) {
                    is OasisPrototype -> (left.get("__sub") as OasisCallable).call(this, listOf(right))
                    is Double -> left - right as Double
                    else -> throw RuntimeError(binop.line, "Cannot subtract")
                }
            }
            TokenType.STAR -> {
                return when(left) {
                    is OasisPrototype -> (left.get("__mul") as OasisCallable).call(this, listOf(right))
                    is Double -> left * right as Double
                    else -> throw RuntimeError(binop.line, "Cannot multiply")
                }
            }
            TokenType.SLASH -> {
                return when(left) {
                    is OasisPrototype -> (left.get("__div") as OasisCallable).call(this, listOf(right))
                    is Double -> left / right as Double
                    else -> throw RuntimeError(binop.line, "Cannot divide")
                }
            }
            TokenType.EQUAL_EQUAL -> {
                if (left != null) {
                    return left == right
                } else {
                    if (right == null) {
                        return true
                    }
                    return false
                }
            }
            TokenType.BANG_EQUAL -> {
                if (left != null) {
                    return left != right
                } else {
                    if (right != null) {
                        return true
                    }
                    return false
                }
            }
            TokenType.GREATER -> {
                return (left as Double) > (right as Double)
            }
            TokenType.GREATER_EQUAL -> {
                return (left as Double) >= (right as Double)
            }
            TokenType.LESS -> {
                return (left as Double) < (right as Double)
            }
            TokenType.LESS_EQUAL -> {
                return (left as Double) <= (right as Double)
            }
            TokenType.MOD -> {
                return (left as Double) % (right as Double)
            }
            else -> throw RuntimeError(binop.line, "Invalid operator")
        }
    }

    override fun visitGroup(group: Group): Any? {
        return group.expr.accept(this)
    }

    override fun visitVariable(variable: Variable): Any? {
        return environment.get(variable.name)
    }

    override fun visitProto(proto: Proto): Any {
        val protoType = OasisPrototype((if (proto.base != null) environment.get(proto.base) else base) as OasisPrototype?, proto.line)
        proto.body.stmts.map { protoType.set((it as Let).left.lexeme, eval(it.value)) }
        return protoType
    }

    override fun visitLet(let: Let) {
        environment.define(let.left.lexeme, eval(let.value))
    }

    override fun visitIfStmt(ifstmt: IfStmt) {
        if(eval(ifstmt.expr) as Boolean) {
            execute(ifstmt.stmtlist)
        } else {
            ifstmt.elseBody?.let { execute(it) }
        }
    }

    override fun visitWhileStmt(whilestmt: WhileStmt) {
        while(eval(whilestmt.expr) as Boolean) {
            execute(whilestmt.body)
        }
    }

    override fun visitStmtList(stmtlist: StmtList) {
        stmtlist.stmts.map {
            execute(it)
        }
    }

    override fun visitReturnStmt(retstmt: RetStmt) {
        retInstance.value = if (retstmt.expr != null) eval(retstmt.expr) else null
        throw retInstance
    }

    override fun visitExprStmt(exprStmt: ExprStmt) {
        if(repl) {
            eval(exprStmt.expr).let { if(it !is Unit) println(it) }
        } else {
            exprStmt.expr.accept(this)
        }
    }

    override fun visitIndexer(indexer: Indexer): Any? {
        return when(val x = eval(indexer.expr)) {
            is String -> x[(eval(indexer.index) as Double).toInt()]
            is ArrayList<*> -> x[(eval(indexer.index) as Double).toInt()]
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

}