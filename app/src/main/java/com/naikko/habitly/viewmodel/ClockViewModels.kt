package com.naikko.habitly.viewmodel

import android.os.SystemClock
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.naikko.habitly.data.local.Alarm
import com.naikko.habitly.data.repository.AlarmRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Stopwatch state is just timestamps, so nothing ticks in the ViewModel. The UI reads the
 * clock every frame while running, which keeps the display perfectly smooth and accurate.
 */
data class StopwatchState(
    val running: Boolean = false,
    /** Time banked before the current run started. */
    val accumulatedMs: Long = 0,
    /** Clock reading when the current run started. */
    val startedAt: Long = 0,
    /** Total elapsed time at each lap press, oldest first. */
    val laps: List<Long> = emptyList(),
) {
    fun elapsed(now: Long): Long = accumulatedMs + if (running) (now - startedAt).coerceAtLeast(0) else 0
}

class StopwatchViewModel(
    private val clock: () -> Long = SystemClock::elapsedRealtime,
) : ViewModel() {
    private val _state = MutableStateFlow(StopwatchState())
    val state: StateFlow<StopwatchState> = _state.asStateFlow()

    fun now(): Long = clock()

    fun startPause() {
        val now = clock()
        _state.update { s ->
            if (s.running) s.copy(running = false, accumulatedMs = s.elapsed(now))
            else s.copy(running = true, startedAt = now)
        }
    }

    fun lap() {
        val now = clock()
        _state.update { s -> if (s.running) s.copy(laps = s.laps + s.elapsed(now)) else s }
    }

    fun reset() {
        _state.value = StopwatchState()
    }
}

class AlarmsViewModel(private val repository: AlarmRepository) : ViewModel() {
    val alarms: StateFlow<List<Alarm>> =
        repository.alarms.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun canScheduleExact(): Boolean = repository.canScheduleExact

    fun save(alarm: Alarm) {
        viewModelScope.launch { repository.save(alarm) }
    }

    fun setEnabled(alarm: Alarm, enabled: Boolean) {
        viewModelScope.launch { repository.setEnabled(alarm, enabled) }
    }

    fun delete(alarm: Alarm) {
        viewModelScope.launch { repository.delete(alarm) }
    }
}
