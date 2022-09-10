package core

import (
	"errors"
	"fmt"
	"oasisgo/desktop"
	"strconv"
)

type OasisList = *[]any

type OasisMap = map[string]any

type VM struct {
	Scopes       *[]OasisEnvironment
	Exports      []string
	IP           int
	Instructions []Instruction
	ReturnValue  any
	Stack        [100]any
	StackPointer int
	JumpFlag     bool
	Finished     bool
	FunctionVM   bool
	ArgSlice     []any
}

func NewVM(instructions []Instruction, env *[]OasisEnvironment, functionVM bool) *VM {
	return &VM{
		Instructions: instructions,
		Scopes:       env,
		Exports:      []string{},
		Stack:        [100]any{},
		IP:           0,
		ReturnValue:  nil,
		JumpFlag:     true,
		Finished:     false,
		FunctionVM:   functionVM,
		ArgSlice:     nil,
	}
}

func (vm *VM) Push(value any) {
	vm.Stack[vm.StackPointer] = value
	vm.StackPointer++
}

func (vm *VM) Pop() any {
	vm.StackPointer--
	return vm.Stack[vm.StackPointer]
}

func (vm *VM) Run() {
	defer func() {
		if r := recover(); r != "InterpreterError" && r != nil {
			panic(r)
		}
	}()
	for !vm.Finished {
		if desktop.HadError {
			return
		}
		if vm.JumpFlag {
			vm.JumpFlag = false
		} else {
			vm.IP++
		}
		instruction := vm.Instructions[vm.IP]
		operations[instruction.Opcode](vm, instruction)
	}
}

var operations [LastInstruction]func(*VM, Instruction)

