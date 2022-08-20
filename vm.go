package main

import (
	"errors"
	"fmt"
	"strconv"
)

type OasisList *[]interface{}
type OasisMap map[string]interface{}

type VM struct {
	Instructions      []Instruction
	Environment       *OasisEnvironment
	Stack             []interface{}
	StackPointer      int
	IP                int
	ReturnValue       interface{}
	JumpFlag          bool
	IteratorExhausted bool
	Finished          bool
	FunctionVM        bool
}

func NewVM(instructions []Instruction, env *OasisEnvironment, functionVM bool) *VM {
	var stackSize = 0
	if functionVM {
		stackSize = 5
	} else {
		stackSize = 100
	}
	return &VM{
		Instructions:      instructions,
		Environment:       env,
		Stack:             make([]interface{}, 0, stackSize),
		IP:                0,
		StackPointer:      -1,
		ReturnValue:       nil,
		JumpFlag:          true,
		IteratorExhausted: false,
		Finished:          false,
		FunctionVM:        functionVM,
	}
}

func (vm *VM) Push(value interface{}) {
	/* vm.StackPointer++
	vm.Stack[vm.StackPointer] = value*/
	vm.Stack = append(vm.Stack, value)
}

func (vm *VM) Pop() interface{} {
	/*var value = vm.Stack[vm.StackPointer]
	vm.StackPointer--
	return value*/
	var value = vm.Stack[len(vm.Stack)-1]
	vm.Stack = vm.Stack[:len(vm.Stack)-1]
	return value
}

func (vm *VM) Run() {
	for !vm.Finished {
		if hadError {
			return
		}
		if vm.JumpFlag {
			vm.JumpFlag = false
		} else {
			vm.IP++
		}
		instruction := vm.Instructions[vm.IP]
		vm.Execute(instruction)
	}
}

