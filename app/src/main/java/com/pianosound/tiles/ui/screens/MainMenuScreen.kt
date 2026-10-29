package com.pianosound.tiles.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pianosound.tiles.ui.theme.*

@Composable
fun MainMenuScreen(
    onPlayClassics: () -> Unit,
    onCreateLevel: () -> Unit,
    onCustomLevels: () -> Unit
) {
    var showInfoDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DeepBackground)
            .statusBarsPadding()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Hero Icon with Gradient Glow
        Box(
            modifier = Modifier
                .size(90.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(
                    Brush.radialGradient(
                        colors = listOf(NeonCyan.copy(alpha = 0.4f), DeepBackground)
                    )
                )
                .border(2.dp, NeonCyan, RoundedCornerShape(24.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.MusicNote,
                contentDescription = null,
                tint = NeonCyan,
                modifier = Modifier.size(50.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "PIANO SOUND",
            style = MaterialTheme.typography.headlineLarge,
            color = TextPrimary,
            fontWeight = FontWeight.Black,
            letterSpacing = 2.sp
        )

        Text(
            text = "RHYTHM TILES & LEVEL CREATOR",
            style = MaterialTheme.typography.bodyMedium,
            color = NeonGold,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Status pill
        Surface(
            shape = CircleShape,
            color = SurfaceCard,
            border = androidx.compose.foundation.BorderStroke(1.dp, BorderDark)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(HitGreen)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Apto para Google Play (Sin Copyright)",
                    color = TextSecondary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Menu Card 1: Canciones Clásicas
        MenuActionCard(
            title = "Canciones Clásicas",
            subtitle = "Für Elise, Oda a la Alegría, Canon en Re",
            icon = Icons.Default.PlayArrow,
            accentColor = NeonCyan,
            onClick = onPlayClassics
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Menu Card 2: Creador de Niveles
        MenuActionCard(
            title = "Creador de Niveles",
            subtitle = "Importa tu MP3 y toca el ritmo en tiempo real",
            icon = Icons.Default.AddCircle,
            accentColor = NeonGold,
            onClick = onCreateLevel
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Menu Card 3: Mis Niveles Creados
        MenuActionCard(
            title = "Mis Niveles",
            subtitle = "Juega o administra tus pistas personalizadas",
            icon = Icons.Default.LibraryMusic,
            accentColor = NeonPurple,
            onClick = onCustomLevels
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Botón de Estrategia Legal Play Store
        TextButton(
            onClick = { showInfoDialog = true },
            colors = ButtonDefaults.textButtonColors(contentColor = TextSecondary)
        ) {
            Icon(Icons.Default.Info, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("¿Cómo funciona la estrategia legal para Play Store?")
        }

        Spacer(modifier = Modifier.height(20.dp))
    }

    if (showInfoDialog) {
        AlertDialog(
            onDismissRequest = { showInfoDialog = false },
            title = {
                Text(
                    "Estrategia de Publicación Segura",
                    fontWeight = FontWeight.Bold,
                    color = NeonGold
                )
            },
            text = {
                Text(
                    "1. Canciones Clásicas: Vienen preinstaladas y son 100% de Dominio Público, sin regalías ni riesgo de baneo.\n\n" +
                    "2. Tu Música (MP3): El creador de niveles carga archivos de música guardados localmente en tu teléfono. El nivel solo guarda marcas de tiempo (JSON), no distribuye archivos de audio en la nube.\n\n" +
                    "3. Cumplimiento con Google Play: Tu app funciona como un editor y reproductor neutral, protegiendo tu cuenta de desarrollador.",
                    color = TextPrimary,
                    fontSize = 14.sp,
                    lineHeight = 20.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = { showInfoDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = NeonCyan)
                ) {
                    Text("Entendido", color = DeepBackground, fontWeight = FontWeight.Bold)
                }
            },
            containerColor = SurfaceDark
        )
    }
}

@Composable
fun MenuActionCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    accentColor: Color,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .clickable(onClick = onClick),
        color = SurfaceDark,
        shape = RoundedCornerShape(18.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderDark)
    ) {
        Row(
            modifier = Modifier.padding(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(accentColor.copy(alpha = 0.15f))
                    .border(1.5.dp, accentColor.copy(alpha = 0.5f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                    fontSize = 13.sp
                )
            }

            Icon(
                imageVector = Icons.Default.PlayArrow,
                contentDescription = null,
                tint = TextMuted,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
