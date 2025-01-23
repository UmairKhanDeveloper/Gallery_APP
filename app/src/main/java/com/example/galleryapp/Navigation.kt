package com.example.galleryapp


import android.annotation.SuppressLint
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Photo
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material.icons.outlined.Photo
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.VideoLibrary
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.google.gson.Gson

@Composable
fun Navigation(
    navController: NavHostController,
    photosItems: List<Pair<String, String>>,
    albumItems: List<Triple<String, String, List<String>>>
) {
    NavHost(navController = navController, startDestination = Screens.PhotoScreen.route) {
        composable(Screens.PhotoScreen.route) {
            PhotoScreen(navController, photosItems)
        }
        composable(Screens.AlbumsScreen.route) {
            AlbumsScreen(albums = albumItems, navController = navController)
        }
        composable(Screens.SettingScreen.route) {
            SettingScreen()
        }
        composable(
            "photoDetailsScreen/{photoUri}",
            arguments = listOf(navArgument("photoUri") { type = NavType.StringType })
        ) {
            PhotoDetailsScreen(navController = navController, photosItems)
        }
        composable("videoPlayerScreen/{videoUri}") { backStackEntry ->
            val videoUri = backStackEntry.arguments?.getString("videoUri") ?: ""
            VideoDetailsScreen(navController, videoUri)
        }
        composable("PhotoEditorScreen/{photoUri}") { backStackEntry ->
            val photoUri = backStackEntry.arguments?.getString("photoUri") ?: ""
            PhotoEditorScreen(navController, photoUri)
        }
        composable("albumDetailScreen/{albumName}/{albumImages}") { backStackEntry ->
            val albumName = backStackEntry.arguments?.getString("albumName") ?: "Unknown Album"
            val albumImagesJson = backStackEntry.arguments?.getString("albumImages") ?: "[]"
            val albumImages = Gson().fromJson(albumImagesJson, Array<String>::class.java).toList()

            AlbumsScreenDetails(albumName = albumName, albumImages = albumImages, navController = navController)
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
        SelectedIcon = Icons.Filled.Photo,
        unSelectedIcon = Icons.Outlined.Photo
    )

    object AlbumsScreen : Screens(
        "AlbumsScreen",
        "AlbumsScreen",
        SelectedIcon = Icons.Filled.VideoLibrary,
        unSelectedIcon = Icons.Outlined.VideoLibrary
    )

    object SettingScreen : Screens(
        "SettingScreen",
        "SettingScreen",
        SelectedIcon = Icons.Filled.Settings,
        unSelectedIcon = Icons.Outlined.Settings
    )

    object photoDetailsScreen : Screens(
        "photoDetailsScreen",
        "photoDetailsScreen",
        SelectedIcon = Icons.Filled.Settings,
        unSelectedIcon = Icons.Outlined.Settings
    )

    object VideoDetailsScreen : Screens(
        "VideoDetailsScreen",
        "VideoDetailsScreen",
        SelectedIcon = Icons.Filled.Settings,
        unSelectedIcon = Icons.Outlined.Settings
    )


    object PhotoEditorScreen : Screens(
        "PhotoEditorScreen",
        "PhotoEditorScreen",
        SelectedIcon = Icons.Filled.Settings,
        unSelectedIcon = Icons.Outlined.Settings
    )
    object AlbumsScreenDetails : Screens(
        "AlbumsScreenDetails",
        "AlbumsScreenDetails",
        SelectedIcon = Icons.Filled.Settings,
        unSelectedIcon = Icons.Outlined.Settings
    )
}

@Composable
fun ButtonNavigation(navController: NavController) {
    val items = listOf(
        Screens.PhotoScreen,
        Screens.AlbumsScreen,
        Screens.SettingScreen,
    )

    NavigationBar(
        containerColor = Color.Gray.copy(1f),
        modifier = Modifier.height(65.dp)
    ) {
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
                Image(
                    imageVector = if (current == it.route) it.SelectedIcon else it.unSelectedIcon,
                    contentDescription = it.title,
                    colorFilter = ColorFilter.tint(
                        if (current == it.route) Color.Black else Color.White
                    )
                )
            },
                colors = NavigationBarItemDefaults.colors(
                    indicatorColor = Color.White
                )
            )


        }

    }

}




@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun NavEntry(photosItems: List<Pair<String, String>>, albumItems: List<Triple<String, String, List<String>>>) {
    val navController = rememberNavController()
    var showBottomNav by remember { mutableStateOf(true) }
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route


    showBottomNav = when {
        currentRoute == null -> true
        currentRoute.contains(Screens.VideoDetailsScreen.route) -> false
        currentRoute.contains(Screens.photoDetailsScreen.route) -> false
        currentRoute.contains(Screens.PhotoEditorScreen.route) -> false
        else -> true
    }

    Scaffold(
        bottomBar = {
            if (showBottomNav) {
                ButtonNavigation(navController = navController)
            }
        }
    ) { innerPadding ->
        Navigation(navController, photosItems, albumItems)
    }
}