func init() {
	operations = [...]func(*VM, Instruction){
		PushInt: func(vm *VM, instruction Instruction) {
			vm.Push(instruction.Args[0])
		},
		PushFloat: func(vm *VM, instruction Instruction) {
			vm.Push(instruction.Args[0])
		},
		PushBool: func(vm *VM, instruction Instruction) {
			vm.Push(instruction.Args[0])
		},
		PushString: func(vm *VM, instruction Instruction) {
			vm.Push(instruction.Args[0])
		},
		PushChar: func(vm *VM, instruction Instruction) {
			vm.Push(instruction.Args[0])
		},
		PushNil: func(vm *VM, instruction Instruction) {
			vm.Push(nil)
		},
		FetchVariable: func(vm *VM, instruction Instruction) {
			value, err := (*vm.Scopes)[instruction.Args[1].(int)].Get(instruction.Args[0].(string))
			if err == nil {
				vm.Push(value)
			} else {
				desktop.InterpreterError(err.Error(), instruction.Line, instruction.Col1, instruction.Col2)
			}
		},
		DefineVariable: func(vm *VM, instruction Instruction) {
			err := (*vm.Scopes)[instruction.Args[1].(int)].Define(instruction.Args[0].(string), instruction.Args[2].(bool), vm.Pop())
			if err != nil {
				desktop.InterpreterError(err.Error(), instruction.Line, instruction.Col1, instruction.Col2)
			}
		},
		AssignVariable: func(vm *VM, instruction Instruction) {
			var item = vm.Pop()
			err := (*vm.Scopes)[instruction.Args[1].(int)].Assign(instruction.Args[0].(string), item)
			if err != nil {
				desktop.InterpreterError(err.Error(), instruction.Line, instruction.Col1, instruction.Col2)
			}
			vm.Push(item)
		},
		GetIndex: func(vm *VM, instruction Instruction) {
			var index = vm.Pop()
			var list = vm.Pop()
			switch t := list.(type) {
			case OasisList:
				if indexInt, ok := index.(int); ok {
					if indexInt < 0 || indexInt >= len(*t) {
						desktop.InterpreterError(fmt.Sprintf("Index %d out of bounds", indexInt), instruction.Line, instruction.Col1, instruction.Col2)
					}
					vm.Push(list)
					vm.Push(index)
					vm.Push((*t)[indexInt])
				} else {
					desktop.InterpreterError("Index must be an integer", instruction.Line, instruction.Col1, instruction.Col2)
				}
			case OasisMap:
				if indexString, ok := index.(string); ok {
					if _, ok := t[indexString]; !ok {
						desktop.InterpreterError("Index not found", instruction.Line, instruction.Col1, instruction.Col2)
					}
					vm.Push(list)
					vm.Push(index)
					vm.Push(t[indexString])
				} else {
					desktop.InterpreterError("Index must be a string", instruction.Line, instruction.Col1, instruction.Col2)
				}
			case Tuple:
				if indexInt, ok := index.(int); ok {
					if indexInt < 0 || indexInt >= len(t.Values) {
						desktop.InterpreterError(fmt.Sprintf("Index %d out of bounds", indexInt), instruction.Line, instruction.Col1, instruction.Col2)
					}
					vm.Push(list)
					vm.Push(index)
					vm.Push(t.Values[indexInt])
				} else {
					desktop.InterpreterError("Index must be an integer", instruction.Line, instruction.Col1, instruction.Col2)
				}
			case *Prototype:
				var fn, err = t.Get("__index")
				if err != nil {
					desktop.InterpreterError(err.Error(), instruction.Line, instruction.Col1, instruction.Col2)
				} else {
					if fn, ok := fn.(OasisCallable); ok {
						vm.Push(list)
						vm.Push(index)
						vm.Push(fn.Call(vm, []any{index}))
					} else {
						desktop.InterpreterError("__index not a function", instruction.Line, instruction.Col1, instruction.Col2)
					}
				}
			default:
				desktop.InterpreterError("Indexing not supported", instruction.Line, instruction.Col1, instruction.Col2)
			}
		},
		GetProperty: func(vm *VM, instruction Instruction) {
			var object = vm.Pop()
			switch t := object.(type) {
			case *Prototype:
				var val, err = t.Get(instruction.Args[0].(string))
				if err != nil {
					desktop.InterpreterError(err.Error(), instruction.Line, instruction.Col1, instruction.Col2)
				} else {
					vm.Push(val)
				}
			// TODO: Add support for other types
			default:
				desktop.InterpreterError(fmt.Sprintf("Property not supported on %t", object), instruction.Line, instruction.Col1, instruction.Col2)
			}
		},
		AssignIndex: func(vm *VM, instruction Instruction) {
			var value = vm.Pop()
			var index = vm.Pop()
			var list = vm.Pop()
			switch t := list.(type) {
			case OasisList:
				if indexInt, ok := index.(int); ok {
					if indexInt < 0 || indexInt >= len(*t) {
						*t = append(*t, value)
					} else {
						(*t)[indexInt] = value
					}
				} else {
					desktop.InterpreterError("Index must be an integer", instruction.Line, instruction.Col1, instruction.Col2)
				}
			case OasisMap:
				if indexString, ok := index.(string); ok {
					t[indexString] = value
				} else {
					desktop.InterpreterError("Index must be a string", instruction.Line, instruction.Col1, instruction.Col2)
				}
			case Tuple:
				desktop.InterpreterError("Tuples are immutable", instruction.Line, instruction.Col1, instruction.Col2)
			case *Prototype:
				var fn, err = t.Get("__index")
				if err != nil {
					desktop.InterpreterError(err.Error(), instruction.Line, instruction.Col1, instruction.Col2)
				} else {
					if fn, ok := fn.(OasisCallable); ok {
						fn.Call(vm, []any{index, value})
					} else {
						desktop.InterpreterError("__setIndex not a function", instruction.Line, instruction.Col1, instruction.Col2)
					}
				}
			}
			vm.Push(value)
		},
		AssignProperty: func(vm *VM, instruction Instruction) {
			var value = vm.Pop()
			var object = vm.Pop()
			switch t := object.(type) {
			case *Prototype:
				t.Set(instruction.Args[0].(string), value)
			default:
				desktop.InterpreterError("Property assignment not supported", instruction.Line, instruction.Col1, instruction.Col2)
			}
			vm.Push(object)
		},
		CallFunction: func(vm *VM, instruction Instruction) {
			if vm.ArgSlice == nil {
				vm.ArgSlice = make([]any, 255)
			}
			for i := instruction.Args[0].(int); i > 0; i-- {
				vm.ArgSlice[i-1] = vm.Pop()
			}
			var fn = vm.Pop()
			if callFn, ok := fn.(OasisCallable); ok {
				if callFn.Arity() != instruction.Args[0].(int) {
					desktop.InterpreterError(fmt.Sprintf("Function arity mismatch, expected %d args but got %d", callFn.Arity(), instruction.Args[0].(int)), instruction.Line, instruction.Col1, instruction.Col2)
				} else {
					vm.Push(callFn.Call(vm, vm.ArgSlice))
				}
			} else {
				desktop.InterpreterError(fmt.Sprintf("Not a function %T", fn), instruction.Line, instruction.Col1, instruction.Col2)
			}
		},
		TailCall: func(vm *VM, instruction Instruction) {
			if vm.ArgSlice == nil {
				vm.ArgSlice = make([]any, 255)
			}
			for i := instruction.Args[0].(int); i > 0; i-- {
				vm.ArgSlice[i-1] = vm.Pop()
			}
			var fn = vm.Pop()
			if callFn, ok := fn.(OasisCallable); ok {
				if callFn.Arity() != instruction.Args[0].(int) {
					desktop.InterpreterError("Function arity mismatch", instruction.Line, instruction.Col1, instruction.Col2)
				} else {
					if oasisFn, ok := callFn.(OasisFunction); ok {
						newScope := append(oasisFn.Closure, (*vm.Scopes)[len(*vm.Scopes)-1])
						vm.Scopes = &newScope
						vm.IP = 0
						vm.StackPointer = 0
						vm.Instructions = oasisFn.Body
						vm.JumpFlag = true
						for i := 0; i < instruction.Args[0].(int); i++ {
							(*vm.Scopes)[len(*vm.Scopes)-1].Values[oasisFn.Args[i]] = vm.ArgSlice[i]
						}
					} else {
						vm.Push(callFn.Call(vm, vm.ArgSlice))
					}
				}
			} else {
				desktop.InterpreterError(fmt.Sprintf("Not a function %T", fn), instruction.Line, instruction.Col1, instruction.Col2)
			}
		},
		SpawnFunction: func(vm *VM, instruction Instruction) {
			if vm.ArgSlice == nil {
				vm.ArgSlice = make([]any, 255)
			}
			for i := instruction.Args[0].(int); i > 0; i-- {
				vm.ArgSlice[i-1] = vm.Pop()
			}
			var fn = vm.Pop()
			if callFn, ok := fn.(OasisCallable); ok {
				if callFn.Arity() != instruction.Args[0].(int) {
					desktop.InterpreterError("Function arity mismatch", instruction.Line, instruction.Col1, instruction.Col2)
				} else {
					go callFn.Call(vm, vm.ArgSlice)
				}
			} else {
				desktop.InterpreterError(fmt.Sprintf("Not a function %T", fn), instruction.Line, instruction.Col1, instruction.Col2)
			}
		},
		SendOp: func(vm *VM, instruction Instruction) {
			var channel = vm.Pop()
			var value = vm.Pop()
			if ch, ok := channel.(chan any); ok {
				ch <- value
			} else {
				desktop.InterpreterError("Not a stream", instruction.Line, instruction.Col1, instruction.Col2)
			}
		},
		RecvOp: func(vm *VM, instruction Instruction) {
			var channel = vm.Pop()
			if ch, ok := channel.(chan any); ok {
				vm.Push(<-ch)
			} else {
				desktop.InterpreterError("Not a stream", instruction.Line, instruction.Col1, instruction.Col2)
			}
		},
		PushEmptyProto: func(vm *VM, instruction Instruction) {
			var inherits = &BasePrototype
			if instruction.Args[0].(bool) {
				inherits = vm.Pop().(*Prototype)
			}
			vm.Push(NewPrototype(inherits))
		},
		MakeFunction: func(vm *VM, instruction Instruction) {
			var args = make([]string, len(instruction.Args))
			for i := 0; i < len(instruction.Args); i++ {
				args[i] = instruction.Args[i].(string)
			}
			var function = OasisFunction{
				Args:    args,
				Body:    []Instruction{},
				Closure: *vm.Scopes,
			}
			var level = 0
			var index = 0
			for i, instruction := range vm.Instructions[vm.IP+1:] {
				i += vm.IP
				if instruction.Opcode == MakeFunction {
					level++
					function.Body = append(function.Body, instruction)
				} else if instruction.Opcode == EndFunction {
					if level != 0 {
						level--
						function.Body = append(function.Body, instruction)
					} else {
						break
					}
				} else {
					function.Body = append(function.Body, instruction)
				}
				index = i
			}
			vm.IP = index + 1
			vm.Push(function)
		},
		ReturnOp: func(vm *VM, instruction Instruction) {
			if !vm.FunctionVM {
				desktop.InterpreterError("Return outside of function", instruction.Line, instruction.Col1, instruction.Col2)
			}
			vm.ReturnValue = vm.Pop()
			vm.Finished = true
		},
		Jump: func(vm *VM, instruction Instruction) {
			vm.IP += instruction.Args[0].(int)
			vm.JumpFlag = true
		},
		JumpIf: func(vm *VM, instruction Instruction) {
			if vm.Truthy(vm.Pop()) {
				vm.IP += instruction.Args[0].(int)
				vm.JumpFlag = true
			}
		},
		JumpIfFalse: func(vm *VM, instruction Instruction) {
			if !vm.Truthy(vm.Pop()) {
				vm.IP += instruction.Args[0].(int)
				vm.JumpFlag = true
			}
		},
		EqualOp: func(vm *VM, instruction Instruction) {
			var a = vm.Pop()
			var b = vm.Pop()
			vm.Push(vm.Equal(b, a))
		},
		LessOp: func(vm *VM, instruction Instruction) {
			var a = vm.Pop()
			var b = vm.Pop()
			vm.Push(vm.Compare(b, a) < 0)
		},
		GreaterOp: func(vm *VM, instruction Instruction) {
			var a = vm.Pop()
			var b = vm.Pop()
			vm.Push(vm.Compare(b, a) > 0)
		},
		LessEqualOp: func(vm *VM, instruction Instruction) {
			var a = vm.Pop()
			var b = vm.Pop()
			vm.Push(vm.Compare(b, a) <= 0)
		},
		GreaterEqualOp: func(vm *VM, instruction Instruction) {
			var a = vm.Pop()
			var b = vm.Pop()
			vm.Push(vm.Compare(b, a) >= 0)
		},
		Add: func(vm *VM, instruction Instruction) {
			var a = vm.Pop()
			var b = vm.Pop()
			add, err := vm.Add(b, a)
			if err == nil {
				vm.Push(add)
			} else {
				desktop.InterpreterError(err.Error(), instruction.Line, instruction.Col1, instruction.Col2)
			}
		},
		Subtract: func(vm *VM, instruction Instruction) {
			var a = vm.Pop()
			var b = vm.Pop()
			subtract, err := vm.Subtract(b, a)
			if err == nil {
				vm.Push(subtract)
			} else {
				desktop.InterpreterError(err.Error(), instruction.Line, instruction.Col1, instruction.Col2)
			}
		},
		Multiply: func(vm *VM, instruction Instruction) {
			var a = vm.Pop()
			var b = vm.Pop()
			multiply, err := vm.Multiply(b, a)
			if err == nil {
				vm.Push(multiply)
			} else {
				desktop.InterpreterError(err.Error(), instruction.Line, instruction.Col1, instruction.Col2)
			}
		},
		Divide: func(vm *VM, instruction Instruction) {
			var a = vm.Pop()
			var b = vm.Pop()
			divide, err := vm.Divide(b, a)
			if err == nil {
				vm.Push(divide)
			} else {
				desktop.InterpreterError(err.Error(), instruction.Line, instruction.Col1, instruction.Col2)
			}
		},
		Modulo: func(vm *VM, instruction Instruction) {
			var a = vm.Pop()
			var b = vm.Pop()
			modulo, err := vm.Modulo(b, a)
			if err == nil {
				vm.Push(modulo)
			} else {
				desktop.InterpreterError(err.Error(), instruction.Line, instruction.Col1, instruction.Col2)
			}
		},
		Negate: func(vm *VM, instruction Instruction) {
			var a = vm.Pop()
			switch a.(type) {
			case int:
				vm.Push(-a.(int))
			case float64:
				vm.Push(-a.(float64))
			default:
				desktop.InterpreterError("Negate not supported for this type", instruction.Line, instruction.Col1, instruction.Col2)
			}
		},
		NotOp: func(vm *VM, instruction Instruction) {
			var a = vm.Pop()
			vm.Push(!vm.Truthy(a))
		},
		AndOp: func(vm *VM, instruction Instruction) {
			var a = vm.Pop()
			var b = vm.Pop()
			vm.Push(vm.Truthy(b) && vm.Truthy(a))
		},
		OrOp: func(vm *VM, instruction Instruction) {
			var a = vm.Pop()
			var b = vm.Pop()
			vm.Push(vm.Truthy(b) || vm.Truthy(a))
		},
		CloneOp: func(vm *VM, instruction Instruction) {
			var a = vm.Pop()
			ok, err := vm.Clone(a)
			if err == nil {
				vm.Push(ok)
			} else {
				desktop.InterpreterError(err.Error(), instruction.Line, instruction.Col1, instruction.Col2)
			}
		},
		CreateList: func(vm *VM, instruction Instruction) {
			vm.Push(CreateOasisList([]any{}))
		},
		PushListItem: func(vm *VM, instruction Instruction) {
			var item = vm.Pop()
			var list = vm.Pop()
			switch t := list.(type) {
			case OasisList:
				*t = append(*(list.(OasisList)), item)
			case Tuple:
				t.Append(item)
				list = t
			default:
				desktop.InterpreterError("!!INTERNAL!!PLEASE REPORT THIS AS BUG!! PushListItem not supported for this type", instruction.Line, instruction.Col1, instruction.Col2)
			}
			vm.Push(list)
		},
		CreateTuple: func(vm *VM, instruction Instruction) {
			vm.Push(CreateOasisTuple([]any{}))
		},
		CreateMap: func(vm *VM, instruction Instruction) {
			vm.Push(make(OasisMap))
		},
		PushMapItem: func(vm *VM, instruction Instruction) {
			var value = vm.Pop()
			var key = vm.Pop()
			var mapObj = vm.Pop()
			switch t := mapObj.(type) {
			case OasisMap:
				t[key.(string)] = value
			default:
				desktop.InterpreterError("!!INTERNAL!!PLEASE REPORT THIS AS BUG!! PushMapItem not supported for this type", instruction.Line, instruction.Col1, instruction.Col2)
			}
			vm.Push(mapObj)
		},
		MapFn: func(vm *VM, instruction Instruction) {
			var obj = vm.Pop()
			var fn = vm.Pop()
			var result = CreateOasisList([]any{})
			if fn, ok := fn.(OasisCallable); ok {
				switch t := obj.(type) {
				case OasisMap:
					for k, v := range t {
						*result = append(*result, fn.Call(vm, []any{CreateOasisTuple([]any{k, v})}))
					}
				case Tuple:
					for _, v := range t.Values {
						*result = append(*result, fn.Call(vm, []any{v}))
					}
				case OasisList:
					for _, v := range *t {
						*result = append(*result, fn.Call(vm, []any{v}))
					}
				case *Prototype:
					if val, err := t.Get("__iter"); err == nil {
						if val, ok := val.(OasisCallable); ok {
							var i = 0
							var iter = val.Call(vm, []any{i})
							for ; !(iter == nil); iter = val.Call(vm, []any{i}) {
								i += 1
							}
						}
					}
				default:
					desktop.InterpreterError("Not an iterable object", instruction.Line, instruction.Col1, instruction.Col2)
				}
			}
			vm.Push(result)
		},
		PushScope: func(vm *VM, instruction Instruction) {
			newScope := append(*vm.Scopes, NewEnvironment())
			vm.Scopes = &newScope
		},
		PopScope: func(vm *VM, instruction Instruction) {
			newScope := (*vm.Scopes)[:len(*vm.Scopes)-1]
			vm.Scopes = &newScope
		},
		EndProgram: func(vm *VM, instruction Instruction) {
			vm.Finished = true
		},
		Dup: func(vm *VM, instruction Instruction) {
			var a = vm.Pop()
			vm.Push(a)
			vm.Push(a)
		},
		CreateIterator: func(vm *VM, instruction Instruction) {
			var obj = vm.Pop()
			vm.Push(NewOasisIterator(obj))
		},
		Next: func(vm *VM, instruction Instruction) {
			var test = vm.Pop()
			if iterator, ok := test.(OasisIterator); ok {
				var err, result = iterator.Next(vm)
				if err != nil {
					desktop.InterpreterError(err.Error(), instruction.Line, instruction.Col1, instruction.Col2)
				} else {
					vm.Push(iterator)
					vm.Push(result)
				}
			} else {
				desktop.InterpreterError(fmt.Sprintf("Not an iterator %T", test), instruction.Line, instruction.Col1, instruction.Col2)
			}
		},
		IsIteratorExhausted: func(vm *VM, instruction Instruction) {
			var test = vm.Pop()
			if iterator, ok := test.(OasisIterator); ok {
				vm.Push(iterator)
				vm.Push(iterator.Exhausted)
			} else {
				desktop.InterpreterError(fmt.Sprintf("Not an iterator %T", test), instruction.Line, instruction.Col1, instruction.Col2)
			}
		},
		SetMarker: func(vm *VM, instruction Instruction) {
			vm.Push(StackMarker{instruction.Args[0].(int)})
		},
		PopMarker: func(vm *VM, instruction Instruction) {
			for {
				var item = vm.Pop()
				if marker, ok := item.(StackMarker); ok {
					if marker.Value == instruction.Args[0].(int) {
						break
					}
				}
			}
		},
		Pop: func(vm *VM, instruction Instruction) {
			vm.Pop()
		},
		Pop1: func(vm *VM, instruction Instruction) {
			var a = vm.Pop()
			vm.Pop()
			vm.Push(a)
		},
		Pop2: func(vm *VM, instruction Instruction) {
			var a = vm.Pop()
			var b = vm.Pop()
			vm.Pop()
			vm.Push(a)
			vm.Push(b)
		},
		NullCoalesce: func(vm *VM, instruction Instruction) {
			var a = vm.Pop()
			var b = vm.Pop()
			if b == nil {
				vm.Push(a)
			} else {
				vm.Push(b)
			}
		},
		EndFunction: func(vm *VM, instruction Instruction) {
			// do nothing
		},
		ExportOp: func(vm *VM, instruction Instruction) {
			for _, name := range instruction.Args {
				vm.Exports = append(vm.Exports, name.(string))
			}
		},
	}
}

