package stdlib

import (
	"oasisgo/core"
	"strings"
)

type String struct{}

func (String) Create(vm *core.VM) (string, any) {
	return "string", &core.Prototype{
		Inherited: &core.BasePrototype,
		Body: map[string]any{
			"contains": &core.NativeFunction{
				Fn: func(vm *core.VM, args []any) any {
					return strings.Contains(args[0].(string), args[1].(string))
				},
				Args: 2,
			},
			"count": &core.NativeFunction{
				Fn: func(vm *core.VM, args []any) any {
					return strings.Count(args[0].(string), args[1].(string))
				},
				Args: 2,
			},
			"hasPrefix": &core.NativeFunction{
				Fn: func(vm *core.VM, args []any) any {
					return strings.HasPrefix(args[0].(string), args[1].(string))
				},
				Args: 2,
			},
			"hasSuffix": &core.NativeFunction{
				Fn: func(vm *core.VM, args []any) any {
					return strings.HasSuffix(args[0].(string), args[1].(string))
				},
				Args: 2,
			},
			"index": &core.NativeFunction{
				Fn: func(vm *core.VM, args []any) any {
					return strings.Index(args[0].(string), args[1].(string))
				},
				Args: 2,
			},
			"lastIndex": &core.NativeFunction{
				Fn: func(vm *core.VM, args []any) any {
					return strings.LastIndex(args[0].(string), args[1].(string))
				},
				Args: 2,
			},
			"repeat": &core.NativeFunction{
				Fn: func(vm *core.VM, args []any) any {
					return strings.Repeat(args[0].(string), args[1].(int))
				},
				Args: 2,
			},
			"replace": &core.NativeFunction{
				Fn: func(vm *core.VM, args []any) any {
					return strings.Replace(args[0].(string), args[1].(string), args[2].(string), -1)
				},
				Args: 3,
			},
			"split": &core.NativeFunction{
				Fn: func(vm *core.VM, args []any) any {
					return strings.Split(args[0].(string), args[1].(string))
				},
				Args: 2,
			},
			"toLower": &core.NativeFunction{
				Fn: func(vm *core.VM, args []any) any {
					return strings.ToLower(args[0].(string))
				},
				Args: 1,
			},
			"toUpper": &core.NativeFunction{
				Fn: func(vm *core.VM, args []any) any {
					return strings.ToUpper(args[0].(string))
				},
				Args: 1,
			},
			"trim": &core.NativeFunction{
				Fn: func(vm *core.VM, args []any) any {
					return strings.Trim(args[0].(string), args[1].(string))
				},
				Args: 2,
			},
			"trimLeft": &core.NativeFunction{
				Fn: func(vm *core.VM, args []any) any {
					return strings.TrimLeft(args[0].(string), args[1].(string))
				},
				Args: 2,
			},
			"trimRight": &core.NativeFunction{
				Fn: func(vm *core.VM, args []any) any {
					return strings.TrimRight(args[0].(string), args[1].(string))
				},
				Args: 2,
			},
			"trimSpace": &core.NativeFunction{
				Fn: func(vm *core.VM, args []any) any {
					return strings.TrimSpace(args[0].(string))
				},
				Args: 1,
			},
		},
	}
}
