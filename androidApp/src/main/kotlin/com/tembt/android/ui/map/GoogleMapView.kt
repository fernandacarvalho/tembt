package com.tembt.android.ui.map

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.foundation.layout.fillMaxSize
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import org.maplibre.geojson.Feature
import org.maplibre.geojson.FeatureCollection
import org.maplibre.geojson.Point
import com.tembt.domain.model.MapCoordinates
import com.tembt.domain.model.Player
import org.maplibre.android.MapLibre
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.geometry.LatLngBounds
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.MapView
import org.maplibre.android.style.expressions.Expression.has
import org.maplibre.android.style.expressions.Expression.get
import org.maplibre.android.style.expressions.Expression.not
import org.maplibre.android.style.expressions.Expression.toString
import org.maplibre.android.style.layers.CircleLayer
import org.maplibre.android.style.layers.FillLayer
import org.maplibre.android.style.layers.LineLayer
import org.maplibre.android.style.layers.PropertyFactory.iconAllowOverlap
import org.maplibre.android.style.layers.PropertyFactory.iconIgnorePlacement
import org.maplibre.android.style.layers.PropertyFactory.iconImage
import org.maplibre.android.style.layers.PropertyFactory.circleColor
import org.maplibre.android.style.layers.PropertyFactory.circleRadius
import org.maplibre.android.style.layers.PropertyFactory.circleStrokeColor
import org.maplibre.android.style.layers.PropertyFactory.circleStrokeWidth
import org.maplibre.android.style.layers.PropertyFactory.fillColor
import org.maplibre.android.style.layers.PropertyFactory.fillOpacity
import org.maplibre.android.style.layers.PropertyFactory.lineColor
import org.maplibre.android.style.layers.PropertyFactory.lineWidth
import org.maplibre.android.style.layers.PropertyFactory.textAllowOverlap
import org.maplibre.android.style.layers.PropertyFactory.textColor
import org.maplibre.android.style.layers.PropertyFactory.textField
import org.maplibre.android.style.layers.PropertyFactory.textIgnorePlacement
import org.maplibre.android.style.layers.PropertyFactory.textSize
import org.maplibre.android.style.layers.SymbolLayer
import org.maplibre.android.style.sources.GeoJsonOptions
import org.maplibre.android.style.sources.GeoJsonSource
import org.maplibre.geojson.Polygon
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

// OpenFreeMap — free OSM-based vector tiles, no API key required.
// Styles available: liberty, bright, positron. See https://openfreemap.org
private const val MAP_STYLE = "https://tiles.openfreemap.org/styles/liberty"
private const val SOURCE_ID = "players-source"
private const val COURT_SOURCE_ID = "court-source"
private const val LAYER_CLUSTER_CIRCLE = "cluster-circle"
private const val LAYER_CLUSTER_COUNT = "cluster-count"
private const val LAYER_PLAYER_PIN = "player-pin"
private const val PLAYER_MARKER_IMAGE = "player-marker"
private const val LAYER_COURT_FILL = "court-fill"
private const val LAYER_COURT_STROKE = "court-stroke"
private const val COURT_RADIUS_METERS = 100.0

// 1 km expressed as degrees of latitude (constant regardless of position)
private const val KM_AS_LAT_DEG = 0.009

