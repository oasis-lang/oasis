package main

import (
	"bufio"
	"fmt"
	"io/ioutil"
	"log"
	"os"
	"runtime/pprof"
	"strings"
)

var code = ""
var inRepl = false
var hadError = false

func interpreterError(msg string, line int, col1 int, col2 int) {
	hadError = true
	if inRepl {
		fmt.Printf("%s\n", msg)
		fmt.Printf("%s", code)
		for i := 0; i < col1; i++ {
			fmt.Printf(" ")
		}
		for i := col1; i < col2; i++ {
			fmt.Printf("^")
		}
		fmt.Printf("\n")
	} else {
		fmt.Printf("line %d at %d -> %d\n", line, col1, col2)
		fmt.Printf("%s\n", msg)
		fmt.Printf("%s\n", strings.Split(code, "\n")[line-1])
		for i := 0; i < col1; i++ {
			fmt.Printf(" ")
		}
		for i := col1; i < col2; i++ {
			fmt.Printf("^")
		}
		fmt.Printf("\n")
		os.Exit(1)
	}
}

func main() {
	//	debug.SetGCPercent(200)
	f, err := os.Create("oasis.pprof")
	if err != nil {
		log.Fatal(err)
	}
	err = pprof.StartCPUProfile(f)
	if err != nil {
		log.Fatal(err)
	}
	defer pprof.StopCPUProfile()
	if len(os.Args) == 1 {
		repl()
	} else if len(os.Args) == 2 {
		run(os.Args[1], []string{})
		if hadError {
			os.Exit(1)
		}
	} else {
		run(os.Args[1], os.Args[2:])
		if hadError {
			os.Exit(1)
		}
	}
}

func run(file string, args []string) {
	var env = NewEnvironment(nil)
	var argsObj = CreateOasisList([]interface{}{})
	for _, arg := range args {
		*argsObj = append(*argsObj, arg)
	}
	_ = env.DefineVariable("args", false, argsObj)
	var code, err = ioutil.ReadFile(file)
	if err != nil {
		fmt.Printf("%s\n", err)
		return
	}
	var instructions = compile(string(code))
	if hadError {
		return
	}
	var vm = NewVM(instructions, env, false)
	vm.Run()
	fmt.Printf("%v\n", vm.Stack)
}

func repl() {
	inRepl = true
	var env = NewEnvironment(nil)
	for {
		fmt.Printf("oasis => ")
		var buffer = bufio.NewReader(os.Stdin)
		line, err := buffer.ReadString('\n')
		if err != nil {
			fmt.Printf("%s\n", err)
			return
		}
		if line == "exit\n" {
			return
		}
		code = line
		var vm = NewVM(compile(line), env, false)
		vm.Run()
		if len(vm.Stack) > 0 {
			fmt.Printf("%v\n", vm.Stack[len(vm.Stack)-1])
		}
		hadError = false
	}
}

func compile(code string) []Instruction {
	var scanner = NewScanner(code)
	scanner.Scan()
	if hadError {
		return []Instruction{}
	}
	var parser = NewParser(scanner.Tokens)
	parser.Parse()
	if hadError {
		return []Instruction{}
	}
	return parser.Code
}
