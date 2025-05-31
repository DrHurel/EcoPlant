package fr.hureljeremy.gitea.ecoplant.services

import android.content.Context
import fr.hureljeremy.gitea.ecoplant.framework.BaseService
import fr.hureljeremy.gitea.ecoplant.framework.ParcelItem
import fr.hureljeremy.gitea.ecoplant.framework.ServiceProvider
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.ItemizedIconOverlay
import org.osmdroid.views.overlay.OverlayItem

@ServiceProvider
class MapService : BaseService() {

    // Méthode pour configurer la carte avec des parcelles
    fun configureParcelsOnMap(
        context: Context,
        mapView: MapView,
        parcels: List<ParcelItem>,
        onMarkerClick: (ParcelItem) -> Unit
    ) {
        // Initialiser les marqueurs
        val items = ArrayList<OverlayItem>()
        val parcelMap = mutableMapOf<String, ParcelItem>()

        // Coordonnées fictives pour démonstration
        val centerLat = 46.603354
        val centerLon = 1.888334

        // Ajouter un marqueur pour chaque parcelle
        parcels.forEach { parcel ->
            // Simuler des coordonnées autour du centre
            val offsetLat = (Math.random() - 0.5) * 5
            val offsetLon = (Math.random() - 0.5) * 10
            val point = GeoPoint(centerLat + offsetLat, centerLon + offsetLon)

            // Créer un marqueur avec un identifiant unique
            val markerId = "parcel_${parcel.id}"
            val item = OverlayItem(markerId, parcel.title, point)
            items.add(item)

            // Stocker la correspondance entre l'identifiant du marqueur et la parcelle
            parcelMap[markerId] = parcel
        }

        // Gérer les clics sur les marqueurs
        val overlay = ItemizedIconOverlay(items,
            object : ItemizedIconOverlay.OnItemGestureListener<OverlayItem> {
                override fun onItemSingleTapUp(index: Int, item: OverlayItem): Boolean {
                    // Récupérer la parcelle correspondante et appeler le callback
                    parcelMap[item.title]?.let { parcel ->
                        onMarkerClick(parcel)
                    }
                    return true
                }

                override fun onItemLongPress(index: Int, item: OverlayItem): Boolean {
                    return false
                }
            }, context
        )

        mapView.overlays.add(overlay)
        mapView.invalidate()
    }
}