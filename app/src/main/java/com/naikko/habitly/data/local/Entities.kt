package com.naikko.habitly.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDate

@Entity(tableName = "habits")
data class Habit(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    /** Key of a [com.naikko.habitly.ui.icons.HabitIcon]. */
    val icon: String = "sparkle",
    /** Index into [com.naikko.habitly.ui.theme.HabitPalette]. */
    val color: Int = 0,
    val reminderEnabled: Boolean = false,
    /** Reminder time as minutes after midnight (local time). */
    val reminderMinutes: Int = 9 * 60,
    val createdEpochDay: Long = LocalDate.now().toEpochDay(),
)

/** One completed day for one habit. A row exists only when the habit was done that day. */
@Entity(
    tableName = "habit_checks",
    primaryKeys = ["habitId", "epochDay"],
    foreignKeys = [
        ForeignKey(
            entity = Habit::class,
            parentColumns = ["id"],
            childColumns = ["habitId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("epochDay")],
)
data class HabitCheck(
    val habitId: Long,
    val epochDay: Long,
)

/** A to-do (checkable) or a free-form note, optionally tagged with a category. */
@Entity(tableName = "notes")
data class Note(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val text: String,
    val tag: String = "",
    val isTask: Boolean = true,
    val done: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
)

@Entity(tableName = "alarms")
data class Alarm(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val hour: Int,
    val minute: Int,
    val label: String = "",
    /** Repeat days as a bitmask, bit 0 = Monday … bit 6 = Sunday. 0 = rings once. */
    val days: Int = 0,
    val enabled: Boolean = true,
)
