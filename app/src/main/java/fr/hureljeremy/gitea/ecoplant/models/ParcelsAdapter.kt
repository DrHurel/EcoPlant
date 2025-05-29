package fr.hureljeremy.gitea.ecoplant.models

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.widget.SwitchCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.LinearLayoutManager
import fr.hureljeremy.gitea.ecoplant.R

class ParcelsAdapter(private val parcelItems: List<ParcelItem>, private val onItemClick: ((ParcelItem) -> Unit)? = null) :
    RecyclerView.Adapter<ParcelsAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val parcelTitle: TextView = itemView.findViewById(R.id.parcel_title)
        val service1: TextView = itemView.findViewById(R.id.services_1)
        val service2: TextView = itemView.findViewById(R.id.services_2)
        val service3: TextView = itemView.findViewById(R.id.services_3)
        val fiabilityScoreInput: EditText = itemView.findViewById(R.id.user_name_edit_text)
        val visibilitySwitch: SwitchCompat = itemView.findViewById(R.id.visibility_switch)
        val manageUsersButton: Button = itemView.findViewById(R.id.manage_users_button)
        val deleteButton: Button = itemView.findViewById(R.id.delete_button)
        val saveButton: Button = itemView.findViewById(R.id.save_button)
        val identificationRecyclerView: RecyclerView = itemView.findViewById(R.id.identification_parcels_recycler_view)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.parcels_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = parcelItems[position]

        holder.parcelTitle.hint = item.title

        // Configurer les services
        if (item.services.isNotEmpty() && item.services.size >= 1) {
            holder.service1.hint = item.services[0].service
        }
        if (item.services.size >= 2) {
            holder.service2.hint = item.services[1].service
        }
        if (item.services.size >= 3) {
            holder.service3.hint = item.services[2].service
        }

        // Configurer le score de fiabilité
        holder.fiabilityScoreInput.hint = item.minimumReliabilityScore.toString()

        // Configurer le switch de visibilité
        holder.visibilitySwitch.isChecked = item.isPublic

        // Configurer le RecyclerView d'identification
        val identificationItems = listOf(
            IdentificationParcelItem(1, "GPS", "43.123, 5.456"),
            IdentificationParcelItem(2, "Surface", "2500 m²"),
            IdentificationParcelItem(3, "Type", "Verger")
        )

        val identificationAdapter = IdentificationParcelsAdapter(identificationItems)
        holder.identificationRecyclerView.layoutManager = LinearLayoutManager(holder.itemView.context)
        holder.identificationRecyclerView.adapter = identificationAdapter

        // Configurer les boutons
        holder.manageUsersButton.setOnClickListener {
            // Action pour gérer les utilisateurs
        }

        holder.deleteButton.setOnClickListener {
            // Action pour supprimer
        }

        holder.saveButton.setOnClickListener {
            // Action pour sauvegarder
        }

        holder.itemView.setOnClickListener {
            onItemClick?.invoke(item)
        }
    }

    override fun getItemCount() = parcelItems.size
}