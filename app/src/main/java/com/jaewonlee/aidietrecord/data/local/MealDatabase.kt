package com.jaewonlee.aidietrecord.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.jaewonlee.aidietrecord.data.model.MealEntity
import com.jaewonlee.aidietrecord.data.model.MealFoodEntity
import com.jaewonlee.aidietrecord.data.model.UserAccount

@Database(
    entities = [MealEntity::class, MealFoodEntity::class, UserAccount::class],
    version = 6,
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
                    .addMigrations(MIGRATION_4_5, MIGRATION_5_6)
                    .fallbackToDestructiveMigration(true)
                    .build()

                INSTANCE = instance
                instance
            }
        }

        private val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE meal_foods ADD COLUMN fiberGram INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE meal_foods ADD COLUMN sugarGram INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE meal_foods ADD COLUMN sodiumMilligram INTEGER NOT NULL DEFAULT 0")
            }
        }

        private val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE meal_foods ADD COLUMN description TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE meal_foods ADD COLUMN imageUri TEXT")
            }
        }
    }
}
