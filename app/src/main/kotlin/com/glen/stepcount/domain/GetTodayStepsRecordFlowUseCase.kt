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
    /**
     * 오늘의 걸음 수 데이터 변동 사항을 emit하는 [Flow].
     * 최초 collect 시점에는 가장 최근에 조회된 오늘의 걸음 수 데이터를 emit.
     *
     * [StepsRecord.Available]인 경우 데이터의 시작 시간이 현재 날짜의 시작 시간과 일치하면
     * [StepsRecord.Available]를 반환하고 일치하지 않는 경우 [StepsRecord.Unavailable] 반환.
     *
     * @return [Flow]<[StepsRecord]>
     */
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