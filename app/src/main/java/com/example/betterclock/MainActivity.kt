package com.example.betterclock

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import java.time.LocalTime

class MainActivity : AppCompatActivity() {

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