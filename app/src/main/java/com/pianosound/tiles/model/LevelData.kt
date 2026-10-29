package com.pianosound.tiles.model

data class LevelData(
    val id: String,
    val title: String,
    val author: String = "Desconocido",
    val audioUri: String? = null,
    val rawResName: String? = null, // "fur_elise", "ode_to_joy", "canon_in_d"
    val durationMs: Long = 0,
    val speedMultiplier: Float = 1.0f,
    val tiles: List<Tile> = emptyList(),
    val isBuiltIn: Boolean = false,
    val dateCreated: Long = System.currentTimeMillis()
)
