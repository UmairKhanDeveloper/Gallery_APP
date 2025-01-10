package com.example.galleryapp

import android.Manifest
import android.annotation.SuppressLint
import android.content.ContentUris
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import coil.compose.AsyncImage
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : ComponentActivity() {
    private var photosItems: List<Pair<String, String>> by mutableStateOf(emptyList())
    private var permissionGranted by mutableStateOf(false)

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        requestPermission()
        setContent {
            if (permissionGranted) {
                NavEntry(photosItems)
            } else {

            }
        }
    }

    fun requestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissionGaranted(this, Manifest.permission.READ_MEDIA_IMAGES) { isGranted ->
                if (isGranted) {
                    photosItems = getPhotosList(applicationContext)
                    permissionGranted = true
                } else {
                    registerActivityResult.launch(Manifest.permission.READ_MEDIA_IMAGES)
                }
            }
        } else {
            permissionGaranted(this, Manifest.permission.READ_EXTERNAL_STORAGE) { isGranted ->
                if (isGranted) {
                    photosItems = getPhotosList(applicationContext)
                    permissionGranted = true
                } else {
                    registerActivityResult.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                }
            }
        }
    }

    val registerActivityResult =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) {
                photosItems = getPhotosList(applicationContext)
                permissionGranted = true
            }
        }

    fun getPhotosList(context: Context): List<Pair<String, String>> {
        val mediaList = mutableListOf<Pair<String, String>>()
        val projection = arrayOf(
            MediaStore.MediaColumns._ID,
            MediaStore.MediaColumns.MIME_TYPE,
            MediaStore.MediaColumns.DATE_TAKEN
        )
        val sortOrder = "${MediaStore.MediaColumns.DATE_TAKEN} DESC"

        val contentResolver = context.contentResolver
        val cursor = contentResolver.query(
            MediaStore.Files.getContentUri("external"),
            projection,
            "${MediaStore.MediaColumns.MIME_TYPE} LIKE ? OR ${MediaStore.MediaColumns.MIME_TYPE} LIKE ?",
            arrayOf("image/%", "video/%"),
            sortOrder
        )

        cursor?.use {
            val idColumn = it.getColumnIndexOrThrow(MediaStore.MediaColumns._ID)
            val mimeTypeColumn = it.getColumnIndexOrThrow(MediaStore.MediaColumns.MIME_TYPE)
            val dateTakenColumn = it.getColumnIndexOrThrow(MediaStore.MediaColumns.DATE_TAKEN)

            while (it.moveToNext()) {
                val mediaId = it.getLong(idColumn)
                val mimeType = it.getString(mimeTypeColumn) ?: continue
                val dateTaken = it.getLong(dateTakenColumn)

                val contentUri = when {
                    mimeType.startsWith("image/") -> MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                    mimeType.startsWith("video/") -> MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                    else -> continue
                }

                val mediaUri = ContentUris.withAppendedId(contentUri, mediaId)

                val date = Date(dateTaken)
                val sdf = SimpleDateFormat("MMM-yyyy", Locale.ENGLISH)
                val monthAndYear = sdf.format(date)

                mediaList.add(Pair(mediaUri.toString(), monthAndYear))
            }
        } ?: Log.e("MediaList", "Cursor is null or empty")

        return mediaList
    }


    @RequiresApi(Build.VERSION_CODES.O)
    @OptIn(ExperimentalMaterial3Api::class)
    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    @Composable
    fun Photos(photos: List<Pair<String, String>>) {
        val context = LocalContext.current

        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(24.dp),
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            items(photos) { media ->
                val mediaUri = media.first
                val mimeType = getMimeType(context, Uri.parse(mediaUri))

                if (mimeType != null && mimeType.startsWith("image/")) {
                    AsyncImage(
                        model = mediaUri,
                        contentDescription = "",
                        modifier = Modifier
                            .clickable { }
                            .aspectRatio(1f)
                            .size(120.dp),
                        contentScale = ContentScale.Crop,
                        placeholder = painterResource(id = R.drawable.ic_launcher_foreground)
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .clickable { }
                            .aspectRatio(1f)
                            .size(120.dp)
                            .background(Color.Gray),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "File Icon",
                            tint = Color.White
                        )
                    }
                }
            }
        }
    }


    fun getMimeType(context: Context, uri: Uri): String? {
        return context.contentResolver.getType(uri)
    }
}

inline fun permissionGaranted(context: Context, permission: String, call: (Boolean) -> Unit) {
    if (ContextCompat.checkSelfPermission(
            context, permission
        ) == PackageManager.PERMISSION_GRANTED
    ) {
        call.invoke(true)
    } else {
        call.invoke(false)
    }
}

fun getVideoThumbnail(context: Context, videoUri: Uri): Bitmap? {
    return try {
        val retriever = MediaMetadataRetriever()
        retriever.setDataSource(context, videoUri)
        val bitmap = retriever.frameAtTime
        retriever.release()
        bitmap
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}


fun getMimeType(context: Context, uri: Uri): String? {
    return try {
        val contentResolver = context.contentResolver
        contentResolver.getType(uri)
    } catch (e: Exception) {
        Log.e("GetMimeTypeError", "Error getting MIME type: ${e.message}")
        null
    }
}

