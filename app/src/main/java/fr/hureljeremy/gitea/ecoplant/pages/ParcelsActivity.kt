package fr.hureljeremy.gitea.ecoplant.pages

    import android.app.Dialog
    import android.os.Bundle
    import android.view.ViewGroup
    import android.view.Window
    import android.widget.Button
    import android.widget.EditText
    import android.widget.Toast
    import androidx.recyclerview.widget.LinearLayoutManager
    import androidx.recyclerview.widget.RecyclerView
    import fr.hureljeremy.gitea.ecoplant.R
    import fr.hureljeremy.gitea.ecoplant.framework.BaseActivity
    import fr.hureljeremy.gitea.ecoplant.framework.Inject
    import fr.hureljeremy.gitea.ecoplant.framework.OnClick
    import fr.hureljeremy.gitea.ecoplant.framework.Page
    import fr.hureljeremy.gitea.ecoplant.models.ParcelItem
    import fr.hureljeremy.gitea.ecoplant.models.ParcelsAdapter
    import fr.hureljeremy.gitea.ecoplant.services.NavigationService

@Page(route = "parcels", layout = "parcels_page", isDefault = false)
    class ParcelsActivity : BaseActivity() {
        @Inject
        private lateinit var navigationService: NavigationService

        private lateinit var recyclerView: RecyclerView
        private lateinit var adapter: ParcelsAdapter

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)

            recyclerView = findViewById(R.id.parcels_recycler_view)
            recyclerView.layoutManager = LinearLayoutManager(this)

            val parcelItems = createSampleData()
            adapter = ParcelsAdapter(parcelItems) { item ->
                Toast.makeText(this, "Parcelle sélectionnée : ${item.title}", Toast.LENGTH_SHORT).show()
            }
            recyclerView.adapter = adapter
        }

        @OnClick("home_button")
        fun navigateToHome() {
            navigationService.navigate(this, "home")
        }

        @OnClick("add_parcel_button")
        fun popupCreateParcel(){
            val dialog = createSaveDialog()
            configureSaveDialog(dialog)
            showDialog(dialog)
        }

        private fun createSampleData(): List<ParcelItem> {
            return listOf(
                ParcelItem(1, "Parcelle A", listOf(), 70.0, true),
                ParcelItem(2, "Parcelle B", listOf(), 50.0, false),
                ParcelItem(3, "Parcelle C", listOf(), 60.0, true),
                ParcelItem(4, "Parcelle D", listOf(), 80.0, false)
            )
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
            val visibilitySwitch = dialog.findViewById<androidx.appcompat.widget.SwitchCompat>(R.id.visibility_switch)
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

                val visibilityText = if (isPublic) "publique" else "privée"

                Toast.makeText(
                    this,
                    "Votre parcelle \"$parcelName\" ($coordinates) a bien été créée en visibilité $visibilityText",
                    Toast.LENGTH_LONG
                ).show()

                dialog.dismiss()
            }
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