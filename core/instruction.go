package core

import "fmt"

type Opcode uint8

//go:generate stringer -type=Opcode
const (
	PushInt Opcode = iota
	PushFloat
	PushBool
	PushString
	PushChar
	PushByte
	PushNil
	FetchVariable
	DefineVariable // arg 1: name of variable
	AssignVariable // arg 1: name of variable
	GetIndex
	GetProperty
	AssignIndex
	AssignProperty // arg 1: name of property
	CallFunction
	PushEmptyProto // arg 1: bool, inherit flag
	MakeFunction   // arg 1 .. x: arg names
	EndFunction
	ReturnOp
	Jump        // arg 1: int, amount forward to jump
	JumpIf      // arg 1: int, amount forward to jump
	JumpIfFalse // arg 1: int, amount forward to jump
	EqualOp
	LessOp
	LessEqualOp
	GreaterOp
	GreaterEqualOp
	Add
	Subtract
	Multiply
	Divide
	Modulo
	Negate
	NotOp
	AndOp
	OrOp
	CloneOp
	CreateList
	PushListItem
	CreateTuple
	CreateMap
	CreateIterator
	PushMapItem
	MapFn
	SetMarker
	PopMarker
	Next
	IsIteratorExhausted
	PushScope
	PopScope
	BreakOp
	ContinueOp
	EndProgram
	Dup
	Pop
	Pop1
	Pop2
	NullCoalesce
	TailCall
	SpawnFunction
	SendOp
	RecvOp
	ExportOp
	LastInstruction // never use - needed for getting the maximum index
)

type Instruction struct {
	Opcode Opcode
	Args   []any
	Line   int
	Col1   int
	Col2   int
}

func (i Instruction) String() string {
	var s = fmt.Sprintf("%s", i.Opcode.String())
	for _, arg := range i.Args {
		s += fmt.Sprintf(" %v", arg)
	}
	return s
}
