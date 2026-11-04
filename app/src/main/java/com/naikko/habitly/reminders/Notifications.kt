package com.naikko.habitly.reminders

import android.Manifest
import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.naikko.habitly.AlarmActivity
import com.naikko.habitly.MainActivity
import com.naikko.habitly.R
import com.naikko.habitly.data.local.Alarm
import com.naikko.habitly.data.local.Habit
import com.naikko.habitly.util.formatClockTime

object Notifications {
    private const val CHANNEL_REMINDERS = "habit_reminders"
    private const val CHANNEL_ALARMS = "alarms"
    private const val PI_FLAGS = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE

    private fun reminderId(habitId: Long) = 10_000 + habitId.toInt()
    private fun alarmId(alarmId: Long) = 20_000 + alarmId.toInt()

    fun createChannels(context: Context) {
        val reminders = NotificationChannel(
            CHANNEL_REMINDERS, "Habit reminders", NotificationManager.IMPORTANCE_DEFAULT,
        ).apply { description = "Daily nudges for habits you haven't checked off yet" }

        val alarms = NotificationChannel(CHANNEL_ALARMS, "Alarms", NotificationManager.IMPORTANCE_HIGH).apply {
            description = "Ringing alarms"
            setSound(
                RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM),
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build(),
            )
            enableVibration(true)
            vibrationPattern = longArrayOf(0, 700, 500, 700)
            lockscreenVisibility = Notification.VISIBILITY_PUBLIC
        }
        context.getSystemService(NotificationManager::class.java)
            .createNotificationChannels(listOf(reminders, alarms))
    }

    fun canPost(context: Context): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
            PackageManager.PERMISSION_GRANTED

    @SuppressLint("MissingPermission") // checked by canPost
    fun showHabitReminder(context: Context, habit: Habit) {
        if (!canPost(context)) return
        val open = PendingIntent.getActivity(
            context, habit.id.toInt(), Intent(context, MainActivity::class.java), PI_FLAGS,
        )
        val markDone = PendingIntent.getBroadcast(
            context,
            habit.id.toInt(),
            Intent(context, ReminderReceiver::class.java)
                .setAction(ReminderReceiver.ACTION_MARK_DONE)
                .putExtra(ReminderReceiver.EXTRA_HABIT_ID, habit.id),
            PI_FLAGS,
        )
        val notification = NotificationCompat.Builder(context, CHANNEL_REMINDERS)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(habit.name)
            .setContentText("Still open for today. Keep the streak going!")
            .setContentIntent(open)
            .setAutoCancel(true)
            .addAction(0, "Mark done", markDone)
            .build()
        NotificationManagerCompat.from(context).notify(reminderId(habit.id), notification)
    }

    fun cancelHabitReminder(context: Context, habitId: Long) =
        NotificationManagerCompat.from(context).cancel(reminderId(habitId))

    /** Rings until dismissed (insistent) and opens the full-screen alarm page on a locked phone. */
    @SuppressLint("MissingPermission") // checked by canPost
    fun showAlarm(context: Context, alarm: Alarm) {
        if (!canPost(context)) return
        val fullScreen = PendingIntent.getActivity(
            context,
            alarm.id.toInt(),
            Intent(context, AlarmActivity::class.java)
                .putExtra(AlarmReceiver.EXTRA_ALARM_ID, alarm.id)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_NO_USER_ACTION),
            PI_FLAGS,
        )
        fun action(action: String) = PendingIntent.getBroadcast(
            context,
            alarm.id.toInt(),
            Intent(context, AlarmReceiver::class.java)
                .setAction(action)
                .putExtra(AlarmReceiver.EXTRA_ALARM_ID, alarm.id),
            PI_FLAGS,
        )
        val notification = NotificationCompat.Builder(context, CHANNEL_ALARMS)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(alarm.label.ifBlank { "Alarm" })
            .setContentText(formatClockTime(context, alarm.hour, alarm.minute))
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setOngoing(true)
            .setContentIntent(fullScreen)
            .setFullScreenIntent(fullScreen, true)
            .addAction(0, "Snooze ${AlarmScheduler.SNOOZE_MINUTES} min", action(AlarmReceiver.ACTION_SNOOZE))
            .addAction(0, "Dismiss", action(AlarmReceiver.ACTION_DISMISS))
            .build()
            .apply { flags = flags or Notification.FLAG_INSISTENT }
        NotificationManagerCompat.from(context).notify(alarmId(alarm.id), notification)
    }

    fun cancelAlarm(context: Context, alarmId: Long) =
        NotificationManagerCompat.from(context).cancel(alarmId(alarmId))
}
