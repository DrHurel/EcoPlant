package fr.hureljeremy.gitea.ecoplant.pages

import android.Manifest
import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import android.view.Window
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.widget.SwitchCompat
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import fr.hureljeremy.gitea.ecoplant.R
import fr.hureljeremy.gitea.ecoplant.framework.BaseActivity
import fr.hureljeremy.gitea.ecoplant.framework.Inject
import fr.hureljeremy.gitea.ecoplant.framework.OnClick
import fr.hureljeremy.gitea.ecoplant.framework.Page
import fr.hureljeremy.gitea.ecoplant.framework.ParcelItem
import fr.hureljeremy.gitea.ecoplant.models.ParcelsAdapter
import fr.hureljeremy.gitea.ecoplant.services.NavigationService
import fr.hureljeremy.gitea.ecoplant.services.ParcelService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Page(route = "parcels", layout = "parcels_page", isDefault = false)
class ParcelsActivity : BaseActivity() {
    @Inject
    private lateinit var navigationService: NavigationService

    @Inject
    private lateinit var parcelService: ParcelService

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ParcelsAdapter
    private val parcelItems = mutableListOf<ParcelItem>()

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1001
    }


    private var pendingLocationDialog: Dialog? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        recyclerView = findViewById(R.id.parcels_recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(this)

        adapter = ParcelsAdapter(
            parcelItems,
            parcelService,
            lifecycleScope,
            onItemClick = { item ->
                Toast.makeText(this, "Parcelle sélectionnée : ${item.title}", Toast.LENGTH_SHORT)
                    .show()
            },
            onDeleteClick = { item ->
                deleteParcel(item)
                updateParcelsList(parcelItems.filter { it.id != item.id })
            },
            onSaveClick = { item, newTitle, newReliabilityScore, isPublic ->
                updateParcel(item, newTitle, newReliabilityScore, isPublic)

            },
            onManageUsersClick = { item ->
                manageUsers(item)
            }
        )
        recyclerView.adapter = adapter

        loadParcels()
    }

    private fun loadParcels() {
        lifecycleScope.launch(Dispatchers.IO) {
            try {

                parcelService.initialize(this@ParcelsActivity)


                val parcels = parcelService.getParcels()

                withContext(Dispatchers.Main) {
                    parcelItems.clear()
                    parcelItems.addAll(parcels)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@ParcelsActivity,
                        "Erreur lors du chargement des parcelles: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    private fun deleteParcel(parcel: ParcelItem) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val success = parcelService.deleteParcel(parcel)
                withContext(Dispatchers.Main) {
                    if (success) {
                        Toast.makeText(
                            this@ParcelsActivity,
                            "Parcelle \"${parcel.title}\" supprimée",
                            Toast.LENGTH_SHORT
                        ).show()
                        loadParcels()
                    } else {
                        Toast.makeText(
                            this@ParcelsActivity,
                            "Échec de la suppression de la parcelle",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@ParcelsActivity,
                        "Erreur: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun updateParcel(
        parcel: ParcelItem,
        newTitle: String,
        newReliabilityScore: Double,
        isPublic: Boolean
    ) {
        val updatedParcel = ParcelItem(
            id = parcel.id,
            title = newTitle,
            minimumReliabilityScore = newReliabilityScore,
            isPublic = isPublic,
            latitude = parcel.latitude,
            longitude = parcel.longitude
        )

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val success = parcelService.updateParcel(updatedParcel)
                withContext(Dispatchers.Main) {
                    if (success) {
                        Toast.makeText(
                            this@ParcelsActivity,
                            "Parcelle mise à jour avec succès",
                            Toast.LENGTH_SHORT
                        ).show()
                        loadParcels()
                    } else {
                        Toast.makeText(
                            this@ParcelsActivity,
                            "Échec de la mise à jour de la parcelle",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@ParcelsActivity,
                        "Erreur: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun manageUsers(parcel: ParcelItem) {
        Toast.makeText(
            this,
            "Gestion des utilisateurs pour ${parcel.title} - Fonctionnalité à venir",
            Toast.LENGTH_SHORT
        ).show()
        // Implémentation future pour la gestion des utilisateurs
        // navigationService.navigate(this, "manage_users", Bundle().apply { putLong("PARCEL_ID", parcel.id) })
    }

    @OnClick("home_button")
    fun navigateToHome() {
        navigationService.navigate(this, "home")
    }

    @OnClick("add_parcel_button")
    fun popupCreateParcel() {
        val dialog = createSaveDialog()
        configureSaveDialog(dialog)
        showDialog(dialog)
    }

    private fun createSaveDialog(): Dialog {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.create_parcel_alert)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        return dialog
    }

    private fun configureSaveDialog(dialog: Dialog) {
        val parcelNameField = dialog.findViewById<EditText>(R.id.parcel_name)
        val visibilitySwitch = dialog.findViewById<SwitchCompat>(R.id.visibility_switch)
        val coordinatesField = dialog.findViewById<EditText>(R.id.parcel_coordinates)
        val getLocationButton = dialog.findViewById<ImageButton>(R.id.get_location_button)

        getLocationButton.setOnClickListener {
            pendingLocationDialog = dialog
            requestLocationPermission()
        }
        dialog.findViewById<Button>(R.id.cancel_button).setOnClickListener {
            dialog.dismiss()
        }

        dialog.findViewById<Button>(R.id.confirm_button).setOnClickListener {
            val parcelName = parcelNameField.text.toString()
            val isPublic = visibilitySwitch.isChecked
            val coordinates = coordinatesField.text.toString()

            if (parcelName.isEmpty()) {
                parcelNameField.error = "Veuillez entrer un nom de parcelle"
                return@setOnClickListener
            }

            if (coordinates.isEmpty()) {
                coordinatesField.error = "Veuillez entrer des coordonnées"
                return@setOnClickListener
            }

            saveNewParcel(parcelName, isPublic, coordinates)
            dialog.dismiss()
        }
    }

    private fun saveNewParcel(name: String, isPublic: Boolean, coordinates: String) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {

                parcelService.initialize(this@ParcelsActivity)

                val newParcel = ParcelItem(
                    id = 0,
                    title = name,
                    minimumReliabilityScore = 50.0,
                    isPublic = isPublic,
                    latitude = coordinates.split(",").getOrNull(0)?.trim() ?: "",
                    longitude = coordinates.split(",").getOrNull(1)?.trim() ?: ""
                )
                val success = parcelService.createParcel(newParcel)

                withContext(Dispatchers.Main) {
                    if (success) {
                        Toast.makeText(
                            this@ParcelsActivity,
                            "Votre parcelle \"$name\" a bien été créée en visibilité ${if (isPublic) "publique" else "privée"}",
                            Toast.LENGTH_LONG
                        ).show()

                        loadParcels()
                        updateParcelsList(parcelService.getParcels())
                    } else {
                        Toast.makeText(
                            this@ParcelsActivity,
                            "Erreur lors de la création de la parcelle",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@ParcelsActivity,
                        "Erreur lors de la création de la parcelle: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    private fun showDialog(dialog: Dialog) {
        dialog.show()

        val displayMetrics = resources.displayMetrics
        val width = (displayMetrics.widthPixels * 0.95).toInt()
        dialog.window?.apply {
            setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT)
            setGravity(Gravity.CENTER_HORIZONTAL or Gravity.CENTER_VERTICAL)
            attributes = attributes.apply {
                y = (displayMetrics.heightPixels * 0.05).toInt()
            }
        }
    }

    fun updateParcelsList(newParcels: List<ParcelItem>) {
        parcelItems.clear()
        parcelItems.addAll(newParcels)
        adapter.notifyDataSetChanged()
    }

    private fun requestLocationPermission() {
        when {
            ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) ==
                    PackageManager.PERMISSION_GRANTED -> {
                getLastLocation()
            }

            shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION) -> {
                Toast.makeText(
                    this,
                    "La permission de localisation est nécessaire pour obtenir vos coordonnées",
                    Toast.LENGTH_LONG
                ).show()
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    LOCATION_PERMISSION_REQUEST_CODE
                )
            }

            else -> {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    LOCATION_PERMISSION_REQUEST_CODE
                )
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun getLastLocation() {
        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager

        try {
            val location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                ?: locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)

            if (location != null) {
                updateCoordinatesField(location)
            } else {
                Toast.makeText(
                    this,
                    "Impossible d'obtenir votre position actuelle",
                    Toast.LENGTH_SHORT
                ).show()
            }
        } catch (e: Exception) {
            Toast.makeText(
                this,
                "Erreur lors de l'obtention de la position: ${e.message}",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun updateCoordinatesField(location: Location) {
        pendingLocationDialog?.let { dialog ->
            val coordinatesField = dialog.findViewById<EditText>(R.id.parcel_coordinates)
            coordinatesField.setText("${location.latitude}, ${location.longitude}")
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLastLocation()
            } else {
                Toast.makeText(
                    this,
                    "Permission refusée, impossible d'obtenir votre position",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

}