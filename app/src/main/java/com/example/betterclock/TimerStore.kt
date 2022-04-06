package com.example.betterclock

interface TimerStore {
    fun addOrUpdate(alarm: Timer): Unit
    fun delete(id: String): Unit
    fun find(id: String): Timer?
    fun list(): Array<Timer>
    fun count(): Int
}

object TimerStores {
    fun inMemoryStore(): TimerStore = InMemoryTimerStore()
}

private class InMemoryTimerStore() : TimerStore {
    private var timers: MutableList<Timer> = mutableListOf()

    override fun addOrUpdate(alarm: Timer) {
        val idx = timers.indexOfFirst { i -> i.id == alarm.id }
        if (idx == -1) {
            timers.add(alarm)
        } else {
            timers[idx] = alarm
        }
    }

    override fun delete(id: String) {
        timers.removeIf { i -> i.id == id }
    }

    override fun find(id: String): Timer? {
        return timers.find { i -> i.id == id }
    }

    override fun list(): Array<Timer> {
        return timers.toTypedArray()
    }

    override fun count(): Int {
        return timers.size
    }
}