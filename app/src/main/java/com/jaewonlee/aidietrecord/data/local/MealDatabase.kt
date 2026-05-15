package com.jaewonlee.aidietrecord.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.jaewonlee.aidietrecord.data.model.MealRecord

@Database(
    entities = [MealRecord::class],
    version = 2,
    exportSchema = true
)
abstract class MealDatabase : RoomDatabase() {
    abstract fun mealDao(): MealDao
}
