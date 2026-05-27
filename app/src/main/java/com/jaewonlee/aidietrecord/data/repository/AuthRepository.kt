package com.jaewonlee.aidietrecord.data.repository

import com.jaewonlee.aidietrecord.data.local.AuthDao
import com.jaewonlee.aidietrecord.data.model.UserAccount
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlinx.coroutines.suspendCancellableCoroutine

class AuthRepository(
    private val authDao: AuthDao,
    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
) {
    suspend fun getUserById(id: Long): UserAccount? {
        return authDao.getUserById(id)
    }

    suspend fun getSignedInUser(): UserAccount? {
        return firebaseAuth.currentUser?.let { firebaseUser ->
            syncLocalAccount(firebaseUser)
        }
    }

    suspend fun login(userId: String, password: String): Result<UserAccount> {
        val email = userId.trim()
        return runCatchingAuth {
            val authResult = firebaseAuth.signInWithEmailAndPassword(email, password).await()
            val firebaseUser = authResult.user
                ?: error("Unable to load signed-in user.")
            syncLocalAccount(firebaseUser)
        }
    }

    suspend fun register(
        userId: String,
        nickname: String,
        password: String
    ): Result<UserAccount> {
        val email = userId.trim()
        val normalizedNickname = nickname.trim()
        return runCatchingAuth {
            val authResult = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
            val firebaseUser = authResult.user
                ?: error("Unable to load created user.")
            firebaseUser.updateProfile(
                UserProfileChangeRequest.Builder()
                    .setDisplayName(normalizedNickname)
                    .build()
            ).await()
            syncLocalAccount(firebaseUser, fallbackNickname = normalizedNickname)
        }
    }

    suspend fun updateProfile(
        currentUser: UserAccount,
        userId: String,
        nickname: String,
        newPassword: String
    ): Result<UserAccount> {
        val firebaseUser = firebaseAuth.currentUser
            ?: return Result.failure(IllegalStateException("Log in again to update your profile."))
        val email = userId.trim()
        val normalizedNickname = nickname.trim()
        return runCatchingAuth {
            if (firebaseUser.displayName != normalizedNickname) {
                firebaseUser.updateProfile(
                    UserProfileChangeRequest.Builder()
                        .setDisplayName(normalizedNickname)
                        .build()
                ).await()
            }
            if (!firebaseUser.email.equals(email, ignoreCase = true)) {
                firebaseUser.updateEmail(email).await()
            }
            if (newPassword.isNotBlank()) {
                firebaseUser.updatePassword(newPassword).await()
            }

            val updatedUser = currentUser.copy(
                firebaseUid = firebaseUser.uid,
                userId = email,
                nickname = normalizedNickname
            )
            authDao.updateUserAccount(updatedUser)
            updatedUser
        }
    }

    suspend fun sendPasswordResetEmail(userId: String): Result<Unit> {
        val email = userId.trim()
        return runCatchingAuth {
            firebaseAuth.sendPasswordResetEmail(email).await()
        }
    }

    fun logout() {
        firebaseAuth.signOut()
    }

    private suspend fun syncLocalAccount(
        firebaseUser: FirebaseUser,
        fallbackNickname: String? = null
    ): UserAccount {
        val email = firebaseUser.email.orEmpty()
        val nickname = firebaseUser.displayName
            ?.takeIf { it.isNotBlank() }
            ?: fallbackNickname
            ?: email.substringBefore("@").ifBlank { "User" }
        val existingByUid = authDao.getUserByFirebaseUid(firebaseUser.uid)
        if (existingByUid != null) {
            val updatedUser = existingByUid.copy(
                userId = email.ifBlank { existingByUid.userId },
                nickname = nickname
            )
            if (updatedUser != existingByUid) {
                authDao.updateUserAccount(updatedUser)
            }
            return updatedUser
        }

        val existingByEmail = email
            .takeIf { it.isNotBlank() }
            ?.let { authDao.getUserByUserId(it) }
        if (existingByEmail != null) {
            val linkedUser = existingByEmail.copy(
                firebaseUid = firebaseUser.uid,
                nickname = nickname
            )
            authDao.updateUserAccount(linkedUser)
            return linkedUser
        }

        val userAccount = UserAccount(
            firebaseUid = firebaseUser.uid,
            userId = email,
            nickname = nickname,
            createdAt = System.currentTimeMillis()
        )
        val createdId = authDao.insertUserAccount(userAccount)
        return userAccount.copy(id = createdId)
    }
}

private suspend fun <T> runCatchingAuth(block: suspend () -> T): Result<T> {
    return runCatching {
        block()
    }.mapError { throwable ->
        when (throwable) {
            is FirebaseAuthRecentLoginRequiredException -> {
                IllegalStateException("Log in again before changing email or password.")
            }
            else -> throwable
        }
    }
}

private inline fun <T> Result<T>.mapError(transform: (Throwable) -> Throwable): Result<T> {
    return fold(
        onSuccess = { Result.success(it) },
        onFailure = { Result.failure(transform(it)) }
    )
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
