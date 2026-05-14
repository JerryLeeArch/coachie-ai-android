package com.jaewonlee.aidietrecord.data.repository

import com.jaewonlee.aidietrecord.data.local.MealDao
import com.jaewonlee.aidietrecord.data.model.MealRecord
import kotlinx.coroutines.flow.Flow

class MealRepository(
    private val mealDao: MealDao
) {
    fun observeMealRecords(): Flow<List<MealRecord>> = mealDao.observeMealRecords()

    suspend fun getMealRecord(mealId: Long): MealRecord? = mealDao.getMealRecord(mealId)

    suspend fun addMealRecord(mealRecord: MealRecord): Long = mealDao.insertMealRecord(mealRecord)

    suspend fun updateMealRecord(mealRecord: MealRecord) = mealDao.updateMealRecord(mealRecord)

    suspend fun deleteMealRecord(mealRecord: MealRecord) = mealDao.deleteMealRecord(mealRecord)
}
