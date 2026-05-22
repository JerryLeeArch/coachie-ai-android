package com.jaewonlee.aidietrecord.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.jaewonlee.aidietrecord.data.model.UserAccount

@Dao
interface AuthDao {
    @Query("SELECT * FROM user_accounts WHERE id = :id LIMIT 1")
    suspend fun getUserById(id: Long): UserAccount?

    @Query("SELECT * FROM user_accounts WHERE userId = :userId LIMIT 1")
    suspend fun getUserByUserId(userId: String): UserAccount?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertUserAccount(userAccount: UserAccount): Long

    @Update
    suspend fun updateUserAccount(userAccount: UserAccount)
}
