package me.snwy.oasis.standardLibrary

import me.snwy.oasis.*
import org.knowm.xchart.QuickChart
import org.knowm.xchart.SwingWrapper
import javax.swing.JFrame

val plot = Module("Plot") { env, interpreter ->
    val plot = OasisPrototype(base, -1, interpreter).apply {
        set("function", KotlinFunction4<Unit, String, Double, Double, OasisCallable> { interpreter, x, y, precision, z ->
            val xData = 0.until(y.toInt() * precision.toInt()).map { it.toDouble() / precision }
            val yData = xData.map { z.call(interpreter, listOf(it)) as? Double ?: throw RuntimeError(line, "Plot function must return numeric value") }
            val chart = QuickChart.getChart(x, "X", "Y", "f(x)", xData.toDoubleArray(), yData.toDoubleArray())
            val frame = SwingWrapper(chart).displayChart()
            javax.swing.SwingUtilities.invokeLater {
                frame.defaultCloseOperation = JFrame.DISPOSE_ON_CLOSE
            }
        })
        set("functionWithRange", KotlinFunction5<Unit, String, Double, Double, Double, OasisCallable> { interpreter, x, range, y, precision, z ->
            val xData = range.toInt().until(y.toInt() * precision.toInt() + 1).map { it.toDouble() / precision }
            val yData = xData.map { z.call(interpreter, listOf(it)) as? Double ?: throw RuntimeError(line, "Plot function must return numeric value") }
            val chart = QuickChart.getChart(x, "X", "Y", "f(x)", xData.toDoubleArray(), yData.toDoubleArray())
            val frame = SwingWrapper(chart).displayChart()
            javax.swing.SwingUtilities.invokeLater {
                frame.defaultCloseOperation = JFrame.DISPOSE_ON_CLOSE
            }
        })
        set("data", KotlinFunction2<Unit, String, ArrayList<Any?>> { x, y ->
            val data = if (y.all { it is Number }) {
                ArrayList(y.map { it as Double })
            } else {
                throw RuntimeError(line, "data must be numeric")
            }
            val xAxis = 0.until(data.size).map { it.toDouble() }
            val chart = QuickChart.getChart(x, "X", "Y", "data", xAxis.toDoubleArray(), data.toDoubleArray())
            val frame = SwingWrapper(chart).displayChart()
            javax.swing.SwingUtilities.invokeLater {
                frame.defaultCloseOperation = JFrame.DISPOSE_ON_CLOSE
            }
        })
    }
    env.define("plot", plot)
}