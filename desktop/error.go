package desktop

import (
	"fmt"
	"os"
	"strings"
)

var HadError = false
var InRepl = false
var Code = ""

func InterpreterError(msg string, line int, col1 int, col2 int) {
	HadError = true
	if InRepl {
		fmt.Printf("%s\n", msg)
		fmt.Printf("%s", Code)
		for i := 0; i < col1; i++ {
			fmt.Printf(" ")
		}
		for i := col1; i < col2; i++ {
			fmt.Printf("^")
		}
		fmt.Printf("\n")
		panic("InterpreterError")
	} else {
		fmt.Printf("line %d at %d -> %d\n", line, col1, col2)
		fmt.Printf("%s\n", msg)
		fmt.Printf("%s\n", strings.Split(Code, "\n")[line-1])
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
