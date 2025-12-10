package com.example.tarik_tambang

import android.app.Activity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.example.tarik_tambang.audio.AudioManager
import com.example.tarik_tambang.ui.screens.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)

        setContent {
            GameNavigation()
        }
    }

    override fun onResume() {
        super.onResume()
        AudioManager.startBackgroundMusic(this)
    }

    override fun onPause() {
        super.onPause()
        AudioManager.stopBackgroundMusic()
    }

    override fun onDestroy() {
        super.onDestroy()
        AudioManager.releaseBackgroundMusic()
        AudioManager.releaseSfx()
    }
}

enum class Screen {
    Login,
    Register,
    MainMenu,
    Lobby,
    Game,
    Leaderboard,
    Settings,
    Profile,
    Update
}

@Composable
fun GameNavigation() {
    val context = LocalContext.current

    DisposableEffect(Unit) {
        AudioManager.initializeSfx(context)
        AudioManager.startBackgroundMusic(context)
        onDispose { }
    }

    var currentScreen by remember { mutableStateOf(Screen.Login) }
    var savedName by remember { mutableStateOf(UserPrefs.getName(context)) }
    var userWins by remember { mutableStateOf(UserPrefs.getWins(context)) }
    var activeRoomCode by remember { mutableStateOf<String?>(null) }
    var activeRole by remember { mutableStateOf<String?>(null) }

    if (savedName != null && currentScreen == Screen.Login) {
        currentScreen = Screen.MainMenu
    }

    // State for double-press-to-exit
    var backPressedTime by remember { mutableStateOf(0L) }

    // Handle System Back Press
    BackHandler {
        when (currentScreen) {
            Screen.Lobby, Screen.Leaderboard, Screen.Settings, Screen.Profile -> {
                AudioManager.playSfx(R.raw.back_sfx)
                currentScreen = Screen.MainMenu
            }
            Screen.Game -> {
                AudioManager.playSfx(R.raw.quit_sfx) // Or back_sfx, depending on desired feel
                activeRoomCode = null
                activeRole = null
                currentScreen = Screen.Lobby
            }
            Screen.MainMenu, Screen.Login -> {
                if (backPressedTime + 2000 > System.currentTimeMillis()) {
                    (context as? Activity)?.finish()
                } else {
                    Toast.makeText(context, "Press back again to exit", Toast.LENGTH_SHORT).show()
                }
                backPressedTime = System.currentTimeMillis()
            }
            Screen.Register -> {
                AudioManager.playSfx(R.raw.back_sfx)
                currentScreen = Screen.Login
            }
            Screen.Update -> {
                AudioManager.playSfx(R.raw.back_sfx)
                currentScreen = Screen.Profile
            }

        }
    }

    when (currentScreen) {
        Screen.Login -> {
            LoginScreen(
                onLogin = { username ->
                    UserPrefs.saveName(context, username)
                    savedName = username
                    currentScreen = Screen.MainMenu
                },
                onRegister = {
                    currentScreen = Screen.Register
                }
            )
        }

        Screen.Register -> {
            RegisterScreen(
                onBackToLogin = {
                    currentScreen = Screen.Login
                }
            )
        }

        Screen.MainMenu -> {
            MainMenuScreen(
                onPlay = { 
                    AudioManager.playSfx(R.raw.button_click)
                    currentScreen = Screen.Lobby 
                },
                onLeaderboard = { 
                    AudioManager.playSfx(R.raw.button_click)
                    currentScreen = Screen.Leaderboard 
                },
                onSettings = { 
                    AudioManager.playSfx(R.raw.button_click)
                    currentScreen = Screen.Settings 
                },
                onProfile = {
                    AudioManager.playSfx(R.raw.button_click)
                    currentScreen = Screen.Profile
                }
            )
        }
        Screen.Profile -> {
            ProfileScreen(
                username = savedName ?: "",
                wins = userWins,
                onUpdateClick = {
                    currentScreen = Screen.Update
                },
                onLogout = {
                    UserPrefs.clear(context)
                    savedName = null
                    currentScreen = Screen.Login
                },
                onBack = {
                    currentScreen = Screen.MainMenu
                }
            )
        }

        Screen.Update -> {
            UpdateScreen(
                currentUsername = savedName ?: "",
                onUpdateSuccess = { newName ->
                    UserPrefs.saveName(context, newName)
                    savedName = newName
                    currentScreen = Screen.Profile
                },
                onBack = {
                    currentScreen = Screen.Profile
                }
            )
        }

        Screen.Lobby -> {
            LobbyScreen(
                fixedName = savedName!!,
                onJoinRoom = { code, role ->
                    AudioManager.playSfx(R.raw.button_click)
                    activeRoomCode = code
                    activeRole = role
                    currentScreen = Screen.Game
                },
                onProfileClick = {
                    AudioManager.playSfx(R.raw.button_click)
                    currentScreen = Screen.Profile
                },
                onBack = { 
                    AudioManager.playSfx(R.raw.back_sfx)
                    currentScreen = Screen.MainMenu 
                }
            )
        }
        Screen.Game -> {
            ActiveGameScreen(
                roomCode = activeRoomCode!!,
                myRole = activeRole!!,
                myName = savedName!!,
                onLeaveRoom = {
                    AudioManager.playSfx(R.raw.quit_sfx)
                    activeRoomCode = null
                    activeRole = null
                    currentScreen = Screen.Lobby
                }
            )
        }
        Screen.Leaderboard -> {
            LeaderboardScreen { 
                AudioManager.playSfx(R.raw.back_sfx)
                currentScreen = Screen.MainMenu 
            }
        }
        Screen.Settings -> {
            SettingsScreen { 
                AudioManager.playSfx(R.raw.back_sfx)
                currentScreen = Screen.MainMenu 
            }
        }
    }
}