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
		interpreter_error(fmt.Sprintf("Expected %s", t), p.Tokens[p.Pos].Line, p.Tokens[p.Pos].Column, p.Tokens[p.Pos].Column+len(p.Tokens[p.Pos].Lexeme))
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
				interpreter_error(fmt.Sprintf("Invalid number: %s", number.Lexeme), p.Tokens[p.Pos].Line, p.Tokens[p.Pos].Column, p.Tokens[p.Pos].Column+len(p.Tokens[p.Pos].Lexeme))
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
		p.Eat(LeftParen)
		p.Expression()
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
		p.MapOrListComprehension()
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
		interpreter_error("Unexpected token", p.Tokens[p.Pos].Line, p.Tokens[p.Pos].Column, p.Tokens[p.Pos].Column+len(p.Tokens[p.Pos].Lexeme))
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

func (p *Parser) Term() {
	p.Factor()
	for p.PeekType(Star) || p.PeekType(Slash) || p.PeekType(Mod) || p.PeekType(And) {
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
		case And:
			p.Code = append(p.Code, Instruction{
				Opcode: AndOp,
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
}

func (p *Parser) Expression() {
	p.Term()
	for p.PeekType(Plus) || p.PeekType(Minus) || p.PeekType(Or) {
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
	case Test:
		p.Test()
	case Break:
		p.Break()
	case Continue:
		p.Continue()
	case Rel:
		p.Rel()
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

func (p *Parser) MapOrListComprehension() {

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

}

func (p *Parser) Is() {

}

func (p *Parser) Test() {

}

func (p *Parser) Rel() {

}

func (p *Parser) Begin() {

}
