package com.example.betterclock

import android.app.TimePickerDialog
import android.content.Context
import android.graphics.drawable.Drawable
import android.widget.LinearLayout
import android.widget.ScrollView
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat

class AlarmEntry(
    var alarm: Alarm,
) {
    lateinit var view: AlarmView

    fun expand() {
        view.expand()
    }

    fun collapse() {
        view.collapse()
    }
}

class AlarmsView(
    val globals: Globals,
    val alarmStore: AlarmStore,
    store: DataStore,
    context: Context,
    rawCollapseDrawable: Drawable = DrawableCompat.wrap(ContextCompat.getDrawable(context, R.drawable.ic_collapse)!!),
) : ScrollView(context) {

    private val ll: LinearLayout
    private val collapseDrawable: Drawable

    private fun buildViews(entries: Array<AlarmEntry>) {
        for (a in entries) {
            a.view = AlarmView(globals, a.alarm, alarmStore, context, collapseDrawable)
            a.view.onTimeClicked = { v ->
                for (cv in entries) {
                    if (cv.view != v) {
                        cv.view.collapse()
                    }
                }
                v.expand()
                val tp = TimePickerDialog(context,
                    { _, h: Int, m: Int ->
                        v.setTime(AlarmTime(h, m))
                    }, v.alarm.time.hour, v.alarm.time.minute, globals.is24HourFormat
                )
                tp.show()
            }
            a.view.onExpandClicked = { v ->
                for (cv in entries) {
                    if (cv.view != v) {
                        cv.view.collapse()
                    }
                }
                v.toggleExpand()
            }
        }
    }

    init {
        val alarms = store.alarmStore().list()
        val entries = Array(alarms.size) { i -> AlarmEntry(alarms[i]) }
        collapseDrawable = rawCollapseDrawable
        collapseDrawable.setTint(globals.alarmTheme.colors.primaryTextColor.enabled)
        buildViews(entries)
        setBackgroundColor(globals.backgroundColor)
        ll = LinearLayout(context)
        addView(ll)
        ll.orientation = LinearLayout.VERTICAL
        for (e in entries) {
            val lp = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            lp.setMargins(5.dp, 3.dp, 5.dp, 4.dp)
            e.view.layoutParams = lp
            ll.addView(e.view)
        }
    }
}
