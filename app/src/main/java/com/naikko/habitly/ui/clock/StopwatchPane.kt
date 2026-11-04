package com.naikko.habitly.ui.clock

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LargeFloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameMillis
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.naikko.habitly.ui.icons.HabitlyIcons
import com.naikko.habitly.ui.theme.TabularNumbers
import com.naikko.habitly.util.formatStopwatch
import com.naikko.habitly.viewmodel.StopwatchViewModel
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun StopwatchPane(viewModel: StopwatchViewModel) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    // Re-read the clock once per display frame while running: smooth hundredths, no drift.
    var now by remember { mutableLongStateOf(viewModel.now()) }
    LaunchedEffect(state.running) {
        if (state.running) while (true) withFrameMillis { now = viewModel.now() }
    }
    val elapsed = state.elapsed(now)

    Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
        Spacer(Modifier.height(12.dp))
        StopwatchDial(elapsed, Modifier.size(264.dp))
        Spacer(Modifier.height(28.dp))
        Controls(
            running = state.running,
            hasTime = elapsed > 0,
            onStartPause = viewModel::startPause,
            onLapReset = { if (state.running) viewModel.lap() else viewModel.reset() },
        )
        Spacer(Modifier.height(20.dp))
        LapList(state.laps, elapsed, state.running, Modifier.weight(1f))
    }
}

/** Ring that sweeps once a minute, with a tick dial and the time in the middle. */
@Composable
private fun StopwatchDial(elapsed: Long, modifier: Modifier = Modifier) {
    val track = MaterialTheme.colorScheme.surfaceContainerHighest
    val ticks = MaterialTheme.colorScheme.outlineVariant
    val accent = MaterialTheme.colorScheme.primary
    val sweep = (elapsed % 60_000L) / 60_000f * 360f
    val (main, fraction) = formatStopwatch(elapsed)
    Box(modifier, contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxSize()) {
            val strokePx = 10.dp.toPx()
            val radius = size.minDimension / 2 - strokePx
            drawCircle(track, radius, style = Stroke(strokePx))
            // 60 small ticks inside the ring, every fifth one longer.
            for (i in 0 until 60) {
                rotate(i * 6f) {
                    val long = i % 5 == 0
                    val inner = radius - strokePx - (if (long) 10.dp else 5.dp).toPx()
                    val outer = radius - strokePx - 2.dp.toPx()
                    drawLine(ticks, Offset(center.x, center.y - inner), Offset(center.x, center.y - outer), 1.5.dp.toPx(), StrokeCap.Round)
                }
            }
            if (sweep > 0f) {
                drawArc(
                    color = accent, startAngle = -90f, sweepAngle = sweep, useCenter = false,
                    topLeft = Offset(center.x - radius, center.y - radius),
                    size = androidx.compose.ui.geometry.Size(radius * 2, radius * 2),
                    style = Stroke(strokePx, cap = StrokeCap.Round),
                )
                val angle = Math.toRadians((sweep - 90f).toDouble())
                drawCircle(accent, strokePx * 0.9f, Offset(center.x + radius * cos(angle).toFloat(), center.y + radius * sin(angle).toFloat()))
            }
        }
        Row(verticalAlignment = Alignment.Bottom) {
            Text(main, style = MaterialTheme.typography.displayMedium.merge(TabularNumbers))
            Text(
                fraction,
                style = MaterialTheme.typography.headlineSmall.merge(TabularNumbers),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 6.dp),
            )
        }
    }
}

@Composable
private fun Controls(running: Boolean, hasTime: Boolean, onStartPause: () -> Unit, onLapReset: () -> Unit) {
    val mainColor by animateColorAsState(
        if (running) MaterialTheme.colorScheme.tertiaryContainer else MaterialTheme.colorScheme.primary,
        tween(250), label = "startPause",
    )
    val mainContent = if (running) MaterialTheme.colorScheme.onTertiaryContainer else MaterialTheme.colorScheme.onPrimary
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(32.dp)) {
        FilledTonalIconButton(
            onClick = onLapReset,
            enabled = running || hasTime,
            modifier = Modifier.size(64.dp),
            colors = IconButtonDefaults.filledTonalIconButtonColors(),
        ) {
            AnimatedContent(running, transitionSpec = { scaleIn() + fadeIn() togetherWith scaleOut() + fadeOut() }, label = "lapReset") { isRunning ->
                Icon(
                    if (isRunning) HabitlyIcons.Lap else HabitlyIcons.Reset,
                    contentDescription = if (isRunning) "Lap" else "Reset",
                )
            }
        }
        LargeFloatingActionButton(
            onClick = onStartPause,
            shape = CircleShape,
            containerColor = mainColor,
            contentColor = mainContent,
        ) {
            AnimatedContent(running, transitionSpec = { scaleIn() + fadeIn() togetherWith scaleOut() + fadeOut() }, label = "playPause") { isRunning ->
                Icon(
                    if (isRunning) HabitlyIcons.Pause else HabitlyIcons.Play,
                    contentDescription = if (isRunning) "Pause" else "Start",
                    modifier = Modifier.size(36.dp),
                )
            }
        }
        // Balances the row so the main button stays centered.
        Spacer(Modifier.width(64.dp))
    }
}

/** Newest lap first, with the live lap on top while running. Fastest/slowest marked once there are 3+. */
@Composable
private fun LapList(laps: List<Long>, elapsed: Long, running: Boolean, modifier: Modifier = Modifier) {
    val splits = laps.mapIndexed { i, total -> total - (if (i == 0) 0 else laps[i - 1]) }
    val fastest = if (splits.size >= 3) splits.indexOf(splits.min()) else -1
    val slowest = if (splits.size >= 3) splits.indexOf(splits.max()) else -1
    val rows = buildList {
        if (running && laps.isNotEmpty()) add(Triple(laps.size + 1, elapsed - laps.last(), elapsed))
        for (i in laps.indices.reversed()) add(Triple(i + 1, splits[i], laps[i]))
    }
    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        items(rows, key = { it.first }) { (number, split, total) ->
            val index = number - 1
            val color = when (index) {
                fastest -> MaterialTheme.colorScheme.primary
                slowest -> MaterialTheme.colorScheme.tertiary
                else -> MaterialTheme.colorScheme.onSurface
            }
            Surface(shape = MaterialTheme.shapes.small, color = MaterialTheme.colorScheme.surfaceContainerLow, modifier = Modifier.animateItem()) {
                Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text("Lap $number", style = MaterialTheme.typography.bodyMedium, color = color, modifier = Modifier.weight(1f))
                    Text(
                        formatStopwatch(split).let { it.first + it.second },
                        style = MaterialTheme.typography.bodyLarge.merge(TabularNumbers),
                        fontWeight = FontWeight.Medium,
                        color = color,
                    )
                    Spacer(Modifier.width(16.dp))
                    Text(
                        formatStopwatch(total).let { it.first + it.second },
                        style = MaterialTheme.typography.bodyMedium.merge(TabularNumbers),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}
