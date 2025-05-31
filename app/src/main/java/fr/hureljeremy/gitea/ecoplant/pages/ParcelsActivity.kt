package fr.hureljeremy.gitea.ecoplant.pages

import android.app.Dialog
import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import android.view.Window
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.widget.SwitchCompat
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        recyclerView = findViewById(R.id.parcels_recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(this)

// Uniquement la partie du code qui initialise l'adaptateur
        adapter = ParcelsAdapter(
            parcelItems,
            parcelService,
            lifecycleScope,  // Ajouter ce paramètre
            onItemClick = { item ->
                Toast.makeText(this, "Parcelle sélectionnée : ${item.title}", Toast.LENGTH_SHORT)
                    .show()
            },
            onDeleteClick = { item ->
                deleteParcel(item)
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
                // Initialiser le service avec le contexte
                parcelService.initialize(this@ParcelsActivity)

                // Récupérer directement la liste des parcelles
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
                // Initialiser explicitement le service avant de l'utiliser
                parcelService.initialize(this@ParcelsActivity)

                val newParcel = ParcelItem(
                    id = 0,
                    title = name,
                    minimumReliabilityScore = 50.0,
                    isPublic = isPublic,
                    latitude = coordinates.split(",").getOrNull(0)?.trim() ?: "",
                    longitude = coordinates.split(",").getOrNull(1)?.trim() ?: ""
                )
                val success = parcelService.updateParcel(newParcel)

                withContext(Dispatchers.Main) {
                    if (success) {
                        Toast.makeText(
                            this@ParcelsActivity,
                            "Votre parcelle \"$name\" a bien été créée en visibilité ${if (isPublic) "publique" else "privée"}",
                            Toast.LENGTH_LONG
                        ).show()

                        loadParcels()
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
}