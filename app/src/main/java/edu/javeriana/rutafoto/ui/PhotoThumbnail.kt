package edu.javeriana.rutafoto.ui

import android.graphics.Bitmap
import android.net.Uri
import android.util.Size
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun photoBitmap(uri: Uri, size: Int = 180): Bitmap? {
    val resolver = LocalContext.current.contentResolver
    return produceState<Bitmap?>(null, uri, size) {
        value = withContext(Dispatchers.IO) {
            try { resolver.loadThumbnail(uri, Size(size, size), null) }
            catch (_: Exception) { null }
        }
    }.value
}

@Composable
fun PhotoThumbnail(uri: Uri, description: String, modifier: Modifier = Modifier, size: Int = 180) {
    val bitmap = photoBitmap(uri, size)
    Box(modifier.background(MaterialTheme.colorScheme.surfaceVariant), contentAlignment = Alignment.Center) {
        if (bitmap != null) Image(bitmap.asImageBitmap(), description,
            Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
        else Text("…", color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
