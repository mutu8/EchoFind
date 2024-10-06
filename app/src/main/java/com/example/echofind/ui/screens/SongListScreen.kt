package com.example.echofind.ui.screens

import android.media.MediaPlayer
import androidx.compose.foundation.ExperimentalFoundationApi
import com.example.echofind.data.model.player.Song
import com.example.echofind.data.viewmodel.AuthViewModel
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

    // Reiniciar el progreso y gestionar la reproducción cada vez que cambie la canción
    LaunchedEffect(playingSongTitle) {
        if (mediaPlayer != null && isPlaying) {
            while (mediaPlayer?.isPlaying == true) {
                currentProgress = mediaPlayer?.currentPosition?.toFloat()?.div(duration) ?: 0f
                delay(100)
            }
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
                        authViewModel.eliminarCancionGuardada(it) // Llamar a la función de eliminación en el ViewModel
                        songs = songs.filter { song -> song != it } // Eliminar la canción de la lista local
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
                    SongCard(
                        song = song,
                        currentProgress = if (playingSongTitle == song.title) currentProgress else 0f,
                        isPlaying = playingSongTitle == song.title && isPlaying,
                        onClick = {
                            onSongClick(song)

                            val previewUrl = song.previewUrl
                            if (!previewUrl.isNullOrEmpty()) {
                                mediaPlayer?.release()
                                mediaPlayer = MediaPlayer().apply {
                                    setDataSource(previewUrl)
                                    setOnPreparedListener {
                                        currentProgress = 0f // Reiniciar el progreso al cambiar de canción
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
                            songToDelete = song // Asignar la canción seleccionada para eliminar
                            showDeleteDialog = true // Mostrar el popup
                        },
                        cardColor = if (playingSongTitle == song.title && isPlaying) Color.Green else Color.LightGray // Cambiar el color de la tarjeta si se está reproduciendo
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
    onLongClick: () -> Unit, // Añadir la función para el long click
    cardColor: Color
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick // Detectar el long click
            ),
        shape = RoundedCornerShape(12.dp),
        backgroundColor = cardColor, // Cambia el color de la tarjeta acorde a la reproducción
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
                            text = song.artist,
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
                    color = Color.Green
                )
            }
        }
    }
}


