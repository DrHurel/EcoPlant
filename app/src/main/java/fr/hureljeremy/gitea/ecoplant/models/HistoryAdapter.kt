package fr.hureljeremy.gitea.ecoplant.models


import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import fr.hureljeremy.gitea.ecoplant.R
import fr.hureljeremy.gitea.ecoplant.databinding.HistoryItemBinding
import java.io.File


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

        // Gestion de l'image
        if (item.imageUrl != null) {

            val imageUri = item.imageUrl
            // Vérifier si l'image existe encore
            if (isImageExists(imageUri, holder.itemView)) {
                holder.image.setImageURI(imageUri)
            } else {
                // Image par défaut si l'image n'existe plus
                holder.image.setImageResource(R.drawable.ic_launcher_background)
            }

        } else {
            holder.image.setImageResource(R.drawable.ic_launcher_background)
        }

        holder.itemView.setOnClickListener {
            onItemClick?.invoke(item)
        }
    }

    private fun isImageExists(uri: Uri, view: View): Boolean {
        return try {
            when (uri.scheme) {
                "content" -> {
                    val context = view.context
                    val cursor = context.contentResolver.query(uri, null, null, null, null)
                    val exists = cursor?.use { it.moveToFirst() && it.count > 0 } ?: false
                    exists
                }

                "file" -> {
                    val file = File(uri.path ?: "")
                    file.exists()
                }

                else -> false
            }
        } catch (e: Exception) {
            false
        }
    }

    override fun getItemCount() = historyItems.size
}