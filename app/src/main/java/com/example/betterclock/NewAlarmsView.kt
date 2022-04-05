package com.example.betterclock

import android.annotation.SuppressLint
import android.content.Context
import android.widget.LinearLayout
import android.widget.ScrollView

class NewAlarmEntry(
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

class NewAlarmsView(val globals: Globals, val updateSink: AlarmUpdateSink, val alarms: Array<Alarm>, context: Context?) : ScrollView(context) {

    @SuppressLint("ResourceType")
    fun buildViews(entries: Array<NewAlarmEntry>) {
        for (a in entries) {
            a.view = AlarmView(globals, a.alarm, updateSink, context)
        }
    }

    init {
        if (context != null) {
            val entries = Array(alarms.size) { i -> NewAlarmEntry(alarms[i]) }
            buildViews(entries)
            setBackgroundColor(globals.backgroundColor)
            val ll = LinearLayout(context)
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
}
