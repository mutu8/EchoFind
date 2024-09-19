package com.example.echofind.data.service

import com.example.echofind.data.model.player.PlaylistTracksResponse
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path
import retrofit2.http.Query

interface SpotifyService {
    @GET("v1/playlists/{playlist_id}/tracks")
    suspend fun getPlaylistTracks(
        @Header("Authorization") token: String,
        @Path("playlist_id") playlistId: String,
        @Query("limit") limit: Int = 100 // Puedes ajustar el límite según necesites
    ): PlaylistTracksResponse
}