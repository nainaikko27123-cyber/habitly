package com.naikko.habitly.reminders

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.naikko.habitly.MainActivity
import com.naikko.habitly.data.local.Alarm
import com.naikko.habitly.data.local.Habit
import com.naikko.habitly.util.TimeMath
import java.time.ZonedDateTime

internal fun AlarmManager.canExact(): Boolean =
    Build.VERSION.SDK_INT < Build.VERSION_CODES.S || canScheduleExactAlarms()

private const val PI_FLAGS = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE

/** Books one daily reminder per habit with AlarmManager. The receiver books the next day after it fires. */
class ReminderScheduler(private val context: Context) {
    private val alarmManager = context.getSystemService(AlarmManager::class.java)

    fun schedule(habit: Habit) {
        if (!habit.reminderEnabled || habit.id == 0L) {
            cancel(habit.id)
            return
        }
        val at = TimeMath.nextOccurrence(
            hour = habit.reminderMinutes / 60,
            minute = habit.reminderMinutes % 60,
            days = 0,
            now = ZonedDateTime.now(),
        ).toInstant().toEpochMilli()
        val pending = remindIntent(habit.id)
        if (alarmManager.canExact()) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, at, pending)
        } else {
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, at, pending)
        }
    }

    fun cancel(habitId: Long) = alarmManager.cancel(remindIntent(habitId))

    private fun remindIntent(habitId: Long): PendingIntent = PendingIntent.getBroadcast(
        context,
        habitId.toInt(),
        Intent(context, ReminderReceiver::class.java)
            .setAction(ReminderReceiver.ACTION_REMIND)
            .putExtra(ReminderReceiver.EXTRA_HABIT_ID, habitId),
        PI_FLAGS,
    )
}

/**
 * Alarm clock scheduling. Uses setAlarmClock, so the system shows the alarm icon and
 * treats it like a real alarm (fires in Doze, exact time).
 */
class AlarmScheduler(private val context: Context) {
    private val alarmManager = context.getSystemService(AlarmManager::class.java)

    val canScheduleExact: Boolean get() = alarmManager.canExact()

    fun schedule(alarm: Alarm) {
        if (!alarm.enabled) {
            cancel(alarm.id)
            return
        }
        val at = TimeMath.nextOccurrence(alarm.hour, alarm.minute, alarm.days, ZonedDateTime.now())
            .toInstant().toEpochMilli()
        setAt(at, fireIntent(alarm.id, snooze = false))
    }

    fun snooze(alarmId: Long, minutes: Int = SNOOZE_MINUTES) {
        setAt(System.currentTimeMillis() + minutes * 60_000L, fireIntent(alarmId, snooze = true))
    }

    fun cancel(alarmId: Long) {
        alarmManager.cancel(fireIntent(alarmId, snooze = false))
        alarmManager.cancel(fireIntent(alarmId, snooze = true))
    }

    private fun setAt(atMillis: Long, operation: PendingIntent) {
        if (alarmManager.canExact()) {
            alarmManager.setAlarmClock(AlarmManager.AlarmClockInfo(atMillis, showIntent()), operation)
        } else {
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, atMillis, operation)
        }
    }

    private fun fireIntent(alarmId: Long, snooze: Boolean): PendingIntent = PendingIntent.getBroadcast(
        context,
        (alarmId * 2 + if (snooze) 1 else 0).toInt(),
        Intent(context, AlarmReceiver::class.java)
            .setAction(AlarmReceiver.ACTION_FIRE)
            .putExtra(AlarmReceiver.EXTRA_ALARM_ID, alarmId)
            .putExtra(AlarmReceiver.EXTRA_SNOOZE, snooze),
        PI_FLAGS,
    )

    /** Opened when the user taps the alarm icon in the status bar / lock screen. */
    private fun showIntent(): PendingIntent = PendingIntent.getActivity(
        context,
        0,
        Intent(context, MainActivity::class.java).putExtra(MainActivity.EXTRA_START_TAB, "clock"),
        PI_FLAGS,
    )

    companion object {
        const val SNOOZE_MINUTES = 10
    }
}
