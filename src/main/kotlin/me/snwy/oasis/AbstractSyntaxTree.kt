package me.snwy.oasis

abstract class Expr(var line: Int, var column: Int) {
    interface Visitor<T> {
        fun visitLiteral(literal: Literal): T
        fun visitAssignment(assignment: AssignmentExpr): T
        fun visitProperty(property: Property): T
        fun visitFunc(func: Func): T
        fun visitFcall(fcall: FCallExpr): T
        fun visitBinOp(binop: BinOp): T
        fun visitGroup(group: Group): T
        fun visitVariable(variable: Variable): T
        fun vistPrecomputed(precomputed: Precomputed): T
        fun visitProto(proto: Proto): T
        fun visitIndexer(indexer: Indexer): T
        fun visitList(list: OasisList): T
        fun visitNegate(negate: Negate): T
        fun visitNew(ref: New): T
        fun visitNot(not: Not): T
        fun visitListComprehension(listComprehension: ListComprehension): T
        fun visitMapLiteral(mapLiteral: MapLiteral): T
        fun visitIfExpression(ifExpression: IfExpression): T
        fun visitTuple(tuple: Tuple): T
    }

    abstract fun <T> accept(visitor: Visitor<T>): T
}

class Precomputed(@JvmField val hash: Int, line: Int, column: Int) : Expr(line, column) {
    override fun <T> accept(visitor: Visitor<T>): T = visitor.vistPrecomputed(this)
    override fun toString(): String {
        return "Precomputed($hash)"
    }
}

abstract class Stmt(var line: Int, var column: Int) {
    interface Visitor<T> {
        fun visitLet(let: Let): T
        fun visitIfStmt(ifstmt: IfStmt): T
        fun visitWhileStmt(whilestmt: WhileStmt): T
        fun visitStmtList(stmtlist: StmtList): T
        fun visitReturnStmt(retstmt: RetStmt): T
        fun visitExprStmt(exprStmt: ExprStmt): T
        fun visitIs(is_: Is): T
        fun visitTest(test: Test): T
        fun visitForLoopTriad(forLoopTriad: ForLoopTriad): T
        fun visitForLoopIterator(forLoopIterator: ForLoopIterator): T
        fun visitBreakStmt(break_: BreakStmt): T
        fun visitContinueStmt(continue_: ContinueStmt): T
        fun visitRelStmt(relstmt: RelStmt): T
        fun visitDoBlock(doblock: DoBlock): T
    }

    abstract fun <T> accept(visitor: Visitor<T>): T
}

class ExprStmt(@JvmField var expr: Expr, line: Int, column: Int) : Stmt(line, column) {
    override fun <T> accept(visitor: Visitor<T>): T {
        return visitor.visitExprStmt(this)
    }

    override fun toString(): String {
        return "ExprStmt($expr) at $line"
    }
}

class BinOp(@JvmField var left: Expr, @JvmField val operator: Token, @JvmField var right: Expr, line: Int, column: Int) : Expr(line, column) {
    override fun <T> accept(visitor: Visitor<T>): T {
        return visitor.visitBinOp(this)
    }

    override fun toString(): String {
        return "BinOp($left ${operator.lexeme} $right) at $line"
    }
}

class Literal(@JvmField val value: Any?, line: Int, column: Int) : Expr(line, column) {
    override fun <T> accept(visitor: Visitor<T>): T {
        return visitor.visitLiteral(this)
    }

    override fun toString(): String {
        return "Literal($value) at $line"
    }
}

class AssignmentExpr(@JvmField var left: Expr, @JvmField var value: Expr, line: Int, column: Int) : Expr(line, column) {
    override fun <T> accept(visitor: Visitor<T>): T {
        return visitor.visitAssignment(this)
    }

    override fun toString(): String {
        return "Assign($left = $value) at $line"
    }
}

