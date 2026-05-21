package com.jaewonlee.aidietrecord.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "user_accounts",
    indices = [Index(value = ["userId"], unique = true)]
)
data class UserAccount(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: String,
    val nickname: String,
    val passwordHash: String,
    val createdAt: Long
)
