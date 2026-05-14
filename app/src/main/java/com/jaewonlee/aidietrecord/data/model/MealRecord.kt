package com.jaewonlee.aidietrecord.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "meal_records")
data class MealRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val foodName: String,
    val calories: Int,
    val memo: String,
    val imageUri: String?,
    val aiFoodName: String?,
    val aiCalories: Int?,
    val createdAt: Long
)
