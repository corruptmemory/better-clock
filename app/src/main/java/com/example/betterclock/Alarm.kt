package com.example.betterclock

import java.time.LocalTime
import java.util.*

enum class AlarmDay(val idx: Int, val short: String, val long: String, val single: String) {
    SUNDAY(0, "Sun", "Sunday", "S"),
    MONDAY(1, "Mon", "Monday", "M"),
    TUESDAY(2, "Tue", "Tuesday", "T"),
    WEDNESDAY(3, "Wed", "Wednesday", "W"),
    THURSDAY(4, "Thu", "Thursday", "T"),
    FRIDAY(5, "Fri", "Friday", "F"),
    SATURDAY(6, "Sat", "Saturday", "S"),
}

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
            String.format("%d:%02d %s", h, minute, ampm)
        }
    }

    fun format24H(leadingZero: Boolean = false): String {
        return if (leadingZero) {
            String.format("%02d:%02d %s", hour, minute)
        } else {
            String.format("%d:%02d %s", hour, minute)
        }
    }

    fun format(is24Hour: Boolean, leadingZero: Boolean = false): String =
        if (is24Hour) format24H(leadingZero) else format12H(leadingZero)

    companion object {
        fun fromLocalTime(t: LocalTime): AlarmTime = AlarmTime(t.hour, t.minute)
    }
}

class Alarm(
    val id: String,
    var time: AlarmTime,
    var label: String?,
    var enabled: Boolean,
    val days: Array<Boolean>,
    var alarmSelection: String?
) {

    fun clone(): Alarm {
        return Alarm(id, time, label, enabled, days.clone(), alarmSelection)
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

    fun allDaysOn(): Boolean {
        var r = true
        for (d in days) {
            r = r and d
        }
        return r
    }

    fun allDaysOff(): Boolean {
        var r = true
        for (d in days) {
            r = r and !d
        }
        return r
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
            days: Array<Boolean>,
            alarmSelection: String?
        ): Alarm =
            Alarm(id, AlarmTime.fromLocalTime(time), label, enabled, days.clone(), alarmSelection)

        fun empty(time: LocalTime, id: String = genAlarmID(), alarmSelection: String? = null): Alarm =
            Alarm(
                id = id,
                time = AlarmTime.fromLocalTime(time),
                label = null,
                enabled = true,
                days = Array(AlarmDay.values().size) { _ -> true },
                alarmSelection = alarmSelection)
    }
}

