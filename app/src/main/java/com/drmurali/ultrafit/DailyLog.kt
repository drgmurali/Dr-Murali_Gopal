package com.drmurali.ultrafit

data class DailyLog(
    val heightCm: Int = 170,
    val weightKg: Double = 70.0,
    val steps: Int = 0,
    val waterLitres: Double = 0.0,
    val sleepHours: Double = 0.0,
    val exerciseProgram: String = ""
)
