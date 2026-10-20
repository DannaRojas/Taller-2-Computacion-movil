package edu.javeriana.rutafoto

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import edu.javeriana.rutafoto.data.LocationUpdates
import edu.javeriana.rutafoto.data.RoutePhoto
import edu.javeriana.rutafoto.data.RouteViewModel
import edu.javeriana.rutafoto.ui.CameraPanel
import edu.javeriana.rutafoto.ui.MapPanel
import edu.javeriana.rutafoto.ui.PhotoViewer
import edu.javeriana.rutafoto.ui.RutaFotoTypography

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
        )
        setContent {
            MaterialTheme(typography = RutaFotoTypography, colorScheme = darkColorScheme(
                primary = Color(0xFF39A9FF), onPrimary = Color(0xFF001D32),
                primaryContainer = Color(0xFF12354D), onPrimaryContainer = Color(0xFFC5E7FF),
                secondary = Color(0xFF31D69A), onSecondary = Color(0xFF002619),
                secondaryContainer = Color(0xFF123D30), onSecondaryContainer = Color(0xFFB5F5D9),
                tertiary = Color(0xFF45C9D5), onTertiary = Color(0xFF002F33),
                background = Color.Black, onBackground = Color(0xFFF1F5F7),
                surface = Color(0xFF121619), onSurface = Color(0xFFF1F5F7),
                surfaceVariant = Color(0xFF20282D), onSurfaceVariant = Color(0xFFB9C7CE),
                surfaceContainer = Color(0xFF181E22), surfaceContainerHighest = Color(0xFF283138),
                surfaceContainerLow = Color(0xFF0D1114), surfaceContainerHigh = Color(0xFF20282D),
                outline = Color(0xFF768991), outlineVariant = Color(0xFF34434B)
            )) { RouteScreen() }
        }
    }
}

@Composable
private fun rememberGpsEnabled(): Boolean {
    val context = LocalContext.current
    val owner = LocalLifecycleOwner.current
    val manager = remember { context.getSystemService(Context.LOCATION_SERVICE) as LocationManager }
    var enabled by remember { mutableStateOf(manager.isLocationEnabled) }
    DisposableEffect(owner) {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) { enabled = manager.isLocationEnabled }
        }
        ContextCompat.registerReceiver(context, receiver,
            IntentFilter(LocationManager.PROVIDERS_CHANGED_ACTION), ContextCompat.RECEIVER_NOT_EXPORTED)
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) enabled = manager.isLocationEnabled
        }
        owner.lifecycle.addObserver(observer)
        onDispose { context.unregisterReceiver(receiver); owner.lifecycle.removeObserver(observer) }
    }
    return enabled
}

// Clase 6: solicitud al iniciar y estados independientes para las dos regiones.
@OptIn(ExperimentalPermissionsApi::class)
@Composable
private fun RouteScreen(model: RouteViewModel = viewModel()) {
    val context = LocalContext.current
    val permissions = rememberMultiplePermissionsState(listOf(Manifest.permission.CAMERA,
        Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION))
    var asked by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        if (!asked) { asked = true; permissions.launchMultiplePermissionRequest() }
    }
    fun granted(name: String) = permissions.permissions.any { it.permission == name && it.status.isGranted }
    val cameraGranted = granted(Manifest.permission.CAMERA)
    val precise = granted(Manifest.permission.ACCESS_FINE_LOCATION)
    val locationGranted = precise || granted(Manifest.permission.ACCESS_COARSE_LOCATION)
    val gpsEnabled = rememberGpsEnabled()
    val message: (Int) -> Unit = { Toast.makeText(context, it, Toast.LENGTH_LONG).show() }
    LocationUpdates(locationGranted && gpsEnabled, model::updateLocation) { message(R.string.location_error) }
    LaunchedEffect(cameraGranted, locationGranted, gpsEnabled) {
        if (!cameraGranted || !locationGranted || !gpsEnabled) model.pause()
    }
    var selected by remember { mutableStateOf<RoutePhoto?>(null) }
    val settings: () -> Unit = {
        context.startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
            Uri.parse("package:${context.packageName}")))
    }
    Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(Modifier.fillMaxSize().safeDrawingPadding().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Column(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                Text(stringResource(R.string.app_name), style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Medium)
                Text(stringResource(R.string.app_subtitle), style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            BoxWithConstraints(Modifier.fillMaxWidth().weight(1f)) {
                val camera: @Composable (Modifier) -> Unit = { modifier ->
                    CameraPanel(model, cameraGranted, locationGranted, gpsEnabled,
                        permissionContent = {
                            PermissionMessage(R.string.camera_permission,
                                { permissions.launchMultiplePermissionRequest() }, settings)
                        }, onMessage = message, onPhotoSelected = { selected = it }, modifier = modifier)
                }
                val map: @Composable (Modifier) -> Unit = { modifier ->
                    MapPanel(model, locationGranted, precise, gpsEnabled, permissionContent = {
                        PermissionMessage(R.string.location_permission,
                            { permissions.launchMultiplePermissionRequest() }, settings)
                    }, onLocationSettings = { context.startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)) },
                        onPhotoSelected = { selected = it }, modifier = modifier)
                }
                // Clases 2-4: Row/Column, pesos, restricciones y componentes reutilizables.
                if (maxWidth >= 760.dp) Row(Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    map(Modifier.weight(1.25f).fillMaxHeight())
                    camera(Modifier.weight(1f).fillMaxHeight())
                } else Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    map(Modifier.weight(1f).fillMaxWidth())
                    camera(Modifier.weight(1.15f).fillMaxWidth())
                }
            }
        }
    }
    selected?.let { photo ->
        PhotoViewer(photo = photo, onClose = { selected = null })
    }
}

@Composable
private fun PermissionMessage(text: Int, request: () -> Unit, settings: () -> Unit) {
    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally) {
        Text(stringResource(text), color = MaterialTheme.colorScheme.error)
        Spacer(Modifier.height(12.dp))
        Button(onClick = request) { Text(stringResource(R.string.allow_permission)) }
        TextButton(onClick = settings) { Text(stringResource(R.string.settings)) }
    }
}
