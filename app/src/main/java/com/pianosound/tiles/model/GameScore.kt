package com.pianosound.tiles.model

data class GameScore(
    val score: Int = 0,
    val combo: Int = 0,
    val maxCombo: Int = 0,
    val perfectHits: Int = 0,
    val greatHits: Int = 0,
    val misses: Int = 0,
    val totalTiles: Int = 0
) {
    val accuracy: Float
        get() = if (totalTiles > 0) {
            ((perfectHits * 100f + greatHits * 70f) / (totalTiles * 100f)) * 100f
        } else 0f
}
