package core

import "fmt"

type OasisCallable interface {
	Call(vm *VM, args []any) any
	Arity() int
}

type OasisFunction struct {
	Args    []string
	Body    []Instruction
	Closure []OasisEnvironment
}

func (f OasisFunction) Call(vm *VM, args []any) any {
	var scopes []OasisEnvironment
	scopes = append(scopes, f.Closure...)
	scopes = append(scopes, NewEnvironment())
	for i, arg := range f.Args {
		scopes[len(scopes)-1].Values[arg] = args[i]
	}
	var fnVm = NewVM(f.Body, &scopes, true)
	fnVm.Run()
	return fnVm.ReturnValue
}

func (f OasisFunction) String() string {
	return fmt.Sprintf("<fn>(%d)", f.Arity())
}

func (f OasisFunction) Arity() int {
	return len(f.Args)
}

type NativeFunction struct {
	Fn   func(vm *VM, args []any) any
	Args int
}

func (f *NativeFunction) Call(vm *VM, args []any) any {
	return f.Fn(vm, args)
}

func (f *NativeFunction) Arity() int {
	return f.Args
}
