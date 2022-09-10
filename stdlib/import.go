package stdlib

import (
	"fmt"
	"io/ioutil"
	"oasisgo/core"
	"oasisgo/desktop"
)

type Import struct{}

func compile(code string) []core.Instruction {
	var scanner = core.NewScanner(code)
	scanner.Scan()
	if desktop.HadError {
		return []core.Instruction{}
	}
	var parser = core.NewParser(scanner.Tokens)
	parser.Parse()
	if desktop.HadError {
		return []core.Instruction{}
	}
	return parser.Code
}

func (Import) Create(vm *core.VM) (string, any) {
	return "import", &core.NativeFunction{
		Fn: func(vm *core.VM, args []any) any {
			var env = core.NewEnvironment()
			var code, err = ioutil.ReadFile(args[0].(string))
			if err != nil {
				fmt.Printf("%s\n", err)
				return nil
			}
			tempCode := desktop.Code
			desktop.Code = string(code)
			var instructions = compile(string(code))
			if desktop.HadError {
				return nil
			}
			scopes := make([]core.OasisEnvironment, 1, 257)
			scopes[0] = env
			var importVm = core.NewVM(instructions, &scopes, false)
			PopulateAll(importVm)
			importVm.Run()
			result := map[string]any{}
			for _, export := range importVm.Exports {
				result[export] = (*importVm.Scopes)[0].Values[export]
			}
			desktop.Code = tempCode
			return &core.Prototype{
				Inherited: &core.BasePrototype,
				Body:      result,
			}
		},
		Args: 1,
	}
}
