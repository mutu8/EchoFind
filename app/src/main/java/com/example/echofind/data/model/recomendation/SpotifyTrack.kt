package com.example.echofind.data.model.recomendation

data class SpotifyTrack(
    val id: String,
    val name: String,
    val preview_url: String?,
    val album: SpotifyAlbum,
    val artists: List<SpotifyArtist>,
    val popularity: Int
)

