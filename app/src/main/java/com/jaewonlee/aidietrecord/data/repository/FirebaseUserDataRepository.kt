package com.jaewonlee.aidietrecord.data.repository

import androidx.room.withTransaction
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.jaewonlee.aidietrecord.data.local.MealDatabase
import com.jaewonlee.aidietrecord.data.model.BodyMeasurementEntity
import com.jaewonlee.aidietrecord.data.model.GoalPlanEntity
import com.jaewonlee.aidietrecord.data.model.MealEntity
import com.jaewonlee.aidietrecord.data.model.MealFoodEntity
import com.jaewonlee.aidietrecord.data.model.MealWithFoods
import com.jaewonlee.aidietrecord.data.model.UserAccount
import com.jaewonlee.aidietrecord.data.model.currentTimeZoneId
import com.jaewonlee.aidietrecord.data.model.localDateEpochDay
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class FirebaseUserDataRepository(
    private val mealDatabase: MealDatabase,
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    private val mealDao = mealDatabase.mealDao()
    private val goalDao = mealDatabase.goalDao()
    private val uploadMutex = Mutex()

    suspend fun downloadUserDataSnapshot(userAccount: UserAccount): FirebaseUploadSummary? {
        return uploadMutex.withLock {
            val firebaseUid = userAccount.firebaseUid
                ?: error("Log in again before syncing data from Firebase.")
            val snapshot = firestore.collection(UsersCollection)
                .document(firebaseUid)
                .collection(DataCollection)
                .document(CurrentSnapshotDocument)
                .get()
                .await()
            if (!snapshot.exists()) return@withLock null

            val snapshotData = snapshot.data.orEmpty()
            val meals = snapshotData.mapList("meals")
            val goalPlans = snapshotData.mapList("goalPlans")
            val bodyMeasurements = snapshotData.mapList("bodyMeasurements")

            mealDatabase.withTransaction {
                mealDao.deleteMealsForOwner(userAccount.id)
                goalDao.deleteGoalPlansForOwner(userAccount.id)
                goalDao.deleteBodyMeasurementsForOwner(userAccount.id)

                meals.forEach { mealData ->
                    val insertedMealId = mealDao.insertMeal(mealData.toMealEntity(userAccount.id))
                    val foods = mealData.mapList("foods")
                        .map { foodData -> foodData.toMealFoodEntity(insertedMealId) }
                    if (foods.isNotEmpty()) {
                        mealDao.insertMealFoods(foods)
                    }
                }
                goalPlans.forEach { goalPlanData ->
                    goalDao.insertGoalPlan(goalPlanData.toGoalPlanEntity(userAccount.id))
                }
                bodyMeasurements.forEach { measurementData ->
                    goalDao.insertBodyMeasurement(measurementData.toBodyMeasurementEntity(userAccount.id))
                }
            }

            FirebaseUploadSummary(
                mealCount = meals.size,
                goalPlanCount = goalPlans.size,
                bodyMeasurementCount = bodyMeasurements.size
            )
        }
    }

    suspend fun uploadUserDataSnapshot(userAccount: UserAccount): FirebaseUploadSummary {
        return uploadMutex.withLock {
            val firebaseUid = userAccount.firebaseUid
                ?: error("Log in again before syncing data to Firebase.")
            val meals = mealDao.getMealRecords(userAccount.id)
            val goalPlans = goalDao.getGoalPlans(userAccount.id)
            val bodyMeasurements = goalDao.getBodyMeasurements(userAccount.id)

            val userDocument = mapOf(
                "firebaseUid" to firebaseUid,
                "email" to userAccount.userId,
                "nickname" to userAccount.nickname,
                "localUserId" to userAccount.id,
                "createdAt" to userAccount.createdAt,
                "updatedAt" to FieldValue.serverTimestamp()
            )
            val snapshotDocument = mapOf(
                "format" to FirebaseDataFormat,
                "schemaVersion" to SchemaVersion,
                "syncedAt" to FieldValue.serverTimestamp(),
                "localOwnerId" to userAccount.id,
                "user" to mapOf(
                    "firebaseUid" to firebaseUid,
                    "email" to userAccount.userId,
                    "nickname" to userAccount.nickname
                ),
                "counts" to mapOf(
                    "meals" to meals.size,
                    "goalPlans" to goalPlans.size,
                    "bodyMeasurements" to bodyMeasurements.size
                ),
                "meals" to meals.map { it.toFirebaseMap() },
                "goalPlans" to goalPlans.map { it.toFirebaseMap() },
                "bodyMeasurements" to bodyMeasurements.map { it.toFirebaseMap() }
            )

            val userRef = firestore.collection(UsersCollection).document(firebaseUid)
            userRef.set(userDocument, SetOptions.merge()).await()
            userRef.collection(DataCollection).document(CurrentSnapshotDocument)
                .set(snapshotDocument)
                .await()

            FirebaseUploadSummary(
                mealCount = meals.size,
                goalPlanCount = goalPlans.size,
                bodyMeasurementCount = bodyMeasurements.size
            )
        }
    }

    companion object {
        private const val UsersCollection = "users"
        private const val DataCollection = "data"
        private const val CurrentSnapshotDocument = "current"
        private const val FirebaseDataFormat = "ai-diet-record-user-data"
        private const val SchemaVersion = 1
    }
}

