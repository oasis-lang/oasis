package me.snwy.oasis
class Token(val type: TokenType, val lexeme: String, val literal: Any?, val line: Int) {
    companion object {
        fun create(type: TokenType, lexeme: String, literal: Any?, line: Int): Token {
            return Token(type, lexeme, literal, line)
        }
        fun create(type: TokenType, lexeme: String, line: Int): Token {
            return Token(type, lexeme, null, line)
        }
        fun create(type: TokenType, line: Int): Token {
            return Token(type, "", null, line)
        }
    }
    override fun toString(): String {
        return "$type $lexeme $literal"
    }
}