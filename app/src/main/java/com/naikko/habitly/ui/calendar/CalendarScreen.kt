package com.naikko.habitly.ui.calendar

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.naikko.habitly.ui.components.EmptyState
import com.naikko.habitly.ui.components.ScreenHeader
import com.naikko.habitly.ui.icons.HabitIcon
import com.naikko.habitly.ui.icons.HabitlyIcons
import com.naikko.habitly.ui.theme.HabitPalette
import com.naikko.habitly.viewmodel.AppViewModelProvider
import com.naikko.habitly.viewmodel.HabitItem
import com.naikko.habitly.viewmodel.HabitsUiState
import com.naikko.habitly.viewmodel.HabitsViewModel
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

private fun YearMonth.index() = year * 12 + monthValue - 1
private fun monthOf(index: Int): YearMonth = YearMonth.of(index / 12, index % 12 + 1)

@Composable
fun CalendarScreen(viewModel: HabitsViewModel = viewModel(factory = AppViewModelProvider.Factory)) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { viewModel.refreshDay() }

    val currentMonth = YearMonth.from(LocalDate.ofEpochDay(state.today)).index()
    var monthIndex by rememberSaveable { mutableIntStateOf(currentMonth) }
    var selectedId by rememberSaveable { mutableStateOf<Long?>(null) }
    val selected = state.habits.firstOrNull { it.habit.id == selectedId }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item { ScreenHeader("Calendar", subtitle = "Streaks at a glance", modifier = Modifier.padding(top = 12.dp)) }
        if (!state.isLoading && state.habits.isEmpty()) {
            item {
                EmptyState(HabitlyIcons.Calendar, "Nothing to show yet", "Your check-ins will light up this calendar.")
            }
            return@LazyColumn
        }
        item {
            HabitFilter(state.habits, selected?.habit?.id) { selectedId = it }
        }
        item {
            MonthCard(
                monthIndex = monthIndex,
                canGoForward = monthIndex < currentMonth,
                onMonthChange = { monthIndex = it },
                state = state,
                selected = selected,
                onToggle = { day -> selected?.let { viewModel.toggle(it.habit.id, day) } },
            )
        }
        item { Stats(state, selected, monthOf(monthIndex)) }
    }
}

@Composable
private fun HabitFilter(habits: List<HabitItem>, selectedId: Long?, onSelect: (Long?) -> Unit) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        item { FilterChip(selected = selectedId == null, onClick = { onSelect(null) }, label = { Text("All habits") }) }
        items(habits, key = { it.habit.id }) { item ->
            val color = HabitPalette.color(item.habit.color)
            FilterChip(
                selected = selectedId == item.habit.id,
                onClick = { onSelect(item.habit.id) },
                label = { Text(item.habit.name) },
                leadingIcon = { Icon(HabitIcon.from(item.habit.icon).vector, null, Modifier.size(18.dp), tint = color) },
            )
        }
    }
}

@Composable
private fun MonthCard(
    monthIndex: Int,
    canGoForward: Boolean,
    onMonthChange: (Int) -> Unit,
    state: HabitsUiState,
    selected: HabitItem?,
    onToggle: (Long) -> Unit,
) {
    Surface(shape = MaterialTheme.shapes.large, color = MaterialTheme.colorScheme.surfaceContainerLow) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { onMonthChange(monthIndex - 1) }) {
                    Icon(HabitlyIcons.ChevronLeft, contentDescription = "Previous month")
                }
                AnimatedContent(
                    targetState = monthIndex,
                    transitionSpec = { fadeIn(tween(200)) togetherWith fadeOut(tween(120)) },
                    modifier = Modifier.weight(1f),
                    label = "monthTitle",
                ) { index ->
                    Text(
                        monthOf(index).format(DateTimeFormatter.ofPattern("MMMM yyyy", Locale.getDefault())),
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    )
                }
                IconButton(onClick = { onMonthChange(monthIndex + 1) }, enabled = canGoForward) {
                    Icon(HabitlyIcons.ChevronRight, contentDescription = "Next month")
                }
            }
            Spacer(Modifier.height(8.dp))
            Row {
                DayOfWeek.entries.forEach { day ->
                    Text(
                        day.getDisplayName(TextStyle.NARROW, Locale.getDefault()),
                        modifier = Modifier.weight(1f),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
            AnimatedContent(
                targetState = monthIndex,
                transitionSpec = {
                    val forward = targetState > initialState
                    (slideInHorizontally(tween(260)) { if (forward) it / 4 else -it / 4 } + fadeIn(tween(260))) togetherWith
                        (slideOutHorizontally(tween(200)) { if (forward) -it / 4 else it / 4 } + fadeOut(tween(160)))
                },
                label = "monthGrid",
            ) { index ->
                MonthGrid(monthOf(index), state, selected, onToggle)
            }
            if (selected != null) {
                Text(
                    "Tap a day to check it off or undo it.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 12.dp),
                )
            }
        }
    }
}

