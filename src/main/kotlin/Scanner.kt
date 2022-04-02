import TokenType.*

class Scanner(val source: String) {
    private var tokens: ArrayList<Token> = ArrayList()
    private var start: Int = 0
    private var current: Int = 0
    private var line: Int = 1

    private var keywords: Map<String, TokenType> = mapOf(
        "let" to LET,
        "proto" to PROTO,
        "fn" to FN,
        "for" to FOR,
        "if" to IF,
        "nil" to NIL,
        "return" to RETURN,
        "true" to TRUE,
        "false" to FALSE,
        "while" to WHILE,
        "end" to END,
        "else" to ELSE,
        "and" to AND,
        "or" to OR,
        "not" to NOT,
        "clone" to NEW
    )

    fun scanTokens(): List<Token> {
        while(!isAtEnd()) {
            start = current
            scanToken()
        }
        tokens.add(Token(EOF, "", null, line))
        return tokens
    }

    private fun isAtEnd(): Boolean {
        return current >= source.length
    }

    private fun scanToken() {
        var c: Char = advance()
        when(c) {
            '(' -> addToken(LEFT_PAREN)
            ')' -> addToken(RIGHT_PAREN)
            ',' -> addToken(COMMA)
            ':' -> addToken(COLON)
            '-' -> addToken(MINUS)
            '+' -> addToken(PLUS)
            '*' -> addToken(STAR)
            '!' -> addToken(if (match('=')) BANG_EQUAL else BANG)
            '=' -> addToken(if (match('=')) EQUAL_EQUAL else EQUAL)
            '<' -> addToken(if (match('=')) LESS_EQUAL else LESS)
            '>' -> addToken(if (match('=')) GREATER_EQUAL else GREATER)
            '/' -> if (match('/'))
                    while (peek() != '\n' && !isAtEnd())
                        advance()
                    else
                        addToken(SLASH)
            '[' -> addToken(LBRAC)
            ']' -> addToken(RBRAC)
            ' ', '\r', '\t', -> null
            '\n' -> line++
            '"' -> string()
            '\'' -> char()
            '%' -> addToken(MOD)
            else -> if(c.isDigit())
                        number()
                    else if(c.isLetter())
                        identifier()
                    else
                        error(line, "Unexpected character '$c'.")
        }
    }

    private fun identifier() {
        while(peek().isDigit() || peek().isLetter()) advance()
        var text: String = source.substring(start, current)
        var type: TokenType? = keywords[text]
        if(type == null) type = IDENTIFIER
        addToken(type)
    }

    private fun string() {
        while (peek() != '"' && !isAtEnd()) {
            if(peek() == '\n') line++
            advance()
        }
        if(isAtEnd()) {
            error(line, "String without end.")
            return
        }
        advance()
        var value: String = source.substring(start + 1, current - 1).replace("\\r", "\r").replace("\\n", "\n")
        addToken(STRING, value)
    }

    private fun char() {
        var c: Char = advance()
        if (advance() != '\'') {
            error(line, "Unterminated char literal.")
            return
        }
        addToken(CHAR, c)
    }

    private fun peek(): Char {
        if(isAtEnd()) return Char(0)
        return source[current]
    }

    private fun peekNext(): Char{
        if(current + 1 >- source.length) return Char(0)
        return source[current + 1]
    }

    private fun number(negative: Boolean = false){
        while(peek().isDigit()) advance()
        if(peek() == '.' && peekNext().isDigit()) {
            advance()
            while(peek().isDigit()) advance()
        }

        addToken(NUMBER, if (!negative) source.substring(start, current).toDouble() else -( source.substring(start, current).toDouble()) )
    }

    private fun match(expected: Char): Boolean {
        if(isAtEnd()) return false
        if(source[current] != expected) return false
        current++
        return true
    }

    private fun advance(): Char {
        return source[current++]
    }

    private fun addToken(type: TokenType) {
        addToken(type, null)
    }

    private fun addToken(type: TokenType, literal: Any?) {
        var text: String = source.substring(start, current)
        tokens.add(Token(type, text, literal, line))
    }

}