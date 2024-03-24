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
    /**
     * 현지 날짜를 기준으로 00:00:00부터 현재까지의 걸음 수를 조회하여 반환
     *
     * @return [StepsRecord]
     */
    suspend operator fun invoke(): StepsRecord {
        val startTime = ZonedDateTime.now().truncatedTo(ChronoUnit.DAYS).toInstant()
        val endTime = Instant.now()
        return healthRepository.fetchStepsRecord(startTime, endTime)
    }
}