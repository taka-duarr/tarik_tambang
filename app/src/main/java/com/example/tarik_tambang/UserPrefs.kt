package com.example.tarik_tambang

import android.content.Context

object UserPrefs {
    private const val PREFS_NAME = "app_prefs"
    private const val NAME_KEY = "username"
    private const val WINS_KEY = "user_wins"
    private const val MUSIC_VOLUME_KEY = "music_volume"
    private const val SFX_VOLUME_KEY = "sfx_volume"
    private const val TOKEN_KEY = "jwt_token"

    fun saveToken(context: Context, token: String) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(TOKEN_KEY, token)
            .apply()
    }

    fun getToken(context: Context): String? {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(TOKEN_KEY, null)
    }

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

    fun saveWins(context: Context, wins: Int) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putInt(WINS_KEY, wins)
            .apply()
    }

    fun getWins(context: Context): Int {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getInt(WINS_KEY, 0)
    }

    fun incrementWins(context: Context) {
        val currentWins = getWins(context)
        saveWins(context, currentWins + 1)
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

    fun saveSfxVolume(context: Context, volume: Float) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putFloat(SFX_VOLUME_KEY, volume)
            .apply()
    }

    fun getSfxVolume(context: Context): Float {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getFloat(SFX_VOLUME_KEY, 0.5f) // Default volume 50%
    }

    fun clear(context: Context) {
        context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
            .edit()
            .clear()
            .apply()
    }

}