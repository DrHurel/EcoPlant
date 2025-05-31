package fr.hureljeremy.gitea.ecoplant.pages

import android.app.Dialog
import android.icu.text.DateFormat
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.core.net.toUri
import androidx.lifecycle.lifecycleScope
import fr.hureljeremy.gitea.ecoplant.R
import fr.hureljeremy.gitea.ecoplant.framework.BaseActivity
import fr.hureljeremy.gitea.ecoplant.framework.Inject
import fr.hureljeremy.gitea.ecoplant.framework.OnClick
import fr.hureljeremy.gitea.ecoplant.framework.Page
import fr.hureljeremy.gitea.ecoplant.framework.ParcelItem
import fr.hureljeremy.gitea.ecoplant.framework.SavedIdentificationResult
import fr.hureljeremy.gitea.ecoplant.framework.ServiceEntry
import fr.hureljeremy.gitea.ecoplant.services.CameraService
import fr.hureljeremy.gitea.ecoplant.services.NavigationService
import fr.hureljeremy.gitea.ecoplant.services.ParcelService
import fr.hureljeremy.gitea.ecoplant.services.PlantNetService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Page(route = "plant_info", layout = "display_plant_info_page", isDefault = false)
class DisplayPlantInfoActivity : BaseActivity() {
    @Inject
    private lateinit var navigationService: NavigationService

    @Inject
    private lateinit var plantNetService: PlantNetService

    @Inject
    private lateinit var parcelService: ParcelService

    @Inject
    private lateinit var cameraService: CameraService

    private lateinit var plantImageView: ImageView
    private lateinit var plantNameTextView: TextView
    private lateinit var plantDescriptionTextView: TextView

    private var plantName: String = "Unknown Plant"
    private var plantDescription: String = "No description available"
    private var plantImageUri: String? = null
    private val parcelItems = mutableListOf<ParcelItem>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        plantImageView = findViewById(R.id.plant_image)
        plantNameTextView = findViewById(R.id.plant_name)
        plantDescriptionTextView = findViewById(R.id.plant_descriptions)

        loadPlantData()
        displayPlantInformation()
        fetchPlantServices()
        loadParcels()
    }

    private fun loadParcels() {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val parcels = mutableListOf<ParcelItem>()
                val iterator = parcelService.getParcels()

                while (iterator.hasNext()) {
                    parcels.add(iterator.next())
                }

                withContext(Dispatchers.Main) {
                    parcelItems.clear()
                    parcelItems.addAll(parcels)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@DisplayPlantInfoActivity,
                        "Erreur lors du chargement des parcelles: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    private fun configureSaveDialog(dialog: Dialog) {
        val spinner = dialog.findViewById<Spinner>(R.id.parcel_spinner)

        if (parcelItems.isEmpty()) {
            Toast.makeText(
                this,
                "Aucune parcelle disponible. Veuillez en créer une d'abord.",
                Toast.LENGTH_LONG
            ).show()
            dialog.dismiss()
            navigationService.navigate(this, "parcels")
            return
        }

        // Adapter pour afficher les titres des parcelles dans le spinner
        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            parcelItems.map { it.title }
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter

        var selectedParcelPosition = 0
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View?,
                position: Int,
                id: Long
            ) {
                selectedParcelPosition = position
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        dialog.findViewById<Button>(R.id.cancel_button).setOnClickListener {
            dialog.dismiss()
        }

        dialog.findViewById<Button>(R.id.confirm_button).setOnClickListener {
            if (parcelItems.isNotEmpty() && selectedParcelPosition < parcelItems.size) {
                val selectedParcel = parcelItems[selectedParcelPosition]
                saveIdentificationToParcel(selectedParcel.id.toInt())
                dialog.dismiss()
            }
        }
    }

    private fun saveIdentificationToParcel(parcelId: Int) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                // Créer un nouvel objet SavedIdentificationResult
                val result = cameraService.getLastImageUri()?.let {
                    SavedIdentificationResult(
                        species = plantName,
                        date = DateFormat.getDateInstance().format(System.currentTimeMillis())
                            .toString(),
                        imageUri = it,
                        description = plantDescription
                    )
                }


                // Ajouter le résultat à la parcelle
                if (result != null) {
                    parcelService.addIdentificationResult(parcelId, result)
                } else {
                    throw IllegalArgumentException("Plant image URI is null")
                }

                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@DisplayPlantInfoActivity,
                        "Identification de $plantName sauvegardée dans la parcelle",
                        Toast.LENGTH_LONG
                    ).show()

                    // Navigation optionnelle vers la parcelle ou autre écran
                    navigationService.navigate(this@DisplayPlantInfoActivity, "home")
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@DisplayPlantInfoActivity,
                        "Erreur lors de la sauvegarde: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    private fun loadPlantData() {
        plantName = intent.extras?.getString("PLANT_NAME") ?: "Unknown Plant"
        plantDescription =
            intent.extras?.getString("PLANT_DESCRIPTION") ?: "No description available"
        plantImageUri = intent.extras?.getString("PLANT_IMAGE_URI")

        plantImageUri?.let { uriString ->
            plantImageView.setImageURI(uriString.toUri())
            plantImageView.scaleType = ImageView.ScaleType.CENTER_CROP
        }
    }


    private fun displayPlantInformation() {
        plantNameTextView.hint = plantName
        plantDescriptionTextView.hint = plantDescription
    }

    private fun fetchPlantServices() {
        lifecycleScope.launch(Dispatchers.IO) {
            val plantServices = plantNetService.getPlantScore(plantName)

            val updatedDescription = plantServices.fold(
                onSuccess = { services ->
                    buildServiceDescription(services)
                },
                onFailure = { error ->
                    "$plantDescription\n\nError fetching plant service details: ${error.message}"
                }
            )

            withContext(Dispatchers.Main) {
                plantDescriptionTextView.hint = updatedDescription
            }
        }
    }

    private fun buildServiceDescription(services: List<ServiceEntry>): String {
        var description = plantDescription

        if (services.isNotEmpty()) {
            for (serviceEntry in services) {
                description += "\n\nThis plant provide as a service: ${serviceEntry.service}\n" +
                        "It has a score of ${serviceEntry.value} out of 100.\n" +
                        "This information has a reliability of ${serviceEntry.reliability} out of 100.\n"
            }
        } else {
            description += "\n\nNo plant service details available."
        }

        return description
    }

    @OnClick("delete_button")
    fun onDeleteButtonClick() {
        navigationService.navigate(this, "home")
    }

    @OnClick("know_more_button")
    fun onKnowMoreButtonClick() {
        plantNetService.displayPlantDetails(plantName)
    }

    @OnClick("save_button")
    private fun showSaveParcelDialog() {
        val dialog = createSaveDialog()
        configureSaveDialog(dialog)
        showDialog(dialog)
    }

    private fun createSaveDialog(): Dialog {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.confirm_save_scanner_alert)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        return dialog
    }


    private fun setupSpinnerAdapter(spinner: Spinner, parcelles: Array<String>) {
        val adapter = object : ArrayAdapter<String>(this, R.layout.spinner_item, parcelles) {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                return getCustomTextView(super.getView(position, convertView, parent))
            }

            override fun getDropDownView(
                position: Int,
                convertView: View?,
                parent: ViewGroup
            ): View {
                return getCustomTextView(super.getDropDownView(position, convertView, parent))
            }

            private fun getCustomTextView(view: View): View {
                (view as TextView).textSize = 18f
                return view
            }
        }
        spinner.adapter = adapter
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