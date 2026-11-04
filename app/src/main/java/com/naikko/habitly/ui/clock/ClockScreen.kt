package com.naikko.habitly.ui.clock

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.naikko.habitly.ui.components.ScreenHeader
import com.naikko.habitly.viewmodel.AlarmsViewModel
import com.naikko.habitly.viewmodel.AppViewModelProvider
import com.naikko.habitly.viewmodel.StopwatchViewModel

@Composable
fun ClockScreen(
    stopwatch: StopwatchViewModel,
    alarms: AlarmsViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    var tab by rememberSaveable { mutableIntStateOf(0) }
    val tabs = listOf("Stopwatch", "Alarms")
    Column(Modifier.fillMaxSize()) {
        ScreenHeader("Clock", subtitle = "Time it, or wake up to it", modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 20.dp))
        SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp)) {
            tabs.forEachIndexed { index, label ->
                SegmentedButton(
                    selected = tab == index,
                    onClick = { tab = index },
                    shape = SegmentedButtonDefaults.itemShape(index, tabs.size),
                ) { Text(label) }
            }
        }
        AnimatedContent(
            targetState = tab,
            transitionSpec = {
                val forward = targetState > initialState
                (fadeIn(tween(220)) + slideInHorizontally(tween(220)) { if (forward) it / 8 else -it / 8 }) togetherWith
                    fadeOut(tween(120))
            },
            label = "clockTab",
        ) { selected ->
            if (selected == 0) StopwatchPane(stopwatch) else AlarmsPane(alarms)
        }
    }
}
