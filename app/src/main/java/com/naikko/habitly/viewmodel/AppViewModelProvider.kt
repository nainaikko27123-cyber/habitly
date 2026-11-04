package com.naikko.habitly.viewmodel

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.naikko.habitly.HabitlyApp

/** One factory for every ViewModel, wired from the app's [com.naikko.habitly.data.AppContainer]. */
object AppViewModelProvider {
    val Factory = viewModelFactory {
        initializer { HabitsViewModel(app().container.habitRepository) }
        initializer { NotesViewModel(app().container.noteRepository) }
        initializer { AlarmsViewModel(app().container.alarmRepository) }
        initializer { StopwatchViewModel() }
    }

    private fun CreationExtras.app(): HabitlyApp =
        this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as HabitlyApp
}
