package me.snwy.oasis.standardLibrary

import me.snwy.oasis.KotlinFunction0
import me.snwy.oasis.KotlinFunction1
import me.snwy.oasis.OasisPrototype
import me.snwy.oasis.Optimizer
import java.io.BufferedReader
import java.io.InputStreamReader
import kotlin.system.exitProcess

val sys = Module("sys") { it, interpreter ->
    val sys = OasisPrototype(base, -1, interpreter)
    sys.set("exit", KotlinFunction1(::exitProcess))
    sys.set("exec", KotlinFunction1<Unit, String> { cmd ->
        val process = Runtime.getRuntime().exec(cmd)
        val reader = BufferedReader(InputStreamReader(process.inputStream))
        var line: String
        while (reader.readLine().also { line = it ?: "" } != null) {
            println(line)
        }
    })
    sys.set("vars", KotlinFunction0<ArrayList<Any?>> { interpreter ->
        ArrayList(Optimizer.nameMap.values)
    })
    it.define("sys", sys)
}