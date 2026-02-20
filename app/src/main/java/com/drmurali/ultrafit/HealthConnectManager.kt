package com.drmurali.ultrafit

import android.content.Context
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.records.SleepSessionRecord
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.time.TimeRangeFilter
import java.time.Instant
import java.time.temporal.ChronoUnit

class HealthConnectManager(private val context: Context) {

    fun availability(): Int = HealthConnectClient.getSdkStatus(context)

    suspend fun readTodaySummary(): Pair<Int, Double> {
        val client = HealthConnectClient.getOrCreate(context)
        val start = Instant.now().truncatedTo(ChronoUnit.DAYS)
        val end = Instant.now()

        val steps = client.readRecords(
            ReadRecordsRequest(
                recordType = StepsRecord::class,
                timeRangeFilter = TimeRangeFilter.between(start, end)
            )
        ).records.sumOf { it.count.toInt() }

        val sleepHours = client.readRecords(
            ReadRecordsRequest(
                recordType = SleepSessionRecord::class,
                timeRangeFilter = TimeRangeFilter.between(start.minus(1, ChronoUnit.DAYS), end)
            )
        ).records.sumOf {
            ChronoUnit.MINUTES.between(it.startTime, it.endTime).coerceAtLeast(0) / 60.0
        }

        return steps to sleepHours
    }
}
