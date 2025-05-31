package fr.hureljeremy.gitea.ecoplant.models

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import fr.hureljeremy.gitea.ecoplant.databinding.FindParcelItemBinding

class FindParcelAdapter(
    private var parcelItems: List<FindParcelItem>,
    private val onItemClick: ((FindParcelItem) -> Unit)? = null
) : RecyclerView.Adapter<FindParcelAdapter.ViewHolder>() {

    class ViewHolder(binding: FindParcelItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        val textView = binding.parcelName
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = FindParcelItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = parcelItems[position]
        holder.textView.hint = item.name

        holder.itemView.setOnClickListener {
            onItemClick?.invoke(item)
        }
    }

    override fun getItemCount() = parcelItems.size

    fun updateData(newList: List<FindParcelItem>) {
        parcelItems = newList
        notifyDataSetChanged()
    }
}