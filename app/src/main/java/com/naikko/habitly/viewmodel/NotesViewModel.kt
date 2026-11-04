package com.naikko.habitly.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.naikko.habitly.data.local.Note
import com.naikko.habitly.data.repository.NoteRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class NotesUiState(
    val notes: List<Note> = emptyList(),
    /** Every tag in use, case-insensitively unique, A–Z. */
    val tags: List<String> = emptyList(),
    /** Selected tag filter, or null for everything. */
    val filter: String? = null,
    val isLoading: Boolean = true,
) {
    val openCount get() = notes.count { it.isTask && !it.done }
    val doneCount get() = notes.count { it.isTask && it.done }
}

class NotesViewModel(private val repository: NoteRepository) : ViewModel() {
    private val filter = MutableStateFlow<String?>(null)

    val uiState: StateFlow<NotesUiState> = combine(repository.notes, filter) { notes, selected ->
        val tags = notes.map { it.tag.trim() }
            .filter { it.isNotEmpty() }
            .distinctBy { it.lowercase() }
            .sortedBy { it.lowercase() }
        // Drop a filter whose last note was deleted.
        val active = selected?.takeIf { tag -> tags.any { it.equals(tag, ignoreCase = true) } }
        NotesUiState(
            notes = if (active == null) notes else notes.filter { it.tag.trim().equals(active, ignoreCase = true) },
            tags = tags,
            filter = active,
            isLoading = false,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), NotesUiState())

    fun setFilter(tag: String?) {
        filter.value = tag
    }

    /** Adds from the quick-add bar, tagged with the current filter so it stays visible. */
    fun quickAdd(text: String, isTask: Boolean) {
        val trimmed = text.trim()
        if (trimmed.isEmpty()) return
        viewModelScope.launch {
            repository.save(Note(text = trimmed, isTask = isTask, tag = uiState.value.filter.orEmpty()))
        }
    }

    fun save(note: Note) {
        viewModelScope.launch { repository.save(note.copy(text = note.text.trim(), tag = note.tag.trim())) }
    }

    fun toggleDone(note: Note) {
        viewModelScope.launch { repository.save(note.copy(done = !note.done)) }
    }

    fun delete(note: Note) {
        viewModelScope.launch { repository.delete(note) }
    }

    fun clearCompleted() {
        viewModelScope.launch { repository.deleteCompleted() }
    }
}
