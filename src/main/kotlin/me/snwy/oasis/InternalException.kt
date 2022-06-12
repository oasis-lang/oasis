package me.snwy.oasis

enum class ExceptionType {
    ITERATOR_EMPTY,
    BREAK,
    CONTINUE,
}

data class InternalException(val type: ExceptionType) : Exception()