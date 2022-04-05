package com.example.betterclock

import android.graphics.Typeface
import java.time.LocalTime
import java.time.format.DateTimeFormatter

class Globals(
    val primaryTextColor: Int,
    val backgroundColor: Int,
    val cardBackgroundColor: Int,
    val primaryTypeface: Typeface,
    val alarmTypeface: Typeface,
    val is24HourFormat: Boolean,
) {
    companion object {
        const val FORMAT_12 = "hh:mm a"
        const val FORMAT_AMPM = "a"
        const val FORMAT_24 = "HH:mm"
    }

    val formatter: DateTimeFormatter = if (is24HourFormat) {
        DateTimeFormatter.ofPattern(FORMAT_24)
    } else {
        DateTimeFormatter.ofPattern(FORMAT_12)
    }
    val amPMFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern(FORMAT_AMPM)

    fun formatAMPM(t: LocalTime):String {
        return t.format(amPMFormatter)
    }

    fun formatTime(t: LocalTime):String {
        return t.format(formatter)
    }
}