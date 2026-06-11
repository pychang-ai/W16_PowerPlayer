package com.example.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.model.RepeatMode
import com.example.model.ShuffleMode
import com.example.model.Song
import com.example.viewmodel.MusicViewModel
import kotlinx.coroutines.delay
import kotlin.math.sin

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MusicPlayerApp(viewModel: MusicViewModel) {
    val playbackState by viewModel.playbackState.collectAsState()
    val configuration = LocalConfiguration.current
    val isExpanded = configuration.screenWidthDp >= 640

    var showAddDialog by remember { mutableStateOf(false) }

    // Sophisticated Dark Palette
    val darkBackground = Color(0xFF1C1B1F)
    val cardSurface = Color(0xFF2B2930)
    val accentNeonGreen = Color(0xFFD0BCFF)
    val accentPink = Color(0xFFF48FB1)
    val fontWhite = Color(0xFFE6E1E5)
    val fontSecondary = Color(0xFFCAC4D0)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.GraphicEq,
                                contentDescription = "Audio Icon",
                                tint = accentNeonGreen,
                                modifier = Modifier
                                    .size(28.dp)
                                    .padding(end = 6.dp)
                            )
                            Text(
                                text = "AURORA MUSIC",
                                fontWeight = FontWeight.Bold,
                                color = fontWhite,
                                letterSpacing = 2.sp,
                                fontSize = 18.sp
                            )
                        }

                        Button(
                            onClick = { showAddDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = accentPink),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .testTag("add_song_button")
                                .padding(end = 8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Add Track",
                                modifier = Modifier.size(16.dp),
                                tint = Color.White
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("CUSTOM TRACK", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = darkBackground,
                    titleContentColor = fontWhite
                )
            )
        },
        containerColor = darkBackground
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(darkBackground)
        ) {
            if (isExpanded) {
                // Wide View: Double Pane Split Layout
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    // Left Panel: Now Playing controls, cover, visualizer
                    Box(
                        modifier = Modifier
                            .weight(1.2f)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(24.dp))
                            .background(cardSurface)
                            .border(1.dp, Color(0xFF49454F), RoundedCornerShape(24.dp))
                            .padding(24.dp)
                    ) {
                        PlayerPanel(
                            playbackState = playbackState,
                            viewModel = viewModel,
                            accentNeonGreen = accentNeonGreen,
                            accentPink = accentPink,
                            fontWhite = fontWhite,
                            fontSecondary = fontSecondary
                        )
                    }

                    // Right Panel: Lyrics (Top Half) + Playlist Track library (Bottom Half)
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        LyricsCard(
                            currentSong = playbackState.currentSong,
                            currentPosSeconds = playbackState.currentPositionSeconds,
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth(),
                            cardSurface = cardSurface,
                            accentNeonGreen = accentNeonGreen,
                            fontWhite = fontWhite,
                            fontSecondary = fontSecondary
                        )

                        TracksLibraryCard(
                            songs = viewModel.getFilteredSongs(),
                            currentSong = playbackState.currentSong,
                            isPlaying = playbackState.isPlaying,
                            searchQuery = playbackState.searchQuery,
                            onQueryChange = { viewModel.setSearchQuery(it) },
                            onSelectSong = { viewModel.setSongAndPlay(it) },
                            onToggleFav = { viewModel.toggleFavorite(it) },
                            onDelete = { viewModel.deleteSong(it) },
                            modifier = Modifier
                                .weight(1.2f)
                                .fillMaxWidth(),
                            cardSurface = cardSurface,
                            accentNeonGreen = accentNeonGreen,
                            fontWhite = fontWhite,
                            fontSecondary = fontSecondary
                        )
                    }
                }
            } else {
                // Portrait Mobile View: Single Panel Scrollable Layout
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .background(darkBackground),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Quick Player View
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(20.dp))
                            .background(cardSurface)
                            .border(1.dp, Color(0xFF49454F), RoundedCornerShape(20.dp))
                            .padding(16.dp)
                    ) {
                        PlayerPanel(
                            playbackState = playbackState,
                            viewModel = viewModel,
                            accentNeonGreen = accentNeonGreen,
                            accentPink = accentPink,
                            fontWhite = fontWhite,
                            fontSecondary = fontSecondary
                        )
                    }

                    // Scrollable Bottom cards
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Synced Scrolling Lyrics Component
                        LyricsCard(
                            currentSong = playbackState.currentSong,
                            currentPosSeconds = playbackState.currentPositionSeconds,
                            modifier = Modifier
                                .height(160.dp)
                                .fillMaxWidth(),
                            cardSurface = cardSurface,
                            accentNeonGreen = accentNeonGreen,
                            fontWhite = fontWhite,
                            fontSecondary = fontSecondary
                        )

                        // Track library component
                        TracksLibraryCard(
                            songs = viewModel.getFilteredSongs(),
                            currentSong = playbackState.currentSong,
                            isPlaying = playbackState.isPlaying,
                            searchQuery = playbackState.searchQuery,
                            onQueryChange = { viewModel.setSearchQuery(it) },
                            onSelectSong = { viewModel.setSongAndPlay(it) },
                            onToggleFav = { viewModel.toggleFavorite(it) },
                            onDelete = { viewModel.deleteSong(it) },
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth(),
                            cardSurface = cardSurface,
                            accentNeonGreen = accentNeonGreen,
                            fontWhite = fontWhite,
                            fontSecondary = fontSecondary
                        )
                    }
                }
            }

            if (showAddDialog) {
                AddSongDialog(
                    onDismiss = { showAddDialog = false },
                    onConfirm = { title, artist, duration, colorStart, colorEnd ->
                        viewModel.addCustomSong(title, artist, duration, colorStart, colorEnd)
                        showAddDialog = false
                    },
                    accentPink = accentPink,
                    cardSurface = cardSurface,
                    fontWhite = fontWhite,
                    fontSecondary = fontSecondary
                )
            }
        }
    }
}

