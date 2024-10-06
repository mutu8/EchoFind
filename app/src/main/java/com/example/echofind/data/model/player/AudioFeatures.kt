package com.example.echofind.data.model.player

data class AudioFeatures(
    val tempo: Double,       // Ritmo en beats por minuto
    val energy: Double,      // Nivel de energía (0 a 1)
    val valence: Double,     // Nivel de positividad (0 a 1)
    val danceability: Double // Bailabilidad (0 a 1)
)