data class FirebaseUploadSummary(
    val mealCount: Int,
    val goalPlanCount: Int,
    val bodyMeasurementCount: Int
)

private fun MealWithFoods.toFirebaseMap(): Map<String, Any?> {
    return mapOf(
        "localId" to meal.id,
        "memo" to meal.memo,
        "imageUri" to meal.imageUri,
        "aiSummary" to meal.aiSummary,
        "createdAt" to meal.createdAt,
        "timeZoneId" to meal.timeZoneId,
        "localDateEpochDay" to meal.localDateEpochDay,
        "foods" to foods
            .sortedBy { it.id }
            .map { it.toFirebaseMap() }
    )
}

private fun MealFoodEntity.toFirebaseMap(): Map<String, Any?> {
    return mapOf(
        "localId" to id,
        "foodName" to foodName,
        "description" to description,
        "imageUri" to imageUri,
        "calories" to calories,
        "carbsGram" to carbsGram,
        "proteinGram" to proteinGram,
        "fatGram" to fatGram,
        "fiberGram" to fiberGram,
        "sugarGram" to sugarGram,
        "sodiumMilligram" to sodiumMilligram,
        "aiFoodName" to aiFoodName,
        "aiCalories" to aiCalories,
        "confidence" to confidence?.toDouble()
    )
}

private fun GoalPlanEntity.toFirebaseMap(): Map<String, Any?> {
    return mapOf(
        "localId" to id,
        "validFromEpochDay" to validFromEpochDay,
        "validToEpochDay" to validToEpochDay,
        "startWeightKg" to startWeightKg,
        "startMuscleMassKg" to startMuscleMassKg,
        "startBodyFatPercent" to startBodyFatPercent,
        "targetWeightKg" to targetWeightKg,
        "targetMuscleMassKg" to targetMuscleMassKg,
        "targetBodyFatPercent" to targetBodyFatPercent,
        "dailyCalories" to dailyCalories,
        "dailyCarbsGram" to dailyCarbsGram,
        "dailyProteinGram" to dailyProteinGram,
        "dailyFatGram" to dailyFatGram,
        "dailyFiberGram" to dailyFiberGram,
        "dailySugarGram" to dailySugarGram,
        "dailySodiumMilligram" to dailySodiumMilligram,
        "planSummary" to planSummary,
        "plannerVersion" to plannerVersion,
        "createdAt" to createdAt
    )
}

private fun BodyMeasurementEntity.toFirebaseMap(): Map<String, Any?> {
    return mapOf(
        "localId" to id,
        "measuredEpochDay" to measuredEpochDay,
        "weightKg" to weightKg,
        "muscleMassKg" to muscleMassKg,
        "basalMetabolicRateKcal" to basalMetabolicRateKcal,
        "bodyFatMassKg" to bodyFatMassKg,
        "bodyFatPercent" to bodyFatPercent,
        "createdAt" to createdAt
    )
}

private fun Map<String, Any?>.toMealEntity(ownerId: Long): MealEntity {
    val createdAt = longValue("createdAt")
    val timeZoneId = stringValue("timeZoneId").ifBlank { currentTimeZoneId() }
    return MealEntity(
        ownerId = ownerId,
        memo = stringValue("memo"),
        imageUri = nullableStringValue("imageUri"),
        aiSummary = nullableStringValue("aiSummary"),
        createdAt = createdAt,
        timeZoneId = timeZoneId,
        localDateEpochDay = nullableLongValue("localDateEpochDay")
            ?: localDateEpochDay(createdAt, timeZoneId)
    )
}