@Composable
fun MapLibreView(center: MapCoordinates, players: List<Player>, recenterTrigger: Int = 0) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val mapRef = remember { mutableStateOf<MapLibreMap?>(null) }

    val latLng = remember(center) { LatLng(center.latitude, center.longitude) }

    val bounds = remember(center) {
        val lngDelta = KM_AS_LAT_DEG / cos(center.latitude * PI / 180.0)
        LatLngBounds.Builder()
            .include(LatLng(center.latitude - KM_AS_LAT_DEG, center.longitude - lngDelta))
            .include(LatLng(center.latitude + KM_AS_LAT_DEG, center.longitude + lngDelta))
            .build()
    }

    // Re-center camera when recenterTrigger is incremented
    LaunchedEffect(recenterTrigger) {
        if (recenterTrigger > 0) {
            mapRef.value?.animateCamera(
                org.maplibre.android.camera.CameraUpdateFactory.newCameraPosition(
                    CameraPosition.Builder().target(latLng).zoom(15.0).build()
                )
            )
        }
    }

    // Update player source whenever players list changes or map becomes available
    LaunchedEffect(players, mapRef.value) {
        mapRef.value?.getStyle { style ->
            (style.getSource(SOURCE_ID) as? GeoJsonSource)
                ?.setGeoJson(toFeatureCollection(players))
        }
    }

    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = { context ->
            MapLibre.getInstance(context)
            MapView(context).apply {
                getMapAsync { map ->
                    mapRef.value = map
                    map.setStyle(MAP_STYLE) { style ->
                        setupCourtCircle(style, center)
                        setupPlayerLayers(style, players)
                    }
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

private fun setupPlayerLayers(style: org.maplibre.android.maps.Style, players: List<Player>) {
    // Clustering GeoJSON source — MapLibre merges nearby features automatically
    val source = GeoJsonSource(
        SOURCE_ID,
        toFeatureCollection(players),
        GeoJsonOptions()
            .withCluster(true)
            .withClusterMaxZoom(18)
            .withClusterRadius(50)
    )
    style.addSource(source)

    // Circle behind the cluster count
    style.addLayer(CircleLayer(LAYER_CLUSTER_CIRCLE, SOURCE_ID).apply {
        setFilter(has("point_count"))
        setProperties(
            circleRadius(18f),
            circleColor("#FF9500"),
            circleStrokeWidth(2f),
            circleStrokeColor("#FFFFFF")
        )
    })

    // Cluster count label
    style.addLayer(SymbolLayer(LAYER_CLUSTER_COUNT, SOURCE_ID).apply {
        setFilter(has("point_count"))
        setProperties(
            textField(toString(get("point_count_abbreviated"))),
            textSize(12f),
            textColor("#FFFFFF"),
            textIgnorePlacement(true),
            textAllowOverlap(true)
        )
    })

    // Individual player pin — custom bitmap icon (figure.tennis equivalent)
    style.addImage(PLAYER_MARKER_IMAGE, createPlayerMarkerBitmap())
    style.addLayer(SymbolLayer(LAYER_PLAYER_PIN, SOURCE_ID).apply {
        setFilter(not(has("point_count")))
        setProperties(
            iconImage(PLAYER_MARKER_IMAGE),
            iconAllowOverlap(true),
            iconIgnorePlacement(true)
        )
    })
}

private fun setupCourtCircle(style: org.maplibre.android.maps.Style, center: MapCoordinates) {
    val polygon = circlePolygon(center.latitude, center.longitude, COURT_RADIUS_METERS)
    style.addSource(GeoJsonSource(COURT_SOURCE_ID, Feature.fromGeometry(polygon)))

    style.addLayer(FillLayer(LAYER_COURT_FILL, COURT_SOURCE_ID).apply {
        setProperties(
            fillColor("#FF9500"),
            fillOpacity(0.10f)
        )
    })
    style.addLayer(LineLayer(LAYER_COURT_STROKE, COURT_SOURCE_ID).apply {
        setProperties(
            lineColor("#FF9500"),
            lineWidth(1.5f)
        )
    })
}

/** Approximates a geographic circle as a GeoJSON polygon with 64 points. */
private fun circlePolygon(lat: Double, lng: Double, radiusMeters: Double): Polygon {
    val points = mutableListOf<Point>()
    val steps = 64
    val earthRadius = 6_371_000.0
    val latRad = lat * PI / 180.0
    val latDelta = (radiusMeters / earthRadius) * (180.0 / PI)
    val lngDelta = (radiusMeters / (earthRadius * cos(latRad))) * (180.0 / PI)
    for (i in 0..steps) {
        val angle = 2 * PI * i / steps
        points.add(Point.fromLngLat(lng + lngDelta * cos(angle), lat + latDelta * sin(angle)))
    }
    return Polygon.fromLngLats(listOf(points))
}

/**
 * Draws a stick figure playing tennis on an orange circle — equivalent to figure.tennis.circle.fill.
 * The bitmap is registered once in the MapLibre style and reused for all player pins.
 */
private fun createPlayerMarkerBitmap(): Bitmap {
    val size = 96
    val cx = size / 2f
    val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)

    // Background circle
    val bg = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#FF9500") }
    canvas.drawCircle(cx, cx, cx - 2f, bg)
    val border = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE; style = Paint.Style.STROKE; strokeWidth = 3f
    }
    canvas.drawCircle(cx, cx, cx - 2f, border)

    // Figure: white stroke
    val fig = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE; style = Paint.Style.STROKE
        strokeWidth = 4.5f; strokeCap = Paint.Cap.ROUND
    }
    // Head
    canvas.drawCircle(cx + 3f, 19f, 7f, Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.WHITE })
    // Body (slightly leaning forward)
    canvas.drawLine(cx, 26f, cx - 5f, 56f, fig)
    // Left arm raised → racket side
    canvas.drawLine(cx, 34f, 18f, 20f, fig)
    // Right arm back
    canvas.drawLine(cx, 34f, 70f, 42f, fig)
    // Left leg forward
    canvas.drawLine(cx - 5f, 56f, 20f, 78f, fig)
    // Right leg back
    canvas.drawLine(cx - 5f, 56f, 56f, 78f, fig)
    // Racket (small oval at end of left arm)
    val racket = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE; style = Paint.Style.STROKE; strokeWidth = 3f
    }
    canvas.drawOval(6f, 6f, 24f, 20f, racket)

    return bitmap
}

private fun toFeatureCollection(players: List<Player>): FeatureCollection =
    FeatureCollection.fromFeatures(
        players.map { Feature.fromGeometry(Point.fromLngLat(it.lng, it.lat)) }
    )
