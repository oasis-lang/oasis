package me.snwy.oasis

import org.eclipse.lsp4j.*
import org.eclipse.lsp4j.jsonrpc.Launcher
import org.eclipse.lsp4j.jsonrpc.messages.Either
import org.eclipse.lsp4j.services.*
import java.io.InputStream
import java.io.OutputStream
import java.nio.file.Files
import java.nio.file.Path
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ExecutionException
import java.util.concurrent.Future


class OasisLanguageServer : LanguageServer, LanguageClientAware{
    private val textDocumentService: TextDocumentService
    override fun getTextDocumentService(): TextDocumentService {
        return textDocumentService
    }

    private val workspaceService: WorkspaceService
    override fun getWorkspaceService(): WorkspaceService {
        return workspaceService
    }

    lateinit var clientCapabilities: ClientCapabilities

    lateinit var languageClient: LanguageClient

    var shutdown = 1

    init {
        textDocumentService = OasisTextDocumentService(this)
        workspaceService = OasisWorkspaceService(this)
    }

    override fun initialize(params: InitializeParams): CompletableFuture<InitializeResult> {
        val response = InitializeResult(ServerCapabilities())
        response.capabilities.textDocumentSync = Either.forLeft(TextDocumentSyncKind.Full)
        clientCapabilities = params.capabilities
        if (!isDynamicCompletionRegistration()) {
            response.capabilities.completionProvider = CompletionOptions()
        }
        return CompletableFuture.supplyAsync { response }
    }

    override fun initialized(params: InitializedParams?) {
        if (isDynamicCompletionRegistration()) {
            val completionRegistrationOptions = CompletionRegistrationOptions()
            val completionRegistration = Registration(
                UUID.randomUUID().toString(),
                "textDocument/completion", completionRegistrationOptions
            )
            languageClient.registerCapability(RegistrationParams(listOf(completionRegistration)))
        }
    }

    override fun shutdown(): CompletableFuture<Any> {
        shutdown = 0
        return CompletableFuture.supplyAsync {
            println("Shutdown")
        }
    }

    override fun exit() {
        System.exit(shutdown)
    }

    override fun connect(client: LanguageClient?) {
        languageClient = client!!
    }

    private fun isDynamicCompletionRegistration(): Boolean {
        val textDocumentCapabilities = clientCapabilities.textDocument
        return textDocumentCapabilities != null && textDocumentCapabilities.completion != null && java.lang.Boolean.FALSE == textDocumentCapabilities.completion.dynamicRegistration
    }
}

class OasisWorkspaceService(val languageServer: OasisLanguageServer) : WorkspaceService {
    override fun didChangeConfiguration(params: DidChangeConfigurationParams) {
        languageServer.languageClient.logMessage(MessageParams(MessageType.Info, "DidChangeConfiguration"))
    }

    override fun didChangeWatchedFiles(params: DidChangeWatchedFilesParams?) {
        languageServer.languageClient.logMessage(MessageParams(MessageType.Info, "DidChangeWatchedFiles"))
    }

    override fun didRenameFiles(params: RenameFilesParams?) {
        languageServer.languageClient.logMessage(MessageParams(MessageType.Info, "DidRenameFiles"))
    }
}

class OasisTextDocumentService(val languageServer: OasisLanguageServer) : TextDocumentService {

    private lateinit var text: String
    private var variables: Map<String, Any> = mutableMapOf()

    private fun fromLineColToIndex(line: Int, col: Int): Int {
        var index = 0
        for (i in 0 until line) {
            index += text.lines()[i].length + 1
        }
        index += col
        return index
    }

    override fun didOpen(params: DidOpenTextDocumentParams?) {
        languageServer.languageClient.logMessage(MessageParams(MessageType.Info, "DidOpen"))
        text = params!!.textDocument.text
    }

    override fun didChange(params: DidChangeTextDocumentParams?) {
        // update text with the changes
        languageServer.languageClient.logMessage(MessageParams(MessageType.Info, "DidChange 1: $text"))
        params?.contentChanges?.forEach {
            text = if (it.range == null) {
                it.text
            } else {
                text.replaceRange(fromLineColToIndex(it.range.start.line, it.range.start.character), fromLineColToIndex(it.range.end.line, it.range.end.character), it.text)
            }
        }

        val diagnostics = mutableListOf<Diagnostic>()
        var parsed: Stmt? = null
        var parser: Parser? = null
        try {
            parser = Parser(Scanner(text).scanTokens())
            parsed = parser.parse()
        } catch (e: ParseException) {
            diagnostics.add(Diagnostic(Range(Position(e.line - 1, e.column), Position(e.line - 1, e.column)), e.parseMessage, DiagnosticSeverity.Error, "oasis parser"))
        }
        val contextVisitor = ContextVisitor()
        parsed?.accept(contextVisitor)
        variables = contextVisitor.variables
        diagnostics.addAll(contextVisitor.diagnostics)
        languageServer.languageClient.publishDiagnostics(PublishDiagnosticsParams(params!!.textDocument.uri, diagnostics))
        languageServer.languageClient.logMessage(MessageParams(MessageType.Info, "DidChange 2: $text"))
    }

