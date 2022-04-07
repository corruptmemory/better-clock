package com.example.betterclock

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
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

@Serializable
class AlarmTime(val hour: Int, val minute: Int) {

    fun timeRemaining(from: LocalTime = LocalTime.now()): Int {
        val target = hour * 3600 + minute * 60
        val test = from.hour * 3600 + from.minute * 60 + from.second
        return target - test
    }

    fun format12H(leadingZero: Boolean = false, am: String = "am", pm: String = "pm"): String {
        val ampm = if (hour < 12) am else pm
        var h = if (hour > 12) hour - 12 else hour
        h = if (h == 0) 12 else h
        return if (leadingZero) {
            String.format("%02d:%02d %s", h, minute, ampm)
        } else {
            String.format("%2d:%02d %s", h, minute, ampm)
        }
    }

    fun format24H(leadingZero: Boolean = false): String {
        return if (leadingZero) {
            String.format("%02d:%02d %s", hour, minute)
        } else {
            String.format("%2d:%02d %s", hour, minute)
        }
    }

    fun format(is24Hour: Boolean, leadingZero: Boolean = false): String =
        if (is24Hour) format24H(leadingZero) else format12H(leadingZero)

    companion object {
        fun fromLocalTime(t: LocalTime): AlarmTime = AlarmTime(t.hour, t.minute)
    }
}

@Serializable
class Alarm(
    val id: String,
    var time: AlarmTime,
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

    fun toJson(): String {
        return Json.encodeToString(this)
    }

    companion object {
        fun genAlarmID(): String {
            val uuid = UUID.randomUUID()
            val now = System.nanoTime()
            return "alarm-$uuid-$now"
        }

        fun fromJson(jsonStr: String): Alarm {
            return Json.decodeFromString(jsonStr)
        }

        fun fromParts(
            id: String,
            time: LocalTime,
            label: String?,
            enabled: Boolean,
            days: Array<Boolean>
        ): Alarm =
            Alarm(id, AlarmTime.fromLocalTime(time), label, enabled, days.clone())

        fun empty(time: LocalTime, id: String = genAlarmID()): Alarm =
            Alarm(
                id = id,
                time = AlarmTime.fromLocalTime(time),
                label = null,
                enabled = true,
                days = Array(AlarmDay.values().size) { _ -> true })
    }
}

