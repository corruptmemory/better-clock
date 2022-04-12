package com.example.betterclock

import android.app.Activity
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import androidx.core.content.ContextCompat
import androidx.core.graphics.ColorUtils

class MainActivity : Activity() {

    private lateinit var appView: AppView
    private lateinit var defaultView: String
    private var darkTheme: Boolean = true

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putString("defaultView", defaultView)
        super.onSaveInstanceState(outState)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val globals = createGlobals()

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

    private fun createGlobals(): Globals {
        val colors = if (darkTheme) {
            Colors(
                primaryTextColor = ColorVariants(
                    enabled = ContextCompat.getColor(this, androidx.appcompat.R.color.primary_text_default_material_dark),
                    disabled = ContextCompat.getColor(this, androidx.appcompat.R.color.primary_text_disabled_material_dark)
                ),
                backgroundColor = ContextCompat.getColor(this, androidx.appcompat.R.color.background_material_dark),
                cardBackgroundColor = ContextCompat.getColor(this, androidx.cardview.R.color.cardview_dark_background),
                cardShadowStart = ContextCompat.getColor(this, androidx.cardview.R.color.cardview_shadow_start_color),
                cardShadowEnd = ContextCompat.getColor(this, androidx.cardview.R.color.cardview_shadow_end_color),
            )
        } else {
            Colors(
                primaryTextColor = ColorVariants(
                    enabled = ContextCompat.getColor(this, androidx.appcompat.R.color.primary_text_default_material_light),
                    disabled = ContextCompat.getColor(this, androidx.appcompat.R.color.primary_text_disabled_material_light)
                ),
                backgroundColor = ContextCompat.getColor(this, androidx.appcompat.R.color.background_material_light),
                cardBackgroundColor = ContextCompat.getColor(this, androidx.cardview.R.color.cardview_light_background),
                cardShadowStart = ContextCompat.getColor(this, androidx.cardview.R.color.cardview_shadow_start_color),
                cardShadowEnd = ContextCompat.getColor(this, androidx.cardview.R.color.cardview_shadow_end_color),
            )
        }

        return Globals(
            backgroundColor = colors.backgroundColor,
            primaryTextColor = NavColorVariants(
                enabled = ColorUtils.setAlphaComponent(colors.primaryTextColor.enabled, 255),
                disabled = ColorUtils.setAlphaComponent(colors.primaryTextColor.disabled, 255),
                checked = ColorUtils.setAlphaComponent(colors.primaryTextColor.enabled, 255),
            ),
            primaryTypeface = Typeface.DEFAULT,
            alarmTheme = AlarmTheme(
                colors = colors,
                primaryTypeface = Typeface.DEFAULT,
                alarmTypeface = Typeface.DEFAULT,
            ),
            is24HourFormat = android.text.format.DateFormat.is24HourFormat(this),
        )
    }
}