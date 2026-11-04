package com.naikko.habitly.ui.clock

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.naikko.habitly.data.local.Alarm
import com.naikko.habitly.ui.components.EmptyState
import com.naikko.habitly.ui.components.rememberNotificationPermissionRequest
import com.naikko.habitly.ui.icons.HabitlyIcons
import com.naikko.habitly.ui.theme.TabularNumbers
import com.naikko.habitly.util.TimeMath
import com.naikko.habitly.util.formatClockTime
import com.naikko.habitly.util.repeatSummary
import com.naikko.habitly.util.ringsIn
import com.naikko.habitly.viewmodel.AlarmsViewModel
import java.time.DayOfWeek
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun AlarmsPane(viewModel: AlarmsViewModel) {
    val alarms by viewModel.alarms.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val requestNotifications = rememberNotificationPermissionRequest()
    var canExact by remember { mutableStateOf(viewModel.canScheduleExact()) }
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { canExact = viewModel.canScheduleExact() }

    var editing by remember { mutableStateOf<Alarm?>(null) }
    var creating by remember { mutableStateOf(false) }

    Box(Modifier.fillMaxSize()) {
        LazyColumn(
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 4.dp, bottom = 104.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            if (!canExact && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                item(key = "exact") {
                    ExactAlarmBanner {
                        context.startActivity(
                            Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM, Uri.parse("package:${context.packageName}")),
                        )
                    }
                }
            }
            if (alarms.isEmpty()) {
                item(key = "empty") {
                    EmptyState(HabitlyIcons.Alarm, "No alarms", "Tap “Add alarm” to set one.")
                }
            }
            items(alarms, key = { it.id }) { alarm ->
                AlarmCard(
                    alarm = alarm,
                    onToggle = { on ->
                        viewModel.setEnabled(alarm, on)
                        if (on) requestNotifications()
                    },
                    onClick = { editing = alarm },
                    modifier = Modifier.animateItem(),
                )
            }
        }
        ExtendedFloatingActionButton(
            onClick = { creating = true },
            icon = { Icon(HabitlyIcons.Plus, null) },
            text = { Text("Add alarm") },
            modifier = Modifier.align(Alignment.BottomEnd).padding(20.dp),
        )
    }

    if (creating || editing != null) {
        AlarmEditorSheet(
            initial = editing,
            onDismiss = { creating = false; editing = null },
            onSave = {
                viewModel.save(it)
                requestNotifications()
                creating = false; editing = null
            },
            onDelete = { viewModel.delete(it); editing = null },
        )
    }
}

@Composable
private fun ExactAlarmBanner(onAllow: () -> Unit) {
    Surface(shape = MaterialTheme.shapes.medium, color = MaterialTheme.colorScheme.tertiaryContainer) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(
                "Alarms may ring late until you allow exact alarms for Habitly.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onTertiaryContainer,
                modifier = Modifier.weight(1f),
            )
            TextButton(onClick = onAllow) { Text("Allow") }
        }
    }
}

@Composable
private fun AlarmCard(alarm: Alarm, onToggle: (Boolean) -> Unit, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val container by animateColorAsState(
        if (alarm.enabled) MaterialTheme.colorScheme.surfaceContainer else MaterialTheme.colorScheme.surfaceContainerLow,
        label = "alarmCard",
    )
    Surface(onClick = onClick, modifier = modifier.fillMaxWidth(), shape = MaterialTheme.shapes.large, color = container) {
        Row(Modifier.padding(horizontal = 20.dp, vertical = 16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f).alpha(if (alarm.enabled) 1f else 0.55f)) {
                Text(
                    formatClockTime(context, alarm.hour, alarm.minute),
                    style = MaterialTheme.typography.displaySmall.merge(TabularNumbers),
                )
                Text(
                    listOf(alarm.label.ifBlank { null }, repeatSummary(alarm.days)).filterNotNull().joinToString(" · "),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                if (alarm.enabled) {
                    Text(ringsIn(alarm), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                }
            }
            Switch(checked = alarm.enabled, onCheckedChange = onToggle)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AlarmEditorSheet(
    initial: Alarm?,
    onDismiss: () -> Unit,
    onSave: (Alarm) -> Unit,
    onDelete: (Alarm) -> Unit,
) {
    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val time = rememberTimePickerState(
        initialHour = initial?.hour ?: 7,
        initialMinute = initial?.minute ?: 0,
        is24Hour = android.text.format.DateFormat.is24HourFormat(context),
    )
    var label by rememberSaveable { mutableStateOf(initial?.label.orEmpty()) }
    var days by rememberSaveable { mutableIntStateOf(initial?.days ?: 0) }

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(
            Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(horizontal = 24.dp).padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                if (initial == null) "New alarm" else "Edit alarm",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.fillMaxWidth(),
            )
            TimePicker(state = time)
            DayToggles(days) { days = it }
            Text(
                repeatSummary(days),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            OutlinedTextField(
                value = label,
                onValueChange = { label = it },
                label = { Text("Label (optional)") },
                singleLine = true,
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier.fillMaxWidth(),
            )
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                if (initial != null) {
                    TextButton(onClick = { onDelete(initial) }) { Text("Delete", color = MaterialTheme.colorScheme.error) }
                }
                Spacer(Modifier.weight(1f))
                TextButton(onClick = onDismiss) { Text("Cancel") }
                Spacer(Modifier.width(8.dp))
                Button(onClick = {
                    onSave(
                        (initial ?: Alarm(hour = 0, minute = 0)).copy(
                            hour = time.hour, minute = time.minute, label = label.trim(), days = days, enabled = true,
                        ),
                    )
                }) { Text("Save") }
            }
        }
    }
}

/** Seven round toggles, Monday first. None selected = rings once. */
@Composable
private fun DayToggles(days: Int, onChange: (Int) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        DayOfWeek.entries.forEach { day ->
            val bit = TimeMath.dayBit(day)
            val on = days and bit != 0
            val background by animateColorAsState(
                if (on) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainerHigh, label = "day",
            )
            Box(
                Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(background)
                    .clickable { onChange(days xor bit) }
                    .semantics {
                        contentDescription = day.getDisplayName(TextStyle.FULL, Locale.getDefault())
                        selected = on
                    },
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    day.getDisplayName(TextStyle.NARROW, Locale.getDefault()),
                    style = MaterialTheme.typography.labelLarge,
                    color = if (on) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                )
            }
        }
    }
}