@Composable
fun PlayerPanel(
    playbackState: com.example.model.PlaybackState,
    viewModel: MusicViewModel,
    accentNeonGreen: Color,
    accentPink: Color,
    fontWhite: Color,
    fontSecondary: Color
) {
    val currentSong = playbackState.currentSong

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Upper section: Title/Artist + Vinyl Record Vinyl Cover
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.weight(1f, fill = false)
        ) {
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(horizontal = 8.dp)
                ) {
                    Text(
                        text = currentSong.title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = fontWhite,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = currentSong.artist,
                        fontSize = 14.sp,
                        color = fontSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Vinyl Spinning Record
            SpinningVinylCover(
                isPlaying = playbackState.isPlaying,
                gradientStart = Color(currentSong.coverGradientStart),
                gradientEnd = Color(currentSong.coverGradientEnd)
            )
        }

        // Mid section: Canvas Wave Visualizer + Slider position indicators
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Spacer(modifier = Modifier.height(14.dp))

            // Bouncing Audio Canvas Spectrogram
            BouncingSpectrogram(
                isPlaying = playbackState.isPlaying,
                primaryColor = Color(currentSong.coverGradientStart),
                secondaryColor = Color(currentSong.coverGradientEnd),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Seeker Slider Bar
            val position = playbackState.currentPositionSeconds
            val totalSeconds = currentSong.durationSeconds
            val progressPercent = if (totalSeconds > 0) position.toFloat() / totalSeconds.toFloat() else 0f

            Slider(
                value = progressPercent,
                onValueChange = { percent ->
                    val newSec = (percent * totalSeconds).toInt()
                    viewModel.seekTo(newSec)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("playback_slider"),
                colors = SliderDefaults.colors(
                    activeTrackColor = accentNeonGreen,
                    inactiveTrackColor = Color(0xFF49454F),
                    thumbColor = accentNeonGreen
                )
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = formatTimeString(position),
                    fontSize = 11.sp,
                    color = fontSecondary,
                    fontWeight = FontWeight.Bold
                )

                // Simulated Repeat indicator text badges
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = when (playbackState.repeatMode) {
                            RepeatMode.OFF -> Icons.Filled.Repeat
                            RepeatMode.ALL -> Icons.Filled.Repeat
                            RepeatMode.ONE -> Icons.Filled.RepeatOne
                        },
                        contentDescription = "Repeat icon badge",
                        modifier = Modifier.size(10.dp),
                        tint = if (playbackState.repeatMode != RepeatMode.OFF) accentNeonGreen else fontSecondary
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = when (playbackState.repeatMode) {
                            RepeatMode.OFF -> "REPEAT: OFF"
                            RepeatMode.ALL -> "REPEAT: ALL"
                            RepeatMode.ONE -> "REPEAT: ONE"
                        },
                        fontSize = 10.sp,
                        color = if (playbackState.repeatMode != RepeatMode.OFF) accentNeonGreen else fontSecondary,
                        fontWeight = FontWeight.Bold
                    )
                }

                Text(
                    text = formatTimeString(totalSeconds),
                    fontSize = 11.sp,
                    color = fontSecondary,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Lower section: Controls (Repeat Mode, Prev, Play, Next, Shuffle, Favorites)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Repeat Button (OFF -> ALL -> ONE -> OFF)
            IconButton(
                onClick = { viewModel.toggleRepeatMode() },
                modifier = Modifier
                    .testTag("repeat_mode_button")
                    .size(44.dp)
            ) {
                Icon(
                    imageVector = when (playbackState.repeatMode) {
                        RepeatMode.OFF -> Icons.Filled.Repeat
                        RepeatMode.ALL -> Icons.Filled.Repeat
                        RepeatMode.ONE -> Icons.Filled.RepeatOne
                    },
                    contentDescription = "Toggle Repeat Mode",
                    tint = if (playbackState.repeatMode != RepeatMode.OFF) accentNeonGreen else fontSecondary,
                    modifier = Modifier.size(24.dp)
                )
            }

            // Skip Previous track
            IconButton(
                onClick = { viewModel.skipToPrevious() },
                modifier = Modifier
                    .testTag("skip_prev_button")
                    .size(44.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.SkipPrevious,
                    contentDescription = "Previous Track",
                    tint = fontWhite,
                    modifier = Modifier.size(28.dp)
                )
            }

            // Play / Pause Circle Action Button (Material FAB style)
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(RoundedCornerShape(28.dp))
                    .background(accentNeonGreen)
                    .clickable { viewModel.playOrPause() }
                    .testTag("play_pause_button"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (playbackState.isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                    contentDescription = if (playbackState.isPlaying) "Pause" else "Play",
                    tint = Color(0xFF381E72),
                    modifier = Modifier.size(40.dp)
                )
            }

            // Skip Next button
            IconButton(
                onClick = { viewModel.skipToNext() },
                modifier = Modifier
                    .testTag("skip_next_button")
                    .size(44.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.SkipNext,
                    contentDescription = "Next Track",
                    tint = fontWhite,
                    modifier = Modifier.size(28.dp)
                )
            }

            // Shuffle Button
            IconButton(
                onClick = { viewModel.toggleShuffleMode() },
                modifier = Modifier
                    .testTag("shuffle_mode_button")
                    .size(44.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Shuffle,
                    contentDescription = "Toggle Shuffle",
                    tint = if (playbackState.shuffleMode == ShuffleMode.ON) accentPink else fontSecondary,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}

@Composable
fun SpinningVinylCover(
    isPlaying: Boolean,
    gradientStart: Color,
    gradientEnd: Color
) {
    // Increment rotationAngle dynamically while song is playing
    var rotationAngle by remember { mutableStateOf(0f) }

    LaunchedEffect(isPlaying) {
        if (isPlaying) {
            while (true) {
                delay(16) // Smooth ~60fps
                rotationAngle = (rotationAngle + 0.6f) % 360f
            }
        }
    }

    Box(
        modifier = Modifier
            .size(160.dp)
            .shadow(16.dp, CircleShape)
            .background(Color(0xFF1C1B1F), CircleShape)
            .border(3.dp, Color(0xFF49454F), CircleShape)
            .padding(4.dp),
        contentAlignment = Alignment.Center
    ) {
        // Vinyl record disc circles
        Canvas(modifier = Modifier.fillMaxSize().rotate(rotationAngle)) {
            val center = Offset(size.width / 2, size.height / 2)
            val radius = size.width / 2

            // Concentric vinyl audio groove lines
            drawCircle(color = Color(0xFF2B2930), radius = radius - 8f, style = Stroke(width = 1.5f))
            drawCircle(color = Color(0xFF49454F), radius = radius - 20f, style = Stroke(width = 1f))
            drawCircle(color = Color(0xFF2B2930), radius = radius - 32f, style = Stroke(width = 1.5f))
            drawCircle(color = Color(0xFF49454F), radius = radius - 44f, style = Stroke(width = 1f))
            drawCircle(color = Color(0xFF2B2930), radius = radius - 55f, style = Stroke(width = 1.5f))
        }

        // Center colored label (representing standard cassette disk center/album cover art)
        Box(
            modifier = Modifier
                .size(76.dp)
                .rotate(rotationAngle)
                .clip(CircleShape)
                .background(Brush.linearGradient(colors = listOf(gradientStart, gradientEnd)))
                .border(2.dp, Color(0xFF1C1B1F), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            // Little spindle center hole
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .background(Color(0xFF1C1B1F), CircleShape)
                    .border(1.5.dp, Color(0xFF49454F), CircleShape)
            )
        }
    }
}

@Composable
fun BouncingSpectrogram(
    isPlaying: Boolean,
    primaryColor: Color,
    secondaryColor: Color,
    modifier: Modifier = Modifier
) {
    // Animated phase that cycles constantly during playback
    val infiniteTransition = rememberInfiniteTransition(label = "visualizer_pulse")
    val animationPhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2f * Math.PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = androidx.compose.animation.core.RepeatMode.Restart
        ),
        label = "phase"
    )

    Canvas(modifier = modifier) {
        val barCount = 28
        val spacing = 8f
        val totalSpacing = spacing * (barCount - 1)
        val barWidth = (size.width - totalSpacing) / barCount
        val maxAvailableHeight = size.height

        for (i in 0 until barCount) {
            // Generate standard sine waves modulated by indices
            val multiplier = if (isPlaying) {
                // Calculate height dynamically based on index and phase to simulate standard audio visual equalizer spectrums
                val rawSine = sin((i.toFloat() * 0.4f) + animationPhase)
                val noise = sin((i.toFloat() * 1.1f) - animationPhase * 1.5f) * 0.3f
                ((rawSine + 1f) / 2f + noise).coerceIn(0.1f, 1f)
            } else {
                // Dimmed, static wave representing silent player standby position
                val base = sin(i.toFloat() * 0.4f)
                ((base + 1f) / 2f) * 0.15f + 0.05f
            }

            val barHeight = maxAvailableHeight * multiplier
            val topLeftX = i * (barWidth + spacing)
            val topLeftY = size.height - barHeight

            val gradientBrush = Brush.verticalGradient(
                colors = listOf(primaryColor, secondaryColor),
                startY = topLeftY,
                endY = size.height
            )

            // Draw clean card spectrogram rectangle shape
            drawRoundRect(
                brush = gradientBrush,
                topLeft = Offset(topLeftX, topLeftY),
                size = androidx.compose.ui.geometry.Size(barWidth, barHeight),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f, 4f)
            )
        }
    }
}