type StackMarker struct {
	Value int
}

func CreateOasisList(values []any) OasisList {
	return &values
}

func CreateOasisTuple(values []any) Tuple {
	return Tuple{Values: values}
}

func (vm *VM) Equal(a, b any) bool {
	switch a.(type) {
	case float64:
		switch b.(type) {
		case int:
			return a.(float64) == float64(b.(int))
		case float64:
			return a.(float64) == b.(float64)
		case uint8:
			return a.(float64) == float64(b.(uint8))
		}
	case uint8:
		switch b.(type) {
		case int:
			return a.(uint8) == uint8(b.(int))
		case float64:
			return float64(a.(uint8)) == b.(float64)
		case uint8:
			return a.(uint8) == b.(uint8)
		}
	case string:
		switch b.(type) {
		case string:
			return a.(string) == b.(string)
		}
	case *Prototype:
		var val, err = a.(*Prototype).Get("__equal")
		if err == nil {
			if val, ok := val.(OasisCallable); ok {
				return val.Call(vm, []any{b}) == true
			}
		}
	case int:
		switch b.(type) {
		case int:
			return a.(int) == b.(int)
		case float64:
			return float64(a.(int)) == b.(float64)
		case uint8:
			return uint8(a.(int)) == b.(uint8)
		}
	case OasisList:
		switch b.(type) {
		case OasisList:
			var aList = a.(OasisList)
			var bList = b.(OasisList)
			if len(*aList) != len(*bList) {
				return false
			}
			for i := 0; i < len(*aList); i++ {
				if !vm.Equal((*aList)[i], (*bList)[i]) {
					return false
				}
			}
		}
	case Tuple:
		switch b.(type) {
		case Tuple:
			var aTuple = a.(Tuple)
			var bTuple = b.(Tuple)
			if len(aTuple.Values) != len(bTuple.Values) {
				return false
			}
			for i := 0; i < len(aTuple.Values); i++ {
				if !vm.Equal(aTuple.Values[i], bTuple.Values[i]) {
					return false
				}
			}
		}
	case OasisMap:
		switch b.(type) {
		case OasisMap:
			var aMap = a.(OasisMap)
			var bMap = b.(OasisMap)
			if len(aMap) != len(bMap) {
				return false
			}
			for k, v := range aMap {
				if !vm.Equal(v, bMap[k]) {
					return false
				}
			}
		}
	case bool:
		switch b.(type) {
		case bool:
			return a.(bool) == b.(bool)
		}
	}
	return false
}

