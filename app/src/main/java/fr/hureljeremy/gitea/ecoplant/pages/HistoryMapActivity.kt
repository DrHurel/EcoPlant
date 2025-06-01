package fr.hureljeremy.gitea.ecoplant.pages

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.preference.PreferenceManager
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import fr.hureljeremy.gitea.ecoplant.R
import fr.hureljeremy.gitea.ecoplant.framework.BaseActivity
import fr.hureljeremy.gitea.ecoplant.framework.Inject
import fr.hureljeremy.gitea.ecoplant.framework.OnClick
import fr.hureljeremy.gitea.ecoplant.framework.Page
import fr.hureljeremy.gitea.ecoplant.services.MapService
import fr.hureljeremy.gitea.ecoplant.services.NavigationService
import fr.hureljeremy.gitea.ecoplant.services.ParcelService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.compass.CompassOverlay

@Page(route = "history_map", layout = "history_map_page", isDefault = false)
class HistoryMapActivity : BaseActivity() {

    @Inject
    private lateinit var navigationService: NavigationService

    @Inject
    private lateinit var parcelService: ParcelService

    @Inject
    private lateinit var mapService: MapService

    private lateinit var mapView: MapView

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
                Manifest.permission.INTERNET,
                Manifest.permission.ACCESS_NETWORK_STATE
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

        val compassOverlay = CompassOverlay(this, mapView)
        compassOverlay.enableCompass()
        mapView.overlays.add(compassOverlay)

        // Charger les vraies parcelles
        loadParcels()
    }

    private fun loadParcels() {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                // Initialiser le service de parcelles
                parcelService.initialize(this@HistoryMapActivity)

                // Récupérer les parcelles réelles
                val parcels = parcelService.getParcels()

                withContext(Dispatchers.Main) {
                    if (parcels.isNotEmpty()) {
                        // Utiliser le MapService pour configurer les parcelles sur la carte
                        mapService.configureParcelsOnMap(
                            this@HistoryMapActivity,
                            mapView,
                            parcels
                        ) { parcel ->
                            // Callback lors du clic sur un marqueur
                            Log.d("HistoryMapActivity", "Parcelle sélectionnée: ${parcel.title}")
                            Toast.makeText(
                                this@HistoryMapActivity,
                                "Parcelle: ${parcel.title}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    } else {
                        Toast.makeText(
                            this@HistoryMapActivity,
                            "Aucune parcelle trouvée",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Log.e("HistoryMapActivity", "Erreur de chargement des parcelles", e)
                    Toast.makeText(
                        this@HistoryMapActivity,
                        "Erreur lors du chargement des parcelles: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (::mapView.isInitialized) {
            mapView.onResume()
        }
    }

    override fun onPause() {
        super.onPause()
        if (::mapView.isInitialized) {
            mapView.onPause()
        }
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
                1
            )
        }
    }
}