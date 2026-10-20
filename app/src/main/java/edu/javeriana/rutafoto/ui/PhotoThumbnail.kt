package edu.javeriana.rutafoto.ui

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import coil3.compose.AsyncImage

@Composable
fun PhotoThumbnail(uri: Uri, description: String, modifier: Modifier = Modifier) {
    AsyncImage(
        model = uri,
        contentDescription = description,
        modifier = modifier.background(MaterialTheme.colorScheme.surfaceVariant),
        contentScale = ContentScale.Crop
    )
}
