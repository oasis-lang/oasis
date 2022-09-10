package main

import (
	"bufio"
	"fmt"
	"io/ioutil"
	"oasisgo/core"
	"oasisgo/desktop"
	"oasisgo/stdlib"
	"os"
)

func main() {
	//defer profile.Start().Stop()
	if len(os.Args) == 1 {
		repl()
	} else if len(os.Args) == 2 {
		run(os.Args[1], []string{})
		if desktop.HadError {
			os.Exit(1)
		}
	} else {
		if os.Args[1] == "-print" {
			code, err := ioutil.ReadFile(os.Args[2])
			if err != nil {
				fmt.Printf("%s\n", err)
				return
			}
			var instructions = compile(string(code))
			for _, instruction := range instructions {
				fmt.Printf("%s\n", instruction)
			}
			return
		} else {
			run(os.Args[1], os.Args[2:])
		}
		if desktop.HadError {
			os.Exit(1)
		}
	}
}

func run(file string, args []string) {
	var env = core.NewEnvironment()
	var argsObj = core.CreateOasisList([]any{})
	for _, arg := range args {
		*argsObj = append(*argsObj, arg)
	}
	_ = env.Define("args", false, argsObj)
	var code, err = ioutil.ReadFile(file)
	if err != nil {
		fmt.Printf("%s\n", err)
		return
	}
	desktop.Code = string(code)
	var instructions = compile(string(code))
	if desktop.HadError {
		return
	}
	scopes := make([]core.OasisEnvironment, 1, 257)
	scopes[0] = env
	var vm = core.NewVM(instructions, &scopes, false)
	stdlib.PopulateAll(vm)
	vm.Run()
}

func runCode(instructions []core.Instruction, args []string) {
	var env = core.NewEnvironment()
	var argsObj = core.CreateOasisList([]any{})
	for _, arg := range args {
		*argsObj = append(*argsObj, arg)
	}
	_ = env.Define("args", false, argsObj)
	if desktop.HadError {
		return
	}
	scopes := make([]core.OasisEnvironment, 1, 257)
	scopes[0] = env
	var vm = core.NewVM(instructions, &scopes, false)
	stdlib.PopulateAll(vm)
	vm.Run()
}

func repl() {
	desktop.InRepl = true
	scopes := make([]core.OasisEnvironment, 1, 257)
	scopes[0] = core.NewEnvironment()
	for {
		fmt.Printf("oasis => ")
		var buffer = bufio.NewReader(os.Stdin)
		line, err := buffer.ReadString('\n')
		if err != nil {
			fmt.Printf("%s\n", err)
			return
		}
		desktop.Code = line
		var vm = core.NewVM(compile(line), &scopes, false)
		if desktop.HadError {
			desktop.HadError = false
			continue
		}
		stdlib.PopulateAll(vm)
		vm.Run()
		if vm.StackPointer > 0 {
			fmt.Printf("%v\n", vm.Stack[0])
		}
	}
}

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
