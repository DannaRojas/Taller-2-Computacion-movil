package edu.javeriana.rutafoto

import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.platform.app.InstrumentationRegistry
import edu.javeriana.rutafoto.data.RoutePhoto
import edu.javeriana.rutafoto.ui.PhotoViewer
import org.junit.Rule
import org.junit.Test
import java.io.File

class PhotoViewerTest {
    @get:Rule val compose = createComposeRule()

    private fun openPhoto() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val file = File(context.cacheDir, "zoom-test.jpg")
        val bitmap = Bitmap.createBitmap(1200, 800, Bitmap.Config.ARGB_8888)
        bitmap.eraseColor(Color.BLUE)
        file.outputStream().use { bitmap.compress(Bitmap.CompressFormat.JPEG, 90, it) }
        bitmap.recycle()
        compose.setContent {
            MaterialTheme { PhotoViewer(RoutePhoto(file.name, Uri.fromFile(file), 4.6273, -74.0641)) {} }
        }
        compose.waitUntil(10_000) {
            compose.onAllNodesWithContentDescription(file.name).fetchSemanticsNodes().isNotEmpty()
        }
    }

    @Test fun buttonsZoomWithinBoundsAndReset() {
        openPhoto()
        compose.onNodeWithText("100 %").assertExists()
        compose.onNodeWithContentDescription("Alejar foto").assertIsNotEnabled()
        compose.onNodeWithContentDescription("Acercar foto").performClick()
        compose.onNodeWithText("150 %").assertExists()
        repeat(3) { compose.onNodeWithContentDescription("Acercar foto").performClick() }
        compose.onNodeWithText("500 %").assertExists()
        compose.onNodeWithContentDescription("Acercar foto").assertIsNotEnabled()
        compose.onNodeWithContentDescription("Restablecer zoom de foto").performClick()
        compose.onNodeWithText("100 %").assertExists()
    }

    @Test fun doubleTapZoomsAndReturnsToFullPhoto() {
        openPhoto()
        compose.onNodeWithTag("photo_zoom_surface").performTouchInput { doubleClick() }
        compose.onNodeWithText("250 %").assertExists()
        compose.onNodeWithTag("photo_zoom_surface").performTouchInput { doubleClick() }
        compose.onNodeWithText("100 %").assertExists()
    }
}
