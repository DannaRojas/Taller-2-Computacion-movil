package edu.javeriana.rutafoto.ui

import android.content.ContentValues
import android.provider.MediaStore
import androidx.camera.compose.CameraXViewfinder
import androidx.camera.viewfinder.core.ImplementationMode
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.core.SurfaceRequest
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Cameraswitch
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import edu.javeriana.rutafoto.R
import edu.javeriana.rutafoto.data.RoutePhoto
import edu.javeriana.rutafoto.data.RouteViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

// Clase 7: Preview + SurfaceRequest + CameraXViewfinder; MediaStore para la galería.
@Composable
private fun CameraSurface(front: Boolean, onReady: (ImageCapture?) -> Unit,
                          onError: (Int) -> Unit, modifier: Modifier) {
    val context = LocalContext.current
    val owner = LocalLifecycleOwner.current
    val rotation = LocalView.current.display?.rotation ?: android.view.Surface.ROTATION_0
    var surfaceRequest by remember { mutableStateOf<SurfaceRequest?>(null) }
    val ready = rememberUpdatedState(onReady)
    val error = rememberUpdatedState(onError)
    DisposableEffect(front, owner, rotation) {
        var disposed = false
        var boundProvider: ProcessCameraProvider? = null
        var boundPreview: Preview? = null
        var boundCapture: ImageCapture? = null
        ready.value(null)
        surfaceRequest = null
        val future = ProcessCameraProvider.getInstance(context)
        future.addListener({
            if (!disposed) {
                try {
                    val provider = future.get()
                    val selector = if (front) CameraSelector.DEFAULT_FRONT_CAMERA else CameraSelector.DEFAULT_BACK_CAMERA
                    if (!provider.hasCamera(selector)) {
                        error.value(R.string.no_other_camera)
                    } else {
                        val preview = Preview.Builder().setTargetRotation(rotation).build().apply {
                            setSurfaceProvider { request -> surfaceRequest = request }
                        }
                        val capture = ImageCapture.Builder()
                            .setTargetRotation(rotation)
                            .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY).build()
                        provider.bindToLifecycle(owner, selector, preview, capture)
                        boundProvider = provider
                        boundPreview = preview
                        boundCapture = capture
                        ready.value(capture)
                    }
                } catch (_: Exception) { error.value(R.string.camera_error) }
            }
        }, ContextCompat.getMainExecutor(context))
        onDispose {
            disposed = true
            ready.value(null)
            val preview = boundPreview
            val capture = boundCapture
            if (preview != null && capture != null) boundProvider?.unbind(preview, capture)
        }
    }
    Box(modifier.background(Color(0xFF142D2A)), contentAlignment = Alignment.Center) {
        val request = surfaceRequest
        // Mantener la cámara dentro de Compose al cambiar de pantalla en un plegable.
        if (request != null) CameraXViewfinder(surfaceRequest = request,
            implementationMode = ImplementationMode.EMBEDDED, modifier = Modifier.fillMaxSize())
        else Text(stringResource(R.string.camera_loading), color = Color.White)
    }
}

