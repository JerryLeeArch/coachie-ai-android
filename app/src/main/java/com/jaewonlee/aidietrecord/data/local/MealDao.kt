package com.jaewonlee.aidietrecord.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.jaewonlee.aidietrecord.data.model.MealRecord
import kotlinx.coroutines.flow.Flow

@Dao
interface MealDao {
    @Query("SELECT * FROM meal_records ORDER BY createdAt DESC")
    fun observeMealRecords(): Flow<List<MealRecord>>

    @Query("SELECT * FROM meal_records WHERE id = :mealId")
    suspend fun getMealRecord(mealId: Long): MealRecord?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMealRecord(mealRecord: MealRecord): Long

    @Update
    suspend fun updateMealRecord(mealRecord: MealRecord)

    @Delete
    suspend fun deleteMealRecord(mealRecord: MealRecord)
}
