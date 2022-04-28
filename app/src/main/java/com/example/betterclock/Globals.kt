package com.example.betterclock

import android.graphics.Typeface
import java.time.LocalTime
import java.time.format.DateTimeFormatter

class ColorVariants(
    val enabled: Int,
    val disabled: Int,
)

class NavColorVariants(
    val enabled: Int,
    val disabled: Int,
    val checked: Int,
)

class Colors(
    val primaryTextColor: ColorVariants,
    val backgroundColor: Int,
    val cardBackgroundColor: Int,
    val cardShadowStart: Int,
    val cardShadowEnd: Int,
)

class AlarmTheme(
    val colors: Colors,
    val primaryTypeface: Typeface,
    val alarmTypeface: Typeface,
)

class AlarmsViewTheme(
    val alarmTheme: AlarmTheme,
    val backgroundColor: Int,
)

class Globals(
    val primaryTypeface: Typeface,
    val primaryTextColor: NavColorVariants,
    val backgroundColor: Int,
    val alarmTheme: AlarmTheme,
    val is24HourFormat: Boolean,
) {
    fun alarmsTheme(): AlarmsViewTheme {
        return AlarmsViewTheme(
            alarmTheme = alarmTheme,
            backgroundColor = backgroundColor,
        )
    }
}
