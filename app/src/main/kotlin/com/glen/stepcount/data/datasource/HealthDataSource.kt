package com.glen.stepcount.data.datasource

import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.request.AggregateRequest
import androidx.health.connect.client.time.TimeRangeFilter
import dagger.Lazy
import java.time.Instant
import javax.inject.Inject

interface HealthDataSource {
    suspend fun getSteps(startTime: Instant, endTime: Instant): Result<Long>
}

class HealthDataSourceImpl @Inject constructor(
    private val healthConnectClient: Lazy<HealthConnectClient>
) : HealthDataSource {
    override suspend fun getSteps(startTime: Instant, endTime: Instant): Result<Long> {
        return runCatching {
            val response = healthConnectClient.get().aggregate(
                AggregateRequest(
                    metrics = setOf(StepsRecord.COUNT_TOTAL),
                    timeRangeFilter = TimeRangeFilter.between(startTime, endTime)
                )
            )
            response[StepsRecord.COUNT_TOTAL] ?: 0L
        }
    }
}