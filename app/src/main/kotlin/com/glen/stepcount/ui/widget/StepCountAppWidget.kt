package com.glen.stepcount.ui.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.glen.stepcount.R
import com.glen.stepcount.domain.GetTodayStepsRecordUseCase
import com.glen.stepcount.model.StepsRecord
import com.glen.stepcount.ui.main.MainActivity
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class StepCountAppWidget : AppWidgetProvider() {

    @Inject
    lateinit var getTodayStepsRecordUseCase: GetTodayStepsRecordUseCase

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        val stepsRecord = getTodayStepsRecordUseCase()
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId, stepsRecord)
        }
    }
}

fun updateAppWidget(context: Context, stepsRecord: StepsRecord) {
    val appWidgetManager = AppWidgetManager.getInstance(context)
    val appWidgetIds = appWidgetManager.getAppWidgetIds(
        ComponentName(context, StepCountAppWidget::class.java)
    )
    for (appWidgetId in appWidgetIds) {
        updateAppWidget(context, appWidgetManager, appWidgetId, stepsRecord)
    }
}

fun updateAppWidget(
    context: Context,
    appWidgetManager: AppWidgetManager,
    appWidgetId: Int,
    stepsRecord: StepsRecord
) {
    val pendingIntent = PendingIntent.getActivity(
        context,
        0,
        Intent(context, MainActivity::class.java),
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )
    val views = RemoteViews(context.packageName, R.layout.step_count_app_widget).apply {
        setTextViewText(
            R.id.step_count,
            when (stepsRecord) {
                is StepsRecord.Available -> stepsRecord.count.toString()
                is StepsRecord.Unavailable -> "?"
            }
        )
        setOnClickPendingIntent(android.R.id.background, pendingIntent)
    }
    appWidgetManager.updateAppWidget(appWidgetId, views)
}