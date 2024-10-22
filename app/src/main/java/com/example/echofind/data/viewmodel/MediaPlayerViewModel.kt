package com.example.echofind.data.viewmodel

import android.media.MediaPlayer
import android.util.Log
import androidx.lifecycle.ViewModel

class MediaPlayerViewModel : ViewModel() {
    private var mediaPlayer: MediaPlayer? = null

    fun startPlayback(url: String, onCompletion: () -> Unit) {
        Log.d("MediaPlayerViewModel", "startPlayback: Iniciando la reproducción de $url")
        if (mediaPlayer == null) {
            mediaPlayer = MediaPlayer().apply {
                setDataSource(url)
                prepare()
                start()
                setOnCompletionListener {
                    Log.d("MediaPlayerViewModel", "startPlayback: Reproducción completada")
                    onCompletion()
                }
            }
        } else {
            mediaPlayer?.apply {
                reset()
                setDataSource(url)
                prepare()
                start()
                Log.d("MediaPlayerViewModel", "startPlayback: MediaPlayer reiniciado y reproduciendo $url")
            }
        }
    }

    fun stopPlayback() {
        Log.d("MediaPlayerViewModel", "stopPlayback: Deteniendo la reproducción")
        try {
            mediaPlayer?.apply {
                if (isPlaying) {
                    stop()
                    reset()
                }
            }
        } catch (e: IllegalStateException) {
            Log.e("MediaPlayerViewModel", "Error al detener la reproducción: ${e.message}")
        }
    }

    // Método para liberar el MediaPlayer
    fun releaseMediaPlayer() {
        Log.d("MediaPlayerViewModel", "releaseMediaPlayer: Liberando el MediaPlayer")
        mediaPlayer?.release()
        mediaPlayer = null
    }

    override fun onCleared() {
        Log.d("MediaPlayerViewModel", "onCleared: ViewModel limpiado, liberando el MediaPlayer")
        super.onCleared()
        releaseMediaPlayer()
    }
}
