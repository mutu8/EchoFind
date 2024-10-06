// Archivo: com.example.echofind.data.test.LoginViewModel.kt

package com.example.echofind.data.viewmodel

import android.annotation.SuppressLint
import android.media.MediaPlayer
import android.util.Base64
import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.echofind.data.model.player.AudioFeatures
import com.example.echofind.data.model.player.TrackItem
import com.example.echofind.data.service.AuthService
import com.example.echofind.data.service.SpotifyService
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.nio.charset.StandardCharsets

class LoginSpotifyViewModel(
    private val authViewModel: AuthViewModel
) : ViewModel() {

    private val retrofit = Retrofit.Builder()
        .baseUrl("https://accounts.spotify.com/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val authService = retrofit.create(AuthService::class.java)
    private var accessToken: String = ""

    // Almacenamiento de las pistas en el ViewModel
    private val _tracks = mutableStateListOf<TrackItem>()
    val tracks: List<TrackItem> = _tracks

    fun login(clientId: String, clientSecret: String, callback: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                // Codificar clientId y clientSecret en Base64
                val credentials = "$clientId:$clientSecret"
                val authHeader = "Basic " + Base64.encodeToString(credentials.toByteArray(StandardCharsets.UTF_8), Base64.NO_WRAP)

                // Hacer la solicitud de login
                val response = authService.login(authorization = authHeader)
                accessToken = response.access_token // Guardamos el token
                callback(true) // Login exitoso
            } catch (e: Exception) {
                e.printStackTrace()
                callback(false) // Login fallido
            }
        }
    }

    fun getToken(): String {
        return accessToken
    }

    // Función para obtener las pistas de una playlist y almacenarlas en el ViewModel
    fun getPlaylistTracks(token: String, playlistId: String, callback: () -> Unit) {
        viewModelScope.launch {
            try {
                val spotifyViewModel = SpotifyViewModel()

                // Obtener las pistas de la playlist
                val playlistTracks = spotifyViewModel.getPlaylistTracks(token, playlistId)

                if (playlistTracks != null) {
                    // Obtener las características de audio de las pistas en paralelo
                    getAudioFeaturesForTracks(token, playlistTracks) { tracksWithFeatures ->
                        _tracks.clear()
                        _tracks.addAll(tracksWithFeatures) // Añadir las pistas con características al ViewModel
                        callback() // Llamar al callback después de actualizar las pistas
                    }
                } else {
                    callback() // No se encontraron pistas en la playlist
                }
            } catch (e: Exception) {
                e.printStackTrace()
                callback() // Error
            }
        }
    }

    private fun getAudioFeaturesForTracks(token: String, trackItems: List<TrackItem>, callback: (List<TrackItem>) -> Unit) {
        viewModelScope.launch {
            try {
                val spotifyViewModel = SpotifyViewModel()

                // Iniciar todas las solicitudes de forma paralela usando async
                val tracksWithFeatures = trackItems.map { trackItem ->
                    async {
                        val audioFeatures = spotifyViewModel.getAudioFeatures(trackItem.id)
                        // Si las características se obtienen, devolver el TrackItem con las características añadidas
                        if (audioFeatures != null) {
                            trackItem.copy(audioFeatures = audioFeatures)
                        } else {
                            null // Si hay un error o no se obtienen las características, devolver null
                        }
                    }
                }.awaitAll().filterNotNull() // Esperar que todas las solicitudes finalicen y filtrar los resultados nulos

                // Devolver la lista de pistas con las características de audio agregadas
                callback(tracksWithFeatures)
            } catch (e: Exception) {
                e.printStackTrace()
                callback(emptyList()) // En caso de error, devolver una lista vacía
            }
        }
    }


    // Función para reproducir la vista previa de una pista y cambiar automáticamente al terminar
    fun playPreviewTrack(previewUrl: String, onCompletion: () -> Unit): MediaPlayer? {
        return try {
            val mediaPlayer = MediaPlayer().apply {
                setDataSource(previewUrl) // Configura la URL de la vista previa
                prepare() // Prepara el reproductor de audio
                start() // Reproduce la pista de vista previa
            }

            mediaPlayer.setOnCompletionListener {
                mediaPlayer.release() // Libera el reproductor cuando termine la reproducción
                onCompletion() // Ejecutar la acción de reproducción automática al finalizar
            }

            mediaPlayer // Devolver el MediaPlayer para poder controlarlo más adelante

        } catch (e: Exception) {
            Log.e("MediaPlayerError", "Error al reproducir la vista previa: ${e.message}")
            null // Retorna null si ocurre algún error
        }
    }

    // ViewModel interno para Spotify
    @SuppressLint("StaticFieldLeak")
    inner class SpotifyViewModel : ViewModel() {
        private val retrofit = Retrofit.Builder()
            .baseUrl("https://api.spotify.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        private val spotifyService = retrofit.create(SpotifyService::class.java)

        // Función para obtener las características de audio de una canción específica
        suspend fun getAudioFeatures(trackId: String): AudioFeatures? {
            return try {
                // Llamar al método de la instancia `spotifyService` en lugar de la interfaz
                spotifyService.getAudioFeatures("Bearer $accessToken", trackId)
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }

        // Función para obtener las pistas de una lista de reproducción
        suspend fun getPlaylistTracks(token: String, playlistId: String): List<TrackItem>? {
            return try {
                val response = spotifyService.getPlaylistTracks("Bearer $token", playlistId)
                response.items.map { it.track }.filter { it.preview_url != null }
            } catch (e: Exception) {
                e.printStackTrace()
                null // Error
            }
        }
    }
}
