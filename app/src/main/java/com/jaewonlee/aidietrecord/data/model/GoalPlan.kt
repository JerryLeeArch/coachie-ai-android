package com.jaewonlee.aidietrecord.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "goal_plans",
    indices = [
        Index(value = ["ownerId", "validFromEpochDay"]),
        Index(value = ["ownerId", "validToEpochDay"])
    ]
)
data class GoalPlanEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val ownerId: Long,
    val validFromEpochDay: Long,
    val validToEpochDay: Long,
    val startWeightKg: Double,
    val startMuscleMassKg: Double?,
    val startBodyFatPercent: Double?,
    val targetWeightKg: Double?,
    val targetMuscleMassKg: Double?,
    val targetBodyFatPercent: Double?,
    val dailyCalories: Int,
    val dailyCarbsGram: Int,
    val dailyProteinGram: Int,
    val dailyFatGram: Int,
    val dailyFiberGram: Int,
    val dailySugarGram: Int,
    val dailySodiumMilligram: Int,
    val planSummary: String,
    val plannerVersion: String,
    val createdAt: Long
)

@Entity(
    tableName = "body_measurements",
    indices = [Index(value = ["ownerId", "measuredEpochDay"])]
)
data class BodyMeasurementEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val ownerId: Long,
    val measuredEpochDay: Long,
    val weightKg: Double?,
    val muscleMassKg: Double?,
    val basalMetabolicRateKcal: Int?,
    val bodyFatMassKg: Double?,
    val bodyFatPercent: Double?,
    val createdAt: Long
)

data class BodyMeasurementDraft(
    val measuredEpochDay: Long,
    val weightKg: Double?,
    val muscleMassKg: Double?,
    val basalMetabolicRateKcal: Int?,
    val bodyFatMassKg: Double?,
    val bodyFatPercent: Double?
) {
    fun hasAnyValue(): Boolean {
        return weightKg != null ||
            muscleMassKg != null ||
            basalMetabolicRateKcal != null ||
            bodyFatMassKg != null ||
            bodyFatPercent != null
    }
}

data class GoalPlanDraft(
    val validFromEpochDay: Long,
    val validToEpochDay: Long,
    val startWeightKg: Double,
    val startMuscleMassKg: Double?,
    val startBodyFatPercent: Double?,
    val targetWeightKg: Double?,
    val targetMuscleMassKg: Double?,
    val targetBodyFatPercent: Double?,
    val dailyCalories: Int,
    val dailyCarbsGram: Int,
    val dailyProteinGram: Int,
    val dailyFatGram: Int,
    val dailyFiberGram: Int,
    val dailySugarGram: Int,
    val dailySodiumMilligram: Int,
    val planSummary: String,
    val plannerVersion: String = "local-goal-planner-v1"
)
