package com.example.echofind.data.viewmodel

import android.app.Activity
import android.media.MediaPlayer
import android.util.Base64
import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.echofind.data.model.player.TrackItem
import com.example.echofind.data.service.AuthService
import com.spotify.sdk.android.auth.AuthorizationClient
import com.spotify.sdk.android.auth.AuthorizationRequest
import com.spotify.sdk.android.auth.AuthorizationResponse
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.nio.charset.StandardCharsets

class LoginSpotifyViewModel(
    private val authViewModel: AuthViewModel ) : ViewModel() {

    private val retrofit = Retrofit.Builder()
        .baseUrl("https://accounts.spotify.com/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val authService = retrofit.create(AuthService::class.java)
    private var accessToken: String = ""

    // Almacenamiento de las pistas en el ViewModel
    private val _tracks = mutableStateListOf<TrackItem>()
    val tracks: List<TrackItem> = _tracks

    // Variables para manejar caché de recomendaciones
    private var recomendacionesCache: List<TrackItem>? = null
    private var cacheTimestamp: Long = 0L

    companion object {
        private const val CLIENT_ID = "90df872385dc4d2384526261a76ddfe3" // Reemplaza con tu Client ID de Spotify
        private const val REDIRECT_URI = "http://testeo8m.my.canva.site/dagtckwngme" // Debe coincidir con el registrado en Spotify
    }
    private val REQUEST_CODE = 1337
    private var refreshToken: String = ""
    private var codeVerifier: String = ""

    fun initiateSpotifyLogin(activity: Activity) {
        val builder = AuthorizationRequest.Builder(
            CLIENT_ID,
            AuthorizationResponse.Type.TOKEN,
            REDIRECT_URI
        )
        builder.setScopes(arrayOf("user-read-private", "user-read-email")) // Agrega los scopes necesarios
        val request = builder.build()
        AuthorizationClient.openLoginActivity(activity, REQUEST_CODE, request)
    }


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

    fun setAccessToken(token: String) {
        accessToken = token
    }

    // Obtener recomendaciones actualizadas sin caché
    fun obtenerRecomendacionesActualizadas(token: String, callback: (List<TrackItem>) -> Unit) {
        authViewModel.generarYFiltrarRecomendaciones(token) { nuevasRecomendaciones ->
            callback(nuevasRecomendaciones)
        }
    }
    fun getPlaylistTracks(token: String, playlistId: String, callback: () -> Unit) {
        authViewModel.obtenerIdsCancionesLikeadasYDeslikeadas { idsLikeadas, idsDislikeadas ->
            val idsAFiltrar = idsLikeadas + idsDislikeadas

            viewModelScope.launch {
                try {
                    val spotifyViewModel = SpotifyViewModel()
                    val playlistTracks = spotifyViewModel.getPlaylistTracks(token, playlistId, idsAFiltrar)
                    _tracks.clear()

                    if (playlistTracks != null) {
                        _tracks.addAll(playlistTracks) // Agregar los tracks filtrados
                    }

                    callback()
                } catch (e: Exception) {
                    e.printStackTrace()
                    callback()
                }
            }
        }
    }

    fun obtenerRecomendacionesConCache(token: String, callback: (List<TrackItem>) -> Unit) {
        val currentTime = System.currentTimeMillis()
        val cacheDuration = 10 * 60 * 1000 // 10 minutos en milisegundos

        if (recomendacionesCache != null && (currentTime - cacheTimestamp) < cacheDuration) {

            // Usar caché si es válida
            callback(recomendacionesCache!!)
        } else {
            // Obtener nuevas recomendaciones usando el método actualizado
            authViewModel.generarYFiltrarRecomendaciones(token) { nuevasRecomendaciones ->
                if (nuevasRecomendaciones.isNotEmpty()) {
                    recomendacionesCache = nuevasRecomendaciones
                    cacheTimestamp = currentTime
                    callback(nuevasRecomendaciones)
                } else {
                    callback(emptyList())
                }
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


    }