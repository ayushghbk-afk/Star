package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.auth.UserAccount
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.YtRed

private val ARTWORK_PRESETS = listOf(
    "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?w=600&auto=format&fit=crop" to "Cyber Studio",
    "https://images.unsplash.com/photo-1470225620780-dba8ba36b745?w=600&auto=format&fit=crop" to "DJ Live Stage",
    "https://images.unsplash.com/photo-1514525253161-7a46d19cd819?w=600&auto=format&fit=crop" to "Neon Concert",
    "https://images.unsplash.com/photo-1518609878373-06d740f60d8b?w=600&auto=format&fit=crop" to "Lofi Sunset",
    "https://images.unsplash.com/photo-1508700115892-45ecd05ae2ad?w=600&auto=format&fit=crop" to "Retro Vinyl",
    "https://images.unsplash.com/photo-1493225457124-a3eb161ffa5f?w=600&auto=format&fit=crop" to "Acoustic Gold"
)

private val GENRE_OPTIONS = listOf(
    "Pop", "Hip-Hop", "Synthwave / EDM", "Lofi Beats", "Rock", "R&B", "Acoustic", "Jazz", "Classical", "Indie"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UploadTrackSheet(
    isUploading: Boolean,
    uploadProgress: Int,
    uploadStatusMessage: String,
    currentUser: UserAccount? = null,
    onStartUpload: (
        title: String,
        artist: String,
        album: String,
        genre: String,
        lyrics: String,
        fileType: String,
        uploader: String,
        audioUri: String?,
        coverUrl: String?,
        durationSec: Int
    ) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    var selectedAudioUri by remember { mutableStateOf<Uri?>(null) }
    var selectedAudioFileName by remember { mutableStateOf<String?>(null) }
    var selectedAudioSize by remember { mutableStateOf<String?>(null) }

    var selectedCoverUri by remember { mutableStateOf<Uri?>(null) }
    var selectedCoverPreset by remember { mutableStateOf(ARTWORK_PRESETS[0].first) }

    var title by remember { mutableStateOf("") }
    var artist by remember { mutableStateOf(currentUser?.displayName ?: "Stream Creator") }
    var featuredArtists by remember { mutableStateOf("") }
    var album by remember { mutableStateOf("") }
    var genre by remember { mutableStateOf("Pop") }
    var lyrics by remember { mutableStateOf("") }
    var uploaderName by remember { mutableStateOf(currentUser?.displayName ?: "Community Creator") }
    var selectedFileType by remember { mutableStateOf("MP3 Audio") } // "MP3 Audio" or "MP4 Video"
    var isExplicit by remember { mutableStateOf(false) }
    var isPreviewPlaying by remember { mutableStateOf(false) }

    // File pickers
    val audioPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            selectedAudioUri = it
            val fileName = it.lastPathSegment?.substringAfterLast("/") ?: "selected_track.mp3"
            selectedAudioFileName = fileName
            selectedAudioSize = "7.8 MB • 320kbps Lossless"
            if (title.isBlank()) {
                val cleanTitle = fileName.replace(".mp3", "", ignoreCase = true)
                    .replace(".wav", "", ignoreCase = true)
                    .replace(".m4a", "", ignoreCase = true)
                    .replace("_", " ")
                    .replace("-", " ")
                title = cleanTitle.replaceFirstChar { char -> char.uppercase() }
            }
        }
    }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            selectedCoverUri = it
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = Modifier.testTag("upload_track_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Header Title
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Surface(
                    color = YtRed.copy(alpha = 0.15f),
                    shape = CircleShape,
                    modifier = Modifier.size(44.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.CloudUpload,
                            contentDescription = "Upload",
                            tint = YtRed,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Music & Song Upload Studio",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Upload audio file, custom artwork & synced lyrics",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            if (isUploading) {
                // Upload Progress View with Step Indicators
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                    ),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(
                                progress = { uploadProgress / 100f },
                                color = YtRed,
                                trackColor = MaterialTheme.colorScheme.outlineVariant,
                                modifier = Modifier.size(80.dp),
                                strokeWidth = 6.dp
                            )
                            Text(
                                text = "$uploadProgress%",
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                color = YtRed
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = uploadStatusMessage,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        LinearProgressIndicator(
                            progress = { uploadProgress / 100f },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = YtRed,
                            trackColor = MaterialTheme.colorScheme.outlineVariant
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        // Stepped Pipeline Status Badges
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            val step1Done = uploadProgress >= 25
                            val step2Done = uploadProgress >= 50
                            val step3Done = uploadProgress >= 75
                            val step4Done = uploadProgress >= 100

                            StepBadge(step = 1, title = "Analyze", isDone = step1Done)
                            StepBadge(step = 2, title = "Encode", isDone = step2Done)
                            StepBadge(step = 3, title = "Lyrics", isDone = step3Done)
                            StepBadge(step = 4, title = "Publish", isDone = step4Done)
                        }
                    }
                }
            } else {
                // FILE TYPE SELECTOR (AUDIO vs VIDEO)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    FilterChip(
                        selected = selectedFileType == "MP3 Audio",
                        onClick = { selectedFileType = "MP3 Audio" },
                        label = { Text("MP3 / Audio File", fontWeight = FontWeight.SemiBold) },
                        leadingIcon = { Icon(Icons.Default.MusicNote, contentDescription = null) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    )
                    FilterChip(
                        selected = selectedFileType == "MP4 Video",
                        onClick = { selectedFileType = "MP4 Video" },
                        label = { Text("MP4 / Video Track", fontWeight = FontWeight.SemiBold) },
                        leadingIcon = { Icon(Icons.Default.Videocam, contentDescription = null) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // SECTION 1: AUDIO FILE SELECTION CARD
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                    ),
                    shape = RoundedCornerShape(18.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "1. Audio Source File",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            if (selectedAudioUri != null) {
                                AssistChip(
                                    onClick = { selectedAudioUri = null; selectedAudioFileName = null },
                                    label = { Text("Change File") },
                                    leadingIcon = { Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp)) }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        if (selectedAudioUri == null) {
                            // File Selection Trigger Area
                            Surface(
                                color = MaterialTheme.colorScheme.surface,
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(
                                        1.dp,
                                        MaterialTheme.colorScheme.outlineVariant,
                                        RoundedCornerShape(14.dp)
                                    )
                                    .clickable {
                                        val mimeType = if (selectedFileType == "MP4 Video") "video/*" else "audio/*"
                                        audioPickerLauncher.launch(mimeType)
                                    }
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.FolderOpen,
                                        contentDescription = "Pick Audio File",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(28.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = "Select Audio File from Device",
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "Supports MP3, WAV, FLAC, M4A, AAC",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        } else {
                            // Audio File Selected Card with Waveform Visualizer Preview
                            Surface(
                                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        IconButton(
                                            onClick = { isPreviewPlaying = !isPreviewPlaying },
                                            modifier = Modifier
                                                .size(40.dp)
                                                .background(YtRed, CircleShape)
                                        ) {
                                            Icon(
                                                imageVector = if (isPreviewPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                                contentDescription = "Preview Play",
                                                tint = Color.White
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = selectedAudioFileName ?: "Selected Audio Track",
                                                style = MaterialTheme.typography.titleSmall,
                                                fontWeight = FontWeight.Bold,
                                                maxLines = 1
                                            )
                                            Text(
                                                text = selectedAudioSize ?: "Local Audio File",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))

                                    // Simulated Audio Waveform Visualizer
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(28.dp)
                                            .background(
                                                MaterialTheme.colorScheme.surface,
                                                RoundedCornerShape(8.dp)
                                            )
                                            .padding(horizontal = 8.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        val barHeights = remember { listOf(0.3f, 0.6f, 0.9f, 0.5f, 0.8f, 1f, 0.4f, 0.7f, 0.9f, 0.6f, 0.3f, 0.8f, 0.5f, 0.9f, 0.4f, 0.7f, 0.2f, 0.6f, 0.8f, 0.5f) }
                                        barHeights.forEach { h ->
                                            Box(
                                                modifier = Modifier
                                                    .width(4.dp)
                                                    .fillMaxHeight(if (isPreviewPlaying) h else h * 0.4f)
                                                    .background(
                                                        if (isPreviewPlaying) YtRed else MaterialTheme.colorScheme.outline,
                                                        RoundedCornerShape(2.dp)
                                                    )
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // SECTION 2: COVER ARTWORK PICKER
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                    ),
                    shape = RoundedCornerShape(18.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "2. Track Artwork / Cover Image",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            // Current Selected Image Preview
                            val currentImageModel = selectedCoverUri?.toString() ?: selectedCoverPreset
                            AsyncImage(
                                model = currentImageModel,
                                contentDescription = "Track Artwork",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(80.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .border(2.dp, YtRed, RoundedCornerShape(12.dp))
                            )

                            Spacer(modifier = Modifier.width(14.dp))

                            Column {
                                OutlinedButton(
                                    onClick = { imagePickerLauncher.launch("image/*") },
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Pick Custom Image")
                                }
                                Text(
                                    text = "or choose a studio theme preset below:",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Preset Theme Selector Row
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            ARTWORK_PRESETS.take(4).forEach { (url, label) ->
                                val isSelected = selectedCoverUri == null && selectedCoverPreset == url
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier
                                        .size(56.dp)
                                        .border(
                                            width = if (isSelected) 2.dp else 0.dp,
                                            color = if (isSelected) YtRed else Color.Transparent,
                                            shape = RoundedCornerShape(8.dp)
                                        )
                                        .clickable {
                                            selectedCoverUri = null
                                            selectedCoverPreset = url
                                        }
                                ) {
                                    AsyncImage(
                                        model = url,
                                        contentDescription = label,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // SECTION 3: SONG METADATA FIELDS
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Song Title *") },
                    placeholder = { Text("e.g. Midnight Horizon") },
                    leadingIcon = { Icon(Icons.Default.MusicNote, contentDescription = null) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("upload_title_input"),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = artist,
                    onValueChange = { artist = it },
                    label = { Text("Main Artist Name *") },
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("upload_artist_input"),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = album,
                        onValueChange = { album = it },
                        label = { Text("Album Name") },
                        placeholder = { Text("Single") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                    OutlinedTextField(
                        value = featuredArtists,
                        onValueChange = { featuredArtists = it },
                        label = { Text("Featured Artists") },
                        placeholder = { Text("feat. DJ Spark") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // GENRE CHIPS SELECTOR
                Text(
                    text = "Select Music Genre:",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))

                Column(
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    GENRE_OPTIONS.chunked(3).forEach { chunk ->
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            chunk.forEach { opt ->
                                FilterChip(
                                    selected = genre == opt,
                                    onClick = { genre = opt },
                                    label = { Text(opt, style = MaterialTheme.typography.labelMedium) },
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = uploaderName,
                    onValueChange = { uploaderName = it },
                    label = { Text("Creator / Uploader Handle") },
                    leadingIcon = { Icon(Icons.Default.AccountCircle, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(14.dp))

                // SECTION 4: SYNCHRONIZED LYRICS EDITOR
                OutlinedTextField(
                    value = lyrics,
                    onValueChange = { lyrics = it },
                    label = { Text("Song Lyrics (Optional - Auto synchronized)") },
                    placeholder = { Text("Line 1: In the midnight light\nLine 2: We ride the cyber wave\nLine 3: High speed music flow") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    maxLines = 6,
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(14.dp))

                // EXPLICIT CONTENT & PUBLIC VISIBILITY TOGGLE
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                            RoundedCornerShape(12.dp)
                        )
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Public,
                            contentDescription = "Public",
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Publish to Public StreamSync Community",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    Switch(
                        checked = true,
                        onCheckedChange = {}
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // SUBMIT UPLOAD BUTTON
                Button(
                    onClick = {
                        val finalCover = selectedCoverUri?.toString() ?: selectedCoverPreset
                        val finalAudio = selectedAudioUri?.toString()
                        val combinedArtist = if (featuredArtists.isNotBlank()) "$artist feat. $featuredArtists" else artist

                        onStartUpload(
                            title.ifBlank { selectedAudioFileName?.replace(".mp3", "") ?: "New Track Stream" },
                            combinedArtist.ifBlank { uploaderName },
                            album.ifBlank { "StreamSync Single" },
                            genre.ifBlank { "Pop" },
                            lyrics,
                            selectedFileType,
                            uploaderName.ifBlank { "Community Creator" },
                            finalAudio,
                            finalCover,
                            210
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("submit_upload_btn"),
                    colors = ButtonDefaults.buttonColors(containerColor = YtRed),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(Icons.Default.CloudUpload, contentDescription = null)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Upload Song & Publish to Server",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun StepBadge(step: Int, title: String, isDone: Boolean) {
    val bgColor by animateColorAsState(
        targetValue = if (isDone) YtRed else MaterialTheme.colorScheme.outlineVariant,
        label = "StepBadgeColor"
    )

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Surface(
            color = bgColor,
            shape = CircleShape,
            modifier = Modifier.size(28.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                if (isDone) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Done",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                } else {
                    Text(
                        text = "$step",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.labelSmall,
            color = if (isDone) YtRed else MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = if (isDone) FontWeight.Bold else FontWeight.Normal
        )
    }
}
