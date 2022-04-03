package standardLibrary

import KotlinFunction1
import KotlinFunction2
import OasisPrototype

val math = Module("math") {
    val math = OasisPrototype(base, -1).apply {
        set("pi", Math.PI)
        set("sin", KotlinFunction1(Math::sin))
        set("abs", KotlinFunction1<Double, Double>(Math::abs))
        set("cos", KotlinFunction1(Math::cos))
        set("ceil", KotlinFunction1(Math::ceil))
        set("floor", KotlinFunction1(Math::floor))
        set("round", KotlinFunction1(Math::round))
        set("sqrt", KotlinFunction1(Math::sqrt))
        set("exp", KotlinFunction1(Math::exp))
        set("log", KotlinFunction1(Math::log))
        set("pow", KotlinFunction2(Math::pow))
    }
    it.define("math", math)
}