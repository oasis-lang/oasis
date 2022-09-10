package stdlib

import (
	"oasisgo/core"
)

type Module interface {
	Create(vm *core.VM) (string, any)
}

func Populate(vm *core.VM, module Module) {
	name, val := module.Create(vm)
	err := (*vm.Scopes)[0].Define(name, false, val)
	if err != nil {
		return
	}
}

var modules = []Module{
	IO{},
	Sys{},
	Time{},
	Debug{},
	Math{},
	Stream{},
	Import{},
	String{},
	Len{},
	JSON{},
	GPIO{},
}

func PopulateAll(vm *core.VM) {
	for _, module := range modules {
		Populate(vm, module)
	}
}
