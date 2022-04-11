package com.example.betterclock

import android.app.Activity
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import androidx.compose.material3


class MainActivity : Activity() {

    private lateinit var appView: AppView

    private lateinit var defaultView: String

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putString("defaultView", defaultView)
        super.onSaveInstanceState(outState)
    }

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

        val store = DataStores.inMemoryTestStore()

        fun saveDefaultView(s: String) {
            defaultView = s
        }

        defaultView = if (savedInstanceState != null) {
            val sds = savedInstanceState.get("defaultView")
            if (sds != null) {
                sds as String
            } else {
                "Home"
            }
        } else {
            "Home"
        }

        appView = AppView(
            defaultView = defaultView,
            updateDefaultView = { s -> saveDefaultView(s) },
            globals = globals,
            store = store,
            this
        )
        setContentView(appView)
    }


}