package com.jaewonlee.aidietrecord.data.local

import android.content.Context

class AuthSessionStore(context: Context) {
    private val preferences = context.applicationContext.getSharedPreferences(
        PREFERENCES_NAME,
        Context.MODE_PRIVATE
    )

    fun getSavedUserId(): Long {
        return preferences.getLong(KEY_CURRENT_USER_ID, 0L)
    }

    fun saveUserId(userId: Long) {
        preferences.edit()
            .putLong(KEY_CURRENT_USER_ID, userId)
            .apply()
    }

    fun clear() {
        preferences.edit()
            .remove(KEY_CURRENT_USER_ID)
            .apply()
    }

    private companion object {
        const val PREFERENCES_NAME = "ai_diet_auth_session"
        const val KEY_CURRENT_USER_ID = "current_user_id"
    }
}
