package standardLibrary
import Environment
import KotlinFunction0
import OasisPrototype

var base = OasisPrototype(null, -1)
object StandardLibrary {
    private var modules = arrayListOf( // Base modules
        io,
        string,
        time,
        type,
        list,
        range,
        sys,
        hashmap,
        json
    )

    fun addModule(x: Module) = modules.add(x)

    fun generateLib(x: Environment) {
        base.set("toString", KotlinFunction0 { return@KotlinFunction0 "<obj>" })
        modules.map { it.func(x) }
    }
}
