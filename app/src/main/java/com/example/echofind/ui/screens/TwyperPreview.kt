// Archivo: com.example.echofind.ui.screens.TwyperScreen.kt
package com.example.echofind.ui.screens

import android.media.MediaPlayer
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import com.example.echofind.R
import com.example.echofind.data.model.dataStore.UserDataStore
import com.example.echofind.data.model.player.TrackItem
import com.example.echofind.data.viewmodel.AuthViewModel
import com.github.theapache64.twyper.Twyper
import com.github.theapache64.twyper.rememberTwyperController
import com.example.echofind.data.viewmodel.LoginSpotifyViewModel
import com.example.echofind.data.viewmodel.LoginSpotifyViewModelFactory
import com.github.theapache64.twyper.SwipedOutDirection
import kotlinx.coroutines.launch

// Define la fuente personalizada utilizando el archivo de la fuente
val customFontFamily = FontFamily(
    Font(R.font.montserrat) // Cambia 'my_custom_font' al nombre del archivo que agregaste en res/font
)

@Composable
fun TwyperPreview(
    loginSpotifyViewModel: LoginSpotifyViewModel = viewModel(),
    authViewModel: AuthViewModel = viewModel(), // Añadir AuthViewModel,
    playlistId: String = "37i9dQZF1DXdpy4ZQQMZKm" // Playlist por defecto
) {
    var mediaPlayer by remember { mutableStateOf<MediaPlayer?>(null) }
    var tracks by remember { mutableStateOf<List<TrackItem>?>(null) }
    var currentTrackIndex by remember { mutableIntStateOf(0) }
    val twyperController = rememberTwyperController()

    // Declaración de contadores de interacción y coroutineScope
    var swipes by remember { mutableStateOf(0) }
    var likes by remember { mutableStateOf(0) }
    var dislikes by remember { mutableStateOf(0) }
    val coroutineScope = rememberCoroutineScope()

    // Definición de userDataStore con manejo de nullabilidad
    val context = LocalContext.current
    val userId = authViewModel.getUserId()
    val userDataStore: UserDataStore? = userId?.let { UserDataStore.getInstance(context, it) }

    // Cargar valores iniciales desde DataStore al iniciar
    LaunchedEffect(userDataStore) {
        userDataStore?.userInteractions?.collect { interactions ->
            swipes = interactions["swipes"] ?: 0
            likes = interactions["likes"] ?: 0
            dislikes = interactions["dislikes"] ?: 0
            Log.d("UserDataStore", "Cargando: Swipes - $swipes, Likes - $likes, Dislikes - $dislikes")
        }
    }


    // Guardar los valores actualizados de los contadores en DataStore
    LaunchedEffect(swipes, likes, dislikes) {
        coroutineScope.launch {
            userDataStore?.saveUserInteraction(swipes, likes, dislikes)
            Log.d("UserDataStore", "Guardando: Swipes - $swipes, Likes - $likes, Dislikes - $dislikes")
        }
    }


    // Inicializar el ViewModel de Spotify con el ViewModel de autenticación
    val loginSpotifyViewModel: LoginSpotifyViewModel = viewModel(
        factory = LoginSpotifyViewModelFactory(authViewModel)
    )

    // Definir el color verde oscuro
    val spotifyGreen = Color(0xFF1DB954)

    // Inicializamos con las pistas de la playlist
    val items = remember { mutableStateListOf<TrackItem>() }

    DisposableEffect(Unit) {
        onDispose {
            mediaPlayer?.release() // Libera el MediaPlayer cuando el Composable es destruido
            mediaPlayer = null
        }
    }

    // Función para reproducir la canción seleccionada
    fun playTrack(previewUrl: String?) {
        mediaPlayer?.release() // Libera el reproductor anterior si existe
        if (previewUrl != null) {
            mediaPlayer = loginSpotifyViewModel.playPreviewTrack(previewUrl) {
                // Acción al terminar la canción: ir a la siguiente canción y actualizar tarjeta
                if (tracks != null && currentTrackIndex < tracks!!.size - 1) {
                    currentTrackIndex++
                    playTrack(tracks?.get(currentTrackIndex)?.preview_url)
                    // Actualizamos la tarjeta actual eliminando la tarjeta actual y añadiendo la nueva canción
                    items.removeAt(0) // Eliminar la tarjeta actual
                    if (currentTrackIndex < tracks!!.size) {
                        items.add(tracks!![currentTrackIndex]) // Añadir la siguiente canción como nueva tarjeta
                    }
                } else {
                    Log.d("P", "No hay más canciones.")
                }
            }
        }
    }

    // Cargar las pistas al iniciar la pantalla y reproducir la primera pista automáticamente
    LaunchedEffect(Unit) {
        if (loginSpotifyViewModel.getToken().isEmpty()) {
            loginSpotifyViewModel.login("5da23dd12217412ab1a9088cb6c6280f", "dc01fd5a2eeb4257982cab398b35599e") { success ->
                if (success) {
                    loginSpotifyViewModel.getPlaylistTracks(loginSpotifyViewModel.getToken(), playlistId) {
                        tracks = loginSpotifyViewModel.tracks
                        // Reordenamos aleatoriamente las pistas antes de añadirlas
                        tracks = tracks!!.shuffled()
                        if (!tracks.isNullOrEmpty()) {
                            items.addAll(tracks!!) // Añadir todas las pistas a las tarjetas
                            playTrack(tracks!![0].preview_url)
                        }
                    }
                } else {
                    Log.e("TwyperPreview", "Error: Could not obtain token.")
                }
            }
        } else {
            loginSpotifyViewModel.getPlaylistTracks(loginSpotifyViewModel.getToken(), playlistId) {
                tracks = loginSpotifyViewModel.tracks
                if (!tracks.isNullOrEmpty()) {
                        items.addAll(tracks!!) // Añadir todas las pistas a las tarjetas
                    playTrack(tracks!![0].preview_url)
                }
            }
        }
    }

    // Pantalla principal con fondo degradado oscuro
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF1B1B1B),   // Gris oscuro al inicio
                        Color(0xFF000000)    // Negro en la parte inferior
                    )
                )
            )
            .padding(16.dp), // Opcional: puedes ajustar el padding si quieres márgenes en los bordes
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        if (!tracks.isNullOrEmpty()) {
            Twyper(
                items = items,
                twyperController = twyperController,
                onItemRemoved = { track, direction ->
                    mediaPlayer?.release() // Detener la reproducción actual

                    // Verificar la dirección del swipe usando swipedOutDirection
                    if (twyperController.currentCardController?.swipedOutDirection == SwipedOutDirection.RIGHT) {
                        likes++
                        swipes++
                        // Si fue hacia la derecha, guardar la canción
                        authViewModel.guardarCancionSeleccionada(track)
                    }
                    if (twyperController.currentCardController?.swipedOutDirection == SwipedOutDirection.LEFT) {
                        dislikes++
                        swipes++
                    }

                        // Para ambos gestos (izquierda y derecha), reproducir la siguiente canción y actualizar la tarjeta
                    if (tracks != null && currentTrackIndex < tracks!!.size - 1) {
                        currentTrackIndex++
                        playTrack(tracks?.get(currentTrackIndex)?.preview_url)
                        items.remove(track) // Eliminar la tarjeta actual
                        if (currentTrackIndex < tracks!!.size) {
                            items.add(tracks!![currentTrackIndex]) // Añadir la siguiente canción como nueva tarjeta
                        }
                    } else {
                        Log.d("TwyperPreview", "No hay más canciones.")
                    }
                },
                onEmpty = {
                    Log.d("TwyperPreview", "Se han terminado las canciones.")
                }
            ) { track ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    // Imagen del álbum que ocupa toda la tarjeta
                    val albumImageUrl = track.album.images.firstOrNull()?.url
                    if (albumImageUrl != null) {
                        Image(
                            painter = rememberAsyncImagePainter(albumImageUrl),
                            contentDescription = "Album Image",
                            modifier = Modifier
                                .size(300.dp) // Tamaño de la tarjeta
                        )
                    }

                    // El texto está fuera de la tarjeta, alineado debajo de la imagen
                    Spacer(modifier = Modifier.height(8.dp))

                    // Mostrar el nombre de la canción con la fuente personalizada
                    Text(
                        text = track.name,
                        fontSize = 24.sp,
                        color = Color.Black, // Cambiado a blanco para mejor contraste con fondo oscuro
                        maxLines = 1, // Limitar a una línea
                        overflow = TextOverflow.Ellipsis, // Si es muy largo, agregar "..."
                        fontFamily = customFontFamily, // Aplicamos la fuente personalizada
                        modifier = Modifier.widthIn(max = 280.dp) // Limitar el ancho máximo para evitar deformar la tarjeta
                    )

                    // Mostrar los artistas con la fuente personalizada
                    Text(
                        text = track.artists.joinToString(", ") { it.name },
                        fontSize = 18.sp,
                        color = Color.Black, // Color claro para mejor visibilidad
                        maxLines = 1, // Limitar a una línea
                        overflow = TextOverflow.Ellipsis, // Si es muy largo, agregar "..."
                        fontFamily = customFontFamily, // Aplicamos la fuente personalizada
                        modifier = Modifier.widthIn(max = 280.dp) // Limitar el ancho máximo para evitar deformar la tarjeta
                    )
                    Spacer(modifier = Modifier.height(5.dp))
                }
            }

            Spacer(modifier = Modifier.height(50.dp))


            Row(
                horizontalArrangement = Arrangement.spacedBy(30.dp),
            ) {
                // Botón ❌ (Cerrar)
                IconButton(
                    onClick = {
                        mediaPlayer?.release() // Detener la reproducción actual
                        dislikes++
                        swipes++
                        if (tracks != null && currentTrackIndex < tracks!!.size - 1) {
                            currentTrackIndex++
                            playTrack(tracks?.get(currentTrackIndex)?.preview_url)
                            items.removeAt(0) // Eliminar la tarjeta actual
                            if (currentTrackIndex < tracks!!.size) {
                                items.add(tracks!![currentTrackIndex]) // Añadir la siguiente canción como nueva tarjeta
                            }
                        }
                    },
                    modifier = Modifier
                        .size(40.dp) // Tamaño del botón
                        .background(spotifyGreen, shape = CircleShape) // Fondo verde oscuro con forma circular
                ) {
                    // Ícono de "Cerrar" (X)
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = Color.White, // Color del ícono
                        modifier = Modifier.size(30.dp)
                    )
                }

                // Botón ✅ (Check)
                IconButton(
                    onClick = {
                        mediaPlayer?.release() // Detener la reproducción actual
                        likes++
                        swipes++
                        val track = tracks?.get(currentTrackIndex) // Obtener la pista actual
                        if (track != null) {
                            authViewModel.guardarCancionSeleccionada(track) // Guardar la canción seleccionada en Firestore
                        }
                        if (tracks != null && currentTrackIndex < tracks!!.size - 1) {
                            currentTrackIndex++
                            playTrack(tracks?.get(currentTrackIndex)?.preview_url)
                            items.removeAt(0) // Eliminar la tarjeta actual
                            if (currentTrackIndex < tracks!!.size) {
                                items.add(tracks!![currentTrackIndex]) // Añadir la siguiente canción como nueva tarjeta
                            }
                        }
                    },
                    modifier = Modifier
                        .size(40.dp) // Tamaño del botón
                        .background(spotifyGreen, shape = CircleShape) // Fondo verde oscuro con forma circular
                ) {
                    // Ícono de "Check"
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Check",
                        tint = Color.White, // Color del ícono
                        modifier = Modifier.size(30.dp)
                    )
                }
            }
        } else {
            // Mostrar un mensaje mientras se cargan las pistas
            Text(text = "Cargando pistas...", fontSize = 18.sp, color = Color.White, fontFamily = customFontFamily)
        }
    }
}
