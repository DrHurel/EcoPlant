package fr.hureljeremy.gitea.ecoplant.models

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import fr.hureljeremy.gitea.ecoplant.databinding.ParcelsItemBinding

class ParcelsAdapter(
    private val parcelItems: List<ParcelItem>,
    private val onItemClick: ((ParcelItem) -> Unit)? = null
) :
    RecyclerView.Adapter<ParcelsAdapter.ViewHolder>() {


    class ViewHolder(binding: ParcelsItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        // Accès direct aux vues via le binding
        val parcelTitle = binding.parcelTitle
        val service1 = binding.services1
        val service2 = binding.services2
        val service3 = binding.services3
        val reliabilityScoreInput = binding.userNameEditText
        val visibilitySwitch = binding.visibilitySwitch
        val manageUsersButton = binding.manageUsersButton
        val deleteButton = binding.deleteButton
        val saveButton = binding.saveButton
        val identificationRecyclerView = binding.identificationParcelsRecyclerView
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ParcelsItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = parcelItems[position]

        holder.parcelTitle.hint = item.title

        // Configurer les services
        if (item.services.isNotEmpty()) {
            holder.service1.hint = item.services[0].service
        }
        if (item.services.size >= 2) {
            holder.service2.hint = item.services[1].service
        }
        if (item.services.size >= 3) {
            holder.service3.hint = item.services[2].service
        }

        // Configurer le score de fiabilité
        holder.reliabilityScoreInput.hint = item.minimumReliabilityScore.toString()

        // Configurer le switch de visibilité
        holder.visibilitySwitch.isChecked = item.isPublic

        // Configurer le RecyclerView d'identification
        val identificationItems = listOf(
            IdentificationParcelItem(1, "GPS", "43.123, 5.456"),
            IdentificationParcelItem(2, "Surface", "2500 m²"),
            IdentificationParcelItem(3, "Type", "Verger")
        )

        val identificationAdapter = IdentificationParcelsAdapter(identificationItems)
        holder.identificationRecyclerView.layoutManager =
            LinearLayoutManager(holder.itemView.context)
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