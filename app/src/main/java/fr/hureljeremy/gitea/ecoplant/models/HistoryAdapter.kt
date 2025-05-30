package fr.hureljeremy.gitea.ecoplant.models

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import fr.hureljeremy.gitea.ecoplant.R
import fr.hureljeremy.gitea.ecoplant.databinding.HistoryItemBinding

class HistoryAdapter(
    private val historyItems: List<HistoryItem>,
    private val onItemClick: ((HistoryItem) -> Unit)? = null
) : RecyclerView.Adapter<HistoryAdapter.ViewHolder>() {

    class ViewHolder(binding: HistoryItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        val title = binding.historyTitle
        val image = binding.historyImage
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = HistoryItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = historyItems[position]
        holder.title.hint = item.name

        // Chargement de l'image (utiliser Glide ou autre bibliothèque)
        if (item.imageUrl != null) {
            // Si vous utilisez Glide:
            // Glide.with(holder.image.context).load(item.imageUrl).into(holder.image)
        } else {
            holder.image.setImageResource(R.drawable.ic_launcher_background)
        }

        holder.itemView.setOnClickListener {
            onItemClick?.invoke(item)
        }
    }

    override fun getItemCount() = historyItems.size
}