@Composable
private fun MonthGrid(month: YearMonth, state: HabitsUiState, selected: HabitItem?, onToggle: (Long) -> Unit) {
    val offset = month.atDay(1).dayOfWeek.value - 1 // Monday-first
    val length = month.lengthOfMonth()
    val weeks = (offset + length + 6) / 7
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        repeat(weeks) { week ->
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                repeat(7) { column ->
                    val dayOfMonth = week * 7 + column - offset + 1
                    Box(Modifier.weight(1f).aspectRatio(1f), contentAlignment = Alignment.Center) {
                        if (dayOfMonth in 1..length) {
                            val epochDay = month.atDay(dayOfMonth).toEpochDay()
                            DayCell(dayOfMonth, epochDay, state, selected, onToggle)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DayCell(dayOfMonth: Int, epochDay: Long, state: HabitsUiState, selected: HabitItem?, onToggle: (Long) -> Unit) {
    val isFuture = epochDay > state.today
    val isToday = epochDay == state.today
    val scheme = MaterialTheme.colorScheme
    // Single habit: solid when done. All habits: heat-map shade by share completed.
    val (fill, text) = if (selected != null) {
        val color = HabitPalette.color(selected.habit.color)
        if (selected.isDone(epochDay)) color to HabitPalette.onColor(color) else Color.Transparent to scheme.onSurface
    } else {
        val share = state.completion(epochDay)
        when {
            share >= 1f -> scheme.primary to scheme.onPrimary
            share > 0f -> scheme.primary.copy(alpha = 0.15f + 0.45f * share) to scheme.onSurface
            else -> Color.Transparent to scheme.onSurface
        }
    }
    val animatedFill by animateColorAsState(fill, tween(220), label = "dayFill")
    Box(
        Modifier
            .fillMaxSize()
            .clip(CircleShape)
            .background(animatedFill)
            .then(if (isToday) Modifier.border(2.dp, scheme.primary, CircleShape) else Modifier)
            .clickable(enabled = selected != null && !isFuture) { onToggle(epochDay) }
            .semantics { contentDescription = LocalDate.ofEpochDay(epochDay).toString() },
        contentAlignment = Alignment.Center,
    ) {
        Text(
            "$dayOfMonth",
            style = MaterialTheme.typography.labelLarge,
            color = if (isFuture) text.copy(alpha = 0.35f) else text,
        )
    }
}

@Composable
private fun Stats(state: HabitsUiState, selected: HabitItem?, month: YearMonth) {
    val lastDay = minOf(month.atEndOfMonth().toEpochDay(), state.today)
    val monthDays = (month.atDay(1).toEpochDay()..lastDay)
    val elapsed = (lastDay - month.atDay(1).toEpochDay() + 1).coerceAtLeast(0)
    val tiles: List<Triple<ImageVector, String, String>> = if (selected != null) {
        val doneThisMonth = monthDays.count { selected.isDone(it) }
        listOf(
            Triple(HabitlyIcons.Flame, "Current streak", days(selected.currentStreak)),
            Triple(HabitlyIcons.Today, "Best streak", days(selected.bestStreak)),
            Triple(HabitlyIcons.Calendar, "This month", "$doneThisMonth / $elapsed"),
            Triple(HabitlyIcons.Check, "All-time", "${selected.doneDays.size} check-ins"),
        )
    } else {
        val perfect = monthDays.count { state.completion(it) >= 1f }
        val checkIns = monthDays.sumOf { state.doneCount(it) }
        listOf(
            Triple(HabitlyIcons.Today, "Perfect days", "$perfect this month"),
            Triple(HabitlyIcons.Check, "Check-ins", "$checkIns this month"),
            Triple(HabitlyIcons.Flame, "Longest active", days(state.habits.maxOfOrNull { it.currentStreak } ?: 0)),
            Triple(HabitlyIcons.Calendar, "Best ever", days(state.habits.maxOfOrNull { it.bestStreak } ?: 0)),
        )
    }
    val tint = selected?.let { HabitPalette.color(it.habit.color) } ?: MaterialTheme.colorScheme.primary
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        tiles.chunked(2).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                row.forEach { (icon, label, value) -> StatTile(icon, label, value, tint, Modifier.weight(1f)) }
            }
        }
    }
}

private fun days(n: Int) = if (n == 1) "1 day" else "$n days"

@Composable
private fun StatTile(icon: ImageVector, label: String, value: String, tint: Color, modifier: Modifier = Modifier) {
    Surface(modifier = modifier, shape = MaterialTheme.shapes.medium, color = MaterialTheme.colorScheme.surfaceContainerLow) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Icon(icon, null, Modifier.size(20.dp), tint = tint)
            Text(value, style = MaterialTheme.typography.titleMedium)
            Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
