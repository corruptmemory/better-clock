package com.example.betterclock

interface AlarmStore {
    fun addOrUpdate(alarm: Alarm): Unit
    fun delete(id: String): Unit
    fun find(id: String): Alarm?
    fun list(): Array<Alarm>
    fun count(): Int
}

object AlarmStores {
    fun inMemoryStore(): AlarmStore = InMemoryAlarmStore()
}

private class InMemoryAlarmStore() : AlarmStore {
    private var alarms: MutableList<Alarm> = mutableListOf()

    override fun addOrUpdate(alarm: Alarm) {
        val idx = alarms.indexOfFirst { i -> i.id == alarm.id }
        if (idx == -1) {
            alarms.add(alarm)
        } else {
            alarms[idx] = alarm
        }
    }

    override fun delete(id: String) {
        alarms.removeIf { i -> i.id == id }
    }

    override fun find(id: String): Alarm? {
        return alarms.find { i -> i.id == id }
    }

    override fun list(): Array<Alarm> {
        return alarms.toTypedArray()
    }

    override fun count(): Int {
        return alarms.size
    }
}