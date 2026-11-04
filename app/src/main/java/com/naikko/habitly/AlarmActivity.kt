package com.naikko.habitly

import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import com.naikko.habitly.data.local.Alarm
import com.naikko.habitly.reminders.AlarmReceiver
import com.naikko.habitly.reminders.AlarmScheduler
import com.naikko.habitly.reminders.Notifications
import com.naikko.habitly.ui.icons.HabitlyIcons
import com.naikko.habitly.ui.theme.HabitlyTheme
import com.naikko.habitly.ui.theme.TabularNumbers
import com.naikko.habitly.util.formatClockTime
import kotlinx.coroutines.launch

/** Full-screen ringing page, shown over the lock screen when an alarm goes off. */
class AlarmActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON)
        }
        enableEdgeToEdge()
        val alarmId = intent.getLongExtra(AlarmReceiver.EXTRA_ALARM_ID, 0L)
        setContent {
            HabitlyTheme {
                var alarm by remember { mutableStateOf<Alarm?>(null) }
                LaunchedEffect(alarmId) { alarm = container.alarmRepository.get(alarmId) }
                RingingScreen(
                    alarm = alarm,
                    onSnooze = {
                        Notifications.cancelAlarm(this, alarmId)
                        container.alarmScheduler.snooze(alarmId)
                        finish()
                    },
                    onDismiss = {
                        Notifications.cancelAlarm(this, alarmId)
                        finish()
                    },
                )
            }
        }
    }
}

@Composable
private fun RingingScreen(alarm: Alarm?, onSnooze: () -> Unit, onDismiss: () -> Unit) {
    val pulse by rememberInfiniteTransition(label = "pulse").animateFloat(
        initialValue = 1f, targetValue = 1.12f,
        animationSpec = infiniteRepeatable(tween(700), RepeatMode.Reverse), label = "pulseScale",
    )
    Column(
        Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surface).padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box(
            Modifier.size(120.dp).scale(pulse).clip(CircleShape).background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center,
        ) {
            Icon(HabitlyIcons.Alarm, null, Modifier.size(56.dp), tint = MaterialTheme.colorScheme.onPrimaryContainer)
        }
        Box(Modifier.height(32.dp))
        if (alarm != null) {
            Text(
                formatClockTime(androidx.compose.ui.platform.LocalContext.current, alarm.hour, alarm.minute),
                style = MaterialTheme.typography.displayLarge.merge(TabularNumbers),
            )
            Text(alarm.label.ifBlank { "Alarm" }, style = MaterialTheme.typography.titleLarge)
        }
        Box(Modifier.height(48.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            FilledTonalButton(onClick = onSnooze, modifier = Modifier.height(56.dp).width(150.dp)) {
                Text("Snooze ${AlarmScheduler.SNOOZE_MINUTES} min")
            }
            Button(onClick = onDismiss, modifier = Modifier.height(56.dp).width(150.dp)) { Text("Dismiss") }
        }
    }
}
