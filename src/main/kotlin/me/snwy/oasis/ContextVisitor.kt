package me.snwy.oasis

import org.eclipse.lsp4j.Diagnostic
import org.eclipse.lsp4j.DiagnosticSeverity
import org.eclipse.lsp4j.Position
import org.eclipse.lsp4j.Range

data class Or<A, B>(val a: A?, val b: B?)

class PrototypeContext(prototype: Proto, visitor: ContextVisitor) {
    val map = mutableMapOf<String, Any>()

    init {
        map.putAll(prototype.accept(visitor).a!!)
    }

    fun get(key: String): Any? = map[key]
}

object StdMap {
    val map = mutableMapOf<String, Any>()

    init {
        map.putAll(
            mapOf(
                "io" to Unit,
                "string" to Unit,
                "time" to Unit,
                "math" to Unit,
                "type" to Unit,
                "list" to Unit,
                "range" to Unit,
                "sys" to Unit,
                "map" to Unit,
                "json" to Unit,
                "prototype" to Unit,
                "async" to Unit,
                "api" to Unit,
                "import" to Unit,
                "func" to Unit,
                "panic" to Unit,
                "plot" to Unit,
            )
        )
    }
}

class ContextVisitor : Expr.Visitor<Or<HashMap<String, Any>, Unit>>, Stmt.Visitor<Unit> {

    var variables = hashMapOf<String, Any>()
    private val immutables = ArrayList<String>()
    private val rels = ArrayList<String>()

    private val prototypes = hashMapOf<String, PrototypeContext>()

    val diagnostics = arrayListOf<Diagnostic>()

    private val NOVALUE = Or<HashMap<String, Any>, Unit>(null, Unit)

    init {
        variables.putAll(StdMap.map)
    }

    override fun visitLiteral(literal: Literal): Or<HashMap<String, Any>, Unit> {
        return Or(null, Unit)
    }

    override fun visitAssignment(assignment: AssignmentExpr): Or<HashMap<String, Any>, Unit> {
        when (assignment.left) {
            is Variable -> {
                if (immutables.contains((assignment.left as Variable).name.lexeme)) {
                    diagnostics.add(
                        Diagnostic(
                            Range(Position((assignment.left as Variable).name.line, (assignment.left as Variable).name.column), Position(
                                (assignment.left as Variable).name.line, (assignment.left as Variable).name.column + (assignment.left as Variable).name.lexeme.length)),
                            "Variable is marked const",
                            DiagnosticSeverity.Error,
                            "oasis"
                        )
                    )
                }
                if (rels.contains((assignment.left as Variable).name.lexeme)) {
                    diagnostics.add(
                        Diagnostic(
                            Range(Position(0, 0), Position(0, 0)),
                            "Can't reassign relative expression",
                            DiagnosticSeverity.Error,
                            "oasis"
                        )
                    )
                }
                assignment.left.accept(this)
                when (assignment.value) {
                    is Proto -> {
                        prototypes[(assignment.left as Variable).name.lexeme] =
                            PrototypeContext(assignment.value as Proto, this)
                    }
                }
            }
            is Property, is Indexer -> {
                // nothing to do, all is good ^v^
            }
            else -> {
                diagnostics.add(
                    Diagnostic(
                        Range(Position(0, 0), Position(0, 0)),
                        "Unsupported assignment",
                        DiagnosticSeverity.Error,
                        "oasis"
                    )
                )
            }
        }
        return Or(null, Unit)
    }

    override fun visitProperty(property: Property): Or<HashMap<String, Any>, Unit> = NOVALUE

    override fun visitFunc(func: Func): Or<HashMap<String, Any>, Unit> {
        func.operands.forEach {
            if (variables.containsKey(it.lexeme)) {
                diagnostics.add(
                    Diagnostic(
                        Range(
                            Position(it.line - 1, it.column - 1),
                            Position(it.line - 1, it.column - 1 + it.lexeme.length)
                        ), "Variable ${it.lexeme} already defined", DiagnosticSeverity.Warning, "oasis"
                    ),
                )
            } else {
                variables[it.lexeme] = object {
                    override fun toString() = "DefinedToken"
                }
            }
        }
        func.body.accept(this)
        func.operands.forEach {
            if (variables[it.lexeme].toString() == "DefinedToken") {
                variables.remove(it.lexeme)
            }
        }
        return NOVALUE
    }

    override fun visitFcall(fcall: FCallExpr): Or<HashMap<String, Any>, Unit> {
        fcall.func.accept(this)
        fcall.operands.forEach {
            it.accept(this)
        }
        return NOVALUE
    }

