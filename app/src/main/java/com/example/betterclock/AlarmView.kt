package com.example.betterclock

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.media.RingtoneManager
import android.net.Uri
import android.util.Log
import android.view.*
import android.widget.Switch
import androidx.activity.result.contract.ActivityResultContract


@SuppressLint("ViewConstructor")
class AlarmView(
    val theme: AlarmTheme,
    val is24HourFormat: Boolean,
    val alarm: Alarm,
    val alarmStore: AlarmStore,
    context: Context,
    val collapseDrawable: Drawable,
) : ViewGroup(context) {

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
        val ALARM_SELECTION_GAP: Float = 18.dpf
        val TEXT_ALIGNMENT_OFFSET: Float = 30.dpf
        val ENABLED_DAYS_MARGIN: Float = ALARM_TEXT_X
    }

    private val paint: Paint = Paint()
    private var expanded: Boolean = false
    private val timeRect: Rect = Rect()
    private val alarmSoundRect: Rect = Rect()
    private val vibrateRect: Rect = Rect()
    private val deleteRect: Rect = Rect()
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
    private var currentY: Int = 0


    init {
        val ts = "12:59 pm"
        paint.typeface = theme.alarmTypeface
        paint.color = theme.colors.primaryTextColor.enabled
        paint.textSize = ALARM_TEXT_SIZE
        paint.getTextBounds(ts, 0, ts.length, timeRect)
        paint.typeface = theme.primaryTypeface
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
        enabledSwitch.isChecked = alarm.enabled
        enabledSwitch.setOnClickListener { _ -> this.toggleEnable() }
        alarmSoundRect.right = width
        vibrateRect.right = width
        deleteRect.right = width
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
                if (alarmSoundRect.contains(x,y)) {
                    val ringtonePicker = object: ActivityResultContract<Int, Uri?>() {
                        override fun createIntent(context: Context, ringtoneType: Int) =
                            Intent(RingtoneManager.ACTION_RINGTONE_PICKER).apply {
                                putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, ringtoneType)
                            }

                        override fun parseResult(resultCode: Int, result: Intent?) : Uri? {
                            if (resultCode != Activity.RESULT_OK) {
                                return null
                            }
                            val result: Uri? = result?.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI)
                            Log.i("BC","result: $result")
                            return result
                        }
                    }

                    Log.i("BC", "Supposed to picking a ringtone")

                    val rt = ringtonePicker.getSynchronousResult(context, RingtoneManager.TYPE_NOTIFICATION)
                    Log.i("BC", "rt: $rt")