func (vm *VM) Compare(a, b any) int {
	switch a.(type) {
	case float64:
		switch b.(type) {
		case int:
			if a.(float64) < float64(b.(int)) {
				return -1
			} else if a.(float64) > float64(b.(int)) {
				return 1
			} else {
				return 0
			}
		}
	case int:
		switch b.(type) {
		case int:
			if a.(int) < b.(int) {
				return -1
			} else if a.(int) > b.(int) {
				return 1
			} else {
				return 0
			}
		}
	case uint8:
		switch b.(type) {
		case int:
			if a.(uint8) < uint8(b.(int)) {
				return -1
			} else if a.(uint8) > uint8(b.(int)) {
				return 1
			} else {
				return 0
			}
		}
	}
	return 0
}

func (vm *VM) Add(a, b any) (any, error) {
	switch a.(type) {
	case float64:
		switch b.(type) {
		case int:
			return a.(float64) + float64(b.(int)), nil
		case float64:
			return a.(float64) + b.(float64), nil
		case uint8:
			return a.(float64) + float64(b.(uint8)), nil
		}
	case int:
		switch b.(type) {
		case int:
			return a.(int) + b.(int), nil
		case float64:
			return float64(a.(int)) + b.(float64), nil
		case uint8:
			return uint8(a.(int)) + b.(uint8), nil
		}
	case string:
		switch b.(type) {
		case string:
			return a.(string) + b.(string), nil
		case int:
			return a.(string) + strconv.Itoa(b.(int)), nil
		case float64:
			return a.(string) + strconv.FormatFloat(b.(float64), 'f', -1, 64), nil
		case uint8:
			return a.(string) + string(b.(uint8)), nil
		}
	case *Prototype:
		// add overload
		var val, err = a.(*Prototype).Get("__add")
		if err == nil {
			if val, ok := val.(OasisCallable); ok {
				return val.Call(vm, []any{b}), nil
			}
		} else {
			return nil, err
		}
	}
	return nil, errors.New("Unsupported type for add")
}

