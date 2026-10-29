package com.pianosound.tiles.ui.screens

import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pianosound.tiles.model.GameScore
import com.pianosound.tiles.model.LevelData
import com.pianosound.tiles.model.Tile
import com.pianosound.tiles.ui.theme.*
import kotlinx.coroutines.delay
import java.io.File
import kotlin.math.abs

@Composable
fun GameScreen(
    level: LevelData,
    onExitGame: () -> Unit,
    onRestart: () -> Unit
) {
    val context = LocalContext.current
    var mediaPlayer by remember { mutableStateOf<MediaPlayer?>(null) }
    var isPlaying by remember { mutableStateOf(false) }
    var isPaused by remember { mutableStateOf(false) }
    var isGameOver by remember { mutableStateOf(false) }

    var audioTimeMs by remember { mutableLongStateOf(0L) }
    var score by remember { mutableIntStateOf(0) }
    var combo by remember { mutableIntStateOf(0) }
    var maxCombo by remember { mutableIntStateOf(0) }
    var perfectCount by remember { mutableIntStateOf(0) }
    var greatCount by remember { mutableIntStateOf(0) }
    var missCount by remember { mutableIntStateOf(0) }

    var lastFeedback by remember { mutableStateOf<String?>(null) }
    var feedbackColor by remember { mutableStateOf(HitGreen) }

    val activeTiles = remember {
        mutableStateListOf<Tile>().apply {
            addAll(level.tiles.map { it.copy(isHit = false, isMissed = false) })
        }
    }

    // Touch effect tracking per lane (lane index -> timestamp)
    val laneTouchTimes = remember { mutableStateMapOf<Int, Long>() }

    // Initialize MediaPlayer
    LaunchedEffect(level) {
        try {
            val player = if (level.isBuiltIn && level.rawResName != null) {
                val resId = context.resources.getIdentifier(level.rawResName, "raw", context.packageName)
                MediaPlayer.create(context, resId)
            } else if (level.audioUri != null) {
                val uri = if (level.audioUri.startsWith("/")) {
                    Uri.fromFile(File(level.audioUri))
                } else {
                    Uri.parse(level.audioUri)
                }
                MediaPlayer().apply {
                    setDataSource(context, uri)
                    prepare()
                }
            } else null

            player?.setOnCompletionListener {
                isGameOver = true
                isPlaying = false
            }

            mediaPlayer = player
            player?.start()
            isPlaying = true
        } catch (e: Exception) {
            Toast.makeText(context, "No se pudo reproducir la música: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    // High frequency game tick
    LaunchedEffect(isPlaying, isPaused) {
        while (isPlaying && !isPaused && !isGameOver) {
            mediaPlayer?.let { player ->
                if (player.isPlaying) {
                    audioTimeMs = player.currentPosition.toLong()

                    // Check for missed tiles that slipped past the hit line
                    val missThreshold = 220L
                    activeTiles.forEach { tile ->
                        if (!tile.isHit && !tile.isMissed && audioTimeMs > (tile.timeMs + missThreshold)) {
                            tile.isMissed = true
                            missCount++
                            combo = 0
                            lastFeedback = "MISS"
                            feedbackColor = MissRed
                        }
                    }

                    // Check victory if all tiles passed
                    val allFinished = activeTiles.all { it.isHit || it.isMissed }
                    if (allFinished && activeTiles.isNotEmpty() && audioTimeMs > (activeTiles.last().timeMs + 500)) {
                        isGameOver = true
                        isPlaying = false
                    }
                }
            }
            delay(16) // ~60fps
        }
    }

    // Clear feedback label after 600ms
    LaunchedEffect(lastFeedback) {
        if (lastFeedback != null) {
            delay(600)
            lastFeedback = null
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            mediaPlayer?.release()
        }
    }

    // Tap lane handler
    val onLanePressed: (Int) -> Unit = { lane ->
        laneTouchTimes[lane] = System.currentTimeMillis()

        // Find nearest unhit tile in this lane
        val candidate = activeTiles.firstOrNull { tile ->
            tile.lane == lane && !tile.isHit && !tile.isMissed && abs(audioTimeMs - tile.timeMs) < 300L
        }

        if (candidate != null) {
            val delta = abs(audioTimeMs - candidate.timeMs)
            candidate.isHit = true
            combo++
            if (combo > maxCombo) maxCombo = combo

            if (delta <= 100L) {
                score += 100 + (combo * 5)
                perfectCount++
                lastFeedback = "PERFECT!"
                feedbackColor = NeonCyan
            } else {
                score += 50 + (combo * 2)
                greatCount++
                lastFeedback = "GREAT"
                feedbackColor = NeonGold
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DeepBackground)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header: Song info, Score, Combo, Pause
            GameHeader(
                title = level.title,
                score = score,
                combo = combo,
                onPauseToggle = {
                    val p = mediaPlayer ?: return@GameHeader
                    if (p.isPlaying) {
                        p.pause()
                        isPaused = true
                    } else {
                        p.start()
                        isPaused = false
                    }
                },
                onExit = onExitGame
            )

            // Game Board Canvas with 4 falling lanes
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                val laneColors = listOf(TileLane0, TileLane1, TileLane2, TileLane3)
                val approachTimeMs = 1200L

                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(Unit) {
                            detectTapGestures { offset ->
                                val laneWidth = size.width / 4f
                                val tappedLane = (offset.x / laneWidth).toInt().coerceIn(0, 3)
                                onLanePressed(tappedLane)
                            }
                        }
                ) {
                    val w = size.width
                    val h = size.height
                    val laneW = w / 4f
                    val hitLineY = h * 0.82f
                    val tileHeight = 110.dp.toPx()

                    // Draw Lane Dividers
                    for (i in 1..3) {
                        drawLine(
                            color = BorderDark.copy(alpha = 0.5f),
                            start = Offset(i * laneW, 0f),
                            end = Offset(i * laneW, h),
                            strokeWidth = 2.dp.toPx()
                        )
                    }

                    // Draw Hit Zone Bar at bottom
                    drawRect(
                        brush = Brush.verticalGradient(
                            colors = listOf(Color.Transparent, NeonCyan.copy(alpha = 0.2f), NeonCyan.copy(alpha = 0.4f)),
                            startY = hitLineY - 40.dp.toPx(),
                            endY = hitLineY + 40.dp.toPx()
                        ),
                        topLeft = Offset(0f, hitLineY - 40.dp.toPx()),
                        size = Size(w, 80.dp.toPx())
                    )
                    drawLine(
                        color = NeonCyan,
                        start = Offset(0f, hitLineY),
                        end = Offset(w, hitLineY),
                        strokeWidth = 3.dp.toPx()
                    )

                    // Draw lane flash when pressed
                    val now = System.currentTimeMillis()
                    for (l in 0..3) {
                        val touchT = laneTouchTimes[l] ?: 0L
                        val elapsed = now - touchT
                        if (elapsed < 180) {
                            val alpha = (1f - elapsed / 180f) * 0.35f
                            drawRect(
                                color = laneColors[l].copy(alpha = alpha),
                                topLeft = Offset(l * laneW, 0f),
                                size = Size(laneW, h)
                            )
                        }
                    }

                    // Draw Falling Tiles
                    activeTiles.forEach { tile ->
                        if (!tile.isHit && !tile.isMissed) {
                            val timeDiff = tile.timeMs - audioTimeMs
                            if (timeDiff in -300L..approachTimeMs) {
                                val progress = 1f - (timeDiff.toFloat() / approachTimeMs.toFloat())
                                val tileY = (hitLineY * progress) - (tileHeight / 2f)

                                val laneX = tile.lane * laneW + 6.dp.toPx()
                                val actualTileW = laneW - 12.dp.toPx()

                                val tileColor = laneColors[tile.lane]
                                drawRoundRect(
                                    brush = Brush.verticalGradient(
                                        colors = listOf(tileColor.copy(alpha = 0.9f), tileColor)
                                    ),
                                    topLeft = Offset(laneX, tileY),
                                    size = Size(actualTileW, tileHeight),
                                    cornerRadius = CornerRadius(14.dp.toPx(), 14.dp.toPx())
                                )
                                // Inner white gloss line
                                drawRoundRect(
                                    color = Color.White.copy(alpha = 0.35f),
                                    topLeft = Offset(laneX + 4.dp.toPx(), tileY + 4.dp.toPx()),
                                    size = Size(actualTileW - 8.dp.toPx(), 10.dp.toPx()),
                                    cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx())
                                )
                            }
                        }
                    }
                }

                // Floating feedback text (PERFECT / GREAT / MISS)
                AnimatedVisibility(
                    visible = lastFeedback != null,
                    enter = fadeIn(),
                    exit = fadeOut(),
                    modifier = Modifier.align(Alignment.Center)
                ) {
                    Text(
                        text = lastFeedback ?: "",
                        color = feedbackColor,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 2.sp
                    )
                }
            }

            // Bottom Lane Buttons for accessible tapping
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(90.dp)
                    .background(SurfaceDark)
                    .border(1.dp, BorderDark),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                val laneColors = listOf(TileLane0, TileLane1, TileLane2, TileLane3)
                for (lane in 0..3) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .border(0.5.dp, BorderDark)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) {
                                onLanePressed(lane)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .clip(CircleShape)
                                    .background(laneColors[lane])
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "TECLA ${lane + 1}",
                                color = TextSecondary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // Pause Overlay
        if (isPaused) {
            AlertDialog(
                onDismissRequest = {},
                title = { Text("Juego en Pausa", fontWeight = FontWeight.Bold, color = TextPrimary) },
                text = { Text("Puntuación actual: $score • Combo: $combo", color = TextSecondary) },
                confirmButton = {
                    Button(
                        onClick = {
                            mediaPlayer?.start()
                            isPaused = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = NeonCyan)
                    ) {
                        Text("Reanudar", color = DeepBackground, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    OutlinedButton(onClick = onExitGame) {
                        Text("Salir al Menú", color = TextPrimary)
                    }
                },
                containerColor = SurfaceDark
            )
        }

        // Game Over / Victory Screen
        if (isGameOver) {
            val totalNotes = level.tiles.size
            val gameScore = GameScore(
                score = score,
                combo = combo,
                maxCombo = maxCombo,
                perfectHits = perfectCount,
                greatHits = greatCount,
                misses = missCount,
                totalTiles = totalNotes
            )

            GameOverDialog(
                score = gameScore,
                onRestart = onRestart,
                onExit = onExitGame
            )
        }
    }
}

@Composable
fun GameHeader(
    title: String,
    score: Int,
    combo: Int,
    onPauseToggle: () -> Unit,
    onExit: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = SurfaceDark,
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderDark)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(onClick = onExit, modifier = Modifier.size(36.dp)) {
                Icon(Icons.Default.Close, contentDescription = "Salir", tint = TextSecondary)
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = title,
                    color = TextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "$score",
                        color = NeonGold,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black
                    )
                    if (combo > 1) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "${combo}x COMBO",
                            color = NeonCyan,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            IconButton(onClick = onPauseToggle, modifier = Modifier.size(36.dp)) {
                Icon(Icons.Default.Pause, contentDescription = "Pausa", tint = TextSecondary)
            }
        }
    }
}

