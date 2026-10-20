package edu.javeriana.rutafoto.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil3.compose.AsyncImage
import edu.javeriana.rutafoto.R
import edu.javeriana.rutafoto.data.RoutePhoto
import kotlin.math.max
import kotlin.math.roundToInt

@Composable
fun PhotoViewer(photo: RoutePhoto, onClose: () -> Unit) {
    var imageSize by remember(photo.uri) { mutableStateOf(IntSize.Zero) }
    var loadingError by remember(photo.uri) { mutableStateOf(false) }
    val loaded = imageSize.width > 0 && imageSize.height > 0
    var scale by remember(photo.uri) { mutableFloatStateOf(1f) }
    var offset by remember(photo.uri) { mutableStateOf(Offset.Zero) }
    var frame by remember { mutableStateOf(IntSize.Zero) }

    fun constrain(candidate: Offset, nextScale: Float): Offset {
        val image = imageSize
        if (!loaded) return Offset.Zero
        if (frame.width == 0 || frame.height == 0) return Offset.Zero
        val fit = minOf(frame.width.toFloat() / image.width, frame.height.toFloat() / image.height)
        val limitX = max(0f, (image.width * fit * nextScale - frame.width) / 2)
        val limitY = max(0f, (image.height * fit * nextScale - frame.height) / 2)
        return Offset(candidate.x.coerceIn(-limitX, limitX), candidate.y.coerceIn(-limitY, limitY))
    }
    fun reset() { scale = 1f; offset = Offset.Zero }
    fun changeScale(factor: Float) {
        val next = (scale * factor).coerceIn(1f, 5f)
        offset = constrain(offset * (next / scale), next)
        scale = next
    }

    Dialog(onDismissRequest = onClose, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(Modifier.fillMaxSize().safeDrawingPadding().padding(16.dp), shape = MaterialTheme.shapes.large) {
            Column(Modifier.fillMaxSize().padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text(photo.name, Modifier.weight(1f), maxLines = 2, overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.titleMedium)
                    IconButton(onClick = onClose) { Icon(Icons.Default.Close, stringResource(R.string.close)) }
                }
                Box(Modifier.weight(1f).fillMaxWidth().clipToBounds().background(Color.Black)
                    .onSizeChanged { frame = it; offset = constrain(offset, scale) }
                    .testTag("photo_zoom_surface")
                    .pointerInput(photo.uri, imageSize, frame) {
                        detectTransformGestures { centroid, pan, zoom, _ ->
                            val next = (scale * zoom).coerceIn(1f, 5f)
                            val ratio = next / scale
                            val center = Offset(frame.width / 2f, frame.height / 2f)
                            offset = constrain(offset * ratio + (center - centroid) * (ratio - 1f) + pan, next)
                            scale = next
                        }
                    }
                    .pointerInput(photo.uri, imageSize, frame) {
                        detectTapGestures(onDoubleTap = { tap ->
                            if (scale > 1f) reset() else {
                                scale = 2.5f
                                offset = constrain((Offset(frame.width / 2f, frame.height / 2f) - tap) * 1.5f, scale)
                            }
                        })
                    }, contentAlignment = Alignment.Center) {
                    AsyncImage(
                        model = photo.uri,
                        contentDescription = photo.name,
                        modifier = Modifier.fillMaxSize().graphicsLayer {
                            scaleX = scale; scaleY = scale
                            translationX = offset.x; translationY = offset.y
                        },
                        contentScale = ContentScale.Fit,
                        onSuccess = { state ->
                            imageSize = IntSize(state.result.image.width, state.result.image.height)
                            loadingError = false
                        },
                        onError = { loadingError = true }
                    )
                    if (loadingError) Text(stringResource(R.string.photo_unavailable), color = Color.White)
                    else if (!loaded) CircularProgressIndicator()
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically) {
                    FilledTonalIconButton(onClick = { changeScale(1f / 1.5f) }, enabled = loaded && scale > 1f) {
                        Icon(Icons.Default.Remove, stringResource(R.string.photo_zoom_out))
                    }
                    Text(stringResource(R.string.zoom_level, (scale * 100).roundToInt()),
                        Modifier.width(88.dp).padding(horizontal = 12.dp))
                    FilledTonalIconButton(onClick = { changeScale(1.5f) }, enabled = loaded && scale < 5f) {
                        Icon(Icons.Default.Add, stringResource(R.string.photo_zoom_in))
                    }
                    Spacer(Modifier.width(12.dp))
                    IconButton(onClick = { reset() }, enabled = loaded) {
                        Icon(Icons.Default.RestartAlt, stringResource(R.string.photo_zoom_reset))
                    }
                }
                Text(stringResource(R.string.photo_zoom_help), style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}
