package fr.hureljeremy.gitea.ecoplant.pages

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.preference.PreferenceManager
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import fr.hureljeremy.gitea.ecoplant.R
import fr.hureljeremy.gitea.ecoplant.framework.BaseActivity
import fr.hureljeremy.gitea.ecoplant.framework.Inject
import fr.hureljeremy.gitea.ecoplant.framework.OnClick
import fr.hureljeremy.gitea.ecoplant.framework.Page
import fr.hureljeremy.gitea.ecoplant.framework.ParcelItem
import fr.hureljeremy.gitea.ecoplant.services.MapService
import fr.hureljeremy.gitea.ecoplant.services.NavigationService
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.ItemizedIconOverlay
import org.osmdroid.views.overlay.OverlayItem
import org.osmdroid.views.overlay.compass.CompassOverlay
import org.osmdroid.views.overlay.gestures.RotationGestureOverlay

@Page(route = "history_map", layout = "history_map_page", isDefault = false)
class HistoryMapActivity : BaseActivity() {

    @Inject
    private lateinit var navigationService: NavigationService

    @Inject
    private lateinit var mapService: MapService

    private lateinit var mapView: MapView
    private val REQUEST_PERMISSIONS_REQUEST_CODE = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Configuration d'OSMDroid
        Configuration.getInstance().load(
            applicationContext,
            PreferenceManager.getDefaultSharedPreferences(applicationContext)
        )

        // Vérifier et demander les permissions
        requestPermissionsIfNecessary(
            arrayOf(
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.ACCESS_NETWORK_STATE,
                Manifest.permission.INTERNET
            )
        )

        // Initialisation de la carte
        mapView = findViewById(R.id.map_view)
        mapView.setTileSource(TileSourceFactory.MAPNIK)
        mapView.setMultiTouchControls(true)

        // Configuration de la carte
        val mapController = mapView.controller
        mapController.setZoom(5.5)
        val startPoint = GeoPoint(46.603354, 1.888334) // Centre de la France
        mapController.setCenter(startPoint)

        // Ajouter une boussole
        val compassOverlay = CompassOverlay(this, mapView)
        compassOverlay.enableCompass()
        mapView.overlays.add(compassOverlay)

        // Ajouter la possibilité de rotation avec deux doigts
        val rotationGestureOverlay = RotationGestureOverlay(mapView)
        rotationGestureOverlay.isEnabled = true
        mapView.overlays.add(rotationGestureOverlay)

        // Chargement des données mockées (pour éviter le crash)
        displayMockParcels()
    }

    private fun displayMockParcels() {
        // Création de parcelles simulées pour démonstration
        val mockParcels = listOf(
            ParcelItem(1, "Parcelle Forêt", 50.0, true),
            ParcelItem(2, "Champ de Blé", 60.0, true),
            ParcelItem(3, "Verger", 70.0, false),
            ParcelItem(4, "Jardin Botanique", 80.0, true),
            ParcelItem(5, "Parc Naturel", 90.0, false)
        )

        // Initialiser les marqueurs
        val items = ArrayList<OverlayItem>()

        // Coordonnées fictives pour démonstration
        val centerLat = 46.603354
        val centerLon = 1.888334

        // Ajouter un marqueur pour chaque parcelle
        mockParcels.forEach { parcel ->
            // Simuler des coordonnées autour du centre
            val offsetLat = (Math.random() - 0.5) * 5
            val offsetLon = (Math.random() - 0.5) * 10
            val point = GeoPoint(centerLat + offsetLat, centerLon + offsetLon)

            // Créer un marqueur
            val item = OverlayItem(parcel.title, "ID: ${parcel.id}", point)
            items.add(item)
        }

        // Gérer les clics sur les marqueurs
        val overlay = ItemizedIconOverlay(items,
            object : ItemizedIconOverlay.OnItemGestureListener<OverlayItem> {
                override fun onItemSingleTapUp(index: Int, item: OverlayItem): Boolean {
                    // Afficher les détails de la parcelle
                    showParcelDetails(item.title)
                    return true
                }

                override fun onItemLongPress(index: Int, item: OverlayItem): Boolean {
                    return false
                }
            }, applicationContext
        )

        mapView.overlays.add(overlay)
        mapView.invalidate()
    }

    private fun showParcelDetails(parcelTitle: String) {
        // À implémenter: naviguer vers la page de détails de la parcelle
        Log.d("HistoryMapActivity", "Parcelle sélectionnée: $parcelTitle")
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

    @OnClick("home_button")
    fun navigateToHome() {
        navigationService.navigate(this, "home")
    }

    @OnClick("history_button")
    fun navigateToHistory() {
        navigationService.navigate(this, "history")
    }

    private fun requestPermissionsIfNecessary(permissions: Array<String>) {
        val permissionsToRequest = ArrayList<String>()
        for (permission in permissions) {
            if (ContextCompat.checkSelfPermission(this, permission) !=
                PackageManager.PERMISSION_GRANTED
            ) {
                permissionsToRequest.add(permission)
            }
        }
        if (permissionsToRequest.isNotEmpty()) {
            ActivityCompat.requestPermissions(
                this,
                permissionsToRequest.toTypedArray(),
                REQUEST_PERMISSIONS_REQUEST_CODE
            )
        }
    }
}