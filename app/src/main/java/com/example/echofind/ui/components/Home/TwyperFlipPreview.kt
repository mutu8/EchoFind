package com.example.echofind.ui.components.Home

import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import coil.compose.rememberImagePainter
import com.example.echofind.data.model.player.Track
import com.example.echofind.di.SpotifyAuth
import com.example.echofind.ui.theme.rememberRandomColor
import com.example.echofind.utils.SpotifyService
import com.github.theapache64.twyper.Twyper
import com.github.theapache64.twyper.rememberTwyperController
import com.google.android.exoplayer2.MediaItem
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

@Preview
@Composable
fun TwyperPreview() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val exoPlayer = rememberExoPlayer(context)
    var tracks by remember { mutableStateOf<List<Track>>(emptyList()) }
    val twyperController = rememberTwyperController()

    LaunchedEffect(Unit) {
        scope.launch {
            try {
                val token = SpotifyAuth.getAccessToken(context)
                val retrofit = Retrofit.Builder()
                    .baseUrl("https://api.spotify.com/")
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()
                val service = retrofit.create(SpotifyService::class.java)
                val response = service.getFavoriteTracks("Bearer $token")
                if (response.isSuccessful) {
                    tracks = response.body()?.items ?: emptyList()
                    if (tracks.isNotEmpty()) {
                        tracks[0].preview_url?.let { previewUrl ->
                            exoPlayer.setMediaItem(MediaItem.fromUri(previewUrl))
                            exoPlayer.prepare()
                            exoPlayer.play()
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Twyper(
            items = tracks,
            twyperController = twyperController,
            onItemRemoved = { track, direction ->
                val index = tracks.indexOf(track)
                if (index != -1) {
                    val nextIndex = (index + 1) % tracks.size
                    tracks[nextIndex].preview_url?.let { previewUrl ->
                        exoPlayer.setMediaItem(MediaItem.fromUri(previewUrl))
                        exoPlayer.prepare()
                        exoPlayer.play()
                    }
                }
            },
            onEmpty = {
                println("End reached")
            }
        ) { track ->
            Box(
                modifier = Modifier
                    .size(300.dp)
                    .background(
                        brush = Brush.horizontalGradient(
                            listOf(
                                rememberRandomColor(),
                                rememberRandomColor(),
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Column {
                    Text(text = track.name, fontSize = 20.sp, color = Color.White)
                    Text(text = track.artists.joinToString { it.name }, fontSize = 16.sp, color = Color.White)
                    track.album.images.firstOrNull()?.url?.let { imageUrl ->
                        Image(
                            painter = rememberAsyncImagePainter(imageUrl),
                            contentDescription = null,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(50.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(30.dp),
        ) {
            IconButton(onClick = {
                twyperController.swipeLeft()
            }) {
                Text(text = "❌", fontSize = 30.sp)
            }

            IconButton(onClick = {
                twyperController.swipeRight()
            }) {
                Text(text = "✅", fontSize = 30.sp)
            }
        }
    }
}