package com.pianosound.tiles.model

data class Tile(
    val id: Long = System.nanoTime(),
    val timeMs: Long,
    val lane: Int, // 0, 1, 2, 3
    val note: String = "",
    var isHit: Boolean = false,
    var isMissed: Boolean = false
)
