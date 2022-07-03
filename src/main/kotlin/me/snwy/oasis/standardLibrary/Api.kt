package me.snwy.oasis.standardLibrary

import com.sun.net.httpserver.HttpServer
import me.snwy.oasis.*
import java.net.InetSocketAddress

var api = Module("api") { it, _ ->
    val api = KotlinFunction1<OasisPrototype, Double> { interpreter, apiIt ->
        val api = OasisPrototype(base, line, interpreter).apply {
            set("port", apiIt.toInt())
            set("__server", HttpServer.create(InetSocketAddress(get("port") as Int), 0))
            set("get", KotlinFunction2<Unit, String, OasisCallable> { interpreter, x, y ->
                (get("__server") as HttpServer).createContext(x) {
                    if ("GET" == it.requestMethod) {
                        val response = interpreter.let { it1 ->
                            y.call(
                                it1,
                                listOf(
                                    it.requestHeaders,
                                    String(it.requestBody.readBytes())
                                )
                            )
                        } as? ArrayList<*> ?: throw OasisException("API: get: response is not an array")
                        val responseCode = (response[0] as? Number)?.toInt() ?: throw OasisException("API: get: first response item is not a status code")
                        val responseText = response[1] as? String ?: throw OasisException("API: get: second response item is not a response body")
                        it.sendResponseHeaders(responseCode, responseText.toByteArray().size.toLong())
                        it.responseBody.write(responseText.toByteArray())
                        it.responseBody.flush()
                    } else {
                        it.sendResponseHeaders(405, -1)
                    }
                    it.close()
                }
            })
            set("post", KotlinFunction2<Unit, String, OasisCallable> { interpreter, x, y ->
                (get("__server") as HttpServer).createContext(x) {
                    if ("POST" == it.requestMethod) {
                        val response = interpreter.let { it1 ->
                            y.call(
                                it1,
                                listOf(
                                    it.requestHeaders,
                                    String(it.requestBody.readBytes())
                                )
                            )
                        } as? ArrayList<*> ?: throw OasisException("API: get: response is not an array")
                        val responseCode = (response[0] as? Number)?.toInt() ?: throw OasisException("API: get: first response item is not a status code")
                        val responseText = response[1] as? String ?: throw OasisException("API: get: second response item is not a response body")
                        it.sendResponseHeaders(responseCode, responseText.toByteArray().size.toLong())
                        it.responseBody.write(responseText.toByteArray())
                        it.responseBody.flush()
                    } else {
                        it.sendResponseHeaders(405, -1)
                    }
                    it.close()
                }
            })
            set("start", KotlinFunction0((get("__server") as HttpServer)::start))
            set("stop", KotlinFunction1((get("__server") as HttpServer)::stop))
        }
        api
    }
    it.define("api", api)
}
