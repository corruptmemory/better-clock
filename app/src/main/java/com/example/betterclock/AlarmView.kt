package com.example.betterclock

import android.app.AlertDialog
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import java.time.LocalTime
import java.util.*

enum class AlarmDay(val idx: Int) {
    SUNDAY(0),
    MONDAY(1),
    TUESDAY(2),
    WEDNESDAY(3),
    THURSDAY(4),
    FRIDAY(5),
    SATURDAY(6),
}

class Alarm(
    val id: String,
    var time: LocalTime,
    var label: String?,
    var enabled: Boolean,
    val days: Array<Boolean>
) {

    fun clone(): Alarm {
        return Alarm(id, time, label, enabled, days.clone())
    }

    fun enableDay(day: AlarmDay): Unit {
        days[day.idx] = true
    }

    fun disableDay(day: AlarmDay): Unit {
        days[day.idx] = false
    }

    fun toggleDay(day: AlarmDay): Unit {
        days[day.idx] = !days[day.idx]
    }

    fun enable(): Unit {
        enabled = true
    }

    fun disable(): Unit {
        enabled = false
    }

    fun toggle(): Unit {
        enabled = !enabled
    }

    companion object {
        fun genAlarmID(): String {
            val uuid = UUID.randomUUID()
            val now = System.nanoTime()
            return "alarm-$uuid-$now"
        }

        fun fromParts(
            id: String,
            time: LocalTime,
            label: String?,
            enabled: Boolean,
            days: Array<Boolean>
        ): Alarm =
            Alarm(id, time, label, enabled, days.clone())

        fun empty(time: LocalTime, id: String = genAlarmID()): Alarm =
            Alarm(
                id = id,
                time = time,
                label = null,
                enabled = true,
                days = Array(AlarmDay.values().size) { _ -> true })
    }
}

typealias AlarmUpdateSink = (Alarm) -> Unit

class AlarmView : View {
    private val alarm: Alarm
    private val updateSink: AlarmUpdateSink
    private val globals: Globals
    private val paint: Paint = Paint()
    private var expanded: Boolean = false
    private val timeRect: Rect = Rect()

    companion object {
        val CORNER_RADIUS: Float = 20.dpf
        val COLLAPSED_HEIGHT: Float = 60.dpf
        val EXPANDED_HEIGHT: Float = 200.dpf
        val ALARM_TEXT_SIZE: Float = 20.dpf
        val ALARM_TEXT_X: Float = 13.dpf
        val ALARM_TEXT_Y: Float = 20.dpf
    }

    fun expand() {
        expanded = true
        invalidate()
    }

    fun collapse() {
        expanded = false
        invalidate()
    }

    fun toggle() {
        expanded = !expanded
        invalidate()
    }

    constructor(
        globals: Globals,
        alarm: Alarm,
        updateSink: AlarmUpdateSink,
        context: Context?,
        attrs: AttributeSet?,
        defStyle: Int
    ) : super(
        context,
        attrs,
        defStyle
    ) {
        this.alarm = alarm
        this.updateSink = updateSink
        this.globals = globals
    }

    constructor(
        globals: Globals,
        alarm: Alarm,
        updateSink: AlarmUpdateSink,
        context: Context?,
        attrs: AttributeSet?
    ) : super(context, attrs) {
        this.alarm = alarm
        this.updateSink = updateSink
        this.globals = globals
    }

    constructor(
        globals: Globals,
        alarm: Alarm,
        updateSink: AlarmUpdateSink,
        context: Context?
    ) : super(context) {
        this.alarm = alarm
        this.updateSink = updateSink
        this.globals = globals
    }

    override fun isClickable(): Boolean {
        return true
    }

    override fun dispatchTouchEvent(event: MotionEvent?): Boolean {
        if (event != null) {
            if (timeRect.contains(event.x.toInt(), event.y.toInt())) {
                val d = AlertDialog.Builder(context)
                d.setTitle("Clicked!")
                d.show()
            }
        }
        return super.dispatchTouchEvent(event)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val desiredWidth = 1080
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

        //MUST CALL THIS
        setMeasuredDimension(width, height)
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
        val timeString = globals.formatTime(alarm.time)
        paint.getTextBounds(timeString, 0, timeString.length, timeRect)
        timeRect.offsetTo(ALARM_TEXT_X.toInt(), ALARM_TEXT_Y.toInt() - timeRect.height())
        canvas.drawText(timeString, ALARM_TEXT_X, ALARM_TEXT_Y, paint)
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
        val timeString = globals.formatTime(alarm.time)
        paint.getTextBounds(timeString, 0, timeString.length, timeRect)
        timeRect.offsetTo(ALARM_TEXT_X.toInt(), ALARM_TEXT_Y.toInt() - timeRect.height())
        canvas.drawText(timeString, ALARM_TEXT_X, ALARM_TEXT_Y, paint)
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
}