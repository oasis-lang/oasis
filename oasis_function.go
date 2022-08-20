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
	env := NewEnvironment(f.Closure)
	for i := 0; i < len(f.Args); i++ {
		env.Values[f.Args[i]] = args[i]
	}
	var fnVm = NewVM(f.Body, env, true)
	fnVm.Run()
	if fnVm.IteratorExhausted && vm != nil {
		vm.IteratorExhausted = true
	}
	return fnVm.ReturnValue
}

func (f OasisFunction) Arity() int {
	return len(f.Args)
}

type NativeFunction struct {
	Fn   func(vm *VM, args []interface{}) interface{}
	Args int
}

func (f *NativeFunction) Call(vm *VM, args []interface{}) interface{} {
	return f.Fn(vm, args)
}

func (f *NativeFunction) Arity() int {
	return f.Args
}
