package com.example.tarik_tambang

import android.content.Context

object UserPrefs {
    private const val NAME_KEY = "username"

    fun saveName(context: Context, name: String) {
        context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
            .edit()
            .putString(NAME_KEY, name)
            .apply()
    }

    fun getName(context: Context): String? {
        return context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
            .getString(NAME_KEY, null)
    }
}