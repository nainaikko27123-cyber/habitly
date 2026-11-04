package com.naikko.habitly.ui.notes

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.naikko.habitly.data.local.Note
import com.naikko.habitly.ui.components.CheckButton
import com.naikko.habitly.ui.components.EmptyState
import com.naikko.habitly.ui.components.ScreenHeader
import com.naikko.habitly.ui.components.TagPill
import com.naikko.habitly.ui.icons.HabitlyIcons
import com.naikko.habitly.viewmodel.AppViewModelProvider
import com.naikko.habitly.viewmodel.NotesViewModel

@Composable
fun NotesScreen(viewModel: NotesViewModel = viewModel(factory = AppViewModelProvider.Factory)) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var editing by remember { mutableStateOf<Note?>(null) }

    Column(Modifier.fillMaxSize()) {
        ScreenHeader(
            title = "Notes",
            subtitle = "${state.openCount} open · ${state.doneCount} done",
            modifier = Modifier.padding(start = 20.dp, end = 12.dp, top = 20.dp),
        ) {
            if (state.doneCount > 0) TextButton(onClick = viewModel::clearCompleted) { Text("Clear done") }
        }
        QuickAddBar(
            tag = state.filter,
            onAdd = viewModel::quickAdd,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
        )
        if (state.tags.isNotEmpty()) {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                item { FilterChip(selected = state.filter == null, onClick = { viewModel.setFilter(null) }, label = { Text("All") }) }
                items(state.tags, key = { it }) { tag ->
                    FilterChip(
                        selected = state.filter.equals(tag, ignoreCase = true),
                        onClick = { viewModel.setFilter(tag) },
                        label = { Text(tag) },
                    )
                }
            }
        }
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            if (!state.isLoading && state.notes.isEmpty()) {
                item(key = "empty") {
                    EmptyState(
                        HabitlyIcons.Notes,
                        if (state.filter == null) "A clean slate" else "Nothing tagged “${state.filter}”",
                        "Jot a to-do or a thought in the box above.",
                    )
                }
            }
            items(state.notes, key = { it.id }) { note ->
                if (note.isTask) {
                    TaskRow(note, onToggle = { viewModel.toggleDone(note) }, onClick = { editing = note }, modifier = Modifier.animateItem())
                } else {
                    NoteCard(note, onClick = { editing = note }, modifier = Modifier.animateItem())
                }
            }
        }
    }

    editing?.let { note ->
        NoteEditorSheet(
            initial = note,
            tags = state.tags,
            onDismiss = { editing = null },
            onSave = { viewModel.save(it); editing = null },
            onDelete = { viewModel.delete(it); editing = null },
        )
    }
}

/** One-line capture: type, press enter. Toggle on the left switches between to-do and note. */
@Composable
private fun QuickAddBar(tag: String?, onAdd: (String, Boolean) -> Unit, modifier: Modifier = Modifier) {
    var text by rememberSaveable { mutableStateOf("") }
    var isTask by rememberSaveable { mutableStateOf(true) }
    val submit = {
        if (text.isNotBlank()) {
            onAdd(text, isTask)
            text = ""
        }
    }
    Surface(modifier = modifier.fillMaxWidth(), shape = MaterialTheme.shapes.large, color = MaterialTheme.colorScheme.surfaceContainer) {
        Row(Modifier.padding(start = 4.dp, end = 6.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { isTask = !isTask }) {
                AnimatedContent(isTask, transitionSpec = { scaleIn() + fadeIn() togetherWith scaleOut() + fadeOut() }, label = "kind") { task ->
                    Icon(
                        if (task) HabitlyIcons.Task else HabitlyIcons.Notes,
                        contentDescription = if (task) "Adding a to-do, switch to note" else "Adding a note, switch to to-do",
                        tint = MaterialTheme.colorScheme.primary,
                    )
                }
            }
            TextField(
                value = text,
                onValueChange = { text = it },
                placeholder = {
                    Text(
                        (if (isTask) "Add a to-do" else "Write a note") + (tag?.let { " in $it" } ?: "") + "…",
                        maxLines = 1, overflow = TextOverflow.Ellipsis,
                    )
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences, imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { submit() }),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                ),
                modifier = Modifier.weight(1f),
            )
            FilledIconButton(onClick = submit, enabled = text.isNotBlank()) {
                Icon(HabitlyIcons.Send, contentDescription = "Add")
            }
        }
    }
}

