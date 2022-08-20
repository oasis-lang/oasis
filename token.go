package main

import "fmt"

type TokenType int

//go:generate stringer -type=TokenType
const (
	LeftParen TokenType = iota
	RightParen
	Comma
	Colon
	Minus
	Plus
	Slash
	Star
	LeftBracket
	RightBracket
	LeftBrace
	RightBrace
	Bang
	BangEqual
	Pipe
	Equal
	EqualEqual
	Greater
	GreaterEqual
	Less
	LessEqual
	Mod
	PlusEqual
	MinusEqual
	SlashEqual
	StarEqual
	ModEqual
	RightPipe
	LeftPipe
	Question
	LambdaArrow
	Identifier
	String
	Float
	Int
	Char
	Byte
	Let
	Proto
	Fn
	For
	If
	Nil
	Return
	True
	False
	While
	End
	Else
	And
	Not
	Or
	Clone
	Map
	Item
	Is
	Const
	Test
	Error
	In
	Break
	Continue
	Of
	Rel
	Begin
	Until
	Unless
	Eof
)

type Token struct {
	Type    TokenType
	Lexeme  string
	Literal string
	Line    int
	Column  int
}

func (t Token) String() string {
	return fmt.Sprintf("Token(%s (%s) '%s' %d %d)", t.Type.String(), t.Lexeme, t.Literal, t.Line, t.Column)
}
