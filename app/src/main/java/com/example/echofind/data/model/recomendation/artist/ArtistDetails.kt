package com.example.echofind.data.model.recomendation.artist

data class ArtistDetails(
    val id: String,
    val name: String,
    val genres: List<String>,
    val popularity: Int,
    val followers: Followers,
    val images: List<ArtistImage>
)
