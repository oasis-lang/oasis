package standardLibrary

import Environment

data class Module(val name: String, val func: (Environment) -> Unit)