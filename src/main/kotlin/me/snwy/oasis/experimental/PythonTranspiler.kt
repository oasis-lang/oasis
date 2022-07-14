package me.snwy.oasis.experimental

import me.snwy.oasis.*

class PythonTranspiler : Expr.Visitor<String>, Stmt.Visitor<String> {

    fun transpile(stmt: Stmt): String {
        val code = stmt.accept(this)
        return globalStatements.joinToString("\n") + "\n" + code
    }


    private val globalStatements = mutableListOf<String>()
    private var indentLevel = 0

    private var inProto = false

    private fun indent(subtract: Boolean = false) =
        if (indentLevel == 0) "" else "\t".repeat(if (subtract) indentLevel - 1 else indentLevel)

    override fun visitLiteral(literal: Literal): String {
        when (literal.value) {
            is Int -> return literal.value.toString()
            is Double -> return if (literal.value.toString().contains("."))
                literal.value.toString().replace(Regex("0*\$"), "").replace(Regex("\\.\$"), "")
            else literal.value.toString()
            is String -> return "\"${literal.value}\""
            is Boolean -> {
                return if (literal.value) {
                    "True"
                } else {
                    "False"
                }
            }
            null -> return "None"
            else -> throw RuntimeException("Unsupported literal")
        }
    }

    override fun visitAssignment(assignment: AssignmentExpr): String {
        return "${assignment.left.accept(this)} = ${assignment.value.accept(this)}"
    }

    override fun visitProperty(property: Property): String {
        return "${property.obj.accept(this)}.${property.indexer.lexeme}"
    }

    override fun visitFunc(func: Func): String {
        val tempIndent = indentLevel
        indentLevel = 1
        globalStatements.add("def f_${
            func.hashCode().toUInt()
        }(${if (inProto) "self, " else ""}${func.operands.joinToString(", ") { it.lexeme }}):\n"
                + (func.body.stmts.map { "${indent()}${it.accept(this)}\n" }).joinToString("")
        )
        indentLevel = tempIndent
        return "f_${func.hashCode().toUInt()}"
    }

    override fun visitFcall(fcall: FCallExpr): String {
        return "${fcall.func.accept(this)}(${fcall.operands.joinToString(", ") { it.accept(this) }})"
    }

    override fun visitBinOp(binop: BinOp): String {
        return "${binop.left.accept(this)} ${binop.operator.lexeme} ${binop.right.accept(this)}"
    }

    override fun visitGroup(group: Group): String {
        return "(${group.expr.accept(this)})"
    }

    override fun visitVariable(variable: Variable): String {
        return if (variable.name.lexeme == "import") "__import__" else variable.name.lexeme
    }

    override fun vistPrecomputed(precomputed: Precomputed): String {
        throw RuntimeException("Unsupported precomputed")
    }

    override fun visitProto(proto: Proto): String {
        inProto = true
        var proto_s = "type('proto_${proto.hashCode()}', (${proto.base?.accept(this) ?: "object"},), {"
        proto.body.stmts.forEach {
            proto_s += "\"${(it as Let).left[0].lexeme}\": ${it.value.accept(this)}, "
        }
        proto_s += "})"
        inProto = false
        return proto_s
    }

    override fun visitIndexer(indexer: Indexer): String {
        return "${indexer.expr.accept(this)}[${indexer.index.accept(this)}]"
    }

    override fun visitList(list: OasisList): String {
        return "[${list.exprs.joinToString(", ") { it.accept(this) }}]"
    }

    override fun visitNegate(negate: Negate): String {
        return "-${negate.value.accept(this)}"
    }

    override fun visitNew(ref: New): String {
        return "__import__(\"copy\").deepcopy(${ref.expr.accept(this)})"
    }

    override fun visitNot(not: Not): String {
        return "not ${not.expr.accept(this)}"
    }

    override fun visitLet(let: Let): String {
        if (let.immutable)
            println("Warning: Immutable values aren't supported in Python translation")
        return "${let.left[0].lexeme} = ${let.value.accept(this)}"
    }

    override fun visitIfStmt(ifstmt: IfStmt): String {
        indentLevel++
        var ifstmt_s =
            "if ${ifstmt.expr.accept(this)}:\n" + (ifstmt.stmtlist.stmts.map { "${indent()}${it.accept(this)}\n" }).joinToString(
                ""
            )
        if (ifstmt.elseBody != null) {
            ifstmt_s += "${indent(true)}else:\n" + (ifstmt.elseBody!!.stmts.map { "${indent()}${it.accept(this)}\n" }).joinToString(
                ""
            )
        }
        indentLevel--
        return ifstmt_s
    }

    override fun visitWhileStmt(whilestmt: WhileStmt): String {
        indentLevel++
        val while_c =
            "while ${whilestmt.expr.accept(this)}:\n" + (whilestmt.body.stmts.map { "${indent()}${it.accept(this)}\n" }).joinToString(
                ""
            )
        indentLevel--
        return while_c
    }

    override fun visitStmtList(stmtlist: StmtList): String {
        return stmtlist.stmts.joinToString("\n") { it.accept(this) }
    }

    override fun visitReturnStmt(retstmt: RetStmt): String {
        return "return ${retstmt.expr?.accept(this)}"
    }

    override fun visitExprStmt(exprStmt: ExprStmt): String {
        return exprStmt.expr.accept(this)
    }

    override fun visitIs(is_: Is): String {
        TODO("Fix this shit")
    }

    override fun visitTest(test: Test): String {
        TODO("exception handling")
    }

    override fun visitForLoopTriad(forLoopTriad: ForLoopTriad): String {
        TODO("Not yet implemented")
    }

    override fun visitForLoopIterator(forLoopIterator: ForLoopIterator): String {
        TODO("Not yet implemented")
    }

    override fun visitBreakStmt(break_: BreakStmt): String {
        TODO("Not yet implemented")
    }

    override fun visitContinueStmt(continue_: ContinueStmt): String {
        TODO("Not yet implemented")
    }

    override fun visitListComprehension(listComprehension: ListComprehension): String {
        TODO("Not yet implemented")
    }

    override fun visitMapLiteral(mapLiteral: MapLiteral): String {
        TODO("Not yet implemented")
    }

    override fun visitIfExpression(ifExpression: IfExpression): String {
        TODO("Not yet implemented")
    }

    override fun visitTuple(tuple: Tuple): String {
        TODO("Not yet implemented")
    }

    override fun visitRelStmt(relstmt: RelStmt): String {
        TODO("Not yet implemented")
    }

    override fun visitDoBlock(doblock: DoBlock): String {
        TODO("Not yet implemented")
    }
}