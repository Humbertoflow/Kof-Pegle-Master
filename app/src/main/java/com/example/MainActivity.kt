package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.credentials.CredentialManager
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.firebase.auth.GoogleAuthHelper
import com.example.firebase.model.ScoreRecord
import com.example.firebase.repository.KofPeggleRepository
import com.example.firebase.ui.AuthGateScreen
import com.example.peggle.data.GamePreferences
import com.example.peggle.data.LevelCatalog
import com.example.peggle.model.GameMode
import com.example.peggle.ui.components.HowToPlayDialog
import com.example.peggle.ui.screens.CampaignMapScreen
import com.example.peggle.ui.screens.GamePlayScreen
import com.example.peggle.ui.screens.LevelEditorScreen
import com.example.peggle.ui.screens.MainMenuScreen
import com.example.peggle.ui.screens.MasterSelectScreen
import com.example.peggle.ui.screens.StageSelectScreen
import com.example.peggle.viewmodel.PeggleViewModel
import com.example.ui.theme.MyApplicationTheme
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import kotlinx.coroutines.launch

enum class AppScreen {
    MAIN_MENU,
    CAMPAIGN_MAP,
    STAGE_SELECT,
    MASTER_SELECT,
    GAME_PLAY,
    LEVEL_EDITOR
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    PeggleApp()
                }
            }
        }
    }
}

@Composable
fun PeggleApp() {
    var currentUser by remember { mutableStateOf(Firebase.auth.currentUser) }

    DisposableEffect(Unit) {
        val listener = FirebaseAuth.AuthStateListener { auth ->
            currentUser = auth.currentUser
        }
        Firebase.auth.addAuthStateListener(listener)
        onDispose {
            Firebase.auth.removeAuthStateListener(listener)
        }
    }

    if (currentUser == null) {
        AuthGateScreen(
            onAuthSuccess = {
                currentUser = Firebase.auth.currentUser
            }
        )
    } else {
        MainGameApp(
            userId = currentUser!!.uid,
            userDisplayName = currentUser?.displayName ?: "Luchador KOF"
        )
    }
}

