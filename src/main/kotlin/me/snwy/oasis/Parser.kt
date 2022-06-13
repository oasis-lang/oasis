package me.snwy.oasis

import me.snwy.oasis.TokenType.*

class Parser(private val tokens: List<Token>) {
    private var current: Int = 0

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

    private fun eat(type: TokenType): Token {
        if (!peek(type)) {
            error(tokens[current].line, "Unexpected token ${tokens[current].lexeme}, expected ${type.name}")
            throw ParseException()
        }
        return tokens[current++]
    }

    private fun fnDef(): Expr {
        val begin = tokens[current].line
        eat(FN)
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
                                RetStmt(expression(), begin)),
                begin)
            return Func(operands, body, begin)
        }
        val body: ArrayList<Stmt> = ArrayList()
        while (!peek(END)) {
            body.add(statement())
        }
        eat(END)
        return Func(operands, StmtList(body, begin), begin)
    }

    private fun proto(): Expr {
        val begin = tokens[current].line
        eat(PROTO)
        var base: Expr? = null
        if (peek(GREATER)) {
            eat(GREATER)
            base = expression()
        }
        val body: ArrayList<Stmt> = ArrayList()
        while (!peek(END)) {
            if(peek(LET))
                body.add(lets())
            else
                body.add(
                    ExprStmt(
                        AssignmentExpr(
                            Variable(
                                eat(IDENTIFIER).also
                                { eat(EQUAL) }, begin
                            ),
                            expression(),
                            begin
                        ),
                        begin
                    )
                )
        }
        eat(END)
        return Proto(base, StmtList(body, begin), begin)
    }

    private fun expression(): Expr {
        val begin = tokens[current].line
        var result: Expr = Literal(null, begin)
        if (peek(IDENTIFIER)) {
            result = Variable(eat(IDENTIFIER), begin)
        } else if (peek(FN)) {
            result = fnDef()
        } else if (peek(NUMBER)) {
            result = Literal(eat(NUMBER).literal, begin)
        } else if (peek(STRING)) {
            result = Literal(eat(STRING).literal, begin)
        } else if(peek(CHAR)) {
            result = Literal(eat(CHAR).literal, begin)
        } else if (peek(TRUE)) {
            eat(TRUE)
            result = Literal(true, begin)
        } else if (peek(FALSE)) {
            eat(FALSE)
            result = Literal(false, begin)
        } else if (peek(PROTO)) {
            result = proto()
        } else if (peek(LEFT_PAREN)) {
            eat(LEFT_PAREN)
            result = Group(expression(), begin)
            eat(RIGHT_PAREN)
        } else if (peek(NIL)) {
            eat(NIL)
            // nothing...
        } else if (peek(LBRAC)) {
            eat(LBRAC)
            val body: ArrayList<Expr> = ArrayList()
            if (!peek(RBRAC)) {
                body.add(expression())
                while (!peek(RBRAC)) {
                    eat(COMMA)
                    body.add(expression())
                }
            }
            eat(RBRAC)
            result = OasisList(body, begin)
        } else if (peek(LBRACE)) {
            result = listComprehension()
        } else if(peek(MINUS)) {
            eat(MINUS)
            result = Negate(expression(), begin)
        } else if(peek(NEW)) {
            eat(NEW)
            result = New(expression(), begin)
        } else if(peek(NOT)){
            eat(NOT)
            result = Not(expression(), begin)
        } else {
            error(tokens[current].line, "Invalid expression")
            throw ParseException()
        }
        if (peek(LEFT_PAREN)) {
            val fcalle = fcall()
            result = FCallExpr(result, fcalle.operands, fcalle.line, fcalle.splat)
        }
        if (peek(COLON)) {
            eat(COLON)
            if(peek(IDENTIFIER)) {
                val ident = eat(IDENTIFIER)
                result = if (peek(LEFT_PAREN)) {
                    val fcalle = fcall()
                    FCallExpr(Property(result, ident, begin), fcalle.operands, fcalle.line, fcalle.splat)
                } else
                    Property(result, ident, begin)
            }
            else if(peek(LEFT_PAREN)) {
                eat(LEFT_PAREN)
                result = Indexer(result, expression(), begin)
                eat(RIGHT_PAREN)
            }
            while (peek(COLON)) {
                eat(COLON)
                if(peek(IDENTIFIER)) {
                    val ident = eat(IDENTIFIER)
                    result = if (peek(LEFT_PAREN)) {
                        val fcalle = fcall()
                        FCallExpr(
                            Property(result, ident, begin),
                            fcalle.operands,
                            fcalle.line,
                            fcalle.splat
                        )
                    } else
                        Property(result, ident, begin)
                }
                else if(peek(LEFT_PAREN)) {
                    eat(LEFT_PAREN)
                    result = Indexer(result, expression(), begin)
                    eat(RIGHT_PAREN)
                }
            }
        }
        if (peek(EQUAL)) {
            eat(EQUAL)
            result = AssignmentExpr(result, expression(), begin)
        }
        if (tokens[current].type in listOf(PLUS_EQUAL, MINUS_EQUAL, STAR_EQUAL, SLASH_EQUAL)) {
            when (tokens[current].type) {
                PLUS_EQUAL -> {
                    eat(PLUS_EQUAL)
                    result = AssignmentExpr(result, BinOp(result, Token.create(PLUS, begin), expression(), begin), begin)
                }
                MINUS_EQUAL -> {
                    eat(MINUS_EQUAL)
                    result = AssignmentExpr(result, BinOp(result, Token.create(MINUS, begin), expression(), begin), begin)
                }
                STAR_EQUAL -> {
                    eat(STAR_EQUAL)
                    result = AssignmentExpr(result, BinOp(result, Token.create(STAR, begin), expression(), begin), begin)
                }
                SLASH_EQUAL -> {
                    eat(SLASH_EQUAL)
                    result = AssignmentExpr(result, BinOp(result, Token.create(SLASH, begin), expression(), begin), begin)
                }
                else -> {
                    error(tokens[current].line, "This should not happen. Please report this bug.")
                    throw ParseException()
                }
            }
        }

        if (operators.contains(tokens[current].type)) {
            while (operators.contains(tokens[current].type)) {
                result = BinOp(result, eat(tokens[current].type), expression(), begin)
            }
        }
        return result
    }

    private fun fcall(): FCallExpr {
        val begin = tokens[current].line
        val operands: ArrayList<Expr> = ArrayList()
        var splat = false
        eat(LEFT_PAREN)
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
        return FCallExpr(Literal(null, -1), operands, begin, splat)
    }

    private fun listComprehension(): Expr {
        val begin = tokens[current].line
        eat(LBRACE)
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
            return MapLiteral(values, begin)
        } else {
            eat(OF)
            val list = expression()
            eat(RBRACE)
            return ListComprehension(func, list, begin)
        }
    }

    private fun statement(): Stmt {
        val begin = tokens[current].line
        val result: Stmt = if (peek(LET)) {
            lets()
        } else if (peek(IF)) {
            ifs()
        } else if (peek(WHILE)) {
            whiles()
        } else if (peek(RETURN)) {
            eat(RETURN)
            RetStmt(expression(), begin)
        } else if(peek(IS)) {
            iss()
        } else if(peek(TEST)) {
            test()
        } else if(peek(FOR)) {
            forl()
        } else if(peek(BREAK)) {
            eat(BREAK)
            return BreakStmt(begin)
        } else if(peek(CONTINUE)) {
            eat(CONTINUE)
            return ContinueStmt(begin)
        } else {
            ExprStmt(expression(), begin)
        }
        return result
    }

    private fun test(): Test {
        val begin = tokens[current].line
        eat(TEST)
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
        return Test(StmtList(testBody, begin), StmtList(errorBody, errorVar.line), errorVar, begin)
    }

    private fun forl(): Stmt {
        eat(FOR)
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
                error(first.line, "Expected name for loop variable")
                throw ParseException()
            }
            return ForLoopIterator(first.expr, second, StmtList(body, first.line), first.line)
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
            return ForLoopTriad(first, cond, step, StmtList(body, first.line), first.line)
        }
    }

    private fun ifs(): IfStmt {
        val begin = tokens[current].line
        eat(IF)
        val cond = expression()
        val body: ArrayList<Stmt> = ArrayList()
        val elseBody: ArrayList<Stmt> = ArrayList()
        while (!peek(END) && !peek(ELSE)) {
            body.add(statement())
        }
        return if (peek(END)) {
            eat(END)
            IfStmt(cond, StmtList(body, begin), null, begin)
        } else {
            eat(ELSE)
            while (!peek(END)) {
                elseBody.add(statement())
            }
            eat(END)
            IfStmt(
                cond,
                StmtList(body, begin),
                StmtList(elseBody, begin),
                begin
            )
        }
    }

    private fun iss(): Is {
        val begin = tokens[current].line
        eat(IS)
        val expr = expression()
        val cases = ArrayList<IfStmt>()
        var else_: StmtList? = null
        while (!peek(END) && !peek(ELSE)) {
            val caseExpr = expression()
            val caseBody = ArrayList<Stmt>()
            while(!peek(END)) {
                caseBody.add(statement())
            }
            eat(END)
            cases.add(
                IfStmt(
                    BinOp(
                        expr,
                        Token(EQUAL_EQUAL, "==", null, begin),
                        caseExpr,
                        begin
                    ), StmtList(caseBody, begin),
                    null,
                    begin
                )
            )
        }
        if (peek(ELSE)) {
            eat(ELSE)
            val caseBody = ArrayList<Stmt>()
            while(!peek(END)) {
                caseBody.add(statement())
            }
            eat(END)
            else_ = StmtList(caseBody, begin)
        }
        eat(END)
        return Is(expr, StmtList(cases, begin), else_, begin)
    }

    private fun whiles(): WhileStmt {
        val begin = tokens[current].line
        eat(WHILE)
        val cond = expression()
        val body: ArrayList<Stmt> = ArrayList()
        while (!peek(END)) {
            body.add(statement())
        }
        eat(END)
        return WhileStmt(cond, StmtList(body, begin), begin)
    }

    private fun lets(): Let {
        val begin = tokens[current].line
        var immutable = false
        eat(LET)
        if (peek(IMMUTABLE)) {
            eat(IMMUTABLE)
            immutable = true
        }
        val name: Token = eat(IDENTIFIER)
        eat(EQUAL)
        val value: Expr = expression()
        return Let(name, value, begin, immutable)
    }

    fun parse(): Stmt {
        val begin = tokens[current].line
        val stmtList: ArrayList<Stmt> = ArrayList()
        while (!peek(EOF)) {
            stmtList.add(statement())
        }
        return StmtList(stmtList, begin)
    }
}