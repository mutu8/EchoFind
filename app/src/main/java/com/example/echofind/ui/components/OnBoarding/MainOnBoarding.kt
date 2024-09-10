package com.example.echofind.ui.components.OnBoarding

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavController
import com.example.echofind.R
import com.example.echofind.data.model.PageData
import com.example.echofind.data.model.dataStore.StoreBoarding
import com.google.accompanist.pager.ExperimentalPagerApi

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MainOnBoarding(navController: NavController, store: StoreBoarding) {
    val items = ArrayList<PageData>()

    items.add(
        PageData(
            R.raw.page1,
            tittle = "Encuentra tu sonido",
            desc = "Explora nuevas canciones basadas en tus gustos y descubre artistas emergentes."
        )
    )

    items.add(
        PageData(
            R.raw.page2,
            tittle = "Playlists personalizadas",
            desc = "Crea y guarda listas de reproducción únicas y accede a ellas desde cualquier dispositivo."
        )
    )

    items.add(
        PageData(
            R.raw.page3,
            tittle = "Conecta con Spotify",
            desc = "Sincroniza tu cuenta de Spotify y disfruta de una experiencia musical sin interrupciones."
        )
    )

    val pagerState = rememberPagerState { 3 }

    OnBoardingPager(item = items, pagerState = pagerState, modifier = Modifier
        .fillMaxWidth()
        .fillMaxSize()
        .background(Color.White), navController, store)


}