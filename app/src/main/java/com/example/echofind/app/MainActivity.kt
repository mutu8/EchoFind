package com.example.echofind.app

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.echofind.data.viewmodel.LoginSpotifyViewModel
import com.example.echofind.navigation.NavManager
import com.example.echofind.ui.theme.EchoFindTheme
import com.spotify.sdk.android.auth.AuthorizationClient
import com.spotify.sdk.android.auth.AuthorizationResponse
import kotlin.system.exitProcess

class MainActivity : ComponentActivity() {
    private val loginSpotifyViewModel: LoginSpotifyViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Mantener la pantalla encendida mientras la actividad está activa
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        enableEdgeToEdge()
        setContent {
            EchoFindTheme {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    NavManager()
                }
            }
        }
    }

    private val REQUEST_CODE = 1337

    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        super.onActivityResult(requestCode, resultCode, intent)
        if (requestCode == REQUEST_CODE) {
            val response = AuthorizationClient.getResponse(resultCode, intent)
            when (response.type) {
                AuthorizationResponse.Type.TOKEN -> {
                    val accessToken = response.accessToken
                    // Almacena el token de acceso en tu ViewModel
                    loginSpotifyViewModel.initiateSpotifyLogin(this)
                    loginSpotifyViewModel.setAccessToken(accessToken)
                }
                AuthorizationResponse.Type.ERROR -> {
                    Log.e("LoginSpotify", "Error en la autenticación: ${response.error}")
                }
                else -> { }
            }
        }
    }

    // Finalizar la aplicación completamente al ser minimizada o puesta en segundo plano
    override fun onStop() {
        super.onStop()
        finish() // Finaliza la actividad actual
        exitProcess(0) // Termina el proceso de la aplicación completamente
    }
}