@Composable
private fun TaskRow(note: Note, onToggle: () -> Unit, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val textAlpha by animateFloatAsState(if (note.done) 0.5f else 1f, label = "taskAlpha")
    val container by animateColorAsState(
        if (note.done) MaterialTheme.colorScheme.surfaceContainerLowest else MaterialTheme.colorScheme.surfaceContainerLow,
        label = "taskBg",
    )
    Surface(onClick = onClick, modifier = modifier.fillMaxWidth(), shape = MaterialTheme.shapes.medium, color = container) {
        Row(Modifier.padding(start = 4.dp, end = 16.dp, top = 6.dp, bottom = 6.dp), verticalAlignment = Alignment.CenterVertically) {
            CheckButton(
                checked = note.done,
                color = MaterialTheme.colorScheme.primary,
                onToggle = onToggle,
                size = 26.dp,
                label = note.text,
            )
            Spacer(Modifier.width(4.dp))
            Column(Modifier.weight(1f).alpha(textAlpha).padding(vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    note.text,
                    style = MaterialTheme.typography.bodyLarge,
                    textDecoration = if (note.done) TextDecoration.LineThrough else null,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                )
                if (note.tag.isNotBlank()) TagPill(note.tag)
            }
        }
    }
}

@Composable
private fun NoteCard(note: Note, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Surface(onClick = onClick, modifier = modifier.fillMaxWidth(), shape = MaterialTheme.shapes.medium, color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.55f)) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(note.text, style = MaterialTheme.typography.bodyLarge, maxLines = 8, overflow = TextOverflow.Ellipsis)
            if (note.tag.isNotBlank()) TagPill(note.tag)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun NoteEditorSheet(
    initial: Note,
    tags: List<String>,
    onDismiss: () -> Unit,
    onSave: (Note) -> Unit,
    onDelete: (Note) -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var text by rememberSaveable { mutableStateOf(initial.text) }
    var tag by rememberSaveable { mutableStateOf(initial.tag) }
    var isTask by rememberSaveable { mutableStateOf(initial.isTask) }

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(
            Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(horizontal = 24.dp).padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text("Edit", style = MaterialTheme.typography.titleLarge)
            SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
                listOf(true to "To-do", false to "Note").forEachIndexed { index, (task, label) ->
                    SegmentedButton(selected = isTask == task, onClick = { isTask = task }, shape = SegmentedButtonDefaults.itemShape(index, 2)) {
                        Text(label)
                    }
                }
            }
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                minLines = 3,
                shape = MaterialTheme.shapes.medium,
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = tag,
                onValueChange = { tag = it },
                label = { Text("Category tag") },
                placeholder = { Text("e.g. Work, Groceries") },
                singleLine = true,
                leadingIcon = { Icon(HabitlyIcons.Tag, null) },
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier.fillMaxWidth(),
            )
            val suggestions = tags.filterNot { it.equals(tag.trim(), ignoreCase = true) }
            if (suggestions.isNotEmpty()) {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    suggestions.forEach { suggestion -> SuggestionChip(onClick = { tag = suggestion }, label = { Text(suggestion) }) }
                    if (tag.isNotBlank()) SuggestionChip(onClick = { tag = "" }, label = { Text("No tag") })
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                TextButton(onClick = { onDelete(initial) }) { Text("Delete", color = MaterialTheme.colorScheme.error) }
                Spacer(Modifier.weight(1f))
                TextButton(onClick = onDismiss) { Text("Cancel") }
                Spacer(Modifier.width(8.dp))
                Button(
                    enabled = text.isNotBlank(),
                    onClick = { onSave(initial.copy(text = text, tag = tag, isTask = isTask, done = isTask && initial.done)) },
                ) { Text("Save") }
            }
        }
    }
}