class Let(@JvmField val left: ArrayList<Token>, @JvmField var value: Expr, line: Int, column: Int, @JvmField val immutable: Boolean = false) :
    Stmt(line, column) {
    override fun <T> accept(visitor: Visitor<T>): T {
        return visitor.visitLet(this)
    }

    override fun toString(): String {
        return "Let(${left.joinToString(", ") { it.lexeme }} = $value) at $line"
    }
}

class StmtList(@JvmField var stmts: List<Stmt>, line: Int, column: Int) : Stmt(line, column) {
    override fun <T> accept(visitor: Visitor<T>): T {
        return visitor.visitStmtList(this)
    }

    override fun toString(): String {
        return "StmtList($stmts) at $line"
    }
}

class Property(@JvmField var obj: Expr, @JvmField val indexer: Token, line: Int, column: Int) : Expr(line, column) {
    override fun <T> accept(visitor: Visitor<T>): T {
        return visitor.visitProperty(this)
    }

    override fun toString(): String {
        return "Property($obj : ${indexer.lexeme}) at $line"
    }
}

class Func(@JvmField val operands: List<Token>, @JvmField var body: StmtList, line: Int, column: Int) : Expr(line, column) {
    override fun <T> accept(visitor: Visitor<T>): T {
        return visitor.visitFunc(this)
    }

    override fun toString(): String {
        return "Func(${operands} : $body) at $line"
    }
}

class FCallExpr(@JvmField var func: Expr, @JvmField var operands: ArrayList<Expr>, line: Int, column: Int, @JvmField var splat: Boolean = false) :
    Expr(line, column) {
    override fun <T> accept(visitor: Visitor<T>): T {
        return visitor.visitFcall(this)
    }

    override fun toString(): String {
        return "Call(${func} : ${operands}) at $line"
    }
}

class Group(@JvmField var expr: Expr, line: Int, column: Int) : Expr(line, column) {
    override fun <T> accept(visitor: Visitor<T>): T {
        return visitor.visitGroup(this)
    }

    override fun toString(): String {
        return "Group($expr) at $line"
    }
}

class IfStmt(@JvmField var expr: Expr, @JvmField var stmtlist: StmtList, @JvmField var elseBody: StmtList?, line: Int, column: Int) :
    Stmt(line, column) {
    override fun <T> accept(visitor: Visitor<T>): T {
        return visitor.visitIfStmt(this)
    }
}

class WhileStmt(@JvmField var expr: Expr, @JvmField var body: StmtList, line: Int, column: Int) : Stmt(line, column) {
    override fun <T> accept(visitor: Visitor<T>): T {
        return visitor.visitWhileStmt(this)
    }
}

class Variable(@JvmField val name: Token, line: Int, column: Int) : Expr(line, column) {
    override fun <T> accept(visitor: Visitor<T>): T {
        return visitor.visitVariable(this)
    }

    override fun toString(): String {
        return "Variable($name) at $line"
    }
}

class Proto(@JvmField var base: Expr?, @JvmField val body: StmtList, line: Int, column: Int) : Expr(line, column) {
    override fun <T> accept(visitor: Visitor<T>): T {
        return visitor.visitProto(this)
    }

    override fun toString(): String {
        return "Variable($base : $body) at $line"
    }
}

class RetStmt(@JvmField var expr: Expr?, line: Int, column: Int) : Stmt(line, column) {
    override fun <T> accept(visitor: Visitor<T>): T {
        return visitor.visitReturnStmt(this)
    }

    override fun toString(): String {
        return "Return($expr) at $line"
    }
}

class Indexer(@JvmField var expr: Expr, @JvmField var index: Expr, line: Int, column: Int) : Expr(line, column) {
    override fun <T> accept(visitor: Visitor<T>): T {
        return visitor.visitIndexer(this)
    }

    override fun toString(): String {
        return "Index($expr : $index) at $line"
    }
}

class OasisList(@JvmField var exprs: ArrayList<Expr>, line: Int, column: Int) : Expr(line, column) {
    override fun <T> accept(visitor: Visitor<T>): T {
        return visitor.visitList(this)
    }
}

