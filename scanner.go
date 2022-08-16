package main

import "fmt"

type Scanner struct {
	Source   string
	Start    int
	Pos      int
	Line     int
	Col      int
	Keywords map[string]TokenType
	Tokens   []Token
}

func NewScanner(Input string) Scanner {
	return Scanner{
		Source: Input,
		Start:  0,
		Pos:    0,
		Line:   1,
		Col:    1,
		Keywords: map[string]TokenType{
			"let":               Let,
			"proto":             Proto,
			"fn":                Fn,
			"for":               For,
			"if":                If,
			"nil":               Nil,
			"return":            Return,
			"true":              True,
			"false":             False,
			"while":             While,
			"end":               End,
			"else":              Else,
			"and":               And,
			"not":               Not,
			"or":                Or,
			"clone":             Clone,
			"is":                Is,
			"const":             Const,
			"test":              Test,
			"interpreter_error": Error,
			"in":                In,
			"break":             Break,
			"continue":          Continue,
			"of":                Of,
			"rel":               Rel,
			"begin":             Begin,
			"until":             Until,
			"unless":            Unless,
		},
	}
}

func (s *Scanner) Scan() {
	for !s.IsAtEnd() {
		s.ScanToken()
		s.Start = s.Pos
	}
	s.Tokens = append(s.Tokens, Token{Type: Eof, Lexeme: "", Line: s.Line, Column: s.Col})
}

func (s *Scanner) IsAtEnd() bool {
	return s.Pos >= len(s.Source)
}

func (s *Scanner) Advance() byte {
	s.Pos += 1
	s.Col += 1
	return s.Source[s.Pos-1]
}

func (s *Scanner) AddToken(tokenType TokenType) {
	var text = s.Source[s.Start:s.Pos]
	s.Tokens = append(s.Tokens, Token{Type: tokenType, Lexeme: text, Line: s.Line, Column: s.Col, Literal: ""})
}

func (s *Scanner) AddTokenLiteral(tokenType TokenType, literal string) {
	var text = s.Source[s.Start:s.Pos]
	s.Tokens = append(s.Tokens, Token{Type: tokenType, Lexeme: text, Line: s.Line, Column: s.Col, Literal: literal})
}

func (s *Scanner) Match(expected uint8) bool {
	if s.IsAtEnd() {
		return false
	}
	if s.Source[s.Pos] != expected {
		return false
	}
	s.Advance()
	return true
}

func (s *Scanner) ScanToken() {
	var c = s.Advance()
	switch c {
	case '(':
		s.AddToken(LeftParen)
	case ')':
		s.AddToken(RightParen)
	case ',':
		s.AddToken(Comma)
	case ':':
		s.AddToken(Colon)
	case '-':
		if s.Match('=') {
			s.AddToken(MinusEqual)
		} else {
			s.AddToken(Minus)
		}
	case '+':
		if s.Match('=') {
			s.AddToken(PlusEqual)
		} else {
			s.AddToken(Plus)
		}
	case '*':
		if s.Match('=') {
			s.AddToken(StarEqual)
		} else {
			s.AddToken(Star)
		}
	case '/':
		if s.Match('/') {
			for !s.IsAtEnd() && s.Peek() != '\n' {
				s.Advance()
			}
		} else if s.Match('*') {
			for !s.IsAtEnd() {
				if s.Match('*') && s.Match('/') {
					break
				}
				s.Advance()
			}
		} else if s.Match('=') {
			s.AddToken(SlashEqual)
		} else {
			s.AddToken(Slash)
		}
	case '%':
		if s.Match('=') {
			s.AddToken(ModEqual)
		} else {
			s.AddToken(Mod)
		}
	case '!':
		if s.Match('=') {
			s.AddToken(BangEqual)
		} else {
			s.AddToken(Bang)
		}
	case '=':
		if s.Match('=') {
			s.AddToken(EqualEqual)
		} else if s.Match('>') {
			s.AddToken(LambdaArrow)
		} else {
			s.AddToken(Equal)
		}
	case '<':
		if s.Match('=') {
			s.AddToken(LessEqual)
		} else if s.Match('|') {
			s.AddToken(LeftPipe)
		} else {
			s.AddToken(Less)
		}
	case '>':
		if s.Match('=') {
			s.AddToken(GreaterEqual)
		} else {
			s.AddToken(Greater)
		}
	case '"':
		s.String()
	case '\'':
		s.Char()
	case '[':
		s.AddToken(LeftBracket)
	case ']':
		s.AddToken(RightBracket)
	case '{':
		s.AddToken(LeftBrace)
	case '}':
		s.AddToken(RightBrace)
	case '#':
		if s.Match('!') {
			for s.Peek() != '\n' {
				s.Advance()
			}
			return
		}
		interpreter_error(fmt.Sprintf("Invalid character %c", c), s.Line, s.Col, s.Col)
	case '|':
		if s.Match('>') {
			s.AddToken(RightPipe)
		} else {
			s.AddToken(Pipe)
		}
	case '?':
		s.AddToken(Question)
	case '0':
		if s.Match('x') {
			s.Hex()
		} else {
			s.Number()
		}
	case '1', '2', '3', '4', '5', '6', '7', '8', '9':
		s.Number()
	case '\n':
		s.Line++
		s.Col = 1
	case ' ', '\t', '\r':
		return
	default:
		if isAlpha(c) {
			s.Identifier()
		} else if isNumeric(c) {
			s.Number()
		} else {
			interpreter_error(fmt.Sprintf("Invalid character %c", c), s.Line, s.Col, s.Col)
		}
	}
}

func (s *Scanner) Hex() {
	for isHex(s.Peek()) {
		s.Advance()
	}
	s.AddToken(Byte)
}

func isHex(c byte) bool {
	return isNumeric(c) || (c >= 'a' && c <= 'f') || (c >= 'A' && c <= 'F')
}

func (s *Scanner) Peek() uint8 {
	if s.IsAtEnd() {
		return 0
	}
	return s.Source[s.Pos]
}

func (s *Scanner) Identifier() {
	for isAlphanumeric(s.Peek()) {
		s.Advance()
	}
	var text = s.Source[s.Start:s.Pos]
	if val, ok := s.Keywords[text]; ok {
		s.AddToken(val)
		return
	}
	s.AddToken(Identifier)
}

func (s *Scanner) String() {
	for !s.Match('"') {
		if s.IsAtEnd() {
			interpreter_error("Unterminated string", s.Line, s.Col, s.Col)
		}
		s.Advance()
	}
	s.AddTokenLiteral(String, s.Source[s.Start+1:s.Pos-1])
}

func (s *Scanner) Char() {
	var c = s.Advance()
	s.Match('\'')
	s.AddTokenLiteral(Char, string(c))
}

func (s *Scanner) Number() {
	var isFloat = false
	for isNumeric(s.Peek()) {
		s.Advance()
	}
	if s.Match('.') {
		isFloat = true
		for isNumeric(s.Peek()) {
			s.Advance()
		}
	}
	if isFloat {
		s.AddToken(Float)
	} else {
		s.AddToken(Int)
	}
}

func isNumeric(b byte) bool {
	return b >= '0' && b <= '9'
}

func isAlpha(b byte) bool {
	return 'a' <= b && b <= 'z' || 'A' <= b && b <= 'Z' || b == '_'
}

func isAlphanumeric(b byte) bool {
	return isNumeric(b) || isAlpha(b)
}