private fun Map<String, Any?>.toMealFoodEntity(mealId: Long): MealFoodEntity {
    return MealFoodEntity(
        mealId = mealId,
        foodName = stringValue("foodName"),
        description = stringValue("description"),
        imageUri = nullableStringValue("imageUri"),
        calories = intValue("calories"),
        carbsGram = intValue("carbsGram"),
        proteinGram = intValue("proteinGram"),
        fatGram = intValue("fatGram"),
        fiberGram = intValue("fiberGram"),
        sugarGram = intValue("sugarGram"),
        sodiumMilligram = intValue("sodiumMilligram"),
        aiFoodName = nullableStringValue("aiFoodName"),
        aiCalories = nullableIntValue("aiCalories"),
        confidence = nullableFloatValue("confidence")
    )
}

private fun Map<String, Any?>.toGoalPlanEntity(ownerId: Long): GoalPlanEntity {
    return GoalPlanEntity(
        ownerId = ownerId,
        validFromEpochDay = longValue("validFromEpochDay"),
        validToEpochDay = longValue("validToEpochDay"),
        startWeightKg = doubleValue("startWeightKg"),
        startMuscleMassKg = nullableDoubleValue("startMuscleMassKg"),
        startBodyFatPercent = nullableDoubleValue("startBodyFatPercent"),
        targetWeightKg = nullableDoubleValue("targetWeightKg"),
        targetMuscleMassKg = nullableDoubleValue("targetMuscleMassKg"),
        targetBodyFatPercent = nullableDoubleValue("targetBodyFatPercent"),
        dailyCalories = intValue("dailyCalories"),
        dailyCarbsGram = intValue("dailyCarbsGram"),
        dailyProteinGram = intValue("dailyProteinGram"),
        dailyFatGram = intValue("dailyFatGram"),
        dailyFiberGram = intValue("dailyFiberGram"),
        dailySugarGram = intValue("dailySugarGram"),
        dailySodiumMilligram = intValue("dailySodiumMilligram"),
        planSummary = stringValue("planSummary"),
        plannerVersion = stringValue("plannerVersion"),
        createdAt = longValue("createdAt")
    )
}

private fun Map<String, Any?>.toBodyMeasurementEntity(ownerId: Long): BodyMeasurementEntity {
    return BodyMeasurementEntity(
        ownerId = ownerId,
        measuredEpochDay = longValue("measuredEpochDay"),
        weightKg = nullableDoubleValue("weightKg"),
        muscleMassKg = nullableDoubleValue("muscleMassKg"),
        basalMetabolicRateKcal = nullableIntValue("basalMetabolicRateKcal"),
        bodyFatMassKg = nullableDoubleValue("bodyFatMassKg"),
        bodyFatPercent = nullableDoubleValue("bodyFatPercent"),
        createdAt = longValue("createdAt")
    )
}

private fun Map<String, Any?>.mapList(name: String): List<Map<String, Any?>> {
    return (this[name] as? List<*>)
        .orEmpty()
        .mapNotNull { value -> value as? Map<*, *> }
        .map { map -> map.toStringKeyMap() }
}

private fun Map<*, *>.toStringKeyMap(): Map<String, Any?> {
    return entries.associate { (key, value) -> key.toString() to value }
}

private fun Map<String, Any?>.stringValue(name: String): String {
    return nullableStringValue(name).orEmpty()
}

private fun Map<String, Any?>.nullableStringValue(name: String): String? {
    return this[name] as? String
}

private fun Map<String, Any?>.intValue(name: String): Int {
    return nullableIntValue(name) ?: 0
}

private fun Map<String, Any?>.nullableIntValue(name: String): Int? {
    return (this[name] as? Number)?.toInt()
}

private fun Map<String, Any?>.longValue(name: String): Long {
    return nullableLongValue(name) ?: 0L
}

private fun Map<String, Any?>.nullableLongValue(name: String): Long? {
    return (this[name] as? Number)?.toLong()
}

private fun Map<String, Any?>.doubleValue(name: String): Double {
    return nullableDoubleValue(name) ?: 0.0
}

private fun Map<String, Any?>.nullableDoubleValue(name: String): Double? {
    return (this[name] as? Number)?.toDouble()
}

private fun Map<String, Any?>.nullableFloatValue(name: String): Float? {
    return (this[name] as? Number)?.toFloat()
}

private suspend fun <T> Task<T>.await(): T {
    return suspendCancellableCoroutine { continuation ->
        addOnSuccessListener { result ->
            continuation.resume(result)
        }
        addOnFailureListener { exception ->
            continuation.resumeWithException(exception)
        }
        addOnCanceledListener {
            continuation.cancel()
        }
    }
}