    override fun visitBinOp(binop: BinOp): Or<HashMap<String, Any>, Unit> {
        binop.left.accept(this)
        binop.right.accept(this)
        return NOVALUE
    }

    override fun visitGroup(group: Group): Or<HashMap<String, Any>, Unit> {
        group.expr.accept(this)
        return NOVALUE
    }

    override fun visitVariable(variable: Variable): Or<HashMap<String, Any>, Unit> {
        if (!variables.containsKey(variable.name.lexeme)) {
            diagnostics.add(
                Diagnostic(
                    Range(
                        Position(variable.line - 1, variable.column - 1),
                        Position(variable.line - 1, variable.column - 1 + variable.name.lexeme.length)
                    ), "Undefined variable ${variable.name.lexeme}", DiagnosticSeverity.Error, "oasis"
                )
            )
        }
        return NOVALUE
    }

    override fun vistPrecomputed(precomputed: Precomputed) = NOVALUE

    override fun visitProto(proto: Proto): Or<HashMap<String, Any>, Unit> {
        val map = hashMapOf<String, Any>()
        proto.body.stmts.forEach {
            when (it) {
                is Let -> {
                    if (it.left.size > 0) {
                        diagnostics.add(
                            Diagnostic(
                                Range(
                                    Position(it.left[0].line - 1, it.left.last().column - 1),
                                    Position(it.left[0].line - 1,
                                        (it.left.last().column - 1) + it.left.last().lexeme.length
                                    )
                                ), "Multiple let definitions not allowed in prototype", DiagnosticSeverity.Error, "oasis"
                            )
                        )
                    }
                    when (it.value) {
                        is Proto -> {
                            map[it.left[0].lexeme] = PrototypeContext(it.value as Proto, this)
                        }
                        else -> {
                            map[it.left[0].lexeme] = it.value
                        }
                    }
                }
                is ExprStmt -> {
                    when (it.expr) {
                        is AssignmentExpr -> {
                            when ((it.expr as AssignmentExpr).left) {
                                is Variable -> {
                                    when ((it.expr as AssignmentExpr).value) {
                                        is Proto -> {
                                            map[((it.expr as AssignmentExpr).left as Variable).name.lexeme] =
                                                PrototypeContext((it.expr as AssignmentExpr).value as Proto, this)
                                        }
                                        else -> {
                                            map[((it.expr as AssignmentExpr).left as Variable).name.lexeme] =
                                                (it.expr as AssignmentExpr).value
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return Or(map, null)
    }

    override fun visitIndexer(indexer: Indexer): Or<HashMap<String, Any>, Unit> {
        indexer.expr.accept(this)
        indexer.index.accept(this)
        return NOVALUE
    }

    override fun visitList(list: OasisList): Or<HashMap<String, Any>, Unit> {
        list.exprs.forEach {
            it.accept(this)
        }
        return NOVALUE
    }

    override fun visitNegate(negate: Negate): Or<HashMap<String, Any>, Unit> {
        negate.value.accept(this)
        return NOVALUE
    }

    override fun visitNew(ref: New): Or<HashMap<String, Any>, Unit> {
        ref.expr.accept(this)
        return NOVALUE
    }

    override fun visitNot(not: Not): Or<HashMap<String, Any>, Unit> {
        not.expr.accept(this)
        return NOVALUE
    }

    override fun visitListComprehension(listComprehension: ListComprehension): Or<HashMap<String, Any>, Unit> {
        listComprehension.expr.accept(this)
        listComprehension.inVal.accept(this)
        return NOVALUE
    }

    override fun visitMapLiteral(mapLiteral: MapLiteral): Or<HashMap<String, Any>, Unit> {
        mapLiteral.exprs.forEach {
            it.first.accept(this)
            it.second.accept(this)
        }
        return NOVALUE
    }

    override fun visitIfExpression(ifExpression: IfExpression): Or<HashMap<String, Any>, Unit> {
        ifExpression.expr.accept(this)
        ifExpression.elseExpr.accept(this)
        return NOVALUE
    }

    override fun visitTuple(tuple: Tuple): Or<HashMap<String, Any>, Unit> {
        tuple.exprs.forEach {
            it.accept(this)
        }
        return NOVALUE
    }

    override fun visitLet(let: Let) {
        let.left.forEach {
            if (it.lexeme in variables) {
                diagnostics.add(
                    Diagnostic(
                        Range(
                            Position(let.line - 1, let.column - 1),
                            Position(let.value.line - 1, it.column + it.lexeme.length - 1)
                        ), "Variable already defined", DiagnosticSeverity.Error, "oasis"
                    )
                )
        }

        }
        if (let.immutable) {
            let.left.forEach {
                immutables.add(it.lexeme)
            }
        }
        let.left.forEach {
            variables[it.lexeme] = if (let.value is Proto) {
                PrototypeContext(let.value as Proto, this)
            } else {
                let.value
            }
        }
    }

    override fun visitIfStmt(ifstmt: IfStmt) {
        ifstmt.stmtlist.accept(this)
        ifstmt.elseBody?.accept(this)
        ifstmt.expr.accept(this)
    }

    override fun visitWhileStmt(whilestmt: WhileStmt) {
        whilestmt.body.accept(this)
        whilestmt.expr.accept(this)
    }

    override fun visitStmtList(stmtlist: StmtList) {
        val oldVariables = variables
        variables = hashMapOf()
        variables.putAll(oldVariables)
        stmtlist.stmts.forEach {
            it.accept(this)
        }
        variables = oldVariables
    }

    override fun visitReturnStmt(retstmt: RetStmt) {
        retstmt.expr?.accept(this)
    }

    override fun visitExprStmt(exprStmt: ExprStmt) {
        exprStmt.expr.accept(this)
    }

    override fun visitIs(is_: Is) {
        is_.expr.accept(this)
        is_.cases.accept(this)
        is_.else_?.accept(this)
    }

    override fun visitTest(test: Test) {
        if (variables.containsKey(test.errorVar.lexeme)) {
            diagnostics.add(
                Diagnostic(
                    Range(
                        Position(test.errorVar.line - 1, test.errorVar.column - 1),
                        Position(test.errorVar.line - 1, test.errorVar.column - 1 + test.errorVar.lexeme.length)
                    ), "Variable already defined", DiagnosticSeverity.Warning, "oasis"
                )
            )
        } else {
            variables[test.errorVar.lexeme] = object {
                override fun toString() = "DefinedToken"
            }
        }
        test.block.accept(this)
        test.errorBlock.accept(this)
        if (variables.containsKey(test.errorVar.lexeme)) {
            if (variables[test.errorVar.lexeme].toString() == "DefinedToken") {
                variables.remove(test.errorVar.lexeme)
            }
        }
    }

    override fun visitForLoopTriad(forLoopTriad: ForLoopTriad) {
        forLoopTriad.init.accept(this)
        forLoopTriad.cond.accept(this)
        forLoopTriad.step.accept(this)
        forLoopTriad.body.accept(this)
    }

    override fun visitForLoopIterator(forLoopIterator: ForLoopIterator) {
        if ((forLoopIterator.varName as Variable).name.lexeme in variables) {
            diagnostics.add(
                Diagnostic(
                    Range(
                        Position(forLoopIterator.line - 1, forLoopIterator.column - 1),
                        Position(
                            forLoopIterator.line - 1,
                            (forLoopIterator.varName as Variable).column - 1 + (forLoopIterator.varName as Variable).name.lexeme.length
                        )
                    ), "Variable already defined", DiagnosticSeverity.Warning, "oasis"
                )
            )
        } else {
            variables[(forLoopIterator.varName as Variable).name.lexeme] = object {
                override fun toString() = "DefinedToken"
            }
        }
        forLoopIterator.body.accept(this)
        if (variables[(forLoopIterator.varName as Variable).name.lexeme].toString() == "DefinedToken") {
            variables.remove((forLoopIterator.varName as Variable).name.lexeme)
        }
    }

    override fun visitBreakStmt(break_: BreakStmt) {
        // nothing
    }

    override fun visitContinueStmt(continue_: ContinueStmt) {
        // nothing
    }

    override fun visitRelStmt(relstmt: RelStmt) {
        if (relstmt.name.lexeme in variables) {
            diagnostics.add(
                Diagnostic(
                    Range(
                        Position(relstmt.line - 1, relstmt.column - 1),
                        Position(relstmt.line - 1, relstmt.name.column - 1 + relstmt.name.lexeme.length)
                    ), "Relative expression can't share name with variable", DiagnosticSeverity.Error, "oasis"
                )
            )
        } else {
            variables[relstmt.name.lexeme] = relstmt.expr
            rels.add(relstmt.name.lexeme)
        }
    }

    override fun visitDoBlock(doblock: DoBlock) {
        doblock.body.accept(this)
    }

}