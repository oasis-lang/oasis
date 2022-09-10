package stdlib

import (
	"fmt"
	"oasisgo/core"
)

type Debug struct{}

func (Debug) Create(vm *core.VM) (string, any) {
	return "debug", &core.Prototype{
		Inherited: &core.BasePrototype,
		Body: map[string]any{
			"disasm": &core.NativeFunction{
				Fn: func(vm *core.VM, args []any) any {
					if val, ok := args[0].(core.OasisFunction); ok {
						for _, ins := range val.Body {
							fmt.Printf("%s\n", ins.String())
						}
					}
					return nil
				},
				Args: 1,
			},
		},
	}
}
