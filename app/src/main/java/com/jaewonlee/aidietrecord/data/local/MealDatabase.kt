package com.jaewonlee.aidietrecord.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.jaewonlee.aidietrecord.data.model.BodyMeasurementEntity
import com.jaewonlee.aidietrecord.data.model.GoalPlanEntity
import com.jaewonlee.aidietrecord.data.model.MealEntity
import com.jaewonlee.aidietrecord.data.model.MealFoodEntity
import com.jaewonlee.aidietrecord.data.model.UserAccount

@Database(
    entities = [
        MealEntity::class,
        MealFoodEntity::class,
        UserAccount::class,
        GoalPlanEntity::class,
        BodyMeasurementEntity::class
    ],
    version = 8,
    exportSchema = false
)
abstract class MealDatabase : RoomDatabase() {
    abstract fun mealDao(): MealDao
    abstract fun authDao(): AuthDao
    abstract fun goalDao(): GoalDao

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
                    .addMigrations(MIGRATION_4_5, MIGRATION_5_6, MIGRATION_6_7, MIGRATION_7_8)
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

        private val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS goal_plans (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        ownerId INTEGER NOT NULL,
                        validFromEpochDay INTEGER NOT NULL,
                        validToEpochDay INTEGER NOT NULL,
                        startWeightKg REAL NOT NULL,
                        startMuscleMassKg REAL,
                        startBodyFatPercent REAL,
                        targetWeightKg REAL,
                        targetMuscleMassKg REAL,
                        targetBodyFatPercent REAL,
                        dailyCalories INTEGER NOT NULL,
                        dailyCarbsGram INTEGER NOT NULL,
                        dailyProteinGram INTEGER NOT NULL,
                        dailyFatGram INTEGER NOT NULL,
                        dailyFiberGram INTEGER NOT NULL,
                        dailySugarGram INTEGER NOT NULL,
                        dailySodiumMilligram INTEGER NOT NULL,
                        planSummary TEXT NOT NULL,
                        plannerVersion TEXT NOT NULL,
                        createdAt INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS body_measurements (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        ownerId INTEGER NOT NULL,
                        measuredEpochDay INTEGER NOT NULL,
                        weightKg REAL NOT NULL,
                        muscleMassKg REAL,
                        bodyFatPercent REAL,
                        createdAt INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS index_goal_plans_ownerId_validFromEpochDay " +
                        "ON goal_plans(ownerId, validFromEpochDay)"
                )
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS index_goal_plans_ownerId_validToEpochDay " +
                        "ON goal_plans(ownerId, validToEpochDay)"
                )
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS index_body_measurements_ownerId_measuredEpochDay " +
                        "ON body_measurements(ownerId, measuredEpochDay)"
                )
            }
        }

        private val MIGRATION_7_8 = object : Migration(7, 8) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS body_measurements_new (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        ownerId INTEGER NOT NULL,
                        measuredEpochDay INTEGER NOT NULL,
                        weightKg REAL,
                        muscleMassKg REAL,
                        basalMetabolicRateKcal INTEGER,
                        bodyFatMassKg REAL,
                        bodyFatPercent REAL,
                        createdAt INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    """
                    INSERT INTO body_measurements_new (
                        id,
                        ownerId,
                        measuredEpochDay,
                        weightKg,
                        muscleMassKg,
                        bodyFatPercent,
                        createdAt
                    )
                    SELECT
                        id,
                        ownerId,
                        measuredEpochDay,
                        weightKg,
                        muscleMassKg,
                        bodyFatPercent,
                        createdAt
                    FROM body_measurements
                    """.trimIndent()
                )
                db.execSQL("DROP TABLE body_measurements")
                db.execSQL("ALTER TABLE body_measurements_new RENAME TO body_measurements")
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS index_body_measurements_ownerId_measuredEpochDay " +
                        "ON body_measurements(ownerId, measuredEpochDay)"
                )
            }
        }
    }
}
