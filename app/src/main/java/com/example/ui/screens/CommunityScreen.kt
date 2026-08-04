package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.db.CommentEntity
import com.example.data.db.CommunityPostEntity
import com.example.data.db.TrackEntity
import com.example.ui.theme.YtRed

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommunityScreen(
    posts: List<CommunityPostEntity>,
    tracks: List<TrackEntity>,
    onPostLikeToggle: (CommunityPostEntity) -> Unit,
    onTrackSelect: (TrackEntity) -> Unit,
    onCreatePost: (author: String, text: String, trackId: Long?) -> Unit,
    onAddComment: (postId: Long, author: String, text: String) -> Unit
) {
    var showCreateDialog by remember { mutableStateOf(false) }
    var activeCommentsPost by remember { mutableStateOf<CommunityPostEntity?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Groups,
                            contentDescription = null,
                            tint = YtRed,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Music Community Feed",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { showCreateDialog = true },
                        modifier = Modifier.testTag("create_post_btn")
                    ) {
                        Icon(Icons.Default.PostAdd, contentDescription = "Create Post", tint = YtRed)
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(4.dp))
                // Share music prompt card
                Surface(
                    onClick = { showCreateDialog = true },
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AsyncImage(
                            model = "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=200&auto=format&fit=crop",
                            contentDescription = "Avatar",
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Share a song recommendation or discussion...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            items(posts) { post ->
                val attachedTrack = tracks.firstOrNull { it.id == post.attachedTrackId }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("community_post_${post.id}"),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        // Author header
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            AsyncImage(
                                model = post.authorAvatarUrl,
                                contentDescription = post.authorName,
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = post.authorName,
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Music Creator • Public",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Text content
                        Text(
                            text = post.textContent,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        // Attached Track Card
                        attachedTrack?.let { track ->
                            Spacer(modifier = Modifier.height(12.dp))
                            Surface(
                                onClick = { onTrackSelect(track) },
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.surface,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    AsyncImage(
                                        model = track.coverUrl,
                                        contentDescription = track.title,
                                        modifier = Modifier
                                            .size(50.dp)
                                            .clip(RoundedCornerShape(6.dp)),
                                        contentScale = ContentScale.Crop
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = track.title,
                                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = track.artist,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    IconButton(
                                        onClick = { onTrackSelect(track) },
                                        modifier = Modifier.testTag("play_attached_track_${track.id}")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.PlayCircleFilled,
                                            contentDescription = "Play",
                                            tint = YtRed,
                                            modifier = Modifier.size(36.dp)
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Actions bar (Like & Comments)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(onClick = { onPostLikeToggle(post) }) {
                                    Icon(
                                        imageVector = if (post.isLikedByMe) Icons.Default.Favorite else Icons.Outlined.FavoriteBorder,
                                        contentDescription = "Like",
                                        tint = if (post.isLikedByMe) YtRed else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Text(
                                    text = "${post.likesCount}",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .clickable { activeCommentsPost = post }
                                    .padding(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.ChatBubbleOutline,
                                    contentDescription = "Comments",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Comments",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }

    // New Post Dialog
    if (showCreateDialog) {
        CreatePostDialog(
            tracks = tracks,
            onDismiss = { showCreateDialog = false },
            onSubmit = { author, text, trackId ->
                onCreatePost(author, text, trackId)
                showCreateDialog = false
            }
        )
    }

    // Comments Sheet
    activeCommentsPost?.let { post ->
        CommentsSheet(
            post = post,
            onDismiss = { activeCommentsPost = null },
            onAddComment = { author, text ->
                onAddComment(post.id, author, text)
            }
        )
    }
}

@Composable
fun CreatePostDialog(
    tracks: List<TrackEntity>,
    onDismiss: () -> Unit,
    onSubmit: (author: String, text: String, trackId: Long?) -> Unit
) {
    var authorName by remember { mutableStateOf("Music Fan") }
    var textContent by remember { mutableStateOf("") }
    var selectedTrackId by remember { mutableStateOf<Long?>(tracks.firstOrNull()?.id) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create Community Post") },
        text = {
            Column {
                OutlinedTextField(
                    value = authorName,
                    onValueChange = { authorName = it },
                    label = { Text("Your Display Name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = textContent,
                    onValueChange = { textContent = it },
                    label = { Text("What's on your mind?") },
                    placeholder = { Text("Share thoughts on your favorite track or audio mix...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    maxLines = 4
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text("Attach Song (Optional):", style = MaterialTheme.typography.labelMedium)

                LazyColumn(modifier = Modifier.height(120.dp)) {
                    items(tracks) { track ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedTrackId = track.id }
                                .padding(vertical = 4.dp)
                        ) {
                            RadioButton(
                                selected = selectedTrackId == track.id,
                                onClick = { selectedTrackId = track.id }
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${track.title} - ${track.artist}",
                                style = MaterialTheme.typography.bodySmall,
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (textContent.isNotBlank()) {
                        onSubmit(authorName, textContent, selectedTrackId)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = YtRed)
            ) {
                Text("Post")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommentsSheet(
    post: CommunityPostEntity,
    onDismiss: () -> Unit,
    onAddComment: (author: String, text: String) -> Unit
) {
    var commentText by remember { mutableStateOf("") }
    var authorName by remember { mutableStateOf("Community User") }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Text(
                text = "Discussion Comments",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Sample pre-populated comments view
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("Marcus Vibe", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                    Text("Awesome audio quality! Equalizer settings really elevate this track.", style = MaterialTheme.typography.bodySmall)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = commentText,
                onValueChange = { commentText = it },
                label = { Text("Write a comment...") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = {
                    if (commentText.isNotBlank()) {
                        onAddComment(authorName, commentText)
                        commentText = ""
                        onDismiss()
                    }
                },
                modifier = Modifier.align(Alignment.End),
                colors = ButtonDefaults.buttonColors(containerColor = YtRed)
            ) {
                Text("Send Comment")
            }
        }
    }
}
