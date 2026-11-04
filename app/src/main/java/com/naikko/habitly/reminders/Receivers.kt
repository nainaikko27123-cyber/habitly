package com.naikko.habitly.reminders

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.naikko.habitly.container
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.time.LocalDate

/** Runs [block] off the main thread while keeping the receiver alive until it finishes. */
private fun BroadcastReceiver.runAsync(block: suspend () -> Unit) {
    val pending = goAsync()
    CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
        try {
            block()
        } finally {
            pending.finish()
        }
    }
}

/** Habit reminder fired, or "Mark done" tapped on the reminder notification. */
class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val habitId = intent.getLongExtra(EXTRA_HABIT_ID, 0L)
        if (habitId == 0L) return
        val container = context.container
        runAsync {
            val repository = container.habitRepository
            val habit = repository.get(habitId) ?: return@runAsync
            val today = LocalDate.now().toEpochDay()
            when (intent.action) {
                ACTION_REMIND -> {
                    // Skip the nudge if it's already done today, then book tomorrow's reminder.
                    if (!repository.isDone(habitId, today)) Notifications.showHabitReminder(context, habit)
                    container.reminderScheduler.schedule(habit)
                }
                ACTION_MARK_DONE -> {
                    repository.setDone(habitId, today, done = true)
                    Notifications.cancelHabitReminder(context, habitId)
                }
            }
        }
    }

    companion object {
        const val ACTION_REMIND = "com.naikko.habitly.action.REMIND"
        const val ACTION_MARK_DONE = "com.naikko.habitly.action.MARK_DONE"
        const val EXTRA_HABIT_ID = "habit_id"
    }
}

/** Alarm clock fired, or Snooze / Dismiss tapped on the ringing notification. */
class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val alarmId = intent.getLongExtra(EXTRA_ALARM_ID, 0L)
        if (alarmId == 0L) return
        val container = context.container
        runAsync {
            when (intent.action) {
                ACTION_FIRE -> {
                    val alarm = container.alarmRepository.get(alarmId) ?: return@runAsync
                    Notifications.showAlarm(context, alarm)
                    if (!intent.getBooleanExtra(EXTRA_SNOOZE, false)) container.alarmRepository.afterFired(alarm)
                }
                ACTION_SNOOZE -> {
                    Notifications.cancelAlarm(context, alarmId)
                    container.alarmScheduler.snooze(alarmId)
                }
                ACTION_DISMISS -> Notifications.cancelAlarm(context, alarmId)
            }
        }
    }

    companion object {
        const val ACTION_FIRE = "com.naikko.habitly.action.ALARM_FIRE"
        const val ACTION_SNOOZE = "com.naikko.habitly.action.ALARM_SNOOZE"
        const val ACTION_DISMISS = "com.naikko.habitly.action.ALARM_DISMISS"
        const val EXTRA_ALARM_ID = "alarm_id"
        const val EXTRA_SNOOZE = "snooze"
    }
}

/** AlarmManager forgets everything on reboot and clock changes, so book it all again. */
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val container = context.container
        runAsync {
            container.habitRepository.rescheduleAll()
            container.alarmRepository.rescheduleAll()
        }
    }
}
