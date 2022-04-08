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
    private val expandoRect: Rect =
        Rect(0, 0, collapseDrawable.minimumWidth, collapseDrawable.minimumHeight)
    private val enabledSwitch = Switch(context)

    init {
        val ts = "12:59 pm"
        paint.typeface = globals.alarmTypeface
        paint.color = globals.primaryTextColor
        paint.textSize = ALARM_TEXT_SIZE
        paint.getTextBounds(ts , 0, "12:59 pm".length, timeRect)
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
        addView(enabledSwitch)
    }

    var onTimeClicked: ((AlarmView) -> Unit)? = null
    var onExpandClicked: ((AlarmView) -> Unit)? = null

    companion object {
        val CORNER_RADIUS: Float = 20.dpf
        val COLLAPSED_HEIGHT: Float = 60.dpf
        val EXPANDED_HEIGHT: Float = 200.dpf
        val ALARM_TEXT_SIZE: Float = 20.dpf
        val ALARM_TEXT_X: Float = 13.dpf
        val ALARM_TEXT_Y: Float = 20.dpf
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

    fun toggle() {
        expanded = !expanded
        doChangeSize()
        invalidate()
    }

    fun setTime(time: AlarmTime) {
        alarm.time = time
        alarmStore.addOrUpdate(alarm.clone())
        invalidate()
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