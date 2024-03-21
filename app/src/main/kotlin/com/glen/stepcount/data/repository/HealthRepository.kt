package com.glen.stepcount.data.repository

import com.glen.stepcount.data.datasource.HealthDataSource
import com.glen.stepcount.model.StepsRecord
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import java.time.Instant
import javax.inject.Inject

interface HealthRepository {
    fun getStepsRecord(): StepsRecord
    fun getStepsRecordFlow(): Flow<StepsRecord>
    suspend fun fetchStepsRecord(startTime: Instant, endTime: Instant): StepsRecord
}

class HealthRepositoryImpl @Inject constructor(
    private val healthDataSource: HealthDataSource
) : HealthRepository {

    private val stepsRecordState = MutableStateFlow<StepsRecord>(StepsRecord.Unavailable)

    override fun getStepsRecord(): StepsRecord {
        return stepsRecordState.value
    }

    override fun getStepsRecordFlow(): Flow<StepsRecord> {
        return stepsRecordState
    }

    override suspend fun fetchStepsRecord(startTime: Instant, endTime: Instant): StepsRecord {
        val stepsRecord = healthDataSource.getSteps(startTime, endTime).map {
            StepsRecord.Available(startTime, endTime, it)
        }.getOrDefault(StepsRecord.Unavailable)
        stepsRecordState.value = stepsRecord
        return stepsRecord
    }
}