func (vm *VM) Subtract(a, b any) (any, error) {
	switch a.(type) {
	case float64:
		switch b.(type) {
		case int:
			return a.(float64) - float64(b.(int)), nil
		case float64:
			return a.(float64) - b.(float64), nil
		case uint8:
			return a.(float64) - float64(b.(uint8)), nil
		}
	case int:
		switch b.(type) {
		case int:
			return a.(int) - b.(int), nil
		case float64:
			return float64(a.(int)) - b.(float64), nil
		case uint8:
			return uint8(a.(int)) - b.(uint8), nil
		}
	case uint8:
		switch b.(type) {
		case int:
			return a.(uint8) - uint8(b.(int)), nil
		case float64:
			return float64(a.(uint8)) - b.(float64), nil
		case uint8:
			return a.(uint8) - b.(uint8), nil
		}
	case *Prototype:
		// div overload
		var val, err = a.(*Prototype).Get("__sub")
		if err == nil {
			if val, ok := val.(OasisCallable); ok {
				return val.Call(vm, []any{b}), nil
			}
		} else {
			return nil, err
		}
	}
	return nil, errors.New("Unsupported type for subtract")
}

func (vm *VM) Multiply(a, b any) (any, error) {
	switch a.(type) {
	case float64:
		switch b.(type) {
		case int:
			return a.(float64) * float64(b.(int)), nil
		case float64:
			return a.(float64) * b.(float64), nil
		case uint8:
			return a.(float64) * float64(b.(uint8)), nil
		}
	case int:
		switch b.(type) {
		case int:
			return a.(int) * b.(int), nil
		case float64:
			return float64(a.(int)) * b.(float64), nil
		case uint8:
			return uint8(a.(int)) * b.(uint8), nil
		}
	case uint8:
		switch b.(type) {
		case int:
			return a.(uint8) * uint8(b.(int)), nil
		case float64:
			return float64(a.(uint8)) * b.(float64), nil
		case uint8:
			return a.(uint8) * b.(uint8), nil
		}
	case *Prototype:
		// mul overload
		var val, err = a.(*Prototype).Get("__mul")
		if err == nil {
			if val, ok := val.(OasisCallable); ok {
				return val.Call(vm, []any{b}), nil
			}
		} else {
			return nil, err
		}
	}
	return nil, errors.New("Unsupported type for multiply")
}

