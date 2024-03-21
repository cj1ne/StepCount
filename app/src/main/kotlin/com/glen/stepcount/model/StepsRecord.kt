package com.glen.stepcount.model

import java.time.Instant

sealed interface StepsRecord {
    data class Available(
        val startTime: Instant,
        val endTime: Instant,
        val count: Long
    ) : StepsRecord

    data object Unavailable : StepsRecord
}