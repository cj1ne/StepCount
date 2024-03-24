package com.glen.stepcount

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import com.glen.stepcount.ui.notification.DEFAULT_CHANNEL_ID
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class StepCountApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        val defaultChannel = NotificationChannel(
            DEFAULT_CHANNEL_ID,
            getString(R.string.notification_default_channel_name),
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply { setShowBadge(false) }
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(defaultChannel)
    }
}