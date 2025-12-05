package com.example.tarik_tambang

import android.content.Context

object UserPrefs {
    private const val PREFS_NAME = "app_prefs"
    private const val NAME_KEY = "username"
    private const val MUSIC_VOLUME_KEY = "music_volume"

    fun saveName(context: Context, name: String) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(NAME_KEY, name)
            .apply()
    }

    fun getName(context: Context): String? {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(NAME_KEY, null)
    }

    fun saveMusicVolume(context: Context, volume: Float) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putFloat(MUSIC_VOLUME_KEY, volume)
            .apply()
    }

    fun getMusicVolume(context: Context): Float {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getFloat(MUSIC_VOLUME_KEY, 0.5f) // Default volume 50%
    }
}