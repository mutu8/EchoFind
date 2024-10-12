package com.example.echofind.data.viewmodel

import android.media.MediaPlayer
import androidx.lifecycle.ViewModel

class MediaPlayerViewModel : ViewModel() {
    var mediaPlayer: MediaPlayer? = null
        private set

    fun startPlayback(previewUrl: String, onCompletion: () -> Unit) {
        mediaPlayer?.release()
        mediaPlayer = MediaPlayer().apply {
            setDataSource(previewUrl)
            setOnPreparedListener {
                start()
            }
            setOnCompletionListener {
                onCompletion()
            }
            prepareAsync()
        }
    }

    fun stopPlayback() {
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
    }

    override fun onCleared() {
        super.onCleared()
        mediaPlayer?.release() // Liberar el MediaPlayer al limpiar el ViewModel
    }
}
