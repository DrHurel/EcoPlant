package fr.hureljeremy.gitea.ecoplant

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class FindParcelAdapter(
    private var parcelItems: List<FindParcelItem>,
    private val onItemClick: (FindParcelItem) -> Unit
) : RecyclerView.Adapter<FindParcelAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textView: TextView = itemView.findViewById(R.id.parcel_name)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.find_parcel_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = parcelItems[position]
        holder.textView.hint = item.name
        holder.itemView.setOnClickListener {
            onItemClick(item)
        }
    }

    override fun getItemCount() = parcelItems.size

    fun updateData(newList: List<FindParcelItem>) {
        parcelItems = newList
        notifyDataSetChanged()
    }
}