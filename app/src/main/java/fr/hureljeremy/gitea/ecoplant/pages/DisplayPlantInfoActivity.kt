package fr.hureljeremy.gitea.ecoplant.pages

import android.app.Dialog
import android.os.Bundle
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

    private var plantName: String = "Unknown Plant"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        plantName = intent.extras?.getString("PLANT_NAME") ?: "Unknown Plant"

        val imageUriString = intent.extras?.getString("PLANT_IMAGE_URI")
        imageUriString?.let { uriString ->
            val imageUri = uriString.toUri()
            findViewById<ImageView>(R.id.plant_image).setImageURI(imageUri)
            findViewById<ImageView>(R.id.plant_image).scaleType = ImageView.ScaleType.CENTER_CROP
        }

        var description =
            intent.extras?.getString("PLANT_DESCRIPTION") ?: "No description available"

        findViewById<TextView>(R.id.plant_name).hint = plantName
        findViewById<TextView>(R.id.plant_descriptions).hint = description

        lifecycleScope.launch(Dispatchers.IO) {
            val plantServices = plantNetService.getPlantScore(plantName)

            plantServices.fold(
                onSuccess = { services ->
                    for (serviceEntry in services) {
                        description += "\n\nThis plant provide as a service: ${serviceEntry.service}\n" +
                                "It has a score of ${serviceEntry.value} out of 100.\n" +
                                "This information has a reliability of ${serviceEntry.reliability} out of 100.\n"
                    }

                    if (services.isEmpty()) {
                        description += "\n\nNo plant service details available."
                    }

                },
                onFailure = { error ->
                    description += "\n\nError fetching plant service details: ${error.message}"
                }
            )

            withContext(Dispatchers.Main) {
                findViewById<TextView>(R.id.plant_descriptions).hint = description
            }
        }
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
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.confirm_save_scanner_alert)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        val spinner = dialog.findViewById<Spinner>(R.id.parcel_spinner)
        val parcelles =
            arrayOf("Parcelle A", "Parcelle B", "Parcelle C", "Parcelle D", "Parcelle E")

        val adapter = object : ArrayAdapter<String>(
            this, R.layout.spinner_item, parcelles
        ) {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = super.getView(position, convertView, parent)
                val textView = view as TextView
                textView.textSize = 18f
                return view
            }

            override fun getDropDownView(
                position: Int, convertView: View?, parent: ViewGroup
            ): View {
                val view = super.getDropDownView(position, convertView, parent)
                val textView = view as TextView
                textView.textSize = 18f
                return view
            }
        }

        spinner.adapter = adapter

        var selectedParcel = parcelles[0]
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>, view: View?, position: Int, id: Long
            ) {
                selectedParcel = parcelles[position]
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // Ne rien faire
            }
        }

        val btnCancel = dialog.findViewById<Button>(R.id.cancel_button)
        val btnConfirm = dialog.findViewById<Button>(R.id.confirm_button)

        btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        btnConfirm.setOnClickListener {
            Toast.makeText(
                this, "Plante sauvegardée dans la parcelle: $selectedParcel", Toast.LENGTH_SHORT
            ).show()
            dialog.dismiss()
        }

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