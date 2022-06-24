package me.snwy.oasis.experimental

import me.snwy.oasis.*

class BytecodeCompiler : Expr.Visitor<Unit>, Stmt.Visitor<Unit> {

    private val symbols: ArrayList<String> = ArrayList()
    private val code: ArrayList<UIntArray> = ArrayList()

    private fun addSymbol(value: String): UInt {
        symbols.add(value)
        return symbols.size.toUInt() - 1u
    }

    private fun returns(stmt: Stmt): Boolean {
        when (stmt) {
            is RetStmt -> {
                return true
            }
            is StmtList -> {
                for (stmt in stmt.stmts) {
                    if (returns(stmt)) {
                        return true
                    }
                }
            }
            is IfStmt -> {
                if (returns(stmt.stmtlist) && (stmt.elseBody != null && returns(stmt.elseBody!!))) {
                    return true
                }
            }
            is WhileStmt -> {
                if (returns(stmt.body)) {
                    return true
                }
            }
            is Is -> {
                for (stmt in stmt.cases.stmts) {
                    if (returns(stmt)) {
                        return true
                    }
                }
            }
        }
        return false
    }

    override fun visitLiteral(literal: Literal) {
        when (literal.value) {
            is Double -> code.add(uintArrayOf(0x0u, literal.value.toFloat().toRawBits().toUInt()))
            is String -> {
                code.add(uintArrayOf(0x01u, addSymbol(literal.value)))
            }
            is Boolean -> code.add(uintArrayOf(0x02u, if (literal.value) 1u else 0u))
            is Char -> code.add(uintArrayOf(0x04u, literal.value.code.toUInt()))
            null -> code.add(uintArrayOf(0x03u, 0x00u))
        }
    }

    override fun visitAssignment(assignment: AssignmentExpr) {
        when (assignment.left) {
            is Variable -> {
                assignment.value.accept(this)
                code.add(
                    uintArrayOf(
                        0x1au,
                        (assignment.left as Variable).name.lexeme.hashCode().toUInt()
                    )
                )
            }
            is Precomputed -> {
                assignment.value.accept(this)
                code.add(uintArrayOf(0x1au, (assignment.left as Precomputed).hash.toUInt()))
            }
            is Property -> {
                (assignment.left as Property).obj.accept(this)
                code.add(uintArrayOf(0x01u, addSymbol((assignment.left as Property).indexer.lexeme)))
                assignment.value.accept(this)
                code.add(uintArrayOf(0x0Fu, 0x00u))
            }
            is Indexer -> {
                (assignment.left as Indexer).expr.accept(this)
                (assignment.left as Indexer).index.accept(this)
                code.add(uintArrayOf(0x11u, 0x00u))
            }
        }
    }

    override fun visitProperty(property: Property) {
        property.obj.accept(this)
        code.add(uintArrayOf(0x01u, addSymbol(property.indexer.lexeme)))
        code.add(uintArrayOf(0x0Eu, 0x00u))
    }

    override fun visitFunc(func: Func) {
        // create func -> arg1, arg2, arg3, ... -> code
        code.add(uintArrayOf(0x1bu, (code.size + func.operands.size + 2).toUInt()))
        for (arg in func.operands) {
            code.add(uintArrayOf(0x1cu, arg.lexeme.hashCode().toUInt()))
        }
        code.add(uintArrayOf(0x0au, 0x00u))
        val jmpIndex = code.size - 1
        func.body.accept(this)
        if (!returns(func.body)) {
            code.add(uintArrayOf(0x03u, 0x00u))
            code.add(uintArrayOf(0x09u, 0x00u))
        }
        code[jmpIndex][1] = (code.size).toUInt()
    }

    override fun visitFcall(fcall: FCallExpr) {
        fcall.func.accept(this)
        for (arg in fcall.operands.reversed()) {
            arg.accept(this)
        }
    }

    override fun visitBinOp(binop: BinOp) {
        binop.left.accept(this)
        binop.right.accept(this)
        when (binop.operator.lexeme) {
            "+" -> code.add(uintArrayOf(0x13u, 0x00u))
            "-" -> code.add(uintArrayOf(0x14u, 0x00u))
            "*" -> code.add(uintArrayOf(0x15u, 0x00u))
            "/" -> code.add(uintArrayOf(0x16u, 0x00u))
            "%" -> code.add(uintArrayOf(0x17u, 0x00u))
            "and" -> code.add(uintArrayOf(0x18u, 0x00u))
            "or" -> code.add(uintArrayOf(0x19u, 0x00u))
            "==" -> code.add(uintArrayOf(0x1eu, 0x00u))
            "!=" -> {
                code.add(uintArrayOf(0x1eu, 0x00u))
                code.add(uintArrayOf(0x1fu, 0x00u))
            }
        }
    }

    override fun visitGroup(group: Group) {
        group.expr.accept(this)
    }

    override fun visitVariable(variable: Variable) {
        code.add(uintArrayOf(0x05u, variable.name.lexeme.hashCode().toUInt()))
    }

    override fun vistPrecomputed(precomputed: Precomputed) {
        code.add(uintArrayOf(0x05u, precomputed.hash.toUInt()))
    }

    override fun visitProto(proto: Proto) {
        TODO("Not yet implemented")
    }

    override fun visitIndexer(indexer: Indexer) {
        TODO("Not yet implemented")
    }

    override fun visitList(list: OasisList) {
        TODO("Not yet implemented")
    }

    override fun visitNegate(negate: Negate) {
        TODO("Not yet implemented")
    }

    override fun visitNew(ref: New) {
        TODO("Not yet implemented")
    }

    override fun visitNot(not: Not) {
        TODO("Not yet implemented")
    }

    override fun visitLet(let: Let) {
        TODO("Not yet implemented")
    }

    override fun visitIfStmt(ifstmt: IfStmt) {
        TODO("Not yet implemented")
    }

    override fun visitWhileStmt(whilestmt: WhileStmt) {
        TODO("Not yet implemented")
    }

    override fun visitStmtList(stmtlist: StmtList) {
        TODO("Not yet implemented")
    }

    override fun visitReturnStmt(retstmt: RetStmt) {
        TODO("Not yet implemented")
    }

    override fun visitExprStmt(exprStmt: ExprStmt) {
        TODO("Not yet implemented")
    }

    override fun visitIs(is_: Is) {
        TODO("Not yet implemented")
    }

    override fun visitTest(test: Test) {
        TODO("Not yet implemented")
    }

    override fun visitForLoopTriad(forLoopTriad: ForLoopTriad) {
        TODO("Not yet implemented")
    }

    override fun visitForLoopIterator(forLoopIterator: ForLoopIterator) {
        TODO("Not yet implemented")
    }

    override fun visitBreakStmt(break_: BreakStmt) {
        TODO("Not yet implemented")
    }

    override fun visitContinueStmt(continue_: ContinueStmt) {
        TODO("Not yet implemented")
    }

    override fun visitListComprehension(listComprehension: ListComprehension) {
        TODO("Not yet implemented")
    }

    override fun visitMapLiteral(mapLiteral: MapLiteral) {
        TODO("Not yet implemented")
    }

    override fun visitIfExpression(ifExpression: IfExpression) {
        TODO("Not yet implemented")
    }

    override fun visitRelStmt(relstmt: RelStmt) {
        TODO("Not yet implemented")
    }
}