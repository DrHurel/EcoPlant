package fr.hureljeremy.gitea.ecoplant.models

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.recyclerview.widget.RecyclerView
import fr.hureljeremy.gitea.ecoplant.databinding.ParcelsItemBinding
import fr.hureljeremy.gitea.ecoplant.framework.ParcelItem
import fr.hureljeremy.gitea.ecoplant.framework.ServiceEntry
import fr.hureljeremy.gitea.ecoplant.services.ParcelService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ParcelsAdapter(
    private val parcelItems: List<ParcelItem>,
    private val parcelService: ParcelService,
    private val lifecycleScope: LifecycleCoroutineScope,
    private val onItemClick: ((ParcelItem) -> Unit)? = null,
    private val onDeleteClick: ((ParcelItem) -> Unit)? = null,
    private val onSaveClick: ((ParcelItem, String, Double, Boolean) -> Unit)? = null,
    private val onManageUsersClick: ((ParcelItem) -> Unit)? = null
) : RecyclerView.Adapter<ParcelsAdapter.ViewHolder>() {

    class ViewHolder(binding: ParcelsItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        val parcelTitle = binding.parcelTitle
        val reliabilityScoreInput = binding.userNameEditText
        val visibilitySwitch = binding.visibilitySwitch
        val deleteButton = binding.deleteButton
        val saveButton = binding.saveButton
        val manageUsersButton = binding.manageUsersButton
        val identificationRecyclerView = binding.identificationParcelsRecyclerView
        val service1 = binding.services1
        val service2 = binding.services2
        val service3 = binding.services3
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ParcelsItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = parcelItems[position]

        holder.parcelTitle.hint = item.title
        holder.reliabilityScoreInput.hint = item.minimumReliabilityScore.toString()
        holder.visibilitySwitch.isChecked = item.isPublic

        // Configurer les boutons
        setupButtons(holder, item)

        // Afficher les services par défaut comme vides ou avec un indicateur de chargement
        holder.service1.hint = "Chargement..."
        holder.service2.hint = ""
        holder.service3.hint = ""

        // Charger les données de manière asynchrone
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val services = parcelService.getService(item)

                withContext(Dispatchers.Main) {
                    updateServicesDisplay(holder, services, item)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    holder.service1.hint = "Erreur de chargement"
                    holder.service2.hint = ""
                    holder.service3.hint = ""
                }
            }
        }
    }

    private fun updateServicesDisplay(
        holder: ViewHolder,
        services: List<ServiceEntry>,
        item: ParcelItem
    ) {
        // Ajouter les coordonnées GPS comme premier service si disponibles
        val displayServices = mutableListOf<String>()

        // Ajouter jusqu'à 3 services de la liste
        services.take(3 - displayServices.size).forEach { result ->
            displayServices.add(result.service)
        }

        // Mettre à jour l'affichage des services
        holder.service1.hint = displayServices.getOrNull(0) ?: ""
        holder.service2.hint = displayServices.getOrNull(1) ?: ""
        holder.service3.hint = displayServices.getOrNull(2) ?: ""
    }

    private fun setupButtons(holder: ViewHolder, item: ParcelItem) {
        holder.manageUsersButton.setOnClickListener {
            onManageUsersClick?.invoke(item)
        }

        holder.deleteButton.setOnClickListener {
            onDeleteClick?.invoke(item)
        }

        holder.saveButton.setOnClickListener {
            val newTitle =
                holder.parcelTitle.text.toString().ifEmpty { holder.parcelTitle.hint.toString() }
            val newReliabilityScore = holder.reliabilityScoreInput.text.toString().toDoubleOrNull()
                ?: holder.reliabilityScoreInput.hint.toString().toDoubleOrNull()
                ?: 50.0
            val isPublic = holder.visibilitySwitch.isChecked

            onSaveClick?.invoke(item, newTitle, newReliabilityScore, isPublic)
        }

        holder.itemView.setOnClickListener {
            onItemClick?.invoke(item)
        }
    }

    override fun getItemCount() = parcelItems.size
}