package stdlib

import "oasisgo/core"

type Stream struct{}

var BufferSize = 1024

func (Stream) Create(vm *core.VM) (string, any) {
	return "stream", &core.Prototype{
		Inherited: &core.BasePrototype,
		Body: map[string]any{
			"__new": &core.NativeFunction{
				Fn: func(vm *core.VM, args []any) any {
					return make(chan any, BufferSize)
				},
				Args: 0,
			},
		},
	}
}