@Composable
fun LyricsCard(
    currentSong: Song,
    currentPosSeconds: Int,
    modifier: Modifier = Modifier,
    cardSurface: Color,
    accentNeonGreen: Color,
    fontWhite: Color,
    fontSecondary: Color
) {
    val lyrics = currentSong.lyrics
    val listState = rememberLazyListState()

    // Find the currently active lyric line index
    val activeIndex = remember(lyrics, currentPosSeconds) {
        lyrics.indexOfLast { it.second <= currentPosSeconds }
    }

    // Smoothly scroll the highlighted active lyric line into the center of view automatically!
    LaunchedEffect(activeIndex) {
        if (activeIndex >= 0) {
            // Scroll to center by animating
            val targetScrollIndex = if (activeIndex > 1) activeIndex - 1 else 0
            listState.animateScrollToItem(targetScrollIndex)
        } else {
            listState.animateScrollToItem(0)
        }
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(cardSurface)
            .border(1.dp, Color(0xFF49454F), RoundedCornerShape(20.dp))
            .padding(16.dp)
            .testTag("lyrics_container")
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.MusicNote,
                        contentDescription = "Lyrics mic",
                        tint = accentNeonGreen,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "SYNCED LYRICS",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = fontWhite,
                        letterSpacing = 1.sp
                    )
                }

                if (activeIndex >= 0 && activeIndex < lyrics.size) {
                    Text(
                        text = "LIVE SYNC",
                        fontSize = 9.sp,
                        color = accentNeonGreen,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .background(accentNeonGreen.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            if (lyrics.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No track lyrics loaded for this song.",
                        color = fontSecondary,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(vertical = 12.dp)
                ) {
                    items(lyrics.size) { index ->
                        val line = lyrics[index]
                        val isActive = index == activeIndex

                        val scale by animateFloatAsState(
                            targetValue = if (isActive) 1.05f else 0.95f,
                            animationSpec = spring(stiffness = Spring.StiffnessLow),
                            label = "font_scale"
                        )

                        Text(
                            text = line.text,
                            fontSize = if (isActive) 15.sp else 13.sp,
                            fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium,
                            color = if (isActive) accentNeonGreen else fontSecondary.copy(alpha = 0.7f),
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp)
                                .animateContentSize()
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TracksLibraryCard(
    songs: List<Song>,
    currentSong: Song,
    isPlaying: Boolean,
    searchQuery: String,
    onQueryChange: (String) -> Unit,
    onSelectSong: (Song) -> Unit,
    onToggleFav: (String) -> Unit,
    onDelete: (String) -> Unit,
    modifier: Modifier = Modifier,
    cardSurface: Color,
    accentNeonGreen: Color,
    fontWhite: Color,
    fontSecondary: Color
) {
    val accentPink = Color(0xFFF48FB1)
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(cardSurface)
            .border(1.dp, Color(0xFF49454F), RoundedCornerShape(20.dp))
            .padding(16.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Search Input Header
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onQueryChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("search_input"),
                placeholder = { Text("Search songs or artists...", fontSize = 13.sp, color = fontSecondary) },
                leadingIcon = { Icon(Icons.Default.Search, "Search icon", tint = fontSecondary, modifier = Modifier.size(18.dp)) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { onQueryChange("") }) {
                            Icon(Icons.Default.Clear, "Clear", tint = fontSecondary, modifier = Modifier.size(16.dp))
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = fontWhite,
                    unfocusedTextColor = fontWhite,
                    focusedBorderColor = accentNeonGreen,
                    unfocusedBorderColor = Color(0xFF49454F),
                    focusedContainerColor = Color(0xFF1C1B1F),
                    unfocusedContainerColor = Color(0xFF1C1B1F)
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "TRACKS QUEUE (${songs.size})",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = fontSecondary,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(start = 4.dp, bottom = 6.dp)
            )

            if (songs.isEmpty()) {
                Box(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No songs match your search.",
                        color = fontSecondary,
                        fontSize = 13.sp
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(vertical = 4.dp)
                ) {
                    items(songs) { song ->
                        val isSelected = song.id == currentSong.id
                        val itemBg = if (isSelected) Color(0xFF381E72).copy(alpha = 0.25f) else Color(0xFF1C1B1F)
                        val borderCol = if (isSelected) accentNeonGreen.copy(alpha = 0.4f) else Color.Transparent

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(itemBg)
                                .border(1.dp, borderCol, RoundedCornerShape(12.dp))
                                .clickable { onSelectSong(song) }
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                                .testTag("track_item_${song.id}"),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Small colored gradient album icon representing cover preview
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        Brush.linearGradient(
                                            colors = listOf(
                                                Color(song.coverGradientStart),
                                                Color(song.coverGradientEnd)
                                            )
                                        )
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                if (isSelected && isPlaying) {
                                    // Mini spinning vinyl spindle icon inside active card
                                    Icon(
                                        imageVector = Icons.Default.GraphicEq,
                                        contentDescription = "Playing Indicator",
                                        tint = Color.White,
                                        modifier = Modifier.size(18.dp)
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.PlayArrow,
                                        contentDescription = "Standard indicator",
                                        tint = Color.White.copy(alpha = 0.8f),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = song.title,
                                    fontSize = 13.s_p,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) accentNeonGreen else fontWhite,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = song.artist,
                                    fontSize = 11.s_p,
                                    color = fontSecondary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            Text(
                                text = formatTimeString(song.durationSeconds),
                                fontSize = 11.sp,
                                color = fontSecondary,
                                modifier = Modifier.padding(horizontal = 4.dp),
                                fontWeight = FontWeight.Bold
                            )

                            // Heart Fav Button
                            IconButton(
                                onClick = { onToggleFav(song.id) },
                                modifier = Modifier.size(34.dp).testTag("fav_button_${song.id}")
                            ) {
                                Icon(
                                    imageVector = if (song.isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                                    contentDescription = "Favorite status icon",
                                    tint = if (song.isFavorite) accentPink else fontSecondary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }

                            // Delete button (Only show for songs when total playlist duration is greater than 1)
                            IconButton(
                                onClick = { onDelete(song.id) },
                                modifier = Modifier.size(34.dp).testTag("delete_button_${song.id}")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Delete Track",
                                    tint = Color(0xFFFF5252).copy(alpha = 0.8f),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddSongDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String, Int, Long, Long) -> Unit,
    accentPink: Color,
    cardSurface: Color,
    fontWhite: Color,
    fontSecondary: Color
) {
    var title by remember { mutableStateOf("") }
    var artist by remember { mutableStateOf("") }
    var durationMinutes by remember { mutableStateOf("2") }
    var durationSeconds by remember { mutableStateOf("30") }

    // Selected Preset Gradient colors
    val gradientPresets = listOf(
        Pair(0xFF00F2FE to 0xFF4FACFE, "Cyan Breeze"),
        Pair(0xFFFF0844 to 0xFFFFB199, "Vibrant Coral"),
        Pair(0xFFF12711 to 0xFFF5AF19, "Golden Sunset"),
        Pair(0xFF11998E to 0xFF38EF7D, "Mint Glow"),
        Pair(0xFFB465DA to 0xFFCF6CC9, "Violet Dream"),
        Pair(0xFFED4264 to 0xFFFFEDBC, "Velvet Rose")
    )
    var selectedPresetIndex by remember { mutableStateOf(0) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .testTag("add_song_dialog_content"),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = cardSurface),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "ADD CUSTOM TRACK",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = fontWhite,
                    letterSpacing = 1.sp
                )

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Song Title", fontSize = 12.sp, color = fontSecondary) },
                    modifier = Modifier.fillMaxWidth().testTag("dialog_title_input"),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = fontWhite,
                        unfocusedTextColor = fontWhite,
                        focusedBorderColor = accentPink,
                        unfocusedBorderColor = Color(0xFF49454F)
                    )
                )

                OutlinedTextField(
                    value = artist,
                    onValueChange = { artist = it },
                    label = { Text("Artist Name", fontSize = 12.sp, color = fontSecondary) },
                    modifier = Modifier.fillMaxWidth().testTag("dialog_artist_input"),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = fontWhite,
                        unfocusedTextColor = fontWhite,
                        focusedBorderColor = accentPink,
                        unfocusedBorderColor = Color(0xFF49454F)
                    )
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = durationMinutes,
                        onValueChange = { durationMinutes = it.filter { char -> char.isDigit() } },
                        label = { Text("Minutes", fontSize = 12.sp, color = fontSecondary) },
                        modifier = Modifier.weight(1f).testTag("dialog_minutes_input"),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = fontWhite,
                            unfocusedTextColor = fontWhite,
                            focusedBorderColor = accentPink,
                            unfocusedBorderColor = Color(0xFF49454F)
                        )
                    )

                    OutlinedTextField(
                        value = durationSeconds,
                        onValueChange = { durationSeconds = it.filter { char -> char.isDigit() } },
                        label = { Text("Seconds", fontSize = 12.sp, color = fontSecondary) },
                        modifier = Modifier.weight(1f).testTag("dialog_seconds_input"),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = fontWhite,
                            unfocusedTextColor = fontWhite,
                            focusedBorderColor = accentPink,
                            unfocusedBorderColor = Color(0xFF49454F)
                        )
                    )
                }

                Text(
                    text = "Select Cover Gradient Profile",
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    color = fontSecondary,
                    letterSpacing = 0.5.sp
                )

                // Horizontal Flow/Row of preset gradients
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    gradientPresets.forEachIndexed { idx, preset ->
                        val isSelected = idx == selectedPresetIndex
                        val borderAlpha = if (isSelected) 1f else 0f
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    Brush.linearGradient(
                                        colors = listOf(
                                            Color(preset.first.first),
                                            Color(preset.first.second)
                                        )
                                    )
                                )
                                .border(
                                    width = 2.dp,
                                    color = if (isSelected) Color.White else Color.Transparent,
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .clickable { selectedPresetIndex = idx }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss, modifier = Modifier.testTag("dialog_cancel_button")) {
                        Text("CANCEL", color = fontSecondary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Button(
                        onClick = {
                            val mins = durationMinutes.toIntOrNull() ?: 2
                            val secs = durationSeconds.toIntOrNull() ?: 30
                            val totalSecs = (mins * 60) + secs
                            val presetVec = gradientPresets[selectedPresetIndex].first
                            onConfirm(
                                title.ifBlank { "Uncharted Grooves" },
                                artist.ifBlank { "Mysterious DJ" },
                                totalSecs.coerceIn(5, 7200),
                                presetVec.first,
                                presetVec.second
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = accentPink),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("dialog_confirm_button")
                    ) {
                        Text("ADD SONG", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Color.White)
                    }
                }
            }
        }
    }
}

fun formatTimeString(seconds: Int): String {
    val mins = seconds / 60
    val secs = seconds % 60
    return String.format("%02d:%02d", mins, secs)
}

// Custom s_p extension to support precise sizing
private val Int.s_p get() = this.sp
