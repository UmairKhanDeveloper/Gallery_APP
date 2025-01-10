package com.example.galleryapp


import android.annotation.SuppressLint
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Photo
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material.icons.outlined.Photo
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.VideoLibrary
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController


@Composable
fun Navigation(navController: NavHostController, photosItems: List<Pair<String, String>>) {
    NavHost(navController = navController, startDestination = Screens.PhotoScreen.route) {
        composable(Screens.PhotoScreen.route) {
            PhotoScreen(navController, photosItems)
        }
        composable(Screens.VideosScreen.route) {
            VideosScreen(navController, photosItems)
        }
        composable(Screens.AlbumsScreen.route) {
            AlbumsScreen(navController)
        }
        composable(Screens.SettingScreen.route) {
            SettingScreen()
        }
        composable(Screens.photoDetailsScreen.route) {
            SettingScreen()
        }
    }
}

sealed class Screens(
    val route: String,
    val title: String,
    val SelectedIcon: ImageVector,
    val unSelectedIcon: ImageVector,
) {
    object PhotoScreen : Screens(
        "PhotoScreen",
        "PhotoScreen",
        SelectedIcon = Icons.Filled.PhotoCamera,
        unSelectedIcon = Icons.Outlined.PhotoCamera
    )

    object VideosScreen : Screens(
        "VideosScreen",
        "VideosScreen",
        SelectedIcon = Icons.Filled.VideoLibrary,
        unSelectedIcon = Icons.Outlined.VideoLibrary
    )

    object AlbumsScreen : Screens(
        "AlbumsScreen",
        "AlbumsScreen",
        SelectedIcon = Icons.Filled.Photo,
        unSelectedIcon = Icons.Outlined.Photo
    )

    object SettingScreen : Screens(
        "SettingScreen",
        "SettingScreen",
        SelectedIcon = Icons.Filled.Settings,
        unSelectedIcon = Icons.Outlined.Settings
    ) object photoDetailsScreen : Screens(
        "photoDetailsScreen",
        "photoDetailsScreen",
        SelectedIcon = Icons.Filled.Settings,
        unSelectedIcon = Icons.Outlined.Settings
    )
}

@Composable
fun ButtonNavigation(navController: NavController) {
    val items = listOf(
        Screens.PhotoScreen,
        Screens.VideosScreen,
        Screens.AlbumsScreen,
        Screens.SettingScreen,
    )

    NavigationBar(containerColor = Color(0xFFCBC3C3)) {
        val navStack by navController.currentBackStackEntryAsState()
        val current = navStack?.destination?.route

        items.forEach {
            NavigationBarItem(selected = current == it.route, onClick = {
                navController.navigate(it.route) {
                    navController.graph?.let {
                        it.route?.let { it1 ->
                            popUpTo(it1)
                            launchSingleTop = true
                            restoreState = true
                        }

                    }
                }
            }, icon = {
                if (current == it.route) {
                    Icon(imageVector = it.SelectedIcon, contentDescription = "")
                } else {
                    Icon(imageVector = it.unSelectedIcon, contentDescription = "")
                }


            })
        }

    }

}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun NavEntry(photosItems: List<Pair<String, String>>) {
    val navController = rememberNavController()
    Scaffold(bottomBar = {
        ButtonNavigation(navController = navController)
    }) {
        Navigation(navController,photosItems)


    }
}

