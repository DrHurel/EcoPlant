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

    fun configureParcelsOnMap(
        context: Context,
        mapView: MapView,
        parcels: List<ParcelItem>,
        onMarkerClick: (ParcelItem) -> Unit
    ) {
        val items = ArrayList<OverlayItem>()
        val parcelMap = mutableMapOf<String, ParcelItem>()

        val centerLat = 46.603354
        val centerLon = 1.888334

        parcels.forEach { parcel ->

            val offsetLat = (Math.random() - 0.5) * 5
            val offsetLon = (Math.random() - 0.5) * 10
            val point = GeoPoint(centerLat + offsetLat, centerLon + offsetLon)

            val markerId = "parcel_${parcel.id}"
            val item = OverlayItem(markerId, parcel.title, point)
            items.add(item)

            parcelMap[markerId] = parcel
        }

        val overlay = ItemizedIconOverlay(
            items,
            object : ItemizedIconOverlay.OnItemGestureListener<OverlayItem> {
                override fun onItemSingleTapUp(index: Int, item: OverlayItem): Boolean {

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