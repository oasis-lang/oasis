package me.snwy.oasis

import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*

internal class OasisEnvironmentTest {

    private val testEnvironment = OasisEnvironment()

    @Test
    fun eval() {
        println("string literals")
        assertEquals(
            "Hello, world!",
            testEnvironment.doExpr("\"Hello, world!\"")
        )
        println("integer literals")
        assertEquals(
            42,
            testEnvironment.doExpr("42")
        )
        println("float literals")
        assertEquals(
            3.14,
            testEnvironment.doExpr("3.14")
        )
        println("boolean literals")
        assertEquals(
            true,
            testEnvironment.doExpr("true")
        )
        assertEquals(
            false,
            testEnvironment.doExpr("false")
        )
        println("null literals")
        assertEquals(
            null,
            testEnvironment.doExpr("nil")
        )
        println("basic operators")
        assertEquals(
            1,
            testEnvironment.doExpr("1 + 0")
        )
        assertEquals(
            0,
            testEnvironment.doExpr("1 - 1")
        )
        assertEquals(
            2,
            testEnvironment.doExpr("1 * 2")
        )
        assertEquals(
            2,
            testEnvironment.doExpr("4 / 2")
        )
        assertEquals(
            0,
            testEnvironment.doExpr("4 % 2")
        )
        println("unary operators")
        assertEquals(
            -1,
            testEnvironment.doExpr("-1")
        )
        assertEquals(
            false,
            testEnvironment.doExpr("not true")
        )
        println("binary operators")
        assertEquals(
            true,
            testEnvironment.doExpr("1 < 2")
        )
        assertEquals(
            true,
            testEnvironment.doExpr("1 <= 1")
        )
        assertEquals(
            true,
            testEnvironment.doExpr("2 > 1")
        )
        assertEquals(
            true,
            testEnvironment.doExpr("2 >= 1")
        )
        assertEquals(
            true,
            testEnvironment.doExpr("1 == 1")
        )
        assertEquals(
            true,
            testEnvironment.doExpr("1 != 0")
        )
        assertEquals(
            true,
            testEnvironment.doExpr("true and true")
        )
        assertEquals(
            false,
            testEnvironment.doExpr("true and false")
        )
        assertEquals(
            true,
            testEnvironment.doExpr("true or false")
        )
        assertEquals(
            false,
            testEnvironment.doExpr("false or false")
        )
        println("conditional operators")
        assertEquals(
            true,
            testEnvironment.doExpr("if true => true else false")
        )
        assertEquals(
            false,
            testEnvironment.doExpr("if false => true else false")
        )
        println("variable assignment")
        assertEquals(
            1,
            testEnvironment.eval("let a = 1").run { testEnvironment.run(); testEnvironment.doExpr("a") }
        )
        assertEquals(
            2,
            testEnvironment.doExpr("a = 2")
        )
        println("functions")
        assertEquals(
            3,
            testEnvironment.eval("let add = fn(x, y) => x + y ").run{testEnvironment.run(); testEnvironment.doExpr("add(1, 2)")},
        )
        assertEquals(
            1,
            testEnvironment.eval("let fn1 = fn => 1").run { testEnvironment.run(); testEnvironment.doExpr("fn1()") }
        )
        assertEquals(
            5,
            testEnvironment.eval("let fn2 = fn(x, y) " +
                                            "return x + y " +
                                       "end"
            ).run {
                testEnvironment.run()
                testEnvironment.doExpr("fn2(2, 3)")
            }
        )
        println("destructuring")
        assertEquals(
            listOf(1, 2, 3),
            testEnvironment.eval("let x, y, z = (1, 2, 3)").run { testEnvironment.run(); testEnvironment.doExpr("[x, y, z]") }
        )
        assertEquals(
            listOf(4, 5, 6),
            testEnvironment.eval("(x, y, z) = (4, 5, 6)").run { testEnvironment.run(); testEnvironment.doExpr("[x, y, z]") }
        )
        assertEquals(
            listOf(1, 2, 3),
            testEnvironment.eval("let foo = fn(x, y, z) => [x, y, z]").run { testEnvironment.run(); testEnvironment.doExpr("foo(*[1, 2, 3])") }
        )
    }
}