package main

import (
	"fmt"
	"strconv"
)

type Parser struct {
	Tokens []Token
	Pos    int
	Code   []Instruction
	Depth  int
}

func NewParser(Tokens []Token) Parser {
	return Parser{
		Tokens: Tokens,
		Pos:    0,
		Code:   []Instruction{},
		Depth:  0,
	}
}

func (p *Parser) Parse() {
	for !p.PeekType(Eof) {
		p.Statement()
	}
	p.Code = append(p.Code, Instruction{
		Opcode: EndProgram,
		Args:   []interface{}{},
	})
}

func (p *Parser) Peek() TokenType {
	return p.Tokens[p.Pos].Type
}

func (p *Parser) PeekType(t TokenType) bool {
	return p.Peek() == t
}

func (p *Parser) Eat(t TokenType) Token {
	if p.PeekType(t) {
		p.Pos++
		return p.Tokens[p.Pos-1]
	} else {
		interpreterError(fmt.Sprintf("Expected %s, got %s", t, p.Peek().String()), p.Tokens[p.Pos].Line, p.Tokens[p.Pos].Column, p.Tokens[p.Pos].Column+len(p.Tokens[p.Pos].Lexeme))
	}
	return Token{
		Type:    Eof,
		Lexeme:  "",
		Literal: "",
		Line:    0,
		Column:  0,
	}
}

func (p *Parser) Factor() {
	var negated = false
	var not = false
	if p.PeekType(Minus) {
		negated = true
		p.Eat(Minus)
	}
	if p.PeekType(Not) {
		not = true
		p.Eat(Not)
	}
	switch p.Peek() {
	case Identifier:
		var identifier = p.Eat(Identifier)
		p.Code = append(p.Code, Instruction{
			Opcode: FetchVariable,
			Args: []interface{}{
				identifier.Lexeme,
			},
			Line: identifier.Line,
			Col1: identifier.Column,
			Col2: len(identifier.Lexeme) + identifier.Column,
		})
	case Int, Float:
		var number = p.Eat(p.Peek())
		// try to parse number as integer. if it fails, as float
		var integer, err = strconv.Atoi(number.Lexeme)
		if err != nil {
			var float, err = strconv.ParseFloat(number.Lexeme, 64)
			if err != nil {
				interpreterError(fmt.Sprintf("Invalid number: %s", number.Lexeme), p.Tokens[p.Pos].Line, p.Tokens[p.Pos].Column, p.Tokens[p.Pos].Column+len(p.Tokens[p.Pos].Lexeme))
			}
			p.Code = append(p.Code, Instruction{
				Opcode: PushFloat,
				Args: []interface{}{
					float,
				},
				Line: number.Line,
				Col1: number.Column,
				Col2: len(number.Lexeme) + number.Column,
			})
		} else {
			p.Code = append(p.Code, Instruction{
				Opcode: PushInt,
				Args: []interface{}{
					integer,
				},
				Line: number.Line,
				Col1: number.Column,
				Col2: len(number.Lexeme) + number.Column,
			})
		}
	case String:
		var stringTok = p.Eat(String)
		p.Code = append(p.Code, Instruction{
			Opcode: PushString,
			Args: []interface{}{
				stringTok.Literal,
			},
			Line: stringTok.Line,
			Col1: stringTok.Column,
			Col2: len(stringTok.Lexeme) + stringTok.Column,
		})
	case True:
		var tok = p.Eat(True)
		p.Code = append(p.Code, Instruction{
			Opcode: PushBool,
			Args: []interface{}{
				true,
			},
			Line: tok.Line,
			Col1: tok.Column,
			Col2: len(tok.Lexeme) + tok.Column,
		})
	case False:
		var tok = p.Eat(False)
		p.Code = append(p.Code, Instruction{
			Opcode: PushBool,
			Args: []interface{}{
				false,
			},
			Line: tok.Line,
			Col1: tok.Column,
			Col2: len(tok.Lexeme) + tok.Column,
		})
	case LeftParen:
		var createTupleIndex = len(p.Code)
		p.Eat(LeftParen)
		p.Expression()
		if p.PeekType(Comma) {
			i, err := insert(p.Code, createTupleIndex, Instruction{
				Opcode: CreateTuple,
				Args:   []interface{}{},
			})
			if err != nil {
				interpreterError(err.Error(), p.Tokens[p.Pos].Line, p.Tokens[p.Pos].Column, p.Tokens[p.Pos].Column+len(p.Tokens[p.Pos].Lexeme))
			}
			p.Code = append(i, Instruction{
				Opcode: PushListItem,
				Args:   []interface{}{},
			})
			for !p.PeekType(RightParen) {
				p.Eat(Comma)
				if !p.PeekType(RightParen) {
					p.Expression()
					p.Code = append(p.Code, Instruction{
						Opcode: PushListItem,
						Args:   []interface{}{},
					})
				}
			}
		}
		p.Eat(RightParen)
	case Nil:
		var tok = p.Eat(Nil)
		p.Code = append(p.Code, Instruction{
			Opcode: PushNil,
			Args:   []interface{}{},
			Line:   tok.Line,
			Col1:   tok.Column,
			Col2:   len(tok.Lexeme) + tok.Column,
		})
	case Proto:
		p.Proto()
	case Fn:
		p.Fn()
	case LeftBrace:
		p.ListComprehension()
	case LeftBracket:
		p.List()
	case Map:
		p.Map()
	case Clone:
		var tok = p.Eat(Clone)
		p.Expression()
		p.Code = append(p.Code, Instruction{
			Opcode: CloneOp,
			Args:   []interface{}{},
			Line:   tok.Line,
			Col1:   tok.Column,
			Col2:   len(tok.Lexeme) + tok.Column,
		})
	default:
		interpreterError("Unexpected token", p.Tokens[p.Pos].Line, p.Tokens[p.Pos].Column, p.Tokens[p.Pos].Column+len(p.Tokens[p.Pos].Lexeme))
	}
	if negated {
		p.Code = append(p.Code, Instruction{
			Opcode: Negate,
			Args:   []interface{}{},
			Line:   p.Tokens[p.Pos].Line,
			Col1:   p.Tokens[p.Pos].Column,
			Col2:   len(p.Tokens[p.Pos].Lexeme) + p.Tokens[p.Pos].Column,
		})
	}
	if not {
		p.Code = append(p.Code, Instruction{
			Opcode: NotOp,
			Args:   []interface{}{},
			Line:   p.Tokens[p.Pos].Line,
			Col1:   p.Tokens[p.Pos].Column,
			Col2:   len(p.Tokens[p.Pos].Lexeme) + p.Tokens[p.Pos].Column,
		})
	}
}

