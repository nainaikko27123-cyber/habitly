package com.naikko.habitly

import com.naikko.habitly.data.Streaks
import com.naikko.habitly.util.TimeMath
import com.naikko.habitly.util.formatStopwatch
import com.naikko.habitly.util.repeatSummary
import com.naikko.habitly.viewmodel.StopwatchViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.DayOfWeek
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.Locale

class StreaksTest {
    @Test fun currentCountsBackFromToday() = assertEquals(3, Streaks.current(setOf(8, 9, 10), today = 10))
    @Test fun unfinishedTodayKeepsYesterdaysStreak() = assertEquals(2, Streaks.current(setOf(8, 9), today = 10))
    @Test fun gapBreaksStreak() = assertEquals(0, Streaks.current(setOf(5, 6), today = 10))
    @Test fun bestFindsLongestRun() = assertEquals(4, Streaks.best(setOf(1, 2, 5, 6, 7, 8, 10)))
    @Test fun bestOfNothingIsZero() = assertEquals(0, Streaks.best(emptySet()))
}

class TimeMathTest {
    private val zone = ZoneId.of("Asia/Karachi")
    // Wednesday 4 Nov 2026, 08:00
    private val now = ZonedDateTime.of(2026, 11, 4, 8, 0, 0, 0, zone)

    @Test fun laterTodayStaysToday() {
        val next = TimeMath.nextOccurrence(9, 30, 0, now)
        assertEquals(4, next.dayOfMonth); assertEquals(9, next.hour); assertEquals(30, next.minute)
    }

    @Test fun earlierTimeMovesToTomorrow() = assertEquals(5, TimeMath.nextOccurrence(7, 0, 0, now).dayOfMonth)

    @Test fun exactlyNowMovesToTomorrow() = assertEquals(5, TimeMath.nextOccurrence(8, 0, 0, now).dayOfMonth)

    @Test fun weekendMaskSkipsToSaturday() {
        val next = TimeMath.nextOccurrence(7, 0, TimeMath.WEEKENDS, now)
        assertEquals(DayOfWeek.SATURDAY, next.dayOfWeek); assertEquals(7, next.dayOfMonth)
    }

    @Test fun mondayOnlyWrapsToNextWeek() {
        val next = TimeMath.nextOccurrence(7, 0, TimeMath.dayBit(DayOfWeek.MONDAY), now)
        assertEquals(DayOfWeek.MONDAY, next.dayOfWeek); assertEquals(9, next.dayOfMonth)
    }

    @Test fun summaries() {
        assertEquals("Once", repeatSummary(0))
        assertEquals("Weekdays", repeatSummary(TimeMath.WEEKDAYS))
        assertEquals("Every day", repeatSummary(TimeMath.EVERY_DAY))
        assertEquals("Mon, Wed", repeatSummary(TimeMath.dayBit(DayOfWeek.MONDAY) or TimeMath.dayBit(DayOfWeek.WEDNESDAY), Locale.UK))
    }
}

class StopwatchTest {
    private var clock = 1_000L
    private val vm = StopwatchViewModel { clock }

    @Test fun runsPausesAndResumes() {
        vm.startPause(); clock += 1_500
        assertEquals(1_500, vm.state.value.elapsed(clock))
        vm.startPause(); clock += 10_000 // paused time doesn't count
        assertFalse(vm.state.value.running)
        assertEquals(1_500, vm.state.value.elapsed(clock))
        vm.startPause(); clock += 500
        assertEquals(2_000, vm.state.value.elapsed(clock))
    }

    @Test fun lapsRecordTotalsAndResetClears() {
        vm.startPause(); clock += 1_000; vm.lap(); clock += 2_000; vm.lap()
        assertEquals(listOf(1_000L, 3_000L), vm.state.value.laps)
        vm.startPause(); vm.reset()
        assertTrue(vm.state.value.laps.isEmpty())
        assertEquals(0, vm.state.value.elapsed(clock))
    }

    @Test fun formatting() {
        assertEquals("01:05" to ".43", formatStopwatch(65_432))
        assertEquals("1:00:00" to ".00", formatStopwatch(3_600_000))
    }
}
