package stdlib

import (
	"oasisgo/core"
	"os"
	"os/exec"
)

type Sys struct{}

func (Sys) Create(vm *core.VM) (string, any) {
	return "sys", &core.Prototype{
		Inherited: &core.BasePrototype,
		Body: map[string]any{
			"exit": &core.NativeFunction{
				Fn: func(vm *core.VM, args []any) any {
					os.Exit(args[0].(int))
					return nil
				},
				Args: 1,
			},
			"resultOf": &core.NativeFunction{
				Fn: func(vm *core.VM, args []any) any {
					out, err := exec.Command(args[0].(string), args[1].([]string)...).Output()
					if err != nil {
						return CreateResult(vm, nil, true, err.Error())
					}
					return CreateResult(vm, string(out), false, "")
				},
				Args: 2,
			},
			"vars": &core.NativeFunction{
				Fn: func(vm *core.VM, args []any) any {
					return CreateResult(vm, os.Environ(), false, "")
				},
			},
		},
	}
}