    override fun didClose(params: DidCloseTextDocumentParams?) {
        languageServer.languageClient.logMessage(MessageParams(MessageType.Info, "DidClose"))
    }

    override fun didSave(params: DidSaveTextDocumentParams?) {
        languageServer.languageClient.logMessage(MessageParams(MessageType.Info, "DidSave"))
    }

    override fun completion(position: CompletionParams?): CompletableFuture<Either<MutableList<CompletionItem>, CompletionList>> {
        languageServer.languageClient.logMessage(MessageParams(MessageType.Info, text))
        if (text.isEmpty()) {
            return CompletableFuture.supplyAsync { Either.forLeft(mutableListOf()) }
        }
        val completions = mutableListOf<CompletionItem>()
        if (position?.position?.line!! > text.lines().size - 1) {
            return CompletableFuture.supplyAsync { Either.forLeft(mutableListOf()) }
        }
        val line = text.lines()[position.position?.line ?: 0]
        languageServer.languageClient.logMessage(MessageParams(MessageType.Info, "Completion A: $line"))
        languageServer.languageClient.logMessage(MessageParams(MessageType.Info, "Completion B: ${position?.position?.character} of ${line.length} with $line"))
        val lineStart = if ((position.position?.character ?: 0) > line.length - 1) {
            line
        } else {
            line.substring(0, position.position?.character ?: 0)
        }
        languageServer.languageClient.logMessage(MessageParams(MessageType.Info, "Completion C: $line : $lineStart"))

        if (lineStart.endsWith(":")) {
            completions.add(CompletionItem("toString").also {
                it.detail = "toString"
                it.insertText = "toString()"
                it.kind = CompletionItemKind.Method
            })
            completions.add(CompletionItem("hashCode").also {
                it.detail = "hashCode"
                it.insertText = "hashCode()"
                it.kind = CompletionItemKind.Method
            })
            val attemptedCompletion = lineStart.substring(0, lineStart.length - 2).split(" ").last()
            val candidates = ArrayList<CompletionItem>()
            variables.forEach { pair ->
                if (pair.key.startsWith(attemptedCompletion)) {
                    if(pair.value is PrototypeContext) {
                        (pair.value as PrototypeContext).map.forEach { pair ->
                            candidates.add(CompletionItem(pair.key).also {
                                it.detail = pair.key
                                it.insertText = pair.key
                                it.kind = when (pair.value) {
                                    is Proto -> CompletionItemKind.Struct
                                    is Func -> CompletionItemKind.Function
                                    else -> CompletionItemKind.Variable
                                }
                            })
                        }
                    }
                }
            }
            completions.addAll(candidates)
        }

        else if (lineStart.endsWith("fn")) {
            completions.add(CompletionItem("fn =>").also {
                it.detail = "fn (lambda)"
                it.insertText = "fn(/* args */) => /* expression */"
                it.kind = CompletionItemKind.Snippet
            })
            completions.add(CompletionItem("fn").also {
                it.detail = "fn (body)"
                it.insertText = "fn(/* args */)\n\t/* body */\nend"
                it.kind = CompletionItemKind.Snippet
            })
        }

        else if (lineStart.endsWith("proto")) {
            completions.add(CompletionItem("proto").also {
                it.detail = "proto"
                it.insertText = "proto\n\t/* body */\nend"
                it.kind = CompletionItemKind.Snippet
            })
            completions.add(CompletionItem("proto < /* superclass */").also {
                it.detail = "proto (inherits)"
                it.insertText = "proto < /* superclass */\n\t/* body */\nend"
                it.kind = CompletionItemKind.Snippet
            })
        }

        else if (lineStart.endsWith("is")) {
            completions.add(CompletionItem("is").also {
                it.detail = "is"
                it.insertText = "is /* expression */\n\t/* value */ =>\n\t\t/* body */\n\tend\nend"
                it.kind = CompletionItemKind.Keyword
            })
        }
        val attemptedCompletion = lineStart.split(" ", "(", ")", "[", "]", "{", "}").last()
        val candidates = ArrayList<CompletionItem>()
        variables.forEach { pair ->
            if(attemptedCompletion.isEmpty() || pair.key.startsWith(attemptedCompletion)) {
                candidates.add(CompletionItem(pair.key).also {
                    it.detail = pair.key
                    it.insertText = pair.key
                    it.kind = when (pair.value) {
                        is PrototypeContext -> CompletionItemKind.Struct
                        is Func -> CompletionItemKind.Function
                        else -> CompletionItemKind.Variable
                    }
                })
            }
        }
        completions.addAll(candidates)
        return CompletableFuture.supplyAsync {
            return@supplyAsync Either.forLeft(completions)
        }
    }
}

class OasisLanguageServiceLauncher {
    fun main() {
        startServer(System.`in`, System.out)
    }

    fun startServer(`in`: InputStream?, out: OutputStream?) {
        val server = OasisLanguageServer()
        val launcher = Launcher.createLauncher(server, LanguageClient::class.java, `in`, out)
        val client = launcher.remoteProxy
        server.connect(client)
        val startListening: Future<*> = launcher.startListening()
        startListening.get()
    }
}