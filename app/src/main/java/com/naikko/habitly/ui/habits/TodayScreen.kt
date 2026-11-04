package com.naikko.habitly.ui.habits

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.naikko.habitly.data.local.Habit
import com.naikko.habitly.ui.components.CheckButton
import com.naikko.habitly.ui.components.EmptyState
import com.naikko.habitly.ui.components.IconBadge
import com.naikko.habitly.ui.components.ProgressRing
import com.naikko.habitly.ui.icons.HabitIcon
import com.naikko.habitly.ui.icons.HabitlyIcons
import com.naikko.habitly.ui.theme.HabitPalette
import com.naikko.habitly.util.formatClockTime
import com.naikko.habitly.viewmodel.AppViewModelProvider
import com.naikko.habitly.viewmodel.HabitItem
import com.naikko.habitly.viewmodel.HabitsUiState
import com.naikko.habitly.viewmodel.HabitsViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

private const val STRIP_DAYS = 21

@Composable
fun TodayScreen(viewModel: HabitsViewModel = viewModel(factory = AppViewModelProvider.Factory)) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { viewModel.refreshDay() }

    var editing by remember { mutableStateOf<Habit?>(null) }
    var creating by remember { mutableStateOf(false) }

    Box(Modifier.fillMaxSize()) {
        LazyColumn(
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 104.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item(key = "header") { TodayHeader(state) }
            item(key = "strip") { DayStrip(state, onSelect = viewModel::selectDay) }
            if (!state.isLoading && state.habits.isEmpty()) {
                item(key = "empty") {
                    EmptyState(
                        icon = HabitlyIcons.Today,
                        title = "No habits yet",
                        message = "Add your first habit and check it off each day to build a streak.",
                    )
                }
            }
            items(state.habits, key = { it.habit.id }) { item ->
                HabitCard(
                    item = item,
                    day = state.selectedDay,
                    onToggle = { viewModel.toggle(item.habit.id, state.selectedDay) },
                    onClick = { editing = item.habit },
                    modifier = Modifier.animateItem(),
                )
            }
        }
        ExtendedFloatingActionButton(
            onClick = { creating = true },
            icon = { Icon(HabitlyIcons.Plus, contentDescription = null) },
            text = { Text("New habit") },
            modifier = Modifier.align(Alignment.BottomEnd).padding(20.dp),
        )
    }

    if (creating || editing != null) {
        HabitEditorSheet(
            initial = editing,
            onDismiss = { creating = false; editing = null },
            onSave = { viewModel.save(it); creating = false; editing = null },
            onDelete = { viewModel.delete(it); editing = null },
        )
    }
}

@Composable
private fun TodayHeader(state: HabitsUiState) {
    val date = LocalDate.ofEpochDay(state.selectedDay)
    val isToday = state.selectedDay == state.today
    val done = state.doneCount(state.selectedDay)
    val total = state.habits.size
    Row(Modifier.fillMaxWidth().padding(top = 12.dp), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text(
                date.format(DateTimeFormatter.ofPattern("EEEE, d MMMM", Locale.getDefault())),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                when {
                    isToday -> "Today"
                    state.selectedDay == state.today - 1 -> "Yesterday"
                    else -> date.format(DateTimeFormatter.ofPattern("d MMM", Locale.getDefault()))
                },
                style = MaterialTheme.typography.headlineLarge,
            )
            Spacer(Modifier.height(2.dp))
            Text(
                when {
                    total == 0 -> "Let's build something"
                    done == total -> "All $total done. Lovely!"
                    else -> "$done of $total done"
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        if (total > 0) {
            Box(contentAlignment = Alignment.Center) {
                ProgressRing(progress = done.toFloat() / total, modifier = Modifier.size(64.dp), strokeWidth = 7.dp)
                Text("${(done * 100) / total}%", style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}

/** Last three weeks as pills; tap one to check off habits for that day. */
@Composable
private fun DayStrip(state: HabitsUiState, onSelect: (Long) -> Unit) {
    val days = remember(state.today) { (state.today - STRIP_DAYS + 1..state.today).toList() }
    val listState = rememberLazyListState(initialFirstVisibleItemIndex = days.size - 1)
    LazyRow(
        state = listState,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(vertical = 4.dp),
    ) {
        items(days, key = { it }) { day ->
            DayPill(
                day = day,
                selected = day == state.selectedDay,
                isToday = day == state.today,
                progress = state.completion(day),
                onClick = { onSelect(day) },
            )
        }
    }
}

@Composable
private fun DayPill(day: Long, selected: Boolean, isToday: Boolean, progress: Float, onClick: () -> Unit) {
    val date = LocalDate.ofEpochDay(day)
    val container by animateColorAsState(
        if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainerLow,
        tween(200), label = "pill",
    )
    val content = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
    Column(
        Modifier
            .width(52.dp)
            .clip(MaterialTheme.shapes.medium)
            .background(container)
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text(
            date.dayOfWeek.getDisplayName(TextStyle.NARROW, Locale.getDefault()),
            style = MaterialTheme.typography.labelMedium,
            color = content.copy(alpha = 0.7f),
        )
        Box(contentAlignment = Alignment.Center) {
            ProgressRing(
                progress = progress,
                modifier = Modifier.size(32.dp),
                color = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary,
                trackColor = content.copy(alpha = 0.15f),
                strokeWidth = 3.dp,
            )
            Text(
                "${date.dayOfMonth}",
                style = MaterialTheme.typography.labelLarge,
                color = if (isToday && !selected) MaterialTheme.colorScheme.primary else content,
            )
        }
    }
}

@Composable
private fun HabitCard(
    item: HabitItem,
    day: Long,
    onToggle: () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val habit = item.habit
    val color = HabitPalette.color(habit.color)
    val done = item.isDone(day)
    val container by animateColorAsState(
        if (done) color.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceContainerLow,
        tween(250), label = "card",
    )
    Surface(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = container,
    ) {
        Row(Modifier.padding(start = 16.dp, end = 8.dp, top = 14.dp, bottom = 14.dp), verticalAlignment = Alignment.CenterVertically) {
            IconBadge(HabitIcon.from(habit.icon).vector, color)
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(habit.name, style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val streak = item.currentStreak
                    Icon(
                        HabitlyIcons.Flame, null, Modifier.size(15.dp),
                        tint = if (streak > 0) color else MaterialTheme.colorScheme.outline,
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        when (streak) {
                            0 -> "No streak yet"
                            1 -> "1 day"
                            else -> "$streak days"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    if (habit.reminderEnabled) {
                        Spacer(Modifier.width(12.dp))
                        Icon(HabitlyIcons.Bell, null, Modifier.size(14.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(Modifier.width(3.dp))
                        Text(
                            formatClockTime(
                                androidx.compose.ui.platform.LocalContext.current,
                                habit.reminderMinutes / 60,
                                habit.reminderMinutes % 60,
                            ),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
            CheckButton(checked = done, color = color, onToggle = onToggle, label = "${habit.name} done")
        }
    }
}
