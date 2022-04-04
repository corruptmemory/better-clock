package com.example.betterclock

import android.content.res.Resources
import android.util.DisplayMetrics
import android.util.TypedValue


val displayMetrics: DisplayMetrics = Resources.getSystem().displayMetrics

val Int.dp: Int
    get() = this.dpf.toInt()

val Int.dpf: Float
    get() = this.toFloat().dpf

val Float.dpf: Float
    get() = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,
        this,
        displayMetrics
    )


val Int.px: Int
    get() = this.pxf.toInt()

val Int.pxf: Float
    get() = this.toFloat().pxf

val Float.pxf: Float
    get() = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_PX,
        this,
        displayMetrics
    )

val Int.sp: Int
    get() = this.spf.toInt()

val Int.spf: Float
    get() = this.toFloat().spf

val Float.spf: Float
    get() = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_SP,
        this,
        displayMetrics
    )
