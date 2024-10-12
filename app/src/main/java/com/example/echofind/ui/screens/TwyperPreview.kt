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
import coil.compose.AsyncImage
import coil.compose.rememberAsyncImagePainter
import com.example.echofind.R
import com.example.echofind.data.model.dataStore.UserDataStore
import com.example.echofind.data.model.player.TrackItem
import com.example.echofind.data.viewmodel.AuthViewModel
import com.github.theapache64.twyper.Twyper
import com.github.theapache64.twyper.rememberTwyperController
import com.example.echofind.data.viewmodel.LoginSpotifyViewModel
import com.example.echofind.data.viewmodel.LoginSpotifyViewModelFactory
import com.example.echofind.data.viewmodel.MediaPlayerViewModel
import com.example.echofind.ui.components.forScreens.HandleLifecycleEvents
import com.github.theapache64.twyper.SwipedOutDirection
import kotlinx.coroutines.launch

// Define la fuente personalizada utilizando el archivo de la fuente
val customFontFamily = FontFamily(
    Font(R.font.montserrat) // Fuente personalizada Montserrat
)

@Composable
fun TwyperPreview(
    loginSpotifyViewModel: LoginSpotifyViewModel = viewModel(),
    authViewModel: AuthViewModel = viewModel(),
    mediaPlayerViewModel: MediaPlayerViewModel = viewModel(), // Añadir MediaPlayerViewModel
    playlistId: String = "37i9dQZF1DXdpy4ZQQMZKm" // Playlist por defecto
) {
    HandleLifecycleEvents(mediaPlayerViewModel) // Se encarga de pausar el MediaPlayer

    // Inicializamos con las pistas de la playlist
    val items = remember { mutableStateListOf<TrackItem>() }
    var tracks by remember { mutableStateOf<List<TrackItem>?>(null) }
    val twyperController = rememberTwyperController()

    // Declaración de contadores de interacción y coroutineScope
    var swipes by remember { mutableIntStateOf(0) }
    var likes by remember { mutableIntStateOf(0) }
    var dislikes by remember { mutableIntStateOf(0) }
    val coroutineScope = rememberCoroutineScope()

    // Definición de userDataStore con manejo de nullabilidad
    val context = LocalContext.current
    val userId = authViewModel.getUserId()
    val userDataStore: UserDataStore? = userId?.let { UserDataStore.getInstance(context, it) }

    val allEvaluatedIds = remember { mutableStateOf<Set<String>>(emptySet()) }

    // Definir el color verde oscuro
    val spotifyGreen = Color(0xFF1DB954)
    // Estado de carga
    var isLoading by remember { mutableStateOf(false) }

    // Inicializar el ViewModel de Spotify con el ViewModel de autenticación
    val loginSpotifyViewModel: LoginSpotifyViewModel = viewModel(
        factory = LoginSpotifyViewModelFactory(authViewModel)
    )

    // Función para reproducir la canción seleccionada usando el MediaPlayerViewModel
    fun playTrack() {
        val previewUrl = items.firstOrNull()?.preview_url
        if (previewUrl != null) {
            mediaPlayerViewModel.startPlayback(previewUrl) {
                // Acción al terminar la canción
                if (items.isNotEmpty()) {
                    items.removeAt(0)
                    if (items.isNotEmpty()) {
                        playTrack()
                    } else {
                        Log.d("TwyperPreview", "No hay más canciones.")
                    }
                }
            }
        }
    }

    // Función para cargar más recomendaciones
    fun loadMoreRecommendations() {
        isLoading = true
        loginSpotifyViewModel.obtenerRecomendacionesActualizadas(loginSpotifyViewModel.getToken()) { recomendaciones ->
            isLoading = false
            if (recomendaciones.isNotEmpty()) {
                // Filtrar canciones ya presentadas
                val nuevasRecomendaciones = recomendaciones.filterNot { track ->
                    authViewModel.getPresentedTrackIds().contains(track.id)
                }

                if (nuevasRecomendaciones.isNotEmpty()) {
                    val nuevasRecomendacionesShuffled = nuevasRecomendaciones.shuffled()
                    tracks = tracks?.plus(nuevasRecomendacionesShuffled) ?: nuevasRecomendacionesShuffled
                    items.addAll(nuevasRecomendacionesShuffled)
                    authViewModel.agregarCancionesPresentadas(nuevasRecomendacionesShuffled)
                } else {
                    Log.d("TwyperPreview", "No hay más recomendaciones.")
                }
            }
        }
    }

    LaunchedEffect(items.size) {
        if (items.size <= 3) {
            loadMoreRecommendations()
        }
    }

    // Cargar valores iniciales desde DataStore al iniciar
    LaunchedEffect(userDataStore) {
        userDataStore?.userInteractions?.collect { interactions ->
            swipes = interactions["swipes"] ?: 0
            likes = interactions["likes"] ?: 0
            dislikes = interactions["dislikes"] ?: 0
            Log.d("UserDataStore", "Cargando: Swipes - $swipes, Likes - $likes, Dislikes - $dislikes")
        }
    }

    LaunchedEffect(Unit) {
        allEvaluatedIds.value = authViewModel.getAllEvaluatedTrackIds()
    }

    // Guardar los valores actualizados de los contadores en DataStore
    LaunchedEffect(swipes, likes, dislikes) {
        coroutineScope.launch {
            userDataStore?.saveUserInteraction(swipes, likes, dislikes)
            Log.d("UserDataStore", "Guardando: Swipes - $swipes, Likes - $likes, Dislikes - $dislikes")
        }
    }

    // Limpiar MediaPlayer al destruir el Composable
    DisposableEffect(Unit) {
        onDispose {
            mediaPlayerViewModel.stopPlayback() // Usar el ViewModel para liberar el MediaPlayer
        }
    }

    // Cargar las pistas al iniciar la pantalla y reproducir la primera pista automáticamente
    LaunchedEffect(Unit) {
        if (loginSpotifyViewModel.getToken().isEmpty()) {
            loginSpotifyViewModel.login("5da23dd12217412ab1a9088cb6c6280f", "dc01fd5a2eeb4257982cab398b35599e") { success ->
                if (success) {
                    loginSpotifyViewModel.getPlaylistTracks(loginSpotifyViewModel.getToken(), playlistId) {
                        tracks = loginSpotifyViewModel.tracks
                        tracks = tracks?.shuffled()
                        if (!tracks.isNullOrEmpty()) {
                            items.addAll(tracks!!.take(5))
                            loadMoreRecommendations()
                            playTrack() // Comienza a reproducir la primera pista en 'items'
                        }
                    }
                } else {
                    Log.e("TwyperPreview", "Error: No se pudo obtener el token.")
                }
            }
        } else {
            loginSpotifyViewModel.getPlaylistTracks(loginSpotifyViewModel.getToken(), playlistId) {
                tracks = loginSpotifyViewModel.tracks
                tracks = tracks?.shuffled()
                if (!tracks.isNullOrEmpty()) {
                    items.addAll(tracks!!.take(5))
                    loadMoreRecommendations()
                    playTrack() // Llamada sin argumentos
                }
            }
        }
    }

    // Interfaz de usuario
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFF1B1B1B), Color(0xFF000000))
                )
            )
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        if (!tracks.isNullOrEmpty()) {
            Twyper(
                items = items,
                twyperController = twyperController,
                onItemRemoved = { track, direction ->
                    mediaPlayerViewModel.stopPlayback()

                    if (direction == SwipedOutDirection.RIGHT) {
                        likes++
                        swipes++
                        coroutineScope.launch {
                            authViewModel.guardarCancionSeleccionada(track)
                            authViewModel.agregarCancionesPresentadas(listOf(track))
                            playTrack()
                        }
                    } else if (direction == SwipedOutDirection.LEFT) {
                        dislikes++
                        swipes++
                        coroutineScope.launch {
                            authViewModel.guardarCancionDislikeada(track)
                            authViewModel.agregarCancionesPresentadas(listOf(track))
                            playTrack()
                        }
                    }
                    items.remove(track)
                    if (items.isNotEmpty()) {
                        playTrack()
                    } else {
                        Log.d("TwyperPreview", "Se han terminado las canciones.")
                    }
                }
            ) { track ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    val albumImageUrl = track.album.images.firstOrNull()?.url
                    if (albumImageUrl != null) {
                        AsyncImage(
                            model = albumImageUrl,
                            contentDescription = "Imagen del álbum",
                            modifier = Modifier.size(300.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = track.name,
                        fontSize = 24.sp,
                        color = Color.Black,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        fontFamily = customFontFamily,
                        modifier = Modifier.widthIn(max = 280.dp)
                    )

                    Text(
                        text = track.artists.joinToString(", ") { it.name },
                        fontSize = 18.sp,
                        color = Color.Black,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        fontFamily = customFontFamily,
                        modifier = Modifier.widthIn(max = 280.dp)
                    )

                    Spacer(modifier = Modifier.height(5.dp))
                }
            }

            Spacer(modifier = Modifier.height(50.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(30.dp)) {
                // Botón ❌ (Dislike)
                IconButton(
                    onClick = {
                        mediaPlayerViewModel.stopPlayback()
                        dislikes++
                        swipes++
                        val track = items.firstOrNull()
                        if (track != null) {
                            coroutineScope.launch {
                                authViewModel.guardarCancionDislikeada(track)
                                items.removeAt(0)
                                authViewModel.agregarCancionesPresentadas(listOf(track))
                                playTrack()
                            }
                        }
                    },
                    modifier = Modifier
                        .size(40.dp)
                        .background(spotifyGreen, shape = CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Dislike",
                        tint = Color.White,
                        modifier = Modifier.size(30.dp)
                    )
                }

                // Botón Like
                IconButton(
                    onClick = {
                        mediaPlayerViewModel.stopPlayback()
                        likes++
                        swipes++
                        val track = items.firstOrNull()
                        if (track != null) {
                            coroutineScope.launch {
                                authViewModel.guardarCancionSeleccionada(track)
                                items.removeAt(0)
                                authViewModel.agregarCancionesPresentadas(listOf(track))
                                playTrack()
                            }
                        }
                    },
                    modifier = Modifier
                        .size(40.dp)
                        .background(spotifyGreen, shape = CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Like",
                        tint = Color.White,
                        modifier = Modifier.size(30.dp)
                    )
                }
            }
        } else {
            Text(text = "Cargando pistas...", fontSize = 18.sp, color = Color.White, fontFamily = customFontFamily)
        }
    }
}
