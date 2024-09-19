package com.example.echofind.data.model.player

data class TrackItem(
    val id: String,
    val name: String,
    val preview_url: String?,
    val album: Album, // Información del álbum
    val artists: List<Artist> // Lista de artistas
)