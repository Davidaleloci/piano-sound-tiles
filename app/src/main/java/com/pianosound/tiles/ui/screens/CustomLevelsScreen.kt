package com.pianosound.tiles.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pianosound.tiles.data.LevelStorage
import com.pianosound.tiles.model.LevelData
import com.pianosound.tiles.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomLevelsScreen(
    onBack: () -> Unit,
    onCreateNew: () -> Unit,
    onPlayLevel: (LevelData) -> Unit
) {
    val context = LocalContext.current
    val storage = remember { LevelStorage(context) }
    var levels by remember { mutableStateOf(storage.getAllCustomLevels()) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mis Niveles", color = TextPrimary, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Atrás", tint = TextPrimary)
                    }
                },
                actions = {
                    IconButton(onClick = onCreateNew) {
                        Icon(Icons.Default.Add, contentDescription = "Nuevo nivel", tint = NeonGold)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DeepBackground)
            )
        },
        containerColor = DeepBackground
    ) { padding ->
        if (levels.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.MusicOff,
                        contentDescription = null,
                        tint = TextMuted,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Aún no has creado niveles personalizados",
                        style = MaterialTheme.typography.titleLarge,
                        color = TextPrimary,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Elige cualquier MP3 de tu teléfono y crea tus propios ritmos para jugar o compartirlos.",
                        color = TextSecondary,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    Button(
                        onClick = onCreateNew,
                        colors = ButtonDefaults.buttonColors(containerColor = NeonGold),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.AddCircle, contentDescription = null, tint = DeepBackground)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Crear Mi Primer Nivel", color = DeepBackground, fontWeight = FontWeight.Bold)
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                item {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${levels.size} pistas personalizadas guardadas",
                        color = NeonPurple,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                }

                items(levels, key = { it.id }) { level ->
                    CustomLevelItem(
                        level = level,
                        onPlay = { onPlayLevel(level) },
                        onDelete = {
                            storage.deleteLevel(level.id)
                            levels = storage.getAllCustomLevels()
                            Toast.makeText(context, "Nivel eliminado", Toast.LENGTH_SHORT).show()
                        }
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(20.dp))
                }
            }
        }
    }
}

@Composable
fun CustomLevelItem(
    level: LevelData,
    onPlay: () -> Unit,
    onDelete: () -> Unit
) {
    val dateStr = remember(level.dateCreated) {
        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        sdf.format(Date(level.dateCreated))
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onPlay),
        color = SurfaceDark,
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderDark)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(NeonPurple.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.MusicNote,
                    contentDescription = null,
                    tint = NeonPurple,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = level.title,
                    style = MaterialTheme.typography.titleLarge,
                    color = TextPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${level.author} • $dateStr",
                    color = TextSecondary,
                    fontSize = 12.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${level.tiles.size} notas • ${(level.durationMs / 1000f).toInt()}s",
                    color = NeonGold,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            IconButton(onClick = onDelete) {
                Icon(Icons.Default.DeleteOutline, contentDescription = "Eliminar", tint = MissRed)
            }

            FilledIconButton(
                onClick = onPlay,
                colors = IconButtonDefaults.filledIconButtonColors(containerColor = NeonPurple)
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = "Jugar", tint = TextPrimary)
            }
        }
    }
}
