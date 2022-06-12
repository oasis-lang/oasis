package me.snwy.oasis.standardLibrary

import me.snwy.oasis.KotlinFunction0
import me.snwy.oasis.KotlinFunction1
import me.snwy.oasis.KotlinFunction2
import me.snwy.oasis.OasisPrototype

val math = Module("math") { it, interpreter ->
    val math = OasisPrototype(base, -1, interpreter).apply {
        set("pi", Math.PI)
        set("sin", KotlinFunction1(Math::sin))
        set("abs", KotlinFunction1<Double, Double>(Math::abs))
        set("cos", KotlinFunction1(Math::cos))
        set("ceil", KotlinFunction1(Math::ceil))
        set("floor", KotlinFunction1(Math::floor))
        set("sqrt", KotlinFunction1(Math::sqrt))
        set("exp", KotlinFunction1(Math::exp))
        set("log", KotlinFunction1(Math::log))
        set("pow", KotlinFunction2(Math::pow))
        set("sq", KotlinFunction1<Double, Double> { it -> it * it })
        set("cb", KotlinFunction1<Double, Double> { it -> it * it * it })
        set("cbrt", KotlinFunction1(Math::cbrt))
        set("tan", KotlinFunction1(Math::tan))
        set("atan", KotlinFunction1(Math::atan))
        set("atan2", KotlinFunction2(Math::atan2))
        set("sinh", KotlinFunction1(Math::sinh))
        set("cosh", KotlinFunction1(Math::cosh))
        set("tanh", KotlinFunction1(Math::tanh))
        set("asin", KotlinFunction1(Math::asin))
        set("acos", KotlinFunction1(Math::acos))
        set("round", KotlinFunction1(Math::round))
        set("random", KotlinFunction0(Math::random))
        set("toDegrees", KotlinFunction1(Math::toDegrees))
        set("toRadians", KotlinFunction1(Math::toRadians))
        set("max", KotlinFunction2<Double, Double, Double>(Math::max))
        set("min", KotlinFunction2<Double, Double, Double>(Math::min))
        set("hypot", KotlinFunction2(Math::hypot))
        set("signum", KotlinFunction1(Math::signum))
        set("nextAfter", KotlinFunction2(Math::nextAfter))
        set("ulp", KotlinFunction1(Math::ulp))
        set("getExponent", KotlinFunction1(Math::getExponent))
        set("getExponent", KotlinFunction1(Math::getExponent))
        set("nextUp", KotlinFunction1(Math::nextUp))
        set("nextDown", KotlinFunction1(Math::nextDown))
        set("copySign", KotlinFunction2(Math::copySign))
        set("expm1", KotlinFunction1(Math::expm1))
        set("log1p", KotlinFunction1(Math::log1p))
    }
    it.define("math", math)
}