func (p *Parser) Comparison() {
	p.Term2()
	for p.PeekType(Greater) || p.PeekType(Less) {
		var opType = p.Peek()
		p.Eat(opType)
		p.Term2()
		switch opType {
		case Greater:
			p.Code = append(p.Code, Instruction{
				Opcode: GreaterOp,
				Args:   []interface{}{},
				Line:   p.Tokens[p.Pos].Line,
				Col1:   p.Tokens[p.Pos].Column,
				Col2:   len(p.Tokens[p.Pos].Lexeme) + p.Tokens[p.Pos].Column,
			})
		case Less:
			p.Code = append(p.Code, Instruction{
				Opcode: LessOp,
				Args:   []interface{}{},
				Line:   p.Tokens[p.Pos].Line,
				Col1:   p.Tokens[p.Pos].Column,
				Col2:   len(p.Tokens[p.Pos].Lexeme) + p.Tokens[p.Pos].Column,
			})
		}
	}
}

func (p *Parser) Equality() {
	p.Comparison()
	for p.PeekType(EqualEqual) || p.PeekType(BangEqual) || p.PeekType(LessEqual) || p.PeekType(GreaterEqual) {
		var opType = p.Peek()
		var op = p.Eat(opType)
		p.Comparison()
		switch opType {
		case EqualEqual:
			p.Code = append(p.Code, Instruction{
				Opcode: EqualOp,
				Args:   []interface{}{},
				Line:   op.Line,
				Col1:   op.Column,
				Col2:   len(op.Lexeme) + op.Column,
			})
		case BangEqual:
			p.Code = append(p.Code, Instruction{
				Opcode: EqualOp,
				Args:   []interface{}{},
				Line:   op.Line,
				Col1:   op.Column,
				Col2:   len(op.Lexeme) + op.Column,
			})
			p.Code = append(p.Code, Instruction{
				Opcode: NotOp,
				Args:   []interface{}{},
				Line:   op.Line,
				Col1:   op.Column,
				Col2:   len(op.Lexeme) + op.Column,
			})
		case LessEqual:
			p.Code = append(p.Code, Instruction{
				Opcode: LessEqualOp,
				Args:   []interface{}{},
				Line:   op.Line,
				Col1:   op.Column,
				Col2:   len(op.Lexeme) + op.Column,
			})
		case GreaterEqual:
			p.Code = append(p.Code, Instruction{
				Opcode: GreaterEqualOp,
				Args:   []interface{}{},
				Line:   op.Line,
				Col1:   op.Column,
				Col2:   len(op.Lexeme) + op.Column,
			})
		}
	}
}

func (p *Parser) Expression() {
	p.Equality()
	for p.PeekType(And) || p.PeekType(Or) || p.PeekType(Question) {
		var opType = p.Peek()
		var op = p.Eat(opType)
		switch opType {
		case And:
			p.Equality()
			p.Code = append(p.Code, Instruction{
				Opcode: AndOp,
				Args:   []interface{}{},
				Line:   op.Line,
				Col1:   op.Column,
				Col2:   len(op.Lexeme) + op.Column,
			})
		case Or:
			p.Equality()
			p.Code = append(p.Code, Instruction{
				Opcode: OrOp,
				Args:   []interface{}{},
				Line:   op.Line,
				Col1:   op.Column,
				Col2:   len(op.Lexeme) + op.Column,
			})
		case Question:
			p.Equality()
			p.Code = append(p.Code, Instruction{
				Opcode: NullCoalesce,
				Args:   []interface{}{},
				Line:   op.Line,
				Col1:   op.Column,
				Col2:   len(op.Lexeme) + op.Column,
			})
		}
	}
}

