package me.snwy.oasis

class Optimizer : Expr.Visitor<Expr>, Stmt.Visitor<Stmt> {

    companion object {
        val nameMap = mutableMapOf<Int, String>()
        val immutables = mutableMapOf<Int, Any?>()
    }

    fun optimize(stmt: Stmt): Stmt {
        return stmt.accept(this)
    }

    override fun visitLiteral(literal: Literal): Expr {
        return literal
    }

    override fun visitAssignment(assignment: AssignmentExpr): Expr {
        assignment.left = assignment.left.accept(this)
        assignment.value = assignment.value.accept(this)
        return assignment
    }

    override fun visitProperty(property: Property): Expr {
        property.obj = property.obj.accept(this)
        return property
    }

    override fun visitFunc(func: Func): Expr {
        func.body = func.body.accept(this) as StmtList
        return func
    }

    override fun visitFcall(fcall: FCallExpr): Expr {
        fcall.func = fcall.func.accept(this)
        fcall.operands = fcall.operands.map {
            it.accept(this)
        }

        return fcall
    }

    override fun visitBinOp(binop: BinOp): Expr {
        binop.left = binop.left.accept(this)
        binop.right = binop.right.accept(this)
        if (binop.left !is BinOp && binop.right !is BinOp) {
            when(binop.left) {
                is Precomputed -> {
                    if (immutables.contains((binop.left as Precomputed).hash)) {
                        binop.left = (immutables[(binop.left as Precomputed).hash] as Expr?)!!
                    }
                }
            }
            when(binop.right) {
                is Precomputed -> {
                    if (immutables.contains((binop.right as Precomputed).hash)) {
                        binop.right = (immutables[(binop.right as Precomputed).hash] as Expr?)!!
                    }
                }
            }
            when(binop.operator.type) {
                TokenType.PLUS -> {
                    if (binop.left is Literal && binop.right is Literal) {
                        when((binop.left as Literal).value) {
                            is Double -> {
                                val left = (binop.left as Literal).value as Double
                                when((binop.right as Literal).value) {
                                    is Double -> {
                                        val right = (binop.right as Literal).value as Double
                                        return Literal(left + right, binop.line)
                                    }
                                }
                            }
                            is String -> {
                                val left = (binop.left as Literal).value as String
                                when((binop.right as Literal).value) {
                                    is String -> {
                                        val right = (binop.right as Literal).value as String
                                        return Literal(left + right, binop.line)
                                    }
                                    is Double -> {
                                        val right = (binop.right as Literal).value as Double
                                        return Literal(left + right.toString(), binop.line)
                                    }
                                }
                            }
                        }
                    }
                }
                TokenType.MINUS -> {
                    if (binop.left is Literal && binop.right is Literal) {
                        when((binop.left as Literal).value) {
                            is Double -> {
                                val left = (binop.left as Literal).value as Double
                                when((binop.right as Literal).value) {
                                    is Double -> {
                                        val right = (binop.right as Literal).value as Double
                                        return Literal(left - right, binop.line)
                                    }
                                }
                            }
                        }
                    }
                }
                TokenType.STAR -> {
                    if (binop.left is Literal && binop.right is Literal) {
                        when((binop.left as Literal).value) {
                            is Double -> {
                                val left = (binop.left as Literal).value as Double
                                when((binop.right as Literal).value) {
                                    is Double -> {
                                        val right = (binop.right as Literal).value as Double
                                        return Literal(left * right, binop.line)
                                    }
                                }
                            }
                        }
                    }
                }
                TokenType.SLASH -> {
                    if (binop.left is Literal && binop.right is Literal) {
                        when((binop.left as Literal).value) {
                            is Double -> {
                                val left = (binop.left as Literal).value as Double
                                when((binop.right as Literal).value) {
                                    is Double -> {
                                        val right = (binop.right as Literal).value as Double
                                        return Literal(left / right, binop.line)
                                    }
                                }
                            }
                        }
                    }
                }
                TokenType.MOD -> {
                    if (binop.left is Literal && binop.right is Literal) {
                        when((binop.left as Literal).value) {
                            is Double -> {
                                val left = (binop.left as Literal).value as Double
                                when((binop.right as Literal).value) {
                                    is Double -> {
                                        val right = (binop.right as Literal).value as Double
                                        return Literal(left % right, binop.line)
                                    }
                                }
                            }
                        }
                    }
                }
                TokenType.AND -> {
                    if (binop.left is Literal && binop.right is Literal) {
                        when((binop.left as Literal).value) {
                            is Boolean -> {
                                val left = (binop.left as Literal).value as Boolean
                                when((binop.right as Literal).value) {
                                    is Boolean -> {
                                        val right = (binop.right as Literal).value as Boolean
                                        return Literal(left && right, binop.line)
                                    }
                                }
                            }
                        }
                    }
                }
                TokenType.OR -> {
                    if (binop.left is Literal && binop.right is Literal) {
                        when((binop.left as Literal).value) {
                            is Boolean -> {
                                val left = (binop.left as Literal).value as Boolean
                                when((binop.right as Literal).value) {
                                    is Boolean -> {
                                        val right = (binop.right as Literal).value as Boolean
                                        return Literal(left || right, binop.line)
                                    }
                                }
                            }
                        }
                    }
                }
                TokenType.BANG_EQUAL -> {
                    if (binop.left is Literal && binop.right is Literal) {
                        when((binop.left as Literal).value) {
                            is Double -> {
                                val left = (binop.left as Literal).value as Double
                                when((binop.right as Literal).value) {
                                    is Double -> {
                                        val right = (binop.right as Literal).value as Double
                                        return Literal(left != right, binop.line)
                                    }
                                }
                            }
                            is String -> {
                                val left = (binop.left as Literal).value as String
                                when((binop.right as Literal).value) {
                                    is String -> {
                                        val right = (binop.right as Literal).value as String
                                        return Literal(left != right, binop.line)
                                    }
                                }
                            }
                            is Boolean -> {
                                val left = (binop.left as Literal).value as Boolean
                                when((binop.right as Literal).value) {
                                    is Boolean -> {
                                        val right = (binop.right as Literal).value as Boolean
                                        return Literal(left != right, binop.line)
                                    }
                                }
                            }
                        }
                    }
                }
                TokenType.EQUAL_EQUAL -> {
                    if (binop.left is Literal && binop.right is Literal) {
                        when((binop.left as Literal).value) {
                            is Double -> {
                                val left = (binop.left as Literal).value as Double
                                when((binop.right as Literal).value) {
                                    is Double -> {
                                        val right = (binop.right as Literal).value as Double
                                        return Literal(left == right, binop.line)
                                    }
                                }
                            }
                            is String -> {
                                val left = (binop.left as Literal).value as String
                                when((binop.right as Literal).value) {
                                    is String -> {
                                        val right = (binop.right as Literal).value as String
                                        return Literal(left == right, binop.line)
                                    }
                                }
                            }
                            is Boolean -> {
                                val left = (binop.left as Literal).value as Boolean
                                when((binop.right as Literal).value) {
                                    is Boolean -> {
                                        val right = (binop.right as Literal).value as Boolean
                                        return Literal(left == right, binop.line)
                                    }
                                }
                            }
                        }
                    }
                }
                TokenType.GREATER -> {
                    if (binop.left is Literal && binop.right is Literal) {
                        when((binop.left as Literal).value) {
                            is Double -> {
                                val left = (binop.left as Literal).value as Double
                                when((binop.right as Literal).value) {
                                    is Double -> {
                                        val right = (binop.right as Literal).value as Double
                                        return Literal(left > right, binop.line)
                                    }
                                }
                            }
                        }
                    }
                }
                TokenType.GREATER_EQUAL -> {
                    if (binop.left is Literal && binop.right is Literal) {
                        when((binop.left as Literal).value) {
                            is Double -> {
                                val left = (binop.left as Literal).value as Double
                                when((binop.right as Literal).value) {
                                    is Double -> {
                                        val right = (binop.right as Literal).value as Double
                                        return Literal(left >= right, binop.line)
                                    }
                                }
                            }
                        }
                    }
                }
                TokenType.LESS -> {
                    if (binop.left is Literal && binop.right is Literal) {
                        when((binop.left as Literal).value) {
                            is Double -> {
                                val left = (binop.left as Literal).value as Double
                                when((binop.right as Literal).value) {
                                    is Double -> {
                                        val right = (binop.right as Literal).value as Double
                                        return Literal(left < right, binop.line)
                                    }
                                }
                            }
                        }
                    }
                }
                TokenType.LESS_EQUAL -> {
                    if (binop.left is Literal && binop.right is Literal) {
                        when((binop.left as Literal).value) {
                            is Double -> {
                                val left = (binop.left as Literal).value as Double
                                when((binop.right as Literal).value) {
                                    is Double -> {
                                        val right = (binop.right as Literal).value as Double
                                        return Literal(left <= right, binop.line)
                                    }
                                }
                            }
                        }
                    }
                }
                else -> {}
            }
        }
        return binop
    }

