package main

import "fmt"

func interpreter_error(msg string, line int, col1 int, col2 int) {
	// TODO: real interpreter_error reporting
	panic(fmt.Sprintf("%s at line %d: %d to %d", msg, line, col1, col2))
}

func main() {
	var source = "(fn(x, y) => x + y)(1, 2)"
	fmt.Println("-- PROGRAM --")
	fmt.Println(source)
	var scanner = NewScanner(source)
	scanner.Scan()
	fmt.Println("-- TOKENS --")
	for _, token := range scanner.Tokens {
		fmt.Printf("%s\n", token)
	}
	var parser = NewParser(scanner.Tokens)
	parser.Parse()
	fmt.Println("-- INSTRUCTIONS --")
	for _, instruction := range parser.Code {
		fmt.Printf("%s\n", instruction)
	}
	var env = NewEnvironment(nil)
	var vm = NewVM(parser.Code, env, false)
	vm.Run()
	fmt.Println("-- STACK --")
	for _, value := range vm.Stack {
		fmt.Println(value)
	}
}
