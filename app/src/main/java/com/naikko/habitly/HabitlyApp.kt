package com.naikko.habitly

import android.app.Application
import android.content.Context
import com.naikko.habitly.data.AppContainer
import com.naikko.habitly.reminders.Notifications

/** Application entry point. Owns the single [AppContainer] (manual dependency injection). */
class HabitlyApp : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
        Notifications.createChannels(this)
    }
}

/** Shortcut for receivers and activities that need the app's dependencies. */
val Context.container: AppContainer
    get() = (applicationContext as HabitlyApp).container
