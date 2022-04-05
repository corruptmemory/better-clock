package com.example.betterclock

import android.content.Context
import android.util.AttributeSet
import android.widget.LinearLayout
import android.widget.Toolbar
import com.google.android.material.bottomnavigation.BottomNavigationView

class AppView : LinearLayout {
    private val globals: Globals
    private val toolbar: Toolbar = Toolbar(context)
    private val bottomNav: BottomNavigationView = BottomNavigationView(context)
    val alarms: Array<Alarm>
    private lateinit var alarmsView: AlarmsView

    constructor(
        globals: Globals,
        alarms: Array<Alarm>,
        context: Context?,
        attrs: AttributeSet?,
        defStyle: Int
    ) : super(
        context,
        attrs,
        defStyle
    ) {
        this.alarms = alarms
        this.globals = globals
        init()
    }

    constructor(
        globals: Globals,
        alarms: Array<Alarm>,
        context: Context?,
        attrs: AttributeSet?
    ) : super(context, attrs) {
        this.alarms = alarms
        this.globals = globals
        init()
    }

    constructor(
        globals: Globals,
        alarms: Array<Alarm>,
        context: Context?
    ) : super(context) {
        this.alarms = alarms
        this.globals = globals
        init()
    }

    fun init() {
        orientation = VERTICAL
        toolbar.id = 1
        toolbar.setBackgroundColor(globals.backgroundColor)

        alarmsView = AlarmsView(globals, { _ -> }, alarms, context)
        alarmsView.id = 2
        alarmsView.layoutParams = LayoutParams(
            LayoutParams.MATCH_PARENT,
            LayoutParams.WRAP_CONTENT
        )
        toolbar.title = "Better Clock"
        toolbar.layoutParams = LayoutParams(
            LayoutParams.MATCH_PARENT,
            LayoutParams.WRAP_CONTENT
        )
        bottomNav.id = 3
        bottomNav.setBackgroundColor(globals.backgroundColor)
        bottomNav.layoutParams = LayoutParams(
            LayoutParams.MATCH_PARENT,
            LayoutParams.WRAP_CONTENT
        )
        bottomNav.z = alarmsView.z + 1.0F
        bottomNav.menu.add("Home")
        bottomNav.menu.add("Alarms")
        bottomNav.menu.add("Timers")
        val tt: Toolbar = Toolbar(context)
        tt.id = 4
        tt.setBackgroundColor(globals.backgroundColor)
        tt.title = "bottom"
        tt.layoutParams = LayoutParams(
            LayoutParams.MATCH_PARENT,
            LayoutParams.WRAP_CONTENT
        )
        addView(toolbar)
//        addView(tt)
        addView(alarmsView)
        addView(bottomNav)
    }

}