package fr.hureljeremy.gitea.ecoplant.components

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.TextView
import fr.hureljeremy.gitea.ecoplant.R
import fr.hureljeremy.gitea.ecoplant.framework.BaseDialog
import fr.hureljeremy.gitea.ecoplant.framework.OnClick

class SaveParcelDialog(
    private val context: Context,
    private val plantName: String,
    private val onConfirm: (String) -> Unit
) : BaseDialog(context) {

    private val parcelles =
        arrayOf("Parcelle A", "Parcelle B", "Parcelle C", "Parcelle D", "Parcelle E")
    private var selectedParcel = parcelles[0]

    override fun configureDialog() {
        dialog.setContentView(R.layout.confirm_save_scanner_alert)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        setupSpinner()
        setupDialogSize()
    }

private fun setupSpinner() {
    val spinner = dialog.findViewById<Spinner>(R.id.parcel_spinner) ?: return

    // Utiliser dialog.context au lieu de context
    val adapter = ArrayAdapter(
        dialog.context,
        android.R.layout.simple_spinner_item,
        parcelles.toList()  // parcelles est non-nullable
    )

    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
    spinner.adapter = adapter

    spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
        override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
            // parcelles est non-nullable
            if (position >= 0 && position < parcelles.size) {
                selectedParcel = parcelles[position]
            }
        }

        override fun onNothingSelected(parent: AdapterView<*>) {
            // Ne rien faire
        }
    }
}
    @OnClick("cancel_button")
    fun onCancelClicked() {
        dialog.dismiss()
    }

    @OnClick("confirm_button")
    fun onConfirmClicked() {
        onConfirm(selectedParcel)
        dialog.dismiss()
    }

    private fun setupDialogSize() {
        val displayMetrics = context.resources.displayMetrics
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