func (vm *VM) Divide(a, b any) (any, error) {
	switch a.(type) {
	case float64:
		switch b.(type) {
		case int:
			return a.(float64) / float64(b.(int)), nil
		case float64:
			return a.(float64) / b.(float64), nil
		case uint8:
			return a.(float64) / float64(b.(uint8)), nil
		}
	case int:
		switch b.(type) {
		case int:
			return a.(int) / b.(int), nil
		case float64:
			return float64(a.(int)) / b.(float64), nil
		case uint8:
			return uint8(a.(int)) / b.(uint8), nil
		}
	case uint8:
		switch b.(type) {
		case int:
			return a.(uint8) / uint8(b.(int)), nil
		case float64:
			return float64(a.(uint8)) / b.(float64), nil
		case uint8:
			return a.(uint8) / b.(uint8), nil
		}
	case *Prototype:
		// div overload
		var val, err = a.(*Prototype).Get("__div")
		if err == nil {
			if val, ok := val.(OasisCallable); ok {
				return val.Call(vm, []any{b}), nil
			}
		} else {
			return nil, err
		}
	}
	return nil, errors.New("Unsupported type for divide")
}

func (vm *VM) Modulo(a, b any) (any, error) {
	switch a.(type) {
	case uint8:
		switch b.(type) {
		case int:
			return a.(uint8) % uint8(b.(int)), nil
		case uint8:
			return a.(uint8) % b.(uint8), nil
		}
	case int:
		switch b.(type) {
		case int:
			return a.(int) % b.(int), nil
		case uint8:
			return uint8(a.(int)) % b.(uint8), nil
		}
	case *Prototype:
		// mod overload
		var val, err = a.(*Prototype).Get("__mod")
		if err == nil {
			if val, ok := val.(OasisCallable); ok {
				return val.Call(vm, []any{b}), nil
			}
		} else {
			return nil, err
		}
	}
	return nil, errors.New("Unsupported type for modulo")
}

