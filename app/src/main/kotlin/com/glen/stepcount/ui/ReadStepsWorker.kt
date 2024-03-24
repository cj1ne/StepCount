package com.glen.stepcount.ui

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Context.NOTIFICATION_SERVICE
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import com.glen.stepcount.R
import com.glen.stepcount.domain.FetchTodayStepsRecordUseCase
import com.glen.stepcount.model.StepsRecord
import com.glen.stepcount.ui.main.MainActivity
import com.glen.stepcount.ui.notification.DEFAULT_CHANNEL_ID
import com.glen.stepcount.ui.widget.updateAppWidget
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.delay

@HiltWorker
class ReadStepsWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val fetchTodayStepsRecordUseCase: FetchTodayStepsRecordUseCase,
) : CoroutineWorker(context, workerParams) {

    private var stepCount = 0L

    override suspend fun doWork(): Result {
        setForeground(getForegroundInfo())
        do {
            val stepsRecord = fetchTodayStepsRecordUseCase()
            updateAppWidget(applicationContext, stepsRecord)
            updateStepCount(stepsRecord)
            delay(READ_INTERVAL)
        } while (stepsRecord != StepsRecord.Unavailable)

        return Result.success()
    }

    override suspend fun getForegroundInfo(): ForegroundInfo {
        return ForegroundInfo(
            READ_STEPS_NOTIFICATION_ID,
            NotificationCompat.Builder(applicationContext, DEFAULT_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_step)
                .setContentTitle(applicationContext.getString(R.string.read_step_work_title))
                .setContentText(applicationContext.getString(R.string.read_step_work_message))
                .build(),
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
            } else {
                0
            }
        )
    }

    private fun updateStepCount(stepsRecord: StepsRecord) {
        stepCount = when (stepsRecord) {
            is StepsRecord.Available -> {
                if (stepCount != 0L && (stepsRecord.count / 100 > (stepCount / 100))) {
                    showCheerNotification((stepsRecord.count / 100) * 100)
                }
                stepsRecord.count
            }
            is StepsRecord.Unavailable -> 0L
        }
    }

    private fun showCheerNotification(stepCount: Long) {
        val pendingIntent = PendingIntent.getActivity(
            applicationContext,
            0,
            Intent(applicationContext, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val notification = NotificationCompat.Builder(applicationContext, DEFAULT_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_step)
            .setContentTitle(
                applicationContext.getString(R.string.cheer_notification_title, stepCount)
            )
            .setContentText(
                applicationContext.getString(R.string.cheer_notification_message, stepCount)
            )
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        val notificationManager = applicationContext.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(CHEER_NOTIFICATION_ID, notification)
    }

    companion object {
        private const val READ_STEPS_NOTIFICATION_ID = 1
        private const val CHEER_NOTIFICATION_ID = 2
        private const val READ_INTERVAL = 1000L
    }
}