package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.YtRed

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UploadTrackSheet(
    isUploading: Boolean,
    uploadProgress: Int,
    uploadStatusMessage: String,
    onStartUpload: (title: String, artist: String, album: String, genre: String, lyrics: String, fileType: String, uploader: String) -> Unit,
    onDismiss: () -> Unit
) {
    var title by remember { mutableStateOf("") }
    var artist by remember { mutableStateOf("") }
    var album by remember { mutableStateOf("") }
    var genre by remember { mutableStateOf("Pop") }
    var lyrics by remember { mutableStateOf("") }
    var uploaderName by remember { mutableStateOf("User Creator") }
    var selectedFileType by remember { mutableStateOf("MP3 Audio") } // "MP3 Audio" or "MP4 Video"

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
            // Header
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.CloudUpload,
                    contentDescription = "Upload",
                    tint = YtRed,
                    modifier = Modifier.size(32.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Upload MP3 or Video Track",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Server will process & make publicly discoverable",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            if (isUploading) {
                // Upload Progress View
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(
                            progress = { uploadProgress / 100f },
                            color = YtRed,
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "$uploadProgress%",
                            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                            color = YtRed
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = uploadStatusMessage,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        LinearProgressIndicator(
                            progress = { uploadProgress / 100f },
                            modifier = Modifier.fillMaxWidth(),
                            color = YtRed
                        )
                    }
                }
            } else {
                // Upload Form
                // File Type Selector
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    FilterChip(
                        selected = selectedFileType == "MP3 Audio",
                        onClick = { selectedFileType = "MP3 Audio" },
                        label = { Text("MP3 Audio File") },
                        leadingIcon = { Icon(Icons.Default.MusicNote, contentDescription = null) },
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = selectedFileType == "MP4 Video",
                        onClick = { selectedFileType = "MP4 Video" },
                        label = { Text("MP4 / Video File") },
                        leadingIcon = { Icon(Icons.Default.Videocam, contentDescription = null) },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Song Title *") },
                    placeholder = { Text("e.g. Cybernetic Glow") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("upload_title_input"),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = artist,
                    onValueChange = { artist = it },
                    label = { Text("Artist Name *") },
                    placeholder = { Text("e.g. Starwave DJ") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("upload_artist_input"),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = album,
                        onValueChange = { album = it },
                        label = { Text("Album Name") },
                        placeholder = { Text("Single") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = genre,
                        onValueChange = { genre = it },
                        label = { Text("Genre") },
                        placeholder = { Text("Synthwave") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = uploaderName,
                    onValueChange = { uploaderName = it },
                    label = { Text("Creator / Uploader Handle") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = lyrics,
                    onValueChange = { lyrics = it },
                    label = { Text("Lyrics (Optional - Server will auto sync timing)") },
                    placeholder = { Text("Line 1\nLine 2\nLine 3") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(110.dp),
                    maxLines = 5
                )

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = {
                        onStartUpload(
                            title.ifBlank { "New Stream Track" },
                            artist.ifBlank { uploaderName },
                            album.ifBlank { "Community Single" },
                            genre.ifBlank { "Dance" },
                            lyrics,
                            selectedFileType,
                            uploaderName.ifBlank { "Community Creator" }
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("submit_upload_btn"),
                    colors = ButtonDefaults.buttonColors(containerColor = YtRed)
                ) {
                    Icon(Icons.Default.CloudUpload, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Upload & Process to Cloud Server",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}
