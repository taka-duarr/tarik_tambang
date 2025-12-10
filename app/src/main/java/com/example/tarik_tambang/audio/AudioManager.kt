package com.example.tarik_tambang.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.SoundPool
import com.example.tarik_tambang.R
import com.example.tarik_tambang.UserPrefs

object AudioManager {

    private var backgroundMusicPlayer: MediaPlayer? = null
    private var soundPool: SoundPool? = null
    private val sfxMap = mutableMapOf<Int, Int>()
    private var sfxVolume = 1.0f

    // --- Background Music Controls ---

    fun startBackgroundMusic(context: Context, musicId: Int = R.raw.background_music) {
        val volume = UserPrefs.getMusicVolume(context)

        if (backgroundMusicPlayer == null) {
            try {
                // Buat instance baru jika belum ada
                backgroundMusicPlayer = MediaPlayer.create(context, musicId)?.apply {
                    isLooping = true // FIX: Pastikan musik selalu looping
                    setVolume(volume, volume)
                    start()
                }
                if (backgroundMusicPlayer == null) {
                    println("AudioManager ERROR: Gagal membuat MediaPlayer. Pastikan file di res/raw/background_music.mp3 tidak kosong/rusak.")
                }
            } catch (e: Exception) {
                println("AudioManager ERROR: ${e.message}")
                backgroundMusicPlayer = null
            }
        } else {
            // Jika sudah ada tapi tidak sedang bermain, lanjutkan
            if (backgroundMusicPlayer?.isPlaying == false) {
                backgroundMusicPlayer?.isLooping = true // FIX: Pastikan looping saat melanjutkan
                backgroundMusicPlayer?.start()
            }
        }
    }

    fun stopBackgroundMusic() {
        if (backgroundMusicPlayer?.isPlaying == true) {
            backgroundMusicPlayer?.pause() // Gunakan pause agar bisa dilanjutkan
        }
    }

    fun releaseBackgroundMusic() {
        backgroundMusicPlayer?.stop()
        backgroundMusicPlayer?.release()
        backgroundMusicPlayer = null
    }

    fun setMusicVolume(context: Context, volume: Float) {
        UserPrefs.saveMusicVolume(context, volume)
        try {
            backgroundMusicPlayer?.setVolume(volume, volume)
        } catch (e: IllegalStateException) {
             println("AudioManager WARN: MediaPlayer not ready to set volume.")
        }
    }

    // --- SFX Controls ---

    fun initializeSfx(context: Context) {
        if (soundPool != null) return // Sudah diinisialisasi

        sfxVolume = UserPrefs.getSfxVolume(context)

        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        soundPool = SoundPool.Builder().setMaxStreams(5).setAudioAttributes(audioAttributes).build()
        
        sfxMap[R.raw.button_click] = soundPool?.load(context, R.raw.button_click, 1) ?: 0
        sfxMap[R.raw.back_sfx] = soundPool?.load(context, R.raw.back_sfx, 1) ?: 0
        sfxMap[R.raw.quit_sfx] = soundPool?.load(context, R.raw.quit_sfx, 1) ?: 0
        sfxMap[R.raw.ready_sfx] = soundPool?.load(context, R.raw.ready_sfx, 1) ?: 0
    }

    fun playSfx(sfxId: Int) {
        sfxMap[sfxId]?.let { soundId ->
            soundPool?.play(soundId, sfxVolume, sfxVolume, 1, 0, 1.0f)
        }
    }

    fun setSfxVolume(context: Context, volume: Float) {
        sfxVolume = volume
        UserPrefs.saveSfxVolume(context, volume)
        // Optionally, play a sample sound to preview the new volume
        playSfx(R.raw.button_click)
    }

    fun releaseSfx() {
        soundPool?.release()
        soundPool = null
        sfxMap.clear()
    }
}