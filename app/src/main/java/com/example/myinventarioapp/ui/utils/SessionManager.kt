package com.example.myinventarioapp.ui.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit

class SessionManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("user_session", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_USER_NAME = "user_name"
        private const val KEY_USER_EMAIL = "user_email"
        private const val KEY_IS_REMEMBERED = "is_remembered"
    }

    fun saveSession(name: String, email: String) {
        prefs.edit {
            putString(KEY_USER_NAME, name)
            putString(KEY_USER_EMAIL, email)
            putBoolean(KEY_IS_REMEMBERED, true)
        }
    }

    fun getUserName(): String? = prefs.getString(KEY_USER_NAME, null)
    fun getUserEmail(): String? = prefs.getString(KEY_USER_EMAIL, null)
    fun isUserRemembered(): Boolean = prefs.getBoolean(KEY_IS_REMEMBERED, false)

    fun clearSession() {
        prefs.edit { clear() }
    }
}