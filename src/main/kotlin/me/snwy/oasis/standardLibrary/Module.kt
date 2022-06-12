package me.snwy.oasis.standardLibrary

import me.snwy.oasis.Environment
import me.snwy.oasis.Interpreter

data class Module(val name: String, val func: (Environment, Interpreter) -> Unit)