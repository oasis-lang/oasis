import java.nio.file.Files
import java.nio.file.Path

var globalInterpreter : Interpreter? = null
var repl = false
fun main(args: Array<String>) {
    if (args.contains("--welcome") or args.contains("-w")) {
        println("Welcome to the Oasis!")
    }
    var interpreter = Interpreter()
    globalInterpreter = interpreter
    (interpreter.environment.get(Token(TokenType.IDENTIFIER, "sys", null, -1)) as OasisPrototype).set("argv", ArrayList<Any?>())
    var program = args.find {
        Files.exists(Path.of(it))
    }
    if(program != null) {
        (interpreter.environment.get(Token(TokenType.IDENTIFIER, "sys", null, -1)) as OasisPrototype).set("argv", args.copyOfRange(1, args.size).toCollection(ArrayList<Any?>()))
        var scanner = Scanner(Files.readString(Path.of(program)))
        var tokens = scanner.scanTokens()
        var parser = Parser(tokens)
        var ast = parser.parse()
        try {
            interpreter.execute(ast)
        }
        catch (e: RuntimeError) {
            error(e.line, e.s)
            System.exit(1)
        } /*catch (e: Exception) {
                error(-1, e.toString())
                exit(1)
            }*/
    } else while(true) {
        repl = true
        print("oasis> ")
        var scanner: Scanner? = readLine()?.let { Scanner(it) }
        if(scanner != null){
            var tokens: List<Token> = scanner.scanTokens()
            var parser = Parser(tokens)
            var ast = parser.parse()
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