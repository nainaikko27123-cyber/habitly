package com.naikko.habitly.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface HabitDao {
    @Query("SELECT * FROM habits ORDER BY createdEpochDay, id")
    fun observeHabits(): Flow<List<Habit>>

    @Query("SELECT * FROM habit_checks")
    fun observeChecks(): Flow<List<HabitCheck>>

    @Query("SELECT * FROM habits")
    suspend fun getAll(): List<Habit>

    @Query("SELECT * FROM habits WHERE id = :id")
    suspend fun get(id: Long): Habit?

    @Insert
    suspend fun insert(habit: Habit): Long

    @Update
    suspend fun update(habit: Habit)

    @Delete
    suspend fun delete(habit: Habit)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertCheck(check: HabitCheck)

    @Query("DELETE FROM habit_checks WHERE habitId = :habitId AND epochDay = :epochDay")
    suspend fun deleteCheck(habitId: Long, epochDay: Long)

    @Query("SELECT COUNT(*) FROM habit_checks WHERE habitId = :habitId AND epochDay = :epochDay")
    suspend fun countCheck(habitId: Long, epochDay: Long): Int
}

@Dao
interface NoteDao {
    /** Open items first, newest first; completed tasks sink to the bottom. */
    @Query("SELECT * FROM notes ORDER BY done ASC, createdAt DESC")
    fun observeAll(): Flow<List<Note>>

    @Insert
    suspend fun insert(note: Note): Long

    @Update
    suspend fun update(note: Note)

    @Delete
    suspend fun delete(note: Note)

    @Query("DELETE FROM notes WHERE isTask = 1 AND done = 1")
    suspend fun deleteCompleted()
}

@Dao
interface AlarmDao {
    @Query("SELECT * FROM alarms ORDER BY hour, minute")
    fun observeAll(): Flow<List<Alarm>>

    @Query("SELECT * FROM alarms")
    suspend fun getAll(): List<Alarm>

    @Query("SELECT * FROM alarms WHERE id = :id")
    suspend fun get(id: Long): Alarm?

    @Insert
    suspend fun insert(alarm: Alarm): Long

    @Update
    suspend fun update(alarm: Alarm)

    @Delete
    suspend fun delete(alarm: Alarm)
}
