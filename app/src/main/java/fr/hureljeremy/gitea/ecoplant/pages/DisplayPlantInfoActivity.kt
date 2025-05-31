package fr.hureljeremy.gitea.ecoplant.pages

import android.app.Dialog
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
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
import fr.hureljeremy.gitea.ecoplant.framework.ServiceEntry
import fr.hureljeremy.gitea.ecoplant.services.NavigationService
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

    private lateinit var plantImageView: android.widget.ImageView

    private lateinit var plantNameTextView: TextView

    private lateinit var plantDescriptionTextView: TextView

    private var plantName: String = "Unknown Plant"
    private var plantDescription: String = "No description available"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        plantImageView = findViewById(R.id.plant_image)
        plantNameTextView = findViewById(R.id.plant_name)
        plantDescriptionTextView = findViewById(R.id.plant_descriptions)
        loadPlantData()
        displayPlantInformation()
        fetchPlantServices()
    }

    private fun loadPlantData() {
        plantName = intent.extras?.getString("PLANT_NAME") ?: "Unknown Plant"
        plantDescription =
            intent.extras?.getString("PLANT_DESCRIPTION") ?: "No description available"

        intent.extras?.getString("PLANT_IMAGE_URI")?.let { uriString ->
            plantImageView.setImageURI(uriString.toUri())
            plantImageView.scaleType = android.widget.ImageView.ScaleType.CENTER_CROP
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

    private fun configureSaveDialog(dialog: Dialog) {
        val spinner = dialog.findViewById<Spinner>(R.id.parcel_spinner)
        val parcelles =
            arrayOf("Parcelle A", "Parcelle B", "Parcelle C", "Parcelle D", "Parcelle E")

        setupSpinnerAdapter(spinner, parcelles)

        var selectedParcel = parcelles[0]
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View?,
                position: Int,
                id: Long
            ) {
                selectedParcel = parcelles[position]
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        dialog.findViewById<Button>(R.id.cancel_button).setOnClickListener {
            dialog.dismiss()
        }

        dialog.findViewById<Button>(R.id.confirm_button).setOnClickListener {
            Toast.makeText(
                this, "Plante sauvegardée dans la parcelle: $selectedParcel", Toast.LENGTH_SHORT
            ).show()
            dialog.dismiss()
        }
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
            setGravity(android.view.Gravity.CENTER_HORIZONTAL or android.view.Gravity.CENTER_VERTICAL)
            attributes = attributes.apply {
                y = (displayMetrics.heightPixels * 0.05).toInt()
            }
        }
    }
}