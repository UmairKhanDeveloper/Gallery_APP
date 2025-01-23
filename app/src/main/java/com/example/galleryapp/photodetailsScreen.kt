package com.example.galleryapp

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhotoDetailsScreen(navController: NavHostController, photosItems: List<Pair<String, String>>) {

    val context = LocalContext.current
    val encodedPhotoUri = navController.currentBackStackEntry?.arguments?.getString("photoUri")
    var editedText by remember { mutableStateOf("") }

    var dateFormatted by remember { mutableStateOf("No Date Available") }
    val photoUri = encodedPhotoUri?.let { Uri.decode(it) }

    LaunchedEffect(photoUri) {
        val dateTaken = photoUri?.let { getDateTaken(context, Uri.parse(it)) }
        dateFormatted = if (dateTaken != null) {
            try {
                SimpleDateFormat("MMM dd, yyyy hh:mm a", Locale.getDefault()).format(Date(dateTaken))
            } catch (e: Exception) {
                Log.e("PhotoDetailsScreen", "Error formatting date: ${e.message}")
                "Error Formatting Date"
            }
        } else {
            "No Date Available"
        }
    }

    var currentPhotoUri by remember { mutableStateOf<Uri?>(photoUri?.let { Uri.parse(it) }) }
    val editedImageUri = navController.currentBackStackEntry
        ?.savedStateHandle
        ?.get<String>("editedImageUri")
    LaunchedEffect(editedImageUri) {
        if (editedImageUri != null) {
            try {
                val newUri = Uri.parse(Uri.decode(editedImageUri))
                currentPhotoUri = newUri.buildUpon().appendQueryParameter("t", System.currentTimeMillis().toString()).build()
                Log.d("PhotoDetailsScreen", "Updated currentPhotoUri to $currentPhotoUri")
                navController.currentBackStackEntry?.savedStateHandle?.remove<String>("editedImageUri")
            } catch (e: Exception) {
                Log.e("PhotoDetailsScreen", "Error updating image URI: ${e.message}")
            }
        } else {
            currentPhotoUri = photoUri?.let { Uri.parse(it) }
        }
    }


    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = dateFormatted,
                        style = MaterialTheme.typography.bodySmall
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.Black
                        )
                    }
                },
                actions = {
                    Image(
                        painter = painterResource(id = R.drawable.image2),
                        contentDescription = "Share to Facebook",
                        modifier = Modifier
                            .size(42.dp)
                            .clickable {
                                currentPhotoUri?.let {
                                    val shareIntent = Intent().apply {
                                        action = Intent.ACTION_SEND
                                        putExtra(Intent.EXTRA_STREAM, it)
                                        type = "image/*"
                                    }
                                    val chooser = Intent.createChooser(shareIntent, "Share Photo")
                                    context.startActivity(chooser)
                                }
                            }
                    )
                    Image(
                        painter = painterResource(id = R.drawable.image1),
                        contentDescription = "Share to WhatsApp",
                        modifier = Modifier
                            .size(35.dp)
                            .clickable {
                                currentPhotoUri?.let {
                                    val shareIntent = Intent().apply {
                                        action = Intent.ACTION_SEND
                                        putExtra(Intent.EXTRA_STREAM, it)
                                        type = "image/*"
                                    }
                                    val chooser = Intent.createChooser(shareIntent, "Share Photo")
                                    context.startActivity(chooser)
                                }
                            }
                    )
                }
            )
        },
        bottomBar = {
            BottomAppBar {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.clickable {
                            currentPhotoUri?.let {
                                val shareIntent = Intent().apply {
                                    action = Intent.ACTION_SEND
                                    putExtra(Intent.EXTRA_STREAM, it)
                                    type = "image/*"
                                }
                                val chooser = Intent.createChooser(shareIntent, "Share Photo")
                                context.startActivity(chooser)
                            }
                        }) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "",
                            modifier = Modifier.size(16.dp)
                        )
                        Text(text = "Share", fontSize = 12.sp)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "",
                            modifier = Modifier
                                .size(16.dp)
                                .clickable {
                                    currentPhotoUri?.let {
                                        navController.navigate("photoEditorScreen/${Uri.encode(it.toString())}")
                                    }
                                }
                        )
                        Text(text = "Edit", fontSize = 12.sp)
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.QrCodeScanner,
                            contentDescription = "",
                            modifier = Modifier.size(16.dp)
                        )
                        Text(text = "Google Lens", fontSize = 12.sp)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(imageVector = Icons.Default.MoreVert, contentDescription = "")
                        Text(text = "More", fontSize = 12.sp)
                    }
                }
            }
        }
    ) { innerPadding ->
        if (currentPhotoUri != null) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                AsyncImage(
                    model = currentPhotoUri,
                    contentDescription = "Photo",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit,
                    placeholder = painterResource(id = R.drawable.ic_launcher_foreground)
                )

            }
        } else {
            Text("No Photo Available")
        }
    }
}


fun getDateTaken(context: Context, uri: Uri): Long? {
    val projection = arrayOf(MediaStore.Images.Media.DATE_TAKEN)
    context.contentResolver.query(uri, projection, null, null, null)?.use { cursor ->
        if (cursor.moveToFirst()) {
            val dateIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_TAKEN)
            return cursor.getLong(dateIndex)
        }
    }
    return null
}