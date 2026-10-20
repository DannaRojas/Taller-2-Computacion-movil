package edu.javeriana.rutafoto.data

import android.location.Location
import android.net.Uri
import android.os.SystemClock
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

data class RoutePhoto(val name: String, val uri: Uri, val latitude: Double, val longitude: Double)


class RouteViewModel : ViewModel() {
    var mapboxToken by mutableStateOf("")
    var currentLocation by mutableStateOf<Location?>(null)
        private set
    var points by mutableStateOf<List<Location>>(emptyList())
        private set
    var photos by mutableStateOf<List<RoutePhoto>>(emptyList())
        private set
    var recording by mutableStateOf(false)
        private set
    var started by mutableStateOf(false)
        private set
    var capturing by mutableStateOf(false)
        private set
    private var generation = 0

    fun updateLocation(location: Location) {
        currentLocation = Location(location)
        if (recording) {
            val previous = points.lastOrNull()
            if (previous == null || previous.distanceTo(location) >= 3f) {
                points = points + Location(location)
            }
        }
    }

    fun freshLocation(): Location? = currentLocation?.takeIf {
        SystemClock.elapsedRealtimeNanos() - it.elapsedRealtimeNanos in 0..15_000_000_000L
    }?.let { Location(it) }

    fun toggleRecording() {
        if (recording) { recording = false; return }
        val location = freshLocation() ?: return
        started = true
        recording = true
        if (points.isEmpty()) points = listOf(location)
    }

    fun pause() { recording = false }
    fun beginCapture(): Int { capturing = true; return generation }
    fun finishCapture() { capturing = false }
    fun addPhoto(photo: RoutePhoto, captureGeneration: Int) {
        if (generation == captureGeneration) photos = photos + photo
    }
    fun clearRoute() {
        generation++
        recording = false
        started = false
        points = emptyList()
        photos = emptyList()
    }
    val distanceMeters: Float get() = points.zipWithNext().sumOf { (a, b) -> a.distanceTo(b).toDouble() }.toFloat()
}
