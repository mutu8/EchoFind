// Archivo: com.example.echofind.data.test.LoginViewModel.kt

package com.example.echofind.data.viewmodel

import android.media.MediaPlayer
import android.util.Base64
import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.echofind.data.model.player.TrackItem
import com.example.echofind.data.service.AuthService
import com.example.echofind.data.service.SpotifyService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.nio.charset.StandardCharsets

class LoginSpotifyViewModel : ViewModel() {

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
                val playlistTracks = spotifyViewModel.getPlaylistTracks(token, playlistId)
                _tracks.clear()
                if (playlistTracks != null) {
                    _tracks.addAll(playlistTracks)
                }
                callback()
            } catch (e: Exception) {
                e.printStackTrace()
                callback()
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
    inner class SpotifyViewModel : ViewModel() {
        private val retrofit = Retrofit.Builder()
            .baseUrl("https://api.spotify.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        private val spotifyService = retrofit.create(SpotifyService::class.java)

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
