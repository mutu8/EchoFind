package com.example.echofind.data.navbar

import com.example.echofind.R

sealed class NavigationItem(var route: String, var icon: Int, var title: String) {
    data object TwyperPreview : NavigationItem(route = "twyperPreview", title = "Home", icon = R.drawable.ic_home)
    data object SongListScreen : NavigationItem(route = "songListScreen", title = "Songs", icon = R.drawable.ic_song)
    data object ProfileScreen : NavigationItem(route = "profileScreen", title = "Profile", icon = R.drawable.ic_profile)

}