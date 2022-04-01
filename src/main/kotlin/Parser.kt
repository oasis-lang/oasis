import TokenType.*

class Parser(private val tokens: List<Token>) {
    private var current: Int = 0

    private var operators: List<TokenType> = listOf<TokenType>(
        MINUS, PLUS, SLASH, STAR,
        BANG, BANG_EQUAL,
        EQUAL, EQUAL_EQUAL,
        GREATER, GREATER_EQUAL,
        LESS, LESS_EQUAL, MOD
    )

    private fun peek(type: TokenType): Boolean {
        return tokens[current].type == type
    }

    private fun eat(type: TokenType): Token {
        if (!peek(type)) {
            println(tokens)
            error(tokens[current].line, "Unexpected token ${tokens[current].lexeme}, expected ${type.name}")
            throw Exception()
        }
        return tokens[current++]
    }

    private fun fnDef(): Expr {
        var begin = tokens[current].line
        eat(FN)
        eat(LEFT_PAREN)
        var operands: ArrayList<Token> = ArrayList()
        if (peek(IDENTIFIER)) {
            operands.add(eat(IDENTIFIER))
            while (!peek(RIGHT_PAREN)) {
                eat(COMMA)
                operands.add(eat(IDENTIFIER))
            }
        }
        eat(RIGHT_PAREN)
        var body: ArrayList<Stmt> = ArrayList()
        while (!peek(END)) {
            body.add(statement())
        }
        eat(END)
        return Func(operands, StmtList(body, begin), begin)
    }

    private fun proto(): Expr {
        var begin = tokens[current].line
        eat(PROTO)
        var base: Token? = null
        if (peek(GREATER)) {
            eat(GREATER)
            base = eat(IDENTIFIER)
        }
        var body: ArrayList<Let> = ArrayList()
        while (!peek(END)) {
            body.add(lets())
        }
        eat(END)
        return Proto(base, StmtList(body, begin), begin)
    }

    private fun expression(): Expr {
        var begin = tokens[current].line
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
            var body: ArrayList<Expr> = ArrayList()
            if (!peek(RBRAC)) {
                body.add(expression())
                while (!peek(RBRAC)) {
                    eat(COMMA)
                    body.add(expression())
                }
            }
            eat(RBRAC)
            result = OasisList(body, begin)
        } else if(peek(MINUS)) {
            eat(MINUS)
            result = Negate(expression(), begin)
        } else {
            error(tokens[current].line, "Invalid expression")
            println(tokens.slice(current until tokens.size))
            throw Exception()
        }
        if (peek(LEFT_PAREN)) {
            val fcalle = fcall()
            result = FCallExpr(result, fcalle.operands, fcalle.line)
        }
        if (peek(COLON)) {
            eat(COLON)
            if(peek(IDENTIFIER)) {
                val ident = eat(IDENTIFIER)
                if (peek(LEFT_PAREN)) {
                    val fcalle = fcall()
                    result = FCallExpr(Property(result, ident, begin), fcalle.operands, fcalle.line)
                } else
                    result = Property(result, ident, begin)
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
                    if (peek(LEFT_PAREN)) {
                        val fcalle = fcall()
                        result = FCallExpr(Property(result, ident, begin), fcalle.operands, fcalle.line)
                    } else
                        result = Property(result, ident, begin)
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

        if (operators.contains(tokens[current].type)) {
            while (operators.contains(tokens[current].type)) {
                result = BinOp(result, eat(tokens[current].type), expression(), begin)
            }
        }
        return result
    }

    private fun fcall(): FCallExpr {
        val begin = tokens[current].line
        eat(LEFT_PAREN)
        val operands: ArrayList<Expr> = ArrayList()
        if (!peek(RIGHT_PAREN)) {
            operands.add(expression())
            while (!peek(RIGHT_PAREN)) {
                eat(COMMA)
                operands.add(expression())
            }
        }
        eat(RIGHT_PAREN)
        return FCallExpr(Literal(null, -1), operands, begin)
    }

    private fun statement(): Stmt {
        var begin = tokens[current].line
        var result: Stmt
        result = if (peek(LET)) {
            lets()
        } else if (peek(IF)) {
            ifs()
        } else if (peek(WHILE)) {
            whiles()
        } else if (peek(RETURN)) {
            eat(RETURN)
            RetStmt(expression(), begin)
        } else {
            ExprStmt(expression(), begin)
        }
        return result
    }

    private fun ifs(): IfStmt {
        var begin = tokens[current].line
        eat(IF)
        var cond = expression()
        var body: ArrayList<Stmt> = ArrayList()
        var elseBody: ArrayList<Stmt> = ArrayList()
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
            IfStmt(cond, StmtList(body, begin), StmtList(elseBody, begin), begin)
        }
    }

    private fun whiles(): WhileStmt {
        var begin = tokens[current].line
        eat(WHILE)
        var cond = expression()
        var body: ArrayList<Stmt> = ArrayList()
        while (!peek(END)) {
            body.add(statement())
        }
        eat(END)
        return WhileStmt(cond, StmtList(body, begin), begin)
    }

    private fun lets(): Let {
        var begin = tokens[current].line
        eat(LET)
        var name: Token = eat(IDENTIFIER)
        eat(EQUAL)
        var value: Expr = expression()
        return Let(name, value, begin)
    }

    fun parse(): Stmt {
        var begin = tokens[current].line
        var stmtList: ArrayList<Stmt> = ArrayList()
        while (!peek(EOF)) {
            stmtList.add(statement())
        }
        return StmtList(stmtList, begin)
    }
}