func (p *Parser) Term() {
	p.Factor()
	for p.PeekType(Star) || p.PeekType(Slash) || p.PeekType(Mod) {
		var opType = p.Peek()
		var tok = p.Eat(opType)
		p.Factor()
		switch opType {
		case Star:
			p.Code = append(p.Code, Instruction{
				Opcode: Multiply,
				Args:   []interface{}{},
				Line:   tok.Line,
				Col1:   tok.Column,
				Col2:   len(tok.Lexeme) + tok.Column,
			})
		case Slash:
			p.Code = append(p.Code, Instruction{
				Opcode: Divide,
				Args:   []interface{}{},
				Line:   tok.Line,
				Col1:   tok.Column,
				Col2:   len(tok.Lexeme) + tok.Column,
			})
		case Mod:
			p.Code = append(p.Code, Instruction{
				Opcode: Modulo,
				Args:   []interface{}{},
				Line:   tok.Line,
				Col1:   tok.Column,
				Col2:   len(tok.Lexeme) + tok.Column,
			})
		}
	}
	if p.PeekType(LeftParen) {
		p.Eat(LeftParen)
		var argCount = 0
		if !p.PeekType(RightParen) {
			p.Expression()
			argCount++
			for p.PeekType(Comma) {
				p.Eat(Comma)
				p.Expression()
				argCount++
			}
		}
		p.Eat(RightParen)
		p.Code = append(p.Code, Instruction{
			Opcode: CallFunction,
			Args:   []interface{}{argCount},
			Line:   p.Tokens[p.Pos].Line,
			Col1:   p.Tokens[p.Pos].Column,
			Col2:   len(p.Tokens[p.Pos].Lexeme) + p.Tokens[p.Pos].Column,
		})
	}
	for p.PeekType(Colon) {
		var tok = p.Eat(Colon)
		if p.PeekType(LeftParen) {
			p.Eat(LeftParen)
			p.Expression()
			p.Eat(RightParen)
			if p.PeekType(Equal) || p.PeekType(PlusEqual) || p.PeekType(MinusEqual) || p.PeekType(StarEqual) || p.PeekType(SlashEqual) || p.PeekType(ModEqual) {
				var opType = p.Peek()
				var op = p.Eat(opType)
				switch opType {
				case Equal:
					p.Expression()
					p.Code = append(p.Code, Instruction{
						Opcode: AssignIndex,
						Args:   []interface{}{tok.Lexeme},
						Line:   op.Line,
						Col1:   op.Column,
						Col2:   len(op.Lexeme) + op.Column,
					})
				case PlusEqual:
					p.Code = append(p.Code, Instruction{
						Opcode: GetIndex,
						Args:   []interface{}{tok.Lexeme},
						Line:   op.Line,
						Col1:   op.Column,
						Col2:   len(op.Lexeme) + op.Column,
					})
					p.Expression()
					p.Code = append(p.Code, Instruction{
						Opcode: Add,
						Args:   []interface{}{},
						Line:   op.Line,
						Col1:   op.Column,
						Col2:   len(op.Lexeme) + op.Column,
					})
					p.Code = append(p.Code, Instruction{
						Opcode: AssignIndex,
						Args:   []interface{}{tok.Lexeme},
						Line:   op.Line,
						Col1:   op.Column,
						Col2:   len(op.Lexeme) + op.Column,
					})
				case MinusEqual:
					p.Code = append(p.Code, Instruction{
						Opcode: GetIndex,
						Args:   []interface{}{tok.Lexeme},
						Line:   op.Line,
						Col1:   op.Column,
						Col2:   len(op.Lexeme) + op.Column,
					})
					p.Expression()
					p.Code = append(p.Code, Instruction{
						Opcode: Subtract,
						Args:   []interface{}{},
						Line:   op.Line,
						Col1:   op.Column,
						Col2:   len(op.Lexeme) + op.Column,
					})
					p.Code = append(p.Code, Instruction{
						Opcode: AssignIndex,
						Args:   []interface{}{tok.Lexeme},
						Line:   op.Line,
						Col1:   op.Column,
						Col2:   len(op.Lexeme) + op.Column,
					})
				case StarEqual:
					p.Code = append(p.Code, Instruction{
						Opcode: GetIndex,
						Args:   []interface{}{tok.Lexeme},
						Line:   op.Line,
						Col1:   op.Column,
						Col2:   len(op.Lexeme) + op.Column,
					})
					p.Expression()
					p.Code = append(p.Code, Instruction{
						Opcode: Multiply,
						Args:   []interface{}{},
						Line:   op.Line,
						Col1:   op.Column,
						Col2:   len(op.Lexeme) + op.Column,
					})
					p.Code = append(p.Code, Instruction{
						Opcode: AssignIndex,
						Args:   []interface{}{tok.Lexeme},
						Line:   op.Line,
						Col1:   op.Column,
						Col2:   len(op.Lexeme) + op.Column,
					})
				case SlashEqual:
					p.Code = append(p.Code, Instruction{
						Opcode: GetIndex,
						Args:   []interface{}{tok.Lexeme},
						Line:   op.Line,
						Col1:   op.Column,
						Col2:   len(op.Lexeme) + op.Column,
					})
					p.Expression()
					p.Code = append(p.Code, Instruction{
						Opcode: Divide,
						Args:   []interface{}{},
						Line:   op.Line,
						Col1:   op.Column,
						Col2:   len(op.Lexeme) + op.Column,
					})
					p.Code = append(p.Code, Instruction{
						Opcode: AssignIndex,
						Args:   []interface{}{tok.Lexeme},
						Line:   op.Line,
						Col1:   op.Column,
						Col2:   len(op.Lexeme) + op.Column,
					})
				case ModEqual:
					p.Code = append(p.Code, Instruction{
						Opcode: GetIndex,
						Args:   []interface{}{tok.Lexeme},
						Line:   op.Line,
						Col1:   op.Column,
						Col2:   len(op.Lexeme) + op.Column,
					})
					p.Expression()
					p.Code = append(p.Code, Instruction{
						Opcode: Modulo,
						Args:   []interface{}{},
						Line:   op.Line,
						Col1:   op.Column,
						Col2:   len(op.Lexeme) + op.Column,
					})
					p.Code = append(p.Code, Instruction{
						Opcode: AssignIndex,
						Args:   []interface{}{tok.Lexeme},
						Line:   op.Line,
						Col1:   op.Column,
						Col2:   len(op.Lexeme) + op.Column,
					})
				}
			} else {
				p.Code = append(p.Code, Instruction{
					Opcode: GetIndex,
					Args:   []interface{}{},
					Line:   tok.Line,
					Col1:   tok.Column,
					Col2:   len(tok.Lexeme) + tok.Column,
				})
				p.Code = append(p.Code, Instruction{
					Opcode: Pop1,
					Args:   []interface{}{},
				})
				p.Code = append(p.Code, Instruction{
					Opcode: Pop1,
					Args:   []interface{}{},
				})
			}
		} else {
			var name = p.Eat(Identifier)
			if p.PeekType(Equal) || p.PeekType(PlusEqual) || p.PeekType(MinusEqual) || p.PeekType(StarEqual) || p.PeekType(SlashEqual) || p.PeekType(ModEqual) {
				var opType = p.Peek()
				var op = p.Eat(opType)
				switch opType {
				case Equal:
					p.Expression()
					p.Code = append(p.Code, Instruction{
						Opcode: AssignProperty,
						Args:   []interface{}{name.Lexeme},
						Line:   op.Line,
						Col1:   op.Column,
						Col2:   len(op.Lexeme) + op.Column,
					})
				case PlusEqual:
					p.Code = append(p.Code, Instruction{
						Opcode: Dup,
						Args:   []interface{}{},
					})
					p.Code = append(p.Code, Instruction{
						Opcode: GetProperty,
						Args:   []interface{}{name.Lexeme},
						Line:   op.Line,
						Col1:   op.Column,
						Col2:   len(op.Lexeme) + op.Column,
					})
					p.Expression()
					p.Code = append(p.Code, Instruction{
						Opcode: Add,
						Args:   []interface{}{},
						Line:   op.Line,
						Col1:   op.Column,
						Col2:   len(op.Lexeme) + op.Column,
					})
					p.Code = append(p.Code, Instruction{
						Opcode: AssignProperty,
						Args:   []interface{}{name.Lexeme},
						Line:   op.Line,
						Col1:   op.Column,
						Col2:   len(op.Lexeme) + op.Column,
					})
				case MinusEqual:
					p.Code = append(p.Code, Instruction{
						Opcode: Dup,
						Args:   []interface{}{},
					})
					p.Code = append(p.Code, Instruction{
						Opcode: GetProperty,
						Args:   []interface{}{name.Lexeme},
						Line:   op.Line,
						Col1:   op.Column,
						Col2:   len(op.Lexeme) + op.Column,
					})
					p.Expression()
					p.Code = append(p.Code, Instruction{
						Opcode: Subtract,
						Args:   []interface{}{},
						Line:   op.Line,
						Col1:   op.Column,
						Col2:   len(op.Lexeme) + op.Column,
					})
					p.Code = append(p.Code, Instruction{
						Opcode: AssignProperty,
						Args:   []interface{}{name.Lexeme},
						Line:   op.Line,
						Col1:   op.Column,
						Col2:   len(op.Lexeme) + op.Column,
					})
				case StarEqual:
					p.Code = append(p.Code, Instruction{
						Opcode: Dup,
						Args:   []interface{}{},
					})
					p.Code = append(p.Code, Instruction{
						Opcode: GetProperty,
						Args:   []interface{}{name.Lexeme},
						Line:   op.Line,
						Col1:   op.Column,
						Col2:   len(op.Lexeme) + op.Column,
					})
					p.Expression()
					p.Code = append(p.Code, Instruction{
						Opcode: Multiply,
						Args:   []interface{}{},
						Line:   op.Line,
						Col1:   op.Column,
						Col2:   len(op.Lexeme) + op.Column,
					})
					p.Code = append(p.Code, Instruction{
						Opcode: AssignProperty,
						Args:   []interface{}{name.Lexeme},
						Line:   op.Line,
						Col1:   op.Column,
						Col2:   len(op.Lexeme) + op.Column,
					})
				case SlashEqual:
					p.Code = append(p.Code, Instruction{
						Opcode: Dup,
						Args:   []interface{}{},
					})
					p.Code = append(p.Code, Instruction{
						Opcode: GetProperty,
						Args:   []interface{}{name.Lexeme},
						Line:   op.Line,
						Col1:   op.Column,
						Col2:   len(op.Lexeme) + op.Column,
					})
					p.Expression()
					p.Code = append(p.Code, Instruction{
						Opcode: Divide,
						Args:   []interface{}{},
						Line:   op.Line,
						Col1:   op.Column,
						Col2:   len(op.Lexeme) + op.Column,
					})
					p.Code = append(p.Code, Instruction{
						Opcode: AssignProperty,
						Args:   []interface{}{name.Lexeme},
						Line:   op.Line,
						Col1:   op.Column,
						Col2:   len(op.Lexeme) + op.Column,
					})
				case ModEqual:
					p.Code = append(p.Code, Instruction{
						Opcode: Dup,
						Args:   []interface{}{},
					})
					p.Code = append(p.Code, Instruction{
						Opcode: GetProperty,
						Args:   []interface{}{name.Lexeme},
						Line:   op.Line,
						Col1:   op.Column,
						Col2:   len(op.Lexeme) + op.Column,
					})
					p.Expression()
					p.Code = append(p.Code, Instruction{
						Opcode: Modulo,
						Args:   []interface{}{},
						Line:   op.Line,
						Col1:   op.Column,
						Col2:   len(op.Lexeme) + op.Column,
					})
					p.Code = append(p.Code, Instruction{
						Opcode: AssignProperty,
						Args:   []interface{}{name.Lexeme},
						Line:   op.Line,
						Col1:   op.Column,
						Col2:   len(op.Lexeme) + op.Column,
					})
				}
			} else {
				p.Code = append(p.Code, Instruction{
					Opcode: GetProperty,
					Args:   []interface{}{name.Lexeme},
					Line:   name.Line,
					Col1:   name.Column,
					Col2:   len(name.Lexeme) + name.Column,
				})
			}
		}
	}
}

