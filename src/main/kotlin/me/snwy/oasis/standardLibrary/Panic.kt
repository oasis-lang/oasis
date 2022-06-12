package me.snwy.oasis.standardLibrary

import me.snwy.oasis.*

val panic = Module("panic") { it, _ ->
    it.define("panic", KotlinFunction1<Unit, Any?> { it ->
        throw OasisException(it)
    })
    it.define("iteratorExhausted", KotlinFunction0 { _ ->
        throw InternalException(ExceptionType.ITERATOR_EMPTY)
    })
}