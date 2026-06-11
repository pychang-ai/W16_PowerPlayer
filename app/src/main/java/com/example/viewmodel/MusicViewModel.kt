package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.model.LyricLine
import com.example.model.PlaybackState
import com.example.model.RepeatMode
import com.example.model.ShuffleMode
import com.example.model.Song
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MusicViewModel : ViewModel() {

    private val defaultSongs = listOf(
        Song(
            id = "1",
            title = "Starlight Drift",
            artist = "Nebula Dreamer",
            durationSeconds = 120,
            coverGradientStart = 0xFF512DA8, // Deep Purple
            coverGradientEnd = 0xFF1976D2,   // Blue
            lyrics = listOf(
                LyricLine(0, "🌌 Entering deep-space orbit..."),
                LyricLine(8, "Searching for a silent beacon in the deep void..."),
                LyricLine(22, "Standard gravity fading, drifting into cosmic dust..."),
                LyricLine(38, "I see the glowing pulsar guiding us back home..."),
                LyricLine(55, "⚡ [Interstellar synth solo vibrating] ⚡"),
                LyricLine(72, "Starlight whispers pulling us beyond the black hole..."),
                LyricLine(92, "Lost inside the gravity of your stardust dream..."),
                LyricLine(110, "✨ Reaching outer atmosphere... fade transition"),
                LyricLine(118, "🏁 [Transmission Ends]")
            )
        ),
        Song(
            id = "2",
            title = "Midnight Neon",
            artist = "Cyber Horizon",
            durationSeconds = 90,
            coverGradientStart = 0xFFD81B60, // Vivid Pink
            coverGradientEnd = 0xFF8E24AA,   // Dark Purple
            lyrics = listOf(
                LyricLine(0, "🏎️💨 Engine roaring, analog synthesizers active..."),
                LyricLine(8, "Cruising under 1980s pink magenta skies..."),
                LyricLine(20, "Chrome reflections bouncing off the rain-slick glass..."),
                LyricLine(35, "Acceleration spikes, as we break the speed of light..."),
                LyricLine(50, "Retro lasers painting gridlines in the night..."),
                LyricLine(66, "Living inside synthetic memory loops forever..."),
                LyricLine(80, "🎹 [Classic VHS tape wrap-up]"),
                LyricLine(88, "🏁 [System Shutdown]")
            )
        ),
        Song(
            id = "3",
            title = "Coffee Raindrops",
            artist = "Chillhop Cafe",
            durationSeconds = 150,
            coverGradientStart = 0xFF3E2723, // Brown Dark
            coverGradientEnd = 0xFF8D6E63,   // Light Brown
            lyrics = listOf(
                LyricLine(0, "🌧️☕ [Soft Rain & Vinyl Crackle Intro]"),
                LyricLine(10, "Hot coffee warming up this quiet wooden room..."),
                LyricLine(25, "Window drops sliding down, tracing random labyrinths..."),
                LyricLine(45, "Soft acoustic guitar strings dancing with the steam..."),
                LyricLine(65, "Time flows like honey when we are sitting here..."),
                LyricLine(88, "Let the record spin and wash away the external noise..."),
                LyricLine(110, "Just you and me, cozy under wool blanket clouds..."),
                LyricLine(130, "🐾 [Cat purring by the warm radiator]"),
                LyricLine(145, "🏁 [Rain continues to pour softly]")
            )
        ),
        Song(
            id = "4",
            title = "Vapor Rave",
            artist = "Aesthetic Grid",
            durationSeconds = 110,
            coverGradientStart = 0xFF00ACC1, // Teal
            coverGradientEnd = 0xFF00E676,   // Bright Green
            lyrics = listOf(
                LyricLine(0, "📼💻 [Synthwave sweeping filter on]"),
                LyricLine(8, "White marble columns floating in cyberspace..."),
                LyricLine(21, "Feel the disco bassline snap, nostalgia spikes..."),
                LyricLine(40, "Glitchy hologram of a long-lost corporate breeze..."),
                LyricLine(60, "Let's dance until the server runs out of ram..."),
                LyricLine(80, "Sinking deep into the virtual vector sunset..."),
                LyricLine(98, "💾 [System saving session metadata...]"),
                LyricLine(107, "🏁 [Grid Disconnected]")
            )
        ),
        Song(
            id = "5",
            title = "Sunset Velvet",
            artist = "Jazz Club Ensemble",
            durationSeconds = 135,
            coverGradientStart = 0xFFF4511E, // Orange
            coverGradientEnd = 0xFFFFB300,   // Gold Amber
            lyrics = listOf(
                LyricLine(0, "🎷🎷 [Enchanting Saxophone Solo Opening]"),
                LyricLine(12, "Golden hour amber rays reflecting off the brass..."),
                LyricLine(28, "Soft snare brushes keeping a lazy velvet swing..."),
                LyricLine(45, "Cocktail conversations blending in the background noise..."),
                LyricLine(65, "Shadows casting long romantic silhouettes..."),
                LyricLine(88, "Take a slow breath, follow the double bass groove..."),
                LyricLine(110, "🎷 [Extravagant Saxophone fade-out cadenza]"),
                LyricLine(130, "🏁 [Crowd applauds softly]")
            )
        )
    )

    private val _playbackState = MutableStateFlow(
        PlaybackState(
            songs = defaultSongs,
            currentSong = defaultSongs.first()
        )
    )
    val playbackState: StateFlow<PlaybackState> = _playbackState.asStateFlow()

    private var tickerJob: Job? = null

    init {
        startTicker()
    }

    private fun startTicker() {
        tickerJob?.cancel()
        tickerJob = viewModelScope.launch {
            while (true) {
                delay(1000)
                val state = _playbackState.value
                if (state.isPlaying) {
                    val currentPos = state.currentPositionSeconds
                    val duration = state.currentSong.durationSeconds
                    if (currentPos < duration) {
                        _playbackState.update { it.copy(currentPositionSeconds = currentPos + 1) }
                    } else {
                        handleSongFinished()
                    }
                }
            }
        }
    }

    private fun handleSongFinished() {
        val state = _playbackState.value
        when (state.repeatMode) {
            RepeatMode.ONE -> {
                // Replay the exact current song from the start
                _playbackState.update { it.copy(currentPositionSeconds = 0) }
            }
            RepeatMode.ALL -> {
                // Proceed to next song; wrap around to index 0 if we reached the end
                advanceToNextSong(wrapAround = true)
            }
            RepeatMode.OFF -> {
                // Proceed sequentially; stop playing if we reached the end of the playlist
                advanceToNextSong(wrapAround = false)
            }
        }
    }

    private fun advanceToNextSong(wrapAround: Boolean) {
        val state = _playbackState.value
        val activeSongs = getFilteredSongs()
        if (activeSongs.isEmpty()) return

        val currentIdx = activeSongs.indexOfFirst { it.id == state.currentSong.id }

        val nextIdx = if (state.shuffleMode == ShuffleMode.ON) {
            if (activeSongs.size <= 1) {
                0
            } else {
                val possibleIndices = activeSongs.indices.filter { it != currentIdx }
                if (possibleIndices.isNotEmpty()) possibleIndices.random() else 0
            }
        } else {
            currentIdx + 1
        }

        if (nextIdx < activeSongs.size) {
            val nextSong = activeSongs[nextIdx]
            _playbackState.update { it.copy(
                currentSong = nextSong,
                currentPositionSeconds = 0,
                isPlaying = true
            ) }
        } else {
            if (wrapAround) {
                val firstSong = activeSongs.first()
                _playbackState.update { it.copy(
                    currentSong = firstSong,
                    currentPositionSeconds = 0,
                    isPlaying = true
                ) }
            } else {
                // Return to first song but stop playing since sequence complete
                val firstSong = activeSongs.firstOrNull() ?: state.currentSong
                _playbackState.update { it.copy(
                    currentSong = firstSong,
                    currentPositionSeconds = 0,
                    isPlaying = false
                ) }
            }
        }
    }

    fun playOrPause() {
        _playbackState.update { it.copy(isPlaying = !it.isPlaying) }
    }

    fun skipToNext() {
        // Handled as immediate user-triggered skip (always moves forward, wraps around if necessary)
        advanceToNextSong(wrapAround = true)
    }

    fun skipToPrevious() {
        val state = _playbackState.value
        // If current song has played for more than 3 seconds, reset track position to 0
        if (state.currentPositionSeconds > 3) {
            _playbackState.update { it.copy(currentPositionSeconds = 0) }
            return
        }

        val activeSongs = getFilteredSongs()
        if (activeSongs.isEmpty()) return

        val currentIdx = activeSongs.indexOfFirst { it.id == state.currentSong.id }
        if (currentIdx == -1) return

        val prevIdx = if (state.shuffleMode == ShuffleMode.ON) {
            if (activeSongs.size <= 1) {
                0
            } else {
                val possibleIndices = activeSongs.indices.filter { it != currentIdx }
                if (possibleIndices.isNotEmpty()) possibleIndices.random() else 0
            }
        } else {
            if (currentIdx - 1 >= 0) currentIdx - 1 else activeSongs.size - 1
        }

        if (prevIdx >= 0 && prevIdx < activeSongs.size) {
            val prevSong = activeSongs[prevIdx]
            _playbackState.update { it.copy(
                currentSong = prevSong,
                currentPositionSeconds = 0,
                isPlaying = true
            ) }
        }
    }

    fun seekTo(seconds: Int) {
        val state = _playbackState.value
        val clamped = seconds.coerceIn(0, state.currentSong.durationSeconds)
        _playbackState.update { it.copy(currentPositionSeconds = clamped) }
    }

    fun toggleRepeatMode() {
        _playbackState.update { currentState ->
            val nextMode = when (currentState.repeatMode) {
                RepeatMode.OFF -> RepeatMode.ALL
                RepeatMode.ALL -> RepeatMode.ONE
                RepeatMode.ONE -> RepeatMode.OFF
            }
            currentState.copy(repeatMode = nextMode)
        }
    }

    fun toggleShuffleMode() {
        _playbackState.update { currentState ->
            val nextShuffle = when (currentState.shuffleMode) {
                ShuffleMode.OFF -> ShuffleMode.ON
                ShuffleMode.ON -> ShuffleMode.OFF
            }
            currentState.copy(shuffleMode = nextShuffle)
        }
    }

    fun setSongAndPlay(song: Song) {
        _playbackState.update { it.copy(
            currentSong = song,
            currentPositionSeconds = 0,
            isPlaying = true
        ) }
    }

    fun setSearchQuery(query: String) {
        _playbackState.update { it.copy(searchQuery = query) }
    }

    fun toggleFavorite(songId: String) {
        _playbackState.update { currentState ->
            val updatedSongs = currentState.songs.map { s ->
                if (s.id == songId) s.copy(isFavorite = !s.isFavorite) else s
            }
            val updatedCurrent = updatedSongs.firstOrNull { it.id == currentState.currentSong.id } 
                ?: currentState.currentSong

            currentState.copy(
                songs = updatedSongs,
                currentSong = updatedCurrent
            )
        }
    }

    fun addCustomSong(title: String, artist: String, durationSecons: Int, startColor: Long, endColor: Long) {
        val nextId = (System.currentTimeMillis()).toString()
        val defaultCustomLyrics = listOf(
            LyricLine(0, "🎵 Starting your custom groove: $title"),
            LyricLine(durationSecons / 4, "Enjoying the rhythm of $artist..."),
            LyricLine(durationSecons / 2, "🎸 Midway beat drop! Keep shining!"),
            LyricLine((durationSecons * 3) / 4, "Savoring the melody, almost there..."),
            LyricLine(durationSecons - 4, "🏁 Soft fadeout... dynamic repeat mode is active!")
        )
        val newSong = Song(
            id = nextId,
            title = title,
            artist = artist,
            durationSeconds = durationSecons,
            coverGradientStart = startColor,
            coverGradientEnd = endColor,
            lyrics = defaultCustomLyrics,
            isCustom = true
        )

        _playbackState.update { currentState ->
            val updatedList = currentState.songs + newSong
            currentState.copy(
                songs = updatedList,
                currentSong = if (currentState.songs.isEmpty()) newSong else currentState.currentSong
            )
        }
    }

    fun deleteSong(songId: String) {
        _playbackState.update { currentState ->
            val updatedSongs = currentState.songs.filter { it.id != songId }
            if (updatedSongs.isEmpty()) {
                // Don't allow deleting the very last song if it leaves us completely empty,
                // or fall back to a safe placeholder
                return@update currentState
            }

            var nextCurrent = currentState.currentSong
            var nextPosition = currentState.currentPositionSeconds
            var nextIsPlaying = currentState.isPlaying

            if (currentState.currentSong.id == songId) {
                nextCurrent = updatedSongs.first()
                nextPosition = 0
                nextIsPlaying = false // Stop playing briefly
            }

            currentState.copy(
                songs = updatedSongs,
                currentSong = nextCurrent,
                currentPositionSeconds = nextPosition,
                isPlaying = nextIsPlaying
            )
        }
    }

    fun getFilteredSongs(): List<Song> {
        val state = _playbackState.value
        val query = state.searchQuery.trim().lowercase()
        return if (query.isEmpty()) {
            state.songs
        } else {
            state.songs.filter {
                it.title.lowercase().contains(query) || it.artist.lowercase().contains(query)
            }
        }
    }

    override fun onCleared() {
        tickerJob?.cancel()
        super.onCleared()
    }
}
