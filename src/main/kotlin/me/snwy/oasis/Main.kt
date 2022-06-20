package me.snwy.oasis

import me.snwy.oasis.experimental.PythonTranspiler
import org.jline.reader.*
import org.jline.reader.impl.history.DefaultHistory
import org.jline.terminal.*
import java.nio.file.Files
import java.nio.file.Path
import kotlin.system.exitProcess

var repl = false
fun main(args: Array<String>) {
    val env = Creator.create("")
    val console = LineReaderBuilder.builder()
        .terminal(TerminalBuilder.terminal())
        .history(DefaultHistory())
        .build()
    val program = args.find {
        Files.exists(Path.of(it))
    }
    val compile = args.find {
        it == "--compile" || it == "-c"
    } != null
    if (compile) {
        var scanner = Scanner(Files.readString(program?.let { Path.of(it) }))
        var tokens = scanner.scanTokens()
        var parser = Parser(tokens)
        val ast: Stmt?
        try {
            ast = parser.parse()
        } catch (e: ParseException) {
            exitProcess(1)
        }
        scanner = object{}.javaClass.classLoader.getResource("libpy.oa")?.readText()?.let { Scanner(it) }!!
        tokens = scanner.scanTokens()
        parser = Parser(tokens)
        Files.writeString(Path.of("$program.py"), "${PythonTranspiler().transpile(parser.parse())}\n${PythonTranspiler().transpile(ast)}\n")
    }
    else if(program != null) {
        (env.get("sys") as OasisPrototype).set("argv", args.copyOfRange(1, args.size).toCollection(ArrayList<Any?>()))
        try {
            env.eval(Files.readString(Path.of(program)))
            env.run()
        }
        catch (e: RuntimeError) {
            error(e.line, e.s)
            println("| ${Files.readString(Path.of(program)).split('\n')[e.line - 1]}")
            exitProcess(1)
        } catch (e: ParseException) {
            println("| ${Files.readString(Path.of(program)).split('\n')[env.parser.tokens[env.parser.current].line - 1]}")
            exitProcess(1)
        }
    } else while(true) {
        repl = true
        try {
            console.readLine("oasis -> ")?.let { env.eval(it) ; env.run() }
        }
        catch (e: RuntimeError) {
            error(e.line, e.s)
        } catch (_: ParseException) { }
    }
}

fun error(line: Int, reason: String) {
    println("On line $line: $reason")
}