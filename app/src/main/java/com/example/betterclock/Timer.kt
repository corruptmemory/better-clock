package com.example.betterclock

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
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
    @Serializable
    private class TimerSaved(val id: String, val duration: Long)
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

    fun toJson(): String {
        val ts = TimerSaved(id, duration.toNanos())
        return Json.encodeToString(ts)
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

        fun fromJson(jsonStr: String): Timer {
            val temp = Json.decodeFromString<TimerSaved>(jsonStr)
            return Timer(temp.id, Duration.ofNanos(temp.duration))
        }

        fun new(duration: Duration, id: String = genTimerID()): Timer =
            Timer(
                id = id,
                duration = duration
            )
    }
}
