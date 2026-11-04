package com.naikko.habitly.ui.habits

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import com.naikko.habitly.data.local.Habit
import com.naikko.habitly.ui.components.TimeDialog
import com.naikko.habitly.ui.components.rememberNotificationPermissionRequest
import com.naikko.habitly.ui.icons.HabitIcon
import com.naikko.habitly.ui.icons.HabitlyIcons
import com.naikko.habitly.ui.theme.HabitPalette
import com.naikko.habitly.util.formatClockTime

/** Create or edit a habit: name, icon, color and an optional daily reminder. */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun HabitEditorSheet(
    initial: Habit?,
    onDismiss: () -> Unit,
    onSave: (Habit) -> Unit,
    onDelete: (Habit) -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var name by rememberSaveable { mutableStateOf(initial?.name.orEmpty()) }
    var icon by rememberSaveable { mutableStateOf(initial?.icon ?: HabitIcon.Sparkle.key) }
    var color by rememberSaveable { mutableIntStateOf(initial?.color ?: 0) }
    var reminder by rememberSaveable { mutableStateOf(initial?.reminderEnabled ?: false) }
    var minutes by rememberSaveable { mutableIntStateOf(initial?.reminderMinutes ?: (9 * 60)) }
    var pickingTime by remember { mutableStateOf(false) }
    var confirmDelete by remember { mutableStateOf(false) }
    val requestNotifications = rememberNotificationPermissionRequest()
    val accent = HabitPalette.color(color)

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(
            Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            Text(if (initial == null) "New habit" else "Edit habit", style = MaterialTheme.typography.titleLarge)

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Name") },
                placeholder = { Text("e.g. Drink water") },
                singleLine = true,
                shape = MaterialTheme.shapes.medium,
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences, imeAction = ImeAction.Done),
                modifier = Modifier.fillMaxWidth(),
            )

            Section("Icon") {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    HabitIcon.entries.forEach { option ->
                        IconChoice(option, selected = option.key == icon, accent = accent) { icon = option.key }
                    }
                }
            }

            Section("Color") {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    HabitPalette.colors.forEachIndexed { index, swatch ->
                        ColorChoice(swatch, selected = index == color) { color = index }
                    }
                }
            }

            Surface(shape = MaterialTheme.shapes.medium, color = MaterialTheme.colorScheme.surfaceContainer) {
                Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(HabitlyIcons.Bell, null, tint = accent)
                    Spacer(Modifier.width(14.dp))
                    Column(Modifier.weight(1f)) {
                        Text("Daily reminder", style = MaterialTheme.typography.titleSmall)
                        Text(
                            if (reminder) "At ${formatClockTime(LocalContext.current, minutes / 60, minutes % 60)} · tap to change"
                            else "Off",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.clickable(enabled = reminder) { pickingTime = true },
                        )
                    }
                    Switch(
                        checked = reminder,
                        onCheckedChange = {
                            reminder = it
                            if (it) requestNotifications()
                        },
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                if (initial != null) {
                    TextButton(onClick = { confirmDelete = true }) {
                        Text("Delete", color = MaterialTheme.colorScheme.error)
                    }
                }
                Spacer(Modifier.weight(1f))
                TextButton(onClick = onDismiss) { Text("Cancel") }
                Spacer(Modifier.width(8.dp))
                Button(
                    enabled = name.isNotBlank(),
                    onClick = {
                        onSave(
                            (initial ?: Habit(name = "")).copy(
                                name = name.trim(),
                                icon = icon,
                                color = color,
                                reminderEnabled = reminder,
                                reminderMinutes = minutes,
                            ),
                        )
                    },
                ) { Text("Save") }
            }
        }
    }

    if (pickingTime) {
        TimeDialog(
            initialHour = minutes / 60,
            initialMinute = minutes % 60,
            onDismiss = { pickingTime = false },
            onConfirm = { h, m -> minutes = h * 60 + m; pickingTime = false },
        )
    }
    if (confirmDelete && initial != null) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            title = { Text("Delete “${initial.name}”?") },
            text = { Text("Its whole history and streaks are removed too. This can't be undone.") },
            confirmButton = {
                TextButton(onClick = { confirmDelete = false; onDelete(initial) }) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = { TextButton(onClick = { confirmDelete = false }) { Text("Cancel") } },
        )
    }
}

@Composable
private fun Section(title: String, content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(title, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        content()
    }
}

@Composable
private fun IconChoice(icon: HabitIcon, selected: Boolean, accent: Color, onClick: () -> Unit) {
    val background by animateColorAsState(
        if (selected) accent else MaterialTheme.colorScheme.surfaceContainerHigh, label = "iconChoice",
    )
    Box(
        Modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(background)
            .clickable(onClick = onClick)
            .semantics { contentDescription = icon.label; this.selected = selected },
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            icon.vector, null, Modifier.size(24.dp),
            tint = if (selected) HabitPalette.onColor(accent) else MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun ColorChoice(color: Color, selected: Boolean, onClick: () -> Unit) {
    Box(
        Modifier
            .size(40.dp)
            .clip(CircleShape)
            .border(if (selected) 3.dp else 0.dp, MaterialTheme.colorScheme.onSurface, CircleShape)
            .padding(if (selected) 5.dp else 0.dp)
            .clip(CircleShape)
            .background(color)
            .clickable(onClick = onClick)
            .semantics { this.selected = selected },
    )
}
