package com.tembt.android.ui.map

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.foundation.layout.fillMaxSize
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.tembt.domain.model.MapCoordinates
import org.maplibre.android.MapLibre
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.geometry.LatLngBounds
import org.maplibre.android.maps.MapView
import kotlin.math.PI
import kotlin.math.cos

// OpenFreeMap — free OSM-based vector tiles, no API key required.
// Styles available: liberty, bright, positron. See https://openfreemap.org
private const val MAP_STYLE = "https://tiles.openfreemap.org/styles/liberty"

// 1 km expressed as degrees of latitude (constant regardless of position)
private const val KM_AS_LAT_DEG = 0.009

@Composable
fun MapLibreView(center: MapCoordinates) {
    val lifecycleOwner = LocalLifecycleOwner.current

    val mapView = remember {
        // MapLibre.getInstance is idempotent — safe to call multiple times
        null // created inside AndroidView factory to get the Context
    }

    val latLng = remember(center) { LatLng(center.latitude, center.longitude) }

    val bounds = remember(center) {
        val lngDelta = KM_AS_LAT_DEG / cos(center.latitude * PI / 180.0)
        LatLngBounds.Builder()
            .include(LatLng(center.latitude - KM_AS_LAT_DEG, center.longitude - lngDelta))
            .include(LatLng(center.latitude + KM_AS_LAT_DEG, center.longitude + lngDelta))
            .build()
    }

    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = { context ->
            MapLibre.getInstance(context)
            MapView(context).apply {
                getMapAsync { map ->
                    map.setStyle(MAP_STYLE)
                    map.moveCamera(
                        org.maplibre.android.camera.CameraUpdateFactory.newCameraPosition(
                            CameraPosition.Builder()
                                .target(latLng)
                                .zoom(15.0)
                                .build()
                        )
                    )
                    map.setLatLngBoundsForCameraTarget(bounds)
                    map.setMinZoomPreference(14.0)
                    map.setMaxZoomPreference(20.0)
                    map.uiSettings.apply {
                        isRotateGesturesEnabled = false
                        isTiltGesturesEnabled = false
                        isCompassEnabled = false
                        isLogoEnabled = true   // required by OpenStreetMap attribution
                        isAttributionEnabled = true
                    }
                }

                // Wire MapView lifecycle to the Compose lifecycle owner
                val observer = object : DefaultLifecycleObserver {
                    override fun onStart(owner: LifecycleOwner) = onStart()
                    override fun onResume(owner: LifecycleOwner) = onResume()
                    override fun onPause(owner: LifecycleOwner) = onPause()
                    override fun onStop(owner: LifecycleOwner) = onStop()
                    override fun onDestroy(owner: LifecycleOwner) = onDestroy()
                }
                lifecycleOwner.lifecycle.addObserver(observer)
            }
        }
    )
}
