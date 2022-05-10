package com.example.betterclock

import java.time.LocalTime

interface DataStore {
    fun alarmStore(): AlarmStore
    fun timerStore(): TimerStore
}

object DataStores {
    private val ims: DataStore by lazy { InMemoryDataStore() }
    private val testIms: DataStore by lazy {
        val ims = InMemoryDataStore()
        val a = ims.alarmStore()
        for (i in 0..1000) {
            a.addOrUpdate(Alarm.empty(LocalTime.now()))
        }
        ims
    }
    fun inMemoryStore(): DataStore = ims
    fun inMemoryTestStore(): DataStore = testIms
}

private class InMemoryDataStore() : DataStore {
    private val alarmStore: AlarmStore = AlarmStores.inMemoryStore()
    private val timerStore: TimerStore = TimerStores.inMemoryStore()

    override fun alarmStore(): AlarmStore = alarmStore
    override fun timerStore(): TimerStore = timerStore
}