package me.snwy.oasis

class ParseException(val line: Int, val column: Int, val parseMessage: String) : Exception()