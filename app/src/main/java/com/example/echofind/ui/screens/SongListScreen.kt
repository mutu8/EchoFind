import android.media.MediaPlayer
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil.compose.rememberImagePainter
import com.example.echofind.data.model.player.Song
import com.example.echofind.data.viewmodel.AuthViewModel
import kotlinx.coroutines.delay

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

    // Estado para controlar el diálogo de confirmación de eliminación
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

    // Mantener actualizado el progreso y gestionar la reproducción
    LaunchedEffect(playingSongTitle, isPlaying) {
        if (mediaPlayer != null && isPlaying) {
            duration = mediaPlayer?.duration ?: 0
            while (mediaPlayer?.isPlaying == true) {
                currentProgress = mediaPlayer?.currentPosition?.toFloat()?.div(duration) ?: 0f
                delay(100)
            }
            // Resetear el estado al terminar la reproducción
            isPlaying = false
            playingSongTitle = null
            currentProgress = 0f
        }
    }

    // Mostrar diálogo de confirmación para eliminar la canción
    if (showDeleteDialog && songToDelete != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text(text = "Eliminar canción") },
            text = { Text("¿Estás seguro de que deseas eliminar ${songToDelete?.title}?") },
            confirmButton = {
                Button(onClick = {
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
                    Text("Eliminar")
                }
            },
            dismissButton = {
                Button(onClick = {
                    showDeleteDialog = false
                    songToDelete = null
                }) {
                    Text("Cancelar")
                }
            }
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(16.dp)
    ) {
        if (songs.isNotEmpty()) {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(songs.size) { index ->
                    val song = songs[index]
                    val isCurrentlyPlaying = playingSongTitle == song.title && isPlaying
                    val cardColor = if (isCurrentlyPlaying) Color.Green else Color.LightGray

                    SongCard(
                        song = song,
                        currentProgress = if (isCurrentlyPlaying) currentProgress else 0f,
                        isPlaying = isCurrentlyPlaying,
                        onClick = {
                            onSongClick(song)

                            val previewUrl = song.previewUrl
                            if (!previewUrl.isNullOrEmpty()) {
                                mediaPlayer?.release()
                                mediaPlayer = MediaPlayer().apply {
                                    setDataSource(previewUrl)
                                    setOnPreparedListener {
                                        currentProgress = 0f
                                        duration = this.duration
                                        start()
                                        isPlaying = true
                                    }
                                    setOnCompletionListener {
                                        isPlaying = false
                                        playingSongTitle = null
                                        currentProgress = 0f
                                    }
                                    prepareAsync()
                                    playingSongTitle = song.title
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
                        cardColor = cardColor
                    )
                }
            }
        } else {
            Text(
                text = "No hay canciones guardadas.",
                color = Color.White,
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
    cardColor: Color
) {
    val artistName = Regex("""name=([^,}]+)""").find(song.artist)?.groupValues?.get(1) ?: "Artista desconocido"

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
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
                        painter = rememberImagePainter(song.imageUrl),
                        contentDescription = "Song image",
                        modifier = Modifier
                            .size(64.dp)
                            .padding(end = 16.dp),
                        contentScale = ContentScale.Crop
                    )
                    Column(
                        modifier = Modifier.align(Alignment.CenterVertically)
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
