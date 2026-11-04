package com.naikko.habitly.data

import android.content.Context
import com.naikko.habitly.data.local.AppDatabase
import com.naikko.habitly.data.repository.AlarmRepository
import com.naikko.habitly.data.repository.HabitRepository
import com.naikko.habitly.data.repository.NoteRepository
import com.naikko.habitly.reminders.AlarmScheduler
import com.naikko.habitly.reminders.ReminderScheduler

/** Creates and holds the app-wide singletons. Everything is lazy so startup stays fast. */
class AppContainer(context: Context) {
    private val appContext = context.applicationContext

    private val database: AppDatabase by lazy { AppDatabase.build(appContext) }

    val reminderScheduler: ReminderScheduler by lazy { ReminderScheduler(appContext) }
    val alarmScheduler: AlarmScheduler by lazy { AlarmScheduler(appContext) }

    val habitRepository: HabitRepository by lazy { HabitRepository(database.habitDao(), reminderScheduler) }
    val noteRepository: NoteRepository by lazy { NoteRepository(database.noteDao()) }
    val alarmRepository: AlarmRepository by lazy { AlarmRepository(database.alarmDao(), alarmScheduler) }
}