func (p *Parser) Term2() {
	p.Term()
	for p.PeekType(Plus) || p.PeekType(Minus) {
		var opType = p.Peek()
		var tok = p.Eat(opType)
		p.Term()
		switch opType {
		case Plus:
			p.Code = append(p.Code, Instruction{
				Opcode: Add,
				Args:   []interface{}{},
				Line:   tok.Line,
				Col1:   tok.Column,
				Col2:   len(tok.Lexeme) + tok.Column,
			})
		case Minus:
			p.Code = append(p.Code, Instruction{
				Opcode: Subtract,
				Args:   []interface{}{},
				Line:   tok.Line,
				Col1:   tok.Column,
				Col2:   len(tok.Lexeme) + tok.Column,
			})
		case Or:
			p.Code = append(p.Code, Instruction{
				Opcode: OrOp,
				Args:   []interface{}{},
				Line:   tok.Line,
				Col1:   tok.Column,
				Col2:   len(tok.Lexeme) + tok.Column,
			})
		}
	}
}

func (p *Parser) Let() {
	var tok = p.Eat(Let)
	var immutable = p.PeekType(Const)
	if immutable {
		p.Eat(Const)
	}
	var name = p.Eat(Identifier).Lexeme
	p.Eat(Equal)
	p.Expression()
	p.Code = append(p.Code, Instruction{
		Opcode: DefineVariable,
		Args: []interface{}{
			name,
			immutable,
		},
		Line: tok.Line,
		Col1: tok.Column,
		Col2: len(tok.Lexeme) + tok.Column,
	})
}

