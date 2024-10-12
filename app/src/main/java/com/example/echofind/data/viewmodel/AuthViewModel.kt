package com.example.echofind.data.viewmodel
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.echofind.data.model.firebase.AuthState
import com.example.echofind.data.model.player.Album
import com.example.echofind.data.model.player.AlbumImage
import com.example.echofind.data.model.player.Artist
import com.example.echofind.data.model.player.Song
import com.example.echofind.data.model.player.TrackItem
import com.example.echofind.data.service.SpotifyService
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class AuthViewModel : ViewModel() {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance() // Inicializa FirebaseAuth
    private val db = FirebaseFirestore.getInstance() // Inicializa Firestore

    private val _authState = MutableLiveData<AuthState>() // LiveData para observar el estado de autenticación
    val authState: LiveData<AuthState> = _authState // LiveData para observar el estado de autenticación

    private val _username = MutableLiveData<String>() // LiveData para observar el nombre de usuario
    val username: LiveData<String> get() = _username // LiveData para observar el nombre de usuario

    private val presentedTrackIds = mutableSetOf<String>()

    // Inicializar el ViewModel
    init {
        checkAuthStatus()
    }

    // Nueva función para verificar el estado de autenticación
    private fun checkAuthStatus() {
        // Verificar si el usuario está autenticado
        if (auth.currentUser == null) {
            _authState.value = AuthState.Unauthenticated
        } else {
            _authState.value = AuthState.Authenticated
        }
    }

    // Nueva función para obtener el userId del usuario autenticado
    fun getUserId(): String? {
        return auth.currentUser?.uid
    }

    // Nueva función para obtener el nombre de usuario desde Firestore
    fun fetchUsername() {
        val userId = getUserId() ?: return
        // Obtener el nombre de usuario desde Firestore
        db.collection("users")
            .document(userId)
            .get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val username = document.getString("username") ?: "Sin Nombre"
                    _username.value = username
                } else {
                    Log.w("Firestore", "Documento del usuario no existe")
                }
            }
            .addOnFailureListener { e ->
                Log.w("Firestore", "Error al obtener el nombre de usuario: ", e)
            }
    }

    fun login(email: String, password: String) {
        viewModelScope.launch {
            try {
                val result = auth.signInWithEmailAndPassword(email, password).await()
                if (result.user != null) {
                    _authState.value = AuthState.Authenticated
                } else {
                    _authState.value = AuthState.Error("Incorrect credentials")
                }
            } catch (e: Exception) {
                if (e.message?.contains("There is no user record corresponding to this identifier") == true) {
                    _authState.value = AuthState.Error("User does not exist")
                } else {
                    _authState.value = AuthState.Error("Incorrect credentials")
                }
            }
        }
    }

    fun signup(email: String, password: String, user: String) {
        if (email.isEmpty() || password.isEmpty() || user.isEmpty()) {
            _authState.value = AuthState.Error("Any value is empty")
            return
        }
        _authState.value = AuthState.Loading
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val firebaseUser = auth.currentUser
                    val userId = firebaseUser?.uid
                    if (userId != null) {
                        val userMap = hashMapOf(
                            "userId" to userId,
                            "email" to email,
                            "username" to user
                        )
                        FirebaseFirestore.getInstance().collection("users").document(userId)
                            .set(userMap)
                            .addOnSuccessListener {
                                _authState.value = AuthState.Authenticated
                            }
                            .addOnFailureListener { e: Exception ->
                                _authState.value =
                                    AuthState.Error(e.message ?: "Something went wrong")
                            }
                    } else {
                        _authState.value = AuthState.Error("User ID is null")
                    }
                } else {
                    _authState.value =
                        AuthState.Error(task.exception?.message ?: "Something went wrong")
                }
            }
    }
    // Nueva función para cerrar sesión
    fun signout() {
        auth.signOut()
        _authState.value = AuthState.Unauthenticated
    }
    // Función para agregar IDs de canciones presentadas
    fun agregarCancionesPresentadas(tracks: List<TrackItem>) {
        tracks.forEach { track ->
            presentedTrackIds.add(track.id)
        }
    }

    // Obtener el conjunto de IDs de canciones presentadas
    fun getPresentedTrackIds(): Set<String> {
        return presentedTrackIds
    }

    // Guardar una canción seleccionada (likeada)
    fun guardarCancionSeleccionada(trackItem: TrackItem) {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            val userId = currentUser.uid
            val cancionMap = hashMapOf(
                "trackId" to trackItem.id,
                "nombre" to trackItem.name,
                "preview_url" to trackItem.preview_url,
                "album" to trackItem.album.images.firstOrNull()?.url,
                "artistas" to trackItem.artists.map { artist ->
                    mapOf(
                        "id" to artist.id,
                        "name" to artist.name
                    )
                },
                "popularity" to trackItem.popularity,
                "timestamp" to System.currentTimeMillis()
            )

            db.collection("users")
                .document(userId)
                .collection("canciones_seleccionadas")
                .add(cancionMap)
                .addOnSuccessListener {
                    Log.d("Firestore", "Canción guardada exitosamente: ${trackItem.name}")
                }
                .addOnFailureListener { e ->
                    Log.w("Firestore", "Error al guardar canción: ", e)
                }
        } else {
            Log.w("Firestore", "Usuario no autenticado")
        }
    }

    // Guardar una canción dislikeada
    fun guardarCancionDislikeada(trackItem: TrackItem) {
        val currentUser = auth.currentUser

        if (currentUser != null) {
            val userId = currentUser.uid
            val cancionMap = hashMapOf(
                "trackId" to trackItem.id,
                "nombre" to trackItem.name,
                "preview_url" to trackItem.preview_url,
                "album" to trackItem.album.images.firstOrNull()?.url,
                "artistas" to trackItem.artists.map { artist ->
                    mapOf(
                        "id" to artist.id,
                        "name" to artist.name
                    )
                },
                "popularity" to trackItem.popularity,
                "timestamp" to System.currentTimeMillis()
            )

            db.collection("users")
                .document(userId)
                .collection("canciones_dislikeadas")
                .add(cancionMap)
                .addOnSuccessListener {
                    Log.d("Firestore", "Canción dislikeada guardada exitosamente: ${trackItem.name}")
                }
                .addOnFailureListener { e ->
                    Log.w("Firestore", "Error al guardar canción dislikeada: ", e)
                }
        } else {
            Log.w("Firestore", "Usuario no autenticado")
        }
    }


    // Nueva función para obtener las canciones guardadas
    fun obtenerCancionesGuardadas(callback: (List<Song>) -> Unit) {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            val userId = currentUser.uid
            db.collection("users")
                .document(userId)
                .collection("canciones_seleccionadas")
                .get()
                .addOnSuccessListener { documents ->
                    val canciones = documents.map { document ->

                        Song(
                            title = document.getString("nombre") ?: "",
                            artist = (document.get("artistas") as? List<String>)?.joinToString(", ") ?: "",
                            imageUrl = document.getString("album") ?: "",
                            previewUrl = document.getString("preview_url") ?: "",// Obtener el preview_url
                        )
                    }
                    callback(canciones) // Retorna las canciones guardadas
                }
                .addOnFailureListener { e ->
                    Log.w("Firestore", "Error al obtener canciones: ", e)
                }
        }
    }

    suspend fun getAllEvaluatedTrackIds(): Set<String> {
        val currentUser = auth.currentUser
        val allTrackIds = mutableSetOf<String>()
        if (currentUser != null) {
            val userId = currentUser.uid

            // Obtenemos los IDs de canciones likeadas
            val cancionesSeleccionadasDocs = db.collection("users")
                .document(userId)
                .collection("canciones_seleccionadas")
                .get()
                .await()
            allTrackIds.addAll(cancionesSeleccionadasDocs.documents.mapNotNull { it.getString("trackId") })

            // Obtenemos los IDs de canciones dislikeadas
            val cancionesDislikeadasDocs = db.collection("users")
                .document(userId)
                .collection("canciones_dislikeadas")
                .get()
                .await()
            allTrackIds.addAll(cancionesDislikeadasDocs.documents.mapNotNull { it.getString("trackId") })
        }
        return allTrackIds
    }

    // Obtener todas las canciones dislikeadas
    suspend fun obtenerTodasLasCancionesDislikeadas(): List<TrackItem> {
        val currentUser = auth.currentUser ?: return emptyList()
        val userId = currentUser.uid

        return withContext(Dispatchers.IO) {
            val documents = db.collection("users")
                .document(userId)
                .collection("canciones_dislikeadas")
                .get()
                .await()

            documents.map { document ->
                val artistasData = document.get("artistas") as? List<Map<String, Any>>
                val artistas = artistasData?.map { artistMap ->
                    Artist(
                        id = artistMap["id"] as? String ?: "",
                        name = artistMap["name"] as? String ?: ""
                    )
                } ?: emptyList()

                TrackItem(
                    id = document.getString("trackId") ?: "",
                    name = document.getString("nombre") ?: "",
                    preview_url = document.getString("preview_url"),
                    album = Album(
                        images = listOf(
                            AlbumImage(
                                url = document.getString("album") ?: "",
                                height = 0,
                                width = 0
                            )
                        )
                    ),
                    artists = artistas,
                    popularity = (document.getLong("popularity")?.toInt()) ?: 0
                )
            }
        }
    }

    // Obtener todas las canciones seleccionadas (likeadas)
    suspend fun obtenerTodasLasCancionesSeleccionadas(): List<TrackItem> {
        val currentUser = auth.currentUser ?: return emptyList()
        val userId = currentUser.uid

        return withContext(Dispatchers.IO) {
            val documents = db.collection("users")
                .document(userId)
                .collection("canciones_seleccionadas")
                .get()
                .await()

            documents.map { document ->
                val artistasData = document.get("artistas") as? List<Map<String, Any>>
                val artistas = artistasData?.map { artistMap ->
                    Artist(
                        id = artistMap["id"] as? String ?: "",
                        name = artistMap["name"] as? String ?: ""
                    )
                } ?: emptyList()

                TrackItem(
                    id = document.getString("trackId") ?: "",
                    name = document.getString("nombre") ?: "",
                    preview_url = document.getString("preview_url"),
                    album = Album(
                        images = listOf(
                            AlbumImage(
                                url = document.getString("album") ?: "",
                                height = 0,
                                width = 0
                            )
                        )
                    ),
                    artists = artistas,
                    popularity = (document.getLong("popularity")?.toInt()) ?: 0
                )
            }
        }
    }

    fun getRecommendationsBasedOnLikes(
        token: String,
        selectedTracks: List<TrackItem>,
        dislikedTracks: List<TrackItem>,
        callback: (List<TrackItem>) -> Unit
    ) {
        viewModelScope.launch {
            try {
                if (token.isNullOrEmpty()) {
                    Log.e("AuthViewModel", "El token de acceso es nulo o vacío")
                    callback(emptyList())
                    return@launch
                }
                val retrofit = Retrofit.Builder()
                    .baseUrl("https://api.spotify.com/")
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()

                val spotifyService = retrofit.create(SpotifyService::class.java)

                // Filtrar canciones likeadas con preview_url
                val likedTracksWithPreview = selectedTracks.filter { it.preview_url != null }
                // Filtrar IDs de tracks válidos
                val likedTrackIds = likedTracksWithPreview.mapNotNull { it.id.takeIf { id -> id.isNotBlank() } }

                // Obtener IDs de canciones dislikeadas
                val dislikedTrackIds = dislikedTracks.mapNotNull { it.id.takeIf { id -> id.isNotBlank() } }

                // Variables para semillas
                var seedTracks = mutableListOf<String>()
                var seedArtists = mutableListOf<String>()
                var seedGenres = mutableListOf<String>()

                if (likedTrackIds.isNotEmpty()) {
                    seedTracks.addAll(likedTrackIds.take(2)) // Limitar a 2 IDs
                }

                // Obtener artistas de canciones likeadas
                val likedArtists = selectedTracks.flatMap { it.artists }
                    .filter { it.id.isNotEmpty() }
                    .distinctBy { it.id }
                val likedArtistIds = likedArtists.mapNotNull { it.id.takeIf { id -> id.isNotBlank() } }

                if (likedArtistIds.isNotEmpty()) {
                    seedArtists.addAll(likedArtistIds.take(2)) // Limitar a 2 IDs
                }

                // Obtener géneros de los artistas likeados
                val artistGenres = mutableSetOf<String>()
                if (likedArtistIds.isNotEmpty()) {
                    for (artist in likedArtists) {
                        val artistId = artist.id
                        if (artistId.isNullOrBlank()) {
                            Log.e("AuthViewModel", "Artist ID is null or blank, skipping.")
                            continue
                        }
                        try {
                            val artistDetails = spotifyService.getArtist("Bearer $token", artistId)
                            artistGenres.addAll(artistDetails.genres)
                        } catch (e: HttpException) {
                            val errorBody = e.response()?.errorBody()?.string()
                            Log.e(
                                "AuthViewModel",
                                "HTTP Error al obtener detalles del artista $artistId: ${e.code()} - $errorBody"
                            )
                        } catch (e: Exception) {
                            Log.e(
                                "AuthViewModel",
                                "Error obteniendo detalles del artista $artistId: ${e.message}"
                            )
                        }
                    }
                    seedGenres.addAll(artistGenres.take(1)) // Limitar a 1 género
                }

                // Si no hay semillas, usar géneros disponibles
                if (seedTracks.isEmpty() && seedArtists.isEmpty() && seedGenres.isEmpty()) {
                    val availableGenresResponse = spotifyService.getAvailableGenreSeeds("Bearer $token")
                    val availableGenres = availableGenresResponse.genres

                    if (availableGenres.isNotEmpty()) {
                        // Seleccionar 5 géneros aleatorios
                        val randomGenres = availableGenres.shuffled().take(5)
                        seedGenres.addAll(randomGenres)
                    } else {
                        Log.e("AuthViewModel", "No hay géneros disponibles para usar como semillas")
                        callback(emptyList())
                        return@launch
                    }
                }

                // Limitar el total de semillas a 5
                val totalSeeds = (seedTracks + seedArtists + seedGenres).distinct().take(5)

                // Separar las semillas finales
                val finalSeedTracks = totalSeeds.filter { seedTracks.contains(it) }.joinToString(",").takeIf { it.isNotEmpty() }
                val finalSeedArtists = totalSeeds.filter { seedArtists.contains(it) }.joinToString(",").takeIf { it.isNotEmpty() }
                val finalSeedGenres = totalSeeds.filter { seedGenres.contains(it) }.joinToString(",").takeIf { it.isNotEmpty() }

                // Asegurarse de que al menos una semilla está presente
                if (finalSeedTracks.isNullOrEmpty() && finalSeedArtists.isNullOrEmpty() && finalSeedGenres.isNullOrEmpty()) {
                    Log.e("AuthViewModel", "No se pueden obtener recomendaciones sin semillas")
                    callback(emptyList())
                    return@launch
                }

                // Agregar logs para depuración
                Log.d("AuthViewModel", "finalSeedTracks: $finalSeedTracks")
                Log.d("AuthViewModel", "finalSeedArtists: $finalSeedArtists")
                Log.d("AuthViewModel", "finalSeedGenres: $finalSeedGenres")

                // Obtener características de audio de las canciones likeadas
                val audioFeatures = if (likedTrackIds.isNotEmpty()) {
                    try {
                        val audioFeaturesResponse = spotifyService.getAudioFeatures(
                            authorization = "Bearer $token",
                            trackIds = likedTrackIds.take(100).joinToString(",")
                        )
                        audioFeaturesResponse.audio_features.filterNotNull()
                    } catch (e: Exception) {
                        Log.e(
                            "AuthViewModel",
                            "Error obteniendo características de audio: ${e.message}"
                        )
                        emptyList()
                    }
                } else {
                    emptyList()
                }

                // Calcular promedios de características si hay datos
                val averageDanceability = audioFeatures.map { it.danceability }.average().takeIf { !it.isNaN() }
                val averageEnergy = audioFeatures.map { it.energy }.average().takeIf { !it.isNaN() }
                val averageValence = audioFeatures.map { it.valence }.average().takeIf { !it.isNaN() }

                // Validar características de audio
                val validatedDanceability = averageDanceability?.takeIf { it in 0.0..1.0 }
                val validatedEnergy = averageEnergy?.takeIf { it in 0.0..1.0 }
                val validatedValence = averageValence?.takeIf { it in 0.0..1.0 }

                // Agregar logs para depuración
                Log.d("AuthViewModel", "validatedDanceability: $validatedDanceability")
                Log.d("AuthViewModel", "validatedEnergy: $validatedEnergy")
                Log.d("AuthViewModel", "validatedValence: $validatedValence")

                // Obtener recomendaciones con las semillas finales
                try {
                    val response = spotifyService.getRecommendations(
                        authorization = "Bearer $token",
                        seedTracks = finalSeedTracks,
                        seedArtists = finalSeedArtists,
                        seedGenres = finalSeedGenres,
                        targetDanceability = validatedDanceability,
                        targetEnergy = validatedEnergy,
                        targetValence = validatedValence,
                        limit = 50
                    )

                    // Procesar y filtrar las recomendaciones
                    val recommendedTracks = response.tracks.map { track ->
                        TrackItem(
                            id = track.id,
                            name = track.name,
                            preview_url = track.preview_url,
                            album = Album(
                                images = track.album.images.map { image ->
                                    AlbumImage(
                                        url = image.url,
                                        height = image.height,
                                        width = image.width
                                    )
                                }
                            ),
                            artists = track.artists.map { artist ->
                                Artist(id = artist.id, name = artist.name)
                            },
                            popularity = track.popularity
                        )
                    }

                    // Eliminar duplicados basados en el ID de la canción
                    val uniqueRecommendedTracks = recommendedTracks.distinctBy { it.id }

                    // Filtrar canciones sin preview_url
                    val tracksWithPreview = uniqueRecommendedTracks.filter { it.preview_url != null }

                    // Combinar IDs de canciones likeadas, dislikeadas y presentadas
                    val idsCancionesAFiltrar = selectedTracks.map { it.id } +
                            dislikedTracks.map { it.id } +
                            presentedTrackIds.toList()

                    val filteredRecommendations = tracksWithPreview.filterNot { recommendedTrack ->
                        idsCancionesAFiltrar.contains(recommendedTrack.id)
                    }

                    // Verificar si hay recomendaciones disponibles
                    if (filteredRecommendations.isEmpty()) {
                        Log.e("AuthViewModel", "No hay más recomendaciones disponibles.")
                    }

                    callback(filteredRecommendations)
                } catch (e: HttpException) {
                    val errorBody = e.response()?.errorBody()?.string()
                    Log.e("AuthViewModel", "HTTP Error: ${e.code()} - $errorBody")
                    callback(emptyList())
                } catch (e: Exception) {
                    Log.e("AuthViewModel", "Error en getRecommendationsBasedOnLikes: ${e.message}")
                    callback(emptyList())
                }

            } catch (e: Exception) {
                e.printStackTrace()
                Log.e("AuthViewModel", "Error en getRecommendationsBasedOnLikes: ${e.message}")
                callback(emptyList())
            }
        }
    }



    // Generar y filtrar recomendaciones
    fun generarYFiltrarRecomendaciones(token: String, callback: (List<TrackItem>) -> Unit) {
        viewModelScope.launch {
            val cancionesSeleccionadas = obtenerTodasLasCancionesSeleccionadas()
            val cancionesDislikeadas = obtenerTodasLasCancionesDislikeadas()
            getRecommendationsBasedOnLikes(
                token = token,
                selectedTracks = cancionesSeleccionadas,
                dislikedTracks = cancionesDislikeadas
            ) { recomendacionesFiltradas ->
                callback(recomendacionesFiltradas)
            }
        }
    }

    // Función para obtener las IDs de canciones likeadas y dislikeadas
    suspend fun obtenerIdsCancionesLikeadasYDislikeadas(): Pair<List<String>, List<String>> {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            val userId = currentUser.uid

            val cancionesLikeadasRef = db.collection("users")
                .document(userId)
                .collection("canciones_seleccionadas")

            val cancionesDislikeadasRef = db.collection("users")
                .document(userId)
                .collection("canciones_dislikeadas")

            return withContext(Dispatchers.IO) {
                try {
                    val cancionesLikeadasDocs = cancionesLikeadasRef.get().await()
                    val cancionesDislikeadasDocs = cancionesDislikeadasRef.get().await()
                    val cancionesLikeadas = cancionesLikeadasDocs.documents.map { it.getString("trackId") ?: "" }
                    val cancionesDislikeadas = cancionesDislikeadasDocs.documents.map { it.getString("trackId") ?: "" }
                    Pair(cancionesLikeadas, cancionesDislikeadas)
                } catch (e: Exception) {
                    Log.e("AuthViewModel", "Error al obtener IDs de canciones: ${e.message}")
                    Pair(emptyList(), emptyList())
                }
            }
        } else {
            Log.w("AuthViewModel", "Usuario no autenticado")
            return Pair(emptyList(), emptyList())
        }
    }

    // Función para obtener las IDs de canciones likeadas y deslikeadas
    fun obtenerIdsCancionesLikeadasYDeslikeadas(callback: (List<String>, List<String>) -> Unit) {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            val userId = currentUser.uid

            val cancionesLikeadasRef = db.collection("users")
                .document(userId)
                .collection("canciones_seleccionadas")

            val cancionesDislikeadasRef = db.collection("users")
                .document(userId)
                .collection("canciones_dislikeadas")

            viewModelScope.launch {
                try {
                    val cancionesLikeadas = cancionesLikeadasRef.get().await().documents.map { it.getString("trackId") ?: "" }
                    val cancionesDislikeadas = cancionesDislikeadasRef.get().await().documents.map { it.getString("trackId") ?: "" }
                    callback(cancionesLikeadas, cancionesDislikeadas)
                } catch (e: Exception) {
                    Log.e("AuthViewModel", "Error al obtener IDs de canciones: ${e.message}")
                    callback(emptyList(), emptyList())
                }
            }
        } else {
            Log.w("AuthViewModel", "Usuario no autenticado")
            callback(emptyList(), emptyList())
        }
    }

    // Nueva función para eliminar una canción guardada
    fun eliminarCancionGuardada(song: Song) {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            val userId = currentUser.uid
            db.collection("users")
                .document(userId)
                .collection("canciones_seleccionadas")
                .whereEqualTo("nombre", song.title)
                .get()
                .addOnSuccessListener { documents ->
                    documents.forEach { document ->
                        db.collection("users")
                            .document(userId)
                            .collection("canciones_seleccionadas")
                            .document(document.id)
                            .delete()
                            .addOnSuccessListener {
                                Log.d("Firestore", "Canción eliminada exitosamente")
                            }
                            .addOnFailureListener { e ->
                                Log.w("Firestore", "Error al eliminar canción: ", e)
                            }
                    }
                }
                .addOnFailureListener { e ->
                    Log.w("Firestore", "Error al obtener canciones: ", e)
                }
        }
    }

}