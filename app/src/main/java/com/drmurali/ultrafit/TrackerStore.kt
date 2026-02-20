package com.drmurali.ultrafit

import android.content.Context

class TrackerStore(context: Context) {
    private val prefs = context.getSharedPreferences("ultrafit_store", Context.MODE_PRIVATE)

    fun load(): DailyLog {
        return DailyLog(
            heightCm = prefs.getInt("heightCm", 170),
            weightKg = prefs.getFloat("weightKg", 70f).toDouble(),
            steps = prefs.getInt("steps", 0),
            waterLitres = prefs.getFloat("waterLitres", 0f).toDouble(),
            sleepHours = prefs.getFloat("sleepHours", 0f).toDouble(),
            exerciseProgram = prefs.getString("exerciseProgram", "") ?: ""
        )
    }

    fun save(log: DailyLog) {
        prefs.edit()
            .putInt("heightCm", log.heightCm)
            .putFloat("weightKg", log.weightKg.toFloat())
            .putInt("steps", log.steps)
            .putFloat("waterLitres", log.waterLitres.toFloat())
            .putFloat("sleepHours", log.sleepHours.toFloat())
            .putString("exerciseProgram", log.exerciseProgram)
            .apply()
    }
}