func (p *Parser) Statement() {
	switch p.Peek() {
	case Let:
		p.Let()
	case For:
		p.For()
	case If, Unless:
		p.If()
	case Return:
		p.Return()
	case While, Until:
		p.While()
	case Is:
		p.Is()
	case Break:
		p.Break()
	case Continue:
		p.Continue()
	case Begin:
		p.Begin()
	default:
		p.Expression()
	}
}

func (p *Parser) Proto() {
	p.Eat(Proto)
	var inherits = false
	if p.PeekType(Colon) {
		p.Eat(Colon)
		p.Expression()
		inherits = true
	}
	p.Code = append(p.Code, Instruction{
		Opcode: PushEmptyProto,
		Args: []interface{}{
			inherits,
		},
	})
	for !p.PeekType(End) {
		p.Code = append(p.Code, Instruction{
			Opcode: Dup,
			Args:   []interface{}{},
		})
		var name = p.Eat(Identifier)
		p.Eat(Equal)
		p.Expression()
		p.Code = append(p.Code, Instruction{
			Opcode: AssignProperty,
			Args: []interface{}{
				name.Lexeme,
			},
			Line: name.Line,
			Col1: name.Column,
			Col2: len(name.Lexeme) + name.Column,
		})
	}
	p.Eat(End)
}

