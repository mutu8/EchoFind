package com.example.echofind.ui.screens

import androidx.navigation.NavHostController
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberImagePainter

data class Song(val title: String, val artist: String, val imageUrl: String)

val sampleSongs = listOf(
    Song(
        title = "Shape of You",
        artist = "Ed Sheeran",
        imageUrl = "https://example.com/shape_of_you.jpg"
    ),
    Song(
        title = "Blinding Lights",
        artist = "The Weeknd",
        imageUrl = "https://example.com/blinding_lights.jpg"
    ),
    Song(
        title = "Levitating",
        artist = "Dua Lipa",
        imageUrl = "https://example.com/levitating.jpg"
    ),
    Song(
        title = "Peaches",
        artist = "Justin Bieber",
        imageUrl = "https://example.com/peaches.jpg"
    ),
    Song(
        title = "Montero (Call Me By Your Name)",
        artist = "Lil Nas X",
        imageUrl = "https://example.com/montero.jpg"
    ),
    Song(
        title = "Good 4 U",
        artist = "Olivia Rodrigo",
        imageUrl = "https://example.com/good_4_u.jpg"
    ),
    Song(
        title = "Stay",
        artist = "The Kid LAROI, Justin Bieber",
        imageUrl = "https://example.com/stay.jpg"
    ),
    Song(
        title = "Save Your Tears",
        artist = "The Weeknd",
        imageUrl = "https://example.com/save_your_tears.jpg"
    ),
    Song(
        title = "Drivers License",
        artist = "Olivia Rodrigo",
        imageUrl = "https://example.com/drivers_license.jpg"
    )
)


@Composable
fun SongListScreen(navController: NavHostController, songs: List<Song>, onSongClick: (Song) -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black) // Aquí añades el fondo negro
            .padding(16.dp)
    ) {
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(songs.size) { index ->
                SongCard(song = songs[index], onClick = {
                    onSongClick(songs[index])
                    // Ejemplo de navegación, puedes ajustar según la estructura de tu app
                    navController.navigate("songDetail/${songs[index].title}")
                })
            }
        }
    }
}

@Composable
fun SongCard(song: Song, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        backgroundColor = Color.LightGray,
        elevation = 4.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Image(
                painter = rememberImagePainter(song.imageUrl),
                contentDescription = "Song image",
                modifier = Modifier
                    .size(64.dp)
                    .padding(end = 16.dp),
                contentScale = ContentScale.Crop
            )
            Column(
                modifier = Modifier
                    .align(Alignment.CenterVertically)
            ) {
                Text(
                    text = song.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = Color.Black // Cambié el color del texto a blanco para contraste
                )
                Text(
                    text = song.artist,
                    fontSize = 14.sp,
                    color = Color.Gray // Mantén el color gris o ajústalo según prefieras
                )
            }
        }
    }
}
