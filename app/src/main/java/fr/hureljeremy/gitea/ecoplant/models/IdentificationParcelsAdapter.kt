package fr.hureljeremy.gitea.ecoplant.models

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import fr.hureljeremy.gitea.ecoplant.databinding.IdentificationParcelsItemBinding

class IdentificationParcelsAdapter(
    private val items: List<IdentificationParcelItem>,
    private val onItemClick: ((IdentificationParcelItem) -> Unit)? = null
) : RecyclerView.Adapter<IdentificationParcelsAdapter.ViewHolder>() {

    class ViewHolder(binding: IdentificationParcelsItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        val textView = binding.identificationText
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = IdentificationParcelsItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.textView.hint = "${item.label}: ${item.value}"

        holder.itemView.setOnClickListener {
            onItemClick?.invoke(item)
        }
    }

    override fun getItemCount() = items.size
}