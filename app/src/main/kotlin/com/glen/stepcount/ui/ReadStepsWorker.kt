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

    /**
     * 앱이 Foreground 상태인 경우에만 걸음 수를 조회할 수 있으므로 ForegroundService를 실행하고
     * [READ_INTERVAL]마다 걸음 수를 조회하여 위젯 업데이트 및 알림 노출.
     *
     * 헬스 커넥트 비활성화, 걸음 수 권한 거부 등으로 인해 걸음 수 조회 결과가 [StepsRecord.Unavailable]인 경우 종료.
     *
     * 헬스 커넥트는 rate limit이 있어서 해당 기준을 초과하여 조회를 요청하는 경우 Exception 발생 가능.
     *
     * https://developer.android.com/health-and-fitness/guides/health-connect/plan/rate-limiting
     *
     * @return [ListenableWorker.Result]
     */
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

    /**
     * 신규로 조회한 걸음 수와 마지막으로 조회한 걸음 수의 백단위 절사가 다른 경우 알림 노출.
     *
     * @param stepsRecord 신규 걸음 수 조회 결과
     */
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