func (p *Parser) Fn() {
	var fnTok = p.Eat(Fn)
	var args = make([]interface{}, 0)
	if p.PeekType(LeftParen) {
		p.Eat(LeftParen)
		for !p.PeekType(RightParen) {
			var name = p.Eat(Identifier).Lexeme
			args = append(args, name)
			for p.PeekType(Comma) {
				p.Eat(Comma)
				name = p.Eat(Identifier).Lexeme
				args = append(args, name)
			}
		}
		p.Eat(RightParen)
	}
	p.Code = append(p.Code, Instruction{
		Opcode: MakeFunction,
		Args:   args,
		Line:   fnTok.Line,
		Col1:   fnTok.Column,
		Col2:   len(fnTok.Lexeme) + fnTok.Column,
	})
	if p.PeekType(LambdaArrow) {
		var tok = p.Eat(LambdaArrow)
		p.Expression()
		p.Code = append(p.Code, Instruction{
			Opcode: ReturnOp,
			Args:   []interface{}{},
			Line:   tok.Line,
			Col1:   tok.Column,
			Col2:   len(tok.Lexeme) + tok.Column,
		})
		p.Code = append(p.Code, Instruction{
			Opcode: EndFunction,
			Args:   []interface{}{},
		})
	} else {
		for !p.PeekType(End) {
			p.Statement()
		}
		var end = p.Eat(End)
		p.Code = append(p.Code, Instruction{
			Opcode: PushNil,
			Args:   []interface{}{},
		})
		p.Code = append(p.Code, Instruction{
			Opcode: ReturnOp,
			Args:   []interface{}{},
		})
		p.Code = append(p.Code, Instruction{
			Opcode: EndFunction,
			Args:   []interface{}{},
			Line:   end.Line,
			Col1:   end.Column,
			Col2:   len(end.Lexeme) + end.Column,
		})
	}
}

func (p *Parser) Map() {
	var tok = p.Eat(Map)
	p.Code = append(p.Code, Instruction{
		Opcode: CreateMap,
		Args:   []interface{}{},
		Line:   tok.Line,
		Col1:   tok.Column,
		Col2:   len(tok.Lexeme) + tok.Column,
	})
	p.Eat(LeftBrace)
	if !p.PeekType(RightBrace) {
		var str = p.Eat(String)
		p.Code = append(p.Code, Instruction{
			Opcode: PushString,
			Args:   []interface{}{str.Literal},
			Line:   str.Line,
			Col1:   str.Column,
			Col2:   len(str.Lexeme) + str.Column,
		})
		p.Eat(Colon)
		p.Expression()
		p.Code = append(p.Code, Instruction{
			Opcode: PushMapItem,
			Args:   []interface{}{},
			Line:   str.Line,
			Col1:   str.Column,
			Col2:   len(str.Lexeme) + str.Column,
		})
		for p.PeekType(Comma) {
			p.Eat(Comma)
			var str = p.Eat(String)
			p.Code = append(p.Code, Instruction{
				Opcode: PushString,
				Args:   []interface{}{str.Literal},
				Line:   str.Line,
				Col1:   str.Column,
				Col2:   len(str.Lexeme) + str.Column,
			})
			p.Eat(Colon)
			p.Expression()
			p.Code = append(p.Code, Instruction{
				Opcode: PushMapItem,
				Args:   []interface{}{},
			})
		}
	}
	p.Eat(RightBrace)
}

func (p *Parser) While() {
	p.Depth++
	p.Code = append(p.Code, Instruction{
		Opcode: PushScope,
		Args:   []interface{}{},
	})
	var loopStart = len(p.Code)
	var typeOfLoop = p.Peek()
	p.Eat(typeOfLoop)
	p.Expression()
	var jumpIndex = len(p.Code)
	if typeOfLoop == While {
		p.Code = append(p.Code, Instruction{
			Opcode: JumpIfFalse,
			Args:   []interface{}{0},
		})
	} else {
		p.Code = append(p.Code, Instruction{
			Opcode: JumpIf,
			Args:   []interface{}{0},
		})
	}
	for !p.PeekType(End) {
		p.Statement()
	}
	p.Eat(End)
	p.Code = append(p.Code, Instruction{
		Opcode: Jump,
		Args: []interface{}{
			loopStart - len(p.Code),
		},
	})
	var loopEnd = len(p.Code)
	p.Code = append(p.Code, Instruction{
		Opcode: PopScope,
		Args:   []interface{}{},
	})
	p.Code[jumpIndex].Args[0] = loopEnd - jumpIndex
	for i, instruction := range p.Code[loopStart:loopEnd] {
		i += loopStart // adjust for the offset of the loop start
		if instruction.Opcode == BreakOp && instruction.Args[0] == p.Depth {
			p.Code[i] = Instruction{
				Opcode: Jump,
				Args: []interface{}{
					loopEnd - i,
				},
				Line: instruction.Line,
				Col1: instruction.Col1,
				Col2: instruction.Col2,
			}
		} else if instruction.Opcode == ContinueOp && instruction.Args[0] == p.Depth {
			p.Code[i] = Instruction{
				Opcode: Jump,
				Args: []interface{}{
					loopStart - i,
				},
				Line: instruction.Line,
				Col1: instruction.Col1,
				Col2: instruction.Col2,
			}
		}
	}
	p.Depth--
}