    override fun visitGroup(group: Group): Expr {
        group.expr = group.expr.accept(this)
        return group.expr
    }

    override fun visitVariable(variable: Variable): Expr {
        if(immutables.contains(variable.name.hashCode())) {
            return immutables[variable.name.hashCode()] as Expr
        }
        nameMap[variable.name.lexeme.hashCode()] = variable.name.lexeme
        return Precomputed(variable.name.lexeme.hashCode(), variable.line)
    }

    override fun visitProto(proto: Proto): Expr {
        proto.base = proto.base?.accept(this)
        return proto
    }

    override fun visitIndexer(indexer: Indexer): Expr {
        indexer.expr = indexer.expr.accept(this)
        indexer.index = indexer.index.accept(this)
        return indexer
    }

    override fun visitList(list: OasisList): Expr {
        list.exprs = list.exprs.map {
            it.accept(this)
        } as ArrayList<Expr>
        return list
    }

    override fun visitNegate(negate: Negate): Expr {
        negate.value = negate.value.accept(this)
        return negate
    }

    override fun visitNew(ref: New): Expr {
        ref.expr = ref.expr.accept(this)
        return ref
    }

    override fun visitNot(not: Not): Expr {
        not.expr = not.expr.accept(this)
        return not
    }

    override fun visitLet(let: Let): Stmt {
        let.value = let.value.accept(this)
        if (let.immutable) {
            immutables[let.left.lexeme.hashCode()] = let.value.accept(this)
        }
        return let
    }

