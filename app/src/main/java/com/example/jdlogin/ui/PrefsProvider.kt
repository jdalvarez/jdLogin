package com.example.jdlogin.ui

import android.content.Context

object PrefsProvider {

    private const val PREF_FILE = "prefs"
    private const val PREF_EMAIL = "email"
    private const val PREF_PROVIDER = "provider"

    fun saveData(email: String, context: Context, providerType: String) {
        context.getSharedPreferences(PREF_FILE, Context.MODE_PRIVATE)
            .edit()
            .putString(PREF_EMAIL, email)
            .putString(PREF_PROVIDER, providerType)
            .apply()
    }

    fun readEmail(context: Context): String? =
        context.getSharedPreferences(PREF_FILE, Context.MODE_PRIVATE)
            .getString(PREF_EMAIL, null)

    fun readAuthProvider(context: Context): String? =
        context.getSharedPreferences(PREF_FILE, Context.MODE_PRIVATE)
            .getString(PREF_PROVIDER, null)



    fun clear(context: Context) {
        context.getSharedPreferences(PREF_FILE, Context.MODE_PRIVATE)
            .edit()
            .clear()
            .apply()
    }
}