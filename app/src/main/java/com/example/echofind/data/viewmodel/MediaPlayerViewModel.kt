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
    // Función para detener la reproducción
    fun stopPlayback() {
        mediaPlayer?.let {
            if (it.isPlaying) {
                it.stop()
            }
            it.reset()
            it.release()
        }
        mediaPlayer = null
    }

    // Función para liberar el MediaPlayer si se destruye la vista
    fun releaseMediaPlayer() {
        stopPlayback()
        mediaPlayer?.release()
        mediaPlayer = null
    }

    override fun onCleared() {
        super.onCleared()
        releaseMediaPlayer()
    }
}
