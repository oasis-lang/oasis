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
    }

    abstract fun <T> accept(visitor: Visitor<T>): T
}

class Precomputed(val hash: Int, line: Int, column: Int) : Expr(line, column) {
    override fun <T> accept(visitor: Visitor<T>): T = visitor.vistPrecomputed(this)
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
    }

    abstract fun <T> accept(visitor: Visitor<T>): T
}

class ExprStmt(var expr: Expr, line: Int, column: Int) : Stmt(line, column) {
    override fun <T> accept(visitor: Visitor<T>): T {
        return visitor.visitExprStmt(this)
    }

    override fun toString(): String {
        return "ExprStmt($expr) at $line"
    }
}

class BinOp(var left: Expr, val operator: Token, var right: Expr, line: Int, column: Int) : Expr(line, column) {
    override fun <T> accept(visitor: Visitor<T>): T {
        return visitor.visitBinOp(this)
    }

    override fun toString(): String {
        return "BinOp($left ${operator.lexeme} $right) at $line"
    }
}

class Literal(val value: Any?, line: Int, column: Int) : Expr(line, column) {
    override fun <T> accept(visitor: Visitor<T>): T {
        return visitor.visitLiteral(this)
    }

    override fun toString(): String {
        return "Literal($value) at $line"
    }
}

class AssignmentExpr(var left: Expr, var value: Expr, line: Int, column: Int) : Expr(line, column) {
    override fun <T> accept(visitor: Visitor<T>): T {
        return visitor.visitAssignment(this)
    }

    override fun toString(): String {
        return "Assign($left = $value) at $line"
    }
}

class Let(val left: Token, var value: Expr, line: Int, column: Int, val immutable: Boolean = false) :
    Stmt(line, column) {
    override fun <T> accept(visitor: Visitor<T>): T {
        return visitor.visitLet(this)
    }

    override fun toString(): String {
        return "Let(${left.lexeme} = $value) at $line"
    }
}

class StmtList(var stmts: List<Stmt>, line: Int, column: Int) : Stmt(line, column) {
    override fun <T> accept(visitor: Visitor<T>): T {
        return visitor.visitStmtList(this)
    }

    override fun toString(): String {
        return "StmtList($stmts) at $line"
    }
}

class Property(var obj: Expr, val indexer: Token, line: Int, column: Int) : Expr(line, column) {
    override fun <T> accept(visitor: Visitor<T>): T {
        return visitor.visitProperty(this)
    }

    override fun toString(): String {
        return "Property($obj : ${indexer.lexeme}) at $line"
    }
}

class Func(val operands: List<Token>, var body: StmtList, line: Int, column: Int) : Expr(line, column) {
    override fun <T> accept(visitor: Visitor<T>): T {
        return visitor.visitFunc(this)
    }

    override fun toString(): String {
        return "Func(${operands} : $body) at $line"
    }
}

class FCallExpr(var func: Expr, var operands: List<Expr>, line: Int, column: Int, var splat: Boolean = false) :
    Expr(line, column) {
    override fun <T> accept(visitor: Visitor<T>): T {
        return visitor.visitFcall(this)
    }

    override fun toString(): String {
        return "Call(${func} : ${operands}) at $line"
    }
}

class Group(var expr: Expr, line: Int, column: Int) : Expr(line, column) {
    override fun <T> accept(visitor: Visitor<T>): T {
        return visitor.visitGroup(this)
    }

    override fun toString(): String {
        return "Group($expr) at $line"
    }
}

class IfStmt(var expr: Expr, var stmtlist: StmtList, var elseBody: StmtList?, line: Int, column: Int) :
    Stmt(line, column) {
    override fun <T> accept(visitor: Visitor<T>): T {
        return visitor.visitIfStmt(this)
    }
}

class WhileStmt(var expr: Expr, var body: StmtList, line: Int, column: Int) : Stmt(line, column) {
    override fun <T> accept(visitor: Visitor<T>): T {
        return visitor.visitWhileStmt(this)
    }
}

