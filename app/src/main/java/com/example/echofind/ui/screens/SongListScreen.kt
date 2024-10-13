import android.media.MediaPlayer
import android.os.Build
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Delete
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import com.example.echofind.R
import com.example.echofind.data.model.player.Song
import com.example.echofind.data.viewmodel.AuthViewModel
import kotlinx.coroutines.delay
import android.content.Intent
import android.net.Uri

@Composable
fun SongListScreen(
    navController: NavHostController,
    authViewModel: AuthViewModel,
    onSongClick: (Song) -> Unit
) {
    var songs by remember { mutableStateOf<List<Song>>(emptyList()) }
    var mediaPlayer by remember { mutableStateOf<MediaPlayer?>(null) }
    var currentProgress by remember { mutableStateOf(0f) }
    var duration by remember { mutableStateOf(0) }
    var isPlaying by remember { mutableStateOf(false) }
    var playingSongTitle by remember { mutableStateOf<String?>(null) }
    var songToDelete by remember { mutableStateOf<Song?>(null) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    DisposableEffect(Unit) {
        onDispose {
            mediaPlayer?.release()
        }
    }

    LaunchedEffect(Unit) {
        authViewModel.obtenerCancionesGuardadas { canciones ->
            songs = canciones
        }
    }

    LaunchedEffect(playingSongTitle, isPlaying) {
        if (mediaPlayer != null && isPlaying) {
            duration = mediaPlayer?.duration ?: 0
            while (mediaPlayer?.isPlaying == true) {
                currentProgress = mediaPlayer?.currentPosition?.toFloat()?.div(duration) ?: 0f
                delay(100)
            }
            isPlaying = false
            playingSongTitle = null
            currentProgress = 0f
        }
    }

    if (showDeleteDialog && songToDelete != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text(text = "Eliminar canción") },
            text = { Text("¿Estás seguro de que deseas eliminar ${songToDelete?.title}?") },
            confirmButton = {
                IconButton(onClick = {
                    songToDelete?.let {
                        authViewModel.eliminarCancionGuardada(it)
                        songs = songs.filter { song -> song != it }
                        if (playingSongTitle == it.title) {
                            mediaPlayer?.release()
                            mediaPlayer = null
                            isPlaying = false
                            playingSongTitle = null
                            currentProgress = 0f
                        }
                    }
                    showDeleteDialog = false
                    songToDelete = null
                }) {
                    Icon(Icons.Default.Delete, contentDescription = "Eliminar")
                }
            },
            dismissButton = {
                IconButton(onClick = {
                    showDeleteDialog = false
                    songToDelete = null
                }) {
                    Icon(Icons.Default.Cancel, contentDescription = "Cancelar")
                }
            },
            modifier = Modifier.fillMaxWidth()
        )
    }

    val blurModifier = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        Modifier.blur(if (showDeleteDialog) 8.dp else 0.dp)
    } else {
        Modifier
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .then(blurModifier)
            .padding(16.dp)
    ) {
        if (songs.isNotEmpty()) {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(songs.size) { index ->
                    val song = songs[index]
                    val isCurrentlyPlaying = playingSongTitle == song.title && isPlaying
                    val cardColor = if (isCurrentlyPlaying) Color.DarkGray else Color.LightGray

                    SongCard(
                        song = song,
                        currentProgress = if (isCurrentlyPlaying) currentProgress else 0f,
                        isPlaying = isCurrentlyPlaying,
                        onClick = {
                            onSongClick(song)

                            if (playingSongTitle == song.title && isPlaying) {
                                mediaPlayer?.release()
                                mediaPlayer = null
                                isPlaying = false
                                playingSongTitle = null
                                currentProgress = 0f
                                return@SongCard
                            }

                            mediaPlayer?.release()
                            mediaPlayer = null
                            isPlaying = false
                            currentProgress = 0f
                            playingSongTitle = null

                            val previewUrl = song.previewUrl
                            if (!previewUrl.isNullOrEmpty()) {
                                mediaPlayer = MediaPlayer().apply {
                                    setDataSource(previewUrl)
                                    setOnPreparedListener {
                                        currentProgress = 0f
                                        duration = this.duration
                                        start()
                                        isPlaying = true
                                        playingSongTitle = song.title
                                    }
                                    setOnCompletionListener {
                                        isPlaying = false
                                        playingSongTitle = null
                                        currentProgress = 0f
                                    }
                                    prepareAsync()
                                }
                            } else {
                                isPlaying = false
                                playingSongTitle = null
                                currentProgress = 0f
                            }
                        },
                        onLongClick = {
                            songToDelete = song
                            showDeleteDialog = true
                        },
                        onSpotifyClick = {
                            val intent = Intent(Intent.ACTION_VIEW).apply {
                                data = Uri.parse("https://open.spotify.com/intl-es/track/${song.id}")
                                `package` = "com.spotify.music"
                            }
                            navController.context.startActivity(intent)
                        },
                        cardColor = cardColor
                    )
                }
            }
        } else {
            Text(
                text = "No hay canciones guardadas.",
                color = Color.Black,
                modifier = Modifier.align(Alignment.Center)
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SongCard(
    song: Song,
    currentProgress: Float,
    isPlaying: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onSpotifyClick: () -> Unit,
    cardColor: Color
) {
    val artistName = Regex("""name=([^,}]+)""").find(song.artist)?.groupValues?.get(1) ?: "Artista desconocido"

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            ),
        shape = RoundedCornerShape(12.dp),
        backgroundColor = cardColor,
        elevation = 4.dp
    ) {
        Box(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painter = rememberAsyncImagePainter(song.imageUrl),
                        contentDescription = "Song image",
                        modifier = Modifier
                            .size(64.dp)
                            .padding(end = 16.dp),
                        contentScale = ContentScale.Crop
                    )
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = song.title,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = Color.Black
                        )
                        Text(
                            text = artistName,
                            fontSize = 14.sp,
                            color = Color.Gray
                        )
                    }

                    IconButton(
                        onClick = onSpotifyClick,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Image(
                            painter = painterResource(R.drawable.ico_spotify),
                            contentDescription = "Spotify",
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }

            if (isPlaying) {
                LinearProgressIndicator(
                    progress = currentProgress,
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter),
                    color = Color.White
                )
            }
        }
    }
}
