package com.naikko.habitly.data

/** Streak math on sets of completed epoch days. Pure functions, unit tested. */
object Streaks {
    /**
     * Consecutive done days ending today. If today isn't done yet, the streak ending
     * yesterday still counts (it isn't broken until the day is over).
     */
    fun current(doneDays: Set<Long>, today: Long): Int {
        var day = if (today in doneDays) today else today - 1
        var count = 0
        while (day in doneDays) {
            count++
            day--
        }
        return count
    }

    /** Longest run of consecutive done days ever. */
    fun best(doneDays: Set<Long>): Int {
        var best = 0
        var run = 0
        var previous: Long? = null
        for (day in doneDays.sorted()) {
            run = if (previous != null && day == previous + 1) run + 1 else 1
            if (run > best) best = run
            previous = day
        }
        return best
    }
}
