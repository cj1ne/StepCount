package com.glen.stepcount.domain

import com.glen.stepcount.data.repository.HealthRepository
import com.glen.stepcount.model.StepsRecord
import java.time.Instant
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit
import javax.inject.Inject

class FetchTodayStepsRecordUseCase @Inject constructor(
    private val healthRepository: HealthRepository
) {
    suspend operator fun invoke(): StepsRecord {
        val startTime = ZonedDateTime.now().truncatedTo(ChronoUnit.DAYS).toInstant()
        val endTime = Instant.now()
        return healthRepository.fetchStepsRecord(startTime, endTime)
    }
}