    override fun visitIfStmt(ifstmt: IfStmt): Stmt {
        ifstmt.expr = ifstmt.expr.accept(this)
        ifstmt.elseBody = ifstmt.elseBody?.accept(this) as StmtList?
        ifstmt.stmtlist = ifstmt.stmtlist.accept(this) as StmtList
        return ifstmt
    }

    override fun visitWhileStmt(whilestmt: WhileStmt): Stmt {
        whilestmt.expr = whilestmt.expr.accept(this)
        whilestmt.body = whilestmt.body.accept(this) as StmtList
        return whilestmt
    }

    override fun visitStmtList(stmtlist: StmtList): Stmt {
        stmtlist.stmts = stmtlist.stmts.map {
            it.accept(this)
        } as ArrayList<Stmt>
        return stmtlist
    }

    override fun visitReturnStmt(retstmt: RetStmt): Stmt {
        retstmt.expr = retstmt.expr?.accept(this)
        return retstmt
    }

    override fun visitExprStmt(exprStmt: ExprStmt): Stmt {
        exprStmt.expr = exprStmt.expr.accept(this)
        return exprStmt
    }

    override fun visitIs(is_: Is): Stmt {
        is_.expr = is_.expr.accept(this)
        is_.cases.stmts = is_.cases.stmts.map {
            it.accept(this)
        } as ArrayList<Stmt>
        is_.else_?.stmts = is_.else_?.stmts?.map {
            it.accept(this)
        } as ArrayList<Stmt>
        return is_
    }

    override fun vistPrecomputed(precomputed: Precomputed): Expr {
        return precomputed
    }

    override fun visitTest(test: Test): Stmt {
        test.block = test.block.accept(this) as StmtList
        test.errorBlock = test.errorBlock.accept(this) as StmtList
        return test
    }

    override fun visitForLoopTriad(forLoopTriad: ForLoopTriad): Stmt {
        forLoopTriad.init = forLoopTriad.init.accept(this)
        forLoopTriad.cond = forLoopTriad.cond.accept(this)
        forLoopTriad.step = forLoopTriad.step.accept(this)
        forLoopTriad.body = forLoopTriad.body.accept(this) as StmtList
        return forLoopTriad
    }

    override fun visitForLoopIterator(forLoopIterator: ForLoopIterator): Stmt {
        forLoopIterator.iterable = forLoopIterator.iterable.accept(this)
        forLoopIterator.body = forLoopIterator.body.accept(this) as StmtList
        return forLoopIterator
    }

    override fun visitBreakStmt(break_: BreakStmt): Stmt {
        return break_
    }

    override fun visitContinueStmt(continue_: ContinueStmt): Stmt {
       return continue_
    }

    override fun visitListComprehension(listComprehension: ListComprehension): Expr {
        listComprehension.expr = listComprehension.expr.accept(this)
        listComprehension.inVal = listComprehension.inVal.accept(this)
        return listComprehension
    }

}