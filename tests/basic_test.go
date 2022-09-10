package tests

import (
	"oasisgo/core"
	"oasisgo/desktop"
	"oasisgo/stdlib"
	"testing"
)

func eval(t *testing.T, code string) any {
	desktop.InRepl = true
	desktop.Code = code
	var scanner = core.NewScanner(code)
	scanner.Scan()
	if desktop.HadError {
		t.Errorf("%s failed to compile.", code)
	}
	var parser = core.NewParser(scanner.Tokens)
	parser.Parse()
	if desktop.HadError {
		t.Errorf("%s failed to compile.", code)
	}
	env := core.NewEnvironment()
	scopes := make([]core.OasisEnvironment, 1, 257)
	scopes[0] = env
	var vm = core.NewVM(parser.Code, &scopes, false)
	stdlib.PopulateAll(vm)
	vm.Run()
	return vm.Stack[0]
}

func TestArithmetic(t *testing.T) {
	t.Log("Addition")
	if eval(t, "1 + 2").(int) != 3 {
		t.Error("1 + 2 failed.")
	}
	t.Log("Subtraction")
	if eval(t, "1 - 2").(int) != -1 {
		t.Error("1 - 2 failed.")
	}
	t.Log("Multiplication")
	if eval(t, "2 * 3").(int) != 6 {
		t.Error("2 * 3 failed.")
	}
	t.Log("Division")
	if eval(t, "6 / 3").(int) != 2 {
		t.Error("6 / 3 failed.")
	}
	t.Log("Modulo")
	if eval(t, "5 % 2").(int) != 1 {
		t.Error("5 % 2 failed.")
	}
	t.Log("Precedence")
	if eval(t, "1 + 2 * 3").(int) != 7 {
		t.Error("1 + 2 * 3 failed.")
	}
	t.Log("Precedence")
	if eval(t, "(1 + 2) * 3").(int) != 9 {
		t.Error("(1 + 2) * 3 failed.")
	}
	t.Log("Precedence")
	if eval(t, "1 + 2 * 3 - 4 / 2").(int) != 5 {
		t.Error("1 + 2 * 3 - 4 / 2 failed.")
	}
	t.Log("Precedence")
	if eval(t, "10 - 2 - 1").(int) != 7 {
		t.Error("10 - 2 - 1 failed.")
	}
}

func TestComparison(t *testing.T) {
	t.Log("Equal")
	if eval(t, "1 == 1").(bool) != true {
		t.Error("1 == 1 failed.")
	}
	t.Log("Not equal")
	if eval(t, "1 != 1").(bool) != false {
		t.Error("1 != 1 failed.")
	}
	t.Log("Less than")
	if eval(t, "1 < 2").(bool) != true {
		t.Error("1 < 2 failed.")
	}
	t.Log("Greater than")
	if eval(t, "2 > 1").(bool) != true {
		t.Error("2 > 1 failed.")
	}
	t.Log("Less than or equal")
	if eval(t, "1 <= 1").(bool) != true {
		t.Error("1 <= 1 failed.")
	}
	t.Log("Greater than or equal")
	if eval(t, "1 >= 1").(bool) != true {
		t.Error("1 >= 1 failed.")
	}
}

func TestLogical(t *testing.T) {
	t.Log("And")
	if eval(t, "true and true").(bool) != true {
		t.Error("true and true failed.")
	}
	t.Log("Or")
	if eval(t, "true or false").(bool) != true {
		t.Error("true or false failed.")
	}
	t.Log("Not")
	if eval(t, "not false").(bool) != true {
		t.Error("not false failed.")
	}
}

func TestVariables(t *testing.T) {
	t.Log("Declare")
	if eval(t, "let x = 1 x").(int) != 1 {
		t.Error("let x = 1 x failed.")
	}
	t.Log("Assign")
	if eval(t, "let x = 1 x = 2 x").(int) != 2 {
		t.Error("let x = 1 x = 2 x failed.")
	}
	t.Log("Cross Assign")
	if eval(t, "let x = 1 let y = 2 x = y x").(int) != 2 {
		t.Error("let x = 1 let y = 2 x = y x failed.")
	}
	t.Log("Plus Assign")
	if eval(t, "let x = 1 x += 1 x").(int) != 2 {
		t.Error("let x = 1 x += 1 x failed.")
	}
	t.Log("Minus Assign")
	if eval(t, "let x = 1 x -= 1 x").(int) != 0 {
		t.Error("let x = 1 x -= 1 x failed.")
	}
	t.Log("Multiply Assign")
	if eval(t, "let x = 1 x *= 2 x").(int) != 2 {
		t.Error("let x = 1 x *= 2 x failed.")
	}
	t.Log("Divide Assign")
	if eval(t, "let x = 1 x /= 2 x").(int) != 0 {
		t.Error("let x = 1 x /= 2 x failed.")
	}
}

func TestStrings(t *testing.T) {
	t.Log("Declare")
	if eval(t, "\"hello\"").(string) != "hello" {
		t.Error("\"hello\" failed.")
	}
	t.Log("Concatenate")
	if eval(t, "\"hello\" + \" world\"").(string) != "hello world" {
		t.Error("\"hello\" + \" world\" failed.")
	}
}