func (p *Parser) If() {
	p.Code = append(p.Code, Instruction{
		Opcode: PushScope,
		Args:   []interface{}{},
	})
	var ifType = p.Peek()
	p.Eat(ifType)
	p.Expression()
	var jumpIndex = len(p.Code)
	if ifType == If {
		p.Code = append(p.Code, Instruction{
			Opcode: JumpIfFalse,
			Args:   []interface{}{0},
		})
	} else {
		p.Code = append(p.Code, Instruction{
			Opcode: JumpIf,
			Args:   []interface{}{0},
		})
	}
	for !p.PeekType(End) && !p.PeekType(Else) {
		p.Statement()
	}
	var jumpIndex2 = len(p.Code)
	p.Code = append(p.Code, Instruction{
		Opcode: Jump,
		Args:   []interface{}{0},
	})
	p.Code[jumpIndex].Args[0] = len(p.Code) - jumpIndex
	if p.PeekType(End) {
		p.Eat(End)
		p.Code[jumpIndex2].Args[0] = len(p.Code) - jumpIndex2
	} else {
		p.Eat(Else)
		for !p.PeekType(End) {
			p.Statement()
		}
		p.Eat(End)
		p.Code[jumpIndex2].Args[0] = len(p.Code) - jumpIndex2
	}
	p.Code = append(p.Code, Instruction{
		Opcode: PopScope,
		Args:   []interface{}{},
	})
}

func (p *Parser) Return() {
	var tok = p.Eat(Return)
	p.Expression()
	p.Code = append(p.Code, Instruction{
		Opcode: ReturnOp,
		Args:   []interface{}{},
		Line:   tok.Line,
		Col1:   tok.Column,
		Col2:   len(tok.Lexeme) + tok.Column,
	})
}

func (p *Parser) Break() {
	var tok = p.Eat(Break)
	p.Code = append(p.Code, Instruction{
		Opcode: BreakOp,
		Args:   []interface{}{p.Depth},
		Line:   tok.Line,
		Col1:   tok.Column,
		Col2:   len(tok.Lexeme) + tok.Column,
	})
}

func (p *Parser) Continue() {
	var tok = p.Eat(Continue)
	p.Code = append(p.Code, Instruction{
		Opcode: ContinueOp,
		Args:   []interface{}{p.Depth},
		Line:   tok.Line,
		Col1:   tok.Column,
		Col2:   len(tok.Lexeme) + tok.Column,
	})
}

func (p *Parser) For() {
	p.Depth++
	p.Code = append(p.Code, Instruction{
		Opcode: PushScope,
		Args:   []interface{}{},
	})
	var loopStart = len(p.Code)
	var tok = p.Eat(For)
	if p.PeekType(Item) {
		p.Eat(Item)
		var name = p.Eat(Identifier)
		p.Eat(In)
		p.Expression()
		p.Code = append(p.Code, Instruction{
			Opcode: PushNil,
			Args:   []interface{}{},
		})
		p.Code = append(p.Code, Instruction{
			Opcode: DefineVariable,
			Args:   []interface{}{name.Lexeme},
		})
		p.Code = append(p.Code, Instruction{
			Opcode: CreateIterator,
			Args:   []interface{}{},
		})
		var jumpIndex = len(p.Code)
		p.Code = append(p.Code, Instruction{
			Opcode: IsIteratorExhausted,
			Args:   []interface{}{},
		})
		var exitIndex = len(p.Code)
		p.Code = append(p.Code, Instruction{
			Opcode: JumpIf,
			Args:   []interface{}{0},
		})
		p.Code = append(p.Code, Instruction{
			Opcode: Next,
			Args:   []interface{}{},
			Line:   tok.Line,
			Col1:   tok.Column,
			Col2:   len(tok.Lexeme) + tok.Column,
		})
		p.Code = append(p.Code, Instruction{
			Opcode: AssignVariable,
			Args:   []interface{}{name.Lexeme},
		})
		p.Code = append(p.Code, Instruction{
			Opcode: SetMarker,
			Args:   []interface{}{p.Depth},
		})
		for !p.PeekType(End) {
			p.Statement()
		}
		p.Code = append(p.Code, Instruction{
			Opcode: PopMarker,
			Args:   []interface{}{p.Depth},
		})
		p.Code = append(p.Code, Instruction{
			Opcode: Jump,
			Args:   []interface{}{jumpIndex - len(p.Code)},
		})
		p.Code = append(p.Code, Instruction{
			Opcode: Pop,
			Args:   []interface{}{},
		})
		p.Eat(End)
		p.Code[exitIndex].Args[0] = len(p.Code) - exitIndex - 1
	} else {
		p.Statement()
		p.Eat(Pipe)
		var condIndex = len(p.Code)
		p.Expression()
		p.Eat(Pipe)
		p.Code = append(p.Code, Instruction{
			Opcode: JumpIfFalse,
			Args:   []interface{}{0},
		})
		var jumpIntoLoop = len(p.Code)
		p.Code = append(p.Code, Instruction{
			Opcode: Jump,
			Args:   []interface{}{0},
		})
		var iterIndex = len(p.Code)
		p.Statement()
		p.Code = append(p.Code, Instruction{
			Opcode: Jump,
			Args:   []interface{}{condIndex - len(p.Code)},
		})
		var bodyStart = len(p.Code)
		p.Code[jumpIntoLoop].Args[0] = bodyStart - jumpIntoLoop
		for !p.PeekType(End) {
			p.Statement()
		}
		p.Code = append(p.Code, Instruction{
			Opcode: Jump,
			Args:   []interface{}{iterIndex - len(p.Code)},
		})
		p.Code[iterIndex].Args[0] = len(p.Code) - iterIndex
		p.Eat(End)
	}
	var loopEnd = len(p.Code)
	p.Code = append(p.Code, Instruction{
		Opcode: PopScope,
		Args:   []interface{}{},
	})
	for i, instruction := range p.Code[loopStart:loopEnd] {
		i += loopStart // adjust for the offset of the loop start
		if instruction.Opcode == BreakOp && instruction.Args[0] == p.Depth {
			p.Code[i] = Instruction{
				Opcode: Jump,
				Args: []interface{}{
					loopEnd - i,
				},
				Line: instruction.Line,
				Col1: instruction.Col1,
				Col2: instruction.Col2,
			}
		} else if instruction.Opcode == ContinueOp && instruction.Args[0] == p.Depth {
			p.Code[i] = Instruction{
				Opcode: Jump,
				Args: []interface{}{
					loopStart - i,
				},
				Line: instruction.Line,
				Col1: instruction.Col1,
				Col2: instruction.Col2,
			}
		}
	}
	p.Depth--
}

