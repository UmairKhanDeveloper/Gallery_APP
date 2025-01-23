package com.example.galleryapp

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.ImageDecoder
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Typeface
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Filter
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.RotateLeft
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.asAndroidColorFilter
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.image.cropview.CropType
import com.image.cropview.EdgeType
import com.image.cropview.ImageCrop
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhotoEditorScreen(navController: NavHostController, photoUri: String?) {
    val context = LocalContext.current
    var isEditModeActive by remember { mutableStateOf(false) }
    var isCropperActive by remember { mutableStateOf(false) }
    var currentImageUri by remember { mutableStateOf(photoUri) }
    var currentBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var previewBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var rotationAngle by remember { mutableFloatStateOf(0f) }
    var currentFilter by remember { mutableStateOf<ColorFilter?>(null) }
    var showFilters by remember { mutableStateOf(false) }
    var showTextDialog by remember { mutableStateOf(false) }
    var addedText by remember { mutableStateOf("") }
    var textColor by remember { mutableStateOf(Color.Black) }
    var textOffset by remember { mutableStateOf(Offset.Zero) }
    var textSize by remember { mutableFloatStateOf(14f) }
    var imageCrop by remember { mutableStateOf<ImageCrop?>(null) }
    var isTextClicked by remember { mutableStateOf(false) }
    var showToast by remember { mutableStateOf<String?>(null) }
    var isTextMoving by remember { mutableStateOf(false) }
    var croppedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var savedImageUri by remember { mutableStateOf<Uri?>(null) }
    var editedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var isCropPerformed by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    val filters = listOf(
        null,
        ColorFilter.lighting(Color.Gray, Color.LightGray),
        ColorFilter.colorMatrix(
            ColorMatrix(
                floatArrayOf(
                    0.393f, 0.769f, 0.189f, 0f, 0f,
                    0.349f, 0.686f, 0.168f, 0f, 0f,
                    0.272f, 0.534f, 0.131f, 0f, 0f,
                    0f, 0f, 0f, 1f, 0f
                )
            )
        ),
        ColorFilter.colorMatrix(ColorMatrix().apply { setToSaturation(0.20f) }),
        ColorFilter.tint(Color.Red.copy(alpha = 0.20f)),
        ColorFilter.tint(Color(0xFF00FF00).copy(alpha = 0.20f)),
        ColorFilter.tint(Color(0xFF0000FF).copy(alpha = 0.20f)),
        ColorFilter.colorMatrix(
            ColorMatrix(
                floatArrayOf(
                    1.5f, 0f, 0f, 0f, -40f,
                    0f, 1.5f, 0f, 0f, -40f,
                    0f, 0f, 1.5f, 0f, -40f,
                    0f, 0f, 0f, 1f, 0f
                )
            )
        ),
        ColorFilter.colorMatrix(
            ColorMatrix(
                floatArrayOf(
                    1f, 0f, 0f, 0f, 30f,
                    0f, 1f, 0f, 0f, 30f,
                    0f, 0f, 1f, 0f, 30f,
                    0f, 0f, 0f, 1f, 0f
                )
            )
        )
    )
    val colorList = listOf(
        Color.Black,
        Color.Red,
        Color.Blue,
        Color.Green,
        Color.Yellow,
        Color.Magenta,
        Color.Cyan,
        Color.White,
    )

    fun loadImage(uriString: String?) {
        uriString?.let { uri ->
            try {
                Log.d("PhotoEditorScreen", "Loading image from uri: $uri")
                val loadedBitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    ImageDecoder.decodeBitmap(
                        ImageDecoder.createSource(
                            context.contentResolver,
                            Uri.parse(uri)
                        )
                    )
                } else {
                    MediaStore.Images.Media.getBitmap(
                        context.contentResolver,
                        Uri.parse(uri)
                    )
                }
                currentBitmap = loadedBitmap.copy(Bitmap.Config.ARGB_8888, true)
                previewBitmap = currentBitmap?.copy(Bitmap.Config.ARGB_8888, true)

                croppedBitmap = null
                editedBitmap = null
                isCropPerformed = false
                Log.d("PhotoEditorScreen", "Image loaded successfully. Size: ${loadedBitmap.width}x${loadedBitmap.height}")
            } catch (e: Exception) {
                e.printStackTrace()
                Log.e("PhotoEditor", "Error loading image: ${e.message}")
                currentBitmap = null
                previewBitmap = null
                editedBitmap = null
                isCropPerformed = false
                showToast("Error Loading Image: ${e.message}",snackbarHostState)
            }
        } ?: run {
            currentBitmap = null
            previewBitmap = null
            editedBitmap = null
            isCropPerformed = false
            Log.d("PhotoEditorScreen", "Image URI is null.")
        }
    }

    LaunchedEffect(currentImageUri) {
        loadImage(currentImageUri)
    }



    fun applyTextToBitmap(
        bitmap: Bitmap,
        text: String,
        textColor: Color,
        textOffset: Offset,
        textSize: Float
    ): Bitmap? {
        if (bitmap == null || text.isEmpty()) return bitmap
        try {
            val config = Bitmap.Config.ARGB_8888
            val newBitmap = bitmap.copy(config, true)
            val canvas = Canvas(newBitmap)
            val paint = Paint().apply {
                color = textColor.toArgb()
                this.textSize = textSize
                typeface = Typeface.DEFAULT
                isAntiAlias = true
            }
            val textX = textOffset.x
            val textY = textOffset.y

            val textBounds = android.graphics.Rect()
            paint.getTextBounds(text, 0, text.length, textBounds)

            canvas.drawText(text, textX, textY, paint)

            return newBitmap
        } catch(e: Exception){
            Log.d("PhotoEditorScreen", "Exception while adding text : ${e.message}")
            return bitmap;
        }

    }

    fun applyFilterToBitmap(bitmap: Bitmap?, filter: ColorFilter?): Bitmap? {
        if (bitmap == null || filter == null) return bitmap
        return try {
            val filteredBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true)
            val canvas = android.graphics.Canvas(filteredBitmap)
            val paint = android.graphics.Paint()
            paint.colorFilter = filter.asAndroidColorFilter()
            canvas.drawBitmap(filteredBitmap, 0f, 0f, paint)
            return filteredBitmap
        } catch (e : Exception){
            Log.d("PhotoEditorScreen", "Exception while filtering the image : ${e.message}")
            return bitmap;
        }
    }

    fun getEditedBitmap(): Bitmap? {
        val tempBitmap =  if (isCropPerformed && croppedBitmap != null) {
            croppedBitmap!!.copy(Bitmap.Config.ARGB_8888, true)
        } else {
            currentBitmap?.copy(Bitmap.Config.ARGB_8888, true)
        } ?: return null

        var modifiedBitmap = tempBitmap
        modifiedBitmap = applyFilterToBitmap(modifiedBitmap, currentFilter) ?: modifiedBitmap
        modifiedBitmap =  applyTextToBitmap(modifiedBitmap, addedText, textColor, textOffset, textSize) ?: modifiedBitmap

        return modifiedBitmap
    }

    fun updatePreview() {
        val tempBitmap =  if (isCropPerformed && croppedBitmap != null) {
            croppedBitmap!!.copy(Bitmap.Config.ARGB_8888, true)
        } else {
            currentBitmap?.copy(Bitmap.Config.ARGB_8888, true)
        } ?: return

        var modifiedBitmap = tempBitmap
        modifiedBitmap = applyFilterToBitmap(modifiedBitmap, currentFilter) ?: modifiedBitmap
        modifiedBitmap =  applyTextToBitmap(modifiedBitmap, addedText, textColor, textOffset, textSize) ?: modifiedBitmap

        previewBitmap = if(isCropperActive) tempBitmap else modifiedBitmap
    }


    LaunchedEffect(isCropperActive, currentFilter, addedText, textOffset, textSize, isCropPerformed) {
        updatePreview()
    }

    fun rotateImage(): Bitmap? {
        return currentBitmap?.let {
            rotateBitmap(context,it,rotationAngle)?.also {
                currentBitmap = it
            }
        }
    }

    fun rotateCroppedImage(): Bitmap? {
        return croppedBitmap?.let {
            rotateBitmap(context,it,rotationAngle)?.also {
                croppedBitmap = it
            }
        }
    }

    fun saveImageAndNavigate() {
        val bitmapToSave = getEditedBitmap() ?: return
        saveBitmapToFile(context, bitmapToSave)?.let { uri ->
            showToast("Image saved successfully!",snackbarHostState)
            savedImageUri = uri
            navController.previousBackStackEntry?.savedStateHandle?.set(
                "editedImageUri",
                uri.toString()
            )
            currentImageUri =  uri.toString() //update currentImageUri
            loadImage(currentImageUri)  //Reload image
            updatePreview()
            navController.popBackStack()
        } ?: run {
            showToast("Failed to save image.",snackbarHostState)
        }
    }


    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Photo Editor") },
                actions = {
                    IconButton(onClick = {
                        saveImageAndNavigate()
                    }) {
                        Text(text = "Save")
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        bottomBar = {
            Column {
                AnimatedVisibility(
                    visible = showFilters && isEditModeActive,
                    enter = slideInVertically(initialOffsetY = { -it }),
                    exit = slideOutVertically(targetOffsetY = { -it })
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .padding(bottom = 8.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        currentBitmap?.let { currentBitmap ->
                            filters.forEach { filter ->
                                val filteredBitmap = applyFilterToBitmap(currentBitmap, filter)
                                filteredBitmap?.let {
                                    Image(
                                        bitmap = it.asImageBitmap(),
                                        contentDescription = "Filtered Image",
                                        modifier = Modifier
                                            .size(50.dp)
                                            .clickable { currentFilter = filter; updatePreview() },
                                        contentScale = ContentScale.Fit
                                    )
                                }
                            }
                        }
                    }
                }
                BottomAppBar {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        IconButton(onClick = {
                            isEditModeActive = !isEditModeActive
                            showFilters = false
                        }) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    if (isEditModeActive) Icons.Default.Close else Icons.Default.Edit,
                                    contentDescription = "Edit",
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = if (isEditModeActive) "Close" else "Edit",
                                    fontSize = 12.sp
                                )
                            }
                        }
                        if (isEditModeActive) {
                            IconButton(onClick = {
                                currentBitmap?.let {
                                    imageCrop = ImageCrop(it)
                                    isCropperActive = true
                                }
                            }) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        Icons.Default.ContentCut,
                                        contentDescription = "Cut",
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(text = "Cut", fontSize = 12.sp)
                                }
                            }

                            IconButton(onClick = { showFilters = !showFilters }) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        Icons.Default.Filter,
                                        contentDescription = "Filter",
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(text = "Filter", fontSize = 12.sp)
                                }
                            }
                            IconButton(onClick = {
                                if (croppedBitmap != null) {
                                    rotateCroppedImage()?.also {
                                        rotationAngle += 90f
                                        updatePreview()
                                    }
                                } else {
                                    rotateImage()?.also {
                                        rotationAngle += 90f
                                        updatePreview()
                                    }
                                }
                            }) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        Icons.Default.RotateLeft,
                                        contentDescription = "Rotate",
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(text = "Rotate", fontSize = 12.sp)
                                }
                            }

                            IconButton(onClick = { showTextDialog = true }) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        Icons.Default.TextFields,
                                        contentDescription = "Text",
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(text = "Text", fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }
        },
        content = { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                if (showTextDialog) {
                    AddTextDialog(
                        initialText = addedText,
                        initialColor = textColor,
                        colorList = colorList,
                        onTextAdded = { text, color ->
                            addedText = text
                            textColor = color
                            updatePreview()
                            showTextDialog = false
                        },
                        onDismiss = { showTextDialog = false }
                    )
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(400.dp)
                ) {

                    previewBitmap?.let { bitmap ->
                        var composableBitmap = bitmap.asImageBitmap()
                        Box(modifier = Modifier
                            .fillMaxWidth()
                            .height(400.dp)) {
                            Image(
                                bitmap = composableBitmap,
                                contentDescription = "Loaded Image",
                                modifier = Modifier
                                    .fillMaxSize(),
                                contentScale = ContentScale.Fit,
                            )

                            if (isCropperActive && imageCrop != null) {
                                imageCrop!!.ImageCropView(
                                    modifier = Modifier.fillMaxSize(),
                                    guideLineColor = Color.LightGray,
                                    guideLineWidth = 2.dp,
                                    edgeCircleSize = 5.dp,
                                    showGuideLines = true,
                                    cropType = CropType.SQUARE,
                                    edgeType = EdgeType.CIRCULAR,
                                )
                            }
                            if (addedText.isNotEmpty()) {
                                Text(
                                    text = addedText,
                                    color = textColor,
                                    fontSize = textSize.sp,
                                    modifier = Modifier
                                        .align(Alignment.Center)
                                        .offset(textOffset.x.dp, textOffset.y.dp)
                                        .pointerInput(Unit) {
                                            detectDragGestures(
                                                onDragStart = { isTextMoving = true },
                                                onDragEnd = { isTextMoving = false },
                                                onDragCancel = { isTextMoving = false },
                                            ) { change, dragAmount ->
                                                change.consume()
                                                textOffset = textOffset.plus(dragAmount)
                                                updatePreview()
                                            }
                                        }
                                        .clickable {
                                            isTextClicked = !isTextClicked
                                        }
                                )

                            }
                        }
                    } ?: Text("No Photo Available")
                }
                AnimatedVisibility(visible = isTextClicked) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        IconButton(onClick = { textSize++; updatePreview() }) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Increase Text"
                            )
                        }
                        IconButton(onClick = { textSize--; updatePreview() }) {
                            Icon(
                                imageVector = Icons.Default.Remove,
                                contentDescription = "Increase Text"
                            )
                        }
                    }
                }
                if (isCropperActive) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        TextButton(onClick = {
                            isCropperActive = false
                            croppedBitmap = null
                            isCropPerformed = true
                            updatePreview()
                        }) {
                            Text(text = "Cancel")
                        }
                        TextButton(onClick = {
                            isCropPerformed = true
                            croppedBitmap = imageCrop?.onCrop()
                            updatePreview()
                            isCropperActive = false
                        }) {
                            Text(text = "Crop")
                        }
                    }
                }
            }
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        }
    )
}


