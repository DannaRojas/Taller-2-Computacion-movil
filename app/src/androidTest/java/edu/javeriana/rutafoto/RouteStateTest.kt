package edu.javeriana.rutafoto

import android.location.Location
import android.net.Uri
import android.os.SystemClock
import androidx.test.ext.junit.runners.AndroidJUnit4
import edu.javeriana.rutafoto.data.RoutePhoto
import edu.javeriana.rutafoto.data.RouteViewModel
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RouteStateTest {
    private fun position(latitude: Double = 4.6273, ageSeconds: Long = 0) = Location("test").apply {
        this.latitude = latitude
        longitude = -74.0641
        accuracy = 5f
        elapsedRealtimeNanos = SystemClock.elapsedRealtimeNanos() - ageSeconds * 1_000_000_000L
    }

    @Test fun routeRequiresRecentPosition() {
        val model = RouteViewModel()
        model.toggleRecording()
        assertFalse(model.recording)
        model.updateLocation(position(ageSeconds = 60))
        model.toggleRecording()
        assertFalse(model.recording)
        model.updateLocation(position())
        model.toggleRecording()
        assertTrue(model.recording)
        assertEquals(1, model.points.size)
    }

    @Test fun positionsRecordOnlyWhileActiveAndFilterStationaryNoise() {
        val model = RouteViewModel()
        model.updateLocation(position())
        assertTrue(model.points.isEmpty())
        model.toggleRecording()
        model.updateLocation(position())
        assertEquals(1, model.points.size)
        model.updateLocation(position(4.6280))
        assertEquals(2, model.points.size)
        assertTrue(model.distanceMeters > 50f)
        model.pause()
        model.updateLocation(position(4.6290))
        assertEquals(2, model.points.size)
        assertEquals(4.6290, model.currentLocation!!.latitude, 0.000001)
    }

    @Test fun clearingRemovesRouteAndPhotosButKeepsCurrentPosition() {
        val model = RouteViewModel()
        model.updateLocation(position())
        model.toggleRecording()
        val generation = model.beginCapture()
        model.addPhoto(RoutePhoto("foto.jpg", Uri.parse("content://test/1"), 4.6273, -74.0641), generation)
        model.finishCapture()
        assertEquals(1, model.photos.size)
        model.clearRoute()
        assertTrue(model.photos.isEmpty())
        assertTrue(model.points.isEmpty())
        assertFalse(model.recording)
        assertFalse(model.started)
        assertNotNull(model.currentLocation)
    }

    @Test fun lateCaptureCannotRepopulateClearedRoute() {
        val model = RouteViewModel()
        model.updateLocation(position())
        model.toggleRecording()
        val oldGeneration = model.beginCapture()
        model.clearRoute()
        model.addPhoto(RoutePhoto("tardia.jpg", Uri.parse("content://test/2"), 4.6273, -74.0641), oldGeneration)
        model.finishCapture()
        assertTrue(model.photos.isEmpty())
        assertFalse(model.capturing)
    }
}