@Composable
fun CameraPanel(model: RouteViewModel, permissionGranted: Boolean, locationGranted: Boolean,
                gpsEnabled: Boolean, permissionContent: @Composable () -> Unit,
                onMessage: (Int) -> Unit, onPhotoSelected: (RoutePhoto) -> Unit,
                modifier: Modifier = Modifier) {
    val context = LocalContext.current
    var front by rememberSaveable { mutableStateOf(false) }
    var imageCapture by remember { mutableStateOf<ImageCapture?>(null) }
    var cameraError by remember { mutableStateOf<Int?>(null) }
    Card(modifier, shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Surface(shape = RoundedCornerShape(8.dp), color = MaterialTheme.colorScheme.secondaryContainer) {
                    Icon(Icons.Default.CameraAlt, null, Modifier.padding(8.dp).size(20.dp),
                        tint = MaterialTheme.colorScheme.onSecondaryContainer)
                }
                Text(stringResource(R.string.camera_title), style = MaterialTheme.typography.titleMedium)
            }
            if (!permissionGranted) {
                Box(Modifier.weight(1f).fillMaxWidth()) { permissionContent() }
            } else {
                Box(Modifier.weight(1f).fillMaxWidth().clip(RoundedCornerShape(16.dp))) {
                    CameraSurface(front, onReady = { imageCapture = it },
                        onError = { cameraError = it }, modifier = Modifier.fillMaxSize())
                    cameraError?.let {
                        Surface(Modifier.align(Alignment.Center).padding(16.dp), shape = RoundedCornerShape(12.dp)) {
                            Text(stringResource(it), Modifier.padding(16.dp))
                        }
                    }
                    FilledIconButton(onClick = { front = !front; cameraError = null },
                        enabled = !model.capturing, modifier = Modifier.align(Alignment.TopEnd).padding(12.dp),
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary)) {
                        Icon(Icons.Default.Cameraswitch, stringResource(R.string.switch_camera))
                    }
                    Button(
                        enabled = imageCapture != null && model.recording && locationGranted && gpsEnabled && !model.capturing,
                        onClick = {
                            val location = model.freshLocation()
                            val capture = imageCapture
                            if (location == null) onMessage(R.string.stale_location)
                            else if (capture != null) {
                                val generation = model.beginCapture()
                                val stamp = SimpleDateFormat("yyyyMMdd_HHmmss_SSS", Locale.ROOT).format(Date())
                                val name = "RUTA_${stamp}_${UUID.randomUUID().toString().take(6)}.jpg"
                                val values = ContentValues().apply {
                                    put(MediaStore.Images.Media.DISPLAY_NAME, name)
                                    put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
                                    put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/RutaFoto")
                                }
                                val options = ImageCapture.OutputFileOptions.Builder(context.contentResolver,
                                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values).build()
                                try {
                                    capture.takePicture(options, ContextCompat.getMainExecutor(context),
                                        object : ImageCapture.OnImageSavedCallback {
                                            override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                                                model.finishCapture()
                                                val uri = output.savedUri
                                                if (uri != null) {
                                                    model.addPhoto(RoutePhoto(name, uri, location.latitude, location.longitude), generation)
                                                    onMessage(R.string.saved)
                                                } else onMessage(R.string.capture_error)
                                            }
                                            override fun onError(exception: ImageCaptureException) {
                                                model.finishCapture()
                                                onMessage(R.string.capture_error)
                                            }
                                        })
                                } catch (_: Exception) {
                                    model.finishCapture()
                                    onMessage(R.string.capture_error)
                                }
                            }
                        }, modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary,
                            contentColor = MaterialTheme.colorScheme.onSecondary)
                    ) {
                        if (model.capturing) CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp)
                        else Icon(Icons.Default.CameraAlt, null)
                        Spacer(Modifier.width(8.dp))
                        Text(stringResource(R.string.capture))
                    }
                }
                if (!model.recording) Text(stringResource(R.string.capture_hint), style = MaterialTheme.typography.bodySmall)
            }
            Text(stringResource(R.string.photos_title, model.photos.size), style = MaterialTheme.typography.titleSmall)
            if (model.photos.isEmpty()) {
                Box(Modifier.fillMaxWidth().height(86.dp).background(MaterialTheme.colorScheme.surfaceVariant,
                    RoundedCornerShape(12.dp)).padding(12.dp), contentAlignment = Alignment.Center) {
                    Text(stringResource(R.string.no_photos), style = MaterialTheme.typography.bodySmall)
                }
            } else LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(model.photos, key = { it.name }) { photo ->
                    Card(onClick = { onPhotoSelected(photo) }, modifier = Modifier.width(100.dp)) {
                        PhotoThumbnail(photo.uri, photo.name, Modifier.fillMaxWidth().height(66.dp))
                        Text(photo.name, Modifier.padding(4.dp), maxLines = 1,
                            style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }
    }
}