@Composable
fun GameOverDialog(
    score: GameScore,
    onRestart: () -> Unit,
    onExit: () -> Unit
) {
    AlertDialog(
        onDismissRequest = {},
        title = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = if (score.misses == 0 && score.totalTiles > 0) "¡FULL COMBO!" else "¡NIVEL COMPLETADO!",
                    color = if (score.misses == 0) NeonGold else NeonCyan,
                    fontWeight = FontWeight.Black,
                    fontSize = 22.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row {
                    repeat(3) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = if (score.accuracy > 70f) NeonGold else TextMuted,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ScoreRow("Puntos Totales", "${score.score}", NeonGold)
                ScoreRow("Precisión", "${String.format("%.1f", score.accuracy)}%", NeonCyan)
                ScoreRow("Max Combo", "${score.maxCombo}x", TextPrimary)
                ScoreRow("Perfect", "${score.perfectHits}", HitGreen)
                ScoreRow("Great", "${score.greatHits}", NeonPurple)
                ScoreRow("Miss", "${score.misses}", MissRed)
            }
        },
        confirmButton = {
            Button(
                onClick = onRestart,
                colors = ButtonDefaults.buttonColors(containerColor = NeonCyan)
            ) {
                Icon(Icons.Default.Refresh, contentDescription = null, tint = DeepBackground)
                Spacer(modifier = Modifier.width(4.dp))
                Text("Reintentar", color = DeepBackground, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onExit) {
                Text("Menú Principal", color = TextPrimary)
            }
        },
        containerColor = SurfaceDark
    )
}

@Composable
fun ScoreRow(label: String, value: String, color: Color) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, color = TextSecondary, fontSize = 14.sp)
        Text(text = value, color = color, fontSize = 15.sp, fontWeight = FontWeight.Bold)
    }
}