//                    val intent = Intent(RingtoneManager.ACTION_RINGTONE_PICKER)
//                    intent.putExtra(
//                        RingtoneManager.EXTRA_RINGTONE_TYPE,
//                        RingtoneManager.TYPE_NOTIFICATION
//                    )
//                    intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, "Select Tone")
//                    intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, null as Uri?)
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

        width = if (widthMode == MeasureSpec.EXACTLY) {
            widthSize
        } else if (widthMode == MeasureSpec.AT_MOST) {
            Math.min(desiredWidth, widthSize)
        } else {
            desiredWidth
        }

        height = if (heightMode == MeasureSpec.EXACTLY) {
            heightSize
        } else if (heightMode == MeasureSpec.AT_MOST) {
            Math.min(desiredHeight, heightSize)
        } else {
            desiredHeight
        }

        expandoRect.offsetTo(
            width - 20.dp - (collapseDrawable.minimumWidth*1.5).toInt(),
            ALARM_TEXT_Y.toInt()
        )
        collapseDrawable.bounds = expandoRect

        enabledDaysRect.right = width - ENABLED_DAYS_MARGIN.toInt()

        alarmSoundRect.right = width
        vibrateRect.right = width
        deleteRect.right = width

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
        paint.color = theme.colors.cardBackgroundColor
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

    private fun drawTimeText(canvas: Canvas) {
        paint.typeface = theme.alarmTypeface
        paint.color = theme.colors.primaryTextColor.enabled
        paint.textSize = ALARM_TEXT_SIZE
        paint.style = Paint.Style.FILL_AND_STROKE
        val timeString = alarm.time.format(is24HourFormat)
        canvas.drawText(timeString, ALARM_TEXT_X, ALARM_TEXT_Y + timeRect.height().toFloat(), paint)
        currentY = timeRect.bottom
    }

    private fun drawScheduleText(canvas: Canvas) {
        currentY += SCHEDULE_TEXT_GAP.toInt()
        val scheduleText = alarmDays()
        paint.typeface = theme.primaryTypeface
        paint.color = theme.colors.primaryTextColor.enabled
        paint.textSize = REGULAR_TEXT_SIZE
        paint.style = Paint.Style.FILL_AND_STROKE
        currentY += regularTextHeight
        canvas.drawText(scheduleText, ALARM_TEXT_X, currentY.toFloat(), paint)
    }

    private fun drawAlarmSelection(canvas: Canvas) {
        currentY += ALARM_SELECTION_GAP.toInt()
        alarmSoundRect.top = currentY
        paint.typeface = theme.primaryTypeface
        paint.color = theme.colors.primaryTextColor.enabled
        paint.textSize = REGULAR_TEXT_SIZE
        paint.style = Paint.Style.FILL_AND_STROKE
        currentY += regularTextHeight
        canvas.drawText("A", ALARM_TEXT_X, currentY.toFloat(), paint)
        if (alarm.alarmSelection == null) {
            paint.color = theme.colors.primaryTextColor.disabled
        }
        canvas.drawText(alarm.alarmSelection ?: "None", ALARM_TEXT_X + TEXT_ALIGNMENT_OFFSET, currentY.toFloat(), paint)
        alarmSoundRect.bottom = currentY
    }

    private fun drawEnabledDays(canvas: Canvas) {
        currentY += ENABLED_DAYS_GAP.toInt()
        paint.style = Paint.Style.STROKE
        paint.color = theme.colors.primaryTextColor.enabled
        paint.typeface = theme.primaryTypeface
        paint.textSize = ENABLED_DAYS_TEXT_SIZE
        enabledDaysRect.top = currentY
        enabledDaysRect.bottom = currentY + enabledDaysHeight
        val cy = enabledDaysRect.centerY()
        val targetWidth = enabledDaysRect.width().toFloat() - 2*enabledDaysCircleRadius
        val sx = enabledDaysRect.left.toFloat() + enabledDaysCircleRadius
        val gap = targetWidth / 6F
        for ((i, v) in AlarmDay.values().withIndex()) {
            paint.color = theme.colors.primaryTextColor.enabled
            if (alarm.days[i]) {
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
                paint.color = theme.colors.backgroundColor
            } else {
                paint.color = theme.colors.primaryTextColor.enabled
            }
            canvas.drawText(v.single, 0, 1, tx - lr.exactCenterX(), cy + (lr.height().toFloat()/2F), paint)
            enabledDaysRects[i] = Rect((tx - enabledDaysCircleRadius).toInt(), (cy - enabledDaysCircleRadius).toInt(), (tx + enabledDaysCircleRadius).toInt(), (cy + enabledDaysCircleRadius).toInt())
        }
        currentY += enabledDaysHeight
    }

    private fun drawCollapsed(canvas: Canvas) {
        drawBackground(canvas, COLLAPSED_HEIGHT)
        drawExpando(canvas)
        drawTimeText(canvas)
        drawScheduleText(canvas)
    }

    private fun drawExpanded(canvas: Canvas) {
        drawBackground(canvas, EXPANDED_HEIGHT)
        drawCollapso(canvas)
        drawTimeText(canvas)
        drawScheduleText(canvas)
        drawEnabledDays(canvas)
        drawAlarmSelection(canvas)
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