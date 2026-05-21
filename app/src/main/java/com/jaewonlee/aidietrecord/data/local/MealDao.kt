package com.jaewonlee.aidietrecord.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.jaewonlee.aidietrecord.data.model.MealEntity
import com.jaewonlee.aidietrecord.data.model.MealFoodEntity
import com.jaewonlee.aidietrecord.data.model.MealWithFoods
import kotlinx.coroutines.flow.Flow

@Dao
interface MealDao {
    @Transaction
    @Query("SELECT * FROM meals WHERE ownerId = :ownerId ORDER BY createdAt DESC")
    fun observeMealRecords(ownerId: Long): Flow<List<MealWithFoods>>

    @Transaction
    @Query("SELECT * FROM meals WHERE id = :mealId AND ownerId = :ownerId")
    suspend fun getMealRecord(mealId: Long, ownerId: Long): MealWithFoods?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMeal(meal: MealEntity): Long

    @Update
    suspend fun updateMeal(meal: MealEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMealFoods(foods: List<MealFoodEntity>)

    @Query("DELETE FROM meal_foods WHERE mealId = :mealId")
    suspend fun deleteMealFoods(mealId: Long)

    @Query("DELETE FROM meals WHERE id = :mealId")
    suspend fun deleteMeal(mealId: Long)
}
