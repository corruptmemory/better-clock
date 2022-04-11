package com.example.betterclock

import android.app.Activity
import android.content.Context
import android.graphics.*
import android.graphics.drawable.Drawable
import android.view.MotionEvent
import android.view.ViewGroup
import android.widget.Switch
import kotlin.math.roundToInt

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
        Rect(0, 0, 2*collapseDrawable.minimumWidth, 2*collapseDrawable.minimumHeight)
    private val enabledSwitch = Switch(context)
    private var enabledDaysRect: Rect = Rect(
        ENABLED_DAYS_MARGIN.toInt(),
        0,
        0,
        0
    )
    private val enabledDaysHeight: Int
    private val enabledDaysTextHeight: Int
    private val enabledDaysCircleRadius: Float
    private val enabledDaysRects: Array<Rect> = Array(7) { _ -> Rect() }


    init {
        val ts = "12:59 pm"
        paint.typeface = globals.alarmTypeface
        paint.color = globals.primaryTextColor
        paint.textSize = ALARM_TEXT_SIZE
        paint.getTextBounds(ts, 0, ts.length, timeRect)
        paint.typeface = globals.primaryTypeface
        paint.textSize = REGULAR_TEXT_SIZE
        val regularTextRect = Rect()
        paint.getTextBounds(ts, 0, ts.length, regularTextRect)
        regularTextHeight = regularTextRect.height()
        val edr = Rect()
        paint.textSize = ENABLED_DAYS_TEXT_SIZE
        paint.getTextBounds(ts, 0, ts.length, edr)
        enabledDaysTextHeight = edr.height()
        enabledDaysCircleRadius = enabledDaysTextHeight.toFloat()
        enabledDaysRect.bottom = enabledDaysCircleRadius.toInt()*2
        enabledDaysHeight = enabledDaysRect.height()
        timeRect.offsetTo(ALARM_TEXT_X.toInt(), ALARM_TEXT_Y.toInt())
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
        val COLLAPSED_HEIGHT: Float = 80.dpf
        val EXPANDED_HEIGHT: Float = 200.dpf
        val ALARM_TEXT_SIZE: Float = 30.dpf
        val ENABLED_DAYS_TEXT_SIZE: Float = 15.dpf
        val REGULAR_TEXT_SIZE: Float = 15.dpf
        val ALARM_TEXT_X: Float = 20.dpf
        val ALARM_TEXT_Y: Float = 10.dpf
        val SCHEDULE_TEXT_GAP: Float = 10.dpf
        val ENABLED_DAYS_GAP: Float = 18.dpf
        val ENABLED_DAYS_MARGIN: Float = ALARM_TEXT_X
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
            } else if (expanded) {
                for ((i, v) in AlarmDay.values().withIndex()) {
                    if (enabledDaysRects[i].contains(x,y)) {
                        alarm.toggleDay(v)
                        invalidate()
                        return super.dispatchTouchEvent(event)
                    }
                }
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
            width - 20.dp - (collapseDrawable.minimumWidth*1.5).toInt(),
            ALARM_TEXT_Y.toInt()
        )
        collapseDrawable.bounds = expandoRect

        enabledDaysRect.right = width - ENABLED_DAYS_MARGIN.toInt()

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

    private fun drawBackground(canvas: Canvas, height: Float) {
        paint.color = globals.cardBackgroundColor
        paint.style = Paint.Style.FILL_AND_STROKE
        canvas.drawRoundRect(
            0F,
            0F,
            width.toFloat(),
            height,
            CORNER_RADIUS,
            CORNER_RADIUS,
            paint
        )
    }

    private fun drawTimeText(canvas: Canvas, x: Float, y: Float) {
        paint.typeface = globals.alarmTypeface
        paint.color = globals.primaryTextColor
        paint.textSize = ALARM_TEXT_SIZE
        paint.style = Paint.Style.FILL_AND_STROKE
        val timeString = alarm.time.format(globals.is24HourFormat)
        canvas.drawText(timeString, x, y + timeRect.height().toFloat(), paint)
    }

    private fun drawScheduleText(canvas: Canvas, x: Float, y: Float) {
        val scheduleText = alarmDays()
        paint.typeface = globals.primaryTypeface
        paint.color = globals.primaryTextColor
        paint.textSize = REGULAR_TEXT_SIZE
        paint.style = Paint.Style.FILL_AND_STROKE
        canvas.drawText(scheduleText, x, y + regularTextHeight, paint)
    }

    private fun drawEnabledDays(canvas: Canvas, y: Float) {
        paint.style = Paint.Style.STROKE
        paint.color = globals.primaryTextColor
        paint.typeface = globals.primaryTypeface
        paint.textSize = ENABLED_DAYS_TEXT_SIZE
        enabledDaysRect.top = y.toInt()
        enabledDaysRect.bottom = y.toInt() + enabledDaysHeight
        val cy = enabledDaysRect.centerY()
        val targetWidth = enabledDaysRect.width().toFloat() - 2*enabledDaysCircleRadius
        val sx = enabledDaysRect.left.toFloat() + enabledDaysCircleRadius
        val gap = targetWidth / 6F
        for ((i, v) in AlarmDay.values().withIndex()) {
            paint.color = globals.primaryTextColor
            if (alarm.days[i]) {
                paint.color = globals.primaryTextColor
                paint.style = Paint.Style.FILL_AND_STROKE
            } else {
                paint.style = Paint.Style.STROKE
            }
            val tx = sx + (i * gap)
            canvas.drawCircle(tx, cy.toFloat(), enabledDaysCircleRadius, paint)
            val lr = Rect()
            paint.getTextBounds(v.single,0, 1, lr)
            paint.style = Paint.Style.FILL_AND_STROKE
            if (alarm.days[i]) {
                paint.color = Color.DKGRAY
            } else {
                paint.color = globals.primaryTextColor
            }
            canvas.drawText(v.single, 0, 1, tx - lr.exactCenterX(), cy + (lr.height().toFloat()/2F), paint)
            enabledDaysRects[i] = Rect((tx - enabledDaysCircleRadius).toInt(), (cy - enabledDaysCircleRadius).toInt(), (tx + enabledDaysCircleRadius).toInt(), (cy + enabledDaysCircleRadius).toInt())
        }
    }

    private fun drawCollapsed(canvas: Canvas) {
        drawBackground(canvas, COLLAPSED_HEIGHT)
        drawExpando(canvas)
        drawTimeText(canvas, ALARM_TEXT_X, ALARM_TEXT_Y)
        drawScheduleText(canvas, ALARM_TEXT_X, timeRect.bottom + SCHEDULE_TEXT_GAP)
    }

    private fun drawExpanded(canvas: Canvas) {
        drawBackground(canvas, EXPANDED_HEIGHT)
        drawCollapso(canvas)
        drawTimeText(canvas, ALARM_TEXT_X, ALARM_TEXT_Y)
        drawScheduleText(canvas, ALARM_TEXT_X, timeRect.bottom + SCHEDULE_TEXT_GAP)
        drawEnabledDays(
            canvas,
            timeRect.bottom + SCHEDULE_TEXT_GAP + regularTextHeight + ENABLED_DAYS_GAP
        )
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
        enabledSwitch.layout(
            expandoRect.right - enabledWidth,
            expandoRect.bottom + 10.dp,
            expandoRect.right,
            expandoRect.bottom + 10.dp + enabledHeight
        )
    }
}