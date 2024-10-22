package com.example.echofind.data.service

import com.example.echofind.data.model.player.PlaylistTracksResponse
import com.example.echofind.data.model.recomendation.artist.ArtistDetails
import com.example.echofind.data.model.recomendation.audio.AudioFeaturesResponse
import com.example.echofind.data.model.recomendation.RecommendationsResponse
import com.example.echofind.data.model.recomendation.artist.GenreSeedsResponse
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
    ): PlaylistTracksResponse;

    @GET("v1/recommendations")
    suspend fun getRecommendations(
        @Header("Authorization") authorization: String,
        @Query("seed_tracks") seedTracks: String? = null,
        @Query("seed_artists") seedArtists: String? = null,
        @Query("seed_genres") seedGenres: String? = null,
        @Query("target_danceability") targetDanceability: Double? = null,
        @Query("target_energy") targetEnergy: Double? = null,
        @Query("target_valence") targetValence: Double? = null,
        @Query("limit") limit: Int = 20
    ): RecommendationsResponse

    @GET("v1/audio-features")
    suspend fun getAudioFeatures(
        @Header("Authorization") authorization: String,
        @Query("ids") trackIds: String
    ): AudioFeaturesResponse

    @GET("v1/artists/{id}")
    suspend fun getArtist(
        @Header("Authorization") authorization: String,
        @Path("id") artistId: String
    ): ArtistDetails

    @GET("v1/recommendations/available-genre-seeds")
    suspend fun getAvailableGenreSeeds(
        @Header("Authorization") authorization: String
    ): GenreSeedsResponse
}