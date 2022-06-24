package me.snwy.oasis.standardLibrary

import me.snwy.oasis.*
import me.snwy.oasis.Optimizer.Companion.nameMap
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.exists

var import = Module("import") { it, _ ->
    it.define("import", KotlinFunction1<OasisPrototype, String> { ginterpreter, it ->
        val module = Files.readString(
            if (Path.of("modules/$it").exists())
                Path.of("modules/$it")
            else
                Path.of(it)
        )
        val interpreter = Interpreter()
        Scanner(module).scanTokens().also { tokens ->
            interpreter.execute(Parser(tokens).parse())
        }.let {
            val modProto = OasisPrototype(base, -1, ginterpreter)
            interpreter.environment.values.map {
                nameMap[it.key]?.let { it1 -> modProto.set(it1, it.value) }
            }
            modProto
        }
    })
}