package com.jaewonlee.aidietrecord.data.repository

import androidx.room.withTransaction
import com.jaewonlee.aidietrecord.data.local.MealDatabase
import com.jaewonlee.aidietrecord.data.model.MealRecord
import com.jaewonlee.aidietrecord.data.model.toMealEntity
import com.jaewonlee.aidietrecord.data.model.toMealFoodEntity
import com.jaewonlee.aidietrecord.data.model.toMealRecord
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class MealRepository(
    private val mealDatabase: MealDatabase
) {
    private val mealDao = mealDatabase.mealDao()

    fun observeMealRecords(ownerId: Long): Flow<List<MealRecord>> {
        return mealDao.observeMealRecords(ownerId)
            .map { meals -> meals.map { it.toMealRecord() } }
    }

    suspend fun getMealRecord(mealId: Long, ownerId: Long): MealRecord? {
        return mealDao.getMealRecord(mealId, ownerId)?.toMealRecord()
    }

    suspend fun addMealRecord(mealRecord: MealRecord): Long {
        return mealDatabase.withTransaction {
            val mealId = mealDao.insertMeal(mealRecord.toMealEntity())
            val foods = mealRecord.foods.map { food -> food.toMealFoodEntity(mealId) }
            if (foods.isNotEmpty()) {
                mealDao.insertMealFoods(foods)
            }
            mealId
        }
    }

    suspend fun updateMealRecord(mealRecord: MealRecord) {
        mealDatabase.withTransaction {
            mealDao.updateMeal(mealRecord.toMealEntity())
            mealDao.deleteMealFoods(mealRecord.id)
            val foods = mealRecord.foods.map { food -> food.copy(id = 0).toMealFoodEntity(mealRecord.id) }
            if (foods.isNotEmpty()) {
                mealDao.insertMealFoods(foods)
            }
        }
    }

    suspend fun deleteMealRecord(mealRecord: MealRecord) {
        mealDao.deleteMeal(mealRecord.id, mealRecord.ownerId)
    }
}
