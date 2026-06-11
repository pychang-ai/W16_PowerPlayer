package com.example.model

data class LyricLine(
    val second: Int,
    val text: String
)

data class Song(
    val id: String,
    val title: String,
    val artist: String,
    val durationSeconds: Int,
    val coverGradientStart: Long, // Hex color like 0xFF3F51B5
    val coverGradientEnd: Long,   // Hex color like 0xFF00BCD4
    val lyrics: List<LyricLine> = emptyList(),
    val isFavorite: Boolean = false,
    val isCustom: Boolean = false
)

enum class RepeatMode {
    OFF,   // Play sequentially and stop at the end
    ALL,   // Cycle through the playlist infinitely
    ONE    // Repeat the current song infinitely
}

enum class ShuffleMode {
    OFF,
    ON
}

data class PlaybackState(
    val songs: List<Song> = emptyList(),
    val currentSong: Song,
    val isPlaying: Boolean = false,
    val currentPositionSeconds: Int = 0,
    val repeatMode: RepeatMode = RepeatMode.OFF,
    val shuffleMode: ShuffleMode = ShuffleMode.OFF,
    val searchQuery: String = ""
)
