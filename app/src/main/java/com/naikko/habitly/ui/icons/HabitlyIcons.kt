package com.naikko.habitly.ui.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.addPathNodes
import androidx.compose.ui.unit.dp

/**
 * The app's own line-icon set: 24×24 grid, 1.8 stroke, rounded ends.
 * Drawn black; [androidx.compose.material3.Icon] tints them to the content color.
 */
private fun lineIcon(name: String, vararg paths: String, stroke: Float = 1.8f): ImageVector =
    ImageVector.Builder(name, 24.dp, 24.dp, 24f, 24f).apply {
        paths.forEach { path ->
            addPath(
                pathData = addPathNodes(path),
                stroke = SolidColor(Color.Black),
                strokeLineWidth = stroke,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round,
            )
        }
    }.build()

private fun solidIcon(name: String, vararg paths: String): ImageVector =
    ImageVector.Builder(name, 24.dp, 24.dp, 24f, 24f).apply {
        paths.forEach { path ->
            addPath(
                pathData = addPathNodes(path),
                fill = SolidColor(Color.Black),
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 1.5f,
                strokeLineJoin = StrokeJoin.Round,
            )
        }
    }.build()

private const val CIRCLE_9 = "M3 12a9 9 0 1 0 18 0a9 9 0 1 0 -18 0"

object HabitlyIcons {
    // Navigation
    val Today by lazy { lineIcon("Today", CIRCLE_9, "M8.2 12.4l2.6 2.6l5 -5.4") }
    val Calendar by lazy {
        lineIcon(
            "Calendar",
            "M5.5 5h13a2.5 2.5 0 0 1 2.5 2.5v11a2.5 2.5 0 0 1 -2.5 2.5h-13a2.5 2.5 0 0 1 -2.5 -2.5v-11a2.5 2.5 0 0 1 2.5 -2.5Z",
            "M3 10h18", "M8 3v4", "M16 3v4", "M8 14h.01", "M12 14h.01", "M16 14h.01", "M8 17.5h.01", "M12 17.5h.01",
        )
    }
    val Timer by lazy { lineIcon("Timer", "M4 13a8 8 0 1 0 16 0a8 8 0 1 0 -16 0", "M10 2.5h4", "M12 13l3.2 -3.2", "M18.5 5.5l1.2 -1.2") }
    val Notes by lazy {
        lineIcon(
            "Notes",
            "M7 3h6.5l5.5 5.5v10a2.5 2.5 0 0 1 -2.5 2.5h-9.5a2.5 2.5 0 0 1 -2.5 -2.5v-13a2.5 2.5 0 0 1 2.5 -2.5Z",
            "M13.5 3v5.5h5.5", "M8.5 13h7", "M8.5 16.5h4.5",
        )
    }

    // Actions
    val Plus by lazy { lineIcon("Plus", "M12 5v14", "M5 12h14", stroke = 2.2f) }
    val Check by lazy { lineIcon("Check", "M5 12.5l4.5 4.5l9.5 -10", stroke = 2.6f) }
    val Close by lazy { lineIcon("Close", "M6.5 6.5l11 11", "M17.5 6.5l-11 11", stroke = 2f) }
    val Trash by lazy {
        lineIcon("Trash", "M4 7h16", "M10 11v6", "M14 11v6", "M6 7l1 12a2 2 0 0 0 2 2h6a2 2 0 0 0 2 -2l1 -12", "M9 7v-3h6v3")
    }
    val Bell by lazy { lineIcon("Bell", "M6 16.5v-5.5a6 6 0 1 1 12 0v5.5l1.5 2h-15Z", "M10 21h4") }
    val Flame by lazy {
        lineIcon(
            "Flame",
            "M12 3c1 3.2 5.5 5.6 5.5 10.5a5.5 5.5 0 0 1 -11 0c0 -2.6 1.4 -4.3 2.6 -5.3c0 2 0.9 3.3 2.1 3.3c0 -3 -0.6 -5.5 0.8 -8.5Z",
        )
    }
    val Play by lazy { solidIcon("Play", "M8 5.5v13l10.5 -6.5Z") }
    val Pause by lazy { lineIcon("Pause", "M8.5 5.5v13", "M15.5 5.5v13", stroke = 3f) }
    val Reset by lazy { lineIcon("Reset", "M4.5 12a7.5 7.5 0 1 0 2.4 -5.5", "M4.5 4v4h4") }
    val Lap by lazy { lineIcon("Lap", "M5.5 21v-17", "M5.5 4h11.5l-2.2 4.2l2.2 4.3h-11.5") }
    val ChevronLeft by lazy { lineIcon("ChevronLeft", "M14.5 5.5l-6.5 6.5l6.5 6.5", stroke = 2f) }
    val ChevronRight by lazy { lineIcon("ChevronRight", "M9.5 5.5l6.5 6.5l-6.5 6.5", stroke = 2f) }
    val Alarm by lazy {
        lineIcon("Alarm", "M5 13a7 7 0 1 0 14 0a7 7 0 1 0 -14 0", "M12 9.5v3.5l2.2 2.2", "M3 6l3 -2.5", "M21 6l-3 -2.5", "M7 19.5l-1.5 1.5", "M17 19.5l1.5 1.5")
    }
    val Tag by lazy { lineIcon("Tag", "M3.5 4.5v6.3l9.7 9.7l7.3 -7.3l-9.7 -9.7h-6.3a1 1 0 0 0 -1 1Z", "M8 8.5h.01") }
    val Task by lazy { lineIcon("Task", "M6 4h12a2 2 0 0 1 2 2v12a2 2 0 0 1 -2 2h-12a2 2 0 0 1 -2 -2v-12a2 2 0 0 1 2 -2Z", "M8.5 12.2l2.4 2.4l4.6 -5") }
    val Send by lazy { lineIcon("Send", "M12 19v-14", "M6 11l6 -6l6 6", stroke = 2.2f) }
}

