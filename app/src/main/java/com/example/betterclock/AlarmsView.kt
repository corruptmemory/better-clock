package com.example.betterclock

import android.app.TimePickerDialog
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.ColorDrawable
import android.util.AttributeSet
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import java.time.LocalTime


class AlarmEntry(
    var alarm: LocalTime,
    var expanded: Boolean = false
) {
    lateinit var timeView: TextView
    lateinit var expando: BorderedTextView
    lateinit var card: CardView

    fun expand() {
        expanded = true
        expando.text = "^"
        card.minimumHeight = 200.dp
        card.invalidate()
    }

    fun collapse() {
        expanded = false
        expando.text = "v"
        card.minimumHeight = 60.dp
        card.invalidate()
    }
}

class Border {
    var style: Int = 0
    var orientation: Int = 0
    var width: Float = 1.dpf
    var color: Int = Color.BLACK

    constructor(s: Int, o: Int = 0, w: Float = 1.dpf, c: Int = Color.BLACK) {
        style = s
        orientation = o
        width = w
        color = c
    }
}

class BorderedTextView : TextView {
    private val paint: Paint = Paint()
    var borders: Array<Border>? = null

    constructor(context: Context?, attrs: AttributeSet?, defStyle: Int) : super(
        context,
        attrs,
        defStyle
    ) {
        init()
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        init()
    }

    constructor(context: Context?) : super(context) {
        init()
    }

    private fun init() {
        paint.setStyle(Paint.Style.STROKE)
        paint.setColor(Color.BLACK)
        paint.setStrokeWidth(4.dpf)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (borders == null) return
        for (border in borders!!) {
            paint.setColor(border.color)
            paint.setStrokeWidth(border.width)
            if (border.style == BORDER_TOP) {
                canvas.drawLine(0.dpf, 0.dpf, width.toFloat(), 0.dpf, paint)
            } else if (border.style == BORDER_RIGHT) {
                canvas.drawLine(width.toFloat(), 0.dpf, width.toFloat(), height.toFloat(), paint)
            } else if (border.style == BORDER_BOTTOM) {
                canvas.drawLine(0.dpf, height.toFloat(), width.toFloat(), height.toFloat(), paint)
            } else if (border.style == BORDER_LEFT) {
                canvas.drawLine(0.dpf, 0.dpf, 0.dpf, height.toFloat(), paint)
            }
        }
    }

    companion object {
        const val BORDER_TOP = 0x00000001
        const val BORDER_RIGHT = 0x00000002
        const val BORDER_BOTTOM = 0x00000004
        const val BORDER_LEFT = 0x00000008

        fun allBorders(width: Float, color: Int): Array<Border> {
            return arrayOf(
                Border(BORDER_TOP, w = width, c = color),
                Border(BORDER_RIGHT, w = width, c = color),
                Border(BORDER_LEFT, w = width, c = color),
                Border(BORDER_BOTTOM, w = width, c = color)
            )
        }
    }
}


class AlarmsView(alarms: Array<LocalTime>, context: Context?) : ScrollView(context) {

    fun buildViews(entries: Array<AlarmEntry>) {
        for (a in entries) {
            val card = CardView(context)
            a.card = card
            fun expandCurrent() {
                val ex = a.expanded
                for (i in entries) {
                    i.collapse()
                }
                if (ex) {
                    a.collapse()
                } else {
                    a.expand()
                }
            }
            card.setPadding(5.dp, 5.dp, 5.dp, 5.dp)
            card.setContentPadding(25.dp, 5.dp, 25.dp, 5.dp)
            card.radius = 20.dpf
            card.setCardBackgroundColor(context.getColor(androidx.appcompat.R.color.background_material_light))
            val cl = ConstraintLayout(context)
            val l = a.alarm
            val t = "${l.hour}:${l.minute}"
            val tv = TextView(context)
            tv.id = 1
            tv.textSize = 10.dpf
            tv.text = t
            tv.setTextColor(context.getColor(androidx.appcompat.R.color.abc_primary_text_material_light))
            tv.setOnClickListener { v ->
                val dialog = TimePickerDialog(
                    context,
                    { _, h: Int, m: Int ->
                        val nv = LocalTime.of(h, m)
                        a.alarm = nv
                        val ntv = "${h}:${m}"
                        a.timeView.text = ntv
                    }, l.hour, l.minute, false
                )
                dialog.show()
                expandCurrent()
            }
            a.timeView = tv
            cl.addView(tv)
            val expando = BorderedTextView(context)
            expando.id = 2
            expando.borders = BorderedTextView.allBorders(1.dpf, Color.WHITE)
            expando.textSize = 10.dpf
            expando.setTextColor(context.getColor(androidx.appcompat.R.color.abc_primary_text_material_light))
            expando.setOnClickListener { v ->
                expandCurrent()
            }
            a.expando = expando
            a.collapse()
            val cs = ConstraintSet()
            cs.connect(1, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP)
            cs.connect(1, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START)
            cs.connect(2, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP)
            cs.connect(2, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END)
            cs.connect(2, ConstraintSet.LEFT, 1, ConstraintSet.RIGHT)
            cs.connect(1, ConstraintSet.RIGHT, 2, ConstraintSet.LEFT)
            cs.constrainWidth(1, WRAP_CONTENT)
            cs.constrainWidth(2, WRAP_CONTENT)
            cl.setConstraintSet(cs)
            cl.addView(expando)
            card.addView(cl)
        }
    }

    init {
        if (context != null) {
            val entries = Array(alarms.size, { i -> AlarmEntry(alarms[i]) })
            buildViews(entries)
            setBackgroundColor(context.getColor(androidx.appcompat.R.color.abc_background_cache_hint_selector_material_light))
            val ll = LinearLayout(context)
            addView(ll)
            ll.orientation = LinearLayout.VERTICAL
            for (e in entries) {
                val lp = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
                lp.setMargins(0.dp, 3.dp, 0.dp, 4.dp)
                e.card.layoutParams = lp
                ll.addView(e.card)
            }
        }
    }
}
