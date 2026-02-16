package com.drmurali.ultrafit

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.PermissionController
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.SleepSessionRecord
import androidx.health.connect.client.records.StepsRecord
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

class MainActivity : ComponentActivity() {

    private lateinit var store: TrackerStore
    private lateinit var healthConnectManager: HealthConnectManager

    private val requiredPermissions = setOf(
        HealthPermission.getReadPermission(StepsRecord::class),
        HealthPermission.getReadPermission(SleepSessionRecord::class)
    )

    private val permissionLauncher =
        registerForActivityResult(PermissionController.createRequestPermissionResultContract()) { granted ->
            if (granted.containsAll(requiredPermissions)) {
                syncFromHealthConnect()
            } else {
                Toast.makeText(this, "Health Connect permissions are required to sync data.", Toast.LENGTH_LONG).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        store = TrackerStore(this)
        healthConnectManager = HealthConnectManager(this)

        bindUi()
    }

    private fun bindUi() {
        val heightInput = findViewById<EditText>(R.id.heightInput)
        val weightInput = findViewById<EditText>(R.id.weightInput)
        val stepsInput = findViewById<EditText>(R.id.stepsInput)
        val waterInput = findViewById<EditText>(R.id.waterInput)
        val sleepInput = findViewById<EditText>(R.id.sleepInput)
        val exerciseProgramInput = findViewById<EditText>(R.id.exerciseProgramInput)
        val calorieOutput = findViewById<TextView>(R.id.calorieOutput)
        val statusText = findViewById<TextView>(R.id.statusText)
        val saveButton = findViewById<Button>(R.id.saveButton)
        val readHealthConnectButton = findViewById<Button>(R.id.readHealthConnectButton)

        fun refreshCalories() {
            val current = DailyLog(
                heightCm = heightInput.text.toString().toIntOrNull() ?: 170,
                weightKg = weightInput.text.toString().toDoubleOrNull() ?: 70.0,
                steps = stepsInput.text.toString().toIntOrNull() ?: 0,
                waterLitres = waterInput.text.toString().toDoubleOrNull() ?: 0.0,
                sleepHours = sleepInput.text.toString().toDoubleOrNull() ?: 0.0,
                exerciseProgram = exerciseProgramInput.text.toString()
            )
            calorieOutput.text = "Daily calories burned: ${estimateCalories(current)} kcal"
        }

        val current = store.load()
        heightInput.setText(current.heightCm.toString())
        weightInput.setText(current.weightKg.toString())
        stepsInput.setText(current.steps.toString())
        waterInput.setText(current.waterLitres.toString())
        sleepInput.setText(current.sleepHours.toString())
        exerciseProgramInput.setText(current.exerciseProgram)
        refreshCalories()

        saveButton.setOnClickListener {
            val log = DailyLog(
                heightCm = heightInput.text.toString().toIntOrNull() ?: 170,
                weightKg = weightInput.text.toString().toDoubleOrNull() ?: 70.0,
                steps = stepsInput.text.toString().toIntOrNull() ?: 0,
                waterLitres = waterInput.text.toString().toDoubleOrNull() ?: 0.0,
                sleepHours = sleepInput.text.toString().toDoubleOrNull() ?: 0.0,
                exerciseProgram = exerciseProgramInput.text.toString()
            )
            store.save(log)
            refreshCalories()
            statusText.text = "Saved daily log locally on device."
        }

        readHealthConnectButton.setOnClickListener {
            lifecycleScope.launch {
                val availability = healthConnectManager.availability()
                if (availability != HealthConnectClient.SDK_AVAILABLE) {
                    statusText.text = "Health Connect is not available on this device."
                    return@launch
                }

                val client = HealthConnectClient.getOrCreate(this@MainActivity)
                val granted = client.permissionController.getGrantedPermissions()
                if (granted.containsAll(requiredPermissions)) {
                    syncFromHealthConnect()
                } else {
                    permissionLauncher.launch(requiredPermissions)
                }
            }
        }
    }

    private fun syncFromHealthConnect() {
        val stepsInput = findViewById<EditText>(R.id.stepsInput)
        val sleepInput = findViewById<EditText>(R.id.sleepInput)
        val statusText = findViewById<TextView>(R.id.statusText)
        val calorieOutput = findViewById<TextView>(R.id.calorieOutput)

        lifecycleScope.launch {
            runCatching {
                healthConnectManager.readTodaySummary()
            }.onSuccess { (steps, sleepHours) ->
                stepsInput.setText(steps.toString())
                sleepInput.setText("%.1f".format(sleepHours))

                val latest = store.load().copy(
                    steps = steps,
                    sleepHours = sleepHours
                )
                store.save(latest)

                calorieOutput.text = "Daily calories burned: ${estimateCalories(latest)} kcal"
                statusText.text = "Synced today's steps and sleep from Health Connect."
            }.onFailure { error ->
                statusText.text = "Health Connect sync failed: ${error.message}"
            }
        }
    }

    private fun estimateCalories(log: DailyLog): Int {
        val weight = log.weightKg.coerceIn(30.0, 220.0)
        val height = log.heightCm.coerceIn(120, 230)
        val basalEstimate = 10 * weight + 6.25 * height
        val stepsBurn = log.steps * weight * 0.0005
        return (basalEstimate + stepsBurn).roundToInt()
    }
}