/** Icons a habit can wear. The key is what gets stored in the database. */
enum class HabitIcon(val key: String, val label: String, private val build: () -> ImageVector) {
    Sparkle("sparkle", "Sparkle", { lineIcon("Sparkle", "M12 3.5l1.9 5.1l5.1 1.9l-5.1 1.9l-1.9 5.1l-1.9 -5.1l-5.1 -1.9l5.1 -1.9Z", "M18.5 16.5v4", "M16.5 18.5h4") }),
    Water("water", "Water", { lineIcon("Water", "M12 3.5c3.6 4.4 6 7.7 6 10.8a6 6 0 0 1 -12 0c0 -3.1 2.4 -6.4 6 -10.8Z", "M9.2 14.5a2.8 2.8 0 0 0 2.8 2.8") }),
    Read("read", "Read", { lineIcon("Read", "M3.5 5.5c2.8 -1.2 5.6 -1.2 8.5 0.5v13.5c-2.9 -1.7 -5.7 -1.7 -8.5 -0.5Z", "M20.5 5.5c-2.8 -1.2 -5.6 -1.2 -8.5 0.5v13.5c2.9 -1.7 5.7 -1.7 8.5 -0.5Z") }),
    Workout("workout", "Workout", { lineIcon("Workout", "M3 10v4", "M6.5 7v10", "M17.5 7v10", "M21 10v4", "M6.5 12h11") }),
    Mindful("mindful", "Mindful", { lineIcon("Mindful", "M5 19.5c0 -8.5 5 -14 15 -15c0 9.5 -5 15 -13.5 15Z", "M5 19.5l7.5 -7.5") }),
    Sleep("sleep", "Sleep", { lineIcon("Sleep", "M20 14.5a8 8 0 1 1 -10.5 -10.5a6.5 6.5 0 0 0 10.5 10.5Z") }),
    Health("health", "Health", { lineIcon("Health", "M12 20s-7.5 -4.6 -7.5 -10a4.2 4.2 0 0 1 7.5 -2.6a4.2 4.2 0 0 1 7.5 2.6c0 5.4 -7.5 10 -7.5 10Z") }),
    Code("code", "Code", { lineIcon("Code", "M8 8l-4 4l4 4", "M16 8l4 4l-4 4", "M13.5 5l-3 14") }),
    Sun("sun", "Morning", { lineIcon("Sun", "M8 12a4 4 0 1 0 8 0a4 4 0 1 0 -8 0", "M12 2.5v2", "M12 19.5v2", "M2.5 12h2", "M19.5 12h2", "M5.3 5.3l1.4 1.4", "M17.3 17.3l1.4 1.4", "M5.3 18.7l1.4 -1.4", "M17.3 6.7l1.4 -1.4") }),
    Music("music", "Music", { lineIcon("Music", "M9 17.5v-12.5l11 -2v12.5", "M3 17.5a3 3 0 1 0 6 0a3 3 0 1 0 -6 0", "M14 15.5a3 3 0 1 0 6 0a3 3 0 1 0 -6 0") }),
    Coffee("coffee", "Coffee", { lineIcon("Coffee", "M4 9h12v5a5 5 0 0 1 -5 5h-2a5 5 0 0 1 -5 -5Z", "M16 10.5h1.5a2.5 2.5 0 0 1 0 5h-1.5", "M8 3.5v2", "M12 3.5v2") }),
    Walk("walk", "Walk", { lineIcon("Walk", "M13 4.5a1.5 1.5 0 1 0 0.01 0", "M10 21l2 -5l-2.5 -3l1 -4.5l3.5 2.5l3 1", "M9.5 9l-3 1.5l-1 3.5", "M14.5 16l2.5 5") });

    val vector: ImageVector by lazy(build)

    companion object {
        fun from(key: String): HabitIcon = entries.firstOrNull { it.key == key } ?: Sparkle
    }
}
