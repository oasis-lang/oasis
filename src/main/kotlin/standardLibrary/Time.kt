package standardLibrary

import KotlinFunction0
import OasisPrototype
import java.util.*

val time = Module("time") {
    val time = OasisPrototype(base, -1)
    time.set("clock", KotlinFunction0 { Date().time.toDouble() })
    time.set("now", KotlinFunction0 {
        val currentTime = OasisPrototype(base, -1)
        currentTime.set("year", Calendar.YEAR)
        currentTime.set("month", Calendar.MONTH)
        currentTime.set("day", Calendar.DAY_OF_MONTH)
        currentTime.set("hour", Calendar.HOUR)
        currentTime.set("min", Calendar.MINUTE)
        currentTime.set("sec", Calendar.SECOND)
        currentTime.set("toString", KotlinFunction0 {
            "${currentTime.get("hour")}:${currentTime.get("min")}:${currentTime.get("sec")} ${currentTime.get("day")}/${currentTime.get("month")}/${currentTime.get("year")}"
        })
        return@KotlinFunction0 currentTime
    })
    it.define("time", time)
}
