import SwiftUI
import MapKit
import shared

/// UIViewRepresentable wrapping MKMapView.
/// Map is centered on the court coordinates and restricted to a ~1 km radius.
/// Player annotations are shown with automatic MapKit clustering when pins overlap.
struct MapViewRepresentable: UIViewRepresentable {

    let center: MapCoordinates
    let players: [PlayerIos]
    var centerTrigger: Int = 0

    private let radiusMeters: CLLocationDistance = 1_000
    private let courtRadiusMeters: CLLocationDistance = 100

    func makeCoordinator() -> Coordinator {
        Coordinator()
    }

    func makeUIView(context: Context) -> MKMapView {
        let mapView = MKMapView()
        mapView.delegate = context.coordinator
        mapView.showsUserLocation = true
        mapView.isRotateEnabled = false
        mapView.isPitchEnabled = false
        mapView.register(
            MKMarkerAnnotationView.self,
            forAnnotationViewWithReuseIdentifier: "player"
        )
        mapView.register(
            MKMarkerAnnotationView.self,
            forAnnotationViewWithReuseIdentifier: MKMapViewDefaultClusterAnnotationViewReuseIdentifier
        )

        addCourtCircle(mapView)
        configure(mapView)
        return mapView
    }

    func updateUIView(_ mapView: MKMapView, context: Context) {
        if centerTrigger != context.coordinator.lastCenterTrigger {
            context.coordinator.lastCenterTrigger = centerTrigger
            configure(mapView, animated: true)
        }
        updatePlayerAnnotations(mapView)
    }

    // MARK: - Private

    private func configure(_ mapView: MKMapView, animated: Bool = false) {
        let coordinate = CLLocationCoordinate2D(
            latitude: center.latitude,
            longitude: center.longitude
        )
        let region = MKCoordinateRegion(
            center: coordinate,
            latitudinalMeters: radiusMeters * 2,
            longitudinalMeters: radiusMeters * 2
        )
        mapView.setRegion(region, animated: animated)
        if #available(iOS 16, *) {
            mapView.setCameraBoundary(
                MKMapView.CameraBoundary(coordinateRegion: region),
                animated: false
            )
            mapView.setCameraZoomRange(
                MKMapView.CameraZoomRange(maxCenterCoordinateDistance: radiusMeters * 2),
                animated: false
            )
        }
    }

    private func addCourtCircle(_ mapView: MKMapView) {
        let coordinate = CLLocationCoordinate2D(latitude: center.latitude, longitude: center.longitude)
        mapView.addOverlay(MKCircle(center: coordinate, radius: courtRadiusMeters))
    }

    private func updatePlayerAnnotations(_ mapView: MKMapView) {
        let existing = mapView.annotations.filter { $0 is PlayerAnnotation }
        mapView.removeAnnotations(existing)
        mapView.addAnnotations(players.map { PlayerAnnotation(player: $0) })
    }

    // MARK: - Coordinator (MKMapViewDelegate + UIGestureRecognizerDelegate)

    final class Coordinator: NSObject, MKMapViewDelegate {

        var lastCenterTrigger: Int = 0

        // MARK: MKMapViewDelegate

        func mapView(_ mapView: MKMapView, rendererFor overlay: MKOverlay) -> MKOverlayRenderer {
            if let circle = overlay as? MKCircle {
                let renderer = MKCircleRenderer(circle: circle)
                renderer.fillColor = UIColor(Color.appSecondary).withAlphaComponent(0.12)
                renderer.strokeColor = UIColor(Color.appSecondary).withAlphaComponent(0.60)
                renderer.lineWidth = 1.5
                return renderer
            }
            return MKOverlayRenderer(overlay: overlay)
        }

        func mapView(_ mapView: MKMapView, didSelect annotation: MKAnnotation) {
            if annotation is MKUserLocation {
                mapView.deselectAnnotation(annotation, animated: false)
            }
        }

        func mapView(_ mapView: MKMapView, viewFor annotation: MKAnnotation) -> MKAnnotationView? {
            if annotation is MKUserLocation { return nil }

            if annotation is MKClusterAnnotation {
                let view = mapView.dequeueReusableAnnotationView(
                    withIdentifier: MKMapViewDefaultClusterAnnotationViewReuseIdentifier,
                    for: annotation
                ) as? MKMarkerAnnotationView
                view?.markerTintColor = UIColor.systemOrange
                view?.titleVisibility = .hidden
                view?.subtitleVisibility = .hidden
                return view
            }

            let id = "player"
            let view = mapView.dequeueReusableAnnotationView(withIdentifier: id, for: annotation)
                as? MKMarkerAnnotationView
                ?? MKMarkerAnnotationView(annotation: annotation, reuseIdentifier: id)
            view.annotation = annotation
            view.clusteringIdentifier = id
            view.displayPriority = .defaultLow
            view.markerTintColor = UIColor(Color.appSecondary)
            view.glyphImage = UIImage(systemName: "figure.tennis.circle.fill")
            view.canShowCallout = false
            return view
        }
    }
}

// MARK: - Player annotation model

final class PlayerAnnotation: NSObject, MKAnnotation {
    let coordinate: CLLocationCoordinate2D

    init(player: PlayerIos) {
        coordinate = CLLocationCoordinate2D(latitude: player.lat, longitude: player.lng)
    }
}
