package com.example.betterclock

import android.app.Activity
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import java.time.LocalTime

class MainActivity : Activity() {

    private lateinit var alarmsView: NewAlarmsView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val globals = Globals(
            primaryTextColor = this.getColor(androidx.appcompat.R.color.primary_text_default_material_light),
            backgroundColor = this.getColor(androidx.appcompat.R.color.background_material_light),
            cardBackgroundColor = Color.DKGRAY,
            primaryTypeface = Typeface.DEFAULT,
            alarmTypeface = Typeface.DEFAULT,
            is24HourFormat = android.text.format.DateFormat.is24HourFormat(this)
        )

        val times = arrayOf(
            LocalTime.now(),
            LocalTime.now(),
            LocalTime.now(),
            LocalTime.now(),
            LocalTime.now(),
            LocalTime.now(),
            LocalTime.now(),
            LocalTime.now(),
            LocalTime.now(),
            LocalTime.now(),
            LocalTime.now(),
            LocalTime.now(),
            LocalTime.now(),
            LocalTime.now(),
            LocalTime.now(),
            LocalTime.now(),
            LocalTime.now(),
            LocalTime.now(),
            LocalTime.now(),
            LocalTime.now(),
            LocalTime.now(),
            LocalTime.now(),
            LocalTime.now(),
            LocalTime.now(),
            LocalTime.now(),
            LocalTime.now(),
            LocalTime.now(),
            LocalTime.now(),
            LocalTime.now(),
            LocalTime.now(),
            LocalTime.now(),
            LocalTime.now(),
            LocalTime.now(),
            LocalTime.now(),
            LocalTime.now(),
            LocalTime.now(),
            LocalTime.now(),
            LocalTime.now(),
            LocalTime.now(),
            LocalTime.now(),
            LocalTime.now(),
            LocalTime.now(),
            LocalTime.now(),
            LocalTime.now(),
            LocalTime.now(),
            LocalTime.now(),
            LocalTime.now(),
            LocalTime.now(),
        )

        val alarms: Array<Alarm> = times.map { t -> Alarm.empty(t) }.toTypedArray()

        alarmsView = NewAlarmsView(globals = globals, updateSink = { _ -> }, alarms = alarms, context = this)
//        alarmsView = AlarmsView(times, this)
        setContentView(alarmsView)
    }


}