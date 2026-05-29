package com.jaewonlee.aidietrecord.data.repository

import androidx.room.withTransaction
import com.jaewonlee.aidietrecord.data.local.MealDatabase
import com.jaewonlee.aidietrecord.data.model.BodyMeasurementEntity
import com.jaewonlee.aidietrecord.data.model.GoalPlanEntity
import com.jaewonlee.aidietrecord.data.model.MealEntity
import com.jaewonlee.aidietrecord.data.model.MealFoodEntity
import com.jaewonlee.aidietrecord.data.model.MealWithFoods
import com.jaewonlee.aidietrecord.data.model.UserAccount
import java.io.InputStream
import java.io.OutputStream
import org.json.JSONArray
import org.json.JSONObject

class UserDataPortabilityRepository(
    private val mealDatabase: MealDatabase
) {
    private val mealDao = mealDatabase.mealDao()
    private val goalDao = mealDatabase.goalDao()

    suspend fun exportUserData(
        userAccount: UserAccount,
        outputStream: OutputStream
    ): ExportSummary {
        val meals = mealDao.getMealRecords(userAccount.id)
        val goalPlans = goalDao.getGoalPlans(userAccount.id)
        val bodyMeasurements = goalDao.getBodyMeasurements(userAccount.id)

        val root = JSONObject()
            .put("format", ExportFormat)
            .put("schemaVersion", SchemaVersion)
            .put("exportedAt", System.currentTimeMillis())
            .put(
                "user",
                JSONObject()
                    .put("firebaseUid", userAccount.firebaseUid)
                    .put("email", userAccount.userId)
                    .put("nickname", userAccount.nickname)
            )
            .put("meals", JSONArray(meals.map { it.toJson() }))
            .put("goalPlans", JSONArray(goalPlans.map { it.toJson() }))
            .put("bodyMeasurements", JSONArray(bodyMeasurements.map { it.toJson() }))

        outputStream.bufferedWriter(Charsets.UTF_8).use { writer ->
            writer.write(root.toString(2))
        }

        return ExportSummary(
            mealCount = meals.size,
            goalPlanCount = goalPlans.size,
            bodyMeasurementCount = bodyMeasurements.size
        )
    }

    suspend fun importUserData(
        ownerId: Long,
        inputStream: InputStream
    ): ExportSummary {
        val root = JSONObject(inputStream.bufferedReader(Charsets.UTF_8).use { it.readText() })
        require(root.optString("format") == ExportFormat) {
            "This file is not an AI Meal Log export."
        }
        require(root.optInt("schemaVersion") == SchemaVersion) {
            "This export version is not supported."
        }

        val meals = root.getJSONArray("meals")
        val goalPlans = root.getJSONArray("goalPlans")
        val bodyMeasurements = root.getJSONArray("bodyMeasurements")

        mealDatabase.withTransaction {
            mealDao.deleteMealsForOwner(ownerId)
            goalDao.deleteGoalPlansForOwner(ownerId)
            goalDao.deleteBodyMeasurementsForOwner(ownerId)

            for (index in 0 until meals.length()) {
                val mealJson = meals.getJSONObject(index)
                val insertedMealId = mealDao.insertMeal(mealJson.toMealEntity(ownerId))
                val foods = mealJson
                    .getJSONArray("foods")
                    .mapObjects { foodJson -> foodJson.toMealFoodEntity(insertedMealId) }
                if (foods.isNotEmpty()) {
                    mealDao.insertMealFoods(foods)
                }
            }
            for (index in 0 until goalPlans.length()) {
                val goalPlanJson = goalPlans.getJSONObject(index)
                goalDao.insertGoalPlan(goalPlanJson.toGoalPlanEntity(ownerId))
            }
            for (index in 0 until bodyMeasurements.length()) {
                val measurementJson = bodyMeasurements.getJSONObject(index)
                goalDao.insertBodyMeasurement(measurementJson.toBodyMeasurementEntity(ownerId))
            }
        }

        return ExportSummary(
            mealCount = meals.length(),
            goalPlanCount = goalPlans.length(),
            bodyMeasurementCount = bodyMeasurements.length()
        )
    }

    companion object {
        const val ExportMimeType = "application/json"
        private const val ExportFormat = "ai-diet-record-user-data"
        private const val SchemaVersion = 1
    }
}

data class ExportSummary(
    val mealCount: Int,
    val goalPlanCount: Int,
    val bodyMeasurementCount: Int
) {
    fun toStatusText(prefix: String): String {
        return "$prefix: $mealCount meals, $goalPlanCount goals, $bodyMeasurementCount body logs."
    }
}

private fun MealWithFoods.toJson(): JSONObject {
    return JSONObject()
        .put("memo", meal.memo)
        .putNullable("imageUri", meal.imageUri)
        .putNullable("aiSummary", meal.aiSummary)
        .put("createdAt", meal.createdAt)
        .put("foods", JSONArray(foods.map { it.toJson() }))
}

private fun MealFoodEntity.toJson(): JSONObject {
    return JSONObject()
        .put("foodName", foodName)
        .put("description", description)
        .putNullable("imageUri", imageUri)
        .put("calories", calories)
        .put("carbsGram", carbsGram)
        .put("proteinGram", proteinGram)
        .put("fatGram", fatGram)
        .put("fiberGram", fiberGram)
        .put("sugarGram", sugarGram)
        .put("sodiumMilligram", sodiumMilligram)
        .putNullable("aiFoodName", aiFoodName)
        .putNullable("aiCalories", aiCalories)
        .putNullable("confidence", confidence)
}

