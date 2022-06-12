package me.snwy.oasis.standardLibrary

import me.snwy.oasis.KotlinFunction0
import me.snwy.oasis.OasisPrototype
import java.time.LocalDateTime
import java.util.*

val time = Module("time") { it, interpreter ->
    val time = OasisPrototype(base, -1, interpreter)
    time.set("clock", KotlinFunction0 { _ -> Date().time.toDouble() })
    time.set("now", KotlinFunction0 { interpreter ->
        val currentTime = OasisPrototype(base, -1, interpreter)
        val time = LocalDateTime.now()
        currentTime.set("year", time.year)
        currentTime.set("month", time.monthValue)
        currentTime.set("day", time.dayOfMonth)
        currentTime.set("hour", time.hour)
        currentTime.set("min", time.minute)
        currentTime.set("sec", time.second)
        currentTime.set("toString", KotlinFunction0 { _ ->
            "${currentTime.get("hour")}:${currentTime.get("min")}:${currentTime.get("sec")} ${currentTime.get("day")}/${currentTime.get("month")}/${currentTime.get("year")}"
        })
        return@KotlinFunction0 currentTime
    })
    it.define("time", time)
}
