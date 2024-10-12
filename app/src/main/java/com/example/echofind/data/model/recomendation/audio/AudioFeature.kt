package com.example.echofind.data.model.recomendation.audio

data class AudioFeature(
    val id: String,
    val danceability: Double,
    val energy: Double,
    val valence: Double,
    val tempo: Double,
    val speechiness: Double,
    val acousticness: Double,
    val instrumentalness: Double,
    val liveness: Double,
    val loudness: Double,
    val mode: Int,
    val key: Int,
    val time_signature: Int,
    // Añade más propiedades si las necesitas
)