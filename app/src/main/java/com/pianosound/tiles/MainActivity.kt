package com.pianosound.tiles

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.pianosound.tiles.model.LevelData
import com.pianosound.tiles.ui.screens.*
import com.pianosound.tiles.ui.theme.DeepBackground
import com.pianosound.tiles.ui.theme.PianoSoundTilesTheme

enum class AppScreen {
    MAIN_MENU,
    CLASSIC_SONGS,
    LEVEL_EDITOR,
    CUSTOM_LEVELS,
    GAMEPLAY
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PianoSoundTilesTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = DeepBackground
                ) {
                    var currentScreen by remember { mutableStateOf(AppScreen.MAIN_MENU) }
                    var activeLevel by remember { mutableStateOf<LevelData?>(null) }
                    var gameSessionId by remember { mutableIntStateOf(0) }

                    // Custom Back Handler
                    BackHandler(enabled = currentScreen != AppScreen.MAIN_MENU) {
                        currentScreen = AppScreen.MAIN_MENU
                    }

                    when (currentScreen) {
                        AppScreen.MAIN_MENU -> {
                            MainMenuScreen(
                                onPlayClassics = { currentScreen = AppScreen.CLASSIC_SONGS },
                                onCreateLevel = { currentScreen = AppScreen.LEVEL_EDITOR },
                                onCustomLevels = { currentScreen = AppScreen.CUSTOM_LEVELS }
                            )
                        }

                        AppScreen.CLASSIC_SONGS -> {
                            ClassicSongsScreen(
                                onBack = { currentScreen = AppScreen.MAIN_MENU },
                                onPlayLevel = { level ->
                                    activeLevel = level
                                    gameSessionId++
                                    currentScreen = AppScreen.GAMEPLAY
                                }
                            )
                        }

                        AppScreen.LEVEL_EDITOR -> {
                            LevelEditorScreen(
                                onBack = { currentScreen = AppScreen.MAIN_MENU },
                                onLevelSaved = { savedLevel ->
                                    activeLevel = savedLevel
                                    gameSessionId++
                                    currentScreen = AppScreen.GAMEPLAY
                                }
                            )
                        }

                        AppScreen.CUSTOM_LEVELS -> {
                            CustomLevelsScreen(
                                onBack = { currentScreen = AppScreen.MAIN_MENU },
                                onCreateNew = { currentScreen = AppScreen.LEVEL_EDITOR },
                                onPlayLevel = { level ->
                                    activeLevel = level
                                    gameSessionId++
                                    currentScreen = AppScreen.GAMEPLAY
                                }
                            )
                        }

                        AppScreen.GAMEPLAY -> {
                            activeLevel?.let { level ->
                                key(gameSessionId) {
                                    GameScreen(
                                        level = level,
                                        onExitGame = { currentScreen = AppScreen.MAIN_MENU },
                                        onRestart = { gameSessionId++ }
                                    )
                                }
                            } ?: run {
                                currentScreen = AppScreen.MAIN_MENU
                            }
                        }
                    }
                }
            }
        }
    }
}