private fun GoalPlanEntity.toJson(): JSONObject {
    return JSONObject()
        .put("validFromEpochDay", validFromEpochDay)
        .put("validToEpochDay", validToEpochDay)
        .put("startWeightKg", startWeightKg)
        .putNullable("startMuscleMassKg", startMuscleMassKg)
        .putNullable("startBodyFatPercent", startBodyFatPercent)
        .putNullable("targetWeightKg", targetWeightKg)
        .putNullable("targetMuscleMassKg", targetMuscleMassKg)
        .putNullable("targetBodyFatPercent", targetBodyFatPercent)
        .put("dailyCalories", dailyCalories)
        .put("dailyCarbsGram", dailyCarbsGram)
        .put("dailyProteinGram", dailyProteinGram)
        .put("dailyFatGram", dailyFatGram)
        .put("dailyFiberGram", dailyFiberGram)
        .put("dailySugarGram", dailySugarGram)
        .put("dailySodiumMilligram", dailySodiumMilligram)
        .put("planSummary", planSummary)
        .put("plannerVersion", plannerVersion)
        .put("createdAt", createdAt)
}

private fun BodyMeasurementEntity.toJson(): JSONObject {
    return JSONObject()
        .put("measuredEpochDay", measuredEpochDay)
        .putNullable("weightKg", weightKg)
        .putNullable("muscleMassKg", muscleMassKg)
        .putNullable("basalMetabolicRateKcal", basalMetabolicRateKcal)
        .putNullable("bodyFatMassKg", bodyFatMassKg)
        .putNullable("bodyFatPercent", bodyFatPercent)
        .put("createdAt", createdAt)
}

private fun JSONObject.toMealEntity(ownerId: Long): MealEntity {
    return MealEntity(
        ownerId = ownerId,
        memo = getString("memo"),
        imageUri = optNullableString("imageUri"),
        aiSummary = optNullableString("aiSummary"),
        createdAt = getLong("createdAt")
    )
}

private fun JSONObject.toMealFoodEntity(mealId: Long): MealFoodEntity {
    return MealFoodEntity(
        mealId = mealId,
        foodName = getString("foodName"),
        description = optString("description"),
        imageUri = optNullableString("imageUri"),
        calories = getInt("calories"),
        carbsGram = getInt("carbsGram"),
        proteinGram = getInt("proteinGram"),
        fatGram = getInt("fatGram"),
        fiberGram = optInt("fiberGram"),
        sugarGram = optInt("sugarGram"),
        sodiumMilligram = optInt("sodiumMilligram"),
        aiFoodName = optNullableString("aiFoodName"),
        aiCalories = optNullableInt("aiCalories"),
        confidence = optNullableFloat("confidence")
    )
}

private fun JSONObject.toGoalPlanEntity(ownerId: Long): GoalPlanEntity {
    return GoalPlanEntity(
        ownerId = ownerId,
        validFromEpochDay = getLong("validFromEpochDay"),
        validToEpochDay = getLong("validToEpochDay"),
        startWeightKg = getDouble("startWeightKg"),
        startMuscleMassKg = optNullableDouble("startMuscleMassKg"),
        startBodyFatPercent = optNullableDouble("startBodyFatPercent"),
        targetWeightKg = optNullableDouble("targetWeightKg"),
        targetMuscleMassKg = optNullableDouble("targetMuscleMassKg"),
        targetBodyFatPercent = optNullableDouble("targetBodyFatPercent"),
        dailyCalories = getInt("dailyCalories"),
        dailyCarbsGram = getInt("dailyCarbsGram"),
        dailyProteinGram = getInt("dailyProteinGram"),
        dailyFatGram = getInt("dailyFatGram"),
        dailyFiberGram = getInt("dailyFiberGram"),
        dailySugarGram = getInt("dailySugarGram"),
        dailySodiumMilligram = getInt("dailySodiumMilligram"),
        planSummary = getString("planSummary"),
        plannerVersion = getString("plannerVersion"),
        createdAt = getLong("createdAt")
    )
}

private fun JSONObject.toBodyMeasurementEntity(ownerId: Long): BodyMeasurementEntity {
    return BodyMeasurementEntity(
        ownerId = ownerId,
        measuredEpochDay = getLong("measuredEpochDay"),
        weightKg = optNullableDouble("weightKg"),
        muscleMassKg = optNullableDouble("muscleMassKg"),
        basalMetabolicRateKcal = optNullableInt("basalMetabolicRateKcal"),
        bodyFatMassKg = optNullableDouble("bodyFatMassKg"),
        bodyFatPercent = optNullableDouble("bodyFatPercent"),
        createdAt = getLong("createdAt")
    )
}

private fun JSONObject.putNullable(name: String, value: Any?): JSONObject {
    return put(name, value ?: JSONObject.NULL)
}

private fun JSONObject.optNullableString(name: String): String? {
    return if (has(name) && !isNull(name)) optString(name) else null
}

private fun JSONObject.optNullableInt(name: String): Int? {
    return if (has(name) && !isNull(name)) optInt(name) else null
}

private fun JSONObject.optNullableDouble(name: String): Double? {
    return if (has(name) && !isNull(name)) optDouble(name) else null
}

private fun JSONObject.optNullableFloat(name: String): Float? {
    return if (has(name) && !isNull(name)) optDouble(name).toFloat() else null
}

private fun JSONArray.forEachObject(action: (JSONObject) -> Unit) {
    for (index in 0 until length()) {
        action(getJSONObject(index))
    }
}

private fun <T> JSONArray.mapObjects(transform: (JSONObject) -> T): List<T> {
    return buildList {
        forEachObject { jsonObject ->
            add(transform(jsonObject))
        }
    }
}
