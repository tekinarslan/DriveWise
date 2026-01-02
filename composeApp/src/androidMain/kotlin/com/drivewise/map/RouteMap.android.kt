package com.drivewise.map

import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.drivewise.matching.LatLon
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState

@Composable
actual fun RouteMap(points: List<LatLon>, modifier: Modifier) {
    val cameraState = rememberCameraPositionState()

    // Map hazır mı? (CameraUpdateFactory crash fix)
    var mapLoaded by remember { mutableStateOf(false) }

    // Marker state'leri remember ile oluştur
    val startMarkerState = remember { MarkerState() }
    val endMarkerState = remember { MarkerState() }

    // LatLng listesi (recompose'ta gereksiz map() olmasın)
    val latLngs = remember(points) { points.map { LatLng(it.lat, it.lon) } }

    // Marker pozisyonlarını güncelle (mapLoaded bağımsız)
    LaunchedEffect(latLngs) {
        if (latLngs.isNotEmpty()) startMarkerState.position = latLngs.first()
        if (latLngs.size >= 2) endMarkerState.position = latLngs.last()
    }

    // ✅ Kamera animasyonunu SADECE map yüklendikten sonra yap
    LaunchedEffect(mapLoaded, latLngs) {
        if (!mapLoaded) return@LaunchedEffect

        when {
            latLngs.size >= 2 -> {
                val bounds = LatLngBounds.builder().apply {
                    latLngs.forEach { include(it) }
                }.build()

                // newLatLngBounds, map loaded olmadan patlar. Artık safe.
                cameraState.animate(CameraUpdateFactory.newLatLngBounds(bounds, 80))
            }

            latLngs.size == 1 -> {
                cameraState.animate(CameraUpdateFactory.newLatLngZoom(latLngs.first(), 16f))
            }
        }
    }

    GoogleMap(
        modifier = modifier,
        cameraPositionState = cameraState,
        onMapLoaded = { mapLoaded = true } // ✅ kritik nokta
    ) {
        if (latLngs.size >= 2) {
            Polyline(
                points = latLngs,
                width = 6f
            )
            Marker(state = startMarkerState, title = "Start")
            Marker(state = endMarkerState, title = "End")
        } else if (latLngs.size == 1) {
            Marker(state = startMarkerState, title = "Point")
        }
    }
}
