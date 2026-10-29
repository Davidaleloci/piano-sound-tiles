package com.pianosound.tiles.ui.screens

import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import android.provider.OpenableColumns
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pianosound.tiles.data.LevelStorage
import com.pianosound.tiles.model.LevelData
import com.pianosound.tiles.model.Tile
import com.pianosound.tiles.ui.theme.*
import kotlinx.coroutines.delay
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LevelEditorScreen(
    onBack: () -> Unit,
    onLevelSaved: (LevelData) -> Unit
) {
    val context = LocalContext.current
    val storage = remember { LevelStorage(context) }

    var selectedAudioUri by remember { mutableStateOf<Uri?>(null) }
    var songFileName by remember { mutableStateOf("") }
    var songTitle by remember { mutableStateOf("") }
    var artistName by remember { mutableStateOf("") }
    var speedMultiplier by remember { mutableFloatStateOf(1.0f) }

    var mediaPlayer by remember { mutableStateOf<MediaPlayer?>(null) }
    var isPlaying by remember { mutableStateOf(false) }
    var currentPositionMs by remember { mutableIntStateOf(0) }
    var durationMs by remember { mutableIntStateOf(0) }

    val recordedTiles = remember { mutableStateListOf<Tile>() }
    var isSaving by remember { mutableStateOf(false) }

    // Audio file picker launcher
    val audioPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            selectedAudioUri = uri
            val name = getFileName(context, uri) ?: "Cancion_personalizada.mp3"
            songFileName = name
            if (songTitle.isBlank()) {
                songTitle = name.substringBeforeLast(".")
            }

            // Release previous player
            mediaPlayer?.release()
            try {
                val player = MediaPlayer().apply {
                    setDataSource(context, uri)
                    prepare()
                }
                mediaPlayer = player
                durationMs = player.duration
                currentPositionMs = 0
                isPlaying = false
                recordedTiles.clear()
            } catch (e: Exception) {
                Toast.makeText(context, "Error al cargar audio: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Playback tracking loop
    LaunchedEffect(isPlaying) {
        while (isPlaying) {
            mediaPlayer?.let { player ->
                if (player.isPlaying) {
                    currentPositionMs = player.currentPosition
                } else {
                    isPlaying = false
                }
            }
            delay(50)
        }
    }

    // Clean up player on leave
    DisposableEffect(Unit) {
        onDispose {
            mediaPlayer?.release()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Creador de Niveles", color = TextPrimary, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Atrás", tint = TextPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DeepBackground)
            )
        },
        containerColor = DeepBackground
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Card: Selección de Archivo de Música
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = SurfaceDark,
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderDark)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "1. Selecciona tu Música (.mp3, .wav)",
                        color = NeonCyan,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Button(
                        onClick = { audioPickerLauncher.launch("audio/*") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = SurfaceCard),
                        border = androidx.compose.foundation.BorderStroke(1.dp, NeonCyan.copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.AudioFile, contentDescription = null, tint = NeonCyan)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (songFileName.isEmpty()) "Examinar archivo de audio" else songFileName,
                            color = TextPrimary,
                            maxLines = 1
                        )
                    }

                    if (durationMs > 0) {
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedTextField(
                            value = songTitle,
                            onValueChange = { songTitle = it },
                            label = { Text("Título de la canción") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = NeonCyan,
                                unfocusedBorderColor = BorderDark,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            )
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = artistName,
                            onValueChange = { artistName = it },
                            label = { Text("Artista / Creador") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = NeonCyan,
                                unfocusedBorderColor = BorderDark,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Card: Grabadora de Ritmo en Vivo
            if (selectedAudioUri != null && durationMs > 0) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = SurfaceDark,
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, BorderDark)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "2. Toca al Ritmo (Live Tap)",
                            color = NeonGold,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                        Text(
                            text = "Presiona Play y toca los 4 carriles al compás de la música para generar las notas en caída.",
                            color = TextSecondary,
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Progress bar
                        LinearProgressIndicator(
                            progress = { if (durationMs > 0) currentPositionMs.toFloat() / durationMs.toFloat() else 0f },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(CircleShape),
                            color = NeonGold,
                            trackColor = SurfaceCard
                        )

                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = formatTime(currentPositionMs),
                                color = TextSecondary,
                                fontSize = 12.sp
                            )
                            Text(
                                text = "${recordedTiles.size} notas creadas",
                                color = HitGreen,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = formatTime(durationMs),
                                color = TextSecondary,
                                fontSize = 12.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // 4 Interactive Record Pads
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            val laneColors = listOf(TileLane0, TileLane1, TileLane2, TileLane3)
                            for (laneIndex in 0..3) {
                                LiveRecordPad(
                                    modifier = Modifier.weight(1f),
                                    laneNumber = laneIndex + 1,
                                    color = laneColors[laneIndex],
                                    onTap = {
                                        if (mediaPlayer != null) {
                                            val pos = mediaPlayer?.currentPosition ?: 0
                                            recordedTiles.add(
                                                Tile(timeMs = pos.toLong(), lane = laneIndex)
                                            )
                                        }
                                    }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Controls: Play / Pause / Reset
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedButton(
                                onClick = {
                                    mediaPlayer?.seekTo(0)
                                    currentPositionMs = 0
                                    recordedTiles.clear()
                                },
                                shape = RoundedCornerShape(12.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, MissRed)
                            ) {
                                Icon(Icons.Default.Refresh, contentDescription = null, tint = MissRed)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Reiniciar", color = MissRed)
                            }

                            FilledIconButton(
                                onClick = {
                                    val player = mediaPlayer ?: return@FilledIconButton
                                    if (player.isPlaying) {
                                        player.pause()
                                        isPlaying = false
                                    } else {
                                        player.start()
                                        isPlaying = true
                                    }
                                },
                                modifier = Modifier.size(56.dp),
                                colors = IconButtonDefaults.filledIconButtonColors(containerColor = NeonGold)
                            ) {
                                Icon(
                                    imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                    contentDescription = "Reproducir / Pausa",
                                    tint = DeepBackground,
                                    modifier = Modifier.size(32.dp)
                                )
                            }

                            Button(
                                onClick = {
                                    if (recordedTiles.isEmpty()) {
                                        Toast.makeText(context, "Toca los carriles para crear notas primero", Toast.LENGTH_SHORT).show()
                                        return@Button
                                    }
                                    val uri = selectedAudioUri ?: return@Button
                                    isSaving = true
                                    val localAudioPath = storage.copyAudioToInternal(uri, songFileName)
                                    val newLevel = LevelData(
                                        id = "custom_${UUID.randomUUID()}",
                                        title = songTitle.ifBlank { "Mi Pista de Piano" },
                                        author = artistName.ifBlank { "Creador Comunitario" },
                                        audioUri = localAudioPath ?: uri.toString(),
                                        durationMs = durationMs.toLong(),
                                        speedMultiplier = speedMultiplier,
                                        tiles = recordedTiles.sortedBy { it.timeMs },
                                        isBuiltIn = false
                                    )
                                    val success = storage.saveLevel(newLevel)
                                    isSaving = false
                                    if (success) {
                                        Toast.makeText(context, "¡Nivel guardado con éxito!", Toast.LENGTH_SHORT).show()
                                        onLevelSaved(newLevel)
                                    } else {
                                        Toast.makeText(context, "Error al guardar el nivel", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = HitGreen),
                                enabled = !isSaving && recordedTiles.isNotEmpty()
                            ) {
                                Icon(Icons.Default.Save, contentDescription = null, tint = DeepBackground)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Guardar", color = DeepBackground, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun LiveRecordPad(
    modifier: Modifier = Modifier,
    laneNumber: Int,
    color: Color,
    onTap: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    LaunchedEffect(isPressed) {
        if (isPressed) {
            onTap()
        }
    }

    val backgroundColor by animateColorAsState(
        targetValue = if (isPressed) color else color.copy(alpha = 0.15f),
        animationSpec = tween(durationMillis = 80),
        label = "padColor"
    )

    Box(
        modifier = modifier
            .fillMaxHeight()
            .clip(RoundedCornerShape(14.dp))
            .background(backgroundColor)
            .border(2.dp, if (isPressed) Color.White else color.copy(alpha = 0.6f), RoundedCornerShape(14.dp))
            .clickable(interactionSource = interactionSource, indication = null) {},
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "$laneNumber",
                color = if (isPressed) DeepBackground else TextPrimary,
                fontSize = 24.sp,
                fontWeight = FontWeight.Black
            )
            Text(
                text = "TOCA",
                color = if (isPressed) DeepBackground else color,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

private fun formatTime(ms: Int): String {
    val totalSeconds = ms / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format("%02d:%02d", minutes, seconds)
}

private fun getFileName(context: Context, uri: Uri): String? {
    var result: String? = null
    if (uri.scheme == "content") {
        context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (index >= 0) {
                    result = cursor.getString(index)
                }
            }
        }
    }
    if (result == null) {
        result = uri.path
        val cut = result?.lastIndexOf('/') ?: -1
        if (cut != -1) {
            result = result?.substring(cut + 1)
        }
    }
    return result
}
