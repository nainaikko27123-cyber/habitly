package com.naikko.habitly.util

import android.content.Context
import android.text.format.DateFormat
import com.naikko.habitly.data.local.Alarm
import java.time.DayOfWeek
import java.time.Duration
import java.time.LocalTime
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

/** "07:30" or "7:30 AM", following the phone's 12/24-hour setting. */
fun formatClockTime(context: Context, hour: Int, minute: Int): String =
    formatClockTime(hour, minute, DateFormat.is24HourFormat(context))

fun formatClockTime(hour: Int, minute: Int, is24Hour: Boolean): String =
    LocalTime.of(hour, minute).format(DateTimeFormatter.ofPattern(if (is24Hour) "HH:mm" else "h:mm a"))

/** "Once", "Every day", "Weekdays", or "Mon, Wed, Fri". */
fun repeatSummary(days: Int, locale: Locale = Locale.getDefault()): String = when (days) {
    0 -> "Once"
    TimeMath.EVERY_DAY -> "Every day"
    TimeMath.WEEKDAYS -> "Weekdays"
    TimeMath.WEEKENDS -> "Weekends"
    else -> DayOfWeek.entries
        .filter { days and TimeMath.dayBit(it) != 0 }
        .joinToString(", ") { it.getDisplayName(TextStyle.SHORT, locale) }
}

/** "Rings in 7 h 5 min" for an enabled alarm. */
fun ringsIn(alarm: Alarm, now: ZonedDateTime = ZonedDateTime.now()): String {
    val next = TimeMath.nextOccurrence(alarm.hour, alarm.minute, alarm.days, now)
    val minutes = Duration.between(now, next).toMinutes() + 1 // round up partial minutes
    val days = minutes / (24 * 60)
    val hours = (minutes % (24 * 60)) / 60
    val mins = minutes % 60
    return buildString {
        append("Rings in ")
        if (days > 0) append("${days} d ")
        if (days > 0 || hours > 0) append("${hours} h ")
        append("${mins} min")
    }
}

/** Stopwatch reading split into the big part ("01:23" / "1:02:03") and hundredths (".45"). */
fun formatStopwatch(ms: Long): Pair<String, String> {
    val totalSeconds = ms / 1000
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60
    val hundredths = (ms % 1000) / 10
    val main = if (hours > 0) "%d:%02d:%02d".format(hours, minutes, seconds) else "%02d:%02d".format(minutes, seconds)
    return main to ".%02d".format(hundredths)
}
