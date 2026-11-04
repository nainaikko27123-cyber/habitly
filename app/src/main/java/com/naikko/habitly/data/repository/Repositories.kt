package com.naikko.habitly.data.repository

import com.naikko.habitly.data.local.Alarm
import com.naikko.habitly.data.local.AlarmDao
import com.naikko.habitly.data.local.Habit
import com.naikko.habitly.data.local.HabitCheck
import com.naikko.habitly.data.local.HabitDao
import com.naikko.habitly.data.local.Note
import com.naikko.habitly.data.local.NoteDao
import com.naikko.habitly.reminders.AlarmScheduler
import com.naikko.habitly.reminders.ReminderScheduler
import kotlinx.coroutines.flow.Flow

/** Habits and their daily check-ins. Keeps reminder alarms in sync with what's saved. */
class HabitRepository(
    private val dao: HabitDao,
    private val reminders: ReminderScheduler,
) {
    val habits: Flow<List<Habit>> = dao.observeHabits()
    val checks: Flow<List<HabitCheck>> = dao.observeChecks()

    suspend fun get(id: Long): Habit? = dao.get(id)

    suspend fun save(habit: Habit): Long {
        val id = if (habit.id == 0L) dao.insert(habit) else habit.id.also { dao.update(habit) }
        reminders.schedule(habit.copy(id = id))
        return id
    }

    suspend fun delete(habit: Habit) {
        reminders.cancel(habit.id)
        dao.delete(habit) // check-ins are removed by the foreign-key cascade
    }

    suspend fun setDone(habitId: Long, epochDay: Long, done: Boolean) {
        if (done) dao.insertCheck(HabitCheck(habitId, epochDay)) else dao.deleteCheck(habitId, epochDay)
    }

    suspend fun isDone(habitId: Long, epochDay: Long): Boolean = dao.countCheck(habitId, epochDay) > 0

    suspend fun rescheduleAll() = dao.getAll().forEach(reminders::schedule)
}

class NoteRepository(private val dao: NoteDao) {
    val notes: Flow<List<Note>> = dao.observeAll()

    suspend fun save(note: Note) {
        if (note.id == 0L) dao.insert(note) else dao.update(note)
    }

    suspend fun delete(note: Note) = dao.delete(note)

    suspend fun deleteCompleted() = dao.deleteCompleted()
}

/** Alarm clock entries. Every write re-schedules (or cancels) the system alarm. */
class AlarmRepository(
    private val dao: AlarmDao,
    private val scheduler: AlarmScheduler,
) {
    val alarms: Flow<List<Alarm>> = dao.observeAll()

    val canScheduleExact: Boolean get() = scheduler.canScheduleExact

    suspend fun get(id: Long): Alarm? = dao.get(id)

    suspend fun save(alarm: Alarm): Long {
        val id = if (alarm.id == 0L) dao.insert(alarm) else alarm.id.also { dao.update(alarm) }
        scheduler.schedule(alarm.copy(id = id))
        return id
    }

    suspend fun setEnabled(alarm: Alarm, enabled: Boolean) {
        save(alarm.copy(enabled = enabled))
    }

    suspend fun delete(alarm: Alarm) {
        scheduler.cancel(alarm.id)
        dao.delete(alarm)
    }

    /** Called when an alarm rings: one-off alarms switch off, repeating ones book the next day. */
    suspend fun afterFired(alarm: Alarm) {
        if (alarm.days == 0) dao.update(alarm.copy(enabled = false)) else scheduler.schedule(alarm)
    }

    suspend fun rescheduleAll() = dao.getAll().forEach(scheduler::schedule)
}
