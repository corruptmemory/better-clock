package com.example.betterclock

import android.app.Activity
import android.os.Bundle
import java.time.LocalTime

class MainActivity : Activity() {

    private lateinit var alarmsView: AlarmsView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

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

        alarmsView = AlarmsView(times, this)
        setContentView(alarmsView)
    }


}