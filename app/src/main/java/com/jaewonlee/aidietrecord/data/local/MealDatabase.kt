package com.jaewonlee.aidietrecord.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.jaewonlee.aidietrecord.data.model.MealEntity
import com.jaewonlee.aidietrecord.data.model.MealFoodEntity
import com.jaewonlee.aidietrecord.data.model.UserAccount

@Database(
    entities = [MealEntity::class, MealFoodEntity::class, UserAccount::class],
    version = 4,
    exportSchema = false
)
abstract class MealDatabase : RoomDatabase() {
    abstract fun mealDao(): MealDao
    abstract fun authDao(): AuthDao

    companion object {
        @Volatile
        private var INSTANCE: MealDatabase? = null

        fun getDatabase(context: Context): MealDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    MealDatabase::class.java,
                    "meal_records.db"
                )
                    .fallbackToDestructiveMigration(true)
                    .build()

                INSTANCE = instance
                instance
            }
        }
    }
}
