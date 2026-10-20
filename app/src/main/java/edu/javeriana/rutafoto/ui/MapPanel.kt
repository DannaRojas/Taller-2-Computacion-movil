package edu.javeriana.rutafoto.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.mapbox.geojson.Point
import com.mapbox.common.MapboxOptions
import com.mapbox.maps.extension.compose.MapboxMap
import com.mapbox.maps.extension.compose.DisposableMapEffect
import com.mapbox.maps.extension.compose.annotation.generated.PolylineAnnotation
import com.mapbox.maps.extension.compose.annotation.Marker
import com.mapbox.maps.extension.compose.annotation.ViewAnnotation
import com.mapbox.maps.MapboxExperimental
import com.mapbox.maps.ViewAnnotationAnchor
import com.mapbox.maps.viewannotation.annotationAnchor
import com.mapbox.maps.viewannotation.geometry
import com.mapbox.maps.viewannotation.viewAnnotationOptions
import com.mapbox.maps.extension.compose.animation.viewport.rememberMapViewportState
import edu.javeriana.rutafoto.R
import edu.javeriana.rutafoto.data.RoutePhoto
import edu.javeriana.rutafoto.data.RouteViewModel
import java.util.Locale

// Clase 9: MapboxMap, cámara del mapa, Marker y PolylineAnnotation.
@Composable
@OptIn(MapboxExperimental::class)
fun MapPanel(model: RouteViewModel, permissionGranted: Boolean, precise: Boolean,
             gpsEnabled: Boolean, permissionContent: @Composable () -> Unit,
             onLocationSettings: () -> Unit, onPhotoSelected: (RoutePhoto) -> Unit,
             modifier: Modifier = Modifier) {
    val buildToken = stringResource(R.string.mapbox_access_token)
    var sdkToken by remember { mutableStateOf("") }
    var tokenDialog by remember { mutableStateOf(false) }
    var draftToken by remember { mutableStateOf("") }
    var mapError by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        if (model.mapboxToken.isBlank() && buildToken.startsWith("pk.")) model.mapboxToken = buildToken
    }
    LaunchedEffect(model.mapboxToken) {
        if (model.mapboxToken.startsWith("pk.")) {
            try {
                MapboxOptions.accessToken = model.mapboxToken
                sdkToken = model.mapboxToken
                mapError = false
            } catch (_: Exception) { mapError = true }
        }
    }
    val tokenReady = sdkToken.isNotEmpty() && sdkToken == model.mapboxToken
    val viewport = rememberMapViewportState {
        // Vista general mientras llega el primer GPS; evita mostrar sólo océano a zoom 15.
        setCameraOptions { zoom(2.0); pitch(0.0); bearing(0.0) }
    }
    var centered by remember { mutableStateOf(false) }
    var clearDialog by remember { mutableStateOf(false) }
    val location = model.currentLocation
    LaunchedEffect(location, permissionGranted) {
        if (location != null && permissionGranted && !centered) {
            viewport.setCameraOptions { center(Point.fromLngLat(location.longitude, location.latitude)); zoom(16.0) }
            centered = true
        }
    }
    OutlinedCard(modifier, shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(Modifier.fillMaxSize().padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(stringResource(R.string.map_title), style = MaterialTheme.typography.titleLarge)
                    Text(stringResource(when {
                        model.recording -> R.string.route_active
                        model.started -> R.string.route_paused
                        else -> R.string.route_ready
                    }), style = MaterialTheme.typography.bodySmall)
                }
                if (permissionGranted && tokenReady) TextButton(
                    onClick = { draftToken = model.mapboxToken; tokenDialog = true }) {
                    Text(stringResource(R.string.map_settings))
                }
            }
            if (!permissionGranted) {
                Box(Modifier.fillMaxWidth().weight(1f)) { permissionContent() }
            } else {
                Box(Modifier.fillMaxWidth().weight(1f)) {
                    if (tokenReady) {
                        key(sdkToken) {
                        MapboxMap(Modifier.fillMaxSize(), mapViewportState = viewport) {
                            DisposableMapEffect(sdkToken) { mapView ->
                                val errors = mapView.mapboxMap.subscribeMapLoadingError { mapError = true }
                                val loaded = mapView.mapboxMap.subscribeStyleLoaded { mapError = false }
                                onDispose { errors.cancel(); loaded.cancel() }
                            }
                            if (model.points.size >= 2) PolylineAnnotation(
                                points = model.points.map { Point.fromLngLat(it.longitude, it.latitude) }
                            ) { lineColor = androidx.compose.ui.graphics.Color(0xFFB94B1A); lineWidth = 5.0 }
                            location?.let {
                                Marker(point = Point.fromLngLat(it.longitude, it.latitude),
                                    text = "",
                                    color = MaterialTheme.colorScheme.primary)
                            }
                            model.photos.forEach { photo ->
                                key(photo.name) {
                                    // Bonus: personalización del mismo ViewAnnotation que utiliza Marker.
                                    ViewAnnotation(options = viewAnnotationOptions {
                                        geometry(Point.fromLngLat(photo.longitude, photo.latitude))
                                        annotationAnchor { anchor(ViewAnnotationAnchor.BOTTOM); allowOverlap(true) }
                                    }) {
                                        Card(onClick = { onPhotoSelected(photo) }, modifier = Modifier.width(96.dp),
                                            shape = RoundedCornerShape(10.dp),
                                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)) {
                                            PhotoThumbnail(photo.uri, photo.name, Modifier.fillMaxWidth().height(48.dp))
                                            Text(photo.name, Modifier.padding(4.dp), maxLines = 1,
                                                overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.labelSmall)
                                        }
                                    }
                                }
                            }
                        }
                        }
                        Column(Modifier.align(Alignment.TopEnd).padding(8.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        FilledTonalIconButton(onClick = {
                            location?.let { viewport.setCameraOptions {
                                center(Point.fromLngLat(it.longitude, it.latitude)); zoom(16.0)
                            } }
                        }, enabled = location != null) {
                            Icon(Icons.Default.MyLocation, stringResource(R.string.center_map))
                        }
                        FilledTonalIconButton(onClick = {
                            val current = viewport.cameraState?.zoom ?: 16.0
                            viewport.setCameraOptions { zoom((current + 1.0).coerceAtMost(20.0)) }
                        }, enabled = (viewport.cameraState?.zoom ?: 16.0) < 20.0) {
                            Icon(Icons.Default.Add, stringResource(R.string.map_zoom_in))
                        }
                        FilledTonalIconButton(onClick = {
                            val current = viewport.cameraState?.zoom ?: 16.0
                            viewport.setCameraOptions { zoom((current - 1.0).coerceAtLeast(2.0)) }
                        }, enabled = (viewport.cameraState?.zoom ?: 16.0) > 2.0) {
                            Icon(Icons.Default.Remove, stringResource(R.string.map_zoom_out))
                        }
                        }
                    } else Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(16.dp)) {
                        Column(Modifier.padding(24.dp), verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(stringResource(R.string.missing_token))
                            Spacer(Modifier.height(12.dp))
                            Button(onClick = { draftToken = model.mapboxToken; tokenDialog = true }) {
                                Text(stringResource(R.string.configure_mapbox))
                            }
                        }
                    }
                    if (mapError) Surface(Modifier.align(Alignment.Center).padding(16.dp),
                        shape = RoundedCornerShape(12.dp), tonalElevation = 4.dp) {
                        Text(stringResource(R.string.map_loading_hint), Modifier.padding(16.dp))
                    }
                    if (!gpsEnabled || location == null) Surface(
                        Modifier.align(Alignment.BottomCenter).padding(12.dp),
                        shape = RoundedCornerShape(12.dp), tonalElevation = 4.dp
                    ) {
                        Column(Modifier.padding(12.dp)) {
                            Text(stringResource(if (gpsEnabled) R.string.waiting_location else R.string.gps_disabled),
                                style = MaterialTheme.typography.bodySmall)
                            if (!gpsEnabled) TextButton(onClick = onLocationSettings) {
                                Text(stringResource(R.string.location_settings))
                            }
                        }
                    }
                }
                if (!precise) Text(stringResource(R.string.approximate), style = MaterialTheme.typography.bodySmall)
                Text(stringResource(R.string.route_summary, model.photos.size,
                    String.format(Locale.getDefault(), "%.0f", model.distanceMeters)), style = MaterialTheme.typography.labelLarge)
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { model.toggleRecording() }, modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary,
                        contentColor = MaterialTheme.colorScheme.onSecondary),
                    enabled = gpsEnabled && model.freshLocation() != null && !model.capturing) {
                    Icon(if (model.recording) Icons.Default.Pause else Icons.Default.PlayArrow, null)
                    Spacer(Modifier.width(8.dp))
                    Text(stringResource(if (model.recording) R.string.pause else if (model.started) R.string.resume else R.string.start))
                }
                OutlinedButton(onClick = { clearDialog = true },
                    enabled = !model.capturing && (model.started || model.photos.isNotEmpty())) {
                    Text(stringResource(R.string.clear))
                }
                }
            }
        }
    }
    if (tokenDialog) AlertDialog(onDismissRequest = { tokenDialog = false },
        title = { Text(stringResource(R.string.configure_mapbox)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(stringResource(R.string.token_help))
                OutlinedTextField(value = draftToken, onValueChange = { draftToken = it },
                    label = { Text(stringResource(R.string.token_label)) }, singleLine = true,
                    visualTransformation = PasswordVisualTransformation(), modifier = Modifier.fillMaxWidth())
            }
        }, confirmButton = {
            TextButton(enabled = draftToken.trim().startsWith("pk.") && draftToken.trim().length > 20,
                onClick = { model.mapboxToken = draftToken.trim(); tokenDialog = false }) {
                Text(stringResource(R.string.apply_token))
            }
        }, dismissButton = { TextButton(onClick = { tokenDialog = false }) { Text(stringResource(R.string.cancel)) } })
    if (clearDialog) AlertDialog(onDismissRequest = { clearDialog = false },
        title = { Text(stringResource(R.string.clear_title)) },
        text = { Text(stringResource(R.string.clear_message)) },
        confirmButton = { TextButton(onClick = { model.clearRoute(); clearDialog = false }) {
            Text(stringResource(R.string.clear))
        } }, dismissButton = { TextButton(onClick = { clearDialog = false }) { Text(stringResource(R.string.cancel)) } })
}
