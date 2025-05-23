package fr.hureljeremy.gitea.ecoplant

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class HistoryAdapter(private val historyItems: List<HistoryItem>,private val onItemClick: ((HistoryItem) -> Unit)? = null) :

    RecyclerView.Adapter<HistoryAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val title: TextView = itemView.findViewById(R.id.history_title)
        val image: ImageView = itemView.findViewById(R.id.history_image)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.history_item, parent, false)
        return ViewHolder(view)
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
            onItemClick?.invoke(item) // Appel du callback avec l'élément cliqué
        }
    }

    override fun getItemCount() = historyItems.size
}