package me.snwy.oasis

class RuntimeError(var line: Int, var s: String) : Exception()