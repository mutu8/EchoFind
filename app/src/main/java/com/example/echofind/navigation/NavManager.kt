// Archivo: com.example.echofind.navigation.NavManager.kt
package com.example.echofind.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.echofind.data.model.dataStore.StoreBoarding
import com.example.echofind.ui.components.OnBoarding.MainOnBoarding
import com.example.echofind.ui.screens.LoginScreen
import com.example.echofind.ui.screens.RegisterScreen
import com.example.echofind.ui.screens.SplashScreen
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.echofind.data.viewmodel.LoginSpotifyViewModel
import com.example.echofind.ui.screens.TwyperPreview

@Composable
fun NavManager(){
    val context = LocalContext.current
    val dataStore = StoreBoarding(context)
    val store = dataStore.getBoarding.collectAsState(initial = false)

    val navController = rememberNavController()

    // Obtener una instancia del LoginViewModel
    val loginSpotifyViewModel: LoginSpotifyViewModel = viewModel()

    NavHost(navController, startDestination = "Splash") {
        composable("OnBoarding") {
            MainOnBoarding(navController, dataStore)
        }
        composable("Login") {
            LoginScreen(navController)
        }
        composable("Splash") {
            SplashScreen(navController, store.value)
        }
        // Reemplazamos TwyperPreview con la nueva TwyperScreen
        composable("TwyperPreview") {
            TwyperPreview(loginSpotifyViewModel = loginSpotifyViewModel)
        }
        composable("register") {
            RegisterScreen(navController)
        }
    }
}