func (vm *VM) Execute(instruction Instruction) {
	switch instruction.Opcode {
	case PushInt:
		fallthrough
	case PushFloat:
		fallthrough
	case PushBool:
		fallthrough
	case PushString:
		fallthrough
	case PushChar:
		vm.Push(instruction.Args[0])
	case PushNil:
		vm.Push(nil)
	case FetchVariable:
		if value, err := vm.Environment.Get(instruction.Args[0].(string)); err == nil {
			vm.Push(value)
		} else {
			interpreterError(err.Error(), instruction.Line, instruction.Col1, instruction.Col2)
		}
	case DefineVariable:
		err := vm.Environment.DefineVariable(instruction.Args[0].(string), instruction.Args[1].(bool), vm.Pop())
		if err != nil {
			interpreterError(err.Error(), instruction.Line, instruction.Col1, instruction.Col2)
		}
	case AssignVariable:
		var item = vm.Pop()
		err := vm.Environment.Set(instruction.Args[0].(string), item)
		if err != nil {
			interpreterError(err.Error(), instruction.Line, instruction.Col1, instruction.Col2)
		}
	case GetIndex:
		var index = vm.Pop()
		var list = vm.Pop()
		switch t := list.(type) {
		case OasisList:
			if indexInt, ok := index.(int); ok {
				if indexInt < 0 || indexInt >= len(*t) {
					interpreterError("Index out of bounds", instruction.Line, instruction.Col1, instruction.Col2)
				}
				vm.Push(list)
				vm.Push(index)
				vm.Push((*t)[indexInt])
			} else {
				interpreterError("Index must be an integer", instruction.Line, instruction.Col1, instruction.Col2)
			}
		case OasisMap:
			if indexString, ok := index.(string); ok {
				if _, ok := t[indexString]; !ok {
					interpreterError("Index not found", instruction.Line, instruction.Col1, instruction.Col2)
				}
				vm.Push(list)
				vm.Push(index)
				vm.Push(t[indexString])
			} else {
				interpreterError("Index must be a string", instruction.Line, instruction.Col1, instruction.Col2)
			}
		case Tuple:
			if indexInt, ok := index.(int); ok {
				if indexInt < 0 || indexInt >= len(t.Values) {
					interpreterError("Index out of bounds", instruction.Line, instruction.Col1, instruction.Col2)
				}
				vm.Push(list)
				vm.Push(index)
				vm.Push(t.Values[indexInt])
			} else {
				interpreterError("Index must be an integer", instruction.Line, instruction.Col1, instruction.Col2)
			}
		case *Prototype:
			var fn, err = t.Get("__index")
			if err != nil {
				interpreterError(err.Error(), instruction.Line, instruction.Col1, instruction.Col2)
			} else {
				if fn, ok := fn.(OasisCallable); ok {
					vm.Push(list)
					vm.Push(index)
					vm.Push(fn.Call(vm, []interface{}{index}))
				} else {
					interpreterError("__index not a function", instruction.Line, instruction.Col1, instruction.Col2)
				}
			}
		default:
			interpreterError("Indexing not supported", instruction.Line, instruction.Col1, instruction.Col2)
		}
	case GetProperty:
		var object = vm.Pop()
		switch t := object.(type) {
		case *Prototype:
			var val, err = t.Get(instruction.Args[0].(string))
			if err != nil {
				interpreterError(err.Error(), instruction.Line, instruction.Col1, instruction.Col2)
			} else {
				vm.Push(val)
			}
		// TODO: Add support for other types
		default:
			interpreterError("Property not supported", instruction.Line, instruction.Col1, instruction.Col2)
		}
	case AssignIndex:
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
				interpreterError("Index must be an integer", instruction.Line, instruction.Col1, instruction.Col2)
			}
		case OasisMap:
			if indexString, ok := index.(string); ok {
				t[indexString] = value
			} else {
				interpreterError("Index must be a string", instruction.Line, instruction.Col1, instruction.Col2)
			}
		case Tuple:
			interpreterError("Tuples are immutable", instruction.Line, instruction.Col1, instruction.Col2)
		case *Prototype:
			var fn, err = t.Get("__index")
			if err != nil {
				interpreterError(err.Error(), instruction.Line, instruction.Col1, instruction.Col2)
			} else {
				if fn, ok := fn.(OasisCallable); ok {
					fn.Call(vm, []interface{}{index, value})
				} else {
					interpreterError("__setIndex not a function", instruction.Line, instruction.Col1, instruction.Col2)
				}
			}
		}
	case AssignProperty:
		var value = vm.Pop()
		var object = vm.Pop()
		switch t := object.(type) {
		case *Prototype:
			t.Set(instruction.Args[0].(string), value)
		default:
			interpreterError("Property assignment not supported", instruction.Line, instruction.Col1, instruction.Col2)
		}
	case CallFunction:
		var args []interface{}
		if instruction.Args[0].(int) != 0 {
			for i := 0; i < instruction.Args[0].(int); i++ {
				args = append(args, nil)
				copy(args[1:], args)
				args[0] = vm.Pop()
			}
		}
		var fn = vm.Pop()
		if callFn, ok := fn.(OasisCallable); ok {
			if callFn.Arity() != instruction.Args[0].(int) {
				interpreterError("Function arity mismatch", instruction.Line, instruction.Col1, instruction.Col2)
			} else {
				vm.Push(callFn.Call(vm, args))
			}
		} else {
			interpreterError(fmt.Sprintf("Not a function %T", fn), instruction.Line, instruction.Col1, instruction.Col2)
		}
	case PushEmptyProto:
		var inherits *Prototype = nil
		if instruction.Args[0].(bool) {
			inherits = vm.Pop().(*Prototype)
		}
		vm.Push(NewPrototype(inherits))
	case MakeFunction:
		var args = make([]string, len(instruction.Args))
		for i := 0; i < len(instruction.Args); i++ {
			args[i] = instruction.Args[i].(string)
		}
		var function = OasisFunction{
			Args:    args,
			Body:    []Instruction{},
			Closure: NewEnvironment(vm.Environment),
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
			index = i + vm.IP
		}
		vm.IP = index + 1
		vm.Push(function)
	case ReturnOp:
		if !vm.FunctionVM {
			interpreterError("Return outside of function", instruction.Line, instruction.Col1, instruction.Col2)
		}
		vm.ReturnValue = vm.Pop()
		vm.Finished = true
	case Jump:
		vm.IP += instruction.Args[0].(int)
		vm.JumpFlag = true
	case JumpIf:
		if vm.Truthy(vm.Pop()) {
			vm.IP += instruction.Args[0].(int)
			vm.JumpFlag = true
		}
	case JumpIfFalse:
		if !vm.Truthy(vm.Pop()) {
			vm.IP += instruction.Args[0].(int)
			vm.JumpFlag = true
		}
	case EqualOp:
		var a = vm.Pop()
		var b = vm.Pop()
		vm.Push(vm.Equal(b, a))
	case LessOp:
		var a = vm.Pop()
		var b = vm.Pop()
		vm.Push(vm.Compare(b, a) < 0)
	case GreaterOp:
		var a = vm.Pop()
		var b = vm.Pop()
		vm.Push(vm.Compare(b, a) > 0)
	case LessEqualOp:
		var a = vm.Pop()
		var b = vm.Pop()
		vm.Push(vm.Compare(b, a) <= 0)
	case GreaterEqualOp:
		var a = vm.Pop()
		var b = vm.Pop()
		vm.Push(vm.Compare(b, a) >= 0)
	case Add:
		var a = vm.Pop()
		var b = vm.Pop()
		add, err := vm.Add(b, a)
		if err == nil {
			vm.Push(add)
		} else {
			interpreterError(err.Error(), instruction.Line, instruction.Col1, instruction.Col2)
		}
	case Subtract:
		var a = vm.Pop()
		var b = vm.Pop()
		subtract, err := vm.Subtract(b, a)
		if err == nil {
			vm.Push(subtract)
		} else {
			interpreterError(err.Error(), instruction.Line, instruction.Col1, instruction.Col2)
		}
	case Multiply:
		var a = vm.Pop()
		var b = vm.Pop()
		multiply, err := vm.Multiply(b, a)
		if err == nil {
			vm.Push(multiply)
		} else {
			interpreterError(err.Error(), instruction.Line, instruction.Col1, instruction.Col2)
		}
	case Divide:
		var a = vm.Pop()
		var b = vm.Pop()
		divide, err := vm.Divide(b, a)
		if err == nil {
			vm.Push(divide)
		} else {
			interpreterError(err.Error(), instruction.Line, instruction.Col1, instruction.Col2)
		}
	case Modulo:
		var a = vm.Pop()
		var b = vm.Pop()
		modulo, err := vm.Modulo(b, a)
		if err == nil {
			vm.Push(modulo)
		} else {
			interpreterError(err.Error(), instruction.Line, instruction.Col1, instruction.Col2)
		}
	case Negate:
		var a = vm.Pop()
		switch a.(type) {
		case int:
			vm.Push(-a.(int))
		case float64:
			vm.Push(-a.(float64))
		default:
			interpreterError("Negate not supported for this type", instruction.Line, instruction.Col1, instruction.Col2)
		}
	case NotOp:
		var a = vm.Pop()
		vm.Push(!vm.Truthy(a))
	case AndOp:
		var a = vm.Pop()
		var b = vm.Pop()
		vm.Push(vm.Truthy(b) && vm.Truthy(a))
	case OrOp:
		var a = vm.Pop()
		var b = vm.Pop()
		vm.Push(vm.Truthy(b) || vm.Truthy(a))
	case CloneOp:
		var a = vm.Pop()
		vm.Push(vm.Clone(a))
	case CreateList:
		vm.Push(CreateOasisList([]interface{}{}))
	case PushListItem:
		var item = vm.Pop()
		var list = vm.Pop()
		switch t := list.(type) {
		case OasisList:
			*t = append(*(list.(OasisList)), item)
		case Tuple:
			t.Append(item)
			list = t
		default:
			interpreterError("!!INTERNAL!!PLEASE REPORT THIS AS BUG!! PushListItem not supported for this type", instruction.Line, instruction.Col1, instruction.Col2)
		}
		vm.Push(list)
	case CreateTuple:
		vm.Push(CreateOasisTuple([]interface{}{}))
	case CreateMap:
		vm.Push(make(OasisMap))
	case PushMapItem:
		var value = vm.Pop()
		var key = vm.Pop()
		var mapObj = vm.Pop()
		switch t := mapObj.(type) {
		case OasisMap:
			t[key.(string)] = value
		default:
			interpreterError("!!INTERNAL!!PLEASE REPORT THIS AS BUG!! PushMapItem not supported for this type", instruction.Line, instruction.Col1, instruction.Col2)
		}
		vm.Push(mapObj)
	case MapFn:
		var obj = vm.Pop()
		var fn = vm.Pop()
		var result = CreateOasisList([]interface{}{})
		if fn, ok := fn.(OasisFunction); ok {
			switch t := obj.(type) {
			case OasisMap:
				for k, v := range t {
					*result = append(*result, fn.Call(vm, []interface{}{CreateOasisTuple([]interface{}{k, v})}))
				}
			case Tuple:
				for _, v := range t.Values {
					*result = append(*result, fn.Call(vm, []interface{}{v}))
				}
			case OasisList:
				for _, v := range *t {
					*result = append(*result, fn.Call(vm, []interface{}{v}))
				}
			case *Prototype:
				if val, err := t.Get("__iter"); err == nil {
					if val, ok := val.(OasisFunction); ok {
						var i = 0
						for ; !vm.IteratorExhausted; val.Call(vm, []interface{}{i}) {
							i += 1
						}
						vm.IteratorExhausted = false
					}
				}
			default:
				interpreterError("Not an iterable object", instruction.Line, instruction.Col1, instruction.Col2)
			}
		}
		vm.Push(result)
	case PushScope:
		vm.Environment = NewEnvironment(vm.Environment)
	case PopScope:
		vm.Environment = vm.Environment.Parent
	case EndProgram:
		vm.Finished = true
	case Dup:
		var a = vm.Pop()
		vm.Push(a)
		vm.Push(a)
	case CreateIterator:
		var obj = vm.Pop()
		vm.Push(NewOasisIterator(obj))
	case Next:
		var test = vm.Pop()
		if iterator, ok := test.(OasisIterator); ok {
			var err, result = iterator.Next(vm)
			if err != nil {
				interpreterError(err.Error(), instruction.Line, instruction.Col1, instruction.Col2)
			} else {
				if iterator.Exhausted {
					vm.IteratorExhausted = true
				}
				vm.Push(iterator)
				vm.Push(result)
			}
		} else {
			interpreterError(fmt.Sprintf("Not an iterator %T", test), instruction.Line, instruction.Col1, instruction.Col2)
		}
	case IsIteratorExhausted:
		vm.Push(vm.IteratorExhausted)
		vm.IteratorExhausted = false
	case SetMarker:
		vm.Push(StackMarker{instruction.Args[0].(int)})
	case PopMarker:
		for {
			var item = vm.Pop()
			if marker, ok := item.(StackMarker); ok {
				if marker.Value == instruction.Args[0].(int) {
					break
				}
			}
		}
	case Pop:
		vm.Pop()
	case Pop1:
		var a = vm.Pop()
		vm.Pop()
		vm.Push(a)
	case Pop2:
		var a = vm.Pop()
		var b = vm.Pop()
		vm.Pop()
		vm.Push(a)
		vm.Push(b)
	case NullCoalesce:
		var a = vm.Pop()
		var b = vm.Pop()
		if b == nil {
			vm.Push(a)
		} else {
			vm.Push(b)
		}
	}
}

