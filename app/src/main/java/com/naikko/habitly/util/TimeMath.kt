package com.naikko.habitly.util

import java.time.DayOfWeek
import java.time.ZonedDateTime

object TimeMath {
    const val EVERY_DAY = 0b1111111
    const val WEEKDAYS = 0b0011111
    const val WEEKENDS = 0b1100000

    /** Bit for a weekday in a Monday-first mask (bit 0 = Monday). */
    fun dayBit(day: DayOfWeek): Int = 1 shl (day.value - 1)

    /**
     * Next moment strictly after [now] at [hour]:[minute] local time.
     * [days] is a Monday-first bitmask; 0 means "any day" (next occurrence).
     * Built from the local date each time so daylight-saving changes are handled.
     */
    fun nextOccurrence(hour: Int, minute: Int, days: Int, now: ZonedDateTime): ZonedDateTime {
        var date = now.toLocalDate()
        repeat(8) {
            val candidate = date.atTime(hour, minute).atZone(now.zone)
            val dayAllowed = days == 0 || (days and dayBit(date.dayOfWeek)) != 0
            if (dayAllowed && candidate.isAfter(now)) return candidate
            date = date.plusDays(1)
        }
        error("No occurrence found for mask $days")
    }
}
