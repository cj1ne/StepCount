package com.glen.stepcount.domain

import com.glen.stepcount.data.repository.HealthRepository
import com.glen.stepcount.model.StepsRecord
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit
import javax.inject.Inject

class GetTodayStepsRecordFlowUseCase @Inject constructor(
    private val healthRepository: HealthRepository
) {
    operator fun invoke(): Flow<StepsRecord> {
        return healthRepository.getStepsRecordFlow().map { record ->
            when (record) {
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
}