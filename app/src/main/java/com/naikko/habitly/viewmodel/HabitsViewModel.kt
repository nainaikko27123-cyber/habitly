package com.naikko.habitly.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.naikko.habitly.data.Streaks
import com.naikko.habitly.data.local.Habit
import com.naikko.habitly.data.repository.HabitRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

data class HabitItem(
    val habit: Habit,
    val doneDays: Set<Long>,
    val currentStreak: Int,
    val bestStreak: Int,
) {
    fun isDone(epochDay: Long) = epochDay in doneDays
}

data class HabitsUiState(
    val today: Long = LocalDate.now().toEpochDay(),
    val selectedDay: Long = today,
    val habits: List<HabitItem> = emptyList(),
    val isLoading: Boolean = true,
) {
    fun doneCount(epochDay: Long) = habits.count { it.isDone(epochDay) }

    /** Share of habits done on a day, 0..1. */
    fun completion(epochDay: Long): Float =
        if (habits.isEmpty()) 0f else doneCount(epochDay).toFloat() / habits.size
}

/** Shared by the Today and Calendar screens. */
class HabitsViewModel(private val repository: HabitRepository) : ViewModel() {
    private val today = MutableStateFlow(LocalDate.now().toEpochDay())
    private val selectedDay = MutableStateFlow(today.value)

    val uiState: StateFlow<HabitsUiState> =
        combine(repository.habits, repository.checks, today, selectedDay) { habits, checks, day, selected ->
            val doneByHabit = checks.groupBy({ it.habitId }, { it.epochDay }).mapValues { it.value.toSet() }
            HabitsUiState(
                today = day,
                selectedDay = selected.coerceAtMost(day),
                habits = habits.map { habit ->
                    val done = doneByHabit[habit.id].orEmpty()
                    HabitItem(habit, done, Streaks.current(done, day), Streaks.best(done))
                },
                isLoading = false,
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HabitsUiState())

    fun selectDay(epochDay: Long) {
        selectedDay.value = epochDay.coerceAtMost(today.value)
    }

    /** Call on resume so the app rolls over to the new day after midnight. */
    fun refreshDay() {
        val now = LocalDate.now().toEpochDay()
        if (now == today.value) return
        val wasOnToday = selectedDay.value == today.value
        today.value = now
        if (wasOnToday) selectedDay.value = now
    }

    fun toggle(habitId: Long, epochDay: Long) {
        if (epochDay > today.value) return // can't check off the future
        val done = uiState.value.habits.firstOrNull { it.habit.id == habitId }?.isDone(epochDay) ?: false
        viewModelScope.launch { repository.setDone(habitId, epochDay, !done) }
    }

    fun save(habit: Habit) {
        viewModelScope.launch { repository.save(habit) }
    }

    fun delete(habit: Habit) {
        viewModelScope.launch { repository.delete(habit) }
    }
}
