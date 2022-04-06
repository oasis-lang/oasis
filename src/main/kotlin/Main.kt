import org.jline.reader.*
import org.jline.reader.impl.history.DefaultHistory
import org.jline.terminal.*
import java.nio.file.Files
import java.nio.file.Path
import kotlin.system.exitProcess

var globalInterpreter : Interpreter? = null
var repl = false
fun main(args: Array<String>) {
    val interpreter = Interpreter()
    globalInterpreter = interpreter
    val console = LineReaderBuilder.builder()
        .terminal(TerminalBuilder.terminal())
        .history(DefaultHistory())
        .build()
    (interpreter.environment.get(Token(TokenType.IDENTIFIER, "sys", null, -1)) as OasisPrototype).set("argv", ArrayList<Any?>())
    val program = args.find {
        Files.exists(Path.of(it))
    }
    if(program != null) {
        (interpreter.environment.get(Token(TokenType.IDENTIFIER, "sys", null, -1)) as OasisPrototype).set("argv", args.copyOfRange(1, args.size).toCollection(ArrayList<Any?>()))
        val scanner = Scanner(Files.readString(Path.of(program)))
        val tokens = scanner.scanTokens()
        val parser = Parser(tokens)
        val ast = parser.parse()
        try {
            interpreter.execute(ast)
        }
        catch (e: RuntimeError) {
            error(e.line, e.s)
            exitProcess(1)
        } /*catch (e: Exception) {
                error(-1, e.toString())
                exit(1)
            }*/
    } else while(true) {
        repl = true
        val scanner: Scanner? = console.readLine("oasis -> ")?.let { Scanner(it) }
        if(scanner != null){
            val tokens: List<Token> = scanner.scanTokens()
            val parser = Parser(tokens)
            val ast = parser.parse()
            try {
                interpreter.execute(ast)
            }
            catch (e: RuntimeError) {
                error(e.line, e.s)
            } catch (e: Exception) {
                println(e)
            }
        }
    }
}

fun error(line: Int, reason: String) {
    println("On line $line: $reason")
}