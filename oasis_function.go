package main

type OasisCallable interface {
	Call(vm *VM, args []interface{}) interface{}
	Arity() int
}

type OasisFunction struct {
	Args    []string
	Body    []Instruction
	Closure *OasisEnvironment
}

func (f OasisFunction) Call(vm *VM, args []interface{}) interface{} {
	var env = NewEnvironment(f.Closure)
	for i, arg := range args {
		env.Values[f.Args[i]] = arg
	}
	var fnVm = NewVM(f.Body, env, true)
	fnVm.Run()
	return fnVm.ReturnValue
}

func (f OasisFunction) Arity() int {
	return len(f.Args)
}

type NativeFunction struct {
	Fn   func(args []interface{}) interface{}
	Args int
}

func (f *NativeFunction) Call(vm *VM, args []interface{}) interface{} {
	return f.Fn(args)
}

func (f *NativeFunction) Arity() int {
	return f.Args
}
