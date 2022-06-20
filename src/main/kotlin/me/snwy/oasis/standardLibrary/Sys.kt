package me.snwy.oasis.standardLibrary

import me.snwy.oasis.KotlinFunction1
import me.snwy.oasis.OasisPrototype
import java.io.BufferedReader
import java.io.InputStreamReader
import kotlin.system.exitProcess

val sys = Module("sys") { it, interpreter ->
    val sys = OasisPrototype(base, -1, interpreter)
    sys.set("exit", KotlinFunction1(::exitProcess))
    sys.set("exec", KotlinFunction1<Unit, String> { cmd ->
        var process = Runtime.getRuntime().exec(cmd)
        var reader = BufferedReader(InputStreamReader(process.inputStream))
        var line: String
        while (reader.readLine().also { line = it ?: "" } != null) {
            println(line)
        }
    })
    it.define("sys", sys)
}