@Composable
fun MainGameApp(
    userId: String,
    userDisplayName: String,
    viewModel: PeggleViewModel = viewModel()
) {
    var currentScreen by remember { mutableStateOf(AppScreen.MAIN_MENU) }
    var showHowToPlay by remember { mutableStateOf(false) }
    val uiState by viewModel.uiState.collectAsState()

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val credentialManager = remember { CredentialManager.create(context) }
    val activity = context as? android.app.Activity
    val preferences = remember { GamePreferences(context) }
    val repository = remember { KofPeggleRepository(context) }

    // Inicializar o crear perfil de usuario en Firestore
    LaunchedEffect(userId) {
        try {
            repository.getOrCreateProfile(userDisplayName)
        } catch (_: Exception) {}
    }

    // Sincronizar victoria o récord cuando showVictoryDialog se activa
    LaunchedEffect(uiState.showVictoryDialog) {
        if (uiState.showVictoryDialog && uiState.currentScore > 0) {
            coroutineScope.launch {
                try {
                    repository.saveScoreRecord(
                        ScoreRecord(
                            userId = userId,
                            recordId = "rec_${System.currentTimeMillis()}",
                            stageId = uiState.levelData.id,
                            stageTitle = uiState.levelData.title,
                            score = uiState.currentScore,
                            masterUsed = uiState.selectedMaster.name,
                            cleared = uiState.isMasterClear
                        )
                    )
                    repository.updateProgress(
                        score = uiState.currentScore,
                        stageNumber = uiState.levelData.stageNumber,
                        pegsHit = 25 - uiState.orangePegsRemaining,
                        feverCount = 1
                    )
                } catch (_: Exception) {}
            }
        }
    }

    val toggleOrientation = {
        activity?.let { act ->
            val isLandscape = act.resources.configuration.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE
            act.requestedOrientation = if (isLandscape) {
                android.content.pm.ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            } else {
                android.content.pm.ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
            }
        }
    }

    when (currentScreen) {
        AppScreen.MAIN_MENU -> {
            MainMenuScreen(
                soundEnabled = uiState.soundEnabled,
                hapticsEnabled = uiState.hapticsEnabled,
                userDisplayName = userDisplayName,
                onSignOut = {
                    GoogleAuthHelper.signOut(
                        context = context,
                        credentialManager = credentialManager,
                        onSignOutComplete = {
                            // Auth listener en PeggleApp manejará el cambio
                        },
                        scope = coroutineScope
                    )
                },
                onStartAdventure = {
                    viewModel.setGameMode(GameMode.ADVENTURE)
                    val firstLevel = LevelCatalog.getAllLevels().first()
                    viewModel.loadLevel(firstLevel, firstLevel.master)
                    currentScreen = AppScreen.GAME_PLAY
                },
                onOpenMap = {
                    currentScreen = AppScreen.CAMPAIGN_MAP
                },
                onQuickPlay = {
                    currentScreen = AppScreen.STAGE_SELECT
                },
                onSelectMasters = {
                    currentScreen = AppScreen.MASTER_SELECT
                },
                onStartDuel = {
                    viewModel.setGameMode(GameMode.DUEL)
                    val duelLevel = LevelCatalog.getAllLevels().first()
                    viewModel.loadLevel(duelLevel, duelLevel.master)
                    currentScreen = AppScreen.GAME_PLAY
                },
                onOpenEditor = {
                    currentScreen = AppScreen.LEVEL_EDITOR
                },
                onOpenHowToPlay = { showHowToPlay = true },
                onToggleSound = { viewModel.toggleSound() },
                onToggleOrientation = { toggleOrientation() }
            )
        }

        AppScreen.CAMPAIGN_MAP -> {
            BackHandler { currentScreen = AppScreen.MAIN_MENU }
            CampaignMapScreen(
                onSelectLevel = { level ->
                    viewModel.setGameMode(GameMode.ADVENTURE)
                    viewModel.loadLevel(level, level.master)
                    currentScreen = AppScreen.GAME_PLAY
                },
                onBack = { currentScreen = AppScreen.MAIN_MENU },
                getHighScore = { preferences.getLevelHighScore(it) },
                getStars = { preferences.getLevelStars(it) },
                isCleared = { preferences.isLevelCleared(it) },
                isMasterClear = { preferences.isMasterClear(it) }
            )
        }

        AppScreen.STAGE_SELECT -> {
            BackHandler { currentScreen = AppScreen.MAIN_MENU }
            StageSelectScreen(
                onSelectLevel = { level ->
                    viewModel.setGameMode(GameMode.QUICK_PLAY)
                    viewModel.loadLevel(level, level.master)
                    currentScreen = AppScreen.GAME_PLAY
                },
                onBack = { currentScreen = AppScreen.MAIN_MENU },
                onOpenMap = { currentScreen = AppScreen.CAMPAIGN_MAP },
                onOpenEditor = { currentScreen = AppScreen.LEVEL_EDITOR },
                getHighScore = { preferences.getLevelHighScore(it) },
                getStars = { preferences.getLevelStars(it) },
                isCleared = { preferences.isLevelCleared(it) },
                isMasterClear = { preferences.isMasterClear(it) }
            )
        }

        AppScreen.MASTER_SELECT -> {
            BackHandler { currentScreen = AppScreen.MAIN_MENU }
            MasterSelectScreen(
                currentMaster = uiState.selectedMaster,
                onSelectMaster = { master ->
                    viewModel.selectMaster(master)
                    currentScreen = AppScreen.MAIN_MENU
                },
                onBack = { currentScreen = AppScreen.MAIN_MENU }
            )
        }

        AppScreen.GAME_PLAY -> {
            BackHandler { currentScreen = AppScreen.MAIN_MENU }
            GamePlayScreen(
                viewModel = viewModel,
                onNavigateHome = {
                    viewModel.soundManager.stopFeverMelody()
                    viewModel.audioEngine.stopFeverMelody()
                    currentScreen = AppScreen.MAIN_MENU
                },
                onNavigateToMap = {
                    viewModel.soundManager.stopFeverMelody()
                    viewModel.audioEngine.stopFeverMelody()
                    currentScreen = AppScreen.CAMPAIGN_MAP
                }
            )
        }

        AppScreen.LEVEL_EDITOR -> {
            BackHandler { currentScreen = AppScreen.MAIN_MENU }
            LevelEditorScreen(
                levelManager = viewModel.levelManager,
                onBack = { currentScreen = AppScreen.MAIN_MENU },
                onPlayCustomLevel = { customLevel ->
                    viewModel.setGameMode(GameMode.QUICK_PLAY)
                    viewModel.loadLevel(customLevel, customLevel.master)
                    currentScreen = AppScreen.GAME_PLAY
                }
            )
        }
    }

    if (showHowToPlay) {
        HowToPlayDialog(onDismiss = { showHowToPlay = false })
    }
}
