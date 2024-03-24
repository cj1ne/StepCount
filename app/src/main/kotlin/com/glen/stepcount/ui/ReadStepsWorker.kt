package com.glen.stepcount.ui

import android.content.Context
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

    override suspend fun doWork(): Result {
        setForeground(getForegroundInfo())
        do {
            val stepsRecord = fetchTodayStepsRecordUseCase()
            updateAppWidget(applicationContext, stepsRecord)
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

    companion object {
        private const val READ_STEPS_NOTIFICATION_ID = 1
        private const val READ_INTERVAL = 1000L
    }
}