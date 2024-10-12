package com.example.echofind.ui.components.forScreens

import android.media.MediaPlayer
import androidx.compose.runtime.*
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.platform.LocalLifecycleOwner
import com.example.echofind.data.viewmodel.MediaPlayerViewModel

@Composable
fun HandleLifecycleEvents(mediaPlayerViewModel: MediaPlayerViewModel) {
    val lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_PAUSE -> {
                    // Detener el MediaPlayer al minimizar la app
                    mediaPlayerViewModel.stopPlayback()
                }
                Lifecycle.Event.ON_STOP -> {
                    // O puedes detenerlo en ON_STOP según tus necesidades
                    mediaPlayerViewModel.stopPlayback()
                }
                else -> { /* Otros eventos */ }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }
}