func (vm *VM) Truthy(a any) bool {
	switch a.(type) {
	case float64:
		return a.(float64) != 0
	case uint8:
		return a.(uint8) != 0
	case string:
		return a.(string) != ""
	case *Prototype:
		return true
	case OasisList:
		return len(*(a.(OasisList))) > 0
	case int:
		return a.(int) != 0
	case Tuple:
		return len(a.(Tuple).Values) > 0
	case OasisMap:
		return len(a.(OasisMap)) > 0
	case OasisCallable:
		return true
	case bool:
		return a.(bool)
	case nil:
		return false
	}
	return false
}

func (vm *VM) Clone(a any) (any, error) {
	switch a.(type) {
	case float64:
		return a.(float64), nil
	case uint8:
		return a.(uint8), nil
	case string:
		return a.(string), nil
	case *Prototype:
		// clone the prototype
		var proto = a.(*Prototype)
		if ok, err := proto.Get("__new"); err == nil {
			if fn, ok := ok.(OasisCallable); ok {
				return fn.Call(vm, []any{}), nil
			} else {
				return nil, errors.New("Cannot clone prototype")
			}
		}
		var clone = &Prototype{}
		clone.Inherited = proto.Inherited
		for k, v := range proto.Body {
			ok, err := vm.Clone(v)
			if err != nil {
				return nil, err
			}
			clone.Body[k] = ok
		}
		return clone, nil
	case OasisList:
		// clone the list
		var list = a.(OasisList)
		var clone = make([]any, len(*list))
		for i, v := range *list {
			ok, err := vm.Clone(v)
			if err != nil {
				return nil, err
			}
			clone[i] = ok
		}
		return CreateOasisList(clone), nil
	case int:
		return a.(int), nil
	case Tuple:
		// tuples are immutable, this doesn't matter
		return a.(Tuple), nil
	case OasisMap:
		// clone the map
		var map1 = a.(OasisMap)
		var clone = OasisMap{}
		for k, v := range map1 {
			ok, err := vm.Clone(v)
			if err != nil {
				return nil, err
			}
			clone[k] = ok
		}
		return clone, nil
	case bool:
		return a.(bool), nil
	case chan any:
		return make(chan any), nil
	case nil:
		return nil, nil
	}
	return nil, errors.New("Unsupported type for clone")
}