@Composable
fun AddTextDialog(
    initialText: String,
    initialColor: Color,
    colorList: List<Color>,
    onTextAdded: (String, Color) -> Unit,
    onDismiss: () -> Unit
) {
    var textState by remember { mutableStateOf(initialText) }
    var colorState by remember { mutableStateOf(initialColor) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Text") },
        text = {
            Column {
                BasicTextField(
                    value = textState,
                    onValueChange = { textState = it },
                    modifier = Modifier.fillMaxWidth()
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                ) {
                    colorList.forEach { color ->
                        IconButton(onClick = { colorState = color }) {
                            Icon(
                                imageVector = Icons.Filled.Circle,
                                contentDescription = "color",
                                tint = color,
                                modifier = Modifier
                                    .size(24.dp)
                                    .border(
                                        width = if (colorState == color) 2.dp else 0.dp,
                                        color = if (colorState == color) Color.Gray else Color.Transparent,
                                        shape = androidx.compose.ui.graphics.RectangleShape
                                    )
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onTextAdded(textState, colorState) }) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

fun saveBitmapToFile(context: Context, bitmap: Bitmap): Uri? {
    val imagesDir = File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), "EditedImages")
    if (!imagesDir.exists()) {
        imagesDir.mkdirs()
    }
    val file = File(imagesDir, "edited_image_${System.currentTimeMillis()}.png")
    var fos: FileOutputStream? = null
    try {
        fos = FileOutputStream(file)
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos)
        fos.flush()
        val newUri = Uri.fromFile(file)
        return newUri

    } catch (e: Exception) {
        e.printStackTrace()
        return null
    } finally {
        fos?.close()
    }
}

fun rotateBitmap(context: Context, bitmap: Bitmap, degrees: Float): Bitmap? {
    return try {
        val matrix = Matrix()
        matrix.postRotate(degrees)
        val rotatedBitmap =  Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
        rotatedBitmap
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}


fun showToast(message: String, snackbarHostState: SnackbarHostState) {
    CoroutineScope(Dispatchers.Main).launch {
        snackbarHostState.showSnackbar(message)
    }
}