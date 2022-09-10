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
					argsL := args[1].(core.OasisList)
					argsStr := make([]string, len(args))
					for i, arg := range *argsL {
						argsStr[i] = arg.(string)
					}
					out, err := exec.Command(args[0].(string), argsStr...).Output()
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
