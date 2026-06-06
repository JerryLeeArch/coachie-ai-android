package com.jaewonlee.aidietrecord.data.repository

import androidx.room.withTransaction
import com.jaewonlee.aidietrecord.data.local.MealDatabase
import com.jaewonlee.aidietrecord.data.model.BodyMeasurementDraft
import com.jaewonlee.aidietrecord.data.model.BodyMeasurementEntity
import com.jaewonlee.aidietrecord.data.model.GoalPlanDraft
import com.jaewonlee.aidietrecord.data.model.GoalPlanEntity
import kotlinx.coroutines.flow.Flow

class GoalRepository(
    private val mealDatabase: MealDatabase
) {
    private val goalDao = mealDatabase.goalDao()

    fun observeBodyMeasurements(ownerId: Long): Flow<List<BodyMeasurementEntity>> {
        return goalDao.observeBodyMeasurements(ownerId)
    }

    fun observeGoalPlans(ownerId: Long): Flow<List<GoalPlanEntity>> {
        return goalDao.observeGoalPlans(ownerId)
    }

    fun observeActiveGoalPlan(ownerId: Long, epochDay: Long): Flow<GoalPlanEntity?> {
        return goalDao.observeActiveGoalPlan(ownerId, epochDay)
    }

    suspend fun saveBodyMeasurement(ownerId: Long, draft: BodyMeasurementDraft) {
        require(draft.hasAnyValue()) { "Enter at least one body measurement." }
        goalDao.insertBodyMeasurement(
            BodyMeasurementEntity(
                ownerId = ownerId,
                measuredEpochDay = draft.measuredEpochDay,
                weightKg = draft.weightKg,
                muscleMassKg = draft.muscleMassKg,
                basalMetabolicRateKcal = draft.basalMetabolicRateKcal,
                bodyFatMassKg = draft.bodyFatMassKg,
                bodyFatPercent = draft.bodyFatPercent,
                createdAt = System.currentTimeMillis()
            )
        )
    }

    suspend fun updateBodyMeasurement(
        ownerId: Long,
        measurement: BodyMeasurementEntity,
        draft: BodyMeasurementDraft
    ) {
        require(measurement.ownerId == ownerId) { "Body measurement owner mismatch." }
        require(draft.hasAnyValue()) { "Enter at least one body measurement." }
        goalDao.updateBodyMeasurement(
            measurement.copy(
                measuredEpochDay = draft.measuredEpochDay,
                weightKg = draft.weightKg,
                muscleMassKg = draft.muscleMassKg,
                basalMetabolicRateKcal = draft.basalMetabolicRateKcal,
                bodyFatMassKg = draft.bodyFatMassKg,
                bodyFatPercent = draft.bodyFatPercent
            )
        )
    }

    suspend fun deleteBodyMeasurement(ownerId: Long, measurementId: Long) {
        goalDao.deleteBodyMeasurement(ownerId, measurementId)
    }

    suspend fun saveGoalPlan(ownerId: Long, draft: GoalPlanDraft) {
        val now = System.currentTimeMillis()
        val validToEpochDay = draft.validToEpochDay
            .takeIf { it > draft.validFromEpochDay }
            ?: (draft.validFromEpochDay + 1L)

        mealDatabase.withTransaction {
            goalDao.closeOverlappingGoalPlans(ownerId, draft.validFromEpochDay)
            goalDao.deleteGoalPlansStartingAtOrAfter(ownerId, draft.validFromEpochDay)
            goalDao.insertGoalPlan(
                GoalPlanEntity(
                    ownerId = ownerId,
                    validFromEpochDay = draft.validFromEpochDay,
                    validToEpochDay = validToEpochDay,
                    startWeightKg = draft.startWeightKg,
                    startMuscleMassKg = draft.startMuscleMassKg,
                    startBodyFatPercent = draft.startBodyFatPercent,
                    targetWeightKg = draft.targetWeightKg,
                    targetMuscleMassKg = draft.targetMuscleMassKg,
                    targetBodyFatPercent = draft.targetBodyFatPercent,
                    dailyCalories = draft.dailyCalories,
                    dailyCarbsGram = draft.dailyCarbsGram,
                    dailyProteinGram = draft.dailyProteinGram,
                    dailyFatGram = draft.dailyFatGram,
                    dailyFiberGram = draft.dailyFiberGram,
                    dailySugarGram = draft.dailySugarGram,
                    dailySodiumMilligram = draft.dailySodiumMilligram,
                    planSummary = draft.planSummary,
                    plannerVersion = draft.plannerVersion,
                    createdAt = now
                )
            )
            goalDao.insertBodyMeasurement(
                BodyMeasurementEntity(
                    ownerId = ownerId,
                    measuredEpochDay = draft.validFromEpochDay,
                    weightKg = draft.startWeightKg,
                    muscleMassKg = draft.startMuscleMassKg,
                    basalMetabolicRateKcal = null,
                    bodyFatMassKg = null,
                    bodyFatPercent = draft.startBodyFatPercent,
                    createdAt = now
                )
            )
        }
    }
}
