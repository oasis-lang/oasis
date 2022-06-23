package me.snwy.oasis

import me.snwy.oasis.TokenType.*
import java.util.*
import kotlin.collections.ArrayList

class Parser(val tokens: List<Token>) {
    var current: Int = 0

    private fun error(line: Int, column: Int, msg: String) {
        throw ParseException(line, column, msg)
    }

    private var operators: List<TokenType> = listOf(
        MINUS, PLUS, SLASH, STAR,
        BANG, BANG_EQUAL,
        EQUAL, EQUAL_EQUAL,
        GREATER, GREATER_EQUAL,
        LESS, LESS_EQUAL, MOD,
        AND, OR, RIGHT_PIPE, LEFT_PIPE,
        QUESTION,
    )

    private fun peek(type: TokenType): Boolean {
        return tokens[current].type == type
    }

    private fun AorAn(word: String): String {
        return if (word.startsWith("a") || word.startsWith("e") || word.startsWith("i") || word.startsWith("o") || word.startsWith(
                "u"
            )
        ) {
            "an $word"
        } else {
            "a $word"
        }
    }

    private fun eat(type: TokenType): Token {
        if (!peek(type)) {
            error(tokens[current].line, tokens[current].column, "Unexpected ${if(tokens[current].lexeme == "" || tokens[current].lexeme == " ") "space" else tokens[current].lexeme.lowercase(
                Locale.getDefault()
            )}, expected ${AorAn(
                type.name.lowercase(Locale.getDefault())
            )}")
        }
        return tokens[current++]
    }

    private fun fnDef(): Expr {
        val begin = tokens[current].line
        var column: Int
        eat(FN).also { column = it.column }
        eat(LEFT_PAREN)
        val operands: ArrayList<Token> = ArrayList()
        if (peek(IDENTIFIER)) {
            operands.add(eat(IDENTIFIER))
            while (!peek(RIGHT_PAREN)) {
                eat(COMMA)
                operands.add(eat(IDENTIFIER))
            }
        }
        eat(RIGHT_PAREN)
        if (peek(LAMBDA_ARROW)) {
            eat(LAMBDA_ARROW)
            val body = StmtList(listOf(
                                RetStmt(expression(), begin, column)),
                begin, column)
            return Func(operands, body, begin, column)
        }
        val body: ArrayList<Stmt> = ArrayList()
        while (!peek(END)) {
            body.add(statement())
        }
        eat(END)
        return Func(operands, StmtList(body, begin, column), begin, column)
    }

    private fun proto(): Expr {
        val begin = tokens[current].line
        var column: Int
        eat(PROTO).also { column = it.column }
        var base: Expr? = null
        if (peek(GREATER)) {
            eat(GREATER)
            base = expression()
        }
        val body: ArrayList<Stmt> = ArrayList()
        while (!peek(END)) {
            var column: Int
            if(peek(LET))
                body.add(lets())
            else
                body.add(
                    ExprStmt(
                        AssignmentExpr(
                            Variable(
                                eat(IDENTIFIER).also
                                { eat(EQUAL); column = it.column }, begin, column
                            ),
                            expression(),
                            begin, column
                        ),
                        begin, column
                    )
                )
        }
        eat(END)
        return Proto(base, StmtList(body, begin, column), begin, column)
    }

    private fun expression(): Expr {
        val begin = tokens[current].line
        var result: Expr = Literal(null, begin, tokens[current].column)
        if (peek(IDENTIFIER)) {
            var column: Int
            result = Variable(eat(IDENTIFIER).also { column = it.column }, begin, column)
        } else if (peek(FN)) {
            result = fnDef()
        } else if (peek(NUMBER)) {
            var column: Int
            result = Literal(eat(NUMBER).also { column = it.column }.literal, begin, column)
        } else if (peek(STRING)) {
            var column: Int
            result = Literal(eat(STRING).also { column = it.column }.literal, begin, column)
        } else if(peek(CHAR)) {
            var column: Int
            result = Literal(eat(CHAR).also { column = it.column }.literal, begin, column)
        } else if (peek(BYTE)) {
            var column: Int
            result = Literal(eat(BYTE).also { column = it.column }.literal, begin, column)
        } else if (peek(TRUE)) {
            var column: Int
            eat(TRUE).also { column = it.column }
            result = Literal(true, begin, column)
        } else if (peek(FALSE)) {
            var column: Int
            eat(FALSE).also { column = it.column }
            result = Literal(false, begin, column)
        } else if (peek(PROTO)) {
            result = proto()
        } else if (peek(LEFT_PAREN)) {
            var column: Int
            eat(LEFT_PAREN).also { column = it.column }
            result = Group(expression(), begin, column)
            eat(RIGHT_PAREN)
        } else if (peek(NIL)) {
            result = Literal(null, begin, eat(NIL).column)
        } else if (peek(LBRAC)) {
            var column: Int
            eat(LBRAC).also { column = it.column }
            val body: ArrayList<Expr> = ArrayList()
            if (!peek(RBRAC)) {
                body.add(expression())
                while (!peek(RBRAC)) {
                    eat(COMMA)
                    body.add(expression())
                }
            }
            eat(RBRAC)
            result = OasisList(body, begin, column)
        } else if (peek(LBRACE)) {
            result = listComprehension()
        } else if(peek(MINUS)) {
            var column: Int
            eat(MINUS).also { column = it.column }
            result = Negate(expression(), begin, column)
        } else if(peek(NEW)) {
            var column: Int
            eat(NEW).also { column = it.column }
            result = New(expression(), begin, column)
        } else if(peek(NOT)){
            var column: Int
            eat(NOT).also { column = it.column }
            result = Not(expression(), begin, column)
        } else if(peek(IF)) {
            var column: Int
            eat(IF).also { column = it.column }
            val condition = expression()
            eat(LAMBDA_ARROW)
            val then = expression()
            eat(ELSE)
            val else_ = expression()
            result = IfExpression(condition, then, else_, begin, column)
        } else {
            error(tokens[current].line, tokens[current].column, "Invalid expression")
        }
        if (peek(LEFT_PAREN)) {
            val fcalle = fcall()
            result = FCallExpr(result, fcalle.operands, fcalle.line, fcalle.column, fcalle.splat)
        }
        if (peek(COLON)) {
            eat(COLON)
            if(peek(IDENTIFIER)) {
                val ident = eat(IDENTIFIER)
                result = if (peek(LEFT_PAREN)) {
                    val fcalle = fcall()
                    FCallExpr(Property(result, ident, begin, result.column), fcalle.operands, fcalle.line, fcalle.column, fcalle.splat)
                } else
                    Property(result, ident, begin, result.column)
            }
            else if(peek(LEFT_PAREN)) {
                eat(LEFT_PAREN)
                result = Indexer(result, expression(), begin, eat(RIGHT_PAREN).column)
            }
            while (peek(COLON)) {
                eat(COLON)
                if(peek(IDENTIFIER)) {
                    val ident = eat(IDENTIFIER)
                    result = if (peek(LEFT_PAREN)) {
                        val fcalle = fcall()
                        FCallExpr(
                            Property(result, ident, begin, fcalle.column),
                            fcalle.operands,
                            fcalle.line,
                            fcalle.column,
                            fcalle.splat
                        )
                    } else
                        Property(result, ident, begin, result.column)
                }
                else if(peek(LEFT_PAREN)) {
                    eat(LEFT_PAREN)
                    result = Indexer(result, expression(), begin, result.column)
                    eat(RIGHT_PAREN)
                }
            }
        }
        if (peek(EQUAL)) {
            eat(EQUAL)
            result = AssignmentExpr(result, expression(), begin, result.column)
        }
        if (tokens[current].type in listOf(PLUS_EQUAL, MINUS_EQUAL, STAR_EQUAL, SLASH_EQUAL)) {
            when (tokens[current].type) {
                PLUS_EQUAL -> {
                    eat(PLUS_EQUAL)
                    result = AssignmentExpr(result, BinOp(result, Token.create(PLUS, begin, tokens[current].column), expression(), begin, result.column), begin, result.column)
                }
                MINUS_EQUAL -> {
                    eat(MINUS_EQUAL)
                    result = AssignmentExpr(result, BinOp(result, Token.create(MINUS, begin, tokens[current].column), expression(), begin, result.column), begin, result.column)
                }
                STAR_EQUAL -> {
                    eat(STAR_EQUAL)
                    result = AssignmentExpr(result, BinOp(result, Token.create(STAR, begin, tokens[current].column), expression(), begin, result.column), begin, result.column)
                }
                SLASH_EQUAL -> {
                    eat(SLASH_EQUAL)
                    result = AssignmentExpr(result, BinOp(result, Token.create(SLASH, begin, tokens[current].column), expression(), begin, result.column), begin, result.column)
                }
                else -> {
                    error(tokens[current].line, "This should not happen. Please report this bug.")
                }
            }
        }

        if (operators.contains(tokens[current].type)) {
            while (operators.contains(tokens[current].type)) {
                result = BinOp(result, eat(tokens[current].type), expression(), begin, result.column)
            }
        }
        return result
    }

    private fun fcall(): FCallExpr {
        val begin = tokens[current].line
        var column: Int
        val operands: ArrayList<Expr> = ArrayList()
        var splat = false
        eat(LEFT_PAREN).also { column = it.column }
        if (peek(STAR)) {
            eat(STAR)
            operands.add(expression())
            splat = true
        } else {
            if (!peek(RIGHT_PAREN)) {
                operands.add(expression())
                while (!peek(RIGHT_PAREN)) {
                    eat(COMMA)
                    operands.add(expression())
                }
            }
        }
        eat(RIGHT_PAREN)
        return FCallExpr(Literal(null, -1, -1), operands, begin, column, splat)
    }

    private fun listComprehension(): Expr {
        val begin = tokens[current].line
        var column: Int
        eat(LBRACE).also { column = it.column }
        val func = expression()
        if (peek(PIPE)) {
            eat(PIPE)
            val value = expression()
            val values = arrayListOf(Pair(func, value))
            while (!peek(RBRACE)) {
                eat(COMMA)
                values.add(Pair(
                    expression().also { eat(PIPE) },
                    expression())
                )
            }
            eat(RBRACE)
            return MapLiteral(values, begin, column)
        } else {
            eat(OF)
            val list = expression()
            eat(RBRACE)
            return ListComprehension(func, list, begin, column)
        }
    }

    private fun statement(): Stmt {
        val begin = tokens[current].line
        val column: Int
        val result: Stmt = if (peek(LET)) {
            lets()
        } else if (peek(IF)) {
            ifs()
        } else if (peek(WHILE)) {
            whiles()
        } else if (peek(RETURN)) {
            eat(RETURN).also { column = it.column }
            RetStmt(if (!peek(END)) expression() else null, begin, column)
        } else if(peek(IS)) {
            iss()
        } else if(peek(TEST)) {
            test()
        } else if(peek(FOR)) {
            forl()
        } else if(peek(BREAK)) {
            eat(BREAK).also { column = it.column }
            return BreakStmt(begin, column)
        } else if(peek(CONTINUE)) {
            eat(CONTINUE).also { column = it.column }
            return ContinueStmt(begin, column)
        } else {
            ExprStmt(expression().also { column = it.column }, begin, column)
        }
        return result
    }

    private fun test(): Test {
        val begin = tokens[current].line
        val column: Int
        eat(TEST).also { column = it.column }
        val testBody = ArrayList<Stmt>()
        while(!peek(ERROR)) {
            testBody.add(statement())
        }
        eat(ERROR)
        eat(LEFT_PAREN)
        val errorVar = eat(IDENTIFIER)
        eat(RIGHT_PAREN)
        val errorBody = ArrayList<Stmt>()
        while(!peek(END)) {
            errorBody.add(statement())
        }
        eat(END)
        return Test(StmtList(testBody, begin, column), StmtList(errorBody, errorVar.line, errorVar.column), errorVar, begin, column)
    }

    private fun forl(): Stmt {
        val column: Int
        eat(FOR).also {
            column = it.column
        }
        val first = statement()
        if(peek(IN)) {
            eat(IN)
            val second = expression()
            val body = ArrayList<Stmt>()
            while(!peek(END)) {
                body.add(statement())
            }
            eat(END)
            if (first !is ExprStmt || first.expr !is Variable) {
                error(first.line, first.column,"Expected name for loop variable")
            }
            return ForLoopIterator((first as ExprStmt).expr, second, StmtList(body, first.line, first.column), first.line, column)
        }
        else {
            eat(PIPE)
            val cond = expression()
            eat(PIPE)
            val step = statement()
            val body = ArrayList<Stmt>()
            while(!peek(END)) {
                body.add(statement())
            }
            eat(END)
            return ForLoopTriad(first, cond, step, StmtList(body, first.line, first.column), first.line, column)
        }
    }

    private fun ifs(): IfStmt {
        val begin = tokens[current].line
        var column: Int
        eat(IF).also { column = it.column }
        val cond = expression()
        val body: ArrayList<Stmt> = ArrayList()
        val elseBody: ArrayList<Stmt> = ArrayList()
        while (!peek(END) && !peek(ELSE)) {
            body.add(statement())
        }
        return if (peek(END)) {
            eat(END)
            IfStmt(cond, StmtList(body, begin, cond.column), null, begin, column)
        } else {
            var elseColumn: Int
            eat(ELSE).also { elseColumn = it.column }
            while (!peek(END)) {
                elseBody.add(statement())
            }
            eat(END)
            IfStmt(
                cond,
                StmtList(body, begin, cond.column),
                StmtList(elseBody, begin, elseColumn),
                begin,
                column
            )
        }
    }

    private fun iss(): Is {
        val begin = tokens[current].line
        var column: Int
        eat(IS).also { column = it.column }
        val expr = expression()
        val cases = ArrayList<IfStmt>()
        var else_: StmtList? = null
        while (!peek(END) && !peek(ELSE)) {
            val caseExpr = expression()
            val thisColumn = caseExpr.column
            eat(LAMBDA_ARROW)
            val caseBody = ArrayList<Stmt>()
            while(!peek(END)) {
                caseBody.add(statement())
            }
            eat(END)
            cases.add(
                IfStmt(
                    BinOp(
                        expr,
                        Token(EQUAL_EQUAL, "==", null, begin, column),
                        caseExpr,
                        begin,
                        thisColumn
                    ), StmtList(caseBody, begin, thisColumn),
                    null,
                    begin,
                    thisColumn
                )
            )
        }
        if (peek(ELSE)) {
            var thisColumn = 0
            eat(ELSE).also { thisColumn = it.column }
            val caseBody = ArrayList<Stmt>()
            while(!peek(END)) {
                caseBody.add(statement())
            }
            eat(END)
            else_ = StmtList(caseBody, begin, thisColumn)
        }
        eat(END)
        return Is(expr, StmtList(cases, begin, column), else_, begin, column)
    }

    private fun whiles(): WhileStmt {
        val begin = tokens[current].line
        var column: Int
        eat(WHILE).also { column = it.column }
        val cond = expression()
        val body: ArrayList<Stmt> = ArrayList()
        while (!peek(END)) {
            body.add(statement())
        }
        eat(END)
        return WhileStmt(cond, StmtList(body, begin, cond.column), begin, column)
    }

    private fun lets(): Let {
        val begin = tokens[current].line
        val column: Int
        var immutable = false
        eat(LET).also { column = it.column }
        if (peek(IMMUTABLE)) {
            eat(IMMUTABLE)
            immutable = true
        }
        val name: Token = eat(IDENTIFIER)
        eat(EQUAL)
        val value: Expr = expression()
        return Let(name, value, begin, column, immutable)
    }

    fun parse(): Stmt {
        val begin = tokens[current].line
        val column = tokens[current].column
        val stmtList: ArrayList<Stmt> = ArrayList()
        while (!peek(EOF)) {
            stmtList.add(statement())
        }
        return StmtList(stmtList, begin, column)
    }
}