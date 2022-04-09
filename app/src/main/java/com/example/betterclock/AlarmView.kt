package com.example.betterclock

import android.app.Activity
import android.content.Context
import android.graphics.*
import android.graphics.drawable.Drawable
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Switch
import android.widget.TextView

class AlarmView(
    val globals: Globals,
    val alarm: Alarm,
    val alarmStore: AlarmStore,
    context: Context,
    val collapseDrawable: Drawable = context.getDrawable(R.drawable.ic_collapse)!!
) : ViewGroup(context) {
    private val paint: Paint = Paint()
    private var expanded: Boolean = false
    private val timeRect: Rect = Rect()
    private val regularTextHeight: Int
    private val expandoRect: Rect =
        Rect(0, 0, collapseDrawable.minimumWidth, collapseDrawable.minimumHeight)
    private val enabledSwitch = Switch(context)

    init {
        val ts = "12:59 pm"
        paint.typeface = globals.alarmTypeface
        paint.color = globals.primaryTextColor
        paint.textSize = ALARM_TEXT_SIZE
        paint.getTextBounds(ts , 0, ts.length, timeRect)
        paint.typeface = globals.primaryTypeface
        paint.textSize = REGULAR_TEXT_SIZE
        val regularTextRect = Rect()
        paint.getTextBounds(ts , 0, ts.length, regularTextRect)
        regularTextHeight = regularTextRect.height()
        timeRect.offsetTo(ALARM_TEXT_X.toInt(), ALARM_TEXT_Y.toInt() - timeRect.height())
        this.setWillNotDraw(false)
        val m = arrayOf(
            -1F, 0F, 0F, 0F, 255F,
            0F, -1F, 0F, 0F, 255F,
            0F, 0F, -1F, 0F, 255F,
            0F, 0F, 0F, 1F, 0F
        )
        val cm = ColorMatrix(m.toFloatArray())
        val cf = ColorMatrixColorFilter(cm)
        collapseDrawable.colorFilter = cf
        enabledSwitch.isChecked = alarm.enabled
        enabledSwitch.setOnClickListener { _ -> this.toggleEnable() }
        addView(enabledSwitch)
    }

    private fun alarmDays(): String {
        if (alarm.enabled) {
            when {
                alarm.allDaysOn() -> return "Every day"
                alarm.allDaysOff() -> return "Today"
                else -> {
                    val l = mutableListOf<AlarmDay>()
                    for (d in AlarmDay.values()) {
                        if (alarm.days[d.idx]) {
                            l.add(d)
                        }
                    }
                    if (l.size == 1) {
                        return l[0].long
                    }
                    return l.joinToString(separator = ", ") { v -> v.short }
                }
            }
        }
        return "Not scheduled"
    }

    var onTimeClicked: ((AlarmView) -> Unit)? = null
    var onExpandClicked: ((AlarmView) -> Unit)? = null

    companion object {
        val CORNER_RADIUS: Float = 20.dpf
        val COLLAPSED_HEIGHT: Float = 60.dpf
        val EXPANDED_HEIGHT: Float = 200.dpf
        val ALARM_TEXT_SIZE: Float = 20.dpf
        val REGULAR_TEXT_SIZE: Float = 10.dpf
        val ALARM_TEXT_X: Float = 13.dpf
        val ALARM_TEXT_Y: Float = 20.dpf
        val SCHEDULE_TEXT_GAP: Float = 10.dpf
    }

    fun doChangeSize() {
        when (val v = parent) {
            is ViewGroup -> {
                val lp = layoutParams
                v.updateViewLayout(this, lp)
            }
        }
    }

    fun expand() {
        expanded = true
        doChangeSize()
        invalidate()
    }

    fun collapse() {
        expanded = false
        doChangeSize()
        invalidate()
    }

    fun toggleExpand() {
        expanded = !expanded
        doChangeSize()
        invalidate()
    }

    fun setTime(time: AlarmTime) {
        alarm.time = time
        alarmStore.addOrUpdate(alarm.clone())
        invalidate()
    }

    private fun toggleEnable() {
        alarm.toggle()
        alarmStore.addOrUpdate(alarm.clone())
    }

    override fun isClickable(): Boolean {
        return true
    }

    override fun dispatchTouchEvent(event: MotionEvent?): Boolean {
        if (event != null) {
            val x = event.x.toInt()
            val y = event.y.toInt()
            if (timeRect.contains(x, y)) {
                onTimeClicked?.let { it(this) }
            } else if (expandoRect.contains(x, y)) {
                onExpandClicked?.let { it(this) }
            }
        }
        return super.dispatchTouchEvent(event)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val activity = (context as Activity)
        val dm = activity.resources.displayMetrics
        val desiredWidth = dm.widthPixels
        val desiredHeight: Int = if (expanded) {
            EXPANDED_HEIGHT.toInt()
        } else {
            COLLAPSED_HEIGHT.toInt()
        }

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

        expandoRect.offsetTo(
            width - collapseDrawable.minimumWidth - 20.dp,
            timeRect.centerY()
        )
        collapseDrawable.bounds = expandoRect


        //MUST CALL THIS
        setMeasuredDimension(width, height)
    }

    private fun drawExpando(canvas: Canvas) {
        canvas.save()
        canvas.rotate(180F, expandoRect.exactCenterX(), expandoRect.exactCenterY())
        collapseDrawable.draw(canvas)
        canvas.restore()
    }

    private fun drawCollapso(canvas: Canvas) {
        collapseDrawable.draw(canvas)
    }

    private fun drawCollapsed(canvas: Canvas) {
        paint.color = globals.cardBackgroundColor
        paint.style = Paint.Style.FILL_AND_STROKE
        canvas.drawRoundRect(
            0F,
            0F,
            width.toFloat(),
            COLLAPSED_HEIGHT,
            CORNER_RADIUS,
            CORNER_RADIUS,
            paint
        )
        paint.typeface = globals.alarmTypeface
        paint.color = globals.primaryTextColor
        paint.textSize = ALARM_TEXT_SIZE
        val timeString = alarm.time.format(globals.is24HourFormat)
        canvas.drawText(timeString, ALARM_TEXT_X, ALARM_TEXT_Y, paint)
        val scheduleText = alarmDays()
        paint.typeface = globals.primaryTypeface
        paint.color = globals.primaryTextColor
        paint.textSize = REGULAR_TEXT_SIZE
        canvas.drawText(scheduleText, ALARM_TEXT_X, ALARM_TEXT_Y + SCHEDULE_TEXT_GAP + regularTextHeight, paint)
        drawExpando(canvas)
    }

    private fun drawExpanded(canvas: Canvas) {
        paint.color = globals.cardBackgroundColor
        paint.style = Paint.Style.FILL_AND_STROKE
        canvas.drawRoundRect(
            0F,
            0F,
            width.toFloat(),
            EXPANDED_HEIGHT,
            CORNER_RADIUS,
            CORNER_RADIUS,
            paint
        )
        paint.typeface = globals.alarmTypeface
        paint.color = globals.primaryTextColor
        paint.textSize = ALARM_TEXT_SIZE
        val timeString = alarm.time.format(globals.is24HourFormat)
        paint.getTextBounds(timeString, 0, timeString.length, timeRect)
        timeRect.offsetTo(ALARM_TEXT_X.toInt(), ALARM_TEXT_Y.toInt() - timeRect.height())
        canvas.drawText(timeString, ALARM_TEXT_X, ALARM_TEXT_Y, paint)
        drawCollapso(canvas)
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        if (canvas != null) {
            if (expanded) {
                drawExpanded(canvas)
            } else {
                drawCollapsed(canvas)
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
        enabledSwitch.measure(
            MeasureSpec.makeMeasureSpec(this.measuredWidth, MeasureSpec.AT_MOST),
            MeasureSpec.makeMeasureSpec(contentHeight, MeasureSpec.AT_MOST)
        )
        val enabledHeight = enabledSwitch.measuredHeight
        val enabledWidth = enabledSwitch.measuredWidth
        enabledSwitch.layout(expandoRect.right - enabledWidth, expandoRect.bottom + 10.dp, expandoRect.right, expandoRect.bottom + 10.dp + enabledHeight)
    }
}