func (p *Parser) Is() {
	p.Eat(Is)
	p.Expression()
	var endJumpIndexes = []int{}
	var loopStart = len(p.Code)
	for !p.PeekType(Else) || p.PeekType(End) {
		p.Code = append(p.Code, Instruction{
			Opcode: Dup,
			Args:   []interface{}{},
		})
		p.Expression()
		p.Code = append(p.Code, Instruction{
			Opcode: EqualOp,
			Args:   []interface{}{},
		})
		p.Eat(LambdaArrow)
		var jumpIndex = len(p.Code)
		p.Code = append(p.Code, Instruction{
			Opcode: JumpIfFalse,
			Args:   []interface{}{0},
		})
		p.Code = append(p.Code, Instruction{
			Opcode: Pop,
			Args:   []interface{}{},
		})
		for !p.PeekType(End) {
			p.Statement()
		}
		p.Eat(End)
		endJumpIndexes = append(endJumpIndexes, len(p.Code))
		p.Code = append(p.Code, Instruction{
			Opcode: Jump,
			Args:   []interface{}{0},
		})
		p.Code[jumpIndex].Args[0] = len(p.Code) - jumpIndex
	}
	if p.PeekType(Else) {
		p.Eat(Else)
		p.Code = append(p.Code, Instruction{
			Opcode: Pop,
			Args:   []interface{}{},
		})
		for !p.PeekType(End) {
			p.Statement()
		}
		p.Eat(End)
	}
	p.Eat(End)
	for i, instruction := range p.Code[loopStart:] {
		i += loopStart // adjust for the offset of the loop start
		if instruction.Opcode == Jump && instruction.Args[0] == 0 {
			p.Code[i].Args[0] = len(p.Code) - i
		}
	}
}

func (p *Parser) Begin() {
	p.Eat(Begin)
	p.Code = append(p.Code, Instruction{
		Opcode: PushScope,
		Args:   []interface{}{},
	})
	for !p.PeekType(End) {
		p.Statement()
	}
	p.Code = append(p.Code, Instruction{
		Opcode: PopScope,
		Args:   []interface{}{},
	})
	p.Eat(End)
}

func (p *Parser) ListComprehension() {
	var start = p.Eat(LeftBrace)
	p.Expression()
	p.Eat(Of)
	p.Expression()
	var end = p.Eat(RightBrace)
	p.Code = append(p.Code, Instruction{
		Opcode: MapFn,
		Args:   []interface{}{},
		Line:   start.Line,
		Col1:   start.Column,
		Col2:   end.Column,
	})
}

func (p *Parser) List() {
	var start = p.Eat(LeftBracket)
	p.Code = append(p.Code, Instruction{
		Opcode: CreateList,
		Args:   []interface{}{},
		Line:   start.Line,
		Col1:   start.Column,
		Col2:   start.Column + len(start.Lexeme),
	})
	if !p.PeekType(RightBracket) {
		p.Expression()
		p.Code = append(p.Code, Instruction{
			Opcode: PushListItem,
			Args:   []interface{}{},
		})
		for !p.PeekType(RightBracket) {
			p.Eat(Comma)
			p.Expression()
			p.Code = append(p.Code, Instruction{
				Opcode: PushListItem,
				Args:   []interface{}{},
			})
		}
	}
	if p.PeekType(Comma) {
		p.Eat(Comma)
	}
	p.Eat(RightBracket)
}
