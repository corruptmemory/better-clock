package com.example.betterclock

import android.app.Activity
import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toolbar
import com.google.android.material.bottomnavigation.BottomNavigationView


class AppView : ViewGroup {
    private val globals: Globals
    private val toolbar: Toolbar = Toolbar(context)
    private val bottomNav: BottomNavigationView = BottomNavigationView(context)
    val alarms: Array<Alarm>
    private lateinit var alarmsView: AlarmsView
    private val homeView: TextView = TextView(context)
    private val timersView: TextView = TextView(context)
    private var activeView: View? = null
    private var displayWidth: Int = 0
    private val defaultView: String
    private val updateDefaultView: (String) -> Unit

    constructor(
        defaultView: String,
        updateDefaultView: (String) -> Unit,
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
        this.defaultView = defaultView
        this.updateDefaultView = updateDefaultView
        this.alarms = alarms
        this.globals = globals
        init()
    }

    constructor(
        defaultView: String,
        updateDefaultView: (String) -> Unit,
        globals: Globals,
        alarms: Array<Alarm>,
        context: Context?,
        attrs: AttributeSet?
    ) : super(context, attrs) {
        this.defaultView = defaultView
        this.updateDefaultView = updateDefaultView
        this.alarms = alarms
        this.globals = globals
        init()
    }

    constructor(
        defaultView: String,
        updateDefaultView: (String) -> Unit,
        globals: Globals,
        alarms: Array<Alarm>,
        context: Context?
    ) : super(context) {
        this.defaultView = defaultView
        this.updateDefaultView = updateDefaultView
        this.alarms = alarms
        this.globals = globals
        init()
    }

    fun init() {
        val activity = (context as Activity)
        val dm = activity.resources.displayMetrics
        displayWidth = dm.widthPixels
        toolbar.setBackgroundColor(globals.backgroundColor)
        alarmsView = AlarmsView(globals, { _ -> }, alarms, context)
        alarmsView.id = 2
        bottomNav.setBackgroundColor(globals.backgroundColor)
        bottomNav.z = alarmsView.z + 1.0F
        homeView.text = "Home"
        homeView.id = 1
        timersView.text = "Timers"
        timersView.id = 3
        fun setActiveView(v: View, title: String) {
            if (activeView != v) {
                toolbar.title = title
                var toDelete: View? = null
                for (i in 0..childCount) {
                    val c = getChildAt(i)
                    if (c == toolbar || c == bottomNav) {
                        continue
                    }
                    toDelete = c
                    break
                }
                if (toDelete != null) {
                    removeView(toDelete)
                }
                addView(v)
                activeView = v
                updateDefaultView(title)
            }
        }

        bottomNav.setOnItemSelectedListener { v ->
            when (v.itemId) {
                1 -> {
                    setActiveView(homeView, "Home")
                    true
                }
                2 -> {
                    setActiveView(alarmsView, "Alarms")
                    true
                }
                3 -> {
                    setActiveView(timersView, "Timers")
                    true
                }
                else -> false
            }
        }
        bottomNav.menu.add(0, 1, 0, "Home")
        bottomNav.menu.add(0, 2, 1, "Alarms")
        bottomNav.menu.add(0, 3, 2, "Timers")

        addView(toolbar)
        addView(bottomNav)
        when (defaultView) {
            "Home" -> {
                bottomNav.selectedItemId = 1
            }
            "Alarms" -> {
                bottomNav.selectedItemId = 2
            }
            "Timers" -> {
                bottomNav.selectedItemId = 3
            }
        }
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        val contentLeft = this.paddingLeft
        val contentTop = this.paddingTop
        val contentRight = this.measuredWidth - this.paddingRight
        val contentBottom = this.measuredHeight - this.paddingBottom
        val contentWidth = contentRight - contentLeft
        val contentHeight = contentBottom - contentTop
        toolbar.measure(
            MeasureSpec.makeMeasureSpec(this.measuredWidth, MeasureSpec.AT_MOST),
            MeasureSpec.makeMeasureSpec(contentHeight, MeasureSpec.AT_MOST)
        )
        val toolbarHeight = toolbar.measuredHeight
        toolbar.layout(0, 0, this.measuredWidth, toolbarHeight)
        bottomNav.measure(
            MeasureSpec.makeMeasureSpec(this.measuredWidth, MeasureSpec.AT_MOST),
            MeasureSpec.makeMeasureSpec(contentHeight, MeasureSpec.AT_MOST)
        )
        val bottomNavHeight = bottomNav.measuredHeight
        bottomNav.layout(0, contentHeight - bottomNavHeight, this.measuredWidth, contentHeight)
        val remainingHeight = contentHeight - toolbarHeight - bottomNavHeight
        activeView!!.measure(
            MeasureSpec.makeMeasureSpec(contentWidth, MeasureSpec.AT_MOST),
            MeasureSpec.makeMeasureSpec(remainingHeight, MeasureSpec.AT_MOST)
        )
        activeView!!.layout(contentLeft, toolbarHeight, contentRight, contentHeight - bottomNavHeight)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val activity = (context as Activity)
        val dm = activity.resources.displayMetrics
        val desiredWidth = dm.widthPixels
        val desiredHeight = dm.heightPixels

        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)

        val width: Int
        val height: Int

        //Measure Width

        //Measure Width
        width = if (widthMode == MeasureSpec.EXACTLY) {
            //Must be this size
            widthSize
        } else if (widthMode == MeasureSpec.AT_MOST) {
            //Can't be bigger than...
            Math.min(desiredWidth, widthSize)
        } else {
            //Be whatever you want
            desiredWidth
        }

        //Measure Height

        //Measure Height
        height = if (heightMode == MeasureSpec.EXACTLY) {
            //Must be this size
            heightSize
        } else if (heightMode == MeasureSpec.AT_MOST) {
            //Can't be bigger than...
            Math.min(desiredHeight, heightSize)
        } else {
            //Be whatever you want
            desiredHeight
        }

        //MUST CALL THIS
        setMeasuredDimension(width, height)
    }

}