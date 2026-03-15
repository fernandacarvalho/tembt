import SwiftUI
import MapKit
import shared

/// UIViewRepresentable wrapping MKMapView.
/// Map is centered on the provided coordinates and restricted to a ~1 km radius.
struct MapViewRepresentable: UIViewRepresentable {

    let center: MapCoordinates

    // 1 km radius in meters for the visible span and boundary
    private let radiusMeters: CLLocationDistance = 1_000

    func makeUIView(context: Context) -> MKMapView {
        let mapView = MKMapView()
        mapView.showsUserLocation = true
        mapView.isRotateEnabled = false
        mapView.isPitchEnabled = false
        configure(mapView)
        return mapView
    }

    func updateUIView(_ mapView: MKMapView, context: Context) {
        configure(mapView)
    }

    private func configure(_ mapView: MKMapView) {
        let coordinate = CLLocationCoordinate2D(
            latitude: center.latitude,
            longitude: center.longitude
        )

        // Set the visible region to 2 km × 2 km (1 km radius in each direction)
        let region = MKCoordinateRegion(
            center: coordinate,
            latitudinalMeters: radiusMeters * 2,
            longitudinalMeters: radiusMeters * 2
        )
        mapView.setRegion(region, animated: false)

        // Prevent the user from panning the camera center beyond the 1 km radius
        let boundary = MKMapView.CameraBoundary(coordinateRegion: region)
        mapView.setCameraBoundary(boundary, animated: false)

        // Prevent zooming out beyond the 1 km radius (maxCenterCoordinateDistance = diameter)
        let zoomRange = MKMapView.CameraZoomRange(
            maxCenterCoordinateDistance: radiusMeters * 2
        )
        mapView.setCameraZoomRange(zoomRange, animated: false)
    }
}
