package me.snwy.oasis.experimental

import me.snwy.*
import me.snwy.oasis.*

class BytecodeCompiler : me.snwy.oasis.Expr.Visitor<Unit>, me.snwy.oasis.Stmt.Visitor<Unit> {

    private val symbols: ArrayList<String> = ArrayList()
    private val code: ArrayList<UIntArray> = ArrayList()

    fun addSymbol(value: String) : UInt {
        symbols.add(value)
        return symbols.size.toUInt() - 1u
    }

    fun returns(stmt: me.snwy.oasis.Stmt) : Boolean {
        when (stmt) {
            is me.snwy.oasis.RetStmt -> {
                return true
            }
            is me.snwy.oasis.StmtList -> {
                for (stmt in stmt.stmts) {
                    if (returns(stmt)) {
                        return true
                    }
                }
            }
            is me.snwy.oasis.IfStmt -> {
                if (returns(stmt.stmtlist) && (stmt.elseBody != null && returns(stmt.elseBody!!))) {
                    return true
                }
            }
            is me.snwy.oasis.WhileStmt -> {
                if (returns(stmt.body)) {
                    return true
                }
            }
            is me.snwy.oasis.Is -> {
                for (stmt in stmt.cases.stmts) {
                    if (returns(stmt)) {
                        return true
                    }
                }
            }
        }
        return false
    }

    override fun visitLiteral(literal: me.snwy.oasis.Literal) {
        when(literal.value) {
            is Double -> code.add(uintArrayOf(0x0u, literal.value.toFloat().toRawBits().toUInt()))
            is String -> {
                code.add(uintArrayOf(0x01u, addSymbol(literal.value)))
            }
            is Boolean -> code.add(uintArrayOf(0x02u, if (literal.value) 1u else 0u))
            is Char -> code.add(uintArrayOf(0x04u, literal.value.code.toUInt()))
            null -> code.add(uintArrayOf(0x03u, 0x00u))
        }
    }

    override fun visitAssignment(assignment: me.snwy.oasis.AssignmentExpr) {
        when(assignment.left) {
            is me.snwy.oasis.Variable -> {
                assignment.value.accept(this)
                code.add(uintArrayOf(0x1au, (assignment.left as me.snwy.oasis.Variable).name.lexeme.hashCode().toUInt()))
            }
            is me.snwy.oasis.Precomputed -> {
                assignment.value.accept(this)
                code.add(uintArrayOf(0x1au, (assignment.left as me.snwy.oasis.Precomputed).hash.toUInt()))
            }
            is me.snwy.oasis.Property -> {
                (assignment.left as me.snwy.oasis.Property).obj.accept(this)
                code.add(uintArrayOf(0x01u, addSymbol((assignment.left as me.snwy.oasis.Property).indexer.lexeme)))
                assignment.value.accept(this)
                code.add(uintArrayOf(0x0Fu, 0x00u))
            }
            is me.snwy.oasis.Indexer -> {
                (assignment.left as me.snwy.oasis.Indexer).expr.accept(this)
                (assignment.left as me.snwy.oasis.Indexer).index.accept(this)
                code.add(uintArrayOf(0x11u, 0x00u))
            }
        }
    }

    override fun visitProperty(property: me.snwy.oasis.Property) {
        property.obj.accept(this)
        code.add(uintArrayOf(0x01u, addSymbol(property.indexer.lexeme)))
        code.add(uintArrayOf(0x0Eu, 0x00u))
    }

    override fun visitFunc(func: me.snwy.oasis.Func) {
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

    override fun visitFcall(fcall: me.snwy.oasis.FCallExpr) {
        fcall.func.accept(this)
        for (arg in fcall.operands.reversed()) {
            arg.accept(this)
        }
    }

    override fun visitBinOp(binop: me.snwy.oasis.BinOp) {
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

    override fun visitGroup(group: me.snwy.oasis.Group) {
        group.expr.accept(this)
    }

    override fun visitVariable(variable: me.snwy.oasis.Variable) {
        code.add(uintArrayOf(0x05u, variable.name.lexeme.hashCode().toUInt()))
    }

    override fun vistPrecomputed(precomputed: me.snwy.oasis.Precomputed) {
        code.add(uintArrayOf(0x05u, precomputed.hash.toUInt()))
    }

    override fun visitProto(proto: me.snwy.oasis.Proto) {
        TODO("Not yet implemented")
    }

    override fun visitIndexer(indexer: me.snwy.oasis.Indexer) {
        TODO("Not yet implemented")
    }

    override fun visitList(list: me.snwy.oasis.OasisList) {
        TODO("Not yet implemented")
    }

    override fun visitNegate(negate: me.snwy.oasis.Negate) {
        TODO("Not yet implemented")
    }

    override fun visitNew(ref: me.snwy.oasis.New) {
        TODO("Not yet implemented")
    }

    override fun visitNot(not: me.snwy.oasis.Not) {
        TODO("Not yet implemented")
    }

    override fun visitLet(let: me.snwy.oasis.Let) {
        TODO("Not yet implemented")
    }

    override fun visitIfStmt(ifstmt: me.snwy.oasis.IfStmt) {
        TODO("Not yet implemented")
    }

    override fun visitWhileStmt(whilestmt: me.snwy.oasis.WhileStmt) {
        TODO("Not yet implemented")
    }

    override fun visitStmtList(stmtlist: me.snwy.oasis.StmtList) {
        TODO("Not yet implemented")
    }

    override fun visitReturnStmt(retstmt: me.snwy.oasis.RetStmt) {
        TODO("Not yet implemented")
    }

    override fun visitExprStmt(exprStmt: me.snwy.oasis.ExprStmt) {
        TODO("Not yet implemented")
    }

    override fun visitIs(is_: me.snwy.oasis.Is) {
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
}