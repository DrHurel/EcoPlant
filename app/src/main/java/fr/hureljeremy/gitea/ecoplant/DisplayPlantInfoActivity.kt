package fr.hureljeremy.gitea.ecoplant

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
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import fr.hureljeremy.gitea.ecoplant.framework.BaseActivity
import fr.hureljeremy.gitea.ecoplant.framework.Page

@Page(route = "plant_info", isDefault = false)
class DisplayPlantInfoActivity : BaseActivity()  {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.display_plant_info_page)


        findViewById<Button>(R.id.save_button).setOnClickListener {
            showSaveParcelDialog()
        }
    }

    private fun showSaveParcelDialog() {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)  // Supprimer la barre de titre
        dialog.setContentView(R.layout.confirm_save_scanner_alert)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent) // On utilise ? car il peut être null

        // Configuration du spinner
        val spinner = dialog.findViewById<Spinner>(R.id.parcel_spinner)

        // Liste des parcelles (à remplacer par vos données réelles)
        val parcelles = arrayOf("Parcelle A", "Parcelle B", "Parcelle C", "Parcelle D", "Parcelle E")

        // Création d'un adaptateur personnalisé utilisant spinner_item.xml
        val adapter = object : ArrayAdapter<String>(
            this,
            R.layout.spinner_item,
            parcelles
        ) {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = super.getView(position, convertView, parent)
                val textView = view as TextView
                textView.textSize = 18f
                return view
            }

            override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = super.getDropDownView(position, convertView, parent)
                val textView = view as TextView
                textView.textSize = 18f
                return view
            }
        }

        // Application de l'adaptateur au spinner
        spinner.adapter = adapter

        // Gestion de la sélection du spinner
        var selectedParcel = parcelles[0] // Valeur par défaut
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                selectedParcel = parcelles[position]
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // Ne rien faire
            }
        }

        // Configuration des bouton
        val btnCancel = dialog.findViewById<Button>(R.id.cancel_button)
        val btnConfirm = dialog.findViewById<Button>(R.id.confirm_button)

        btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        btnConfirm.setOnClickListener {
            // Affichage simple
            Toast.makeText(
                this,
                "Plante sauvegardée dans la parcelle: $selectedParcel",
                Toast.LENGTH_SHORT
            ).show()


            dialog.dismiss()
        }

        dialog.show()

        // Redimensionner la boîte de dialogue après l'affichage
        val displayMetrics = resources.displayMetrics
        val width = (displayMetrics.widthPixels * 0.95).toInt()  // 95% de la largeur d'écran
        val height = (displayMetrics.heightPixels * 0.4).toInt() // 40% de la hauteur d'écran
        dialog.window?.setLayout(width, height)
    }
}