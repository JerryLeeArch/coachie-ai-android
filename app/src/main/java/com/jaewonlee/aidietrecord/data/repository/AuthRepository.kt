package com.jaewonlee.aidietrecord.data.repository

import com.jaewonlee.aidietrecord.data.local.AuthDao
import com.jaewonlee.aidietrecord.data.model.UserAccount
import java.security.MessageDigest

class AuthRepository(
    private val authDao: AuthDao
) {
    suspend fun login(userId: String, password: String): UserAccount? {
        val normalizedUserId = userId.trim()
        val user = authDao.getUserByUserId(normalizedUserId) ?: return null
        return user.takeIf { it.passwordHash == password.toPasswordHash() }
    }

    suspend fun register(
        userId: String,
        nickname: String,
        password: String
    ): Result<UserAccount> {
        val normalizedUserId = userId.trim()
        val normalizedNickname = nickname.trim()

        if (authDao.getUserByUserId(normalizedUserId) != null) {
            return Result.failure(IllegalArgumentException("이미 사용 중인 아이디입니다."))
        }

        val userAccount = UserAccount(
            userId = normalizedUserId,
            nickname = normalizedNickname,
            passwordHash = password.toPasswordHash(),
            createdAt = System.currentTimeMillis()
        )
        val createdId = authDao.insertUserAccount(userAccount)
        return Result.success(userAccount.copy(id = createdId))
    }

    suspend fun updateProfile(
        currentUser: UserAccount,
        userId: String,
        nickname: String,
        newPassword: String
    ): Result<UserAccount> {
        val normalizedUserId = userId.trim()
        val duplicatedUser = authDao.getUserByUserId(normalizedUserId)
        if (duplicatedUser != null && duplicatedUser.id != currentUser.id) {
            return Result.failure(IllegalArgumentException("이미 사용 중인 아이디입니다."))
        }

        val updatedUser = currentUser.copy(
            userId = normalizedUserId,
            nickname = nickname.trim(),
            passwordHash = newPassword
                .takeIf { it.isNotBlank() }
                ?.toPasswordHash()
                ?: currentUser.passwordHash
        )
        authDao.updateUserAccount(updatedUser)
        return Result.success(updatedUser)
    }
}

private fun String.toPasswordHash(): String {
    val digest = MessageDigest.getInstance("SHA-256")
    val bytes = digest.digest("AIDietRecord:$this".toByteArray())
    return bytes.joinToString(separator = "") { byte -> "%02x".format(byte) }
}
