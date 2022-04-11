package standardLibrary

import Interpreter
import KotlinFunction1
import OasisPrototype
import Parser
import Scanner
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.exists

var import = Module("import") {
    it.define("import", KotlinFunction1<OasisPrototype, String> {
        var module = Files.readString(
            if (Path.of("modules/$it").exists())
                Path.of("modules/$it")
            else
                Path.of(it))
        var interpreter = Interpreter()
        Scanner(module).scanTokens().also { tokens ->
            interpreter.execute(Parser(tokens).parse())
        }.let {
            val modProto = OasisPrototype(base, -1)
            interpreter.environment.values.map {
                modProto.set(it.key, it.value)
            }
            modProto
        }
    })
}