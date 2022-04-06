package com.example.betterclock

import java.time.LocalTime
import java.util.*

enum class AlarmDay(val idx: Int) {
    SUNDAY(0),
    MONDAY(1),
    TUESDAY(2),
    WEDNESDAY(3),
    THURSDAY(4),
    FRIDAY(5),
    SATURDAY(6),
}

class Alarm(
    val id: String,
    var time: LocalTime,
    var label: String?,
    var enabled: Boolean,
    val days: Array<Boolean>
) {

    fun clone(): Alarm {
        return Alarm(id, time, label, enabled, days.clone())
    }

    fun enableDay(day: AlarmDay): Unit {
        days[day.idx] = true
    }

    fun disableDay(day: AlarmDay): Unit {
        days[day.idx] = false
    }

    fun toggleDay(day: AlarmDay): Unit {
        days[day.idx] = !days[day.idx]
    }

    fun enable(): Unit {
        enabled = true
    }

    fun disable(): Unit {
        enabled = false
    }

    fun toggle(): Unit {
        enabled = !enabled
    }

    companion object {
        fun genAlarmID(): String {
            val uuid = UUID.randomUUID()
            val now = System.nanoTime()
            return "alarm-$uuid-$now"
        }

        fun fromParts(
            id: String,
            time: LocalTime,
            label: String?,
            enabled: Boolean,
            days: Array<Boolean>
        ): Alarm =
            Alarm(id, time, label, enabled, days.clone())

        fun empty(time: LocalTime, id: String = genAlarmID()): Alarm =
            Alarm(
                id = id,
                time = time,
                label = null,
                enabled = true,
                days = Array(AlarmDay.values().size) { _ -> true })
    }
}

