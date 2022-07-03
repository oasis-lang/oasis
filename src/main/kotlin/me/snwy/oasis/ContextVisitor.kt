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

    val variables = hashMapOf<String, Any>()
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
                            Range(Position(0, 0), Position(0, 0)),
                            "Variable is marked immutable",
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

    override fun visitFcall(fcall: FCallExpr) = NOVALUE

    override fun visitBinOp(binop: BinOp) = NOVALUE

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
                    when (it.value) {
                        is Proto -> {
                            map[it.left.lexeme] = PrototypeContext(it.value as Proto, this)
                        }
                        else -> {
                            map[it.left.lexeme] = it.value
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

    override fun visitIndexer(indexer: Indexer) = NOVALUE

    override fun visitList(list: OasisList) = NOVALUE

    override fun visitNegate(negate: Negate) = NOVALUE

    override fun visitNew(ref: New) = NOVALUE

    override fun visitNot(not: Not) = NOVALUE

    override fun visitListComprehension(listComprehension: ListComprehension) = NOVALUE

    override fun visitMapLiteral(mapLiteral: MapLiteral) = NOVALUE

    override fun visitIfExpression(ifExpression: IfExpression): Or<HashMap<String, Any>, Unit> {
        ifExpression.expr.accept(this)
        ifExpression.elseExpr.accept(this)
        return NOVALUE
    }

    override fun visitLet(let: Let) {
        if (let.left.lexeme in variables) {
            diagnostics.add(
                Diagnostic(
                    Range(
                        Position(let.line - 1, let.column - 1),
                        Position(let.value.line - 1, let.left.column + let.left.lexeme.length - 1)
                    ), "Variable already defined", DiagnosticSeverity.Error, "oasis"
                )
            )
        }
        if (let.immutable) {
            immutables.add(let.left.lexeme)
        }
        variables[let.left.lexeme] = if (let.value is Proto) {
            PrototypeContext(let.value as Proto, this)
        } else {
            let.value
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
        stmtlist.stmts.forEach {
            it.accept(this)
        }
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
        test.block.accept(this)
        test.errorBlock.accept(this)
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

}