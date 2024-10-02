package com.example.echofind.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
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
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.echofind.data.viewmodel.LoginSpotifyViewModel
import com.example.echofind.ui.components.forScreens.BottomNavigationBar
import com.example.echofind.data.navbar.NavigationItem
import com.example.echofind.ui.screens.TwyperPreview
import androidx.compose.foundation.layout.systemBarsPadding
import com.example.echofind.data.viewmodel.AuthViewModel
import com.example.echofind.ui.screens.ProfileScreen
import com.example.echofind.ui.screens.Song
import com.example.echofind.ui.screens.SongListScreen
import com.example.echofind.ui.screens.sampleSongs

@Composable
fun NavManager() {
    val context = LocalContext.current
    val dataStore = StoreBoarding(context)
    val store = dataStore.getBoarding.collectAsState(initial = false)

    val navController = rememberNavController()

    // Obtener una instancia del LoginViewModel
    val loginSpotifyViewModel: LoginSpotifyViewModel = viewModel()
    val loginAppViewModel: AuthViewModel = viewModel()

    // Verificar si la ruta actual es TwyperPreview para mostrar la barra de navegación
    val navBackStackEntry = navController.currentBackStackEntryAsState().value
    val currentRoute = navBackStackEntry?.destination?.route

    Scaffold(
        bottomBar = {
            if (currentRoute == NavigationItem.TwyperPreview.route || currentRoute == "profileScreen" || currentRoute == "songListScreen") {
                BottomNavigationBar(navController)
            }
        },
        modifier = Modifier.systemBarsPadding() // Ajuste para evitar superposición con la barra del sistema
    ) { innerPadding ->
        // innerPadding contiene los valores de padding generados por Scaffold
        NavHost(
            navController = navController,
            startDestination = "Splash",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("OnBoarding") {
                MainOnBoarding(navController, dataStore)
            }
            composable("Login") {
                LoginScreen(navController)
            }
            composable("Splash") {
                SplashScreen(navController, store.value)
            }
            composable(NavigationItem.TwyperPreview.route) {
                TwyperPreview(loginSpotifyViewModel = loginSpotifyViewModel)
            }
            composable("register") {
                RegisterScreen(navController)
            }
            // Nueva ruta para ProfileScreen
            composable("profileScreen") {
                ProfileScreen(navController, loginAppViewModel)
            }
            // Nueva ruta para ProfileScreen
            composable("songListScreen") {
                SongListScreen(
                    navController = navController,
                    songs = sampleSongs,  // Lista de canciones definida antes
                    onSongClick = {
                    }
                )
            }
        }
    }
}
