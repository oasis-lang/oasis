package stdlib

import (
	"oasisgo/core"
	"oasisgo/desktop"
)

func CreateResult(vm *core.VM, value any, isErr bool, err string) *core.Prototype {
	var result = core.Prototype{
		Inherited: &core.BasePrototype,
	}
	result.Body = map[string]any{
		"value":   value,
		"isError": isErr,
		"error":   err,
		"unwrap": &core.NativeFunction{
			Fn: func(vm *core.VM, args []any) any {
				if isErr {
					desktop.InterpreterError("Unwrapped an error value: "+err, vm.Instructions[vm.IP].Line, vm.Instructions[vm.IP].Col1, vm.Instructions[vm.IP].Col2)
					return nil
				}
				return value
			},
			Args: 0,
		},
		"unwrapErr": &core.NativeFunction{
			Fn: func(vm *core.VM, args []any) any {
				if !isErr {
					desktop.InterpreterError("Unwrapped a value", vm.Instructions[vm.IP].Line, vm.Instructions[vm.IP].Col1, vm.Instructions[vm.IP].Col2)
					return nil
				}
				return err
			},
			Args: 0,
		},
		"unwrapWith": &core.NativeFunction{
			Fn: func(vm *core.VM, args []any) any {
				if !isErr {
					if fn, ok := args[0].(core.OasisCallable); ok {
						return fn.Call(vm, []any{value})
					} else {
						desktop.InterpreterError("UnwrapWith called with non-function", vm.Instructions[vm.IP].Line, vm.Instructions[vm.IP].Col1, vm.Instructions[vm.IP].Col2)
					}
				}
				return nil
			},
			Args: 1,
		},
		"unwrapErrWith": &core.NativeFunction{
			Fn: func(vm *core.VM, args []any) any {
				if isErr {
					if fn, ok := args[0].(core.OasisCallable); ok {
						return fn.Call(vm, []any{value})
					} else {
						desktop.InterpreterError("UnwrapWith called with non-function", vm.Instructions[vm.IP].Line, vm.Instructions[vm.IP].Col1, vm.Instructions[vm.IP].Col2)
					}
				}
				return nil
			},
			Args: 1,
		},
	}
	return &result
}
