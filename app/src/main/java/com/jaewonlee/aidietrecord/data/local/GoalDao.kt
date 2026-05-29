package com.jaewonlee.aidietrecord.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.jaewonlee.aidietrecord.data.model.BodyMeasurementEntity
import com.jaewonlee.aidietrecord.data.model.GoalPlanEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface GoalDao {
    @Query(
        """
        SELECT * FROM body_measurements
        WHERE ownerId = :ownerId
        ORDER BY measuredEpochDay DESC, createdAt DESC
        """
    )
    fun observeBodyMeasurements(ownerId: Long): Flow<List<BodyMeasurementEntity>>

    @Query(
        """
        SELECT * FROM body_measurements
        WHERE ownerId = :ownerId
        ORDER BY measuredEpochDay DESC, createdAt DESC
        """
    )
    suspend fun getBodyMeasurements(ownerId: Long): List<BodyMeasurementEntity>

    @Query(
        """
        SELECT * FROM goal_plans
        WHERE ownerId = :ownerId
        ORDER BY validFromEpochDay DESC, createdAt DESC
        """
    )
    fun observeGoalPlans(ownerId: Long): Flow<List<GoalPlanEntity>>

    @Query(
        """
        SELECT * FROM goal_plans
        WHERE ownerId = :ownerId
        ORDER BY validFromEpochDay DESC, createdAt DESC
        """
    )
    suspend fun getGoalPlans(ownerId: Long): List<GoalPlanEntity>

    @Query(
        """
        SELECT * FROM goal_plans
        WHERE ownerId = :ownerId
            AND validFromEpochDay <= :epochDay
            AND :epochDay < validToEpochDay
        ORDER BY validFromEpochDay DESC, createdAt DESC
        LIMIT 1
        """
    )
    fun observeActiveGoalPlan(ownerId: Long, epochDay: Long): Flow<GoalPlanEntity?>

    @Query(
        """
        UPDATE goal_plans
        SET validToEpochDay = :newValidToEpochDay
        WHERE ownerId = :ownerId
            AND validFromEpochDay < :newValidToEpochDay
            AND validToEpochDay > :newValidToEpochDay
        """
    )
    suspend fun closeOverlappingGoalPlans(ownerId: Long, newValidToEpochDay: Long)

    @Query(
        """
        DELETE FROM goal_plans
        WHERE ownerId = :ownerId
            AND validFromEpochDay >= :fromEpochDay
        """
    )
    suspend fun deleteGoalPlansStartingAtOrAfter(ownerId: Long, fromEpochDay: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGoalPlan(goalPlan: GoalPlanEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBodyMeasurement(bodyMeasurement: BodyMeasurementEntity): Long

    @Query("DELETE FROM goal_plans WHERE ownerId = :ownerId")
    suspend fun deleteGoalPlansForOwner(ownerId: Long)

    @Query("DELETE FROM body_measurements WHERE ownerId = :ownerId")
    suspend fun deleteBodyMeasurementsForOwner(ownerId: Long)
}
