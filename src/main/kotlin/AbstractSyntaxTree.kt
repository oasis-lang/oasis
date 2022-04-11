abstract class Expr(var line: Int) {
    interface Visitor<T> {
        fun visitLiteral(literal: Literal): T
        fun visitAssignment(assignment: AssignmentExpr): T
        fun visitProperty(property: Property): T
        fun visitFunc(func: Func): T
        fun visitFcall(fcall: FCallExpr): T
        fun visitBinOp(binop: BinOp): T
        fun visitGroup(group: Group): T
        fun visitVariable(variable: Variable): T
        fun visitProto(proto: Proto): T
        fun visitIndexer(indexer: Indexer): T
        fun visitList(list: OasisList): T
        fun visitNegate(negate: Negate): T
        fun visitNew(ref: New): T
        fun visitNot(not: Not): T
    }
    abstract fun <T> accept(visitor: Visitor<T>): T
}

abstract class Stmt(var line: Int) {
    interface Visitor<T> {
        fun visitLet(let: Let): T
        fun visitIfStmt(ifstmt: IfStmt): T
        fun visitWhileStmt(whilestmt: WhileStmt): T
        fun visitStmtList(stmtlist: StmtList): T
        fun visitReturnStmt(retstmt: RetStmt): T
        fun visitExprStmt(exprStmt: ExprStmt): T
        fun visitIs(is_: Is): T
    }
    abstract fun <T> accept(visitor: Visitor<T>): T
}

class ExprStmt(val expr: Expr, line: Int) : Stmt(line) {
    override fun <T> accept(visitor: Visitor<T>): T {
        return visitor.visitExprStmt(this)
    }

    override fun toString(): String {
        return "ExprStmt($expr) at $line"
    }
}

class BinOp(val left: Expr, val operator: Token, val right: Expr, line: Int) : Expr(line) {
    override fun <T> accept(visitor: Visitor<T>): T {
        return visitor.visitBinOp(this)
    }
    override fun toString(): String {
        return "BinOp($left ${operator.lexeme} $right) at $line"
    }
}

class Literal(val value: Any?, line: Int) : Expr(line) {
    override fun <T> accept(visitor: Visitor<T>): T {
        return visitor.visitLiteral(this)
    }
    override fun toString(): String {
        return "Literal($value) at $line"
    }
}

class AssignmentExpr(val left: Expr, val value: Expr, line: Int) : Expr(line) {
    override fun <T> accept(visitor: Visitor<T>): T {
        return visitor.visitAssignment(this)
    }
    override fun toString(): String {
        return "Assign($left = $value) at $line"
    }
}

class Let(val left: Token, val value: Expr, line: Int) : Stmt(line) {
    override fun <T> accept(visitor: Visitor<T>): T {
        return visitor.visitLet(this)
    }

    override fun toString(): String {
        return "Let(${left.lexeme} = $value) at $line"
    }
}

class StmtList(val stmts: List<Stmt>, line: Int) : Stmt(line) {
    override fun <T> accept(visitor: Visitor<T>): T {
        return visitor.visitStmtList(this)
    }
    override fun toString(): String {
        return "StmtList($stmts) at $line"
    }
}

class Property(val obj: Expr, val indexer: Token, line: Int) : Expr(line){
    override fun <T> accept(visitor: Visitor<T>): T {
        return visitor.visitProperty(this)
    }
    override fun toString(): String {
        return "Property($obj : ${indexer.lexeme}) at $line"
    }
}

class Func(val operands: List<Token>, val body: StmtList, line: Int): Expr(line) {
    override fun <T> accept(visitor: Visitor<T>): T {
        return visitor.visitFunc(this)
    }
    override fun toString(): String {
        return "Func(${operands} : $body) at $line"
    }
}

class FCallExpr(val func: Expr, val operands: List<Expr>, line: Int) : Expr(line) {
    override fun <T> accept(visitor: Visitor<T>): T {
        return visitor.visitFcall(this)
    }
    override fun toString(): String {
        return "Call(${func} : ${operands}) at $line"
    }
}

class Group(val expr: Expr, line: Int) : Expr(line) {
    override fun <T> accept(visitor: Visitor<T>): T {
        return visitor.visitGroup(this)
    }
    override fun toString(): String {
        return "Group($expr) at $line"
    }
}

class IfStmt(val expr: Expr, val stmtlist: StmtList, val elseBody: StmtList?, line: Int) : Stmt(line) {
    override fun <T> accept(visitor: Visitor<T>): T {
        return visitor.visitIfStmt(this)
    }
}

class WhileStmt(val expr: Expr, val body: StmtList, line: Int) : Stmt(line) {
    override fun <T> accept(visitor: Visitor<T>): T {
        return visitor.visitWhileStmt(this)
    }
}

class Variable(val name: Token, line: Int) : Expr(line) {
    override fun <T> accept(visitor: Visitor<T>): T {
        return visitor.visitVariable(this)
    }
    override fun toString(): String {
        return "Variable($name) at $line"
    }
}

class Proto(val base: Token?, val body: StmtList, line: Int): Expr(line) {
    override fun <T> accept(visitor: Visitor<T>): T {
        return visitor.visitProto(this)
    }
    override fun toString(): String {
        return "Variable($base : $body) at $line"
    }
}

class RetStmt(val expr: Expr?, line: Int): Stmt(line) {
    override fun <T> accept(visitor: Visitor<T>): T {
        return visitor.visitReturnStmt(this)
    }

    override fun toString(): String {
        return "Return($expr) at $line"
    }
}

class Indexer(val expr: Expr, val index: Expr, line: Int): Expr(line) {
    override fun <T> accept(visitor: Visitor<T>): T {
        return visitor.visitIndexer(this)
    }

    override fun toString(): String {
        return "Index($expr : $index) at $line"
    }
}

class OasisList(val exprs: ArrayList<Expr>, line: Int): Expr(line) {
    override fun <T> accept(visitor: Visitor<T>): T {
        return visitor.visitList(this)
    }
}

class Negate(val value: Expr, line: Int): Expr(line) {
    override fun <T> accept(visitor: Visitor<T>): T {
        return visitor.visitNegate(this)
    }
}

class New(val expr: Expr, line: Int): Expr(line) {
    override fun <T> accept(visitor: Visitor<T>): T {
        return visitor.visitNew(this)
    }

    override fun toString(): String {
        return "New(${expr}) at $line"
    }
}

class Not(val expr: Expr, line: Int): Expr(line) {
    override fun <T> accept(visitor: Visitor<T>): T {
        return visitor.visitNot(this)
    }

    override fun toString(): String {
        return "Not(${expr}) at $line"
    }
}

class Is(val expr: Expr, val cases: StmtList, val else_: StmtList?, line: Int): Stmt(line) {
    override fun <T> accept(visitor: Visitor<T>): T {
        return visitor.visitIs(this)
    }
}