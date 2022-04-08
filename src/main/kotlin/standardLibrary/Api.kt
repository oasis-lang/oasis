package standardLibrary

import KotlinFunction0
import KotlinFunction1
import KotlinFunction2
import OasisCallable
import OasisPrototype
import com.sun.net.httpserver.HttpServer
import globalInterpreter
import java.net.InetSocketAddress
import java.util.*

var api = Module("api") {
    val api = KotlinFunction1<OasisPrototype, Double> {
        val api = OasisPrototype(base, -1).apply {
            set("port", it.toInt())
            set("__server", HttpServer.create(InetSocketAddress(get("port") as Int), 0))
            set("get", KotlinFunction2<Unit, String, OasisCallable> { x, y ->
                (get("__server") as HttpServer).createContext(x) {
                    if ("GET" == it.requestMethod) {
                        val response = globalInterpreter?.let { it1 ->
                            y.call(
                                it1,
                                listOf(it.requestHeaders,
                                    String(it.requestBody.readBytes())))
                        } as ArrayList<Any?>
                        val responseCode = (response[0] as Double).toInt()
                        val responseText = response[1] as String
                        it.sendResponseHeaders(responseCode, responseText.toByteArray().size.toLong());
                        it.responseBody.write(responseText.toByteArray())
                        it.responseBody.flush()
                    } else {
                        it.sendResponseHeaders(405, -1)
                    }
                    it.close()
                }
            })
            set("post", KotlinFunction2<Unit, String, OasisCallable> { x, y ->
                (get("__server") as HttpServer).createContext(x) {
                    if ("POST" == it.requestMethod) {
                        val response = globalInterpreter?.let { it1 ->
                            y.call(
                                it1,
                                listOf(it.requestHeaders,
                                    String(it.requestBody.readBytes())))
                        } as ArrayList<Any?>
                        val responseCode = (response[0] as Double).toInt()
                        val responseText = response[1] as String
                        it.sendResponseHeaders(responseCode, responseText.toByteArray().size.toLong());
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
