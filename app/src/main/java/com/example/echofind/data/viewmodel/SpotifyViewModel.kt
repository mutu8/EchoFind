package com.example.echofind.data.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import com.example.echofind.data.model.player.AudioFeatures
import com.example.echofind.data.model.player.TrackItem
import com.example.echofind.data.service.SpotifyService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class SpotifyViewModel : ViewModel() {

    private val retrofit = Retrofit.Builder()
        .baseUrl("https://api.spotify.com/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val spotifyService = retrofit.create(SpotifyService::class.java)
    private var accessToken: String = ""

    fun setAccessToken(token: String) {
        accessToken = token
    }

    suspend fun getPlaylistTracks(token: String, playlistId: String, idsCancionesAFiltrar: List<String>): List<TrackItem>? {
        return withContext(Dispatchers.IO) {
            try {
                val response = spotifyService.getPlaylistTracks("Bearer $token", playlistId)
                response.items.map { it.track }
                    .filter { it.preview_url != null }
                    .filterNot { idsCancionesAFiltrar.contains(it.id) } // Filtrar canciones likeadas y deslikeadas
            } catch (e: Exception) {
                Log.e("SpotifyViewModel", "Error al obtener pistas: ${e.message}")
                null
            }
        }
    }


}
