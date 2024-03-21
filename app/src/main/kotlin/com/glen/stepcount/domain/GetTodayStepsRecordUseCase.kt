package com.glen.stepcount.domain

import com.glen.stepcount.data.repository.HealthRepository
import com.glen.stepcount.model.StepsRecord

import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit
import javax.inject.Inject

class GetTodayStepsRecordUseCase @Inject constructor(
    private val healthRepository: HealthRepository
) {
    operator fun invoke(): StepsRecord {
        return when (val record = healthRepository.getStepsRecord()) {
            is StepsRecord.Available -> {
                val startTime = ZonedDateTime.now().truncatedTo(ChronoUnit.DAYS).toInstant()
                if (startTime == record.startTime) {
                    record
                } else {
                    StepsRecord.Unavailable
                }
            }

            is StepsRecord.Unavailable -> record
        }
    }
}