class Variable(val name: Token, line: Int, column: Int) : Expr(line, column) {
    override fun <T> accept(visitor: Visitor<T>): T {
        return visitor.visitVariable(this)
    }

    override fun toString(): String {
        return "Variable($name) at $line"
    }
}

class Proto(var base: Expr?, val body: StmtList, line: Int, column: Int) : Expr(line, column) {
    override fun <T> accept(visitor: Visitor<T>): T {
        return visitor.visitProto(this)
    }

    override fun toString(): String {
        return "Variable($base : $body) at $line"
    }
}

class RetStmt(var expr: Expr?, line: Int, column: Int) : Stmt(line, column) {
    override fun <T> accept(visitor: Visitor<T>): T {
        return visitor.visitReturnStmt(this)
    }

    override fun toString(): String {
        return "Return($expr) at $line"
    }
}

class Indexer(var expr: Expr, var index: Expr, line: Int, column: Int) : Expr(line, column) {
    override fun <T> accept(visitor: Visitor<T>): T {
        return visitor.visitIndexer(this)
    }

    override fun toString(): String {
        return "Index($expr : $index) at $line"
    }
}

class OasisList(var exprs: ArrayList<Expr>, line: Int, column: Int) : Expr(line, column) {
    override fun <T> accept(visitor: Visitor<T>): T {
        return visitor.visitList(this)
    }
}

class Negate(var value: Expr, line: Int, column: Int) : Expr(line, column) {
    override fun <T> accept(visitor: Visitor<T>): T {
        return visitor.visitNegate(this)
    }
}

class New(var expr: Expr, line: Int, column: Int) : Expr(line, column) {
    override fun <T> accept(visitor: Visitor<T>): T {
        return visitor.visitNew(this)
    }

    override fun toString(): String {
        return "New(${expr}) at $line"
    }
}

class Not(var expr: Expr, line: Int, column: Int) : Expr(line, column) {
    override fun <T> accept(visitor: Visitor<T>): T {
        return visitor.visitNot(this)
    }

    override fun toString(): String {
        return "Not(${expr}) at $line"
    }
}

class MapLiteral(var exprs: ArrayList<Pair<Expr, Expr>>, line: Int, column: Int) : Expr(line, column) {
    override fun <T> accept(visitor: Visitor<T>): T {
        return visitor.visitMapLiteral(this)
    }
}

class ListComprehension(var expr: Expr, var inVal: Expr, line: Int, column: Int) : Expr(line, column) {
    override fun <T> accept(visitor: Visitor<T>): T {
        return visitor.visitListComprehension(this)
    }
}

class IfExpression(var expr: Expr, var thenExpr: Expr, var elseExpr: Expr, line: Int, column: Int) :
    Expr(line, column) {
    override fun <T> accept(visitor: Visitor<T>): T {
        return visitor.visitIfExpression(this)
    }
}

class Is(var expr: Expr, val cases: StmtList, val else_: StmtList?, line: Int, column: Int) : Stmt(line, column) {
    override fun <T> accept(visitor: Visitor<T>): T {
        return visitor.visitIs(this)
    }
}

class Test(var block: StmtList, var errorBlock: StmtList, var errorVar: Token, line: Int, column: Int) :
    Stmt(line, column) {
    override fun <T> accept(visitor: Visitor<T>): T {
        return visitor.visitTest(this)
    }
}

class ForLoopTriad(var init: Stmt, var cond: Expr, var step: Stmt, var body: StmtList, line: Int, column: Int) :
    Stmt(line, column) {
    override fun <T> accept(visitor: Visitor<T>): T {
        return visitor.visitForLoopTriad(this)
    }
}

class ForLoopIterator(var varName: Expr, var iterable: Expr, var body: StmtList, line: Int, column: Int) :
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

class RelStmt(var name: Token, var expr: Expr, line: Int, column: Int) : Stmt(line, column) {
    override fun <T> accept(visitor: Visitor<T>): T {
        return visitor.visitRelStmt(this)
    }
}