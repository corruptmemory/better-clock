package com.example.betterclock

import java.time.Duration
import java.util.*

sealed class TimerState
object TimerStates {
    class Stopped(dur: Long) : TimerState() {
        private val duration: Long = dur
        fun newAlarmTime(): Long {
            return System.nanoTime() + duration
        }
    }

    class Running(target: Long) : TimerState() {
        private val alarmTime: Long = target
        fun remaining(): Double {
            return remainingNanos().toDouble() / 1000000000F
        }
        fun remainingNanos(): Long {
            return alarmTime - System.nanoTime()
        }
    }

    class Paused(rem: Long) : TimerState() {
        private val remaining: Long = rem
        fun remaining(): Double {
            return remaining.toDouble() / 1000000000F
        }

        fun newAlarmTime(): Long {
            return System.nanoTime() + remaining
        }
    }
}

class Timer(
    val id: String,
    val duration: Duration
) {
    private var internalState: TimerState = TimerStates.Stopped(duration.toNanos())

    fun stop() {
        internalState = TimerStates.Stopped(duration.toNanos())
    }

    fun pause() {
        when (internalState) {
            is TimerStates.Running -> internalState = TimerStates.Paused((internalState as TimerStates.Running).remainingNanos())
            else -> {}
        }
    }

    fun start() {
        when (internalState) {
            is TimerStates.Stopped -> internalState = TimerStates.Running((internalState as TimerStates.Stopped).newAlarmTime())
            is TimerStates.Paused -> internalState = TimerStates.Running((internalState as TimerStates.Paused).newAlarmTime())
            else -> {}
        }
    }

    fun state(): TimerState = internalState

    companion object {
        fun genTimerID(): String {
            val uuid = UUID.randomUUID()
            val now = System.nanoTime()
            return "timer-$uuid-$now"
        }

        fun new(duration: Duration, id: String = genTimerID()): Timer =
            Timer(
                id = id,
                duration = duration
            )
    }
}