type StackMarker struct {
	Value int
}

func CreateOasisList(values []interface{}) OasisList {
	return &values
}

func CreateOasisTuple(values []interface{}) Tuple {
	return Tuple{Values: values}
}

func (vm *VM) Equal(a, b interface{}) bool {
	switch a.(type) {
	case int:
		switch b.(type) {
		case int:
			return a.(int) == b.(int)
		case float64:
			return float64(a.(int)) == b.(float64)
		case uint8:
			return uint8(a.(int)) == b.(uint8)
		}
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
			if val, ok := val.(OasisFunction); ok {
				return val.Call(vm, []interface{}{b}) == true
			}
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

func (vm *VM) Compare(a, b interface{}) int {
	switch a.(type) {
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

func (vm *VM) Add(a, b interface{}) (interface{}, error) {
	switch a.(type) {
	case int:
		switch b.(type) {
		case int:
			return a.(int) + b.(int), nil
		case float64:
			return float64(a.(int)) + b.(float64), nil
		case uint8:
			return uint8(a.(int)) + b.(uint8), nil
		}
	case float64:
		switch b.(type) {
		case int:
			return a.(float64) + float64(b.(int)), nil
		case float64:
			return a.(float64) + b.(float64), nil
		case uint8:
			return a.(float64) + float64(b.(uint8)), nil
		}
	case uint8:
		switch b.(type) {
		case int:
			return a.(uint8) + uint8(b.(int)), nil
		case float64:
			return float64(a.(uint8)) + b.(float64), nil
		case uint8:
			return a.(uint8) + b.(uint8), nil
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
			if val, ok := val.(OasisFunction); ok {
				return val.Call(vm, []interface{}{b}), nil
			}
		} else {
			return nil, err
		}
	}
	return nil, errors.New("Unsupported type for add")
}

func (vm *VM) Subtract(a, b interface{}) (interface{}, error) {
	switch a.(type) {
	case int:
		switch b.(type) {
		case int:
			return a.(int) - b.(int), nil
		case float64:
			return float64(a.(int)) - b.(float64), nil
		case uint8:
			return uint8(a.(int)) - b.(uint8), nil
		}
	case float64:
		switch b.(type) {
		case int:
			return a.(float64) - float64(b.(int)), nil
		case float64:
			return a.(float64) - b.(float64), nil
		case uint8:
			return a.(float64) - float64(b.(uint8)), nil
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
			if val, ok := val.(OasisFunction); ok {
				return val.Call(vm, []interface{}{b}), nil
			}
		} else {
			return nil, err
		}
	}
	return nil, errors.New("Unsupported type for subtract")
}

func (vm *VM) Multiply(a, b interface{}) (interface{}, error) {
	switch a.(type) {
	case int:
		switch b.(type) {
		case int:
			return a.(int) * b.(int), nil
		case float64:
			return float64(a.(int)) * b.(float64), nil
		case uint8:
			return uint8(a.(int)) * b.(uint8), nil
		}
	case float64:
		switch b.(type) {
		case int:
			return a.(float64) * float64(b.(int)), nil
		case float64:
			return a.(float64) * b.(float64), nil
		case uint8:
			return a.(float64) * float64(b.(uint8)), nil
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
			if val, ok := val.(OasisFunction); ok {
				return val.Call(vm, []interface{}{b}), nil
			}
		} else {
			return nil, err
		}
	}
	return nil, errors.New("Unsupported type for multiply")
}

func (vm *VM) Divide(a, b interface{}) (interface{}, error) {
	switch a.(type) {
	case int:
		switch b.(type) {
		case int:
			return a.(int) / b.(int), nil
		case float64:
			return float64(a.(int)) / b.(float64), nil
		case uint8:
			return uint8(a.(int)) / b.(uint8), nil
		}
	case float64:
		switch b.(type) {
		case int:
			return a.(float64) / float64(b.(int)), nil
		case float64:
			return a.(float64) / b.(float64), nil
		case uint8:
			return a.(float64) / float64(b.(uint8)), nil
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
			if val, ok := val.(OasisFunction); ok {
				return val.Call(vm, []interface{}{b}), nil
			}
		} else {
			return nil, err
		}
	}
	return nil, errors.New("Unsupported type for divide")
}

func (vm *VM) Modulo(a, b interface{}) (interface{}, error) {
	switch a.(type) {
	case int:
		switch b.(type) {
		case int:
			return a.(int) % b.(int), nil
		case uint8:
			return uint8(a.(int)) % b.(uint8), nil
		}
	case uint8:
		switch b.(type) {
		case int:
			return a.(uint8) % uint8(b.(int)), nil
		case uint8:
			return a.(uint8) % b.(uint8), nil
		}
	case *Prototype:
		// mod overload
		var val, err = a.(*Prototype).Get("__mod")
		if err == nil {
			if val, ok := val.(OasisFunction); ok {
				return val.Call(vm, []interface{}{b}), nil
			}
		} else {
			return nil, err
		}
	}
	return nil, errors.New("Unsupported type for modulo")
}

func (vm *VM) Truthy(a interface{}) bool {
	switch a.(type) {
	case int:
		return a.(int) != 0
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
	case Tuple:
		return len(a.(Tuple).Values) > 0
	case OasisMap:
		return len(a.(OasisMap)) > 0
	case OasisFunction:
		return true
	case bool:
		return a.(bool)
	case nil:
		return false
	}
	return false
}

func (vm *VM) Clone(a interface{}) interface{} {
	switch a.(type) {
	case int:
		return a.(int)
	case float64:
		return a.(float64)
	case uint8:
		return a.(uint8)
	case string:
		return a.(string)
	case *Prototype:
		// clone the prototype
		var proto = a.(*Prototype)
		var clone = &Prototype{}
		clone.Inherited = proto.Inherited
		for k, v := range proto.Body {
			clone.Body[k] = v
		}
		return clone
	case OasisList:
		// clone the list
		var list = a.(OasisList)
		var clone = CreateOasisList(*list)
		return clone
	case Tuple:
		// tuples are immutable, this doesn't matter
		return a.(Tuple)
	case OasisMap:
		// clone the map
		var map1 = a.(OasisMap)
		var clone = OasisMap{}
		for k, v := range map1 {
			clone[k] = v
		}
		return clone
	case OasisFunction:
		// can't clone functions, just return the original
		return a.(OasisFunction)
	case bool:
		return a.(bool)
	case nil:
		return nil
	}
	return nil
}
