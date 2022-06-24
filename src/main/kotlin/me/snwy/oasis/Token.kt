package me.snwy.oasis

class Token(val type: TokenType, val lexeme: String, val literal: Any?, val line: Int, val column: Int) {
    companion object {
        fun create(type: TokenType, lexeme: String, literal: Any?, line: Int, column: Int): Token {
            return Token(type, lexeme, literal, line, column)
        }

        fun create(type: TokenType, lexeme: String, line: Int, column: Int): Token {
            return Token(type, lexeme, null, line, column)
        }

        fun create(type: TokenType, line: Int, column: Int): Token {
            return Token(type, "", null, line, column)
        }
    }

    override fun toString(): String {
        return "$type $lexeme $literal"
    }
}