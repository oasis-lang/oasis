package me.snwy.oasis

class KotlinFunction0<T>(val function: (interpreter: Interpreter) -> T) : OasisCallable {
    constructor(function: () -> T) : this({ interpreter -> function() })
    override fun call(interpreter: Interpreter, arguments: List<Any?>): Any? {
        if (arguments.size < arity()) throw RuntimeError(line, "Expected ${arity()} arguments, got ${arguments.size}")
        return function(interpreter)
    }

    override fun arity(): Int {
        return 0
    }

    override fun toString(): String {
        return "NativeFunction(${arity()})"
    }
}

class KotlinFunction1<T, A>(val function: (interpreter: Interpreter, A) -> T) : OasisCallable {
    constructor(function: (A) -> T) : this({ interpreter, x -> function(x) })
    override fun call(interpreter: Interpreter, arguments: List<Any?>): Any? {
        if (arguments.size < arity()) throw RuntimeError(line, "Expected ${arity()} arguments, got ${arguments.size}")
        return function(interpreter, arguments[0] as A)
    }

    override fun arity(): Int {
        return 1
    }

    override fun toString(): String {
        return "NativeFunction(${arity()})"
    }
}

class KotlinFunction2<T, A, B>(val function: (interpreter: Interpreter, a: A, b: B) -> T) : OasisCallable {
    constructor(function: (A, B) -> T) : this({ interpreter, x, y -> function(x, y) })
    override fun call(interpreter: Interpreter, arguments: List<Any?>): Any? {
        if (arguments.size < arity()) throw RuntimeError(line, "Expected ${arity()} arguments, got ${arguments.size}")
        return function(interpreter, arguments[0] as A, arguments[1] as B)
    }

    override fun arity(): Int {
        return 2
    }

    override fun toString(): String {
        return "NativeFunction(${arity()})"
    }
}

class KotlinFunction3<T, A, B, C>(val function: (interpreter: Interpreter, a: A, b: B, c: C) -> T) : OasisCallable {
    constructor(function: (A, B, C) -> T) : this({ interpreter, x, y, z -> function(x, y, z) })
    override fun call(interpreter: Interpreter, arguments: List<Any?>): Any? {
        if (arguments.size < arity()) throw RuntimeError(line, "Expected ${arity()} arguments, got ${arguments.size}")
        return function(interpreter, arguments[0] as A, arguments[1] as B, arguments[2] as C)
    }

    override fun arity(): Int {
        return 3
    }

    override fun toString(): String {
        return "NativeFunction(${arity()})"
    }
}

class KotlinFunction4<T, A, B, C, D>(val function: (interpreter: Interpreter, a: A, b: B, c: C, D) -> T) : OasisCallable {
    constructor(function: (A, B, C, D) -> T) : this({ interpreter, w, x, y, z -> function(w, x, y, z) })
    override fun call(interpreter: Interpreter, arguments: List<Any?>): Any? {
        if (arguments.size < arity()) throw RuntimeError(line, "Expected ${arity()} arguments, got ${arguments.size}")
        return function(interpreter, arguments[0] as A, arguments[1] as B, arguments[2] as C, arguments[3] as D)
    }

    override fun arity(): Int {
        return 4
    }

    override fun toString(): String {
        return "NativeFunction(${arity()})"
    }
}

class KotlinFunction5<T, A, B, C, D, E>(val function: (interpreter: Interpreter, a: A, b: B, c: C, d: D, e: E) -> T) : OasisCallable {
    constructor(function: (A, B, C, D, E) -> T) : this({ interpreter, v, w, x, y, z -> function(v, w, x, y, z) })
    override fun call(interpreter: Interpreter, arguments: List<Any?>): Any? {
        if (arguments.size < arity()) throw RuntimeError(line, "Expected ${arity()} arguments, got ${arguments.size}")
        return function(interpreter, arguments[0] as A, arguments[1] as B, arguments[2] as C, arguments[3] as D, arguments[4] as E)
    }

    override fun arity(): Int {
        return 5
    }

    override fun toString(): String {
        return "NativeFunction(${arity()})"
    }
}