func TestLists(t *testing.T) {
	t.Log("Declare")
	if (*eval(t, "[1, 2, 3]").(core.OasisList))[1].(int) != 2 {
		t.Error("[1, 2, 3] failed.")
	}
	t.Log("Index")
	if eval(t, "[1, 2, 3]:(1)").(int) != 2 {
		t.Error("[1, 2, 3]:(1) failed.")
	}
	t.Log("Length")
	if eval(t, "len([1, 2, 3])").(int) != 3 {
		t.Error("len([1, 2, 3]) failed.")
	}
	t.Log("Update")
	if eval(t, "let x = [1, 2, 3] x:(1) = 4 x:(1)").(int) != 4 {
		t.Error("let x = [1, 2, 3] x:(1) = 4 x:(1) failed.")
	}
}

func TestMaps(t *testing.T) {
	t.Log("Declare")
	if eval(t, "dict {\"1\": 2}:(\"1\")").(int) != 2 {
		t.Error("dict {\"1\": 2}:(\"1\") failed.")
	}
	t.Log("Length")
	if eval(t, "len(dict {\"1\": 2})").(int) != 1 {
		t.Error("len(dict {\"1\": 2}) failed.")
	}
	t.Log("Update")
	if eval(t, "let x = dict {\"1\": 2} "+
		"x:(\"1\") = 3 "+
		"x:(\"1\")").(int) != 3 {
		t.Error("let x = dict {\"1\": 2} x:(\"1\") = 3 x:(\"1\") failed.")
	}
}

func TestFunctions(t *testing.T) {
	t.Log("Calling")
	if eval(t, "let x = fn => 1  "+
		"x()").(int) != 1 {
		t.Error("let x = fn => 1  x() failed.")
	}
	t.Log("Calling with arguments")
	if eval(t, "let x = fn(a) => a  "+
		"x(1)").(int) != 1 {
		t.Error("let x = fn(a) => a  x(1) failed.")
	}
	t.Log("Calling with multiple arguments")
	if eval(t, "let x = fn(a, b) => a + b  "+
		"x(1, 2)").(int) != 3 {
		t.Error("let x = fn(a, b) => a + b  x(1, 2) failed.")
	}
	t.Log("Passing function as argument")
	if eval(t, "let x = fn(a) => a(1)  "+
		"x(fn(a) => a + 1)").(int) != 2 {
		t.Error("let x = fn(a) => a(1)  x(fn(a) => a + 1) failed.")
	}
	t.Log("Tail call optimization")
	if eval(t, "let x = fn(a) "+
		"if a == 0 "+
		"return 0 "+
		"else "+
		"return x(a - 1) "+
		"end "+
		"end "+
		"x(100000)").(int) != 0 {
		t.Error("let x = fn(a) if a == 0 return 0 else return x(a - 1) end end x(100000) failed.")
	}
}

func TestClosures(t *testing.T) {
	t.Log("Calling")
	if eval(t, "let x = fn "+
		"let y = 1 "+
		"return y "+
		"end "+
		"x()").(int) != 1 {
		t.Error("let x = fn let y = 1 return y end x() failed.")
	}
	t.Log("External variables")
	if eval(t, "let x = 1"+
		" let y = fn => x "+
		"y()").(int) != 1 {
		t.Error("let x = 1 let y = fn => x y() failed.")
	}
	t.Log("External variables update")
	if eval(t, "let x = 1 "+
		"let y = fn => x "+
		"x = 2 "+
		"y() ").(int) != 2 {
		t.Error("let x = 1 let y = fn => x x = 2 y() failed.")
	}
}

func TestLoops(t *testing.T) {
	t.Log("While")
	if eval(t, "(fn "+
		"let x = 0 "+
		"while x < 10 "+
		"x += 1 "+
		"end "+
		"return x "+
		"end)()").(int) != 10 {
		t.Error("let x = 0 while x < 10 x += 1 end x failed.")
	}
	t.Log("Until")
	if eval(t, "(fn "+
		"let x = 0 "+
		"until x == 10 "+
		"x += 1 "+
		"end "+
		"return x "+
		"end)()").(int) != 10 {
		t.Error("let x = 0 until x == 10 x += 1 end x failed.")
	}
	t.Log("For item")
	if eval(t, "(fn "+
		"let x = 0 "+
		"for item i in [1, 2, 3] "+
		"x += i "+
		"end "+
		"return x "+
		"end)()").(int) != 6 {
		t.Error("let x = 0 for item i in [1, 2, 3] x += i end x failed.")
	}
	t.Log("Classic for")
	if eval(t, "(fn "+
		"let x = 0 "+
		"for let i = 0 | i < 10 | i += 1 "+
		"x += i "+
		"end "+
		"return x "+
		"end)()").(int) != 45 {
		t.Error("let x = 0 for let i = 0 | i < 10 | i += 1 x += i end x failed.")
	}
}

func TestConditionals(t *testing.T) {
	t.Log("If else")
	if eval(t, "(fn "+
		"if true "+
		"return 1 "+
		"else "+
		"return 2 "+
		"end "+
		"end)()").(int) != 1 {
		t.Error("(fn if true return 1 else return 2 end end)()")
	}
	t.Log("Unless else")
	if eval(t, "(fn "+
		"unless false "+
		"return 1 "+
		"else "+
		"return 2 "+
		"end "+
		"end)()").(int) != 1 {
		t.Error("unless false return 1 else return 2 end failed.")
	}
	t.Log("If else if else")
	if eval(t, "(fn "+
		"if false "+
		"return 1 "+
		"else if true "+
		"return 2 "+
		"else "+
		"return 3 "+
		"end end "+
		"end)()").(int) != 2 {
		t.Error("(fn if false return 1 else if true return 2 else return 3 end end end)() failed.")
	}
}
