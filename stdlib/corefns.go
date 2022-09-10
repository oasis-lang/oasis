package stdlib

import "oasisgo/core"

type Len struct{}

func (Len) Create(vm *core.VM) (string, any) {
	return "len", &core.NativeFunction{
		Fn: func(vm *core.VM, args []any) any {
			switch args[0].(type) {
			case string:
				return len(args[0].(string))
			case core.OasisList:
				return len(*args[0].(core.OasisList))
			case core.OasisMap:
				return len(args[0].(core.OasisMap))
			case core.Tuple:
				return len(args[0].(core.Tuple).Values)
			}
			return nil
		},
		Args: 1,
	}
}