class Negate(@JvmField var value: Expr, line: Int, column: Int) : Expr(line, column) {
    override fun <T> accept(visitor: Visitor<T>): T {
        return visitor.visitNegate(this)
    }
}

class New(@JvmField var expr: Expr, line: Int, column: Int) : Expr(line, column) {
    override fun <T> accept(visitor: Visitor<T>): T {
        return visitor.visitNew(this)
    }

    override fun toString(): String {
        return "New(${expr}) at $line"
    }
}

class Not(@JvmField var expr: Expr, line: Int, column: Int) : Expr(line, column) {
    override fun <T> accept(visitor: Visitor<T>): T {
        return visitor.visitNot(this)
    }

    override fun toString(): String {
        return "Not(${expr}) at $line"
    }
}

class MapLiteral(@JvmField var exprs: ArrayList<Pair<Expr, Expr>>, line: Int, column: Int) : Expr(line, column) {
    override fun <T> accept(visitor: Visitor<T>): T {
        return visitor.visitMapLiteral(this)
    }
}

class ListComprehension(@JvmField var expr: Expr, @JvmField var inVal: Expr, line: Int, column: Int) : Expr(line, column) {
    override fun <T> accept(visitor: Visitor<T>): T {
        return visitor.visitListComprehension(this)
    }
}

class IfExpression(@JvmField var expr: Expr, @JvmField var thenExpr: Expr, @JvmField var elseExpr: Expr, line: Int, column: Int) :
    Expr(line, column) {
    override fun <T> accept(visitor: Visitor<T>): T {
        return visitor.visitIfExpression(this)
    }
}

class Tuple(@JvmField var exprs: ArrayList<Expr>, line: Int, column: Int) : Expr(line, column) {
    override fun <T> accept(visitor: Visitor<T>): T {
        return visitor.visitTuple(this)
    }
}

class Is(@JvmField var expr: Expr, @JvmField val cases: StmtList, @JvmField val else_: StmtList?, line: Int, column: Int) : Stmt(line, column) {
    override fun <T> accept(visitor: Visitor<T>): T {
        return visitor.visitIs(this)
    }
}

class Test(@JvmField var block: StmtList, @JvmField var errorBlock: StmtList, @JvmField var errorVar: Token, line: Int, column: Int) :
    Stmt(line, column) {
    override fun <T> accept(visitor: Visitor<T>): T {
        return visitor.visitTest(this)
    }
}

class ForLoopTriad(@JvmField var init: Stmt, @JvmField var cond: Expr, @JvmField var step: Stmt, @JvmField var body: StmtList, line: Int, column: Int) :
    Stmt(line, column) {
    override fun <T> accept(visitor: Visitor<T>): T {
        return visitor.visitForLoopTriad(this)
    }
}

class ForLoopIterator(@JvmField var varName: Expr, @JvmField var iterable: Expr, @JvmField var body: StmtList, line: Int, column: Int) :
    Stmt(line, column) {
    override fun <T> accept(visitor: Visitor<T>): T {
        return visitor.visitForLoopIterator(this)
    }
}

class BreakStmt(line: Int, column: Int) : Stmt(line, column) {
    override fun <T> accept(visitor: Visitor<T>): T {
        return visitor.visitBreakStmt(this)
    }
}

class ContinueStmt(line: Int, column: Int) : Stmt(line, column) {
    override fun <T> accept(visitor: Visitor<T>): T {
        return visitor.visitContinueStmt(this)
    }
}

class RelStmt(@JvmField var name: Token, @JvmField var expr: Expr, line: Int, column: Int) : Stmt(line, column) {
    override fun <T> accept(visitor: Visitor<T>): T {
        return visitor.visitRelStmt(this)
    }
}

class DoBlock(@JvmField var body: StmtList, line: Int, column: Int) : Stmt(line, column) {
    override fun <T> accept(visitor: Visitor<T>): T {
        return visitor.visitDoBlock(this)
    }
}