package standardLibrary

import KotlinFunction0
import OasisPrototype
import java.time.LocalDateTime
import java.util.*

val time = Module("time") {
    val time = OasisPrototype(base, -1)
    time.set("clock", KotlinFunction0 { Date().time.toDouble() })
    time.set("now", KotlinFunction0 {
        val currentTime = OasisPrototype(base, -1)
        val time = LocalDateTime.now()
        currentTime.set("year", time.year)
        currentTime.set("month", time.monthValue)
        currentTime.set("day", time.dayOfMonth)
        currentTime.set("hour", time.hour)
        currentTime.set("min", time.minute)
        currentTime.set("sec", time.second)
        currentTime.set("toString", KotlinFunction0 {
            "${currentTime.get("hour")}:${currentTime.get("min")}:${currentTime.get("sec")} ${currentTime.get("day")}/${currentTime.get("month")}/${currentTime.get("year")}"
        })
        return@KotlinFunction0 currentTime
    })
    it.define("time", time)
}
