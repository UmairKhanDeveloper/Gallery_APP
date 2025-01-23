package com.example.galleryapp

import android.net.Uri
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhotoScreen(navController: NavHostController, initialPhotosItems: List<Pair<String, String>>) {
    var imageList by remember { mutableStateOf(initialPhotosItems) }
    val context = LocalContext.current


    val editedImageUri = navController.currentBackStackEntry
        ?.savedStateHandle
        ?.get<String>("editedImageUri")

    LaunchedEffect(editedImageUri) {
        editedImageUri?.let { uriString ->
            try {
                val decodedUri = Uri.decode(uriString)
                val photoUriArgument = navController.previousBackStackEntry?.arguments?.getString("photoUri")
                if (photoUriArgument != null) {
                    val photoUri = Uri.decode(photoUriArgument)
                    val index = imageList.indexOfFirst { Uri.decode(it.first) == photoUri }
                    if(index != -1) {
                        val updatedItem = decodedUri to imageList[index].second
                        val mutableList = imageList.toMutableList()
                        mutableList[index] = updatedItem
                        imageList = mutableList.toList()
                        Log.d("PhotoScreen", "Updated Image Uri at index $index, new Uri is $decodedUri")
                    } else {
                        Log.e("PhotoScreen", "photoUri not found in imageList")
                    }

                } else {
                    Log.e("PhotoScreen", "photoUriArgument is null")
                }
                navController.currentBackStackEntry?.savedStateHandle?.remove<String>("editedImageUri")

            } catch (e: Exception) {
                Log.e("PhotoScreen", "Error updating image URI: ${e.message}")
            }
        }

    }
    val groupedPhotos = imageList.groupBy { it.second }


    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 60.dp)
    ) {
        Text(
            text = "Photos",
            modifier = Modifier.align(Alignment.CenterHorizontally),
            fontSize = 40.sp,
            color = Color.Black
        )

        Spacer(modifier = Modifier.height(16.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(4),
            verticalArrangement = Arrangement.spacedBy(2.dp),
            horizontalArrangement = Arrangement.spacedBy(2.dp),
            modifier = Modifier
                .padding(2.dp)
                .fillMaxHeight()
        ) {
            groupedPhotos.forEach { (monthAndYear, photos) ->
                item(span = { GridItemSpan(4) }) {
                    Text(
                        text = monthAndYear,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp, horizontal = 16.dp),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                }

                items(photos, key = { it.first }) { (photoUri, _) ->
                    val mimeType = getMimeType(context, Uri.parse(photoUri))

                    Box(
                        modifier = Modifier
                            .aspectRatio(1f)
                            .clickable {
                                val encodedUri = Uri.encode(photoUri)
                                if (mimeType?.startsWith("video/") == true) {
                                    navController.navigate("videoPlayerScreen/$encodedUri")
                                } else {
                                    navController.navigate("photoDetailsScreen/$encodedUri")
                                }
                            }
                    ) {
                        if (mimeType != null && mimeType.startsWith("video/")) {
                            val thumbnail = getVideoThumbnail(context, Uri.parse(photoUri))
                            if (thumbnail != null) {
                                Image(
                                    bitmap = thumbnail.asImageBitmap(),
                                    contentDescription = "Video Thumbnail",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(Color.Black.copy(alpha = 0.3f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PlayArrow,
                                        contentDescription = "Play Video",
                                        tint = Color.White,
                                        modifier = Modifier.size(30.dp)
                                    )
                                }
                            } else {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(Color.Gray),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PlayArrow,
                                        contentDescription = "Play Video",
                                        tint = Color.White
                                    )
                                }
                            }
                        } else {
                            AsyncImage(
                                model = photoUri,
                                contentDescription = "Photo",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop,
                                placeholder = painterResource(id = R.drawable.ic_launcher_foreground)
                            )
                        }
                    }
                }
            }
        }
    }
}