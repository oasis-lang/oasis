package me.snwy.oasis.standardLibrary

import me.snwy.oasis.Environment
import me.snwy.oasis.Interpreter
import me.snwy.oasis.KotlinFunction0
import me.snwy.oasis.OasisPrototype

var base = OasisPrototype(null, -1)

object StandardLibrary {
    private var modules = arrayListOf(
        // Base modules
        io,
        string,
        time,
        type,
        list,
        range,
        sys,
        hashmap,
        json,
        prototype,
        math,
        async,
        api,
        import,
        func,
        panic,
        plot,
    )

    fun addModule(x: Module) = modules.add(x)

    fun generateLib(x: Environment, y: Interpreter) {
        base.let { it.set("toString", KotlinFunction0 { _ -> return@KotlinFunction0 "<obj ${it.hashCode()}>" }) }
        modules.map { it.func(x, y) }
    }
}
