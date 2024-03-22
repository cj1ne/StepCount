package com.glen.stepcount.ui.main

import com.glen.stepcount.R
import com.glen.stepcount.model.StepsRecord
import com.glen.stepcount.ui.model.UiString

data class MainUiState(
    private val stepsRecord: StepsRecord = StepsRecord.Unavailable,
    private val isHealthConnectAvailable: Boolean = false,
    val hasReadStepsPermission: Boolean = false,
) {
    val stepCount = when (stepsRecord) {
        is StepsRecord.Available -> stepsRecord.count.toString()
        is StepsRecord.Unavailable -> "?"
    }

    val settingsStatus = if (isHealthConnectAvailable) {
        UiString.Resource(R.string.settings_completed)
    } else {
        UiString.Resource(R.string.install)
    }
    val settingsEnabled = isHealthConnectAvailable.not()

    val permissionStatus = if (hasReadStepsPermission) {
        UiString.Resource(R.string.approve_completed)
    } else {
        UiString.Resource(R.string.approve)
    }
    val permissionEnabled = isHealthConnectAvailable && hasReadStepsPermission.not()
}
